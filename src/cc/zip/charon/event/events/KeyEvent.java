package cc.zip.charon.event.events;

import cc.zip.charon.event.EventStage;

public class KeyEvent extends EventStage {
   private final int key;

   public KeyEvent(int key) {
      this.key = key;
   }

   public int getKey() {
      return this.key;
   }
}
