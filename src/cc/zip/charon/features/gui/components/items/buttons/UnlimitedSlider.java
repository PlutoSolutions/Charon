package cc.zip.charon.features.gui.components.items.buttons;

import cc.zip.charon.Charon;
import cc.zip.charon.features.gui.Gui;
import cc.zip.charon.features.modules.client.ClickGui;
import cc.zip.charon.features.setting.Setting;
import cc.zip.charon.util.RenderUtil;
import com.mojang.realmsclient.gui.ChatFormatting;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.init.SoundEvents;

public class UnlimitedSlider extends Button {
   public Setting setting;

   public UnlimitedSlider(Setting setting) {
      super(setting.getName());
      this.setting = setting;
      this.width = 15;
   }

   public void drawScreen(int mouseX, int mouseY, float partialTicks) {
      RenderUtil.drawRect(this.x, this.y, this.x + (float)this.width + 7.4F, this.y + (float)this.height - 0.5F, !this.isHovering(mouseX, mouseY) ? Charon.colorManager.getColorWithAlpha((Integer)((ClickGui)Charon.moduleManager.getModuleByClass(ClickGui.class)).hoverAlpha.getValue()) : Charon.colorManager.getColorWithAlpha((Integer)((ClickGui)Charon.moduleManager.getModuleByClass(ClickGui.class)).alpha.getValue()));
      Charon.textManager.drawStringWithShadow(" - " + this.setting.getName() + " " + ChatFormatting.GRAY + this.setting.getValue() + ChatFormatting.WHITE + " +", this.x + 2.3F, this.y - 1.7F - (float)Gui.getClickGui().getTextOffset(), this.getState() ? -1 : -5592406);
   }

   public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
      super.mouseClicked(mouseX, mouseY, mouseButton);
      if (this.isHovering(mouseX, mouseY)) {
         mc.getSoundHandler().playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 1.0F));
         if (this.isRight(mouseX)) {
            if (this.setting.getValue() instanceof Double) {
               this.setting.setValue((Double)this.setting.getValue() + 1.0D);
            } else if (this.setting.getValue() instanceof Float) {
               this.setting.setValue((Float)this.setting.getValue() + 1.0F);
            } else if (this.setting.getValue() instanceof Integer) {
               this.setting.setValue((Integer)this.setting.getValue() + 1);
            }
         } else if (this.setting.getValue() instanceof Double) {
            this.setting.setValue((Double)this.setting.getValue() - 1.0D);
         } else if (this.setting.getValue() instanceof Float) {
            this.setting.setValue((Float)this.setting.getValue() - 1.0F);
         } else if (this.setting.getValue() instanceof Integer) {
            this.setting.setValue((Integer)this.setting.getValue() - 1);
         }
      }

   }

   public void update() {
      this.setHidden(!this.setting.isVisible());
   }

   public int getHeight() {
      return 14;
   }

   public void toggle() {
   }

   public boolean getState() {
      return true;
   }

   public boolean isRight(int x) {
      return (float)x > this.x + ((float)this.width + 7.4F) / 2.0F;
   }
}
