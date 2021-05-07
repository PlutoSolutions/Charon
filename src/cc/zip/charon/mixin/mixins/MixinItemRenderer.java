package cc.zip.charon.mixin.mixins;

import cc.zip.charon.Charon;
import cc.zip.charon.event.events.TransformSideFirstPersonEvent;
import cc.zip.charon.features.modules.render.CustomView;
import cc.zip.charon.features.modules.render.SmallShield;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumHand;
import net.minecraft.util.EnumHandSide;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({ItemRenderer.class})
public abstract class MixinItemRenderer {
   private boolean injection = true;

   @Shadow
   public abstract void renderItemInFirstPerson(AbstractClientPlayer var1, float var2, float var3, EnumHand var4, float var5, ItemStack var6, float var7);

   @Inject(
      method = {"renderItemInFirstPerson(Lnet/minecraft/client/entity/AbstractClientPlayer;FFLnet/minecraft/util/EnumHand;FLnet/minecraft/item/ItemStack;F)V"},
      at = {@At("HEAD")},
      cancellable = true
   )
   public void renderItemInFirstPersonHook(AbstractClientPlayer player, float p_187457_2_, float p_187457_3_, EnumHand hand, float p_187457_5_, ItemStack stack, float p_187457_7_, CallbackInfo info) {
      if (this.injection) {
         info.cancel();
         SmallShield offset = SmallShield.getINSTANCE();
         float xOffset = 0.0F;
         float yOffset = 0.0F;
         this.injection = false;
         if (hand == EnumHand.MAIN_HAND) {
            if (offset.isOn() && player.getHeldItemMainhand() != ItemStack.EMPTY) {
               xOffset = (Float)offset.mainX.getValue();
               yOffset = (Float)offset.mainY.getValue();
            }
         } else if (!(Boolean)offset.normalOffset.getValue() && offset.isOn() && player.getHeldItemOffhand() != ItemStack.EMPTY) {
            xOffset = (Float)offset.offX.getValue();
            yOffset = (Float)offset.offY.getValue();
         }

         this.renderItemInFirstPerson(player, p_187457_2_, p_187457_3_, hand, p_187457_5_ + xOffset, stack, p_187457_7_ + yOffset);
         this.injection = true;
      }

   }

   @Redirect(
      method = {"renderArmFirstPerson"},
      at = @At(
   value = "INVOKE",
   target = "Lnet/minecraft/client/renderer/GlStateManager;translate(FFF)V",
   ordinal = 0
)
   )
   public void translateHook(float x, float y, float z) {
      SmallShield offset = SmallShield.getINSTANCE();
      boolean shiftPos = Minecraft.getMinecraft().player != null && Minecraft.getMinecraft().player.getHeldItemMainhand() != ItemStack.EMPTY && offset.isOn();
      GlStateManager.translate(x + (shiftPos ? (Float)offset.mainX.getValue() : 0.0F), y + (shiftPos ? (Float)offset.mainY.getValue() : 0.0F), z);
   }

   @Inject(
      method = {"transformSideFirstPerson"},
      at = {@At("HEAD")}
   )
   public void transformSideFirstPerson(EnumHandSide hand, float p_187459_2_, CallbackInfo callbackInfo) {
      new TransformSideFirstPersonEvent(hand);
   }

   @Inject(
      method = {"transformEatFirstPerson"},
      at = {@At("HEAD")},
      cancellable = true
   )
   public void transformEatFirstPerson(float p_187454_1_, EnumHandSide hand, ItemStack stack, CallbackInfo callbackInfo) {
      new TransformSideFirstPersonEvent(hand);
      CustomView customView = (CustomView)Charon.moduleManager.getModuleByClass(CustomView.class);
      if (customView.isEnabled() && (Boolean)customView.cancelEating.getValue()) {
         callbackInfo.cancel();
      }

   }

   @Inject(
      method = {"transformFirstPerson"},
      at = {@At("HEAD")}
   )
   public void transformFirstPerson(EnumHandSide hand, float p_187453_2_, CallbackInfo callbackInfo) {
      new TransformSideFirstPersonEvent(hand);
   }
}
