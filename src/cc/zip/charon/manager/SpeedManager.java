package cc.zip.charon.manager;

import cc.zip.charon.features.Feature;
import java.util.HashMap;
import java.util.Iterator;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.MathHelper;

public class SpeedManager extends Feature {
   public static final double LAST_JUMP_INFO_DURATION_DEFAULT = 3.0D;
   public static boolean didJumpThisTick = false;
   public static boolean isJumping = false;
   private final int distancer = 20;
   public double firstJumpSpeed = 0.0D;
   public double lastJumpSpeed = 0.0D;
   public double percentJumpSpeedChanged = 0.0D;
   public double jumpSpeedChanged = 0.0D;
   public boolean didJumpLastTick = false;
   public long jumpInfoStartTime = 0L;
   public boolean wasFirstJump = true;
   public double speedometerCurrentSpeed = 0.0D;
   public HashMap<EntityPlayer, Double> playerSpeeds = new HashMap();

   public static void setDidJumpThisTick(boolean val) {
      didJumpThisTick = val;
   }

   public static void setIsJumping(boolean val) {
      isJumping = val;
   }

   public float lastJumpInfoTimeRemaining() {
      return (float)(Minecraft.getSystemTime() - this.jumpInfoStartTime) / 1000.0F;
   }

   public void updateValues() {
      double distTraveledLastTickX = mc.player.posX - mc.player.prevPosX;
      double distTraveledLastTickZ = mc.player.posZ - mc.player.prevPosZ;
      this.speedometerCurrentSpeed = distTraveledLastTickX * distTraveledLastTickX + distTraveledLastTickZ * distTraveledLastTickZ;
      if (didJumpThisTick && (!mc.player.onGround || isJumping)) {
         if (didJumpThisTick && !this.didJumpLastTick) {
            this.wasFirstJump = this.lastJumpSpeed == 0.0D;
            this.percentJumpSpeedChanged = this.speedometerCurrentSpeed != 0.0D ? this.speedometerCurrentSpeed / this.lastJumpSpeed - 1.0D : -1.0D;
            this.jumpSpeedChanged = this.speedometerCurrentSpeed - this.lastJumpSpeed;
            this.jumpInfoStartTime = Minecraft.getSystemTime();
            this.lastJumpSpeed = this.speedometerCurrentSpeed;
            this.firstJumpSpeed = this.wasFirstJump ? this.lastJumpSpeed : 0.0D;
         }

         this.didJumpLastTick = didJumpThisTick;
      } else {
         this.didJumpLastTick = false;
         this.lastJumpSpeed = 0.0D;
      }

      this.updatePlayers();
   }

   public void updatePlayers() {
      Iterator var1 = mc.world.playerEntities.iterator();

      while(var1.hasNext()) {
         EntityPlayer player = (EntityPlayer)var1.next();
         double var10000 = mc.player.getDistanceSq(player);
         this.getClass();
         this.getClass();
         if (var10000 < (double)(20 * 20)) {
            double distTraveledLastTickX = player.posX - player.prevPosX;
            double distTraveledLastTickZ = player.posZ - player.prevPosZ;
            double playerSpeed = distTraveledLastTickX * distTraveledLastTickX + distTraveledLastTickZ * distTraveledLastTickZ;
            this.playerSpeeds.put(player, playerSpeed);
         }
      }

   }

   public double getPlayerSpeed(EntityPlayer player) {
      return this.playerSpeeds.get(player) == null ? 0.0D : this.turnIntoKpH((Double)this.playerSpeeds.get(player));
   }

   public double turnIntoKpH(double input) {
      return (double)MathHelper.sqrt(input) * 71.2729367892D;
   }

   public double getSpeedKpH() {
      double speedometerkphdouble = this.turnIntoKpH(this.speedometerCurrentSpeed);
      speedometerkphdouble = (double)Math.round(10.0D * speedometerkphdouble) / 10.0D;
      return speedometerkphdouble;
   }

   public double getSpeedMpS() {
      double speedometerMpsdouble = this.turnIntoKpH(this.speedometerCurrentSpeed) / 3.6D;
      speedometerMpsdouble = (double)Math.round(10.0D * speedometerMpsdouble) / 10.0D;
      return speedometerMpsdouble;
   }
}
