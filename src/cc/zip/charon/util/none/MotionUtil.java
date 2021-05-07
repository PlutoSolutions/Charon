package cc.zip.charon.util.none;

import cc.zip.charon.util.Util;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.potion.Potion;

public class MotionUtil implements Util {
   public static boolean isMoving(EntityLivingBase entity) {
      return entity.moveForward != 0.0F || entity.moveStrafing != 0.0F;
   }

   public static void setSpeed(EntityLivingBase entity, double speed) {
      double[] dir = forward(speed);
      entity.motionX = dir[0];
      entity.motionZ = dir[1];
   }

   public static double getBaseMoveSpeed() {
      double baseSpeed = 0.2873D;
      if (mc.player != null && mc.player.isPotionActive(Potion.getPotionById(1))) {
         int amplifier = mc.player.getActivePotionEffect(Potion.getPotionById(1)).getAmplifier();
         baseSpeed *= 1.0D + 0.2D * (double)(amplifier + 1);
      }

      return baseSpeed;
   }

   public static double getSpeed(EntityLivingBase entity) {
      return Math.sqrt(entity.motionX * entity.motionX + entity.motionZ * entity.motionZ);
   }

   public static double[] forward(double speed) {
      float forward = mc.player.movementInput.moveForward;
      float side = mc.player.movementInput.moveStrafe;
      float yaw = mc.player.prevRotationYaw + (mc.player.rotationYaw - mc.player.prevRotationYaw) * mc.getRenderPartialTicks();
      if (forward != 0.0F) {
         if (side > 0.0F) {
            yaw += (float)(forward > 0.0F ? -45 : 45);
         } else if (side < 0.0F) {
            yaw += (float)(forward > 0.0F ? 45 : -45);
         }

         side = 0.0F;
         if (forward > 0.0F) {
            forward = 1.0F;
         } else if (forward < 0.0F) {
            forward = -1.0F;
         }
      }

      double sin = Math.sin(Math.toRadians((double)(yaw + 90.0F)));
      double cos = Math.cos(Math.toRadians((double)(yaw + 90.0F)));
      double posX = (double)forward * speed * cos + (double)side * speed * sin;
      double posZ = (double)forward * speed * sin - (double)side * speed * cos;
      return new double[]{posX, posZ};
   }
}
