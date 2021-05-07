package cc.zip.charon.features.modules.render;

import cc.zip.charon.event.events.Render3DEvent;
import cc.zip.charon.features.modules.Module;
import com.google.common.base.Predicate;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBow;
import net.minecraft.item.ItemEgg;
import net.minecraft.item.ItemEnderPearl;
import net.minecraft.item.ItemExpBottle;
import net.minecraft.item.ItemFishingRod;
import net.minecraft.item.ItemSnowball;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.RayTraceResult.Type;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.Cylinder;

public class Trajectories extends Module {
   public Trajectories() {
      super("Trajectories", "Draws trajectories.", Module.Category.RENDER, false, false, false);
   }

   public void onRender3D(Render3DEvent event) {
      if (mc.world != null && mc.player != null && mc.getRenderManager() != null) {
         double renderPosX = mc.player.lastTickPosX + (mc.player.posX - mc.player.lastTickPosX) * (double)event.getPartialTicks();
         double renderPosY = mc.player.lastTickPosY + (mc.player.posY - mc.player.lastTickPosY) * (double)event.getPartialTicks();
         double renderPosZ = mc.player.lastTickPosZ + (mc.player.posZ - mc.player.lastTickPosZ) * (double)event.getPartialTicks();
         mc.player.getHeldItem(EnumHand.MAIN_HAND);
         if (mc.gameSettings.thirdPersonView == 0 && (mc.player.getHeldItem(EnumHand.MAIN_HAND).getItem() instanceof ItemBow || mc.player.getHeldItem(EnumHand.MAIN_HAND).getItem() instanceof ItemFishingRod || mc.player.getHeldItem(EnumHand.MAIN_HAND).getItem() instanceof ItemEnderPearl || mc.player.getHeldItem(EnumHand.MAIN_HAND).getItem() instanceof ItemEgg || mc.player.getHeldItem(EnumHand.MAIN_HAND).getItem() instanceof ItemSnowball || mc.player.getHeldItem(EnumHand.MAIN_HAND).getItem() instanceof ItemExpBottle)) {
            GL11.glPushMatrix();
            Item item = mc.player.getHeldItem(EnumHand.MAIN_HAND).getItem();
            double posX = renderPosX - (double)(MathHelper.cos(mc.player.rotationYaw / 180.0F * 3.1415927F) * 0.16F);
            double posY = renderPosY + (double)mc.player.getEyeHeight() - 0.1000000014901161D;
            double posZ = renderPosZ - (double)(MathHelper.sin(mc.player.rotationYaw / 180.0F * 3.1415927F) * 0.16F);
            double motionX = (double)(-MathHelper.sin(mc.player.rotationYaw / 180.0F * 3.1415927F) * MathHelper.cos(mc.player.rotationPitch / 180.0F * 3.1415927F)) * (item instanceof ItemBow ? 1.0D : 0.4D);
            double motionY = (double)(-MathHelper.sin(mc.player.rotationPitch / 180.0F * 3.1415927F)) * (item instanceof ItemBow ? 1.0D : 0.4D);
            double motionZ = (double)(MathHelper.cos(mc.player.rotationYaw / 180.0F * 3.1415927F) * MathHelper.cos(mc.player.rotationPitch / 180.0F * 3.1415927F)) * (item instanceof ItemBow ? 1.0D : 0.4D);
            int var6 = 72000 - mc.player.getItemInUseCount();
            float power = (float)var6 / 20.0F;
            power = (power * power + power * 2.0F) / 3.0F;
            if (power > 1.0F) {
               power = 1.0F;
            }

            float distance = MathHelper.sqrt(motionX * motionX + motionY * motionY + motionZ * motionZ);
            motionX /= (double)distance;
            motionY /= (double)distance;
            motionZ /= (double)distance;
            float pow = item instanceof ItemBow ? power * 2.0F : (item instanceof ItemFishingRod ? 1.25F : (mc.player.getHeldItem(EnumHand.MAIN_HAND).getItem() == Items.EXPERIENCE_BOTTLE ? 0.9F : 1.0F));
            motionX *= (double)(pow * (item instanceof ItemFishingRod ? 0.75F : (mc.player.getHeldItem(EnumHand.MAIN_HAND).getItem() == Items.EXPERIENCE_BOTTLE ? 0.75F : 1.5F)));
            motionY *= (double)(pow * (item instanceof ItemFishingRod ? 0.75F : (mc.player.getHeldItem(EnumHand.MAIN_HAND).getItem() == Items.EXPERIENCE_BOTTLE ? 0.75F : 1.5F)));
            motionZ *= (double)(pow * (item instanceof ItemFishingRod ? 0.75F : (mc.player.getHeldItem(EnumHand.MAIN_HAND).getItem() == Items.EXPERIENCE_BOTTLE ? 0.75F : 1.5F)));
            this.enableGL3D(2.0F);
            GlStateManager.color(0.0F, 1.0F, 0.0F, 1.0F);
            GL11.glEnable(2848);
            float size = (float)(item instanceof ItemBow ? 0.3D : 0.25D);
            boolean hasLanded = false;
            Entity landingOnEntity = null;
            RayTraceResult landingPosition = null;

            while(!hasLanded && posY > 0.0D) {
               Vec3d present = new Vec3d(posX, posY, posZ);
               Vec3d future = new Vec3d(posX + motionX, posY + motionY, posZ + motionZ);
               RayTraceResult possibleLandingStrip = mc.world.rayTraceBlocks(present, future, false, true, false);
               if (possibleLandingStrip != null && possibleLandingStrip.typeOfHit != Type.MISS) {
                  landingPosition = possibleLandingStrip;
                  hasLanded = true;
               }

               AxisAlignedBB arrowBox = new AxisAlignedBB(posX - (double)size, posY - (double)size, posZ - (double)size, posX + (double)size, posY + (double)size, posZ + (double)size);
               List entities = this.getEntitiesWithinAABB(arrowBox.offset(motionX, motionY, motionZ).expand(1.0D, 1.0D, 1.0D));
               Iterator var34 = entities.iterator();

               while(var34.hasNext()) {
                  Object entity = var34.next();
                  Entity boundingBox = (Entity)entity;
                  if (boundingBox.canBeCollidedWith() && boundingBox != mc.player) {
                     float var8 = 0.3F;
                     AxisAlignedBB var9 = boundingBox.getEntityBoundingBox().expand(0.30000001192092896D, 0.30000001192092896D, 0.30000001192092896D);
                     RayTraceResult possibleEntityLanding = var9.calculateIntercept(present, future);
                     if (possibleEntityLanding != null) {
                        hasLanded = true;
                        landingOnEntity = boundingBox;
                        landingPosition = possibleEntityLanding;
                     }
                  }
               }

               if (landingOnEntity != null) {
                  GlStateManager.color(1.0F, 0.0F, 0.0F, 1.0F);
               }

               posX += motionX;
               posY += motionY;
               posZ += motionZ;
               float motionAdjustment = 0.99F;
               motionX *= 0.9900000095367432D;
               motionY *= 0.9900000095367432D;
               motionZ *= 0.9900000095367432D;
               motionY -= item instanceof ItemBow ? 0.05D : 0.03D;
               this.drawLine3D(posX - renderPosX, posY - renderPosY, posZ - renderPosZ);
            }

            if (landingPosition != null && landingPosition.typeOfHit == Type.BLOCK) {
               GlStateManager.translate(posX - renderPosX, posY - renderPosY, posZ - renderPosZ);
               int side = landingPosition.sideHit.getIndex();
               if (side == 2) {
                  GlStateManager.rotate(90.0F, 1.0F, 0.0F, 0.0F);
               } else if (side == 3) {
                  GlStateManager.rotate(90.0F, 1.0F, 0.0F, 0.0F);
               } else if (side == 4) {
                  GlStateManager.rotate(90.0F, 0.0F, 0.0F, 1.0F);
               } else if (side == 5) {
                  GlStateManager.rotate(90.0F, 0.0F, 0.0F, 1.0F);
               }

               Cylinder c = new Cylinder();
               GlStateManager.rotate(-90.0F, 1.0F, 0.0F, 0.0F);
               c.setDrawStyle(100011);
               if (landingOnEntity != null) {
                  GlStateManager.color(0.0F, 0.0F, 0.0F, 1.0F);
                  GL11.glLineWidth(2.5F);
                  c.draw(0.6F, 0.3F, 0.0F, 4, 1);
                  GL11.glLineWidth(0.1F);
                  GlStateManager.color(1.0F, 0.0F, 0.0F, 1.0F);
               }

               c.draw(0.6F, 0.3F, 0.0F, 4, 1);
            }

            this.disableGL3D();
            GL11.glPopMatrix();
         }
      }

   }

   public void enableGL3D(float lineWidth) {
      GL11.glDisable(3008);
      GL11.glEnable(3042);
      GL11.glBlendFunc(770, 771);
      GL11.glDisable(3553);
      GL11.glDisable(2929);
      GL11.glDepthMask(false);
      GL11.glEnable(2884);
      mc.entityRenderer.disableLightmap();
      GL11.glEnable(2848);
      GL11.glHint(3154, 4354);
      GL11.glHint(3155, 4354);
      GL11.glLineWidth(lineWidth);
   }

   public void disableGL3D() {
      GL11.glEnable(3553);
      GL11.glEnable(2929);
      GL11.glDisable(3042);
      GL11.glEnable(3008);
      GL11.glDepthMask(true);
      GL11.glCullFace(1029);
      GL11.glDisable(2848);
      GL11.glHint(3154, 4352);
      GL11.glHint(3155, 4352);
   }

   public void drawLine3D(double var1, double var2, double var3) {
      GL11.glVertex3d(var1, var2, var3);
   }

   private List getEntitiesWithinAABB(AxisAlignedBB bb) {
      ArrayList list = new ArrayList();
      int chunkMinX = MathHelper.floor((bb.minX - 2.0D) / 16.0D);
      int chunkMaxX = MathHelper.floor((bb.maxX + 2.0D) / 16.0D);
      int chunkMinZ = MathHelper.floor((bb.minZ - 2.0D) / 16.0D);
      int chunkMaxZ = MathHelper.floor((bb.maxZ + 2.0D) / 16.0D);

      for(int x = chunkMinX; x <= chunkMaxX; ++x) {
         for(int z = chunkMinZ; z <= chunkMaxZ; ++z) {
            if (mc.world.getChunkProvider().getLoadedChunk(x, z) != null) {
               mc.world.getChunk(x, z).getEntitiesWithinAABBForEntity(mc.player, bb, list, (Predicate)null);
            }
         }
      }

      return list;
   }
}
