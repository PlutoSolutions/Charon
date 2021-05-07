package cc.zip.charon.manager;

import cc.zip.charon.util.Util;
import net.minecraft.network.play.client.CPacketHeldItemChange;

public class InventoryManager implements Util {
   public int currentPlayerItem;
   private int recoverySlot = -1;

   public void update() {
      if (this.recoverySlot != -1) {
         mc.player.connection.sendPacket(new CPacketHeldItemChange(this.recoverySlot == 8 ? 7 : this.recoverySlot + 1));
         mc.player.connection.sendPacket(new CPacketHeldItemChange(this.recoverySlot));
         mc.player.inventory.currentItem = this.recoverySlot;
         int i = mc.player.inventory.currentItem;
         if (i != this.currentPlayerItem) {
            this.currentPlayerItem = i;
            mc.player.connection.sendPacket(new CPacketHeldItemChange(this.currentPlayerItem));
         }

         this.recoverySlot = -1;
      }

   }

   public void recoverSilent(int slot) {
      this.recoverySlot = slot;
   }
}
