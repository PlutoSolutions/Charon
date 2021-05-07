package cc.zip.charon.features.modules.client;

import cc.zip.charon.features.modules.Module;
import cc.zip.charon.features.setting.Setting;

public class Componentik extends Module {
   private static Componentik INSTANCE = new Componentik();
   public Setting<Integer> red1 = this.register(new Setting("top-red", 179, 0, 255));
   public Setting<Integer> green1 = this.register(new Setting("top-green", 84, 0, 255));
   public Setting<Integer> blue1 = this.register(new Setting("top-blue", 179, 0, 255));
   public Setting<Integer> alpha1 = this.register(new Setting("top-alpha", 255, 0, 255));
   public Setting<Integer> r = this.register(new Setting("Red - Open", 0, 0, 255));
   public Setting<Integer> g = this.register(new Setting("Green - Open", 0, 0, 255));
   public Setting<Integer> b = this.register(new Setting("Blue - Open", 0, 0, 255));
   public Setting<Integer> a = this.register(new Setting("Alpha - Open", 129, 0, 255));

   public Componentik() {
      super("Component", "Makes you offhand lower.", Module.Category.RENDER, false, false, false);
      this.setInstance();
   }

   private void setInstance() {
      INSTANCE = this;
   }

   public static Componentik getINSTANCE() {
      if (INSTANCE == null) {
         INSTANCE = new Componentik();
      }

      return INSTANCE;
   }
}
