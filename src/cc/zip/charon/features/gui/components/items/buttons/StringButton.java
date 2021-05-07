package cc.zip.charon.features.gui.components.items.buttons;

import cc.zip.charon.Charon;
import cc.zip.charon.features.gui.Gui;
import cc.zip.charon.features.modules.client.ClickGui;
import cc.zip.charon.features.setting.Setting;
import cc.zip.charon.util.RenderUtil;
import com.mojang.realmsclient.gui.ChatFormatting;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.init.SoundEvents;
import net.minecraft.util.ChatAllowedCharacters;

public class StringButton extends Button {
   private final Setting setting;
   public boolean isListening;
   private StringButton.CurrentString currentString = new StringButton.CurrentString("");

   public StringButton(Setting setting) {
      super(setting.getName());
      this.setting = setting;
      this.width = 15;
   }

   public static String removeLastChar(String str) {
      String output = "";
      if (str != null && str.length() > 0) {
         output = str.substring(0, str.length() - 1);
      }

      return output;
   }

   public void drawScreen(int mouseX, int mouseY, float partialTicks) {
      RenderUtil.drawRect(this.x, this.y, this.x + (float)this.width + 7.4F, this.y + (float)this.height - 0.5F, this.getState() ? (!this.isHovering(mouseX, mouseY) ? Charon.colorManager.getColorWithAlpha((Integer)((ClickGui)Charon.moduleManager.getModuleByClass(ClickGui.class)).hoverAlpha.getValue()) : Charon.colorManager.getColorWithAlpha((Integer)((ClickGui)Charon.moduleManager.getModuleByClass(ClickGui.class)).alpha.getValue())) : (!this.isHovering(mouseX, mouseY) ? 290805077 : -2007673515));
      if (this.isListening) {
         Charon.textManager.drawStringWithShadow(this.currentString.getString() + Charon.textManager.getIdleSign(), this.x + 2.3F, this.y - 1.7F - (float)Gui.getClickGui().getTextOffset(), this.getState() ? -1 : -5592406);
      } else {
         Charon.textManager.drawStringWithShadow((this.setting.getName().equals("Buttons") ? "Buttons " : (this.setting.getName().equals("Prefix") ? "Prefix  " + ChatFormatting.GRAY : "")) + this.setting.getValue(), this.x + 2.3F, this.y - 1.7F - (float)Gui.getClickGui().getTextOffset(), this.getState() ? -1 : -5592406);
      }

   }

   public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
      super.mouseClicked(mouseX, mouseY, mouseButton);
      if (this.isHovering(mouseX, mouseY)) {
         mc.getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 1.0F));
      }

   }

   public void onKeyTyped(char typedChar, int keyCode) {
      if (this.isListening) {
         switch(keyCode) {
         case 1:
            return;
         case 28:
            this.enterString();
         case 14:
            this.setString(removeLastChar(this.currentString.getString()));
         default:
            if (ChatAllowedCharacters.isAllowedCharacter(typedChar)) {
               this.setString(this.currentString.getString() + typedChar);
            }
         }
      }

   }

   public void update() {
      this.setHidden(!this.setting.isVisible());
   }

   private void enterString() {
      if (this.currentString.getString().isEmpty()) {
         this.setting.setValue(this.setting.getDefaultValue());
      } else {
         this.setting.setValue(this.currentString.getString());
      }

      this.setString("");
      this.onMouseClick();
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

   public void setString(String newString) {
      this.currentString = new StringButton.CurrentString(newString);
   }

   public static class CurrentString {
      private final String string;

      public CurrentString(String string) {
         this.string = string;
      }

      public String getString() {
         return this.string;
      }
   }
}
