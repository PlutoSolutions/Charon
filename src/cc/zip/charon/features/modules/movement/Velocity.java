package cc.zip.charon.features.modules.movement;

import cc.zip.charon.features.modules.Module;
import cc.zip.charon.features.setting.Setting;
import cc.zip.charon.util.none.PacketEvents;
import cc.zip.charon.util.none.PushEvents;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.EntityFishHook;
import net.minecraft.network.play.server.SPacketEntityStatus;
import net.minecraft.network.play.server.SPacketEntityVelocity;
import net.minecraft.network.play.server.SPacketExplosion;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class Velocity extends Module {
   private static Velocity INSTANCE = new Velocity();
   public Setting<Boolean> knockBack = this.register(new Setting("KnockBack", true));
   public Setting<Boolean> noPush = this.register(new Setting("NoPush", true));
   public Setting<Float> horizontal = this.register(new Setting("Horizontal", 0.0F, 0.0F, 100.0F));
   public Setting<Float> vertical = this.register(new Setting("Vertical", 0.0F, 0.0F, 100.0F));
   public Setting<Boolean> explosions = this.register(new Setting("Explosions", true));
   public Setting<Boolean> bobbers = this.register(new Setting("Bobbers", true));
   public Setting<Boolean> water = this.register(new Setting("Water", false));
   public Setting<Boolean> blocks = this.register(new Setting("Blocks", false));
   public Setting<Boolean> ice = this.register(new Setting("Ice", false));

   public Velocity() {
      super("Velocity", "Allows you to control your velocity", Module.Category.MOVEMENT, true, false, false);
      this.setInstance();
   }

   public static Velocity getINSTANCE() {
      if (INSTANCE == null) {
         INSTANCE = new Velocity();
      }

      return INSTANCE;
   }

   private void setInstance() {
      INSTANCE = this;
   }

   @SubscribeEvent
   public void onPacketReceived(PacketEvents.Receive event) {
      if (event.getStage() == 0 && mc.player != null) {
         SPacketEntityVelocity velocity;
         if ((Boolean)this.knockBack.getValue() && event.getPacket() instanceof SPacketEntityVelocity && (velocity = (SPacketEntityVelocity)event.getPacket()).getEntityID() == mc.player.entityId) {
            if ((Float)this.horizontal.getValue() == 0.0F && (Float)this.vertical.getValue() == 0.0F) {
               event.setCanceled(true);
               return;
            }

            velocity.motionX = (int)((float)velocity.motionX * (Float)this.horizontal.getValue());
            velocity.motionY = (int)((float)velocity.motionY * (Float)this.vertical.getValue());
            velocity.motionZ = (int)((float)velocity.motionZ * (Float)this.horizontal.getValue());
         }

         Entity entity;
         SPacketEntityStatus packet;
         if (event.getPacket() instanceof SPacketEntityStatus && (Boolean)this.bobbers.getValue() && (packet = (SPacketEntityStatus)event.getPacket()).getOpCode() == 31 && (entity = packet.getEntity(mc.world)) instanceof EntityFishHook) {
            EntityFishHook fishHook = (EntityFishHook)entity;
            if (fishHook.caughtEntity == mc.player) {
               event.setCanceled(true);
            }
         }

         if ((Boolean)this.explosions.getValue() && event.getPacket() instanceof SPacketExplosion) {
            SPacketExplosion velocity_ = (SPacketExplosion)event.getPacket();
            velocity_.motionX *= (Float)this.horizontal.getValue();
            velocity_.motionY *= (Float)this.vertical.getValue();
            velocity_.motionZ *= (Float)this.horizontal.getValue();
         }
      }

   }

   @SubscribeEvent
   public void onPush(PushEvents event) {
      if (event.getStage() == 0 && (Boolean)this.noPush.getValue() && event.entity.equals(mc.player)) {
         if ((Float)this.horizontal.getValue() == 0.0F && (Float)this.vertical.getValue() == 0.0F) {
            event.setCanceled(true);
            return;
         }

         event.x = -event.x * (double)(Float)this.horizontal.getValue();
         event.y = -event.y * (double)(Float)this.vertical.getValue();
         event.z = -event.z * (double)(Float)this.horizontal.getValue();
      } else if (event.getStage() == 1 && (Boolean)this.blocks.getValue()) {
         event.setCanceled(true);
      } else if (event.getStage() == 2 && (Boolean)this.water.getValue() && mc.player != null && mc.player.equals(event.entity)) {
         event.setCanceled(true);
      }

   }
}
