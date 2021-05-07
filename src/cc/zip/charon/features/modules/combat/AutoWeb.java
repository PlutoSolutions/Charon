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
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import net.minecraft.block.BlockWeb;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

public class AutoWeb extends Module {
   public static boolean isPlacing = false;
   private final Setting<Integer> delay = this.register(new Setting("Delay", 50, 0, 250));
   private final Setting<Integer> blocksPerPlace = this.register(new Setting("BlocksPerTick", 8, 1, 30));
   private final Setting<Boolean> packet = this.register(new Setting("PacketPlace", false));
   private final Setting<Boolean> disable = this.register(new Setting("AutoDisable", false));
   private final Setting<Boolean> rotate = this.register(new Setting("Rotate", true));
   private final Setting<Boolean> raytrace = this.register(new Setting("Raytrace", false));
   private final Setting<Boolean> lowerbody = this.register(new Setting("Feet", true));
   private final Setting<Boolean> upperBody = this.register(new Setting("Face", false));
   private final Timer timer = new Timer();
   public EntityPlayer target;
   private boolean didPlace = false;
   private boolean switchedItem;
   private boolean isSneaking;
   private int lastHotbarSlot;
   private int placements = 0;
   private boolean smartRotate = false;
   private BlockPos startPos = null;

   public AutoWeb() {
      super("AutoWeb", "Traps other players in webs", Module.Category.COMBAT, true, false, false);
   }

   public void onEnable() {
      if (!fullNullCheck()) {
         this.startPos = EntityUtil.getRoundedBlockPos(mc.player);
         this.lastHotbarSlot = mc.player.inventory.currentItem;
      }
   }

   public void onTick() {
      this.smartRotate = false;
      this.doTrap();
   }

   public String getDisplayInfo() {
      return this.target != null ? this.target.getName() : null;
   }

   public void onDisable() {
      isPlacing = false;
      this.isSneaking = EntityUtil.stopSneaking(this.isSneaking);
      this.switchItem(true);
   }

   private void doTrap() {
      if (!this.check()) {
         this.doWebTrap();
         if (this.didPlace) {
            this.timer.reset();
         }

      }
   }

   private void doWebTrap() {
      List<Vec3d> placeTargets = this.getPlacements();
      this.placeList(placeTargets);
   }

   private List<Vec3d> getPlacements() {
      ArrayList<Vec3d> list = new ArrayList();
      Vec3d baseVec = this.target.getPositionVector();
      if ((Boolean)this.lowerbody.getValue()) {
         list.add(baseVec);
      }

      if ((Boolean)this.upperBody.getValue()) {
         list.add(baseVec.add(0.0D, 1.0D, 0.0D));
      }

      return list;
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
         BlockPos position;
         int placeability;
         do {
            if (!var2.hasNext()) {
               return;
            }

            Vec3d vec3d3 = (Vec3d)var2.next();
            position = new BlockPos(vec3d3);
            placeability = BlockUtil.isPositionPlaceable(position, (Boolean)this.raytrace.getValue());
         } while(placeability != 3 && placeability != 1);

         this.placeBlock(position);
      }
   }

   private boolean check() {
      isPlacing = false;
      this.didPlace = false;
      this.placements = 0;
      int obbySlot = InventoryUtil.findHotbarBlock(BlockWeb.class);
      if (this.isOff()) {
         return true;
      } else if ((Boolean)this.disable.getValue() && !this.startPos.equals(EntityUtil.getRoundedBlockPos(mc.player))) {
         this.disable();
         return true;
      } else if (obbySlot == -1) {
         Command.sendMessage("<" + this.getDisplayName() + "> " + ChatFormatting.RED + "No Webs in hotbar disabling...");
         this.toggle();
         return true;
      } else {
         if (mc.player.inventory.currentItem != this.lastHotbarSlot && mc.player.inventory.currentItem != obbySlot) {
            this.lastHotbarSlot = mc.player.inventory.currentItem;
         }

         this.switchItem(true);
         this.isSneaking = EntityUtil.stopSneaking(this.isSneaking);
         this.target = this.getTarget(10.0D);
         return this.target == null || !this.timer.passedMs((long)(Integer)this.delay.getValue());
      }
   }

   private EntityPlayer getTarget(double range) {
      EntityPlayer target = null;
      double distance = Math.pow(range, 2.0D) + 1.0D;
      Iterator var6 = mc.world.playerEntities.iterator();

      while(var6.hasNext()) {
         EntityPlayer player = (EntityPlayer)var6.next();
         if (!EntityUtil.isntValid(player, range) && !player.isInWeb && !(Charon.speedManager.getPlayerSpeed(player) > 30.0D)) {
            if (target == null) {
               target = player;
               distance = mc.player.getDistanceSq(player);
            } else if (mc.player.getDistanceSq(player) < distance) {
               target = player;
               distance = mc.player.getDistanceSq(player);
            }
         }
      }

      return target;
   }

   private void placeBlock(BlockPos pos) {
      if (this.placements < (Integer)this.blocksPerPlace.getValue() && mc.player.getDistanceSq(pos) <= MathUtil.square(6.0D) && this.switchItem(false)) {
         isPlacing = true;
         this.isSneaking = this.smartRotate ? BlockUtil.placeBlockSmartRotate(pos, EnumHand.MAIN_HAND, true, (Boolean)this.packet.getValue(), this.isSneaking) : BlockUtil.placeBlock(pos, EnumHand.MAIN_HAND, (Boolean)this.rotate.getValue(), (Boolean)this.packet.getValue(), this.isSneaking);
         this.didPlace = true;
         ++this.placements;
      }

   }

   private boolean switchItem(boolean back) {
      boolean[] value = InventoryUtil.switchItem(back, this.lastHotbarSlot, this.switchedItem, InventoryUtil.Switch.NORMAL, BlockWeb.class);
      this.switchedItem = value[0];
      return value[1];
   }
}
