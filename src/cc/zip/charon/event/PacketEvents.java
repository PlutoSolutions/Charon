package cc.zip.charon.event;

import net.minecraft.network.Packet;
import net.minecraftforge.fml.common.eventhandler.Cancelable;

@Cancelable
public class PacketEvents extends EventMo {
   Packet<?> packet;

   public PacketEvents(Packet<?> packet, EventMo.Stage stage) {
      super(stage);
      this.packet = packet;
   }

   public Packet<?> getPacket() {
      return this.packet;
   }
}
