package cc.zip.charon.features.modules.movement;

import cc.zip.charon.features.modules.Module;
import cc.zip.charon.features.setting.Setting;

public class Step extends Module {
   public Setting<Integer> stepHeight = this.register(new Setting("Height", 2, 1, 4));
   private double[] selectedPositions;
   private int packets;
   private static Step instance;

   public Step() {
      super("Step", "Allows you to step up blocks", Module.Category.MOVEMENT, true, false, false);
      instance = this;
   }

   public static Step getInstance() {
      if (instance == null) {
         instance = new Step();
      }

      return instance;
   }

   public void onToggle() {
      mc.player.stepHeight = 0.6F;
   }

   public void onUpdate() {
      if (!mc.player.isOnLadder() || mc.player.isInWater() || mc.player.isInLava()) {
         mc.player.stepHeight = (float)(Integer)this.stepHeight.getValue();
      }
   }
}
