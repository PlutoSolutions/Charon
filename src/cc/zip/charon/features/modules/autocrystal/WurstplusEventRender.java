package cc.zip.charon.features.modules.autocrystal;

import cc.zip.charon.event.EventStage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.util.math.Vec3d;

public class WurstplusEventRender extends EventStage {
   private final ScaledResolution res = new ScaledResolution(Minecraft.getMinecraft());
   private final Tessellator tessellator;
   private final Vec3d render_pos;

   public WurstplusEventRender(Tessellator tessellator, Vec3d pos) {
      this.tessellator = tessellator;
      this.render_pos = pos;
   }

   public Tessellator get_tessellator() {
      return this.tessellator;
   }

   public Vec3d get_render_pos() {
      return this.render_pos;
   }

   public BufferBuilder get_buffer_build() {
      return this.tessellator.getBuffer();
   }

   public void set_translation(Vec3d pos) {
      this.get_buffer_build().setTranslation(-pos.x, -pos.y, -pos.z);
   }

   public void reset_translation() {
      this.set_translation(this.render_pos);
   }

   public double get_screen_width() {
      return this.res.getScaledWidth_double();
   }

   public double get_screen_height() {
      return this.res.getScaledHeight_double();
   }
}
