package cc.zip.charon.features.gui.components.items.buttons;

import cc.zip.charon.Charon;
import cc.zip.charon.features.gui.Gui;
import cc.zip.charon.features.modules.client.ClickGui;
import cc.zip.charon.features.setting.Bind;
import cc.zip.charon.features.setting.Setting;
import cc.zip.charon.util.ColorUtil;
import cc.zip.charon.util.RenderUtil;
import com.mojang.realmsclient.gui.ChatFormatting;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.init.SoundEvents;

public class BindButton extends Button {
   private final Setting setting;
   public boolean isListening;

   public BindButton(Setting setting) {
      super(setting.getName());
      this.setting = setting;
      this.width = 15;
   }

   public void drawScreen(int mouseX, int mouseY, float partialTicks) {
      int color = ColorUtil.toARGB((Integer)ClickGui.getInstance().red.getValue(), (Integer)ClickGui.getInstance().green.getValue(), (Integer)ClickGui.getInstance().blue.getValue(), 255);
      RenderUtil.drawRect(this.x, this.y, this.x + (float)this.width + 7.4F, this.y + (float)this.height - 0.5F, this.getState() ? (!this.isHovering(mouseX, mouseY) ? 290805077 : -2007673515) : (!this.isHovering(mouseX, mouseY) ? Charon.colorManager.getColorWithAlpha((Integer)((ClickGui)Charon.moduleManager.getModuleByClass(ClickGui.class)).hoverAlpha.getValue()) : Charon.colorManager.getColorWithAlpha((Integer)((ClickGui)Charon.moduleManager.getModuleByClass(ClickGui.class)).alpha.getValue())));
      if (this.isListening) {
         Charon.textManager.drawStringWithShadow("Press a Key...", this.x + 2.3F, this.y - 1.7F - (float)Gui.getClickGui().getTextOffset(), -1);
      } else {
         Charon.textManager.drawStringWithShadow(this.setting.getName() + " " + ChatFormatting.GRAY + this.setting.getValue().toString().toUpperCase(), this.x + 2.3F, this.y - 1.7F - (float)Gui.getClickGui().getTextOffset(), this.getState() ? -1 : -5592406);
      }

   }

   public void update() {
      this.setHidden(!this.setting.isVisible());
   }

   public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
      super.mouseClicked(mouseX, mouseY, mouseButton);
      if (this.isHovering(mouseX, mouseY)) {
         mc.getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 1.0F));
      }

   }

   public void onKeyTyped(char typedChar, int keyCode) {
      if (this.isListening) {
         Bind bind = new Bind(keyCode);
         if (bind.toString().equalsIgnoreCase("Escape")) {
            return;
         }

         if (bind.toString().equalsIgnoreCase("Delete")) {
            bind = new Bind(-1);
         }

         this.setting.setValue(bind);
         this.onMouseClick();
      }

   }

   public int getHeight() {
      return 14;
   }

   public void toggle() {
      this.isListening = !this.isListening;
   }

   public boolean getState() {
      return !this.isListening;
   }
}
