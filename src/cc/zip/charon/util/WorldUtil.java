package cc.zip.charon.util;

import com.mojang.authlib.GameProfile;
import java.util.Iterator;
import java.util.UUID;
import javax.annotation.Nullable;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.network.play.client.CPacketEntityAction.Action;
import net.minecraft.network.play.client.CPacketPlayer.Rotation;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class WorldUtil implements MinecraftInstance {
   public static void placeBlock(BlockPos pos) {
      EnumFacing[] var1 = EnumFacing.values();
      int var2 = var1.length;

      for(int var3 = 0; var3 < var2; ++var3) {
         EnumFacing enumFacing = var1[var3];
         if (!MinecraftInstance.mc.world.getBlockState(pos.offset(enumFacing)).getBlock().equals(Blocks.AIR) && !isIntercepted(pos)) {
            Vec3d vec = new Vec3d((double)pos.getX() + 0.5D + (double)enumFacing.getXOffset() * 0.5D, (double)pos.getY() + 0.5D + (double)enumFacing.getYOffset() * 0.5D, (double)pos.getZ() + 0.5D + (double)enumFacing.getZOffset() * 0.5D);
            float[] old = new float[]{MinecraftInstance.mc.player.rotationYaw, MinecraftInstance.mc.player.rotationPitch};
            MinecraftInstance.mc.player.connection.sendPacket(new Rotation((float)Math.toDegrees(Math.atan2(vec.z - MinecraftInstance.mc.player.posZ, vec.x - MinecraftInstance.mc.player.posX)) - 90.0F, (float)(-Math.toDegrees(Math.atan2(vec.y - (MinecraftInstance.mc.player.posY + (double)MinecraftInstance.mc.player.getEyeHeight()), Math.sqrt((vec.x - MinecraftInstance.mc.player.posX) * (vec.x - MinecraftInstance.mc.player.posX) + (vec.z - MinecraftInstance.mc.player.posZ) * (vec.z - MinecraftInstance.mc.player.posZ))))), MinecraftInstance.mc.player.onGround));
            MinecraftInstance.mc.player.connection.sendPacket(new CPacketEntityAction(MinecraftInstance.mc.player, Action.START_SNEAKING));
            MinecraftInstance.mc.playerController.processRightClickBlock(MinecraftInstance.mc.player, MinecraftInstance.mc.world, pos.offset(enumFacing), enumFacing.getOpposite(), new Vec3d(pos), EnumHand.MAIN_HAND);
            MinecraftInstance.mc.player.swingArm(EnumHand.MAIN_HAND);
            MinecraftInstance.mc.player.connection.sendPacket(new CPacketEntityAction(MinecraftInstance.mc.player, Action.STOP_SNEAKING));
            MinecraftInstance.mc.player.connection.sendPacket(new Rotation(old[0], old[1], MinecraftInstance.mc.player.onGround));
            return;
         }
      }

   }

   public static void createFakePlayer(@Nullable String name, boolean copyInventory, boolean copyAngles, boolean health, boolean player, int entityID) {
      EntityOtherPlayerMP entity = player ? new EntityOtherPlayerMP(mc.world, mc.getSession().getProfile()) : new EntityOtherPlayerMP(mc.world, new GameProfile(UUID.fromString("70ee432d-0a96-4137-a2c0-37cc9df67f03"), name));
      entity.copyLocationAndAnglesFrom(mc.player);
      if (copyInventory) {
         entity.inventory.copyInventory(mc.player.inventory);
      }

      if (copyAngles) {
         entity.rotationYaw = mc.player.rotationYaw;
         entity.rotationYawHead = mc.player.rotationYawHead;
      }

      if (health) {
         entity.setHealth(mc.player.getHealth() + mc.player.getAbsorptionAmount());
      }

      mc.world.addEntityToWorld(entityID, entity);
   }

   public static void placeBlock(BlockPos pos, int slot) {
      if (slot != -1) {
         int prev = MinecraftInstance.mc.player.inventory.currentItem;
         MinecraftInstance.mc.player.inventory.currentItem = slot;
         placeBlock(pos);
         MinecraftInstance.mc.player.inventory.currentItem = prev;
      }
   }

   public static boolean isIntercepted(BlockPos pos) {
      Iterator var1 = MinecraftInstance.mc.world.loadedEntityList.iterator();

      Entity entity;
      do {
         if (!var1.hasNext()) {
            return false;
         }

         entity = (Entity)var1.next();
      } while(!(new AxisAlignedBB(pos)).intersects(entity.getEntityBoundingBox()));

      return true;
   }

   public static BlockPos GetLocalPlayerPosFloored() {
      return new BlockPos(Math.floor(MinecraftInstance.mc.player.posX), Math.floor(MinecraftInstance.mc.player.posY), Math.floor(MinecraftInstance.mc.player.posZ));
   }

   public static boolean canBreak(BlockPos pos) {
      return MinecraftInstance.mc.world.getBlockState(pos).getBlock().getBlockHardness(MinecraftInstance.mc.world.getBlockState(pos), MinecraftInstance.mc.world, pos) != -1.0F;
   }
}
