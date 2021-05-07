package cc.zip.charon.features.modules.movement;

import cc.zip.charon.event.events.MoveEvent;
import cc.zip.charon.event.events.PacketEvent;
import cc.zip.charon.event.events.UpdateWalkingPlayerEvent;
import cc.zip.charon.features.modules.Module;
import cc.zip.charon.features.setting.Setting;
import cc.zip.charon.util.MathUtil;
import cc.zip.charon.util.Timer;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.network.play.client.CPacketEntityAction.Action;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class ElytraFly extends Module {
   public Setting<ElytraFly.Mode> mode;
   public Setting<Integer> devMode;
   public Setting<Float> speed;
   public Setting<Float> vSpeed;
   public Setting<Float> hSpeed;
   public Setting<Float> glide;
   public Setting<Float> tooBeeSpeed;
   public Setting<Boolean> autoStart;
   public Setting<Boolean> disableInLiquid;
   public Setting<Boolean> infiniteDura;
   public Setting<Boolean> noKick;
   public Setting<Boolean> allowUp;
   public Setting<Boolean> lockPitch;
   private static ElytraFly INSTANCE = new ElytraFly();
   private final Timer timer;
   private final Timer bypassTimer;
   private boolean vertical;
   private Double posX;
   private Double flyHeight;
   private Double posZ;

   public ElytraFly() {
      super("ElytraFly", "Makes Elytra Flight better.", Module.Category.MOVEMENT, true, false, false);
      this.mode = this.register(new Setting("Mode", ElytraFly.Mode.FLY));
      this.devMode = this.register(new Setting("Type", 2, 1, 3, (v) -> {
         return this.mode.getValue() == ElytraFly.Mode.BETTER;
      }, "EventMode"));
      this.speed = this.register(new Setting("Speed", 1.0F, 0.0F, 10.0F, (v) -> {
         return this.mode.getValue() != ElytraFly.Mode.FLY && this.mode.getValue() != ElytraFly.Mode.BOOST && this.mode.getValue() != ElytraFly.Mode.BETTER;
      }, "The Speed."));
      this.vSpeed = this.register(new Setting("VSpeed", 0.3F, 0.0F, 10.0F, (v) -> {
         return this.mode.getValue() == ElytraFly.Mode.BETTER;
      }, "Vertical Speed"));
      this.hSpeed = this.register(new Setting("HSpeed", 1.0F, 0.0F, 10.0F, (v) -> {
         return this.mode.getValue() == ElytraFly.Mode.BETTER;
      }, "Horizontal Speed"));
      this.glide = this.register(new Setting("Glide", 1.0E-4F, 0.0F, 0.2F, (v) -> {
         return this.mode.getValue() == ElytraFly.Mode.BETTER;
      }, "Glide Speed"));
      this.tooBeeSpeed = this.register(new Setting("TooBeeSpeed", 1.8000001F, 1.0F, 2.0F, "Speed for flight on 2b2t"));
      this.autoStart = this.register(new Setting("AutoStart", true));
      this.disableInLiquid = this.register(new Setting("NoLiquid", true));
      this.infiniteDura = this.register(new Setting("InfiniteDura", false));
      this.noKick = this.register(new Setting("NoKick", false, (v) -> {
         return this.mode.getValue() == ElytraFly.Mode.SuolBypass2;
      }));
      this.allowUp = this.register(new Setting("AllowUp", true, (v) -> {
         return this.mode.getValue() == ElytraFly.Mode.BETTER;
      }));
      this.lockPitch = this.register(new Setting("LockPitch", false));
      this.timer = new Timer();
      this.bypassTimer = new Timer();
      this.setInstance();
   }

   private void setInstance() {
      INSTANCE = this;
   }

   public static ElytraFly getInstance() {
      if (INSTANCE == null) {
         INSTANCE = new ElytraFly();
      }

      return INSTANCE;
   }

   public void onEnable() {
      if (this.mode.getValue() == ElytraFly.Mode.BETTER && !(Boolean)this.autoStart.getValue() && (Integer)this.devMode.getValue() == 1) {
         mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, Action.START_FALL_FLYING));
      }

      this.flyHeight = null;
      this.posX = null;
      this.posZ = null;
   }

   public String getDisplayInfo() {
      return this.mode.currentEnumName();
   }

   public void onUpdate() {
   }

   @SubscribeEvent
   public void onSendPacket(PacketEvent.Send event) {
   }

   @SubscribeEvent
   public void onMove(MoveEvent event) {
   }

   private void setMoveSpeed(MoveEvent event, double speed) {
      double forward = (double)mc.player.movementInput.moveForward;
      double strafe = (double)mc.player.movementInput.moveStrafe;
      float yaw = mc.player.rotationYaw;
      if (forward == 0.0D && strafe == 0.0D) {
         event.setX(0.0D);
         event.setZ(0.0D);
         mc.player.motionX = 0.0D;
         mc.player.motionZ = 0.0D;
      } else {
         if (forward != 0.0D) {
            if (strafe > 0.0D) {
               yaw += (float)(forward > 0.0D ? -45 : 45);
            } else if (strafe < 0.0D) {
               yaw += (float)(forward > 0.0D ? 45 : -45);
            }

            strafe = 0.0D;
            if (forward > 0.0D) {
               forward = 1.0D;
            } else if (forward < 0.0D) {
               forward = -1.0D;
            }
         }

         double x = forward * speed * -Math.sin(Math.toRadians((double)yaw)) + strafe * speed * Math.cos(Math.toRadians((double)yaw));
         double z = forward * speed * Math.cos(Math.toRadians((double)yaw)) - strafe * speed * -Math.sin(Math.toRadians((double)yaw));
         event.setX(x);
         event.setZ(z);
         mc.player.motionX = x;
         mc.player.motionZ = z;
      }

   }

   public void onTick() {
      if (mc.player.isElytraFlying()) {
         switch((ElytraFly.Mode)this.mode.getValue()) {
         case BOOST:
            if (mc.player.isInWater()) {
               mc.getConnection().sendPacket(new CPacketEntityAction(mc.player, Action.START_FALL_FLYING));
               return;
            }

            EntityPlayerSP var10000;
            if (mc.gameSettings.keyBindJump.isKeyDown()) {
               var10000 = mc.player;
               var10000.motionY += 0.08D;
            } else if (mc.gameSettings.keyBindSneak.isKeyDown()) {
               var10000 = mc.player;
               var10000.motionY -= 0.04D;
            }

            float yaw;
            if (mc.gameSettings.keyBindForward.isKeyDown()) {
               yaw = (float)Math.toRadians((double)mc.player.rotationYaw);
               var10000 = mc.player;
               var10000.motionX -= (double)(MathHelper.sin(yaw) * 0.05F);
               var10000 = mc.player;
               var10000.motionZ += (double)(MathHelper.cos(yaw) * 0.05F);
            } else if (mc.gameSettings.keyBindBack.isKeyDown()) {
               yaw = (float)Math.toRadians((double)mc.player.rotationYaw);
               var10000 = mc.player;
               var10000.motionX += (double)(MathHelper.sin(yaw) * 0.05F);
               var10000 = mc.player;
               var10000.motionZ -= (double)(MathHelper.cos(yaw) * 0.05F);
            }
            break;
         case FLY:
            mc.player.capabilities.isFlying = true;
         }

      }
   }

   @SubscribeEvent
   public void onUpdateWalkingPlayer(UpdateWalkingPlayerEvent event) {
      if (mc.player.getItemStackFromSlot(EntityEquipmentSlot.CHEST).getItem() == Items.ELYTRA) {
         switch(event.getStage()) {
         case 0:
            if ((Boolean)this.disableInLiquid.getValue() && (mc.player.isInWater() || mc.player.isInLava())) {
               if (mc.player.isElytraFlying()) {
                  mc.getConnection().sendPacket(new CPacketEntityAction(mc.player, Action.START_FALL_FLYING));
               }

               return;
            }

            if ((Boolean)this.autoStart.getValue() && mc.gameSettings.keyBindJump.isKeyDown() && !mc.player.isElytraFlying() && mc.player.motionY < 0.0D && this.timer.passedMs(250L)) {
               mc.getConnection().sendPacket(new CPacketEntityAction(mc.player, Action.START_FALL_FLYING));
               this.timer.reset();
            }

            EntityPlayerSP var10000;
            if (this.mode.getValue() == ElytraFly.Mode.BETTER) {
               double[] dir = MathUtil.directionSpeed((Integer)this.devMode.getValue() == 1 ? (double)(Float)this.speed.getValue() : (double)(Float)this.hSpeed.getValue());
               switch((Integer)this.devMode.getValue()) {
               case 1:
                  mc.player.setVelocity(0.0D, 0.0D, 0.0D);
                  mc.player.jumpMovementFactor = (Float)this.speed.getValue();
                  if (mc.gameSettings.keyBindJump.isKeyDown()) {
                     var10000 = mc.player;
                     var10000.motionY += (double)(Float)this.speed.getValue();
                  }

                  if (mc.gameSettings.keyBindSneak.isKeyDown()) {
                     var10000 = mc.player;
                     var10000.motionY -= (double)(Float)this.speed.getValue();
                  }

                  if (mc.player.movementInput.moveStrafe == 0.0F && mc.player.movementInput.moveForward == 0.0F) {
                     mc.player.motionX = 0.0D;
                     mc.player.motionZ = 0.0D;
                     break;
                  }

                  mc.player.motionX = dir[0];
                  mc.player.motionZ = dir[1];
                  break;
               case 2:
                  if (!mc.player.isElytraFlying()) {
                     this.flyHeight = null;
                     return;
                  }

                  if (this.flyHeight == null) {
                     this.flyHeight = mc.player.posY;
                  }

                  if ((Boolean)this.noKick.getValue()) {
                     this.flyHeight = this.flyHeight - (double)(Float)this.glide.getValue();
                  }

                  this.posX = 0.0D;
                  this.posZ = 0.0D;
                  if (mc.player.movementInput.moveStrafe != 0.0F || mc.player.movementInput.moveForward != 0.0F) {
                     this.posX = dir[0];
                     this.posZ = dir[1];
                  }

                  if (mc.gameSettings.keyBindJump.isKeyDown()) {
                     this.flyHeight = mc.player.posY + (double)(Float)this.vSpeed.getValue();
                  }

                  if (mc.gameSettings.keyBindSneak.isKeyDown()) {
                     this.flyHeight = mc.player.posY - (double)(Float)this.vSpeed.getValue();
                  }

                  mc.player.setPosition(mc.player.posX + this.posX, this.flyHeight, mc.player.posZ + this.posZ);
                  mc.player.setVelocity(0.0D, 0.0D, 0.0D);
                  break;
               case 3:
                  if (!mc.player.isElytraFlying()) {
                     this.flyHeight = null;
                     this.posX = null;
                     this.posZ = null;
                     return;
                  }

                  if (this.flyHeight == null || this.posX == null || this.posX == 0.0D || this.posZ == null || this.posZ == 0.0D) {
                     this.flyHeight = mc.player.posY;
                     this.posX = mc.player.posX;
                     this.posZ = mc.player.posZ;
                  }

                  if ((Boolean)this.noKick.getValue()) {
                     this.flyHeight = this.flyHeight - (double)(Float)this.glide.getValue();
                  }

                  if (mc.player.movementInput.moveStrafe != 0.0F || mc.player.movementInput.moveForward != 0.0F) {
                     this.posX = this.posX + dir[0];
                     this.posZ = this.posZ + dir[1];
                  }

                  if ((Boolean)this.allowUp.getValue() && mc.gameSettings.keyBindJump.isKeyDown()) {
                     this.flyHeight = mc.player.posY + (double)((Float)this.vSpeed.getValue() / 10.0F);
                  }

                  if (mc.gameSettings.keyBindSneak.isKeyDown()) {
                     this.flyHeight = mc.player.posY - (double)((Float)this.vSpeed.getValue() / 10.0F);
                  }

                  mc.player.setPosition(this.posX, this.flyHeight, this.posZ);
                  mc.player.setVelocity(0.0D, 0.0D, 0.0D);
               }
            }

            double rotationYaw = Math.toRadians((double)mc.player.rotationYaw);
            if (mc.player.isElytraFlying()) {
               switch((ElytraFly.Mode)this.mode.getValue()) {
               case SuolBypass:
                  float speedScaled = (Float)this.speed.getValue() * 0.05F;
                  if (mc.gameSettings.keyBindJump.isKeyDown()) {
                     var10000 = mc.player;
                     var10000.motionY += (double)speedScaled;
                  }

                  if (mc.gameSettings.keyBindSneak.isKeyDown()) {
                     var10000 = mc.player;
                     var10000.motionY -= (double)speedScaled;
                  }

                  if (mc.gameSettings.keyBindForward.isKeyDown()) {
                     var10000 = mc.player;
                     var10000.motionX -= Math.sin(rotationYaw) * (double)speedScaled;
                     var10000 = mc.player;
                     var10000.motionZ += Math.cos(rotationYaw) * (double)speedScaled;
                  }

                  if (mc.gameSettings.keyBindBack.isKeyDown()) {
                     var10000 = mc.player;
                     var10000.motionX += Math.sin(rotationYaw) * (double)speedScaled;
                     var10000 = mc.player;
                     var10000.motionZ -= Math.cos(rotationYaw) * (double)speedScaled;
                  }
                  break;
               case SuolBypass2:
                  this.freezePlayer(mc.player);
                  this.runNoKick(mc.player);
                  double[] directionSpeedPacket = MathUtil.directionSpeed((double)(Float)this.speed.getValue());
                  if (mc.player.movementInput.jump) {
                     mc.player.motionY = (double)(Float)this.speed.getValue();
                  }

                  if (mc.player.movementInput.sneak) {
                     mc.player.motionY = (double)(-(Float)this.speed.getValue());
                  }

                  if (mc.player.movementInput.moveStrafe != 0.0F || mc.player.movementInput.moveForward != 0.0F) {
                     mc.player.motionX = directionSpeedPacket[0];
                     mc.player.motionZ = directionSpeedPacket[1];
                  }

                  mc.getConnection().sendPacket(new CPacketEntityAction(mc.player, Action.START_FALL_FLYING));
                  mc.getConnection().sendPacket(new CPacketEntityAction(mc.player, Action.START_FALL_FLYING));
               }
            }

            if ((Boolean)this.infiniteDura.getValue()) {
               mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, Action.START_FALL_FLYING));
            }
            break;
         case 1:
            if ((Boolean)this.infiniteDura.getValue()) {
               mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, Action.START_FALL_FLYING));
            }
         }

      }
   }

   private double[] forwardStrafeYaw(double forward, double strafe, double yaw) {
      double[] result = new double[]{forward, strafe, yaw};
      if ((forward != 0.0D || strafe != 0.0D) && forward != 0.0D) {
         if (strafe > 0.0D) {
            result[2] += (double)(forward > 0.0D ? -45 : 45);
         } else if (strafe < 0.0D) {
            result[2] += (double)(forward > 0.0D ? 45 : -45);
         }

         result[1] = 0.0D;
         if (forward > 0.0D) {
            result[0] = 1.0D;
         } else if (forward < 0.0D) {
            result[0] = -1.0D;
         }
      }

      return result;
   }

   private void freezePlayer(EntityPlayer player) {
      player.motionX = 0.0D;
      player.motionY = 0.0D;
      player.motionZ = 0.0D;
   }

   private void runNoKick(EntityPlayer player) {
      if ((Boolean)this.noKick.getValue() && !player.isElytraFlying() && player.ticksExisted % 4 == 0) {
         player.motionY = -0.03999999910593033D;
      }

   }

   public void onDisable() {
      if (!fullNullCheck() && !mc.player.capabilities.isCreativeMode) {
         mc.player.capabilities.isFlying = false;
      }
   }

   public static enum Mode {
      SuolBypass,
      SuolBypass2,
      BOOST,
      FLY,
      BETTER;
   }
}
