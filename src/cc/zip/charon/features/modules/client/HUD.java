package cc.zip.charon.features.modules.client;

import cc.zip.charon.Charon;
import cc.zip.charon.event.events.ClientEvent;
import cc.zip.charon.event.events.Render2DEvent;
import cc.zip.charon.features.modules.Module;
import cc.zip.charon.features.setting.Setting;
import cc.zip.charon.util.ColorUtil;
import cc.zip.charon.util.EntityUtil;
import cc.zip.charon.util.MathUtil;
import cc.zip.charon.util.RenderUtil;
import cc.zip.charon.util.TextUtil;
import cc.zip.charon.util.Timer;
import com.mojang.realmsclient.gui.ChatFormatting;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import org.lwjgl.opengl.GL11;

public class HUD extends Module {
   private static final ResourceLocation box = new ResourceLocation("textures/gui/container/shulker_box.png");
   private static final ItemStack totem;
   private static HUD INSTANCE;
   private final Setting<Boolean> grayNess = this.register(new Setting("Gray", true));
   private final Setting<Boolean> renderingUp = this.register(new Setting("RenderingUp", false, "Orientation of the HUD-Elements."));
   private final Setting<Boolean> waterMark = this.register(new Setting("Watermark", false, "displays watermark"));
   public Setting<HUD.WatermarkSelect> watermakss;
   private final Setting<Boolean> arrayList;
   private final Setting<Boolean> coords;
   private final Setting<Boolean> direction;
   private final Setting<Boolean> armor;
   private final Setting<Boolean> totems;
   private final Setting<Boolean> greeter;
   private final Setting<Boolean> speed;
   private final Setting<Boolean> potions;
   private final Setting<Boolean> ping;
   private final Setting<Boolean> tps;
   private final Setting<Boolean> fps;
   private final Setting<Boolean> lag;
   private final Timer timer;
   private final Map<String, Integer> players;
   public Setting<String> command;
   public Setting<TextUtil.Color> bracketColor;
   public Setting<TextUtil.Color> commandColor;
   public Setting<String> commandBracket;
   public Setting<String> commandBracket2;
   public Setting<Boolean> notifyToggles;
   public Setting<HUD.RenderingMode> chatnotify;
   public Setting<Integer> animationHorizontalTime;
   public Setting<Integer> animationVerticalTime;
   public Setting<HUD.RenderingMode> renderingMode;
   public Setting<Boolean> time;
   public Setting<Integer> lagTime;
   private int color;
   private boolean shouldIncrement;
   private int hitMarkerTimer;

   public HUD() {
      super("HUDEditor", "HUD Elements rendered on your screen", Module.Category.CLIENT, true, false, false);
      this.watermakss = this.register(new Setting("Mode", HUD.WatermarkSelect.Big, (v) -> {
         return (Boolean)this.waterMark.getValue();
      }));
      this.arrayList = this.register(new Setting("ActiveModules", false, "Lists the active modules."));
      this.coords = this.register(new Setting("Coords", false, "Your current coordinates"));
      this.direction = this.register(new Setting("Direction", false, "The Direction you are facing."));
      this.armor = this.register(new Setting("Armor", false, "ArmorHUD"));
      this.totems = this.register(new Setting("Totems", false, "TotemHUD"));
      this.greeter = this.register(new Setting("Welcomer", false, "The time"));
      this.speed = this.register(new Setting("Speed", false, "Your Speed"));
      this.potions = this.register(new Setting("Potions", false, "Your Speed"));
      this.ping = this.register(new Setting("Ping", false, "Your response time to the server."));
      this.tps = this.register(new Setting("TPS", false, "Ticks per second of the server."));
      this.fps = this.register(new Setting("FPS", false, "Your frames per second."));
      this.lag = this.register(new Setting("LagNotifier", false, "The time"));
      this.timer = new Timer();
      this.players = new HashMap();
      this.command = this.register(new Setting("Command", "charon.eu"));
      this.bracketColor = this.register(new Setting("BracketColor", TextUtil.Color.GRAY));
      this.commandColor = this.register(new Setting("NameColor", TextUtil.Color.WHITE));
      this.commandBracket = this.register(new Setting("Bracket", "["));
      this.commandBracket2 = this.register(new Setting("Bracket2", "]"));
      this.notifyToggles = this.register(new Setting("ChatNotify", false, "notifys in chat"));
      this.chatnotify = this.register(new Setting("Ordering", HUD.ChatS.SILENT));
      this.animationHorizontalTime = this.register(new Setting("AnimationHTime", 500, 1, 1000, (v) -> {
         return (Boolean)this.arrayList.getValue();
      }));
      this.animationVerticalTime = this.register(new Setting("AnimationVTime", 50, 1, 500, (v) -> {
         return (Boolean)this.arrayList.getValue();
      }));
      this.renderingMode = this.register(new Setting("Ordering", HUD.RenderingMode.ABC));
      this.time = this.register(new Setting("Time", false, "The time"));
      this.lagTime = this.register(new Setting("LagTime", 1000, 0, 2000));
      this.setInstance();
   }

   public static HUD getInstance() {
      if (INSTANCE == null) {
         INSTANCE = new HUD();
      }

      return INSTANCE;
   }

   private void setInstance() {
      INSTANCE = this;
   }

   public void onUpdate() {
      if (this.shouldIncrement) {
         ++this.hitMarkerTimer;
      }

      if (this.hitMarkerTimer == 10) {
         this.hitMarkerTimer = 0;
         this.shouldIncrement = false;
      }

   }

   public void onRender2D(Render2DEvent event) {
      if (!fullNullCheck()) {
         int width = this.renderer.scaledWidth;
         int height = this.renderer.scaledHeight;
         this.color = ColorUtil.toRGBA((Integer)ClickGui.getInstance().red.getValue(), (Integer)ClickGui.getInstance().green.getValue(), (Integer)ClickGui.getInstance().blue.getValue());
         int[] counter1;
         int var10002;
         if ((Boolean)this.waterMark.getValue() && this.watermakss.getValue() == HUD.WatermarkSelect.Big) {
            counter1 = new int[]{1};
            GL11.glPushMatrix();
            GL11.glScalef(1.8F, 1.8F, 1.8F);
            this.renderer.drawString("charon.", 2.0F, 0.0F, ColorUtil.rainbow(counter1[0] * (Integer)ClickGui.getInstance().rainbowHue.getValue()).getRGB(), true);
            GL11.glPopMatrix();
            GL11.glPushMatrix();
            GL11.glScalef(1.3F, 1.3F, 1.3F);
            this.renderer.drawString("eu", 54.0F, 2.95F, ColorUtil.rainbow(counter1[0] * (Integer)ClickGui.getInstance().rainbowHue.getValue()).getRGB(), true);
            GL11.glPopMatrix();
            var10002 = counter1[0]++;
         } else if ((Boolean)this.waterMark.getValue() && this.watermakss.getValue() == HUD.WatermarkSelect.Small) {
            counter1 = new int[]{1};
            this.renderer.drawString("charon.eu", 2.0F, 13.0F, ColorUtil.rainbow(counter1[0] * (Integer)ClickGui.getInstance().rainbowHue.getValue()).getRGB(), true);
            var10002 = counter1[0]++;
         }

         counter1 = new int[]{1};
         int j = mc.currentScreen instanceof GuiChat && !(Boolean)this.renderingUp.getValue() ? 14 : 0;
         String fpsText;
         if ((Boolean)this.arrayList.getValue()) {
            int k;
            String str;
            Module module;
            if ((Boolean)this.renderingUp.getValue()) {
               if (this.renderingMode.getValue() == HUD.RenderingMode.ABC) {
                  for(k = 0; k < Charon.moduleManager.sortedModulesABC.size(); ++k) {
                     str = (String)Charon.moduleManager.sortedModulesABC.get(k);
                     this.renderer.drawString(str, (float)(width - 2 - this.renderer.getStringWidth(str)), (float)(2 + j * 10), (Boolean)ClickGui.getInstance().rainbow.getValue() ? (ClickGui.getInstance().rainbowModeA.getValue() == ClickGui.rainbowModeArray.Up ? ColorUtil.rainbow(counter1[0] * (Integer)ClickGui.getInstance().rainbowHue.getValue()).getRGB() : ColorUtil.rainbow((Integer)ClickGui.getInstance().rainbowHue.getValue()).getRGB()) : this.color, true);
                     RenderUtil.drawRect((float)(width - 2 - this.renderer.getStringWidth(str)), (float)(2 + j * 10), (float)(width - 2 - this.renderer.getStringWidth(str)), 5.0F, 1996488704);
                     ++j;
                     var10002 = counter1[0]++;
                  }
               } else {
                  for(k = 0; k < Charon.moduleManager.sortedModules.size(); ++k) {
                     module = (Module)Charon.moduleManager.sortedModules.get(k);
                     fpsText = module.getDisplayName() + ChatFormatting.GRAY + (module.getDisplayInfo() != null ? " [" + ChatFormatting.WHITE + module.getDisplayInfo() + ChatFormatting.GRAY + "]" : "");
                     this.renderer.drawString(fpsText, (float)(width - 2 - this.renderer.getStringWidth(fpsText)), (float)(2 + j * 10), (Boolean)ClickGui.getInstance().rainbow.getValue() ? (ClickGui.getInstance().rainbowModeA.getValue() == ClickGui.rainbowModeArray.Up ? ColorUtil.rainbow(counter1[0] * (Integer)ClickGui.getInstance().rainbowHue.getValue()).getRGB() : ColorUtil.rainbow((Integer)ClickGui.getInstance().rainbowHue.getValue()).getRGB()) : this.color, true);
                     RenderUtil.drawRect((float)(width - 2 - this.renderer.getStringWidth(fpsText)), (float)(2 + j * 10), (float)(width - 2 - this.renderer.getStringWidth(fpsText)), 5.0F, 1996488704);
                     ++j;
                     var10002 = counter1[0]++;
                  }
               }
            } else if (this.renderingMode.getValue() == HUD.RenderingMode.ABC) {
               for(k = 0; k < Charon.moduleManager.sortedModulesABC.size(); ++k) {
                  str = (String)Charon.moduleManager.sortedModulesABC.get(k);
                  j += 10;
                  this.renderer.drawString(str, (float)(width - 2 - this.renderer.getStringWidth(str)), (float)(height - j), (Boolean)ClickGui.getInstance().rainbow.getValue() ? (ClickGui.getInstance().rainbowModeA.getValue() == ClickGui.rainbowModeArray.Up ? ColorUtil.rainbow(counter1[0] * (Integer)ClickGui.getInstance().rainbowHue.getValue()).getRGB() : ColorUtil.rainbow((Integer)ClickGui.getInstance().rainbowHue.getValue()).getRGB()) : this.color, true);
                  RenderUtil.drawRect((float)(width - 2 - this.renderer.getStringWidth(str)), (float)(2 + j * 10), (float)(width - 2 - this.renderer.getStringWidth(str)), 5.0F, 1996488704);
                  var10002 = counter1[0]++;
               }
            } else {
               for(k = 0; k < Charon.moduleManager.sortedModules.size(); ++k) {
                  module = (Module)Charon.moduleManager.sortedModules.get(k);
                  fpsText = module.getDisplayName() + ChatFormatting.GRAY + (module.getDisplayInfo() != null ? " [" + ChatFormatting.WHITE + module.getDisplayInfo() + ChatFormatting.GRAY + "]" : "");
                  j += 10;
                  this.renderer.drawString(fpsText, (float)(width - 2 - this.renderer.getStringWidth(fpsText)), (float)(height - j), (Boolean)ClickGui.getInstance().rainbow.getValue() ? (ClickGui.getInstance().rainbowModeA.getValue() == ClickGui.rainbowModeArray.Up ? ColorUtil.rainbow(counter1[0] * (Integer)ClickGui.getInstance().rainbowHue.getValue()).getRGB() : ColorUtil.rainbow((Integer)ClickGui.getInstance().rainbowHue.getValue()).getRGB()) : this.color, true);
                  RenderUtil.drawRect((float)(width - 2 - this.renderer.getStringWidth(fpsText) + 50), (float)(2 + j * 10), (float)(width - 2 - this.renderer.getStringWidth(fpsText)), 5.0F, 1996488704);
                  var10002 = counter1[0]++;
               }
            }
         }

         String grayString = (Boolean)this.grayNess.getValue() ? String.valueOf(ChatFormatting.GRAY) : "";
         int i = mc.currentScreen instanceof GuiChat && (Boolean)this.renderingUp.getValue() ? 13 : ((Boolean)this.renderingUp.getValue() ? -2 : 0);
         Iterator var9;
         PotionEffect potionEffect;
         String str;
         ArrayList effects;
         String str1;
         if ((Boolean)this.renderingUp.getValue()) {
            if ((Boolean)this.potions.getValue()) {
               effects = new ArrayList(Minecraft.getMinecraft().player.getActivePotionEffects());
               var9 = effects.iterator();

               while(var9.hasNext()) {
                  potionEffect = (PotionEffect)var9.next();
                  str = Charon.potionManager.getColoredPotionString(potionEffect);
                  i += 10;
                  this.renderer.drawString(str, (float)(width - this.renderer.getStringWidth(str) - 2), (float)(height - 2 - i), potionEffect.getPotion().getLiquidColor(), true);
               }
            }

            if ((Boolean)this.speed.getValue()) {
               fpsText = grayString + "Speed " + ChatFormatting.WHITE + Charon.speedManager.getSpeedKpH() + " km/h";
               i += 10;
               this.renderer.drawString(fpsText, (float)(width - this.renderer.getStringWidth(fpsText) - 2), (float)(height - 2 - i), (Boolean)ClickGui.getInstance().rainbow.getValue() ? (ClickGui.getInstance().rainbowModeA.getValue() == ClickGui.rainbowModeArray.Up ? ColorUtil.rainbow(counter1[0] * (Integer)ClickGui.getInstance().rainbowHue.getValue()).getRGB() : ColorUtil.rainbow((Integer)ClickGui.getInstance().rainbowHue.getValue()).getRGB()) : this.color, true);
               var10002 = counter1[0]++;
            }

            if ((Boolean)this.time.getValue()) {
               fpsText = grayString + "Time " + ChatFormatting.WHITE + (new SimpleDateFormat("h:mm a")).format(new Date());
               i += 10;
               this.renderer.drawString(fpsText, (float)(width - this.renderer.getStringWidth(fpsText) - 2), (float)(height - 2 - i), (Boolean)ClickGui.getInstance().rainbow.getValue() ? (ClickGui.getInstance().rainbowModeA.getValue() == ClickGui.rainbowModeArray.Up ? ColorUtil.rainbow(counter1[0] * (Integer)ClickGui.getInstance().rainbowHue.getValue()).getRGB() : ColorUtil.rainbow((Integer)ClickGui.getInstance().rainbowHue.getValue()).getRGB()) : this.color, true);
               var10002 = counter1[0]++;
            }

            if ((Boolean)this.tps.getValue()) {
               fpsText = grayString + "TPS " + ChatFormatting.WHITE + Charon.serverManager.getTPS();
               i += 10;
               this.renderer.drawString(fpsText, (float)(width - this.renderer.getStringWidth(fpsText) - 2), (float)(height - 2 - i), (Boolean)ClickGui.getInstance().rainbow.getValue() ? (ClickGui.getInstance().rainbowModeA.getValue() == ClickGui.rainbowModeArray.Up ? ColorUtil.rainbow(counter1[0] * (Integer)ClickGui.getInstance().rainbowHue.getValue()).getRGB() : ColorUtil.rainbow((Integer)ClickGui.getInstance().rainbowHue.getValue()).getRGB()) : this.color, true);
               var10002 = counter1[0]++;
            }

            fpsText = grayString + "FPS " + ChatFormatting.WHITE + Minecraft.debugFPS;
            str1 = grayString + "Ping " + ChatFormatting.WHITE + Charon.serverManager.getPing();
            if (this.renderer.getStringWidth(str1) > this.renderer.getStringWidth(fpsText)) {
               if ((Boolean)this.ping.getValue()) {
                  i += 10;
                  this.renderer.drawString(str1, (float)(width - this.renderer.getStringWidth(str1) - 2), (float)(height - 2 - i), (Boolean)ClickGui.getInstance().rainbow.getValue() ? (ClickGui.getInstance().rainbowModeA.getValue() == ClickGui.rainbowModeArray.Up ? ColorUtil.rainbow(counter1[0] * (Integer)ClickGui.getInstance().rainbowHue.getValue()).getRGB() : ColorUtil.rainbow((Integer)ClickGui.getInstance().rainbowHue.getValue()).getRGB()) : this.color, true);
                  var10002 = counter1[0]++;
               }

               if ((Boolean)this.fps.getValue()) {
                  i += 10;
                  this.renderer.drawString(fpsText, (float)(width - this.renderer.getStringWidth(fpsText) - 2), (float)(height - 2 - i), (Boolean)ClickGui.getInstance().rainbow.getValue() ? (ClickGui.getInstance().rainbowModeA.getValue() == ClickGui.rainbowModeArray.Up ? ColorUtil.rainbow(counter1[0] * (Integer)ClickGui.getInstance().rainbowHue.getValue()).getRGB() : ColorUtil.rainbow((Integer)ClickGui.getInstance().rainbowHue.getValue()).getRGB()) : this.color, true);
                  var10002 = counter1[0]++;
               }
            } else {
               if ((Boolean)this.fps.getValue()) {
                  i += 10;
                  this.renderer.drawString(fpsText, (float)(width - this.renderer.getStringWidth(fpsText) - 2), (float)(height - 2 - i), (Boolean)ClickGui.getInstance().rainbow.getValue() ? (ClickGui.getInstance().rainbowModeA.getValue() == ClickGui.rainbowModeArray.Up ? ColorUtil.rainbow(counter1[0] * (Integer)ClickGui.getInstance().rainbowHue.getValue()).getRGB() : ColorUtil.rainbow((Integer)ClickGui.getInstance().rainbowHue.getValue()).getRGB()) : this.color, true);
                  var10002 = counter1[0]++;
               }

               if ((Boolean)this.ping.getValue()) {
                  i += 10;
                  this.renderer.drawString(str1, (float)(width - this.renderer.getStringWidth(str1) - 2), (float)(height - 2 - i), (Boolean)ClickGui.getInstance().rainbow.getValue() ? (ClickGui.getInstance().rainbowModeA.getValue() == ClickGui.rainbowModeArray.Up ? ColorUtil.rainbow(counter1[0] * (Integer)ClickGui.getInstance().rainbowHue.getValue()).getRGB() : ColorUtil.rainbow((Integer)ClickGui.getInstance().rainbowHue.getValue()).getRGB()) : this.color, true);
                  var10002 = counter1[0]++;
               }
            }
         } else {
            if ((Boolean)this.potions.getValue()) {
               effects = new ArrayList(Minecraft.getMinecraft().player.getActivePotionEffects());
               var9 = effects.iterator();

               while(var9.hasNext()) {
                  potionEffect = (PotionEffect)var9.next();
                  str = Charon.potionManager.getColoredPotionString(potionEffect);
                  this.renderer.drawString(str, (float)(width - this.renderer.getStringWidth(str) - 2), (float)(2 + i++ * 10), potionEffect.getPotion().getLiquidColor(), true);
               }
            }

            if ((Boolean)this.speed.getValue()) {
               fpsText = grayString + "Speed " + ChatFormatting.WHITE + Charon.speedManager.getSpeedKpH() + " km/h";
               this.renderer.drawString(fpsText, (float)(width - this.renderer.getStringWidth(fpsText) - 2), (float)(2 + i++ * 10), (Boolean)ClickGui.getInstance().rainbow.getValue() ? (ClickGui.getInstance().rainbowModeA.getValue() == ClickGui.rainbowModeArray.Up ? ColorUtil.rainbow(counter1[0] * (Integer)ClickGui.getInstance().rainbowHue.getValue()).getRGB() : ColorUtil.rainbow((Integer)ClickGui.getInstance().rainbowHue.getValue()).getRGB()) : this.color, true);
               var10002 = counter1[0]++;
            }

            if ((Boolean)this.time.getValue()) {
               fpsText = grayString + "Time " + ChatFormatting.WHITE + (new SimpleDateFormat("h:mm a")).format(new Date());
               this.renderer.drawString(fpsText, (float)(width - this.renderer.getStringWidth(fpsText) - 2), (float)(2 + i++ * 10), (Boolean)ClickGui.getInstance().rainbow.getValue() ? (ClickGui.getInstance().rainbowModeA.getValue() == ClickGui.rainbowModeArray.Up ? ColorUtil.rainbow(counter1[0] * (Integer)ClickGui.getInstance().rainbowHue.getValue()).getRGB() : ColorUtil.rainbow((Integer)ClickGui.getInstance().rainbowHue.getValue()).getRGB()) : this.color, true);
               var10002 = counter1[0]++;
            }

            if ((Boolean)this.tps.getValue()) {
               fpsText = grayString + "TPS " + ChatFormatting.WHITE + Charon.serverManager.getTPS();
               this.renderer.drawString(fpsText, (float)(width - this.renderer.getStringWidth(fpsText) - 2), (float)(2 + i++ * 10), (Boolean)ClickGui.getInstance().rainbow.getValue() ? (ClickGui.getInstance().rainbowModeA.getValue() == ClickGui.rainbowModeArray.Up ? ColorUtil.rainbow(counter1[0] * (Integer)ClickGui.getInstance().rainbowHue.getValue()).getRGB() : ColorUtil.rainbow((Integer)ClickGui.getInstance().rainbowHue.getValue()).getRGB()) : this.color, true);
               var10002 = counter1[0]++;
            }

            fpsText = grayString + "FPS " + ChatFormatting.WHITE + Minecraft.debugFPS;
            str1 = grayString + "Ping " + ChatFormatting.WHITE + Charon.serverManager.getPing();
            if (this.renderer.getStringWidth(str1) > this.renderer.getStringWidth(fpsText)) {
               if ((Boolean)this.ping.getValue()) {
                  this.renderer.drawString(str1, (float)(width - this.renderer.getStringWidth(str1) - 2), (float)(2 + i++ * 10), (Boolean)ClickGui.getInstance().rainbow.getValue() ? (ClickGui.getInstance().rainbowModeA.getValue() == ClickGui.rainbowModeArray.Up ? ColorUtil.rainbow(counter1[0] * (Integer)ClickGui.getInstance().rainbowHue.getValue()).getRGB() : ColorUtil.rainbow((Integer)ClickGui.getInstance().rainbowHue.getValue()).getRGB()) : this.color, true);
                  var10002 = counter1[0]++;
               }

               if ((Boolean)this.fps.getValue()) {
                  this.renderer.drawString(fpsText, (float)(width - this.renderer.getStringWidth(fpsText) - 2), (float)(2 + i++ * 10), (Boolean)ClickGui.getInstance().rainbow.getValue() ? (ClickGui.getInstance().rainbowModeA.getValue() == ClickGui.rainbowModeArray.Up ? ColorUtil.rainbow(counter1[0] * (Integer)ClickGui.getInstance().rainbowHue.getValue()).getRGB() : ColorUtil.rainbow((Integer)ClickGui.getInstance().rainbowHue.getValue()).getRGB()) : this.color, true);
                  var10002 = counter1[0]++;
               }
            } else {
               if ((Boolean)this.fps.getValue()) {
                  this.renderer.drawString(fpsText, (float)(width - this.renderer.getStringWidth(fpsText) - 2), (float)(2 + i++ * 10), (Boolean)ClickGui.getInstance().rainbow.getValue() ? (ClickGui.getInstance().rainbowModeA.getValue() == ClickGui.rainbowModeArray.Up ? ColorUtil.rainbow(counter1[0] * (Integer)ClickGui.getInstance().rainbowHue.getValue()).getRGB() : ColorUtil.rainbow((Integer)ClickGui.getInstance().rainbowHue.getValue()).getRGB()) : this.color, true);
                  var10002 = counter1[0]++;
               }

               if ((Boolean)this.ping.getValue()) {
                  this.renderer.drawString(str1, (float)(width - this.renderer.getStringWidth(str1) - 2), (float)(2 + i++ * 10), (Boolean)ClickGui.getInstance().rainbow.getValue() ? (ClickGui.getInstance().rainbowModeA.getValue() == ClickGui.rainbowModeArray.Up ? ColorUtil.rainbow(counter1[0] * (Integer)ClickGui.getInstance().rainbowHue.getValue()).getRGB() : ColorUtil.rainbow((Integer)ClickGui.getInstance().rainbowHue.getValue()).getRGB()) : this.color, true);
                  var10002 = counter1[0]++;
               }
            }
         }

         boolean inHell = mc.world.getBiome(mc.player.getPosition()).getBiomeName().equals("Hell");
         int posX = (int)mc.player.posX;
         int posY = (int)mc.player.posY;
         int posZ = (int)mc.player.posZ;
         float nether = !inHell ? 0.125F : 8.0F;
         int hposX = (int)(mc.player.posX * (double)nether);
         int hposZ = (int)(mc.player.posZ * (double)nether);
         i = mc.currentScreen instanceof GuiChat ? 14 : 0;
         String coordinates = ChatFormatting.WHITE + "XYZ " + ChatFormatting.RESET + (inHell ? posX + ", " + posY + ", " + posZ + ChatFormatting.WHITE + " [" + ChatFormatting.RESET + hposX + ", " + hposZ + ChatFormatting.WHITE + "]" + ChatFormatting.RESET : posX + ", " + posY + ", " + posZ + ChatFormatting.WHITE + " [" + ChatFormatting.RESET + hposX + ", " + hposZ + ChatFormatting.WHITE + "]");
         String direction = (Boolean)this.direction.getValue() ? Charon.rotationManager.getDirection4D(false) : "";
         String coords = (Boolean)this.coords.getValue() ? coordinates : "";
         i += 10;
         if ((Boolean)ClickGui.getInstance().rainbow.getValue()) {
            String rainbowCoords = (Boolean)this.coords.getValue() ? "XYZ " + (inHell ? posX + ", " + posY + ", " + posZ + " [" + hposX + ", " + hposZ + "]" : posX + ", " + posY + ", " + posZ + " [" + hposX + ", " + hposZ + "]") : "";
            if (ClickGui.getInstance().rainbowModeHud.getValue() == ClickGui.rainbowMode.Static) {
               this.renderer.drawString(direction, 2.0F, (float)(height - i - 11), ColorUtil.rainbow((Integer)ClickGui.getInstance().rainbowHue.getValue()).getRGB(), true);
               this.renderer.drawString(rainbowCoords, 2.0F, (float)(height - i), ColorUtil.rainbow((Integer)ClickGui.getInstance().rainbowHue.getValue()).getRGB(), true);
            } else {
               int[] counter2 = new int[]{1};
               char[] stringToCharArray = direction.toCharArray();
               float s = 0.0F;
               char[] var22 = stringToCharArray;
               int var23 = stringToCharArray.length;

               for(int var24 = 0; var24 < var23; ++var24) {
                  char c = var22[var24];
                  this.renderer.drawString(String.valueOf(c), 2.0F + s, (float)(height - i - 11), ColorUtil.rainbow(counter2[0] * (Integer)ClickGui.getInstance().rainbowHue.getValue()).getRGB(), true);
                  s += (float)this.renderer.getStringWidth(String.valueOf(c));
                  var10002 = counter2[0]++;
               }

               int[] counter3 = new int[]{1};
               char[] stringToCharArray2 = rainbowCoords.toCharArray();
               float u = 0.0F;
               char[] var41 = stringToCharArray2;
               int var26 = stringToCharArray2.length;

               for(int var27 = 0; var27 < var26; ++var27) {
                  char c = var41[var27];
                  this.renderer.drawString(String.valueOf(c), 2.0F + u, (float)(height - i), ColorUtil.rainbow(counter3[0] * (Integer)ClickGui.getInstance().rainbowHue.getValue()).getRGB(), true);
                  u += (float)this.renderer.getStringWidth(String.valueOf(c));
                  var10002 = counter3[0]++;
               }
            }
         } else {
            this.renderer.drawString(direction, 2.0F, (float)(height - i - 11), this.color, true);
            this.renderer.drawString(coords, 2.0F, (float)(height - i), this.color, true);
         }

         if ((Boolean)this.armor.getValue()) {
            this.renderArmorHUD(true);
         }

         if ((Boolean)this.totems.getValue()) {
            this.renderTotemHUD();
         }

         if ((Boolean)this.greeter.getValue()) {
            this.renderGreeter();
         }

         if ((Boolean)this.lag.getValue()) {
            this.renderLag();
         }

      }
   }

   public Map<String, Integer> getTextRadarPlayers() {
      return EntityUtil.getTextRadarPlayers();
   }

   public void renderGreeter() {
      int width = this.renderer.scaledWidth;
      String text = "";
      String text2 = "Welcome to charon.eu / ";
      if ((Boolean)this.greeter.getValue()) {
         text = text + text2 + mc.player.getDisplayNameString();
      }

      if ((Boolean)ClickGui.getInstance().rainbow.getValue()) {
         if (ClickGui.getInstance().rainbowModeHud.getValue() == ClickGui.rainbowMode.Static) {
            this.renderer.drawString(text, 2.0F, 2.0F, ColorUtil.rainbow((Integer)ClickGui.getInstance().rainbowHue.getValue()).getRGB(), true);
         } else {
            int[] counter1 = new int[]{1};
            this.renderer.drawString(text, 2.0F, 2.0F, ColorUtil.rainbow(counter1[0] * (Integer)ClickGui.getInstance().rainbowHue.getValue()).getRGB(), true);
            int var10002 = counter1[0]++;
         }
      } else {
         this.renderer.drawString(text, 2.0F, 2.0F, this.color, true);
      }

   }

   public void renderLag() {
      int width = this.renderer.scaledWidth;
      if (Charon.serverManager.isServerNotResponding()) {
         String text = ChatFormatting.RED + "Server not responding " + MathUtil.round((float)Charon.serverManager.serverRespondingTime() / 1000.0F, 1) + "s.";
         this.renderer.drawString(text, (float)width / 2.0F - (float)this.renderer.getStringWidth(text) / 2.0F + 2.0F, 20.0F, this.color, true);
      }

   }

   public void renderTotemHUD() {
      int width = this.renderer.scaledWidth;
      int height = this.renderer.scaledHeight;
      int totems = mc.player.inventory.mainInventory.stream().filter((itemStack) -> {
         return itemStack.getItem() == Items.TOTEM_OF_UNDYING;
      }).mapToInt(ItemStack::getCount).sum();
      if (mc.player.getHeldItemOffhand().getItem() == Items.TOTEM_OF_UNDYING) {
         totems += mc.player.getHeldItemOffhand().getCount();
      }

      if (totems > 0) {
         GlStateManager.enableTexture2D();
         int i = width / 2;
         int iteration = 0;
         int y = height - 55 - (mc.player.isInWater() && mc.playerController.gameIsSurvivalOrAdventure() ? 10 : 0);
         int x = i - 189 + 180 + 2;
         GlStateManager.enableDepth();
         RenderUtil.itemRender.zLevel = 200.0F;
         RenderUtil.itemRender.renderItemAndEffectIntoGUI(totem, x, y);
         RenderUtil.itemRender.renderItemOverlayIntoGUI(mc.fontRenderer, totem, x, y, "");
         RenderUtil.itemRender.zLevel = 0.0F;
         GlStateManager.enableTexture2D();
         GlStateManager.disableLighting();
         GlStateManager.disableDepth();
         this.renderer.drawStringWithShadow(totems + "", (float)(x + 19 - 2 - this.renderer.getStringWidth(totems + "")), (float)(y + 9), 16777215);
         GlStateManager.enableDepth();
         GlStateManager.disableLighting();
      }

   }

   public void renderArmorHUD(boolean percent) {
      int width = this.renderer.scaledWidth;
      int height = this.renderer.scaledHeight;
      GlStateManager.enableTexture2D();
      int i = width / 2;
      int iteration = 0;
      int y = height - 55 - (mc.player.isInWater() && mc.playerController.gameIsSurvivalOrAdventure() ? 10 : 0);
      Iterator var7 = mc.player.inventory.armorInventory.iterator();

      while(var7.hasNext()) {
         ItemStack is = (ItemStack)var7.next();
         ++iteration;
         if (!is.isEmpty()) {
            int x = i - 90 + (9 - iteration) * 20 + 2;
            GlStateManager.enableDepth();
            RenderUtil.itemRender.zLevel = 200.0F;
            RenderUtil.itemRender.renderItemAndEffectIntoGUI(is, x, y);
            RenderUtil.itemRender.renderItemOverlayIntoGUI(mc.fontRenderer, is, x, y, "");
            RenderUtil.itemRender.zLevel = 0.0F;
            GlStateManager.enableTexture2D();
            GlStateManager.disableLighting();
            GlStateManager.disableDepth();
            String s = is.getCount() > 1 ? is.getCount() + "" : "";
            this.renderer.drawStringWithShadow(s, (float)(x + 19 - 2 - this.renderer.getStringWidth(s)), (float)(y + 9), 16777215);
            if (percent) {
               float green = (float)((is.getMaxDamage() - is.getItemDamage()) / is.getMaxDamage());
               float red = 1.0F - green;
               int dmg = 100 - (int)(red * 100.0F);
               this.renderer.drawStringWithShadow(dmg + "", (float)(x + 8 - this.renderer.getStringWidth(dmg + "") / 2), (float)(y - 11), ColorUtil.toRGBA((int)(red * 255.0F), (int)(green * 255.0F), 0));
            }
         }
      }

      GlStateManager.enableDepth();
      GlStateManager.disableLighting();
   }

   @SubscribeEvent
   public void onUpdateWalkingPlayer(AttackEntityEvent event) {
      this.shouldIncrement = true;
   }

   public void onLoad() {
      Charon.commandManager.setClientMessage(this.getCommandMessage());
   }

   @SubscribeEvent
   public void onSettingChange(ClientEvent event) {
      if (event.getStage() == 2 && this.equals(event.getSetting().getFeature())) {
         Charon.commandManager.setClientMessage(this.getCommandMessage());
      }

   }

   public String getCommandMessage() {
      return TextUtil.coloredString((String)this.commandBracket.getPlannedValue(), (TextUtil.Color)this.bracketColor.getPlannedValue()) + TextUtil.coloredString((String)this.command.getPlannedValue(), (TextUtil.Color)this.commandColor.getPlannedValue()) + TextUtil.coloredString((String)this.commandBracket2.getPlannedValue(), (TextUtil.Color)this.bracketColor.getPlannedValue());
   }

   public void drawTextRadar(int yOffset) {
      if (!this.players.isEmpty()) {
         int y = this.renderer.getFontHeight() + 7 + yOffset;

         int textheight;
         for(Iterator var3 = this.players.entrySet().iterator(); var3.hasNext(); y += textheight) {
            Entry<String, Integer> player = (Entry)var3.next();
            String text = (String)player.getKey() + " ";
            textheight = this.renderer.getFontHeight() + 1;
            this.renderer.drawString(text, 2.0F, (float)y, this.color, true);
         }
      }

   }

   static {
      totem = new ItemStack(Items.TOTEM_OF_UNDYING);
      INSTANCE = new HUD();
   }

   public static enum ChatS {
      SILENT,
      SPAM;
   }

   public static enum WatermarkSelect {
      Big,
      Small,
      Csgo;
   }

   public static enum RenderingMode {
      Length,
      ABC;
   }
}
