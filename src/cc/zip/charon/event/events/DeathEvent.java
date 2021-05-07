package cc.zip.charon.event.events;

import cc.zip.charon.event.EventStage;
import net.minecraft.entity.player.EntityPlayer;

public class DeathEvent extends EventStage {
   public EntityPlayer player;

   public DeathEvent(EntityPlayer player) {
      this.player = player;
   }
}
