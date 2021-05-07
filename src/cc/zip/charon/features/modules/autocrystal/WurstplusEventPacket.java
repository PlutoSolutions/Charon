package cc.zip.charon.features.modules.autocrystal;

import cc.zip.charon.event.EventStage;
import net.minecraft.network.Packet;

public class WurstplusEventPacket extends EventStage {
   private final Packet packet;

   public WurstplusEventPacket(Packet packet) {
      this.packet = packet;
   }

   public Packet get_packet() {
      return this.packet;
   }

   public static class ReceivePacket extends WurstplusEventPacket {
      public ReceivePacket(Packet packet) {
         super(packet);
      }
   }

   public static class SendPacket extends WurstplusEventPacket {
      public SendPacket(Packet packet) {
         super(packet);
      }
   }
}
