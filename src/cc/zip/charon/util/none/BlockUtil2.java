package cc.zip.charon.util.none;

import cc.zip.charon.features.command.Command;
import cc.zip.charon.util.EntityUtil;
import cc.zip.charon.util.RotationUtil;
import cc.zip.charon.util.Util;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import net.minecraft.block.Block;
import net.minecraft.block.BlockAir;
import net.minecraft.block.BlockDeadBush;
import net.minecraft.block.BlockFire;
import net.minecraft.block.BlockLiquid;
import net.minecraft.block.BlockSnow;
import net.minecraft.block.BlockTallGrass;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;

public class BlockUtil2 implements Util {
   public static List<Block> unSolidBlocks;

   public static List<BlockPos> getBlockSphere(float breakRange, Class clazz) {
      NonNullList positions = NonNullList.create();
      positions.addAll((Collection)getSphere(EntityUtil.getPlayerPos(Util.mc.player), breakRange, (int)breakRange, false, true, 0).stream().filter((pos) -> {
         return clazz.isInstance(Util.mc.world.getBlockState(pos).getBlock());
      }).collect(Collectors.toList()));
      return positions;
   }

   public static List<EnumFacing> getPossibleSides(BlockPos pos) {
      ArrayList<EnumFacing> facings = new ArrayList();
      EnumFacing[] var2 = EnumFacing.values();
      int var3 = var2.length;

      for(int var4 = 0; var4 < var3; ++var4) {
         EnumFacing side = var2[var4];
         BlockPos neighbour = pos.offset(side);
         if (Util.mc.world.getBlockState(neighbour).getBlock().canCollideCheck(Util.mc.world.getBlockState(neighbour), false) && !Util.mc.world.getBlockState(neighbour).getMaterial().isReplaceable()) {
            facings.add(side);
         }
      }

      return facings;
   }

   public static EnumFacing getFirstFacing(BlockPos pos) {
      Iterator<EnumFacing> iterator = getPossibleSides(pos).iterator();
      if (iterator.hasNext()) {
         EnumFacing facing = (EnumFacing)iterator.next();
         return facing;
      } else {
         return null;
      }
   }

   public static EnumFacing getRayTraceFacing(BlockPos pos) {
      RayTraceResult result = Util.mc.world.rayTraceBlocks(new Vec3d(Util.mc.player.posX, Util.mc.player.posY + (double)Util.mc.player.getEyeHeight(), Util.mc.player.posZ), new Vec3d((double)pos.getX() + 0.5D, (double)pos.getX() - 0.5D, (double)pos.getX() + 0.5D));
      return result != null && result.sideHit != null ? result.sideHit : EnumFacing.UP;
   }

   public static int isPositionPlaceable(BlockPos pos, boolean rayTrace) {
      return isPositionPlaceable(pos, rayTrace, true);
   }

   public static int isPositionPlaceable(BlockPos pos, boolean rayTrace, boolean entityCheck) {
      Block block = Util.mc.world.getBlockState(pos).getBlock();
      if (!(block instanceof BlockAir) && !(block instanceof BlockLiquid) && !(block instanceof BlockTallGrass) && !(block instanceof BlockFire) && !(block instanceof BlockDeadBush) && !(block instanceof BlockSnow)) {
         return 0;
      } else if (!rayTracePlaceCheck(pos, rayTrace, 0.0F)) {
         return -1;
      } else {
         Iterator var4;
         if (entityCheck) {
            var4 = Util.mc.world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(pos)).iterator();

            while(var4.hasNext()) {
               Entity entity = (Entity)var4.next();
               if (!(entity instanceof EntityItem) && !(entity instanceof EntityXPOrb)) {
                  return 1;
               }
            }
         }

         var4 = getPossibleSides(pos).iterator();

         EnumFacing side;
         do {
            if (!var4.hasNext()) {
               return 2;
            }

            side = (EnumFacing)var4.next();
         } while(!canBeClicked(pos.offset(side)));

         return 3;
      }
   }

   public static void rightClickBlock(BlockPos pos, Vec3d vec, EnumHand hand, EnumFacing direction, boolean packet) {
      if (packet) {
         float f = (float)(vec.x - (double)pos.getX());
         float f1 = (float)(vec.y - (double)pos.getY());
         float f2 = (float)(vec.z - (double)pos.getZ());
         Util.mc.player.connection.sendPacket(new CPacketPlayerTryUseItemOnBlock(pos, direction, hand, f, f1, f2));
      } else {
         Util.mc.playerController.processRightClickBlock(Util.mc.player, Util.mc.world, pos, direction, vec, hand);
      }

      Util.mc.player.swingArm(EnumHand.MAIN_HAND);
      Util.mc.rightClickDelayTimer = 4;
   }

   public static Vec3d[] getHelpingBlocks(Vec3d vec3d) {
      return new Vec3d[]{new Vec3d(vec3d.x, vec3d.y - 1.0D, vec3d.z), new Vec3d(vec3d.x != 0.0D ? vec3d.x * 2.0D : vec3d.x, vec3d.y, vec3d.x != 0.0D ? vec3d.z : vec3d.z * 2.0D), new Vec3d(vec3d.x == 0.0D ? vec3d.x + 1.0D : vec3d.x, vec3d.y, vec3d.x == 0.0D ? vec3d.z : vec3d.z + 1.0D), new Vec3d(vec3d.x == 0.0D ? vec3d.x - 1.0D : vec3d.x, vec3d.y, vec3d.x == 0.0D ? vec3d.z : vec3d.z - 1.0D), new Vec3d(vec3d.x, vec3d.y + 1.0D, vec3d.z)};
   }

   public static List<BlockPos> possiblePlacePositions(float placeRange) {
      NonNullList positions = NonNullList.create();
      positions.addAll((Collection)getSphere(EntityUtil.getPlayerPos(Util.mc.player), placeRange, (int)placeRange, false, true, 0).stream().filter(BlockUtil2::canPlaceCrystal).collect(Collectors.toList()));
      return positions;
   }

   public static List<BlockPos> getSphere(BlockPos pos, float r, int h, boolean hollow, boolean sphere, int plus_y) {
      ArrayList<BlockPos> circleblocks = new ArrayList();
      int cx = pos.getX();
      int cy = pos.getY();
      int cz = pos.getZ();

      for(int x = cx - (int)r; (float)x <= (float)cx + r; ++x) {
         for(int z = cz - (int)r; (float)z <= (float)cz + r; ++z) {
            int y = sphere ? cy - (int)r : cy;

            while(true) {
               float f = (float)y;
               float f2 = sphere ? (float)cy + r : (float)(cy + h);
               if (!(f < f2)) {
                  break;
               }

               double dist = (double)((cx - x) * (cx - x) + (cz - z) * (cz - z) + (sphere ? (cy - y) * (cy - y) : 0));
               if (dist < (double)(r * r) && (!hollow || !(dist < (double)((r - 1.0F) * (r - 1.0F))))) {
                  BlockPos l = new BlockPos(x, y + plus_y, z);
                  circleblocks.add(l);
               }

               ++y;
            }
         }
      }

      return circleblocks;
   }

   public static List<BlockPos> getDisc(BlockPos pos, float r) {
      ArrayList<BlockPos> circleblocks = new ArrayList();
      int cx = pos.getX();
      int cy = pos.getY();
      int cz = pos.getZ();

      for(int x = cx - (int)r; (float)x <= (float)cx + r; ++x) {
         for(int z = cz - (int)r; (float)z <= (float)cz + r; ++z) {
            double dist = (double)((cx - x) * (cx - x) + (cz - z) * (cz - z));
            if (dist < (double)(r * r)) {
               BlockPos position = new BlockPos(x, cy, z);
               circleblocks.add(position);
            }
         }
      }

      return circleblocks;
   }

   public static boolean canPlaceCrystal(BlockPos blockPos) {
      BlockPos boost = blockPos.add(0, 1, 0);
      BlockPos boost2 = blockPos.add(0, 2, 0);

      try {
         return (Util.mc.world.getBlockState(blockPos).getBlock() == Blocks.BEDROCK || Util.mc.world.getBlockState(blockPos).getBlock() == Blocks.OBSIDIAN) && Util.mc.world.getBlockState(boost).getBlock() == Blocks.AIR && Util.mc.world.getBlockState(boost2).getBlock() == Blocks.AIR && Util.mc.world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(boost)).isEmpty() && Util.mc.world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(boost2)).isEmpty();
      } catch (Exception var4) {
         return false;
      }
   }

   public static List<BlockPos> possiblePlacePositions(float placeRange, boolean specialEntityCheck, boolean oneDot15) {
      NonNullList positions = NonNullList.create();
      positions.addAll((Collection)getSphere(EntityUtil.getPlayerPos(Util.mc.player), placeRange, (int)placeRange, false, true, 0).stream().filter((pos) -> {
         return canPlaceCrystal(pos, specialEntityCheck, oneDot15);
      }).collect(Collectors.toList()));
      return positions;
   }

   public static boolean canPlaceCrystal(BlockPos blockPos, boolean specialEntityCheck, boolean oneDot15) {
      BlockPos boost = blockPos.add(0, 1, 0);
      BlockPos boost2 = blockPos.add(0, 2, 0);

      try {
         if (Util.mc.world.getBlockState(blockPos).getBlock() != Blocks.BEDROCK && Util.mc.world.getBlockState(blockPos).getBlock() != Blocks.OBSIDIAN) {
            return false;
         } else if ((Util.mc.world.getBlockState(boost).getBlock() != Blocks.AIR || Util.mc.world.getBlockState(boost2).getBlock() != Blocks.AIR) && !oneDot15) {
            return false;
         } else if (specialEntityCheck) {
            Iterator var5 = Util.mc.world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(boost)).iterator();

            Entity entity;
            while(var5.hasNext()) {
               entity = (Entity)var5.next();
               if (!(entity instanceof EntityEnderCrystal)) {
                  return false;
               }
            }

            if (!oneDot15) {
               var5 = Util.mc.world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(boost2)).iterator();

               while(var5.hasNext()) {
                  entity = (Entity)var5.next();
                  if (!(entity instanceof EntityEnderCrystal)) {
                     return false;
                  }
               }
            }

            return true;
         } else {
            return Util.mc.world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(boost)).isEmpty() && (oneDot15 || Util.mc.world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(boost2)).isEmpty());
         }
      } catch (Exception var7) {
         return false;
      }
   }

   public static boolean canBeClicked(BlockPos pos) {
      return getBlock(pos).canCollideCheck(getState(pos), false);
   }

   private static Block getBlock(BlockPos pos) {
      return getState(pos).getBlock();
   }

   private static IBlockState getState(BlockPos pos) {
      return Util.mc.world.getBlockState(pos);
   }

   public static boolean isBlockAboveEntitySolid(Entity entity) {
      if (entity != null) {
         BlockPos pos = new BlockPos(entity.posX, entity.posY + 2.0D, entity.posZ);
         return isBlockSolid(pos);
      } else {
         return false;
      }
   }

   public static void debugPos(String message, BlockPos pos) {
      Command.sendMessage(message + pos.getX() + "x, " + pos.getY() + "y, " + pos.getZ() + "z");
   }

   public static void placeCrystalOnBlock(BlockPos pos, EnumHand hand) {
      RayTraceResult result = Util.mc.world.rayTraceBlocks(new Vec3d(Util.mc.player.posX, Util.mc.player.posY + (double)Util.mc.player.getEyeHeight(), Util.mc.player.posZ), new Vec3d((double)pos.getX() + 0.5D, (double)pos.getY() - 0.5D, (double)pos.getZ() + 0.5D));
      EnumFacing facing = result != null && result.sideHit != null ? result.sideHit : EnumFacing.UP;
      Util.mc.player.connection.sendPacket(new CPacketPlayerTryUseItemOnBlock(pos, facing, hand, 0.0F, 0.0F, 0.0F));
   }

   public static BlockPos[] toBlockPos(Vec3d[] vec3ds) {
      BlockPos[] list = new BlockPos[vec3ds.length];

      for(int i = 0; i < vec3ds.length; ++i) {
         list[i] = new BlockPos(vec3ds[i]);
      }

      return list;
   }

   public static Vec3d posToVec3d(BlockPos pos) {
      return new Vec3d(pos);
   }

   public static BlockPos vec3dToPos(Vec3d vec3d) {
      return new BlockPos(vec3d);
   }

   public static Boolean isPosInFov(BlockPos pos) {
      int dirnumber = RotationUtil.getDirection4D();
      if (dirnumber == 0 && (double)pos.getZ() - Util.mc.player.getPositionVector().z < 0.0D) {
         return false;
      } else if (dirnumber == 1 && (double)pos.getX() - Util.mc.player.getPositionVector().x > 0.0D) {
         return false;
      } else {
         return dirnumber == 2 && (double)pos.getZ() - Util.mc.player.getPositionVector().z > 0.0D ? false : dirnumber != 3 || !((double)pos.getX() - Util.mc.player.getPositionVector().x < 0.0D);
      }
   }

   public static boolean isBlockBelowEntitySolid(Entity entity) {
      if (entity != null) {
         BlockPos pos = new BlockPos(entity.posX, entity.posY - 1.0D, entity.posZ);
         return isBlockSolid(pos);
      } else {
         return false;
      }
   }

   public static boolean isBlockSolid(BlockPos pos) {
      return !isBlockUnSolid(pos);
   }

   public static boolean isBlockUnSolid(BlockPos pos) {
      return isBlockUnSolid(Util.mc.world.getBlockState(pos).getBlock());
   }

   public static boolean isBlockUnSolid(Block block) {
      return unSolidBlocks.contains(block);
   }

   public static Vec3d[] convertVec3ds(Vec3d vec3d, Vec3d[] input) {
      Vec3d[] output = new Vec3d[input.length];

      for(int i = 0; i < input.length; ++i) {
         output[i] = vec3d.add(input[i]);
      }

      return output;
   }

   public static Vec3d[] convertVec3ds(EntityPlayer entity, Vec3d[] input) {
      return convertVec3ds(entity.getPositionVector(), input);
   }

   public static boolean canBreak(BlockPos pos) {
      IBlockState blockState = Util.mc.world.getBlockState(pos);
      Block block = blockState.getBlock();
      return block.getBlockHardness(blockState, Util.mc.world, pos) != -1.0F;
   }

   public static boolean isValidBlock(BlockPos pos) {
      Block block = Util.mc.world.getBlockState(pos).getBlock();
      return !(block instanceof BlockLiquid) && block.getMaterial((IBlockState)null) != Material.AIR;
   }

   public static boolean isScaffoldPos(BlockPos pos) {
      return Util.mc.world.isAirBlock(pos) || Util.mc.world.getBlockState(pos).getBlock() == Blocks.SNOW_LAYER || Util.mc.world.getBlockState(pos).getBlock() == Blocks.TALLGRASS || Util.mc.world.getBlockState(pos).getBlock() instanceof BlockLiquid;
   }

   public static boolean rayTracePlaceCheck(BlockPos pos, boolean shouldCheck, float height) {
      return !shouldCheck || Util.mc.world.rayTraceBlocks(new Vec3d(Util.mc.player.posX, Util.mc.player.posY + (double)Util.mc.player.getEyeHeight(), Util.mc.player.posZ), new Vec3d((double)pos.getX(), (double)((float)pos.getY() + height), (double)pos.getZ()), false, true, false) == null;
   }

   public static boolean rayTracePlaceCheck(BlockPos pos, boolean shouldCheck) {
      return rayTracePlaceCheck(pos, shouldCheck, 1.0F);
   }

   public static boolean rayTracePlaceCheck(BlockPos pos) {
      return rayTracePlaceCheck(pos, true);
   }

   static {
      unSolidBlocks = Arrays.asList(Blocks.FLOWING_LAVA, Blocks.FLOWER_POT, Blocks.SNOW, Blocks.CARPET, Blocks.END_ROD, Blocks.SKULL, Blocks.FLOWER_POT, Blocks.TRIPWIRE, Blocks.TRIPWIRE_HOOK, Blocks.WOODEN_BUTTON, Blocks.LEVER, Blocks.STONE_BUTTON, Blocks.LADDER, Blocks.UNPOWERED_COMPARATOR, Blocks.POWERED_COMPARATOR, Blocks.UNPOWERED_REPEATER, Blocks.POWERED_REPEATER, Blocks.UNLIT_REDSTONE_TORCH, Blocks.REDSTONE_TORCH, Blocks.REDSTONE_WIRE, Blocks.AIR, Blocks.PORTAL, Blocks.END_PORTAL, Blocks.WATER, Blocks.FLOWING_WATER, Blocks.LAVA, Blocks.FLOWING_LAVA, Blocks.SAPLING, Blocks.RED_FLOWER, Blocks.YELLOW_FLOWER, Blocks.BROWN_MUSHROOM, Blocks.RED_MUSHROOM, Blocks.WHEAT, Blocks.CARROTS, Blocks.POTATOES, Blocks.BEETROOTS, Blocks.REEDS, Blocks.PUMPKIN_STEM, Blocks.MELON_STEM, Blocks.WATERLILY, Blocks.NETHER_WART, Blocks.COCOA, Blocks.CHORUS_FLOWER, Blocks.CHORUS_PLANT, Blocks.TALLGRASS, Blocks.DEADBUSH, Blocks.VINE, Blocks.FIRE, Blocks.RAIL, Blocks.ACTIVATOR_RAIL, Blocks.DETECTOR_RAIL, Blocks.GOLDEN_RAIL, Blocks.TORCH);
   }
}
