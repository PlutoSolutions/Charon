package cc.zip.charon.features.modules.render;

import cc.zip.charon.features.modules.Module;
import cc.zip.charon.features.setting.Setting;
import cc.zip.charon.util.RenderUtil;
import cc.zip.charon.util.Timer;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class CrossHairs extends Module {
   private final Setting<Boolean> custom = this.register(new Setting("Custom", true));
   public Setting<CrossHairs.Modes> modes;
   public Setting<Float> thickness;
   public Setting<Float> separation;
   public Setting<Float> width;
   public Setting<Float> bend;
   Timer hitTimer;

   public CrossHairs() {
      super("CrossHairs", "fff.", Module.Category.RENDER, true, false, false);
      this.modes = this.register(new Setting("Swing", CrossHairs.Modes.Dynamic));
      this.thickness = this.register(new Setting("thickness", 2.0F, 0.0F, 5.0F));
      this.separation = this.register(new Setting("separation", 2.0F, 0.0F, 10.0F));
      this.width = this.register(new Setting("width", 4.0F, 0.0F, 10.0F));
      this.bend = this.register(new Setting("bend", 2.0F, 0.0F, 5.0F));
      this.hitTimer = new Timer();
   }

   @SubscribeEvent
   public void onRenderGameOverlay(RenderGameOverlayEvent event) {
      if ((Boolean)this.custom.getValue() && event.getType() == ElementType.CROSSHAIRS) {
         event.setCanceled(true);
      }

      RenderUtil.drawCrosshairs((double)(Float)this.separation.getValue(), (double)(Float)this.bend.getValue(), (double)(Float)this.width.getValue(), (double)(Float)this.thickness.getValue(), this.modes.getValue() == CrossHairs.Modes.Static, 155);
   }

   public static enum Modes {
      Dynamic,
      Static;
   }
}
