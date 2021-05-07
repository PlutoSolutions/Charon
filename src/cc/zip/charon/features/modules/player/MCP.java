package cc.zip.charon.features.modules.player;

import cc.zip.charon.features.modules.Module;
import cc.zip.charon.util.InventoryUtil;
import net.minecraft.init.Items;
import net.minecraft.item.ItemEnderPearl;
import net.minecraft.util.EnumHand;
import org.lwjgl.input.Mouse;

public class MCP extends Module {
   private boolean clicked = false;

   public MCP() {
      super("MCP", "Throws a pearl", Module.Category.PLAYER, false, false, false);
   }

   public void onEnable() {
      if (fullNullCheck()) {
         this.disable();
      }

   }

   public void onTick() {
      if (Mouse.isButtonDown(2)) {
         if (!this.clicked) {
            this.throwPearl();
         }

         this.clicked = true;
      } else {
         this.clicked = false;
      }

   }

   private void throwPearl() {
      int pearlSlot = InventoryUtil.findHotbarBlock(ItemEnderPearl.class);
      boolean offhand = mc.player.getHeldItemOffhand().getItem() == Items.ENDER_PEARL;
      if (pearlSlot != -1 || offhand) {
         int oldslot = mc.player.inventory.currentItem;
         if (!offhand) {
            InventoryUtil.switchToHotbarSlot(pearlSlot, false);
         }

         mc.playerController.processRightClick(mc.player, mc.world, offhand ? EnumHand.OFF_HAND : EnumHand.MAIN_HAND);
         if (!offhand) {
            InventoryUtil.switchToHotbarSlot(oldslot, false);
         }
      }

   }
}
