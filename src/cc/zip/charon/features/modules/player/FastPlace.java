package cc.zip.charon.features.modules.player;

import cc.zip.charon.features.modules.Module;
import cc.zip.charon.util.InventoryUtil;
import net.minecraft.item.ItemExpBottle;

public class FastPlace extends Module {
   public FastPlace() {
      super("FastPlace", "Fast everything.", Module.Category.PLAYER, true, false, false);
   }

   public void onUpdate() {
      if (!fullNullCheck()) {
         if (InventoryUtil.holdingItem(ItemExpBottle.class)) {
            mc.rightClickDelayTimer = 0;
         }

      }
   }
}
