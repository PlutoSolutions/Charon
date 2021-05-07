package cc.zip.charon.features.modules.combat;

import cc.zip.charon.event.events.PacketEvent;
import cc.zip.charon.features.modules.Module;
import cc.zip.charon.features.setting.Setting;
import cc.zip.charon.util.Timer;
import java.util.Objects;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.network.play.client.CPacketUseEntity;
import net.minecraft.network.play.client.CPacketPlayer.Position;
import net.minecraft.network.play.client.CPacketUseEntity.Action;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class Criticals extends Module {
   private final Setting<Integer> packets = this.register(new Setting("Packets", 2, 1, 4, "Amount of packets you want to send."));
   private final Timer timer = new Timer();
   private final boolean resetTimer = false;

   public Criticals() {
      super("Criticals", "Scores criticals for you", Module.Category.COMBAT, true, false, false);
   }

   @SubscribeEvent
   public void onPacketSend(PacketEvent.Send event) {
      CPacketUseEntity packet;
      if (event.getPacket() instanceof CPacketUseEntity && (packet = (CPacketUseEntity)event.getPacket()).getAction() == Action.ATTACK) {
         this.getClass();
         if (!this.timer.passedMs(0L)) {
            return;
         }

         if (mc.player.onGround && !mc.gameSettings.keyBindJump.isKeyDown() && packet.getEntityFromWorld(mc.world) instanceof EntityLivingBase && !mc.player.isInWater() && !mc.player.isInLava()) {
            switch((Integer)this.packets.getValue()) {
            case 1:
               mc.player.connection.sendPacket(new Position(mc.player.posX, mc.player.posY + 0.10000000149011612D, mc.player.posZ, false));
               mc.player.connection.sendPacket(new Position(mc.player.posX, mc.player.posY, mc.player.posZ, false));
               break;
            case 2:
               mc.player.connection.sendPacket(new Position(mc.player.posX, mc.player.posY + 0.0625101D, mc.player.posZ, false));
               mc.player.connection.sendPacket(new Position(mc.player.posX, mc.player.posY, mc.player.posZ, false));
               mc.player.connection.sendPacket(new Position(mc.player.posX, mc.player.posY + 1.1E-5D, mc.player.posZ, false));
               mc.player.connection.sendPacket(new Position(mc.player.posX, mc.player.posY, mc.player.posZ, false));
               break;
            case 3:
               mc.player.connection.sendPacket(new Position(mc.player.posX, mc.player.posY + 0.0625101D, mc.player.posZ, false));
               mc.player.connection.sendPacket(new Position(mc.player.posX, mc.player.posY, mc.player.posZ, false));
               mc.player.connection.sendPacket(new Position(mc.player.posX, mc.player.posY + 0.0125D, mc.player.posZ, false));
               mc.player.connection.sendPacket(new Position(mc.player.posX, mc.player.posY, mc.player.posZ, false));
               break;
            case 4:
               mc.player.connection.sendPacket(new Position(mc.player.posX, mc.player.posY + 0.1625D, mc.player.posZ, false));
               mc.player.connection.sendPacket(new Position(mc.player.posX, mc.player.posY, mc.player.posZ, false));
               mc.player.connection.sendPacket(new Position(mc.player.posX, mc.player.posY + 4.0E-6D, mc.player.posZ, false));
               mc.player.connection.sendPacket(new Position(mc.player.posX, mc.player.posY, mc.player.posZ, false));
               mc.player.connection.sendPacket(new Position(mc.player.posX, mc.player.posY + 1.0E-6D, mc.player.posZ, false));
               mc.player.connection.sendPacket(new Position(mc.player.posX, mc.player.posY, mc.player.posZ, false));
               mc.player.connection.sendPacket(new CPacketPlayer());
               mc.player.onCriticalHit((Entity)Objects.requireNonNull(packet.getEntityFromWorld(mc.world)));
            }

            this.timer.reset();
         }
      }

   }

   public String getDisplayInfo() {
      return "Packet";
   }
}
