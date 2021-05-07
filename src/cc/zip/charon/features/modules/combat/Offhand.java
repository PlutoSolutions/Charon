package cc.zip.charon.features.modules.combat;

import cc.zip.charon.event.events.PacketEvent;
import cc.zip.charon.event.events.ProcessRightClickBlockEvent;
import cc.zip.charon.features.modules.Module;
import cc.zip.charon.features.setting.Setting;
import cc.zip.charon.util.EntityUtil;
import cc.zip.charon.util.InventoryUtil;
import cc.zip.charon.util.Timer;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import net.minecraft.block.BlockObsidian;
import net.minecraft.block.BlockWeb;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.init.Items;
import net.minecraft.inventory.EntityEquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.network.play.client.CPacketPlayerTryUseItem;
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock;
import net.minecraft.util.EnumHand;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.input.Mouse;

public class Offhand extends Module {
   private static Offhand instance;
   private final Queue<InventoryUtil.Task> taskList = new ConcurrentLinkedQueue();
   private final Timer timer = new Timer();
   private final Timer secondTimer = new Timer();
   public Setting<Boolean> crystal = this.register(new Setting("Crystal", true));
   public Setting<Float> crystalHealth = this.register(new Setting("CrystalHP", 13.0F, 0.1F, 36.0F));
   public Setting<Float> crystalHoleHealth = this.register(new Setting("CrystalHoleHP", 3.5F, 0.1F, 36.0F));
   public Setting<Boolean> gapple = this.register(new Setting("Gapple", true));
   public Setting<Boolean> armorCheck = this.register(new Setting("ArmorCheck", true));
   public Setting<Integer> actions = this.register(new Setting("Packets", 4, 1, 4));
   public Offhand.Mode2 currentMode;
   public int totems;
   public int crystals;
   public int gapples;
   public int lastTotemSlot;
   public int lastGappleSlot;
   public int lastCrystalSlot;
   public int lastObbySlot;
   public int lastWebSlot;
   public boolean holdingCrystal;
   public boolean holdingTotem;
   public boolean holdingGapple;
   public boolean didSwitchThisTick;
   private boolean second;
   private boolean switchedForHealthReason;

   public Offhand() {
      super("Offhand", "Allows you to switch up your Offhand.", Module.Category.COMBAT, true, false, false);
      this.currentMode = Offhand.Mode2.TOTEMS;
      this.totems = 0;
      this.crystals = 0;
      this.gapples = 0;
      this.lastTotemSlot = -1;
      this.lastGappleSlot = -1;
      this.lastCrystalSlot = -1;
      this.lastObbySlot = -1;
      this.lastWebSlot = -1;
      this.holdingCrystal = false;
      this.holdingTotem = false;
      this.holdingGapple = false;
      this.didSwitchThisTick = false;
      this.second = false;
      this.switchedForHealthReason = false;
      instance = this;
   }

   public static Offhand getInstance() {
      if (instance == null) {
         instance = new Offhand();
      }

      return instance;
   }

   @SubscribeEvent
   public void onUpdateWalkingPlayer(ProcessRightClickBlockEvent event) {
      if (event.hand == EnumHand.MAIN_HAND && event.stack.getItem() == Items.END_CRYSTAL && mc.player.getHeldItemOffhand().getItem() == Items.GOLDEN_APPLE && mc.objectMouseOver != null && event.pos == mc.objectMouseOver.getBlockPos()) {
         event.setCanceled(true);
         mc.player.setActiveHand(EnumHand.OFF_HAND);
         mc.playerController.processRightClick(mc.player, mc.world, EnumHand.OFF_HAND);
      }

   }

   public void onUpdate() {
      if (this.timer.passedMs(50L)) {
         if (mc.player != null && mc.player.getHeldItemOffhand().getItem() == Items.GOLDEN_APPLE && mc.player.getHeldItemMainhand().getItem() == Items.END_CRYSTAL && Mouse.isButtonDown(1)) {
            mc.player.setActiveHand(EnumHand.OFF_HAND);
            mc.gameSettings.keyBindUseItem.pressed = Mouse.isButtonDown(1);
         }
      } else if (mc.player.getHeldItemOffhand().getItem() == Items.GOLDEN_APPLE && mc.player.getHeldItemMainhand().getItem() == Items.END_CRYSTAL) {
         mc.gameSettings.keyBindUseItem.pressed = false;
      }

      if (!nullCheck()) {
         this.doOffhand();
         if (this.secondTimer.passedMs(50L) && this.second) {
            this.second = false;
            this.timer.reset();
         }

      }
   }

   @SubscribeEvent
   public void onPacketSend(PacketEvent.Send event) {
      if (!fullNullCheck() && mc.player.getHeldItemOffhand().getItem() == Items.GOLDEN_APPLE && mc.player.getHeldItemMainhand().getItem() == Items.END_CRYSTAL && mc.gameSettings.keyBindUseItem.isKeyDown()) {
         if (event.getPacket() instanceof CPacketPlayerTryUseItemOnBlock) {
            CPacketPlayerTryUseItemOnBlock packet2 = (CPacketPlayerTryUseItemOnBlock)event.getPacket();
            if (packet2.getHand() == EnumHand.MAIN_HAND) {
               if (this.timer.passedMs(50L)) {
                  mc.player.setActiveHand(EnumHand.OFF_HAND);
                  mc.player.connection.sendPacket(new CPacketPlayerTryUseItem(EnumHand.OFF_HAND));
               }

               event.setCanceled(true);
            }
         } else if (event.getPacket() instanceof CPacketPlayerTryUseItem && ((CPacketPlayerTryUseItem)event.getPacket()).getHand() == EnumHand.OFF_HAND && !this.timer.passedMs(50L)) {
            event.setCanceled(true);
         }
      }

   }

   public String getDisplayInfo() {
      if (mc.player.getHeldItemOffhand().getItem() == Items.END_CRYSTAL) {
         return "Crystals";
      } else if (mc.player.getHeldItemOffhand().getItem() == Items.TOTEM_OF_UNDYING) {
         return "Totems";
      } else {
         return mc.player.getHeldItemOffhand().getItem() == Items.GOLDEN_APPLE ? "Gapples" : null;
      }
   }

   public void doOffhand() {
      this.didSwitchThisTick = false;
      this.holdingCrystal = mc.player.getHeldItemOffhand().getItem() == Items.END_CRYSTAL;
      this.holdingTotem = mc.player.getHeldItemOffhand().getItem() == Items.TOTEM_OF_UNDYING;
      this.holdingGapple = mc.player.getHeldItemOffhand().getItem() == Items.GOLDEN_APPLE;
      this.totems = mc.player.inventory.mainInventory.stream().filter((itemStack) -> {
         return itemStack.getItem() == Items.TOTEM_OF_UNDYING;
      }).mapToInt(ItemStack::getCount).sum();
      if (this.holdingTotem) {
         this.totems += mc.player.inventory.offHandInventory.stream().filter((itemStack) -> {
            return itemStack.getItem() == Items.TOTEM_OF_UNDYING;
         }).mapToInt(ItemStack::getCount).sum();
      }

      this.crystals = mc.player.inventory.mainInventory.stream().filter((itemStack) -> {
         return itemStack.getItem() == Items.END_CRYSTAL;
      }).mapToInt(ItemStack::getCount).sum();
      if (this.holdingCrystal) {
         this.crystals += mc.player.inventory.offHandInventory.stream().filter((itemStack) -> {
            return itemStack.getItem() == Items.END_CRYSTAL;
         }).mapToInt(ItemStack::getCount).sum();
      }

      this.gapples = mc.player.inventory.mainInventory.stream().filter((itemStack) -> {
         return itemStack.getItem() == Items.GOLDEN_APPLE;
      }).mapToInt(ItemStack::getCount).sum();
      if (this.holdingGapple) {
         this.gapples += mc.player.inventory.offHandInventory.stream().filter((itemStack) -> {
            return itemStack.getItem() == Items.GOLDEN_APPLE;
         }).mapToInt(ItemStack::getCount).sum();
      }

      this.doSwitch();
   }

   public void doSwitch() {
      this.currentMode = Offhand.Mode2.TOTEMS;
      if ((Boolean)this.gapple.getValue() && mc.player.getHeldItemMainhand().getItem() instanceof ItemSword && mc.gameSettings.keyBindUseItem.isKeyDown()) {
         this.currentMode = Offhand.Mode2.GAPPLES;
      } else if (this.currentMode != Offhand.Mode2.CRYSTALS && (Boolean)this.crystal.getValue() && (EntityUtil.isSafe(mc.player) && EntityUtil.getHealth(mc.player, true) > (Float)this.crystalHoleHealth.getValue() || EntityUtil.getHealth(mc.player, true) > (Float)this.crystalHealth.getValue())) {
         this.currentMode = Offhand.Mode2.CRYSTALS;
      }

      if (this.currentMode == Offhand.Mode2.CRYSTALS && this.crystals == 0) {
         this.setMode(Offhand.Mode2.TOTEMS);
      }

      if (this.currentMode == Offhand.Mode2.CRYSTALS && (!EntityUtil.isSafe(mc.player) && EntityUtil.getHealth(mc.player, true) <= (Float)this.crystalHealth.getValue() || EntityUtil.getHealth(mc.player, true) <= (Float)this.crystalHoleHealth.getValue())) {
         if (this.currentMode == Offhand.Mode2.CRYSTALS) {
            this.switchedForHealthReason = true;
         }

         this.setMode(Offhand.Mode2.TOTEMS);
      }

      if (this.switchedForHealthReason && (EntityUtil.isSafe(mc.player) && EntityUtil.getHealth(mc.player, true) > (Float)this.crystalHoleHealth.getValue() || EntityUtil.getHealth(mc.player, true) > (Float)this.crystalHealth.getValue())) {
         this.setMode(Offhand.Mode2.CRYSTALS);
         this.switchedForHealthReason = false;
      }

      if (this.currentMode == Offhand.Mode2.CRYSTALS && (Boolean)this.armorCheck.getValue() && (mc.player.getItemStackFromSlot(EntityEquipmentSlot.CHEST).getItem() == Items.AIR || mc.player.getItemStackFromSlot(EntityEquipmentSlot.HEAD).getItem() == Items.AIR || mc.player.getItemStackFromSlot(EntityEquipmentSlot.LEGS).getItem() == Items.AIR || mc.player.getItemStackFromSlot(EntityEquipmentSlot.FEET).getItem() == Items.AIR)) {
         this.setMode(Offhand.Mode2.TOTEMS);
      }

      if (!(mc.currentScreen instanceof GuiContainer) || mc.currentScreen instanceof GuiInventory) {
         Item currentOffhandItem = mc.player.getHeldItemOffhand().getItem();
         int i;
         switch(this.currentMode) {
         case TOTEMS:
            if (this.totems > 0 && !this.holdingTotem) {
               this.lastTotemSlot = InventoryUtil.findItemInventorySlot(Items.TOTEM_OF_UNDYING, false);
               i = this.getLastSlot(currentOffhandItem, this.lastTotemSlot);
               this.putItemInOffhand(this.lastTotemSlot, i);
            }
            break;
         case GAPPLES:
            if (this.gapples > 0 && !this.holdingGapple) {
               this.lastGappleSlot = InventoryUtil.findItemInventorySlot(Items.GOLDEN_APPLE, false);
               i = this.getLastSlot(currentOffhandItem, this.lastGappleSlot);
               this.putItemInOffhand(this.lastGappleSlot, i);
            }
            break;
         default:
            if (this.crystals > 0 && !this.holdingCrystal) {
               this.lastCrystalSlot = InventoryUtil.findItemInventorySlot(Items.END_CRYSTAL, false);
               i = this.getLastSlot(currentOffhandItem, this.lastCrystalSlot);
               this.putItemInOffhand(this.lastCrystalSlot, i);
            }
         }

         for(i = 0; i < (Integer)this.actions.getValue(); ++i) {
            InventoryUtil.Task task = (InventoryUtil.Task)this.taskList.poll();
            if (task != null) {
               task.run();
               if (task.isSwitching()) {
                  this.didSwitchThisTick = true;
               }
            }
         }

      }
   }

   private int getLastSlot(Item item, int slotIn) {
      if (item == Items.END_CRYSTAL) {
         return this.lastCrystalSlot;
      } else if (item == Items.GOLDEN_APPLE) {
         return this.lastGappleSlot;
      } else if (item == Items.TOTEM_OF_UNDYING) {
         return this.lastTotemSlot;
      } else if (InventoryUtil.isBlock(item, BlockObsidian.class)) {
         return this.lastObbySlot;
      } else if (InventoryUtil.isBlock(item, BlockWeb.class)) {
         return this.lastWebSlot;
      } else {
         return item == Items.AIR ? -1 : slotIn;
      }
   }

   private void putItemInOffhand(int slotIn, int slotOut) {
      if (slotIn != -1 && this.taskList.isEmpty()) {
         this.taskList.add(new InventoryUtil.Task(slotIn));
         this.taskList.add(new InventoryUtil.Task(45));
         this.taskList.add(new InventoryUtil.Task(slotOut));
         this.taskList.add(new InventoryUtil.Task());
      }

   }

   public void setMode(Offhand.Mode2 mode) {
      this.currentMode = this.currentMode == mode ? Offhand.Mode2.TOTEMS : mode;
   }

   public static enum Mode2 {
      TOTEMS,
      GAPPLES,
      CRYSTALS;
   }
}
