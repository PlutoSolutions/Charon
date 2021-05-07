package cc.zip.charon.features.modules.client;

import cc.zip.charon.Charon;
import cc.zip.charon.event.events.ClientEvent;
import cc.zip.charon.features.command.Command;
import cc.zip.charon.features.gui.Gui;
import cc.zip.charon.features.modules.Module;
import cc.zip.charon.features.setting.Setting;
import com.mojang.realmsclient.gui.ChatFormatting;
import net.minecraft.client.settings.GameSettings.Options;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class ClickGui extends Module {
   private static ClickGui INSTANCE = new ClickGui();
   public Setting<String> prefix = this.register(new Setting("Prefix", "-"));
   public Setting<Boolean> customFov = this.register(new Setting("CustomFov", false));
   public Setting<Boolean> blurs = this.register(new Setting("Blur", true));
   public Setting<Boolean> customcolorb = this.register(new Setting("Custom Color", true));
   public Setting<Float> fov = this.register(new Setting("Fov", 150.0F, -180.0F, 180.0F));
   public Setting<Integer> red = this.register(new Setting("Red", 126, 0, 255));
   public Setting<Integer> green = this.register(new Setting("Green", 198, 0, 255));
   public Setting<Integer> blue = this.register(new Setting("Blue", 198, 0, 255));
   public Setting<Integer> hoverAlpha = this.register(new Setting("Alpha", 198, 0, 255));
   public Setting<Integer> redopen = this.register(new Setting("Red - Open", 126, 0, 255, (v) -> {
      return (Boolean)this.customcolorb.getValue();
   }));
   public Setting<Integer> greenopen = this.register(new Setting("Green - Open", 198, 0, 255, (v) -> {
      return (Boolean)this.customcolorb.getValue();
   }));
   public Setting<Integer> blueopen = this.register(new Setting("Blue - Open", 198, 0, 255, (v) -> {
      return (Boolean)this.customcolorb.getValue();
   }));
   public Setting<Integer> alphaopen = this.register(new Setting("Alpha - Open", 198, 0, 255, (v) -> {
      return (Boolean)this.customcolorb.getValue();
   }));
   public Setting<Integer> topRed = this.register(new Setting("SecondRed", 95, 0, 255));
   public Setting<Integer> topGreen = this.register(new Setting("SecondGreen", 95, 0, 255));
   public Setting<Integer> topBlue = this.register(new Setting("SecondBlue", 95, 0, 255));
   public Setting<Integer> alpha = this.register(new Setting("HoverAlpha", 240, 0, 255));
   public Setting<Boolean> rainbow = this.register(new Setting("Rainbow", false));
   public Setting<ClickGui.rainbowMode> rainbowModeHud;
   public Setting<ClickGui.rainbowModeArray> rainbowModeA;
   public Setting<Integer> rainbowHue;
   public Setting<Float> rainbowBrightness;
   public Setting<Float> rainbowSaturation;
   private Gui click;

   public ClickGui() {
      super("ClickGui", "Opens the ClickGui", Module.Category.CLIENT, true, false, false);
      this.rainbowModeHud = this.register(new Setting("HRainbowMode", ClickGui.rainbowMode.Static, (v) -> {
         return (Boolean)this.rainbow.getValue();
      }));
      this.rainbowModeA = this.register(new Setting("ARainbowMode", ClickGui.rainbowModeArray.Static, (v) -> {
         return (Boolean)this.rainbow.getValue();
      }));
      this.rainbowHue = this.register(new Setting("Delay", 240, 0, 600, (v) -> {
         return (Boolean)this.rainbow.getValue();
      }));
      this.rainbowBrightness = this.register(new Setting("Brightness ", 150.0F, 1.0F, 255.0F, (v) -> {
         return (Boolean)this.rainbow.getValue();
      }));
      this.rainbowSaturation = this.register(new Setting("Saturation", 150.0F, 1.0F, 255.0F, (v) -> {
         return (Boolean)this.rainbow.getValue();
      }));
      this.setInstance();
   }

   public static ClickGui getInstance() {
      if (INSTANCE == null) {
         INSTANCE = new ClickGui();
      }

      return INSTANCE;
   }

   private void setInstance() {
      INSTANCE = this;
   }

   public void onUpdate() {
      if ((Boolean)this.customFov.getValue()) {
         mc.gameSettings.setOptionFloatValue(Options.FOV, (Float)this.fov.getValue());
      }

      if (!(Boolean)this.blurs.getValue()) {
         mc.entityRenderer.getShaderGroup().deleteShaderGroup();
      }

   }

   @SubscribeEvent
   public void onSettingChange(ClientEvent event) {
      if (event.getStage() == 2 && event.getSetting().getFeature().equals(this)) {
         if (event.getSetting().equals(this.prefix)) {
            Charon.commandManager.setPrefix((String)this.prefix.getPlannedValue());
            Command.sendMessage("Prefix set to " + ChatFormatting.DARK_GRAY + Charon.commandManager.getPrefix());
         }

         Charon.colorManager.setColor((Integer)this.red.getPlannedValue(), (Integer)this.green.getPlannedValue(), (Integer)this.blue.getPlannedValue(), (Integer)this.hoverAlpha.getPlannedValue());
      }

   }

   public void onEnable() {
      if (!nullCheck()) {
         mc.displayGuiScreen(Gui.getClickGui());
         if ((Boolean)this.blurs.getValue()) {
            mc.entityRenderer.loadShader(new ResourceLocation("shaders/post/blur.json"));
         }

      }
   }

   public void onLoad() {
      Charon.colorManager.setColor((Integer)this.red.getValue(), (Integer)this.green.getValue(), (Integer)this.blue.getValue(), (Integer)this.hoverAlpha.getValue());
      Charon.commandManager.setPrefix((String)this.prefix.getValue());
   }

   public void onTick() {
      if (!(mc.currentScreen instanceof Gui)) {
         this.disable();
      }

   }

   public static enum rainbowMode {
      Static,
      Sideway;
   }

   public static enum rainbowModeArray {
      Static,
      Up;
   }
}
