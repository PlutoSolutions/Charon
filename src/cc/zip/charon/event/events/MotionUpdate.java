package cc.zip.charon.event.events;

import cc.zip.charon.event.EventStage;

public class MotionUpdate extends EventStage {
   public int stage;

   public MotionUpdate(int stage) {
      this.stage = stage;
   }
}
