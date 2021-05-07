package cc.zip.charon.features.modules.render;

import cc.zip.charon.features.modules.Module;
import cc.zip.charon.features.setting.Setting;

public class NoHitBox extends Module {
   private static NoHitBox INSTANCE = new NoHitBox();
   public Setting<Boolean> pickaxe = this.register(new Setting("Pickaxe", true));
   public Setting<Boolean> crystal = this.register(new Setting("Crystal", true));
   public Setting<Boolean> gapple = this.register(new Setting("Gapple", true));

   public NoHitBox() {
      super("NoHitBox", "NoHitBox.", Module.Category.RENDER, false, false, false);
      this.setInstance();
   }

   public static NoHitBox getINSTANCE() {
      if (INSTANCE == null) {
         INSTANCE = new NoHitBox();
      }

      return INSTANCE;
   }

   private void setInstance() {
      INSTANCE = this;
   }
}
