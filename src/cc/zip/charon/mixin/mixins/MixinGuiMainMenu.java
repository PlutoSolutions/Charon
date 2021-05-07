package cc.zip.charon.mixin.mixins;

import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.ResourceLocation;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({GuiMainMenu.class})
public class MixinGuiMainMenu extends GuiScreen {
   @Inject(
      method = {"drawScreen"},
      at = {@At("TAIL")},
      cancellable = true
   )
   public void drawText(int mouseX, int mouseY, float partialTicks, CallbackInfo callbackInfo) {
      ResourceLocation resourceLocation = new ResourceLocation("util", "charoneu.png");
      this.mc.getTextureManager().bindTexture(resourceLocation);
      drawModalRectWithCustomSizedTexture(2, 2, 0.0F, 0.0F, 160, 32, 160.0F, 32.0F);
   }
}
