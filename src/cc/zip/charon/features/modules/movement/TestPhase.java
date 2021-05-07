package cc.zip.charon.features.modules.movement;

import cc.zip.charon.event.events.MoveEvent;
import cc.zip.charon.event.events.PacketEvent;
import cc.zip.charon.event.events.PushEvent;
import cc.zip.charon.event.events.UpdateWalkingPlayerEvent;
import cc.zip.charon.features.modules.Module;
import cc.zip.charon.features.setting.Setting;
import cc.zip.charon.util.EntityUtil;
import cc.zip.charon.util.Timer;
import io.netty.util.internal.ConcurrentSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.client.gui.GuiDownloadTerrain;
import net.minecraft.network.play.client.CPacketConfirmTeleport;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.network.play.client.CPacketPlayer.Position;
import net.minecraft.network.play.server.SPacketPlayerPosLook;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class TestPhase extends Module {
   private static TestPhase instance;
   private final Set<CPacketPlayer> packets = new ConcurrentSet();
   private final Map<Integer, TestPhase.IDtime> teleportmap = new ConcurrentHashMap();
   public Setting<Boolean> flight = this.register(new Setting("Flight", true));
   public Setting<Integer> flightMode = this.register(new Setting("FMode", 0, 0, 1));
   public Setting<Boolean> doAntiFactor = this.register(new Setting("Factorize", true));
   public Setting<Double> antiFactor = this.register(new Setting("AntiFactor", 2.5D, 0.1D, 3.0D));
   public Setting<Double> extraFactor = this.register(new Setting("ExtraFactor", 1.0D, 0.1D, 3.0D));
   public Setting<Boolean> strafeFactor = this.register(new Setting("StrafeFactor", true));
   public Setting<Integer> loops = this.register(new Setting("Loops", 1, 1, 10));
   public Setting<Boolean> clearTeleMap = this.register(new Setting("ClearMap", true));
   public Setting<Integer> mapTime = this.register(new Setting("ClearTime", 30, 1, 500));
   public Setting<Boolean> clearIDs = this.register(new Setting("ClearIDs", true));
   public Setting<Boolean> setYaw = this.register(new Setting("SetYaw", true));
   public Setting<Boolean> setID = this.register(new Setting("SetID", true));
   public Setting<Boolean> setMove = this.register(new Setting("SetMove", false));
   public Setting<Boolean> nocliperino = this.register(new Setting("NoClip", false));
   public Setting<Boolean> sendTeleport = this.register(new Setting("Teleport", true));
   public Setting<Boolean> resetID = this.register(new Setting("ResetID", true));
   public Setting<Boolean> setPos = this.register(new Setting("SetPos", false));
   public Setting<Boolean> invalidPacket = this.register(new Setting("InvalidPacket", true));
   private int flightCounter = 0;
   private int teleportID = 0;

   public TestPhase() {
      super("Packetfly", "Uses packets to fly!", Module.Category.MOVEMENT, true, false, false);
      instance = this;
   }

   public static TestPhase getInstance() {
      if (instance == null) {
         instance = new TestPhase();
      }

      return instance;
   }

   public void onToggle() {
   }

   public void onTick() {
      this.teleportmap.entrySet().removeIf((idTime) -> {
         return (Boolean)this.clearTeleMap.getValue() && ((TestPhase.IDtime)idTime.getValue()).getTimer().passedS((double)(Integer)this.mapTime.getValue());
      });
   }

   @SubscribeEvent
   public void onUpdateWalkingPlayer(UpdateWalkingPlayerEvent event) {
      if (event.getStage() != 1) {
         mc.player.setVelocity(0.0D, 0.0D, 0.0D);
         double speed = 0.0D;
         boolean checkCollisionBoxes = this.checkHitBoxes();
         speed = mc.player.movementInput.jump && (checkCollisionBoxes || !EntityUtil.isMoving()) ? ((Boolean)this.flight.getValue() && !checkCollisionBoxes ? ((Integer)this.flightMode.getValue() == 0 ? (this.resetCounter(10) ? -0.032D : 0.062D) : (this.resetCounter(20) ? -0.032D : 0.062D)) : 0.062D) : (mc.player.movementInput.sneak ? -0.062D : (!checkCollisionBoxes ? (this.resetCounter(4) ? ((Boolean)this.flight.getValue() ? -0.04D : 0.0D) : 0.0D) : 0.0D));
         if ((Boolean)this.doAntiFactor.getValue() && checkCollisionBoxes && EntityUtil.isMoving() && speed != 0.0D) {
            speed /= (Double)this.antiFactor.getValue();
         }

         double[] strafing = this.getMotion((Boolean)this.strafeFactor.getValue() && checkCollisionBoxes ? 0.031D : 0.26D);

         for(int i = 1; i < (Integer)this.loops.getValue() + 1; ++i) {
            mc.player.motionX = strafing[0] * (double)i * (Double)this.extraFactor.getValue();
            mc.player.motionY = speed * (double)i;
            mc.player.motionZ = strafing[1] * (double)i * (Double)this.extraFactor.getValue();
            this.sendPackets(mc.player.motionX, mc.player.motionY, mc.player.motionZ, (Boolean)this.sendTeleport.getValue());
         }

      }
   }

   @SubscribeEvent
   public void onMove(MoveEvent event) {
      if ((Boolean)this.setMove.getValue() && this.flightCounter != 0) {
         event.setX(mc.player.motionX);
         event.setY(mc.player.motionY);
         event.setZ(mc.player.motionZ);
         if ((Boolean)this.nocliperino.getValue() && this.checkHitBoxes()) {
            mc.player.noClip = true;
         }
      }

   }

   @SubscribeEvent
   public void onPacketSend(PacketEvent.Send event) {
      if (event.getPacket() instanceof CPacketPlayer && !this.packets.remove((CPacketPlayer)event.getPacket())) {
         event.setCanceled(true);
      }

   }

   @SubscribeEvent
   public void onPushOutOfBlocks(PushEvent event) {
      if (event.getStage() == 1) {
         event.setCanceled(true);
      }

   }

   @SubscribeEvent
   public void onPacketReceive(PacketEvent.Receive event) {
      if (event.getPacket() instanceof SPacketPlayerPosLook && !fullNullCheck()) {
         SPacketPlayerPosLook packet = (SPacketPlayerPosLook)event.getPacket();
         if (mc.player.isEntityAlive() && mc.world.isBlockLoaded(new BlockPos(mc.player.posX, mc.player.posY, mc.player.posZ), false) && !(mc.currentScreen instanceof GuiDownloadTerrain) && (Boolean)this.clearIDs.getValue()) {
            this.teleportmap.remove(packet.getTeleportId());
         }

         if ((Boolean)this.setYaw.getValue()) {
            packet.yaw = mc.player.rotationYaw;
            packet.pitch = mc.player.rotationPitch;
         }

         if ((Boolean)this.setID.getValue()) {
            this.teleportID = packet.getTeleportId();
         }
      }

   }

   private boolean checkHitBoxes() {
      return !mc.world.getCollisionBoxes(mc.player, mc.player.getEntityBoundingBox().expand(-0.0625D, -0.0625D, -0.0625D)).isEmpty();
   }

   private boolean resetCounter(int counter) {
      if (++this.flightCounter >= counter) {
         this.flightCounter = 0;
         return true;
      } else {
         return false;
      }
   }

   private double[] getMotion(double speed) {
      float moveForward = mc.player.movementInput.moveForward;
      float moveStrafe = mc.player.movementInput.moveStrafe;
      float rotationYaw = mc.player.prevRotationYaw + (mc.player.rotationYaw - mc.player.prevRotationYaw) * mc.getRenderPartialTicks();
      if (moveForward != 0.0F) {
         if (moveStrafe > 0.0F) {
            rotationYaw += (float)(moveForward > 0.0F ? -45 : 45);
         } else if (moveStrafe < 0.0F) {
            rotationYaw += (float)(moveForward > 0.0F ? 45 : -45);
         }

         moveStrafe = 0.0F;
         if (moveForward > 0.0F) {
            moveForward = 1.0F;
         } else if (moveForward < 0.0F) {
            moveForward = -1.0F;
         }
      }

      double posX = (double)moveForward * speed * -Math.sin(Math.toRadians((double)rotationYaw)) + (double)moveStrafe * speed * Math.cos(Math.toRadians((double)rotationYaw));
      double posZ = (double)moveForward * speed * Math.cos(Math.toRadians((double)rotationYaw)) - (double)moveStrafe * speed * -Math.sin(Math.toRadians((double)rotationYaw));
      return new double[]{posX, posZ};
   }

   private void sendPackets(double x, double y, double z, boolean teleport) {
      Vec3d vec = new Vec3d(x, y, z);
      Vec3d position = mc.player.getPositionVector().add(vec);
      Vec3d outOfBoundsVec = this.outOfBoundsVec(vec, position);
      this.packetSender(new Position(position.x, position.y, position.z, mc.player.onGround));
      if ((Boolean)this.invalidPacket.getValue()) {
         this.packetSender(new Position(outOfBoundsVec.x, outOfBoundsVec.y, outOfBoundsVec.z, mc.player.onGround));
      }

      if ((Boolean)this.setPos.getValue()) {
         mc.player.setPosition(position.x, position.y, position.z);
      }

      this.teleportPacket(position, teleport);
   }

   private void teleportPacket(Vec3d pos, boolean shouldTeleport) {
      if (shouldTeleport) {
         mc.player.connection.sendPacket(new CPacketConfirmTeleport(++this.teleportID));
         this.teleportmap.put(this.teleportID, new TestPhase.IDtime(pos, new Timer()));
      }

   }

   private Vec3d outOfBoundsVec(Vec3d offset, Vec3d position) {
      return position.add(0.0D, 1337.0D, 0.0D);
   }

   private void packetSender(CPacketPlayer packet) {
      this.packets.add(packet);
      mc.player.connection.sendPacket(packet);
   }

   private void clean() {
      this.teleportmap.clear();
      this.flightCounter = 0;
      if ((Boolean)this.resetID.getValue()) {
         this.teleportID = 0;
      }

      this.packets.clear();
   }

   public static class IDtime {
      private final Vec3d pos;
      private final Timer timer;

      public IDtime(Vec3d pos, Timer timer) {
         this.pos = pos;
         this.timer = timer;
         this.timer.reset();
      }

      public Vec3d getPos() {
         return this.pos;
      }

      public Timer getTimer() {
         return this.timer;
      }
   }
}
