package cc.zip.charon.features.modules.misc;

import cc.zip.charon.event.events.DeathEvent;
import cc.zip.charon.features.command.Command;
import cc.zip.charon.features.modules.Module;
import cc.zip.charon.features.modules.client.HUD;
import cc.zip.charon.util.EntityUtil;
import cc.zip.charon.util.TextUtil;
import java.util.Objects;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityExpBottle;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class Tracker extends Module {
   private static Tracker instance;
   private EntityPlayer trackedPlayer;
   private int usedExp = 0;
   private int usedStacks = 0;

   public Tracker() {
      super("Duel", "Tracks players in 1v1s.", Module.Category.MISC, true, false, false);
      instance = this;
   }

   public static Tracker getInstance() {
      if (instance == null) {
         instance = new Tracker();
      }

      return instance;
   }

   public void onUpdate() {
      if (this.trackedPlayer == null) {
         this.trackedPlayer = EntityUtil.getClosestEnemy(1000.0D);
      } else if (this.usedStacks != this.usedExp / 64) {
         this.usedStacks = this.usedExp / 64;
         Command.sendMessage(TextUtil.coloredString(this.trackedPlayer.getName() + " has used " + this.usedStacks + " stacks of XP!", (TextUtil.Color)HUD.getInstance().commandColor.getValue()));
      }

   }

   public void onSpawnEntity(Entity entity) {
      if (entity instanceof EntityExpBottle && Objects.equals(mc.world.getClosestPlayerToEntity(entity, 3.0D), this.trackedPlayer)) {
         ++this.usedExp;
      }

   }

   public void onDisable() {
      this.trackedPlayer = null;
      this.usedExp = 0;
      this.usedStacks = 0;
   }

   @SubscribeEvent
   public void onDeath(DeathEvent event) {
      if (event.player.equals(this.trackedPlayer)) {
         this.usedExp = 0;
         this.usedStacks = 0;
      }

   }

   public String getDisplayInfo() {
      return this.trackedPlayer != null ? this.trackedPlayer.getName() : null;
   }
}
