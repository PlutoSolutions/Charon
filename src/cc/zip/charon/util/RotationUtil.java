package cc.zip.charon.util;

import cc.zip.charon.features.modules.client.ClickGui;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.play.client.CPacketPlayer.Rotation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

public class RotationUtil implements Util {
   public static Vec3d getEyesPos() {
      return new Vec3d(mc.player.posX, mc.player.posY + (double)mc.player.getEyeHeight(), mc.player.posZ);
   }

   public static double[] calculateLookAt(double px, double py, double pz, EntityPlayer me) {
      double dirx = me.posX - px;
      double diry = me.posY - py;
      double dirz = me.posZ - pz;
      double len = Math.sqrt(dirx * dirx + diry * diry + dirz * dirz);
      double pitch = Math.asin(diry / len);
      double yaw = Math.atan2(dirz / len, dirx / len);
      pitch = pitch * 180.0D / 3.141592653589793D;
      yaw = yaw * 180.0D / 3.141592653589793D;
      return new double[]{yaw += 90.0D, pitch};
   }

   public static float[] getLegitRotations(Vec3d vec) {
      Vec3d eyesPos = getEyesPos();
      double diffX = vec.x - eyesPos.x;
      double diffY = vec.y - eyesPos.y;
      double diffZ = vec.z - eyesPos.z;
      double diffXZ = Math.sqrt(diffX * diffX + diffZ * diffZ);
      float yaw = (float)Math.toDegrees(Math.atan2(diffZ, diffX)) - 90.0F;
      float pitch = (float)(-Math.toDegrees(Math.atan2(diffY, diffXZ)));
      return new float[]{mc.player.rotationYaw + MathHelper.wrapDegrees(yaw - mc.player.rotationYaw), mc.player.rotationPitch + MathHelper.wrapDegrees(pitch - mc.player.rotationPitch)};
   }

   public static void faceYawAndPitch(float yaw, float pitch) {
      mc.player.connection.sendPacket(new Rotation(yaw, pitch, mc.player.onGround));
   }

   public static boolean isInFov(BlockPos pos) {
      return pos != null && (mc.player.getDistanceSq(pos) < 4.0D || yawDist(pos) < (double)(getHalvedfov() + 2.0F));
   }

   public static boolean isInFov(Entity entity) {
      return entity != null && (mc.player.getDistanceSq(entity) < 4.0D || yawDist(entity) < (double)(getHalvedfov() + 2.0F));
   }

   public static double yawDist(BlockPos pos) {
      if (pos != null) {
         Vec3d difference = (new Vec3d(pos)).subtract(mc.player.getPositionEyes(mc.getRenderPartialTicks()));
         double d = Math.abs((double)mc.player.rotationYaw - (Math.toDegrees(Math.atan2(difference.z, difference.x)) - 90.0D)) % 360.0D;
         return d > 180.0D ? 360.0D - d : d;
      } else {
         return 0.0D;
      }
   }

   public static double yawDist(Entity e) {
      if (e != null) {
         Vec3d difference = e.getPositionVector().add(0.0D, (double)(e.getEyeHeight() / 2.0F), 0.0D).subtract(mc.player.getPositionEyes(mc.getRenderPartialTicks()));
         double d = Math.abs((double)mc.player.rotationYaw - (Math.toDegrees(Math.atan2(difference.z, difference.x)) - 90.0D)) % 360.0D;
         return d > 180.0D ? 360.0D - d : d;
      } else {
         return 0.0D;
      }
   }

   public static float getHalvedfov() {
      return getFov() / 2.0F;
   }

   public static boolean isInFov(Vec3d vec3d, Vec3d other) {
      if (mc.player.rotationPitch > 30.0F) {
         if (other.y > mc.player.posY) {
            return true;
         }
      } else if (mc.player.rotationPitch < -30.0F && other.y < mc.player.posY) {
         return true;
      }

      float angle = MathUtil.calcAngleNoY(vec3d, other)[0] - transformYaw();
      if (angle < -270.0F) {
         return true;
      } else {
         float fov = ((Boolean)ClickGui.getInstance().customFov.getValue() ? (Float)ClickGui.getInstance().fov.getValue() : mc.gameSettings.fovSetting) / 2.0F;
         return angle < fov + 10.0F && angle > -fov - 10.0F;
      }
   }

   public static float getFov() {
      return (Boolean)ClickGui.getInstance().customFov.getValue() ? (Float)ClickGui.getInstance().fov.getValue() : mc.gameSettings.fovSetting;
   }

   public static void faceVector(Vec3d vec, boolean normalizeAngle) {
      float[] rotations = getLegitRotations(vec);
      mc.player.connection.sendPacket(new Rotation(rotations[0], normalizeAngle ? (float)MathHelper.normalizeAngle((int)rotations[1], 360) : rotations[1], mc.player.onGround));
   }

   public static float transformYaw() {
      float yaw = mc.player.rotationYaw % 360.0F;
      if (mc.player.rotationYaw > 0.0F) {
         if (yaw > 180.0F) {
            yaw = -180.0F + (yaw - 180.0F);
         }
      } else if (yaw < -180.0F) {
         yaw = 180.0F + yaw + 180.0F;
      }

      return yaw < 0.0F ? 180.0F + yaw : -180.0F + yaw;
   }

   public static void faceEntity(Entity entity) {
      float[] angle = MathUtil.calcAngle(mc.player.getPositionEyes(mc.getRenderPartialTicks()), entity.getPositionEyes(mc.getRenderPartialTicks()));
      faceYawAndPitch(angle[0], angle[1]);
   }

   public static float[] getAngle(Entity entity) {
      return MathUtil.calcAngle(mc.player.getPositionEyes(mc.getRenderPartialTicks()), entity.getPositionEyes(mc.getRenderPartialTicks()));
   }

   public static int getDirection4D() {
      return MathHelper.floor((double)(mc.player.rotationYaw * 4.0F / 360.0F) + 0.5D) & 3;
   }

   public static String getDirection4D(boolean northRed) {
      int dirnumber = getDirection4D();
      if (dirnumber == 0) {
         return "South (+Z)";
      } else if (dirnumber == 1) {
         return "West (-X)";
      } else if (dirnumber == 2) {
         return (northRed ? "Â§c" : "") + "North (-Z)";
      } else {
         return dirnumber == 3 ? "East (+X)" : "Loading...";
      }
   }
}
