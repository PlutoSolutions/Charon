package cc.zip.charon.features.modules.render;

import cc.zip.charon.event.events.Render3DEvent;
import cc.zip.charon.features.modules.Module;
import cc.zip.charon.features.modules.client.ClickGui;
import cc.zip.charon.features.setting.Setting;
import cc.zip.charon.util.ColorUtil;
import cc.zip.charon.util.RenderUtil;
import java.awt.Color;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.RayTraceResult.Type;

public class BlockHighlight extends Module {
   private final Setting<Float> lineWidth = this.register(new Setting("LineWidth", 1.0F, 0.1F, 5.0F));
   private final Setting<Integer> cAlpha = this.register(new Setting("Alpha", 255, 0, 255));

   public BlockHighlight() {
      super("BlockHighlight", "Highlights the block u look at.", Module.Category.RENDER, false, false, false);
   }

   public void onRender3D(Render3DEvent event) {
      RayTraceResult ray = mc.objectMouseOver;
      if (ray != null && ray.typeOfHit == Type.BLOCK) {
         BlockPos blockpos = ray.getBlockPos();
         RenderUtil.drawBlockOutline(blockpos, (Boolean)ClickGui.getInstance().rainbow.getValue() ? ColorUtil.rainbow((Integer)ClickGui.getInstance().rainbowHue.getValue()) : new Color((Integer)ClickGui.getInstance().red.getValue(), (Integer)ClickGui.getInstance().green.getValue(), (Integer)ClickGui.getInstance().blue.getValue(), (Integer)this.cAlpha.getValue()), (Float)this.lineWidth.getValue(), false);
      }

   }
}
