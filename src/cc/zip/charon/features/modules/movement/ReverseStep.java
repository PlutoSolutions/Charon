package cc.zip.charon.features.modules.movement;

import cc.zip.charon.features.modules.Module;
import cc.zip.charon.features.setting.Setting;
import cc.zip.charon.util.Util;

public class ReverseStep extends Module {
   private static ReverseStep INSTANCE = new ReverseStep();
   private final Setting<Boolean> twoBlocks;

   public ReverseStep() {
      super("ReverseStep", "ReverseStep.", Module.Category.MOVEMENT, true, false, false);
      this.twoBlocks = this.register(new Setting("2Blocks", Boolean.FALSE));
      this.setInstance();
   }

   public static ReverseStep getInstance() {
      if (INSTANCE == null) {
         INSTANCE = new ReverseStep();
      }

      return INSTANCE;
   }

   private void setInstance() {
      INSTANCE = this;
   }

   public void onUpdate() {
      if (Util.mc.player != null && Util.mc.world != null && !Util.mc.player.isInWater() && !Util.mc.player.isInLava()) {
         if (Util.mc.player.onGround) {
            --Util.mc.player.motionY;
         }

      }
   }
}
