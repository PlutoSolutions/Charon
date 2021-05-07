package cc.zip.charon.event.events;

import cc.zip.charon.event.EventStage;
import net.minecraft.util.EnumHandSide;

public class TransformSideFirstPersonEvent extends EventStage {
   private final EnumHandSide enumHandSide;

   public TransformSideFirstPersonEvent(EnumHandSide enumHandSide) {
      this.enumHandSide = enumHandSide;
   }

   public EnumHandSide getEnumHandSide() {
      return this.enumHandSide;
   }
}
