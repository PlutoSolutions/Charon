package cc.zip.charon.features.modules.misc;

import cc.zip.charon.event.events.PacketEvent;
import cc.zip.charon.features.modules.Module;
import io.netty.buffer.Unpooled;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.client.CPacketCustomPayload;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.network.internal.FMLProxyPacket;

public class NoHandShake extends Module {
   public NoHandShake() {
      super("NoHandshake", "Doesnt send your modlist to the server.", Module.Category.MISC, true, false, false);
   }

   @SubscribeEvent
   public void onPacketSend(PacketEvent.Send event) {
      if (event.getPacket() instanceof FMLProxyPacket && !mc.isSingleplayer()) {
         event.setCanceled(true);
      }

      CPacketCustomPayload packet;
      if (event.getPacket() instanceof CPacketCustomPayload && (packet = (CPacketCustomPayload)event.getPacket()).getChannelName().equals("MC|Brand")) {
         packet.data = (new PacketBuffer(Unpooled.buffer())).writeString("vanilla");
      }

   }
}
