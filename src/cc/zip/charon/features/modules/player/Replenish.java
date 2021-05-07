package cc.zip.charon.features.modules.player;

import cc.zip.charon.features.modules.Module;
import cc.zip.charon.features.setting.Setting;
import cc.zip.charon.util.Timer;
import java.util.ArrayList;
import net.minecraft.init.Items;
import net.minecraft.inventory.ClickType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

public class Replenish extends Module {
   private final Setting<Integer> delay = this.register(new Setting("Delay", 0, 0, 10));
   private final Setting<Integer> gapStack = this.register(new Setting("GapStack", 1, 50, 64));
   private final Setting<Integer> xpStackAt = this.register(new Setting("XPStack", 1, 50, 64));
   private final Timer timer = new Timer();
   private final ArrayList<Item> Hotbar = new ArrayList();

   public Replenish() {
      super("Replenish", "Replenishes your hotbar", Module.Category.PLAYER, false, false, false);
   }

   public void onEnable() {
      if (!fullNullCheck()) {
         this.Hotbar.clear();

         for(int l_I = 0; l_I < 9; ++l_I) {
            ItemStack l_Stack = mc.player.inventory.getStackInSlot(l_I);
            if (!l_Stack.isEmpty() && !this.Hotbar.contains(l_Stack.getItem())) {
               this.Hotbar.add(l_Stack.getItem());
            } else {
               this.Hotbar.add(Items.AIR);
            }
         }

      }
   }

   public void onUpdate() {
      if (mc.currentScreen == null) {
         if (this.timer.passedMs((long)((Integer)this.delay.getValue() * 1000))) {
            for(int l_I = 0; l_I < 9; ++l_I) {
               if (this.RefillSlotIfNeed(l_I)) {
                  this.timer.reset();
                  return;
               }
            }

         }
      }
   }

   private boolean RefillSlotIfNeed(int p_Slot) {
      ItemStack l_Stack = mc.player.inventory.getStackInSlot(p_Slot);
      if (!l_Stack.isEmpty() && l_Stack.getItem() != Items.AIR) {
         if (!l_Stack.isStackable()) {
            return false;
         } else if (l_Stack.getCount() >= l_Stack.getMaxStackSize()) {
            return false;
         } else if (l_Stack.getItem().equals(Items.GOLDEN_APPLE) && l_Stack.getCount() >= (Integer)this.gapStack.getValue()) {
            return false;
         } else if (l_Stack.getItem().equals(Items.EXPERIENCE_BOTTLE) && l_Stack.getCount() > (Integer)this.xpStackAt.getValue()) {
            return false;
         } else {
            for(int l_I = 9; l_I < 36; ++l_I) {
               ItemStack l_Item = mc.player.inventory.getStackInSlot(l_I);
               if (!l_Item.isEmpty() && this.CanItemBeMergedWith(l_Stack, l_Item)) {
                  mc.playerController.windowClick(mc.player.inventoryContainer.windowId, l_I, 0, ClickType.QUICK_MOVE, mc.player);
                  mc.playerController.updateController();
                  return true;
               }
            }

            return false;
         }
      } else {
         return false;
      }
   }

   private boolean CanItemBeMergedWith(ItemStack p_Source, ItemStack p_Target) {
      return p_Source.getItem() == p_Target.getItem() && p_Source.getDisplayName().equals(p_Target.getDisplayName());
   }
}
