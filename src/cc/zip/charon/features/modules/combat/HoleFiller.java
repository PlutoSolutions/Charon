package cc.zip.charon.features.modules.combat;

import cc.zip.charon.features.modules.Module;
import cc.zip.charon.features.setting.Setting;
import cc.zip.charon.util.BlockUtil;
import cc.zip.charon.util.EntityUtil;
import cc.zip.charon.util.InventoryUtil;
import cc.zip.charon.util.TestUtil;
import cc.zip.charon.util.Timer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import net.minecraft.block.BlockEnderChest;
import net.minecraft.block.BlockObsidian;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;

public class HoleFiller extends Module {
   private static final BlockPos[] surroundOffset = BlockUtil.toBlockPos(EntityUtil.getOffsets(0, true));
   private static HoleFiller INSTANCE = new HoleFiller();
   private final Setting<Integer> range = this.register(new Setting("PlaceRange", 8, 0, 10));
   private final Setting<Integer> delay = this.register(new Setting("Delay", 50, 0, 250));
   private final Setting<Integer> blocksPerTick = this.register(new Setting("BlocksPerTick", 20, 8, 30));
   private final Timer offTimer = new Timer();
   private final Timer timer = new Timer();
   private final Map<BlockPos, Integer> retries = new HashMap();
   private final Timer retryTimer = new Timer();
   private int blocksThisTick = 0;
   private ArrayList<BlockPos> holes = new ArrayList();
   private int trie;

   public HoleFiller() {
      super("HoleFiller", "Fills holes around you.", Module.Category.COMBAT, true, false, true);
      this.setInstance();
   }

   public static HoleFiller getInstance() {
      if (INSTANCE == null) {
         INSTANCE = new HoleFiller();
      }

      return INSTANCE;
   }

   private void setInstance() {
      INSTANCE = this;
   }

   public void onEnable() {
      if (fullNullCheck()) {
         this.disable();
      }

      this.offTimer.reset();
      this.trie = 0;
   }

   public void onTick() {
      if (this.isOn()) {
         this.doHoleFill();
      }

   }

   public void onDisable() {
      this.retries.clear();
   }

   private void doHoleFill() {
      if (!this.check()) {
         this.holes = new ArrayList();
         Iterable<BlockPos> blocks = BlockPos.getAllInBox(mc.player.getPosition().add(-(Integer)this.range.getValue(), -(Integer)this.range.getValue(), -(Integer)this.range.getValue()), mc.player.getPosition().add((Integer)this.range.getValue(), (Integer)this.range.getValue(), (Integer)this.range.getValue()));
         Iterator var2 = blocks.iterator();

         while(true) {
            BlockPos pos;
            do {
               do {
                  if (!var2.hasNext()) {
                     this.holes.forEach(this::placeBlock);
                     this.toggle();
                     return;
                  }

                  pos = (BlockPos)var2.next();
               } while(mc.world.getBlockState(pos).getMaterial().blocksMovement());
            } while(mc.world.getBlockState(pos.add(0, 1, 0)).getMaterial().blocksMovement());

            boolean solidNeighbours = mc.world.getBlockState(pos.add(1, 0, 0)).getBlock() == Blocks.BEDROCK | mc.world.getBlockState(pos.add(1, 0, 0)).getBlock() == Blocks.OBSIDIAN && mc.world.getBlockState(pos.add(0, 0, 1)).getBlock() == Blocks.BEDROCK | mc.world.getBlockState(pos.add(0, 0, 1)).getBlock() == Blocks.OBSIDIAN && mc.world.getBlockState(pos.add(-1, 0, 0)).getBlock() == Blocks.BEDROCK | mc.world.getBlockState(pos.add(-1, 0, 0)).getBlock() == Blocks.OBSIDIAN && mc.world.getBlockState(pos.add(0, 0, -1)).getBlock() == Blocks.BEDROCK | mc.world.getBlockState(pos.add(0, 0, -1)).getBlock() == Blocks.OBSIDIAN && mc.world.getBlockState(pos.add(0, 0, 0)).getMaterial() == Material.AIR && mc.world.getBlockState(pos.add(0, 1, 0)).getMaterial() == Material.AIR && mc.world.getBlockState(pos.add(0, 2, 0)).getMaterial() == Material.AIR;
            if (solidNeighbours) {
               this.holes.add(pos);
            }
         }
      }
   }

   private void placeBlock(BlockPos pos) {
      Iterator var2 = mc.world.getEntitiesWithinAABBExcludingEntity((Entity)null, new AxisAlignedBB(pos)).iterator();

      Entity entity;
      do {
         if (!var2.hasNext()) {
            if (this.blocksThisTick < (Integer)this.blocksPerTick.getValue()) {
               int obbySlot = InventoryUtil.findHotbarBlock(BlockObsidian.class);
               int eChestSot = InventoryUtil.findHotbarBlock(BlockEnderChest.class);
               if (obbySlot == -1 && eChestSot == -1) {
                  this.toggle();
               }

               int originalSlot = mc.player.inventory.currentItem;
               mc.player.inventory.currentItem = obbySlot == -1 ? eChestSot : obbySlot;
               mc.playerController.updateController();
               TestUtil.placeBlock(pos);
               if (mc.player.inventory.currentItem != originalSlot) {
                  mc.player.inventory.currentItem = originalSlot;
                  mc.playerController.updateController();
               }

               this.timer.reset();
               ++this.blocksThisTick;
            }

            return;
         }

         entity = (Entity)var2.next();
      } while(!(entity instanceof EntityLivingBase));

   }

   private boolean check() {
      if (fullNullCheck()) {
         this.disable();
         return true;
      } else {
         this.blocksThisTick = 0;
         if (this.retryTimer.passedMs(2000L)) {
            this.retries.clear();
            this.retryTimer.reset();
         }

         return !this.timer.passedMs((long)(Integer)this.delay.getValue());
      }
   }
}
