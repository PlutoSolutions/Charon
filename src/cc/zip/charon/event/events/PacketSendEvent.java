package cc.zip.charon.event.events;

import cc.zip.charon.event.EventMo;
import cc.zip.charon.event.PacketEvents;
import net.minecraft.network.Packet;
import net.minecraftforge.fml.common.eventhandler.Cancelable;

@Cancelable
public class PacketSendEvent extends PacketEvents {
   public PacketSendEvent(Packet<?> packet, EventMo.Stage stage) {
      super(packet, stage);
   }
}
