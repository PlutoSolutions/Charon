package cc.zip.charon.features.modules.render;

import cc.zip.charon.features.modules.Module;
import cc.zip.charon.features.setting.Setting;

public class WorldTime extends Module {
   private final Setting<Integer> time = this.register(new Setting("Time", 24000, 0, 24000));

   public WorldTime() {
      super("WorldTime", "world timeeeee", Module.Category.RENDER, true, false, false);
   }

   public void onUpdate() {
      if (!nullCheck()) {
         mc.world.setWorldTime((long)(Integer)this.time.getValue());
         mc.world.setWorldTime((long)(Integer)this.time.getValue());
      }
   }
}
