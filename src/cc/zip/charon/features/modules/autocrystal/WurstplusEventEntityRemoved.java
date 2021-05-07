package cc.zip.charon.features.modules.autocrystal;

import cc.zip.charon.event.EventStage;
import net.minecraft.entity.Entity;

public class WurstplusEventEntityRemoved extends EventStage {
   private final Entity entity;

   public WurstplusEventEntityRemoved(Entity entity) {
      this.entity = entity;
   }

   public Entity get_entity() {
      return this.entity;
   }
}
