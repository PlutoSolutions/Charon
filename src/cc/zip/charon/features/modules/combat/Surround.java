package cc.zip.charon.features.modules.combat;

import cc.zip.charon.Charon;
import cc.zip.charon.features.command.Command;
import cc.zip.charon.features.modules.Module;
import cc.zip.charon.features.setting.Setting;
import cc.zip.charon.util.BlockUtil;
import cc.zip.charon.util.EntityUtil;
import cc.zip.charon.util.InventoryUtil;
import cc.zip.charon.util.Timer;
import cc.zip.charon.util.Util;
import com.mojang.realmsclient.gui.ChatFormatting;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import net.minecraft.block.BlockEnderChest;
import net.minecraft.block.BlockObsidian;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class Surround extends Module {
   public static boolean isPlacing = false;
   private final Setting<Integer> blocksPerTick = this.register(new Setting("BlocksPerTick", 12, 1, 20));
   private final Setting<Integer> delay = this.register(new Setting("Delay", 0, 0, 250));
   private final Setting<Boolean> noGhost = this.register(new Setting("PacketPlace", false));
   private final Setting<Boolean> center = this.register(new Setting("TPCenter", false));
   private final Setting<Boolean> rotate = this.register(new Setting("Rotate", true));
   private final Timer timer = new Timer();
   private final Timer retryTimer = new Timer();
   private final Set<Vec3d> extendingBlocks = new HashSet();
   private final Map<BlockPos, Integer> retries = new HashMap();
   private int isSafe;
   private BlockPos startPos;
   private boolean didPlace = false;
   private boolean switchedItem;
   private int lastHotbarSlot;
   private boolean isSneaking;
   private int placements = 0;
   private int extenders = 1;
   private int obbySlot = -1;
   private boolean offHand = false;

   public Surround() {
      super("Surround", "Surrounds you with Obsidian", Module.Category.COMBAT, true, false, false);
   }

   public void onEnable() {
      if (fullNullCheck()) {
         this.disable();
      }

      this.lastHotbarSlot = Util.mc.player.inventory.currentItem;
      this.startPos = EntityUtil.getRoundedBlockPos(Util.mc.player);
      if ((Boolean)this.center.getValue()) {
         Charon.positionManager.setPositionPacket((double)this.startPos.getX() + 0.5D, (double)this.startPos.getY(), (double)this.startPos.getZ() + 0.5D, true, true, true);
      }

      this.retries.clear();
      this.retryTimer.reset();
   }

   public void onTick() {
      this.doFeetPlace();
   }

   public void onDisable() {
      if (!nullCheck()) {
         isPlacing = false;
         this.isSneaking = EntityUtil.stopSneaking(this.isSneaking);
      }
   }

   public String getDisplayInfo() {
      switch(this.isSafe) {
      case 0:
         return ChatFormatting.RED + "Unsafe";
      case 1:
         return ChatFormatting.YELLOW + "Safe";
      default:
         return ChatFormatting.GREEN + "Safe";
      }
   }

   private void doFeetPlace() {
      if (!this.check()) {
         if (!EntityUtil.isSafe(Util.mc.player, 0, true)) {
            this.isSafe = 0;
            this.placeBlocks(Util.mc.player.getPositionVector(), EntityUtil.getUnsafeBlockArray(Util.mc.player, 0, true), true, false, false);
         } else if (!EntityUtil.isSafe(Util.mc.player, -1, false)) {
            this.isSafe = 1;
            this.placeBlocks(Util.mc.player.getPositionVector(), EntityUtil.getUnsafeBlockArray(Util.mc.player, -1, false), false, false, true);
         } else {
            this.isSafe = 2;
         }

         this.processExtendingBlocks();
         if (this.didPlace) {
            this.timer.reset();
         }

      }
   }

   private void processExtendingBlocks() {
      if (this.extendingBlocks.size() == 2 && this.extenders < 1) {
         Vec3d[] array = new Vec3d[2];
         int i = 0;

         for(Iterator iterator = this.extendingBlocks.iterator(); iterator.hasNext(); ++i) {
            array[i] = (Vec3d)iterator.next();
         }

         int placementsBefore = this.placements;
         if (this.areClose(array) != null) {
            this.placeBlocks(this.areClose(array), EntityUtil.getUnsafeBlockArrayFromVec3d(this.areClose(array), 0, true), true, false, true);
         }

         if (placementsBefore < this.placements) {
            this.extendingBlocks.clear();
         }
      } else if (this.extendingBlocks.size() > 2 || this.extenders >= 1) {
         this.extendingBlocks.clear();
      }

   }

   private Vec3d areClose(Vec3d[] vec3ds) {
      int matches = 0;
      Vec3d[] var3 = vec3ds;
      int var4 = vec3ds.length;

      for(int var5 = 0; var5 < var4; ++var5) {
         Vec3d vec3d = var3[var5];
         Vec3d[] var7 = EntityUtil.getUnsafeBlockArray(Util.mc.player, 0, true);
         int var8 = var7.length;

         for(int var9 = 0; var9 < var8; ++var9) {
            Vec3d pos = var7[var9];
            if (vec3d.equals(pos)) {
               ++matches;
            }
         }
      }

      if (matches == 2) {
         return Util.mc.player.getPositionVector().add(vec3ds[0].add(vec3ds[1]));
      } else {
         return null;
      }
   }

   private boolean placeBlocks(Vec3d pos, Vec3d[] vec3ds, boolean hasHelpingBlocks, boolean isHelping, boolean isExtending) {
      boolean gotHelp = true;
      Vec3d[] var7 = vec3ds;
      int var8 = vec3ds.length;

      for(int var9 = 0; var9 < var8; ++var9) {
         Vec3d vec3d = var7[var9];
         gotHelp = true;
         BlockPos position = (new BlockPos(pos)).add(vec3d.x, vec3d.y, vec3d.z);
         switch(BlockUtil.isPositionPlaceable(position, false)) {
         case 1:
            if (this.retries.get(position) != null && (Integer)this.retries.get(position) >= 4) {
               if (Charon.speedManager.getSpeedKpH() == 0.0D && !isExtending && this.extenders < 1) {
                  this.placeBlocks(Util.mc.player.getPositionVector().add(vec3d), EntityUtil.getUnsafeBlockArrayFromVec3d(Util.mc.player.getPositionVector().add(vec3d), 0, true), hasHelpingBlocks, false, true);
                  this.extendingBlocks.add(vec3d);
                  ++this.extenders;
               }
               break;
            }

            this.placeBlock(position);
            this.retries.put(position, this.retries.get(position) == null ? 1 : (Integer)this.retries.get(position) + 1);
            this.retryTimer.reset();
            break;
         case 2:
            if (!hasHelpingBlocks) {
               break;
            }

            gotHelp = this.placeBlocks(pos, BlockUtil.getHelpingBlocks(vec3d), false, true, true);
         case 3:
            if (gotHelp) {
               this.placeBlock(position);
            }

            if (isHelping) {
               return true;
            }
         }
      }

      return false;
   }

   private boolean check() {
      if (nullCheck()) {
         return true;
      } else {
         int obbySlot = InventoryUtil.findHotbarBlock(BlockObsidian.class);
         int eChestSot = InventoryUtil.findHotbarBlock(BlockEnderChest.class);
         if (obbySlot == -1 && eChestSot == -1) {
            this.toggle();
         }

         this.offHand = InventoryUtil.isBlock(Util.mc.player.getHeldItemOffhand().getItem(), BlockObsidian.class);
         isPlacing = false;
         this.didPlace = false;
         this.extenders = 1;
         this.placements = 0;
         this.obbySlot = InventoryUtil.findHotbarBlock(BlockObsidian.class);
         int echestSlot = InventoryUtil.findHotbarBlock(BlockEnderChest.class);
         if (this.isOff()) {
            return true;
         } else {
            if (this.retryTimer.passedMs(2500L)) {
               this.retries.clear();
               this.retryTimer.reset();
            }

            if (this.obbySlot == -1 && !this.offHand && echestSlot == -1) {
               Command.sendMessage("<" + this.getDisplayName() + "> " + ChatFormatting.RED + "No Obsidian in hotbar disabling...");
               this.disable();
               return true;
            } else {
               this.isSneaking = EntityUtil.stopSneaking(this.isSneaking);
               if (Util.mc.player.inventory.currentItem != this.lastHotbarSlot && Util.mc.player.inventory.currentItem != this.obbySlot && Util.mc.player.inventory.currentItem != echestSlot) {
                  this.lastHotbarSlot = Util.mc.player.inventory.currentItem;
               }

               if (!this.startPos.equals(EntityUtil.getRoundedBlockPos(Util.mc.player))) {
                  this.disable();
                  return true;
               } else {
                  return !this.timer.passedMs((long)(Integer)this.delay.getValue());
               }
            }
         }
      }
   }

   private void placeBlock(BlockPos pos) {
      if (this.placements < (Integer)this.blocksPerTick.getValue()) {
         int originalSlot = Util.mc.player.inventory.currentItem;
         int obbySlot = InventoryUtil.findHotbarBlock(BlockObsidian.class);
         int eChestSot = InventoryUtil.findHotbarBlock(BlockEnderChest.class);
         if (obbySlot == -1 && eChestSot == -1) {
            this.toggle();
         }

         isPlacing = true;
         Util.mc.player.inventory.currentItem = obbySlot == -1 ? eChestSot : obbySlot;
         Util.mc.playerController.updateController();
         this.isSneaking = BlockUtil.placeBlock(pos, this.offHand ? EnumHand.OFF_HAND : EnumHand.MAIN_HAND, (Boolean)this.rotate.getValue(), (Boolean)this.noGhost.getValue(), this.isSneaking);
         Util.mc.player.inventory.currentItem = originalSlot;
         Util.mc.playerController.updateController();
         this.didPlace = true;
         ++this.placements;
      }

   }
}
