package cc.zip.charon.features.modules.combat;

import cc.zip.charon.Charon;
import cc.zip.charon.features.command.Command;
import cc.zip.charon.features.modules.Module;
import cc.zip.charon.features.setting.Setting;
import cc.zip.charon.util.BlockUtil;
import cc.zip.charon.util.EntityUtil;
import cc.zip.charon.util.InventoryUtil;
import cc.zip.charon.util.MathUtil;
import cc.zip.charon.util.Timer;
import com.mojang.realmsclient.gui.ChatFormatting;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import net.minecraft.block.BlockEnderChest;
import net.minecraft.block.BlockObsidian;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class AutoTrap extends Module {
   public static boolean isPlacing = false;
   private final Setting<Integer> delay = this.register(new Setting("Delay", 50, 0, 250));
   private final Setting<Integer> blocksPerPlace = this.register(new Setting("BlocksPerTick", 8, 1, 30));
   private final Setting<Boolean> rotate = this.register(new Setting("Rotate", true));
   private final Setting<Boolean> raytrace = this.register(new Setting("Raytrace", false));
   private final Setting<Boolean> antiScaffold = this.register(new Setting("AntiScaffold", false));
   private final Setting<Boolean> antiStep = this.register(new Setting("AntiStep", false));
   private final Timer timer = new Timer();
   private final Map<BlockPos, Integer> retries = new HashMap();
   private final Timer retryTimer = new Timer();
   public EntityPlayer target;
   private boolean didPlace = false;
   private boolean switchedItem;
   private boolean isSneaking;
   private int lastHotbarSlot;
   private int placements = 0;
   private boolean smartRotate = false;
   private BlockPos startPos = null;

   public AutoTrap() {
      super("AutoTrap", "Traps other players", Module.Category.COMBAT, true, false, false);
   }

   public void onEnable() {
      if (!fullNullCheck()) {
         this.startPos = EntityUtil.getRoundedBlockPos(mc.player);
         this.lastHotbarSlot = mc.player.inventory.currentItem;
         this.retries.clear();
      }
   }

   public void onTick() {
      if (!fullNullCheck()) {
         this.smartRotate = false;
         this.doTrap();
      }
   }

   public String getDisplayInfo() {
      return this.target != null ? this.target.getName() : null;
   }

   public void onDisable() {
      isPlacing = false;
      this.isSneaking = EntityUtil.stopSneaking(this.isSneaking);
   }

   private void doTrap() {
      if (!this.check()) {
         this.doStaticTrap();
         if (this.didPlace) {
            this.timer.reset();
         }

      }
   }

   private void doStaticTrap() {
      List<Vec3d> placeTargets = EntityUtil.targets(this.target.getPositionVector(), (Boolean)this.antiScaffold.getValue(), (Boolean)this.antiStep.getValue(), false, false, false, (Boolean)this.raytrace.getValue());
      this.placeList(placeTargets);
   }

   private void placeList(List<Vec3d> list) {
      list.sort((vec3d, vec3d2) -> {
         return Double.compare(mc.player.getDistanceSq(vec3d2.x, vec3d2.y, vec3d2.z), mc.player.getDistanceSq(vec3d.x, vec3d.y, vec3d.z));
      });
      list.sort(Comparator.comparingDouble((vec3d) -> {
         return vec3d.y;
      }));
      Iterator var2 = list.iterator();

      while(true) {
         while(var2.hasNext()) {
            Vec3d vec3d3 = (Vec3d)var2.next();
            BlockPos position = new BlockPos(vec3d3);
            int placeability = BlockUtil.isPositionPlaceable(position, (Boolean)this.raytrace.getValue());
            if (placeability == 1 && (this.retries.get(position) == null || (Integer)this.retries.get(position) < 4)) {
               this.placeBlock(position);
               this.retries.put(position, this.retries.get(position) == null ? 1 : (Integer)this.retries.get(position) + 1);
               this.retryTimer.reset();
            } else if (placeability == 3) {
               this.placeBlock(position);
            }
         }

         return;
      }
   }

   private boolean check() {
      isPlacing = false;
      this.didPlace = false;
      this.placements = 0;
      int obbySlot2 = InventoryUtil.findHotbarBlock(BlockObsidian.class);
      if (obbySlot2 == -1) {
         this.toggle();
      }

      int obbySlot = InventoryUtil.findHotbarBlock(BlockObsidian.class);
      if (this.isOff()) {
         return true;
      } else if (!this.startPos.equals(EntityUtil.getRoundedBlockPos(mc.player))) {
         this.disable();
         return true;
      } else {
         if (this.retryTimer.passedMs(2000L)) {
            this.retries.clear();
            this.retryTimer.reset();
         }

         if (obbySlot == -1) {
            Command.sendMessage("<" + this.getDisplayName() + "> " + ChatFormatting.RED + "No Obsidian in hotbar disabling...");
            this.disable();
            return true;
         } else {
            if (mc.player.inventory.currentItem != this.lastHotbarSlot && mc.player.inventory.currentItem != obbySlot) {
               this.lastHotbarSlot = mc.player.inventory.currentItem;
            }

            this.isSneaking = EntityUtil.stopSneaking(this.isSneaking);
            this.target = this.getTarget(10.0D, true);
            return this.target == null || !this.timer.passedMs((long)(Integer)this.delay.getValue());
         }
      }
   }

   private EntityPlayer getTarget(double range, boolean trapped) {
      EntityPlayer target = null;
      double distance = Math.pow(range, 2.0D) + 1.0D;
      Iterator var7 = mc.world.playerEntities.iterator();

      while(true) {
         EntityPlayer player;
         do {
            do {
               if (!var7.hasNext()) {
                  return target;
               }

               player = (EntityPlayer)var7.next();
            } while(EntityUtil.isntValid(player, range));
         } while(trapped && EntityUtil.isTrapped(player, (Boolean)this.antiScaffold.getValue(), (Boolean)this.antiStep.getValue(), false, false, false));

         if (!(Charon.speedManager.getPlayerSpeed(player) > 10.0D)) {
            if (target == null) {
               target = player;
               distance = mc.player.getDistanceSq(player);
            } else if (mc.player.getDistanceSq(player) < distance) {
               target = player;
               distance = mc.player.getDistanceSq(player);
            }
         }
      }
   }

   private void placeBlock(BlockPos pos) {
      if (this.placements < (Integer)this.blocksPerPlace.getValue() && mc.player.getDistanceSq(pos) <= MathUtil.square(5.0D)) {
         isPlacing = true;
         int originalSlot = mc.player.inventory.currentItem;
         int obbySlot = InventoryUtil.findHotbarBlock(BlockObsidian.class);
         int eChestSot = InventoryUtil.findHotbarBlock(BlockEnderChest.class);
         if (obbySlot == -1 && eChestSot == -1) {
            this.toggle();
         }

         if (this.smartRotate) {
            mc.player.inventory.currentItem = obbySlot == -1 ? eChestSot : obbySlot;
            mc.playerController.updateController();
            this.isSneaking = BlockUtil.placeBlockSmartRotate(pos, EnumHand.MAIN_HAND, true, true, this.isSneaking);
            mc.player.inventory.currentItem = originalSlot;
            mc.playerController.updateController();
         } else {
            mc.player.inventory.currentItem = obbySlot == -1 ? eChestSot : obbySlot;
            mc.playerController.updateController();
            this.isSneaking = BlockUtil.placeBlock(pos, EnumHand.MAIN_HAND, (Boolean)this.rotate.getValue(), true, this.isSneaking);
            mc.player.inventory.currentItem = originalSlot;
            mc.playerController.updateController();
         }

         this.didPlace = true;
         ++this.placements;
      }

   }
}
