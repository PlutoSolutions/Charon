package cc.zip.charon.features.modules.render;

import cc.zip.charon.features.modules.Module;
import cc.zip.charon.features.setting.Setting;

public class HandChams extends Module {
   private static HandChams INSTANCE = new HandChams();
   public Setting<HandChams.RenderMode> mode;
   public Setting<Integer> red;
   public Setting<Integer> green;
   public Setting<Integer> blue;
   public Setting<Integer> alpha;

   public HandChams() {
      super("HandChams", "Changes your hand color.", Module.Category.RENDER, false, false, false);
      this.mode = this.register(new Setting("Mode", HandChams.RenderMode.SOLID));
      this.red = this.register(new Setting("Red", 255, 0, 255));
      this.green = this.register(new Setting("Green", 0, 0, 255));
      this.blue = this.register(new Setting("Blue", 0, 0, 255));
      this.alpha = this.register(new Setting("Alpha", 240, 0, 255));
      this.setInstance();
   }

   public static HandChams getINSTANCE() {
      if (INSTANCE == null) {
         INSTANCE = new HandChams();
      }

      return INSTANCE;
   }

   private void setInstance() {
      INSTANCE = this;
   }

   public static enum RenderMode {
      SOLID,
      WIREFRAME;
   }
}
