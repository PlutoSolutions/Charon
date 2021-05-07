package cc.zip.charon.features.modules.render;

import cc.zip.charon.event.events.Render3DEvent;
import cc.zip.charon.features.modules.Module;
import cc.zip.charon.features.setting.Setting;
import cc.zip.charon.util.EntityUtil;
import cc.zip.charon.util.RenderUtil;
import java.awt.Color;
import java.util.Iterator;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityEnderPearl;
import net.minecraft.entity.item.EntityExpBottle;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.item.EntityXPOrb;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.opengl.GL11;

public class ESP extends Module {
   private static ESP INSTANCE = new ESP();
   private final Setting<Boolean> items = this.register(new Setting("Items", false));
   private final Setting<Boolean> xporbs = this.register(new Setting("XpOrbs", false));
   private final Setting<Boolean> xpbottles = this.register(new Setting("XpBottles", false));
   private final Setting<Boolean> pearl = this.register(new Setting("Pearls", false));
   private final Setting<Integer> red = this.register(new Setting("Red", 255, 0, 255));
   private final Setting<Integer> green = this.register(new Setting("Green", 255, 0, 255));
   private final Setting<Integer> blue = this.register(new Setting("Blue", 255, 0, 255));
   private final Setting<Integer> boxAlpha = this.register(new Setting("BoxAlpha", 120, 0, 255));
   private final Setting<Integer> alpha = this.register(new Setting("Alpha", 255, 0, 255));

   public ESP() {
      super("ESP", "Renders a nice ESP.", Module.Category.RENDER, false, false, false);
      this.setInstance();
   }

   public static ESP getInstance() {
      if (INSTANCE == null) {
         INSTANCE = new ESP();
      }

      return INSTANCE;
   }

   private void setInstance() {
      INSTANCE = this;
   }

   public void onRender3D(Render3DEvent event) {
      AxisAlignedBB bb;
      Vec3d interp;
      int i;
      Iterator var5;
      Entity entity;
      if ((Boolean)this.items.getValue()) {
         i = 0;
         var5 = mc.world.loadedEntityList.iterator();

         while(var5.hasNext()) {
            entity = (Entity)var5.next();
            if (entity instanceof EntityItem && mc.player.getDistanceSq(entity) < 2500.0D) {
               interp = EntityUtil.getInterpolatedRenderPos(entity, mc.getRenderPartialTicks());
               bb = new AxisAlignedBB(entity.getEntityBoundingBox().minX - 0.05D - entity.posX + interp.x, entity.getEntityBoundingBox().minY - 0.0D - entity.posY + interp.y, entity.getEntityBoundingBox().minZ - 0.05D - entity.posZ + interp.z, entity.getEntityBoundingBox().maxX + 0.05D - entity.posX + interp.x, entity.getEntityBoundingBox().maxY + 0.1D - entity.posY + interp.y, entity.getEntityBoundingBox().maxZ + 0.05D - entity.posZ + interp.z);
               GlStateManager.pushMatrix();
               GlStateManager.enableBlend();
               GlStateManager.disableDepth();
               GlStateManager.tryBlendFuncSeparate(770, 771, 0, 1);
               GlStateManager.disableTexture2D();
               GlStateManager.depthMask(false);
               GL11.glEnable(2848);
               GL11.glHint(3154, 4354);
               GL11.glLineWidth(1.0F);
               RenderGlobal.renderFilledBox(bb, (float)(Integer)this.red.getValue() / 255.0F, (float)(Integer)this.green.getValue() / 255.0F, (float)(Integer)this.blue.getValue() / 255.0F, (float)(Integer)this.boxAlpha.getValue() / 255.0F);
               GL11.glDisable(2848);
               GlStateManager.depthMask(true);
               GlStateManager.enableDepth();
               GlStateManager.enableTexture2D();
               GlStateManager.disableBlend();
               GlStateManager.popMatrix();
               RenderUtil.drawBlockOutline(bb, new Color((Integer)this.red.getValue(), (Integer)this.green.getValue(), (Integer)this.blue.getValue(), (Integer)this.alpha.getValue()), 1.0F);
               ++i;
               if (i >= 50) {
                  break;
               }
            }
         }
      }

      if ((Boolean)this.xporbs.getValue()) {
         i = 0;
         var5 = mc.world.loadedEntityList.iterator();

         while(var5.hasNext()) {
            entity = (Entity)var5.next();
            if (entity instanceof EntityXPOrb && mc.player.getDistanceSq(entity) < 2500.0D) {
               interp = EntityUtil.getInterpolatedRenderPos(entity, mc.getRenderPartialTicks());
               bb = new AxisAlignedBB(entity.getEntityBoundingBox().minX - 0.05D - entity.posX + interp.x, entity.getEntityBoundingBox().minY - 0.0D - entity.posY + interp.y, entity.getEntityBoundingBox().minZ - 0.05D - entity.posZ + interp.z, entity.getEntityBoundingBox().maxX + 0.05D - entity.posX + interp.x, entity.getEntityBoundingBox().maxY + 0.1D - entity.posY + interp.y, entity.getEntityBoundingBox().maxZ + 0.05D - entity.posZ + interp.z);
               GlStateManager.pushMatrix();
               GlStateManager.enableBlend();
               GlStateManager.disableDepth();
               GlStateManager.tryBlendFuncSeparate(770, 771, 0, 1);
               GlStateManager.disableTexture2D();
               GlStateManager.depthMask(false);
               GL11.glEnable(2848);
               GL11.glHint(3154, 4354);
               GL11.glLineWidth(1.0F);
               RenderGlobal.renderFilledBox(bb, (float)(Integer)this.red.getValue() / 255.0F, (float)(Integer)this.green.getValue() / 255.0F, (float)(Integer)this.blue.getValue() / 255.0F, (float)(Integer)this.boxAlpha.getValue() / 255.0F);
               GL11.glDisable(2848);
               GlStateManager.depthMask(true);
               GlStateManager.enableDepth();
               GlStateManager.enableTexture2D();
               GlStateManager.disableBlend();
               GlStateManager.popMatrix();
               RenderUtil.drawBlockOutline(bb, new Color((Integer)this.red.getValue(), (Integer)this.green.getValue(), (Integer)this.blue.getValue(), (Integer)this.alpha.getValue()), 1.0F);
               ++i;
               if (i >= 50) {
                  break;
               }
            }
         }
      }

      if ((Boolean)this.pearl.getValue()) {
         i = 0;
         var5 = mc.world.loadedEntityList.iterator();

         while(var5.hasNext()) {
            entity = (Entity)var5.next();
            if (entity instanceof EntityEnderPearl && mc.player.getDistanceSq(entity) < 2500.0D) {
               interp = EntityUtil.getInterpolatedRenderPos(entity, mc.getRenderPartialTicks());
               bb = new AxisAlignedBB(entity.getEntityBoundingBox().minX - 0.05D - entity.posX + interp.x, entity.getEntityBoundingBox().minY - 0.0D - entity.posY + interp.y, entity.getEntityBoundingBox().minZ - 0.05D - entity.posZ + interp.z, entity.getEntityBoundingBox().maxX + 0.05D - entity.posX + interp.x, entity.getEntityBoundingBox().maxY + 0.1D - entity.posY + interp.y, entity.getEntityBoundingBox().maxZ + 0.05D - entity.posZ + interp.z);
               GlStateManager.pushMatrix();
               GlStateManager.enableBlend();
               GlStateManager.disableDepth();
               GlStateManager.tryBlendFuncSeparate(770, 771, 0, 1);
               GlStateManager.disableTexture2D();
               GlStateManager.depthMask(false);
               GL11.glEnable(2848);
               GL11.glHint(3154, 4354);
               GL11.glLineWidth(1.0F);
               RenderGlobal.renderFilledBox(bb, (float)(Integer)this.red.getValue() / 255.0F, (float)(Integer)this.green.getValue() / 255.0F, (float)(Integer)this.blue.getValue() / 255.0F, (float)(Integer)this.boxAlpha.getValue() / 255.0F);
               GL11.glDisable(2848);
               GlStateManager.depthMask(true);
               GlStateManager.enableDepth();
               GlStateManager.enableTexture2D();
               GlStateManager.disableBlend();
               GlStateManager.popMatrix();
               RenderUtil.drawBlockOutline(bb, new Color((Integer)this.red.getValue(), (Integer)this.green.getValue(), (Integer)this.blue.getValue(), (Integer)this.alpha.getValue()), 1.0F);
               ++i;
               if (i >= 50) {
                  break;
               }
            }
         }
      }

      if ((Boolean)this.xpbottles.getValue()) {
         i = 0;
         var5 = mc.world.loadedEntityList.iterator();

         while(var5.hasNext()) {
            entity = (Entity)var5.next();
            if (entity instanceof EntityExpBottle && mc.player.getDistanceSq(entity) < 2500.0D) {
               interp = EntityUtil.getInterpolatedRenderPos(entity, mc.getRenderPartialTicks());
               bb = new AxisAlignedBB(entity.getEntityBoundingBox().minX - 0.05D - entity.posX + interp.x, entity.getEntityBoundingBox().minY - 0.0D - entity.posY + interp.y, entity.getEntityBoundingBox().minZ - 0.05D - entity.posZ + interp.z, entity.getEntityBoundingBox().maxX + 0.05D - entity.posX + interp.x, entity.getEntityBoundingBox().maxY + 0.1D - entity.posY + interp.y, entity.getEntityBoundingBox().maxZ + 0.05D - entity.posZ + interp.z);
               GlStateManager.pushMatrix();
               GlStateManager.enableBlend();
               GlStateManager.disableDepth();
               GlStateManager.tryBlendFuncSeparate(770, 771, 0, 1);
               GlStateManager.disableTexture2D();
               GlStateManager.depthMask(false);
               GL11.glEnable(2848);
               GL11.glHint(3154, 4354);
               GL11.glLineWidth(1.0F);
               RenderGlobal.renderFilledBox(bb, (float)(Integer)this.red.getValue() / 255.0F, (float)(Integer)this.green.getValue() / 255.0F, (float)(Integer)this.blue.getValue() / 255.0F, (float)(Integer)this.boxAlpha.getValue() / 255.0F);
               GL11.glDisable(2848);
               GlStateManager.depthMask(true);
               GlStateManager.enableDepth();
               GlStateManager.enableTexture2D();
               GlStateManager.disableBlend();
               GlStateManager.popMatrix();
               RenderUtil.drawBlockOutline(bb, new Color((Integer)this.red.getValue(), (Integer)this.green.getValue(), (Integer)this.blue.getValue(), (Integer)this.alpha.getValue()), 1.0F);
               ++i;
               if (i >= 50) {
                  break;
               }
            }
         }
      }

   }
}
