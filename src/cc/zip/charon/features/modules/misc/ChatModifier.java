package cc.zip.charon.features.modules.misc;

import cc.zip.charon.Charon;
import cc.zip.charon.event.events.PacketEvent;
import cc.zip.charon.features.modules.Module;
import cc.zip.charon.features.setting.Setting;
import net.minecraft.network.play.client.CPacketChatMessage;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class ChatModifier extends Module {
   private static ChatModifier INSTANCE = new ChatModifier();
   public Setting<Boolean> clean = this.register(new Setting("NoChatBackground", false, "Cleans your chat"));
   public Setting<Boolean> infinite = this.register(new Setting("InfiniteChat", false, "Makes your chat infinite."));
   public boolean check;

   public ChatModifier() {
      super("BetterChat", "Modifies your chat", Module.Category.MISC, true, false, false);
      this.setInstance();
   }

   public static ChatModifier getInstance() {
      if (INSTANCE == null) {
         INSTANCE = new ChatModifier();
      }

      return INSTANCE;
   }

   private void setInstance() {
      INSTANCE = this;
   }

   @SubscribeEvent
   public void onPacketSend(PacketEvent.Send event) {
      if (event.getPacket() instanceof CPacketChatMessage) {
         String s = ((CPacketChatMessage)event.getPacket()).getMessage();
         this.check = !s.startsWith(Charon.commandManager.getPrefix());
      }

   }
}
