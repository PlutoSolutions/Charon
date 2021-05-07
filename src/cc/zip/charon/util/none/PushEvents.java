package cc.zip.charon.util.none;

import cc.zip.charon.event.EventStage;
import net.minecraft.entity.Entity;
import net.minecraftforge.fml.common.eventhandler.Cancelable;

@Cancelable
public class PushEvents extends EventStage {
   public Entity entity;
   public double x;
   public double y;
   public double z;
   public boolean airbone;

   public PushEvents(Entity entity, double x, double y, double z, boolean airbone) {
      super(0);
      this.entity = entity;
      this.x = x;
      this.y = y;
      this.z = z;
      this.airbone = airbone;
   }

   public PushEvents(int stage) {
      super(stage);
   }

   public PushEvents(int stage, Entity entity) {
      super(stage);
      this.entity = entity;
   }
}
