package cc.zip.charon.event;

import net.minecraftforge.fml.common.eventhandler.Cancelable;
import net.minecraftforge.fml.common.eventhandler.Event;

@Cancelable
public class EventMo extends Event {
   EventMo.Stage stage;

   public EventMo() {
   }

   public EventMo(EventMo.Stage stage) {
      this.stage = stage;
   }

   public EventMo.Stage getStage() {
      return this.stage;
   }

   public void setStage(EventMo.Stage stage) {
      this.stage = stage;
      this.setCanceled(false);
   }

   public static enum Stage {
      PRE,
      POST;
   }
}
