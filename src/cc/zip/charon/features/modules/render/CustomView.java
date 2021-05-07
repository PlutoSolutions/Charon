package cc.zip.charon.features.modules.render;

import cc.zip.charon.features.modules.Module;
import cc.zip.charon.features.setting.Setting;
import net.minecraftforge.client.event.EntityViewRenderEvent.FOVModifier;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class CustomView extends Module {
   private final Setting<Integer> custom_fov = this.register(new Setting("custom fov", 100, 90, 169));
   public Setting<Boolean> cancelEating = this.register(new Setting("cancelEating", true));
   private float fov;

   public CustomView() {
      super("CustomView", "tf", Module.Category.RENDER, true, false, false);
   }

   public void onEnable() {
      this.fov = mc.gameSettings.fovSetting;
      MinecraftForge.EVENT_BUS.register(this);
   }

   public void onDisable() {
      mc.gameSettings.fovSetting = this.fov;
      MinecraftForge.EVENT_BUS.unregister(this);
   }

   public void onUpdate() {
      mc.gameSettings.fovSetting = (float)(Integer)this.custom_fov.getValue();
   }

   @SubscribeEvent
   public void fov_event(FOVModifier m) {
      m.setFOV((float)(Integer)this.custom_fov.getValue());
   }
}
