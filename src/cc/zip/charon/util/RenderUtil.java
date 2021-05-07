package cc.zip.charon.util;

import cc.zip.charon.Charon;
import java.awt.Color;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import java.util.HashMap;
import java.util.Objects;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.client.renderer.RenderItem;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.GlStateManager.DestFactor;
import net.minecraft.client.renderer.GlStateManager.SourceFactor;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.EXTFramebufferObject;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.GLU;
import org.lwjgl.util.glu.Sphere;

public class RenderUtil implements Util {
   private static final Frustum frustrum = new Frustum();
   private static final FloatBuffer screenCoords = BufferUtils.createFloatBuffer(3);
   private static final IntBuffer viewport = BufferUtils.createIntBuffer(16);
   private static final FloatBuffer modelView = BufferUtils.createFloatBuffer(16);
   private static final FloatBuffer projection = BufferUtils.createFloatBuffer(16);
   public static RenderItem itemRender;
   public static ICamera camera;
   private static boolean depth;
   private static boolean texture;
   private static boolean clean;
   private static boolean bind;
   private static boolean override;

   public static void drawRectangleCorrectly(int x, int y, int w, int h, int color) {
      GL11.glLineWidth(1.0F);
      Gui.drawRect(x, y, x + w, y + h, color);
   }

   public static void drawTriangle(float x, float y, float size, float theta, int color) {
      GL11.glTranslated((double)x, (double)y, 0.0D);
      GL11.glRotatef(180.0F + theta, 0.0F, 0.0F, 1.0F);
      float alpha = (float)(color >> 24 & 255) / 255.0F;
      float red = (float)(color >> 16 & 255) / 255.0F;
      float green = (float)(color >> 8 & 255) / 255.0F;
      float blue = (float)(color & 255) / 255.0F;
      GL11.glColor4f(red, green, blue, alpha);
      GL11.glEnable(3042);
      GL11.glDisable(3553);
      GL11.glEnable(2848);
      GL11.glBlendFunc(770, 771);
      GL11.glLineWidth(1.0F);
      GL11.glBegin(6);
      GL11.glVertex2d(0.0D, (double)(1.0F * size));
      GL11.glVertex2d((double)(1.0F * size / 2.0F), (double)(-(1.0F * size)));
      GL11.glVertex2d((double)(-(1.0F * size / 2.0F)), (double)(-(1.0F * size)));
      GL11.glEnd();
      GL11.glDisable(2848);
      GL11.glEnable(3553);
      GL11.glDisable(3042);
      GL11.glRotatef(-180.0F - theta, 0.0F, 0.0F, 1.0F);
      GL11.glTranslated((double)(-x), (double)(-y), 0.0D);
   }

   public static AxisAlignedBB interpolateAxis(AxisAlignedBB bb) {
      return new AxisAlignedBB(bb.minX - mc.getRenderManager().viewerPosX, bb.minY - mc.getRenderManager().viewerPosY, bb.minZ - mc.getRenderManager().viewerPosZ, bb.maxX - mc.getRenderManager().viewerPosX, bb.maxY - mc.getRenderManager().viewerPosY, bb.maxZ - mc.getRenderManager().viewerPosZ);
   }

   public static boolean isInViewFrustrum(Entity entity) {
      return isInViewFrustrum(entity.getEntityBoundingBox()) || entity.ignoreFrustumCheck;
   }

   public static boolean isInViewFrustrum(AxisAlignedBB bb) {
      Entity current = Minecraft.getMinecraft().getRenderViewEntity();
      frustrum.setPosition(current.posX, current.posY, current.posZ);
      return frustrum.isBoundingBoxInFrustum(bb);
   }

   public static Vec3d to2D(double x, double y, double z) {
      GL11.glGetFloat(2982, modelView);
      GL11.glGetFloat(2983, projection);
      GL11.glGetInteger(2978, viewport);
      boolean result = GLU.gluProject((float)x, (float)y, (float)z, modelView, projection, viewport, screenCoords);
      return result ? new Vec3d((double)screenCoords.get(0), (double)((float)Display.getHeight() - screenCoords.get(1)), (double)screenCoords.get(2)) : null;
   }

   public static void drawTracerPointer(float x, float y, float size, float widthDiv, float heightDiv, boolean outline, float outlineWidth, int color) {
      boolean blend = GL11.glIsEnabled(3042);
      float alpha = (float)(color >> 24 & 255) / 255.0F;
      GL11.glEnable(3042);
      GL11.glDisable(3553);
      GL11.glBlendFunc(770, 771);
      GL11.glEnable(2848);
      GL11.glPushMatrix();
      hexColor(color);
      GL11.glBegin(7);
      GL11.glVertex2d((double)x, (double)y);
      GL11.glVertex2d((double)(x - size / widthDiv), (double)(y + size));
      GL11.glVertex2d((double)x, (double)(y + size / heightDiv));
      GL11.glVertex2d((double)(x + size / widthDiv), (double)(y + size));
      GL11.glVertex2d((double)x, (double)y);
      GL11.glEnd();
      if (outline) {
         GL11.glLineWidth(outlineWidth);
         GL11.glColor4f(0.0F, 0.0F, 0.0F, alpha);
         GL11.glBegin(2);
         GL11.glVertex2d((double)x, (double)y);
         GL11.glVertex2d((double)(x - size / widthDiv), (double)(y + size));
         GL11.glVertex2d((double)x, (double)(y + size / heightDiv));
         GL11.glVertex2d((double)(x + size / widthDiv), (double)(y + size));
         GL11.glVertex2d((double)x, (double)y);
         GL11.glEnd();
      }

      GL11.glPopMatrix();
      GL11.glEnable(3553);
      if (!blend) {
         GL11.glDisable(3042);
      }

      GL11.glDisable(2848);
   }

   public static void hexColor(int hexColor) {
      float red = (float)(hexColor >> 16 & 255) / 255.0F;
      float green = (float)(hexColor >> 8 & 255) / 255.0F;
      float blue = (float)(hexColor & 255) / 255.0F;
      float alpha = (float)(hexColor >> 24 & 255) / 255.0F;
      GL11.glColor4f(red, green, blue, alpha);
   }

   public static void drawCrosshairs(double separation, double bend, double width, double thickness, boolean dynamic, int color) {
      int screenWidth = (new ScaledResolution(mc)).getScaledWidth();
      int screenHeight = (new ScaledResolution(mc)).getScaledHeight();
      separation += MotionUtil.isMovings() && dynamic ? 1.0D : 0.0D;
      drawLine((float)((double)(screenWidth / 2) - separation), (float)((double)(screenHeight / 2) - bend / 2.0D), (float)((double)(screenWidth / 2) - separation - width), (float)((double)(screenHeight / 2) - bend / 2.0D), (float)((int)thickness), color);
      drawLine((float)((double)(screenWidth / 2) + separation), (float)((double)(screenHeight / 2) - bend / 2.0D), (float)((double)(screenWidth / 2) + separation + width), (float)((double)(screenHeight / 2) + bend / 2.0D), (float)((int)thickness), color);
      drawLine((float)((double)(screenWidth / 2) - bend / 2.0D), (float)((double)(screenHeight / 2) - separation), (float)((double)(screenWidth / 2) - bend / 2.0D), (float)((double)(screenHeight / 2) - separation - width), (float)((int)thickness), color);
      drawLine((float)((double)(screenWidth / 2) - bend / 2.0D), (float)((double)(screenHeight / 2) + separation), (float)((double)(screenWidth / 2) + bend / 2.0D), (float)((double)(screenHeight / 2) + separation + width), (float)((int)thickness), color);
   }

   public static void drawTexturedRect(int x, int y, int textureX, int textureY, int width, int height, int zLevel) {
      Tessellator tessellator = Tessellator.getInstance();
      BufferBuilder BufferBuilder2 = tessellator.getBuffer();
      BufferBuilder2.begin(7, DefaultVertexFormats.POSITION_TEX);
      BufferBuilder2.pos((double)(x + 0), (double)(y + height), (double)zLevel).tex((double)((float)(textureX + 0) * 0.00390625F), (double)((float)(textureY + height) * 0.00390625F)).endVertex();
      BufferBuilder2.pos((double)(x + width), (double)(y + height), (double)zLevel).tex((double)((float)(textureX + width) * 0.00390625F), (double)((float)(textureY + height) * 0.00390625F)).endVertex();
      BufferBuilder2.pos((double)(x + width), (double)(y + 0), (double)zLevel).tex((double)((float)(textureX + width) * 0.00390625F), (double)((float)(textureY + 0) * 0.00390625F)).endVertex();
      BufferBuilder2.pos((double)(x + 0), (double)(y + 0), (double)zLevel).tex((double)((float)(textureX + 0) * 0.00390625F), (double)((float)(textureY + 0) * 0.00390625F)).endVertex();
      tessellator.draw();
   }

   public static void blockESP(BlockPos b, Color c, double length, double length2) {
      blockEsp(b, c, length, length2);
   }

   public static void drawBoxESP(BlockPos pos, Color color, boolean secondC, Color secondColor, float lineWidth, boolean outline, boolean box, int boxAlpha, boolean air) {
      if (box) {
         drawBox(pos, new Color(color.getRed(), color.getGreen(), color.getBlue(), boxAlpha));
      }

      if (outline) {
         drawBlockOutline(pos, secondC ? secondColor : color, lineWidth, air);
      }

   }

   public static void glScissor(float x, float y, float x1, float y1, ScaledResolution sr) {
      GL11.glScissor((int)(x * (float)sr.getScaleFactor()), (int)((float)mc.displayHeight - y1 * (float)sr.getScaleFactor()), (int)((x1 - x) * (float)sr.getScaleFactor()), (int)((y1 - y) * (float)sr.getScaleFactor()));
   }

   public static void drawLine(float x, float y, float x1, float y1, float thickness, int hex) {
      float red = (float)(hex >> 16 & 255) / 255.0F;
      float green = (float)(hex >> 8 & 255) / 255.0F;
      float blue = (float)(hex & 255) / 255.0F;
      float alpha = (float)(hex >> 24 & 255) / 255.0F;
      GlStateManager.pushMatrix();
      GlStateManager.disableTexture2D();
      GlStateManager.enableBlend();
      GlStateManager.disableAlpha();
      GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
      GlStateManager.shadeModel(7425);
      GL11.glLineWidth(thickness);
      GL11.glEnable(2848);
      GL11.glHint(3154, 4354);
      Tessellator tessellator = Tessellator.getInstance();
      BufferBuilder bufferbuilder = tessellator.getBuffer();
      bufferbuilder.begin(3, DefaultVertexFormats.POSITION_COLOR);
      bufferbuilder.pos((double)x, (double)y, 0.0D).color(red, green, blue, alpha).endVertex();
      bufferbuilder.pos((double)x1, (double)y1, 0.0D).color(red, green, blue, alpha).endVertex();
      tessellator.draw();
      GlStateManager.shadeModel(7424);
      GL11.glDisable(2848);
      GlStateManager.disableBlend();
      GlStateManager.enableAlpha();
      GlStateManager.enableTexture2D();
      GlStateManager.popMatrix();
   }

   public static void drawBox(BlockPos pos, Color color) {
      AxisAlignedBB bb = new AxisAlignedBB((double)pos.getX() - mc.getRenderManager().viewerPosX, (double)pos.getY() - mc.getRenderManager().viewerPosY, (double)pos.getZ() - mc.getRenderManager().viewerPosZ, (double)(pos.getX() + 1) - mc.getRenderManager().viewerPosX, (double)(pos.getY() + 1) - mc.getRenderManager().viewerPosY, (double)(pos.getZ() + 1) - mc.getRenderManager().viewerPosZ);
      camera.setPosition(((Entity)Objects.requireNonNull(mc.getRenderViewEntity())).posX, mc.getRenderViewEntity().posY, mc.getRenderViewEntity().posZ);
      if (camera.isBoundingBoxInFrustum(new AxisAlignedBB(bb.minX + mc.getRenderManager().viewerPosX, bb.minY + mc.getRenderManager().viewerPosY, bb.minZ + mc.getRenderManager().viewerPosZ, bb.maxX + mc.getRenderManager().viewerPosX, bb.maxY + mc.getRenderManager().viewerPosY, bb.maxZ + mc.getRenderManager().viewerPosZ))) {
         GlStateManager.pushMatrix();
         GlStateManager.enableBlend();
         GlStateManager.disableDepth();
         GlStateManager.tryBlendFuncSeparate(770, 771, 0, 1);
         GlStateManager.disableTexture2D();
         GlStateManager.depthMask(false);
         GL11.glEnable(2848);
         GL11.glHint(3154, 4354);
         RenderGlobal.renderFilledBox(bb, (float)color.getRed() / 255.0F, (float)color.getGreen() / 255.0F, (float)color.getBlue() / 255.0F, (float)color.getAlpha() / 255.0F);
         GL11.glDisable(2848);
         GlStateManager.depthMask(true);
         GlStateManager.enableDepth();
         GlStateManager.enableTexture2D();
         GlStateManager.disableBlend();
         GlStateManager.popMatrix();
      }

   }

   public static void drawBlockOutline(BlockPos pos, Color color, float linewidth, boolean air) {
      IBlockState iblockstate = mc.world.getBlockState(pos);
      if ((air || iblockstate.getMaterial() != Material.AIR) && mc.world.getWorldBorder().contains(pos)) {
         assert mc.renderViewEntity != null;

         Vec3d interp = EntityUtil.interpolateEntity(mc.renderViewEntity, mc.getRenderPartialTicks());
         drawBlockOutline(iblockstate.getSelectedBoundingBox(mc.world, pos).grow(0.0020000000949949026D).offset(-interp.x, -interp.y, -interp.z), color, linewidth);
      }

   }

   public static void drawBlockOutline(AxisAlignedBB bb, Color color, float linewidth) {
      float red = (float)color.getRed() / 255.0F;
      float green = (float)color.getGreen() / 255.0F;
      float blue = (float)color.getBlue() / 255.0F;
      float alpha = (float)color.getAlpha() / 255.0F;
      GlStateManager.pushMatrix();
      GlStateManager.enableBlend();
      GlStateManager.disableDepth();
      GlStateManager.tryBlendFuncSeparate(770, 771, 0, 1);
      GlStateManager.disableTexture2D();
      GlStateManager.depthMask(false);
      GL11.glEnable(2848);
      GL11.glHint(3154, 4354);
      GL11.glLineWidth(linewidth);
      Tessellator tessellator = Tessellator.getInstance();
      BufferBuilder bufferbuilder = tessellator.getBuffer();
      bufferbuilder.begin(3, DefaultVertexFormats.POSITION_COLOR);
      bufferbuilder.pos(bb.minX, bb.minY, bb.minZ).color(red, green, blue, alpha).endVertex();
      bufferbuilder.pos(bb.minX, bb.minY, bb.maxZ).color(red, green, blue, alpha).endVertex();
      bufferbuilder.pos(bb.maxX, bb.minY, bb.maxZ).color(red, green, blue, alpha).endVertex();
      bufferbuilder.pos(bb.maxX, bb.minY, bb.minZ).color(red, green, blue, alpha).endVertex();
      bufferbuilder.pos(bb.minX, bb.minY, bb.minZ).color(red, green, blue, alpha).endVertex();
      bufferbuilder.pos(bb.minX, bb.maxY, bb.minZ).color(red, green, blue, alpha).endVertex();
      bufferbuilder.pos(bb.minX, bb.maxY, bb.maxZ).color(red, green, blue, alpha).endVertex();
      bufferbuilder.pos(bb.minX, bb.minY, bb.maxZ).color(red, green, blue, alpha).endVertex();
      bufferbuilder.pos(bb.maxX, bb.minY, bb.maxZ).color(red, green, blue, alpha).endVertex();
      bufferbuilder.pos(bb.maxX, bb.maxY, bb.maxZ).color(red, green, blue, alpha).endVertex();
      bufferbuilder.pos(bb.minX, bb.maxY, bb.maxZ).color(red, green, blue, alpha).endVertex();
      bufferbuilder.pos(bb.maxX, bb.maxY, bb.maxZ).color(red, green, blue, alpha).endVertex();
      bufferbuilder.pos(bb.maxX, bb.maxY, bb.minZ).color(red, green, blue, alpha).endVertex();
      bufferbuilder.pos(bb.maxX, bb.minY, bb.minZ).color(red, green, blue, alpha).endVertex();
      bufferbuilder.pos(bb.maxX, bb.maxY, bb.minZ).color(red, green, blue, alpha).endVertex();
      bufferbuilder.pos(bb.minX, bb.maxY, bb.minZ).color(red, green, blue, alpha).endVertex();
      tessellator.draw();
      GL11.glDisable(2848);
      GlStateManager.depthMask(true);
      GlStateManager.enableDepth();
      GlStateManager.enableTexture2D();
      GlStateManager.disableBlend();
      GlStateManager.popMatrix();
   }

   public static void drawBoxESP(BlockPos pos, Color color, float lineWidth, boolean outline, boolean box, int boxAlpha) {
      AxisAlignedBB bb = new AxisAlignedBB((double)pos.getX() - mc.getRenderManager().viewerPosX, (double)pos.getY() - mc.getRenderManager().viewerPosY, (double)pos.getZ() - mc.getRenderManager().viewerPosZ, (double)(pos.getX() + 1) - mc.getRenderManager().viewerPosX, (double)(pos.getY() + 1) - mc.getRenderManager().viewerPosY, (double)(pos.getZ() + 1) - mc.getRenderManager().viewerPosZ);
      camera.setPosition(((Entity)Objects.requireNonNull(mc.getRenderViewEntity())).posX, mc.getRenderViewEntity().posY, mc.getRenderViewEntity().posZ);
      if (camera.isBoundingBoxInFrustum(new AxisAlignedBB(bb.minX + mc.getRenderManager().viewerPosX, bb.minY + mc.getRenderManager().viewerPosY, bb.minZ + mc.getRenderManager().viewerPosZ, bb.maxX + mc.getRenderManager().viewerPosX, bb.maxY + mc.getRenderManager().viewerPosY, bb.maxZ + mc.getRenderManager().viewerPosZ))) {
         GlStateManager.pushMatrix();
         GlStateManager.enableBlend();
         GlStateManager.disableDepth();
         GlStateManager.tryBlendFuncSeparate(770, 771, 0, 1);
         GlStateManager.disableTexture2D();
         GlStateManager.depthMask(false);
         GL11.glEnable(2848);
         GL11.glHint(3154, 4354);
         GL11.glLineWidth(lineWidth);
         double dist = mc.player.getDistance((double)((float)pos.getX() + 0.5F), (double)((float)pos.getY() + 0.5F), (double)((float)pos.getZ() + 0.5F)) * 0.75D;
         if (box) {
            RenderGlobal.renderFilledBox(bb, (float)color.getRed() / 255.0F, (float)color.getGreen() / 255.0F, (float)color.getBlue() / 255.0F, (float)boxAlpha / 255.0F);
         }

         if (outline) {
            RenderGlobal.drawBoundingBox(bb.minX, bb.minY, bb.minZ, bb.maxX, bb.maxY, bb.maxZ, (float)color.getRed() / 255.0F, (float)color.getGreen() / 255.0F, (float)color.getBlue() / 255.0F, (float)color.getAlpha() / 255.0F);
         }

         GL11.glDisable(2848);
         GlStateManager.depthMask(true);
         GlStateManager.enableDepth();
         GlStateManager.enableTexture2D();
         GlStateManager.disableBlend();
         GlStateManager.popMatrix();
      }

   }

   public static void drawText(BlockPos pos, String text) {
      GlStateManager.pushMatrix();
      glBillboardDistanceScaled((float)pos.getX() + 0.5F, (float)pos.getY() + 0.5F, (float)pos.getZ() + 0.5F, mc.player, 1.0F);
      GlStateManager.disableDepth();
      GlStateManager.translate(-((double)Charon.textManager.getStringWidth(text) / 2.0D), 0.0D, 0.0D);
      Charon.textManager.drawStringWithShadow(text, 0.0F, 0.0F, -5592406);
      GlStateManager.popMatrix();
   }

   public static void drawOutlinedBlockESP(BlockPos pos, Color color, float linewidth) {
      IBlockState iblockstate = mc.world.getBlockState(pos);
      Vec3d interp = EntityUtil.interpolateEntity(mc.player, mc.getRenderPartialTicks());
      drawBoundingBox(iblockstate.getSelectedBoundingBox(mc.world, pos).grow(0.0020000000949949026D).offset(-interp.x, -interp.y, -interp.z), linewidth, ColorUtil.toRGBA(color));
   }

   public static void blockEsp(BlockPos blockPos, Color c, double length, double length2) {
      double x = (double)blockPos.getX() - mc.renderManager.renderPosX;
      double y = (double)blockPos.getY() - mc.renderManager.renderPosY;
      double z = (double)blockPos.getZ() - mc.renderManager.renderPosZ;
      GL11.glPushMatrix();
      GL11.glBlendFunc(770, 771);
      GL11.glEnable(3042);
      GL11.glLineWidth(2.0F);
      GL11.glDisable(3553);
      GL11.glDisable(2929);
      GL11.glDepthMask(false);
      GL11.glColor4d((double)((float)c.getRed() / 255.0F), (double)((float)c.getGreen() / 255.0F), (double)((float)c.getBlue() / 255.0F), 0.25D);
      drawColorBox(new AxisAlignedBB(x, y, z, x + length2, y + 1.0D, z + length), 0.0F, 0.0F, 0.0F, 0.0F);
      GL11.glColor4d(0.0D, 0.0D, 0.0D, 0.5D);
      drawSelectionBoundingBox(new AxisAlignedBB(x, y, z, x + length2, y + 1.0D, z + length));
      GL11.glLineWidth(2.0F);
      GL11.glEnable(3553);
      GL11.glEnable(2929);
      GL11.glDepthMask(true);
      GL11.glDisable(3042);
      GL11.glPopMatrix();
      GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
   }

   public static void drawRect(float x, float y, float w, float h, int color) {
      float alpha = (float)(color >> 24 & 255) / 255.0F;
      float red = (float)(color >> 16 & 255) / 255.0F;
      float green = (float)(color >> 8 & 255) / 255.0F;
      float blue = (float)(color & 255) / 255.0F;
      Tessellator tessellator = Tessellator.getInstance();
      BufferBuilder bufferbuilder = tessellator.getBuffer();
      GlStateManager.enableBlend();
      GlStateManager.disableTexture2D();
      GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
      bufferbuilder.begin(7, DefaultVertexFormats.POSITION_COLOR);
      bufferbuilder.pos((double)x, (double)h, 0.0D).color(red, green, blue, alpha).endVertex();
      bufferbuilder.pos((double)w, (double)h, 0.0D).color(red, green, blue, alpha).endVertex();
      bufferbuilder.pos((double)w, (double)y, 0.0D).color(red, green, blue, alpha).endVertex();
      bufferbuilder.pos((double)x, (double)y, 0.0D).color(red, green, blue, alpha).endVertex();
      tessellator.draw();
      GlStateManager.enableTexture2D();
      GlStateManager.disableBlend();
   }

   public static void drawColorBox(AxisAlignedBB axisalignedbb, float red, float green, float blue, float alpha) {
      Tessellator ts = Tessellator.getInstance();
      BufferBuilder vb = ts.getBuffer();
      vb.begin(7, DefaultVertexFormats.POSITION_TEX);
      vb.pos(axisalignedbb.minX, axisalignedbb.minY, axisalignedbb.minZ).color(red, green, blue, alpha).endVertex();
      vb.pos(axisalignedbb.minX, axisalignedbb.maxY, axisalignedbb.minZ).color(red, green, blue, alpha).endVertex();
      vb.pos(axisalignedbb.maxX, axisalignedbb.minY, axisalignedbb.minZ).color(red, green, blue, alpha).endVertex();
      vb.pos(axisalignedbb.maxX, axisalignedbb.maxY, axisalignedbb.minZ).color(red, green, blue, alpha).endVertex();
      vb.pos(axisalignedbb.maxX, axisalignedbb.minY, axisalignedbb.maxZ).color(red, green, blue, alpha).endVertex();
      vb.pos(axisalignedbb.maxX, axisalignedbb.maxY, axisalignedbb.maxZ).color(red, green, blue, alpha).endVertex();
      vb.pos(axisalignedbb.minX, axisalignedbb.minY, axisalignedbb.maxZ).color(red, green, blue, alpha).endVertex();
      vb.pos(axisalignedbb.minX, axisalignedbb.maxY, axisalignedbb.maxZ).color(red, green, blue, alpha).endVertex();
      ts.draw();
      vb.begin(7, DefaultVertexFormats.POSITION_TEX);
      vb.pos(axisalignedbb.maxX, axisalignedbb.maxY, axisalignedbb.minZ).color(red, green, blue, alpha).endVertex();
      vb.pos(axisalignedbb.maxX, axisalignedbb.minY, axisalignedbb.minZ).color(red, green, blue, alpha).endVertex();
      vb.pos(axisalignedbb.minX, axisalignedbb.maxY, axisalignedbb.minZ).color(red, green, blue, alpha).endVertex();
      vb.pos(axisalignedbb.minX, axisalignedbb.minY, axisalignedbb.minZ).color(red, green, blue, alpha).endVertex();
      vb.pos(axisalignedbb.minX, axisalignedbb.maxY, axisalignedbb.maxZ).color(red, green, blue, alpha).endVertex();
      vb.pos(axisalignedbb.minX, axisalignedbb.minY, axisalignedbb.maxZ).color(red, green, blue, alpha).endVertex();
      vb.pos(axisalignedbb.maxX, axisalignedbb.maxY, axisalignedbb.maxZ).color(red, green, blue, alpha).endVertex();
      vb.pos(axisalignedbb.maxX, axisalignedbb.minY, axisalignedbb.maxZ).color(red, green, blue, alpha).endVertex();
      ts.draw();
      vb.begin(7, DefaultVertexFormats.POSITION_TEX);
      vb.pos(axisalignedbb.minX, axisalignedbb.maxY, axisalignedbb.minZ).color(red, green, blue, alpha).endVertex();
      vb.pos(axisalignedbb.maxX, axisalignedbb.maxY, axisalignedbb.minZ).color(red, green, blue, alpha).endVertex();
      vb.pos(axisalignedbb.maxX, axisalignedbb.maxY, axisalignedbb.maxZ).color(red, green, blue, alpha).endVertex();
      vb.pos(axisalignedbb.minX, axisalignedbb.maxY, axisalignedbb.maxZ).color(red, green, blue, alpha).endVertex();
      vb.pos(axisalignedbb.minX, axisalignedbb.maxY, axisalignedbb.minZ).color(red, green, blue, alpha).endVertex();
      vb.pos(axisalignedbb.minX, axisalignedbb.maxY, axisalignedbb.maxZ).color(red, green, blue, alpha).endVertex();
      vb.pos(axisalignedbb.maxX, axisalignedbb.maxY, axisalignedbb.maxZ).color(red, green, blue, alpha).endVertex();
      vb.pos(axisalignedbb.maxX, axisalignedbb.maxY, axisalignedbb.minZ).color(red, green, blue, alpha).endVertex();
      ts.draw();
      vb.begin(7, DefaultVertexFormats.POSITION_TEX);
      vb.pos(axisalignedbb.minX, axisalignedbb.minY, axisalignedbb.minZ).color(red, green, blue, alpha).endVertex();
      vb.pos(axisalignedbb.maxX, axisalignedbb.minY, axisalignedbb.minZ).color(red, green, blue, alpha).endVertex();
      vb.pos(axisalignedbb.maxX, axisalignedbb.minY, axisalignedbb.maxZ).color(red, green, blue, alpha).endVertex();
      vb.pos(axisalignedbb.minX, axisalignedbb.minY, axisalignedbb.maxZ).color(red, green, blue, alpha).endVertex();
      vb.pos(axisalignedbb.minX, axisalignedbb.minY, axisalignedbb.minZ).color(red, green, blue, alpha).endVertex();
      vb.pos(axisalignedbb.minX, axisalignedbb.minY, axisalignedbb.maxZ).color(red, green, blue, alpha).endVertex();
      vb.pos(axisalignedbb.maxX, axisalignedbb.minY, axisalignedbb.maxZ).color(red, green, blue, alpha).endVertex();
      vb.pos(axisalignedbb.maxX, axisalignedbb.minY, axisalignedbb.minZ).color(red, green, blue, alpha).endVertex();
      ts.draw();
      vb.begin(7, DefaultVertexFormats.POSITION_TEX);
      vb.pos(axisalignedbb.minX, axisalignedbb.minY, axisalignedbb.minZ).color(red, green, blue, alpha).endVertex();
      vb.pos(axisalignedbb.minX, axisalignedbb.maxY, axisalignedbb.minZ).color(red, green, blue, alpha).endVertex();
      vb.pos(axisalignedbb.minX, axisalignedbb.minY, axisalignedbb.maxZ).color(red, green, blue, alpha).endVertex();
      vb.pos(axisalignedbb.minX, axisalignedbb.maxY, axisalignedbb.maxZ).color(red, green, blue, alpha).endVertex();
      vb.pos(axisalignedbb.maxX, axisalignedbb.minY, axisalignedbb.maxZ).color(red, green, blue, alpha).endVertex();
      vb.pos(axisalignedbb.maxX, axisalignedbb.maxY, axisalignedbb.maxZ).color(red, green, blue, alpha).endVertex();
      vb.pos(axisalignedbb.maxX, axisalignedbb.minY, axisalignedbb.minZ).color(red, green, blue, alpha).endVertex();
      vb.pos(axisalignedbb.maxX, axisalignedbb.maxY, axisalignedbb.minZ).color(red, green, blue, alpha).endVertex();
      ts.draw();
      vb.begin(7, DefaultVertexFormats.POSITION_TEX);
      vb.pos(axisalignedbb.minX, axisalignedbb.maxY, axisalignedbb.maxZ).color(red, green, blue, alpha).endVertex();
      vb.pos(axisalignedbb.minX, axisalignedbb.minY, axisalignedbb.maxZ).color(red, green, blue, alpha).endVertex();
      vb.pos(axisalignedbb.minX, axisalignedbb.maxY, axisalignedbb.minZ).color(red, green, blue, alpha).endVertex();
      vb.pos(axisalignedbb.minX, axisalignedbb.minY, axisalignedbb.minZ).color(red, green, blue, alpha).endVertex();
      vb.pos(axisalignedbb.maxX, axisalignedbb.maxY, axisalignedbb.minZ).color(red, green, blue, alpha).endVertex();
      vb.pos(axisalignedbb.maxX, axisalignedbb.minY, axisalignedbb.minZ).color(red, green, blue, alpha).endVertex();
      vb.pos(axisalignedbb.maxX, axisalignedbb.maxY, axisalignedbb.maxZ).color(red, green, blue, alpha).endVertex();
      vb.pos(axisalignedbb.maxX, axisalignedbb.minY, axisalignedbb.maxZ).color(red, green, blue, alpha).endVertex();
      ts.draw();
   }

   public static void drawSelectionBoundingBox(AxisAlignedBB boundingBox) {
      Tessellator tessellator = Tessellator.getInstance();
      BufferBuilder vertexbuffer = tessellator.getBuffer();
      vertexbuffer.begin(3, DefaultVertexFormats.POSITION);
      vertexbuffer.pos(boundingBox.minX, boundingBox.minY, boundingBox.minZ).endVertex();
      vertexbuffer.pos(boundingBox.maxX, boundingBox.minY, boundingBox.minZ).endVertex();
      vertexbuffer.pos(boundingBox.maxX, boundingBox.minY, boundingBox.maxZ).endVertex();
      vertexbuffer.pos(boundingBox.minX, boundingBox.minY, boundingBox.maxZ).endVertex();
      vertexbuffer.pos(boundingBox.minX, boundingBox.minY, boundingBox.minZ).endVertex();
      tessellator.draw();
      vertexbuffer.begin(3, DefaultVertexFormats.POSITION);
      vertexbuffer.pos(boundingBox.minX, boundingBox.maxY, boundingBox.minZ).endVertex();
      vertexbuffer.pos(boundingBox.maxX, boundingBox.maxY, boundingBox.minZ).endVertex();
      vertexbuffer.pos(boundingBox.maxX, boundingBox.maxY, boundingBox.maxZ).endVertex();
      vertexbuffer.pos(boundingBox.minX, boundingBox.maxY, boundingBox.maxZ).endVertex();
      vertexbuffer.pos(boundingBox.minX, boundingBox.maxY, boundingBox.minZ).endVertex();
      tessellator.draw();
      vertexbuffer.begin(1, DefaultVertexFormats.POSITION);
      vertexbuffer.pos(boundingBox.minX, boundingBox.minY, boundingBox.minZ).endVertex();
      vertexbuffer.pos(boundingBox.minX, boundingBox.maxY, boundingBox.minZ).endVertex();
      vertexbuffer.pos(boundingBox.maxX, boundingBox.minY, boundingBox.minZ).endVertex();
      vertexbuffer.pos(boundingBox.maxX, boundingBox.maxY, boundingBox.minZ).endVertex();
      vertexbuffer.pos(boundingBox.maxX, boundingBox.minY, boundingBox.maxZ).endVertex();
      vertexbuffer.pos(boundingBox.maxX, boundingBox.maxY, boundingBox.maxZ).endVertex();
      vertexbuffer.pos(boundingBox.minX, boundingBox.minY, boundingBox.maxZ).endVertex();
      vertexbuffer.pos(boundingBox.minX, boundingBox.maxY, boundingBox.maxZ).endVertex();
      tessellator.draw();
   }

   public static void glrendermethod() {
      GL11.glEnable(3042);
      GL11.glBlendFunc(770, 771);
      GL11.glEnable(2848);
      GL11.glLineWidth(2.0F);
      GL11.glDisable(3553);
      GL11.glEnable(2884);
      GL11.glDisable(2929);
      double viewerPosX = mc.getRenderManager().viewerPosX;
      double viewerPosY = mc.getRenderManager().viewerPosY;
      double viewerPosZ = mc.getRenderManager().viewerPosZ;
      GL11.glPushMatrix();
      GL11.glTranslated(-viewerPosX, -viewerPosY, -viewerPosZ);
   }

   public static void glStart(float n, float n2, float n3, float n4) {
      glrendermethod();
      GL11.glColor4f(n, n2, n3, n4);
   }

   public static void glEnd() {
      GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
      GL11.glPopMatrix();
      GL11.glEnable(2929);
      GL11.glEnable(3553);
      GL11.glDisable(3042);
      GL11.glDisable(2848);
   }

   public static AxisAlignedBB getBoundingBox(BlockPos blockPos) {
      return mc.world.getBlockState(blockPos).getBoundingBox(mc.world, blockPos).offset(blockPos);
   }

   public static void drawOutlinedBox(AxisAlignedBB axisAlignedBB) {
      GL11.glBegin(1);
      GL11.glVertex3d(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.minZ);
      GL11.glVertex3d(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.minZ);
      GL11.glVertex3d(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.minZ);
      GL11.glVertex3d(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.maxZ);
      GL11.glVertex3d(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.maxZ);
      GL11.glVertex3d(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.maxZ);
      GL11.glVertex3d(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.maxZ);
      GL11.glVertex3d(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.minZ);
      GL11.glVertex3d(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.minZ);
      GL11.glVertex3d(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.minZ);
      GL11.glVertex3d(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.minZ);
      GL11.glVertex3d(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.minZ);
      GL11.glVertex3d(axisAlignedBB.maxX, axisAlignedBB.minY, axisAlignedBB.maxZ);
      GL11.glVertex3d(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.maxZ);
      GL11.glVertex3d(axisAlignedBB.minX, axisAlignedBB.minY, axisAlignedBB.maxZ);
      GL11.glVertex3d(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.maxZ);
      GL11.glVertex3d(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.minZ);
      GL11.glVertex3d(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.minZ);
      GL11.glVertex3d(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.minZ);
      GL11.glVertex3d(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.maxZ);
      GL11.glVertex3d(axisAlignedBB.maxX, axisAlignedBB.maxY, axisAlignedBB.maxZ);
      GL11.glVertex3d(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.maxZ);
      GL11.glVertex3d(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.maxZ);
      GL11.glVertex3d(axisAlignedBB.minX, axisAlignedBB.maxY, axisAlignedBB.minZ);
      GL11.glEnd();
   }

   public static void drawFilledBoxESPN(BlockPos pos, Color color) {
      AxisAlignedBB bb = new AxisAlignedBB((double)pos.getX() - mc.getRenderManager().viewerPosX, (double)pos.getY() - mc.getRenderManager().viewerPosY, (double)pos.getZ() - mc.getRenderManager().viewerPosZ, (double)(pos.getX() + 1) - mc.getRenderManager().viewerPosX, (double)(pos.getY() + 1) - mc.getRenderManager().viewerPosY, (double)(pos.getZ() + 1) - mc.getRenderManager().viewerPosZ);
      int rgba = ColorUtil.toRGBA(color);
      drawFilledBox(bb, rgba);
   }

   public static void drawFilledBox(AxisAlignedBB bb, int color) {
      GlStateManager.pushMatrix();
      GlStateManager.enableBlend();
      GlStateManager.disableDepth();
      GlStateManager.tryBlendFuncSeparate(770, 771, 0, 1);
      GlStateManager.disableTexture2D();
      GlStateManager.depthMask(false);
      float alpha = (float)(color >> 24 & 255) / 255.0F;
      float red = (float)(color >> 16 & 255) / 255.0F;
      float green = (float)(color >> 8 & 255) / 255.0F;
      float blue = (float)(color & 255) / 255.0F;
      Tessellator tessellator = Tessellator.getInstance();
      BufferBuilder bufferbuilder = tessellator.getBuffer();
      bufferbuilder.begin(7, DefaultVertexFormats.POSITION_COLOR);
      bufferbuilder.pos(bb.minX, bb.minY, bb.minZ).color(red, green, blue, alpha).endVertex();
      bufferbuilder.pos(bb.maxX, bb.minY, bb.minZ).color(red, green, blue, alpha).endVertex();
      bufferbuilder.pos(bb.maxX, bb.minY, bb.maxZ).color(red, green, blue, alpha).endVertex();
      bufferbuilder.pos(bb.minX, bb.minY, bb.maxZ).color(red, green, blue, alpha).endVertex();
      bufferbuilder.pos(bb.minX, bb.maxY, bb.minZ).color(red, green, blue, alpha).endVertex();
      bufferbuilder.pos(bb.minX, bb.maxY, bb.maxZ).color(red, green, blue, alpha).endVertex();
      bufferbuilder.pos(bb.maxX, bb.maxY, bb.maxZ).color(red, green, blue, alpha).endVertex();
      bufferbuilder.pos(bb.maxX, bb.maxY, bb.minZ).color(red, green, blue, alpha).endVertex();
      bufferbuilder.pos(bb.minX, bb.minY, bb.minZ).color(red, green, blue, alpha).endVertex();
      bufferbuilder.pos(bb.minX, bb.maxY, bb.minZ).color(red, green, blue, alpha).endVertex();
      bufferbuilder.pos(bb.maxX, bb.maxY, bb.minZ).color(red, green, blue, alpha).endVertex();
      bufferbuilder.pos(bb.maxX, bb.minY, bb.minZ).color(red, green, blue, alpha).endVertex();
      bufferbuilder.pos(bb.maxX, bb.minY, bb.minZ).color(red, green, blue, alpha).endVertex();
      bufferbuilder.pos(bb.maxX, bb.maxY, bb.minZ).color(red, green, blue, alpha).endVertex();
      bufferbuilder.pos(bb.maxX, bb.maxY, bb.maxZ).color(red, green, blue, alpha).endVertex();
      bufferbuilder.pos(bb.maxX, bb.minY, bb.maxZ).color(red, green, blue, alpha).endVertex();
      bufferbuilder.pos(bb.minX, bb.minY, bb.maxZ).color(red, green, blue, alpha).endVertex();
      bufferbuilder.pos(bb.maxX, bb.minY, bb.maxZ).color(red, green, blue, alpha).endVertex();
      bufferbuilder.pos(bb.maxX, bb.maxY, bb.maxZ).color(red, green, blue, alpha).endVertex();
      bufferbuilder.pos(bb.minX, bb.maxY, bb.maxZ).color(red, green, blue, alpha).endVertex();
      bufferbuilder.pos(bb.minX, bb.minY, bb.minZ).color(red, green, blue, alpha).endVertex();
      bufferbuilder.pos(bb.minX, bb.minY, bb.maxZ).color(red, green, blue, alpha).endVertex();
      bufferbuilder.pos(bb.minX, bb.maxY, bb.maxZ).color(red, green, blue, alpha).endVertex();
      bufferbuilder.pos(bb.minX, bb.maxY, bb.minZ).color(red, green, blue, alpha).endVertex();
      tessellator.draw();
      GlStateManager.depthMask(true);
      GlStateManager.enableDepth();
      GlStateManager.enableTexture2D();
      GlStateManager.disableBlend();
      GlStateManager.popMatrix();
   }

   public static void drawBoundingBox(AxisAlignedBB bb, float width, int color) {
      GlStateManager.pushMatrix();
      GlStateManager.enableBlend();
      GlStateManager.disableDepth();
      GlStateManager.tryBlendFuncSeparate(770, 771, 0, 1);
      GlStateManager.disableTexture2D();
      GlStateManager.depthMask(false);
      GL11.glEnable(2848);
      GL11.glHint(3154, 4354);
      GL11.glLineWidth(width);
      float alpha = (float)(color >> 24 & 255) / 255.0F;
      float red = (float)(color >> 16 & 255) / 255.0F;
      float green = (float)(color >> 8 & 255) / 255.0F;
      float blue = (float)(color & 255) / 255.0F;
      Tessellator tessellator = Tessellator.getInstance();
      BufferBuilder bufferbuilder = tessellator.getBuffer();
      bufferbuilder.begin(3, DefaultVertexFormats.POSITION_COLOR);
      bufferbuilder.pos(bb.minX, bb.minY, bb.minZ).color(red, green, blue, alpha).endVertex();
      bufferbuilder.pos(bb.minX, bb.minY, bb.maxZ).color(red, green, blue, alpha).endVertex();
      bufferbuilder.pos(bb.maxX, bb.minY, bb.maxZ).color(red, green, blue, alpha).endVertex();
      bufferbuilder.pos(bb.maxX, bb.minY, bb.minZ).color(red, green, blue, alpha).endVertex();
      bufferbuilder.pos(bb.minX, bb.minY, bb.minZ).color(red, green, blue, alpha).endVertex();
      bufferbuilder.pos(bb.minX, bb.maxY, bb.minZ).color(red, green, blue, alpha).endVertex();
      bufferbuilder.pos(bb.minX, bb.maxY, bb.maxZ).color(red, green, blue, alpha).endVertex();
      bufferbuilder.pos(bb.minX, bb.minY, bb.maxZ).color(red, green, blue, alpha).endVertex();
      bufferbuilder.pos(bb.maxX, bb.minY, bb.maxZ).color(red, green, blue, alpha).endVertex();
      bufferbuilder.pos(bb.maxX, bb.maxY, bb.maxZ).color(red, green, blue, alpha).endVertex();
      bufferbuilder.pos(bb.minX, bb.maxY, bb.maxZ).color(red, green, blue, alpha).endVertex();
      bufferbuilder.pos(bb.maxX, bb.maxY, bb.maxZ).color(red, green, blue, alpha).endVertex();
      bufferbuilder.pos(bb.maxX, bb.maxY, bb.minZ).color(red, green, blue, alpha).endVertex();
      bufferbuilder.pos(bb.maxX, bb.minY, bb.minZ).color(red, green, blue, alpha).endVertex();
      bufferbuilder.pos(bb.maxX, bb.maxY, bb.minZ).color(red, green, blue, alpha).endVertex();
      bufferbuilder.pos(bb.minX, bb.maxY, bb.minZ).color(red, green, blue, alpha).endVertex();
      tessellator.draw();
      GL11.glDisable(2848);
      GlStateManager.depthMask(true);
      GlStateManager.enableDepth();
      GlStateManager.enableTexture2D();
      GlStateManager.disableBlend();
      GlStateManager.popMatrix();
   }

   public static void glBillboard(float x, float y, float z) {
      float scale = 0.02666667F;
      GlStateManager.translate((double)x - mc.getRenderManager().renderPosX, (double)y - mc.getRenderManager().renderPosY, (double)z - mc.getRenderManager().renderPosZ);
      GlStateManager.glNormal3f(0.0F, 1.0F, 0.0F);
      GlStateManager.rotate(-mc.player.rotationYaw, 0.0F, 1.0F, 0.0F);
      GlStateManager.rotate(mc.player.rotationPitch, mc.gameSettings.thirdPersonView == 2 ? -1.0F : 1.0F, 0.0F, 0.0F);
      GlStateManager.scale(-scale, -scale, scale);
   }

   public static void glBillboardDistanceScaled(float x, float y, float z, EntityPlayer player, float scale) {
      glBillboard(x, y, z);
      int distance = (int)player.getDistance((double)x, (double)y, (double)z);
      float scaleDistance = (float)distance / 2.0F / (2.0F + (2.0F - scale));
      if (scaleDistance < 1.0F) {
         scaleDistance = 1.0F;
      }

      GlStateManager.scale(scaleDistance, scaleDistance, scaleDistance);
   }

   public static void drawColoredBoundingBox(AxisAlignedBB bb, float width, float red, float green, float blue, float alpha) {
      GlStateManager.pushMatrix();
      GlStateManager.enableBlend();
      GlStateManager.disableDepth();
      GlStateManager.tryBlendFuncSeparate(770, 771, 0, 1);
      GlStateManager.disableTexture2D();
      GlStateManager.depthMask(false);
      GL11.glEnable(2848);
      GL11.glHint(3154, 4354);
      GL11.glLineWidth(width);
      Tessellator tessellator = Tessellator.getInstance();
      BufferBuilder bufferbuilder = tessellator.getBuffer();
      bufferbuilder.begin(3, DefaultVertexFormats.POSITION_COLOR);
      bufferbuilder.pos(bb.minX, bb.minY, bb.minZ).color(red, green, blue, 0.0F).endVertex();
      bufferbuilder.pos(bb.minX, bb.minY, bb.minZ).color(red, green, blue, alpha).endVertex();
      bufferbuilder.pos(bb.maxX, bb.minY, bb.minZ).color(red, green, blue, alpha).endVertex();
      bufferbuilder.pos(bb.maxX, bb.minY, bb.maxZ).color(red, green, blue, alpha).endVertex();
      bufferbuilder.pos(bb.minX, bb.minY, bb.maxZ).color(red, green, blue, alpha).endVertex();
      bufferbuilder.pos(bb.minX, bb.minY, bb.minZ).color(red, green, blue, alpha).endVertex();
      bufferbuilder.pos(bb.minX, bb.maxY, bb.minZ).color(red, green, blue, alpha).endVertex();
      bufferbuilder.pos(bb.maxX, bb.maxY, bb.minZ).color(red, green, blue, alpha).endVertex();
      bufferbuilder.pos(bb.maxX, bb.maxY, bb.maxZ).color(red, green, blue, alpha).endVertex();
      bufferbuilder.pos(bb.minX, bb.maxY, bb.maxZ).color(red, green, blue, alpha).endVertex();
      bufferbuilder.pos(bb.minX, bb.maxY, bb.minZ).color(red, green, blue, alpha).endVertex();
      bufferbuilder.pos(bb.minX, bb.maxY, bb.maxZ).color(red, green, blue, 0.0F).endVertex();
      bufferbuilder.pos(bb.minX, bb.minY, bb.maxZ).color(red, green, blue, alpha).endVertex();
      bufferbuilder.pos(bb.maxX, bb.maxY, bb.maxZ).color(red, green, blue, 0.0F).endVertex();
      bufferbuilder.pos(bb.maxX, bb.minY, bb.maxZ).color(red, green, blue, alpha).endVertex();
      bufferbuilder.pos(bb.maxX, bb.maxY, bb.minZ).color(red, green, blue, 0.0F).endVertex();
      bufferbuilder.pos(bb.maxX, bb.minY, bb.minZ).color(red, green, blue, alpha).endVertex();
      bufferbuilder.pos(bb.maxX, bb.minY, bb.minZ).color(red, green, blue, 0.0F).endVertex();
      tessellator.draw();
      GL11.glDisable(2848);
      GlStateManager.depthMask(true);
      GlStateManager.enableDepth();
      GlStateManager.enableTexture2D();
      GlStateManager.disableBlend();
      GlStateManager.popMatrix();
   }

   public static void drawSphere(double x, double y, double z, float size, int slices, int stacks) {
      Sphere s = new Sphere();
      GL11.glPushMatrix();
      GL11.glBlendFunc(770, 771);
      GL11.glEnable(3042);
      GL11.glLineWidth(1.2F);
      GL11.glDisable(3553);
      GL11.glDisable(2929);
      GL11.glDepthMask(false);
      s.setDrawStyle(100013);
      GL11.glTranslated(x - mc.renderManager.renderPosX, y - mc.renderManager.renderPosY, z - mc.renderManager.renderPosZ);
      s.draw(size, slices, stacks);
      GL11.glLineWidth(2.0F);
      GL11.glEnable(3553);
      GL11.glEnable(2929);
      GL11.glDepthMask(true);
      GL11.glDisable(3042);
      GL11.glPopMatrix();
   }

   public static void GLPre(float lineWidth) {
      depth = GL11.glIsEnabled(2896);
      texture = GL11.glIsEnabled(3042);
      clean = GL11.glIsEnabled(3553);
      bind = GL11.glIsEnabled(2929);
      override = GL11.glIsEnabled(2848);
      GLPre(depth, texture, clean, bind, override, lineWidth);
   }

   public static void GlPost() {
      GLPost(depth, texture, clean, bind, override);
   }

   private static void GLPre(boolean depth, boolean texture, boolean clean, boolean bind, boolean override, float lineWidth) {
      if (depth) {
         GL11.glDisable(2896);
      }

      if (!texture) {
         GL11.glEnable(3042);
      }

      GL11.glLineWidth(lineWidth);
      if (clean) {
         GL11.glDisable(3553);
      }

      if (bind) {
         GL11.glDisable(2929);
      }

      if (!override) {
         GL11.glEnable(2848);
      }

      GlStateManager.blendFunc(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA);
      GL11.glHint(3154, 4354);
      GlStateManager.depthMask(false);
   }

   public static float[][] getBipedRotations(ModelBiped biped) {
      float[][] rotations = new float[5][];
      float[] headRotation = new float[]{biped.bipedHead.rotateAngleX, biped.bipedHead.rotateAngleY, biped.bipedHead.rotateAngleZ};
      rotations[0] = headRotation;
      float[] rightArmRotation = new float[]{biped.bipedRightArm.rotateAngleX, biped.bipedRightArm.rotateAngleY, biped.bipedRightArm.rotateAngleZ};
      rotations[1] = rightArmRotation;
      float[] leftArmRotation = new float[]{biped.bipedLeftArm.rotateAngleX, biped.bipedLeftArm.rotateAngleY, biped.bipedLeftArm.rotateAngleZ};
      rotations[2] = leftArmRotation;
      float[] rightLegRotation = new float[]{biped.bipedRightLeg.rotateAngleX, biped.bipedRightLeg.rotateAngleY, biped.bipedRightLeg.rotateAngleZ};
      rotations[3] = rightLegRotation;
      float[] leftLegRotation = new float[]{biped.bipedLeftLeg.rotateAngleX, biped.bipedLeftLeg.rotateAngleY, biped.bipedLeftLeg.rotateAngleZ};
      rotations[4] = leftLegRotation;
      return rotations;
   }

   private static void GLPost(boolean depth, boolean texture, boolean clean, boolean bind, boolean override) {
      GlStateManager.depthMask(true);
      if (!override) {
         GL11.glDisable(2848);
      }

      if (bind) {
         GL11.glEnable(2929);
      }

      if (clean) {
         GL11.glEnable(3553);
      }

      if (!texture) {
         GL11.glDisable(3042);
      }

      if (depth) {
         GL11.glEnable(2896);
      }

   }

   public static void drawArc(float cx, float cy, float r, float start_angle, float end_angle, int num_segments) {
      GL11.glBegin(4);

      for(int i = (int)((float)num_segments / (360.0F / start_angle)) + 1; (float)i <= (float)num_segments / (360.0F / end_angle); ++i) {
         double previousangle = 6.283185307179586D * (double)(i - 1) / (double)num_segments;
         double angle = 6.283185307179586D * (double)i / (double)num_segments;
         GL11.glVertex2d((double)cx, (double)cy);
         GL11.glVertex2d((double)cx + Math.cos(angle) * (double)r, (double)cy + Math.sin(angle) * (double)r);
         GL11.glVertex2d((double)cx + Math.cos(previousangle) * (double)r, (double)cy + Math.sin(previousangle) * (double)r);
      }

      glEnd();
   }

   public static void drawArcOutline(float cx, float cy, float r, float start_angle, float end_angle, int num_segments) {
      GL11.glBegin(2);

      for(int i = (int)((float)num_segments / (360.0F / start_angle)) + 1; (float)i <= (float)num_segments / (360.0F / end_angle); ++i) {
         double angle = 6.283185307179586D * (double)i / (double)num_segments;
         GL11.glVertex2d((double)cx + Math.cos(angle) * (double)r, (double)cy + Math.sin(angle) * (double)r);
      }

      glEnd();
   }

   public static void drawCircleOutline(float x, float y, float radius) {
      drawCircleOutline(x, y, radius, 0, 360, 40);
   }

   public static void drawCircleOutline(float x, float y, float radius, int start, int end, int segments) {
      drawArcOutline(x, y, radius, (float)start, (float)end, segments);
   }

   public static void drawCircle(float x, float y, float radius) {
      drawCircle(x, y, radius, 0, 360, 64);
   }

   public static void drawCircle(float x, float y, float radius, int start, int end, int segments) {
      drawArc(x, y, radius, (float)start, (float)end, segments);
   }

   public static void drawOutlinedRoundedRectangle(int x, int y, int width, int height, float radius, float dR, float dG, float dB, float dA, float outlineWidth) {
      drawRoundedRectangle((float)x, (float)y, (float)width, (float)height, radius);
      GL11.glColor4f(dR, dG, dB, dA);
      drawRoundedRectangle((float)x + outlineWidth, (float)y + outlineWidth, (float)width - outlineWidth * 2.0F, (float)height - outlineWidth * 2.0F, radius);
   }

   public static void drawRectangle(float x, float y, float width, float height) {
      GL11.glEnable(3042);
      GL11.glBlendFunc(770, 771);
      GL11.glBegin(2);
      GL11.glVertex2d((double)width, 0.0D);
      GL11.glVertex2d(0.0D, 0.0D);
      GL11.glVertex2d(0.0D, (double)height);
      GL11.glVertex2d((double)width, (double)height);
      glEnd();
   }

   public static void drawRectangleXY(float x, float y, float width, float height) {
      GL11.glEnable(3042);
      GL11.glBlendFunc(770, 771);
      GL11.glBegin(2);
      GL11.glVertex2d((double)(x + width), (double)y);
      GL11.glVertex2d((double)x, (double)y);
      GL11.glVertex2d((double)x, (double)(y + height));
      GL11.glVertex2d((double)(x + width), (double)(y + height));
      glEnd();
   }

   public static void drawFilledRectangle(float x, float y, float width, float height) {
      GL11.glEnable(3042);
      GL11.glBlendFunc(770, 771);
      GL11.glBegin(7);
      GL11.glVertex2d((double)(x + width), (double)y);
      GL11.glVertex2d((double)x, (double)y);
      GL11.glVertex2d((double)x, (double)(y + height));
      GL11.glVertex2d((double)(x + width), (double)(y + height));
      glEnd();
   }

   public static void drawRoundedRectangle(float x, float y, float width, float height, float radius) {
      GL11.glEnable(3042);
      drawArc(x + width - radius, y + height - radius, radius, 0.0F, 90.0F, 16);
      drawArc(x + radius, y + height - radius, radius, 90.0F, 180.0F, 16);
      drawArc(x + radius, y + radius, radius, 180.0F, 270.0F, 16);
      drawArc(x + width - radius, y + radius, radius, 270.0F, 360.0F, 16);
      GL11.glBegin(4);
      GL11.glVertex2d((double)(x + width - radius), (double)y);
      GL11.glVertex2d((double)(x + radius), (double)y);
      GL11.glVertex2d((double)(x + width - radius), (double)(y + radius));
      GL11.glVertex2d((double)(x + width - radius), (double)(y + radius));
      GL11.glVertex2d((double)(x + radius), (double)y);
      GL11.glVertex2d((double)(x + radius), (double)(y + radius));
      GL11.glVertex2d((double)(x + width), (double)(y + radius));
      GL11.glVertex2d((double)x, (double)(y + radius));
      GL11.glVertex2d((double)x, (double)(y + height - radius));
      GL11.glVertex2d((double)(x + width), (double)(y + radius));
      GL11.glVertex2d((double)x, (double)(y + height - radius));
      GL11.glVertex2d((double)(x + width), (double)(y + height - radius));
      GL11.glVertex2d((double)(x + width - radius), (double)(y + height - radius));
      GL11.glVertex2d((double)(x + radius), (double)(y + height - radius));
      GL11.glVertex2d((double)(x + width - radius), (double)(y + height));
      GL11.glVertex2d((double)(x + width - radius), (double)(y + height));
      GL11.glVertex2d((double)(x + radius), (double)(y + height - radius));
      GL11.glVertex2d((double)(x + radius), (double)(y + height));
      glEnd();
   }

   public static void renderOne(float lineWidth) {
      checkSetupFBO();
      GL11.glPushAttrib(1048575);
      GL11.glDisable(3008);
      GL11.glDisable(3553);
      GL11.glDisable(2896);
      GL11.glEnable(3042);
      GL11.glBlendFunc(770, 771);
      GL11.glLineWidth(lineWidth);
      GL11.glEnable(2848);
      GL11.glEnable(2960);
      GL11.glClear(1024);
      GL11.glClearStencil(15);
      GL11.glStencilFunc(512, 1, 15);
      GL11.glStencilOp(7681, 7681, 7681);
      GL11.glPolygonMode(1032, 6913);
   }

   public static void renderTwo() {
      GL11.glStencilFunc(512, 0, 15);
      GL11.glStencilOp(7681, 7681, 7681);
      GL11.glPolygonMode(1032, 6914);
   }

   public static void renderThree() {
      GL11.glStencilFunc(514, 1, 15);
      GL11.glStencilOp(7680, 7680, 7680);
      GL11.glPolygonMode(1032, 6913);
   }

   public static void renderFour(Color color) {
      setColor(color);
      GL11.glDepthMask(false);
      GL11.glDisable(2929);
      GL11.glEnable(10754);
      GL11.glPolygonOffset(1.0F, -2000000.0F);
      OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240.0F, 240.0F);
   }

   public static void renderFive() {
      GL11.glPolygonOffset(1.0F, 2000000.0F);
      GL11.glDisable(10754);
      GL11.glEnable(2929);
      GL11.glDepthMask(true);
      GL11.glDisable(2960);
      GL11.glDisable(2848);
      GL11.glHint(3154, 4352);
      GL11.glEnable(3042);
      GL11.glEnable(2896);
      GL11.glEnable(3553);
      GL11.glEnable(3008);
      GL11.glPopAttrib();
   }

   public static void setColor(Color color) {
      GL11.glColor4d((double)color.getRed() / 255.0D, (double)color.getGreen() / 255.0D, (double)color.getBlue() / 255.0D, (double)color.getAlpha() / 255.0D);
   }

   public static void checkSetupFBO() {
      Framebuffer fbo = mc.framebuffer;
      if (fbo != null && fbo.depthBuffer > -1) {
         setupFBO(fbo);
         fbo.depthBuffer = -1;
      }

   }

   private static void setupFBO(Framebuffer fbo) {
      EXTFramebufferObject.glDeleteRenderbuffersEXT(fbo.depthBuffer);
      int stencilDepthBufferID = EXTFramebufferObject.glGenRenderbuffersEXT();
      EXTFramebufferObject.glBindRenderbufferEXT(36161, stencilDepthBufferID);
      EXTFramebufferObject.glRenderbufferStorageEXT(36161, 34041, mc.displayWidth, mc.displayHeight);
      EXTFramebufferObject.glFramebufferRenderbufferEXT(36160, 36128, 36161, stencilDepthBufferID);
      EXTFramebufferObject.glFramebufferRenderbufferEXT(36160, 36096, 36161, stencilDepthBufferID);
   }

   static {
      itemRender = mc.getItemRenderer().itemRenderer;
      camera = new Frustum();
      depth = GL11.glIsEnabled(2896);
      texture = GL11.glIsEnabled(3042);
      clean = GL11.glIsEnabled(3553);
      bind = GL11.glIsEnabled(2929);
      override = GL11.glIsEnabled(2848);
      itemRender = mc.getRenderItem();
      camera = new Frustum();
      depth = GL11.glIsEnabled(2896);
      texture = GL11.glIsEnabled(3042);
      clean = GL11.glIsEnabled(3553);
      bind = GL11.glIsEnabled(2929);
      override = GL11.glIsEnabled(2848);
   }

   public static class RenderTesselator extends Tessellator {
      public static RenderUtil.RenderTesselator INSTANCE = new RenderUtil.RenderTesselator();

      public RenderTesselator() {
         super(2097152);
      }

      public static void prepare(int mode) {
         prepareGL();
         begin(mode);
      }

      public static void prepareGL() {
         GL11.glBlendFunc(770, 771);
         GlStateManager.tryBlendFuncSeparate(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA, SourceFactor.ONE, DestFactor.ZERO);
         GlStateManager.glLineWidth(1.5F);
         GlStateManager.disableTexture2D();
         GlStateManager.depthMask(false);
         GlStateManager.enableBlend();
         GlStateManager.disableDepth();
         GlStateManager.disableLighting();
         GlStateManager.disableCull();
         GlStateManager.enableAlpha();
         GlStateManager.color(1.0F, 1.0F, 1.0F);
      }

      public static void begin(int mode) {
         INSTANCE.getBuffer().begin(mode, DefaultVertexFormats.POSITION_COLOR);
      }

      public static void release() {
         render();
         releaseGL();
      }

      public static void render() {
         INSTANCE.draw();
      }

      public static void releaseGL() {
         GlStateManager.enableCull();
         GlStateManager.depthMask(true);
         GlStateManager.enableTexture2D();
         GlStateManager.enableBlend();
         GlStateManager.enableDepth();
      }

      public static void drawBox(BlockPos blockPos, int argb, int sides) {
         int a = argb >>> 24 & 255;
         int r = argb >>> 16 & 255;
         int g = argb >>> 8 & 255;
         int b = argb & 255;
         drawBox(blockPos, r, g, b, a, sides);
      }

      public static void drawBox(float x, float y, float z, int argb, int sides) {
         int a = argb >>> 24 & 255;
         int r = argb >>> 16 & 255;
         int g = argb >>> 8 & 255;
         int b = argb & 255;
         drawBox(INSTANCE.getBuffer(), x, y, z, 1.0F, 1.0F, 1.0F, r, g, b, a, sides);
      }

      public static void drawBox(BlockPos blockPos, int r, int g, int b, int a, int sides) {
         drawBox(INSTANCE.getBuffer(), (float)blockPos.getX(), (float)blockPos.getY(), (float)blockPos.getZ(), 1.0F, 1.0F, 1.0F, r, g, b, a, sides);
      }

      public static BufferBuilder getBufferBuilder() {
         return INSTANCE.getBuffer();
      }

      public static void drawBox(BufferBuilder buffer, float x, float y, float z, float w, float h, float d, int r, int g, int b, int a, int sides) {
         if ((sides & 1) != 0) {
            buffer.pos((double)(x + w), (double)y, (double)z).color(r, g, b, a).endVertex();
            buffer.pos((double)(x + w), (double)y, (double)(z + d)).color(r, g, b, a).endVertex();
            buffer.pos((double)x, (double)y, (double)(z + d)).color(r, g, b, a).endVertex();
            buffer.pos((double)x, (double)y, (double)z).color(r, g, b, a).endVertex();
         }

         if ((sides & 2) != 0) {
            buffer.pos((double)(x + w), (double)(y + h), (double)z).color(r, g, b, a).endVertex();
            buffer.pos((double)x, (double)(y + h), (double)z).color(r, g, b, a).endVertex();
            buffer.pos((double)x, (double)(y + h), (double)(z + d)).color(r, g, b, a).endVertex();
            buffer.pos((double)(x + w), (double)(y + h), (double)(z + d)).color(r, g, b, a).endVertex();
         }

         if ((sides & 4) != 0) {
            buffer.pos((double)(x + w), (double)y, (double)z).color(r, g, b, a).endVertex();
            buffer.pos((double)x, (double)y, (double)z).color(r, g, b, a).endVertex();
            buffer.pos((double)x, (double)(y + h), (double)z).color(r, g, b, a).endVertex();
            buffer.pos((double)(x + w), (double)(y + h), (double)z).color(r, g, b, a).endVertex();
         }

         if ((sides & 8) != 0) {
            buffer.pos((double)x, (double)y, (double)(z + d)).color(r, g, b, a).endVertex();
            buffer.pos((double)(x + w), (double)y, (double)(z + d)).color(r, g, b, a).endVertex();
            buffer.pos((double)(x + w), (double)(y + h), (double)(z + d)).color(r, g, b, a).endVertex();
            buffer.pos((double)x, (double)(y + h), (double)(z + d)).color(r, g, b, a).endVertex();
         }

         if ((sides & 16) != 0) {
            buffer.pos((double)x, (double)y, (double)z).color(r, g, b, a).endVertex();
            buffer.pos((double)x, (double)y, (double)(z + d)).color(r, g, b, a).endVertex();
            buffer.pos((double)x, (double)(y + h), (double)(z + d)).color(r, g, b, a).endVertex();
            buffer.pos((double)x, (double)(y + h), (double)z).color(r, g, b, a).endVertex();
         }

         if ((sides & 32) != 0) {
            buffer.pos((double)(x + w), (double)y, (double)(z + d)).color(r, g, b, a).endVertex();
            buffer.pos((double)(x + w), (double)y, (double)z).color(r, g, b, a).endVertex();
            buffer.pos((double)(x + w), (double)(y + h), (double)z).color(r, g, b, a).endVertex();
            buffer.pos((double)(x + w), (double)(y + h), (double)(z + d)).color(r, g, b, a).endVertex();
         }

      }

      public static void drawLines(BufferBuilder buffer, float x, float y, float z, float w, float h, float d, int r, int g, int b, int a, int sides) {
         if ((sides & 17) != 0) {
            buffer.pos((double)x, (double)y, (double)z).color(r, g, b, a).endVertex();
            buffer.pos((double)x, (double)y, (double)(z + d)).color(r, g, b, a).endVertex();
         }

         if ((sides & 18) != 0) {
            buffer.pos((double)x, (double)(y + h), (double)z).color(r, g, b, a).endVertex();
            buffer.pos((double)x, (double)(y + h), (double)(z + d)).color(r, g, b, a).endVertex();
         }

         if ((sides & 33) != 0) {
            buffer.pos((double)(x + w), (double)y, (double)z).color(r, g, b, a).endVertex();
            buffer.pos((double)(x + w), (double)y, (double)(z + d)).color(r, g, b, a).endVertex();
         }

         if ((sides & 34) != 0) {
            buffer.pos((double)(x + w), (double)(y + h), (double)z).color(r, g, b, a).endVertex();
            buffer.pos((double)(x + w), (double)(y + h), (double)(z + d)).color(r, g, b, a).endVertex();
         }

         if ((sides & 5) != 0) {
            buffer.pos((double)x, (double)y, (double)z).color(r, g, b, a).endVertex();
            buffer.pos((double)(x + w), (double)y, (double)z).color(r, g, b, a).endVertex();
         }

         if ((sides & 6) != 0) {
            buffer.pos((double)x, (double)(y + h), (double)z).color(r, g, b, a).endVertex();
            buffer.pos((double)(x + w), (double)(y + h), (double)z).color(r, g, b, a).endVertex();
         }

         if ((sides & 9) != 0) {
            buffer.pos((double)x, (double)y, (double)(z + d)).color(r, g, b, a).endVertex();
            buffer.pos((double)(x + w), (double)y, (double)(z + d)).color(r, g, b, a).endVertex();
         }

         if ((sides & 10) != 0) {
            buffer.pos((double)x, (double)(y + h), (double)(z + d)).color(r, g, b, a).endVertex();
            buffer.pos((double)(x + w), (double)(y + h), (double)(z + d)).color(r, g, b, a).endVertex();
         }

         if ((sides & 20) != 0) {
            buffer.pos((double)x, (double)y, (double)z).color(r, g, b, a).endVertex();
            buffer.pos((double)x, (double)(y + h), (double)z).color(r, g, b, a).endVertex();
         }

         if ((sides & 36) != 0) {
            buffer.pos((double)(x + w), (double)y, (double)z).color(r, g, b, a).endVertex();
            buffer.pos((double)(x + w), (double)(y + h), (double)z).color(r, g, b, a).endVertex();
         }

         if ((sides & 24) != 0) {
            buffer.pos((double)x, (double)y, (double)(z + d)).color(r, g, b, a).endVertex();
            buffer.pos((double)x, (double)(y + h), (double)(z + d)).color(r, g, b, a).endVertex();
         }

         if ((sides & 40) != 0) {
            buffer.pos((double)(x + w), (double)y, (double)(z + d)).color(r, g, b, a).endVertex();
            buffer.pos((double)(x + w), (double)(y + h), (double)(z + d)).color(r, g, b, a).endVertex();
         }

      }

      public static void drawBoundingBox(AxisAlignedBB bb, float width, float red, float green, float blue, float alpha) {
         GlStateManager.pushMatrix();
         GlStateManager.enableBlend();
         GlStateManager.disableDepth();
         GlStateManager.tryBlendFuncSeparate(770, 771, 0, 1);
         GlStateManager.disableTexture2D();
         GlStateManager.depthMask(false);
         GL11.glEnable(2848);
         GL11.glHint(3154, 4354);
         GL11.glLineWidth(width);
         Tessellator tessellator = Tessellator.getInstance();
         BufferBuilder bufferbuilder = tessellator.getBuffer();
         bufferbuilder.begin(3, DefaultVertexFormats.POSITION_COLOR);
         bufferbuilder.pos(bb.minX, bb.minY, bb.minZ).color(red, green, blue, alpha).endVertex();
         bufferbuilder.pos(bb.minX, bb.minY, bb.maxZ).color(red, green, blue, alpha).endVertex();
         bufferbuilder.pos(bb.maxX, bb.minY, bb.maxZ).color(red, green, blue, alpha).endVertex();
         bufferbuilder.pos(bb.maxX, bb.minY, bb.minZ).color(red, green, blue, alpha).endVertex();
         bufferbuilder.pos(bb.minX, bb.minY, bb.minZ).color(red, green, blue, alpha).endVertex();
         bufferbuilder.pos(bb.minX, bb.maxY, bb.minZ).color(red, green, blue, alpha).endVertex();
         bufferbuilder.pos(bb.minX, bb.maxY, bb.maxZ).color(red, green, blue, alpha).endVertex();
         bufferbuilder.pos(bb.minX, bb.minY, bb.maxZ).color(red, green, blue, alpha).endVertex();
         bufferbuilder.pos(bb.maxX, bb.minY, bb.maxZ).color(red, green, blue, alpha).endVertex();
         bufferbuilder.pos(bb.maxX, bb.maxY, bb.maxZ).color(red, green, blue, alpha).endVertex();
         bufferbuilder.pos(bb.minX, bb.maxY, bb.maxZ).color(red, green, blue, alpha).endVertex();
         bufferbuilder.pos(bb.maxX, bb.maxY, bb.maxZ).color(red, green, blue, alpha).endVertex();
         bufferbuilder.pos(bb.maxX, bb.maxY, bb.minZ).color(red, green, blue, alpha).endVertex();
         bufferbuilder.pos(bb.maxX, bb.minY, bb.minZ).color(red, green, blue, alpha).endVertex();
         bufferbuilder.pos(bb.maxX, bb.maxY, bb.minZ).color(red, green, blue, alpha).endVertex();
         bufferbuilder.pos(bb.minX, bb.maxY, bb.minZ).color(red, green, blue, alpha).endVertex();
         tessellator.draw();
         GL11.glDisable(2848);
         GlStateManager.depthMask(true);
         GlStateManager.enableDepth();
         GlStateManager.enableTexture2D();
         GlStateManager.disableBlend();
         GlStateManager.popMatrix();
      }

      public static void drawFullBox(AxisAlignedBB bb, BlockPos blockPos, float width, int argb, int alpha2) {
         int a = argb >>> 24 & 255;
         int r = argb >>> 16 & 255;
         int g = argb >>> 8 & 255;
         int b = argb & 255;
         drawFullBox(bb, blockPos, width, r, g, b, a, alpha2);
      }

      public static void drawFullBox(AxisAlignedBB bb, BlockPos blockPos, float width, int red, int green, int blue, int alpha, int alpha2) {
         prepare(7);
         drawBox(blockPos, red, green, blue, alpha, 63);
         release();
         drawBoundingBox(bb, width, (float)red, (float)green, (float)blue, (float)alpha2);
      }

      public static void drawHalfBox(BlockPos blockPos, int argb, int sides) {
         int a = argb >>> 24 & 255;
         int r = argb >>> 16 & 255;
         int g = argb >>> 8 & 255;
         int b = argb & 255;
         drawHalfBox(blockPos, r, g, b, a, sides);
      }

      public static void drawHalfBox(BlockPos blockPos, int r, int g, int b, int a, int sides) {
         drawBox(INSTANCE.getBuffer(), (float)blockPos.getX(), (float)blockPos.getY(), (float)blockPos.getZ(), 1.0F, 0.5F, 1.0F, r, g, b, a, sides);
      }
   }

   public static final class GeometryMasks {
      public static final HashMap FACEMAP = new HashMap();

      static {
         FACEMAP.put(EnumFacing.DOWN, 1);
         FACEMAP.put(EnumFacing.WEST, 16);
         FACEMAP.put(EnumFacing.NORTH, 4);
         FACEMAP.put(EnumFacing.SOUTH, 8);
         FACEMAP.put(EnumFacing.EAST, 32);
         FACEMAP.put(EnumFacing.UP, 2);
      }

      public static final class Line {
         public static final int DOWN_WEST = 17;
         public static final int UP_WEST = 18;
         public static final int DOWN_EAST = 33;
         public static final int UP_EAST = 34;
         public static final int DOWN_NORTH = 5;
         public static final int UP_NORTH = 6;
         public static final int DOWN_SOUTH = 9;
         public static final int UP_SOUTH = 10;
         public static final int NORTH_WEST = 20;
         public static final int NORTH_EAST = 36;
         public static final int SOUTH_WEST = 24;
         public static final int SOUTH_EAST = 40;
         public static final int ALL = 63;
      }

      public static final class Quad {
         public static final int DOWN = 1;
         public static final int UP = 2;
         public static final int NORTH = 4;
         public static final int SOUTH = 8;
         public static final int WEST = 16;
         public static final int EAST = 32;
         public static final int ALL = 63;
      }
   }
}
