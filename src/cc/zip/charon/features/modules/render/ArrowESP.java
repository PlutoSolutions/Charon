package cc.zip.charon.features.modules.render;

import cc.zip.charon.event.events.Render2DEvent;
import cc.zip.charon.features.modules.Module;
import cc.zip.charon.features.setting.Setting;
import cc.zip.charon.util.EntityUtil;
import cc.zip.charon.util.RenderUtil;
import cc.zip.charon.util.Util;
import com.google.common.collect.Maps;
import java.awt.Color;
import java.util.Iterator;
import java.util.Map;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;

public class ArrowESP extends Module {
   private final Setting<Integer> red = this.register(new Setting("Red", 255, 0, 255));
   private final Setting<Integer> green = this.register(new Setting("Green", 0, 0, 255));
   private final Setting<Integer> blue = this.register(new Setting("Blue", 255, 0, 255));
   private final Setting<Integer> radius = this.register(new Setting("Placement", 45, 10, 200));
   private final Setting<Float> size = this.register(new Setting("Size", 10.0F, 5.0F, 25.0F));
   private final Setting<Boolean> outline = this.register(new Setting("Outline", true));
   private final Setting<Float> outlineWidth = this.register(new Setting("Outline-Width", 1.0F, 0.1F, 3.0F));
   private final Setting<Integer> fadeDistance = this.register(new Setting("Range", 100, 10, 200));
   private final Setting<Boolean> invisibles = this.register(new Setting("Invisibles", false));
   private final ArrowESP.EntityListener entityListener = new ArrowESP.EntityListener();

   public ArrowESP() {
      super("ArrowESP", "Arrow tracers ", Module.Category.RENDER, true, false, false);
   }

   public void onRender2D(Render2DEvent event) {
      this.entityListener.render();
      mc.world.loadedEntityList.forEach((o) -> {
         if (o instanceof EntityPlayer && this.isValid((EntityPlayer)o)) {
            EntityPlayer entity = (EntityPlayer)o;
            Vec3d pos = (Vec3d)this.entityListener.getEntityLowerBounds().get(entity);
            if (pos != null && !this.isOnScreen(pos) && !RenderUtil.isInViewFrustrum((Entity)entity)) {
               Color color = EntityUtil.getColor(entity, (Integer)this.red.getValue(), (Integer)this.green.getValue(), (Integer)this.blue.getValue(), (int)MathHelper.clamp(255.0F - 255.0F / (float)(Integer)this.fadeDistance.getValue() * mc.player.getDistance(entity), 100.0F, 255.0F), true);
               int x = Display.getWidth() / 2 / (mc.gameSettings.guiScale == 0 ? 1 : mc.gameSettings.guiScale);
               int y = Display.getHeight() / 2 / (mc.gameSettings.guiScale == 0 ? 1 : mc.gameSettings.guiScale);
               float yaw = this.getRotations(entity) - mc.player.rotationYaw;
               GL11.glTranslatef((float)x, (float)y, 0.0F);
               GL11.glRotatef(yaw, 0.0F, 0.0F, 1.0F);
               GL11.glTranslatef((float)(-x), (float)(-y), 0.0F);
               RenderUtil.drawTracerPointer((float)x, (float)(y - (Integer)this.radius.getValue()), (Float)this.size.getValue(), 2.0F, 1.0F, (Boolean)this.outline.getValue(), (Float)this.outlineWidth.getValue(), color.getRGB());
               GL11.glTranslatef((float)x, (float)y, 0.0F);
               GL11.glRotatef(-yaw, 0.0F, 0.0F, 1.0F);
               GL11.glTranslatef((float)(-x), (float)(-y), 0.0F);
            }
         }

      });
   }

   private boolean isOnScreen(Vec3d pos) {
      if (!(pos.x > -1.0D)) {
         return false;
      } else if (!(pos.y < 1.0D)) {
         return false;
      } else if (!(pos.x > -1.0D)) {
         return false;
      } else if (!(pos.z < 1.0D)) {
         return false;
      } else {
         int n = mc.gameSettings.guiScale == 0 ? 1 : mc.gameSettings.guiScale;
         if (!(pos.x / (double)n >= 0.0D)) {
            return false;
         } else {
            int n2 = mc.gameSettings.guiScale == 0 ? 1 : mc.gameSettings.guiScale;
            if (!(pos.x / (double)n2 <= (double)Display.getWidth())) {
               return false;
            } else {
               int n3 = mc.gameSettings.guiScale == 0 ? 1 : mc.gameSettings.guiScale;
               if (!(pos.y / (double)n3 >= 0.0D)) {
                  return false;
               } else {
                  int n4 = mc.gameSettings.guiScale == 0 ? 1 : mc.gameSettings.guiScale;
                  return pos.y / (double)n4 <= (double)Display.getHeight();
               }
            }
         }
      }
   }

   private boolean isValid(EntityPlayer entity) {
      return entity != mc.player && (!entity.isInvisible() || (Boolean)this.invisibles.getValue()) && entity.isEntityAlive();
   }

   private float getRotations(EntityLivingBase ent) {
      double x = ent.posX - mc.player.posX;
      double z = ent.posZ - mc.player.posZ;
      return (float)(-(Math.atan2(x, z) * 57.29577951308232D));
   }

   private static class EntityListener {
      private final Map<Entity, Vec3d> entityUpperBounds;
      private final Map<Entity, Vec3d> entityLowerBounds;

      private EntityListener() {
         this.entityUpperBounds = Maps.newHashMap();
         this.entityLowerBounds = Maps.newHashMap();
      }

      private void render() {
         if (!this.entityUpperBounds.isEmpty()) {
            this.entityUpperBounds.clear();
         }

         if (!this.entityLowerBounds.isEmpty()) {
            this.entityLowerBounds.clear();
         }

         Iterator var1 = Util.mc.world.loadedEntityList.iterator();

         while(var1.hasNext()) {
            Entity e = (Entity)var1.next();
            Vec3d bound = this.getEntityRenderPosition(e);
            bound.add(new Vec3d(0.0D, (double)e.height + 0.2D, 0.0D));
            Vec3d upperBounds = RenderUtil.to2D(bound.x, bound.y, bound.z);
            Vec3d lowerBounds = RenderUtil.to2D(bound.x, bound.y - 2.0D, bound.z);
            if (upperBounds != null && lowerBounds != null) {
               this.entityUpperBounds.put(e, upperBounds);
               this.entityLowerBounds.put(e, lowerBounds);
            }
         }

      }

      private Vec3d getEntityRenderPosition(Entity entity) {
         double partial = (double)Util.mc.timer.renderPartialTicks;
         double x = entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * partial - Util.mc.getRenderManager().viewerPosX;
         double y = entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * partial - Util.mc.getRenderManager().viewerPosY;
         double z = entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * partial - Util.mc.getRenderManager().viewerPosZ;
         return new Vec3d(x, y, z);
      }

      public Map<Entity, Vec3d> getEntityLowerBounds() {
         return this.entityLowerBounds;
      }

      // $FF: synthetic method
      EntityListener(Object x0) {
         this();
      }
   }
}
