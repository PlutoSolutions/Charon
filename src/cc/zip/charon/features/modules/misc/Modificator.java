package cc.zip.charon.features.modules.misc;

import cc.zip.charon.features.modules.Module;
import cc.zip.charon.features.setting.Setting;

public class Modificator extends Module {
   public Setting<Boolean> timersetting = this.register(new Setting("--Timer--", true));

   public Modificator() {
      super("Modificator", "troll", Module.Category.MISC, true, false, false);
   }
}
