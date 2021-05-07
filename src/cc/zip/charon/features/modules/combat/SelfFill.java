package cc.zip.charon.features.modules.combat;

import cc.zip.charon.Charon;
import cc.zip.charon.features.modules.Module;
import cc.zip.charon.features.setting.Setting;
import cc.zip.charon.util.InventoryUtil;
import cc.zip.charon.util.MappingUtil;
import cc.zip.charon.util.Util;
import cc.zip.charon.util.WorldUtil;
import java.lang.reflect.Field;
import net.minecraft.client.Minecraft;
import net.minecraft.init.Blocks;
import net.minecraft.network.play.client.CPacketChatMessage;
import net.minecraft.network.play.client.CPacketPlayer.Position;
import net.minecraft.util.Timer;
import net.minecraft.util.math.BlockPos;

public class SelfFill extends Module {
   private BlockPos playerPos;
   private final Setting<SelfFill.Modes> mode;
   private final Setting<Boolean> test2;
   private final Setting<Boolean> futureBeta;
   private final Setting<Boolean> toggleRStep;
   private final Setting<Boolean> test;
   private final Setting<SelfFill.Settings> setting;

   public SelfFill() {
      super("Burrow", "SelfFills yourself in a hole.", Module.Category.MOVEMENT, true, false, true);
      this.mode = this.register(new Setting("Settings", SelfFill.Modes.Silent));
      this.test2 = this.register(new Setting("Test", false, (v) -> {
         return this.mode.getValue() == SelfFill.Modes.Silent;
      }));
      this.futureBeta = this.register(new Setting("SilentJump", false, (v) -> {
         return this.mode.getValue() == SelfFill.Modes.Silent;
      }));
      this.toggleRStep = this.register(new Setting("ToggleRStep", false, (v) -> {
         return this.mode.getValue() == SelfFill.Modes.Silent;
      }));
      this.test = this.register(new Setting("Test", false, (v) -> {
         return this.mode.getValue() == SelfFill.Modes.Silent;
      }));
      this.setting = this.register(new Setting("Settings", SelfFill.Settings.Obsidian, (v) -> {
         return this.mode.getValue() == SelfFill.Modes.Silent;
      }));
   }

   public void onEnable() {
      if (this.mode.getValue() == SelfFill.Modes.Silent) {
         if ((Boolean)this.futureBeta.getValue()) {
            this.setTimer(50.0F);
         }

         if ((Boolean)this.toggleRStep.getValue()) {
            Charon.moduleManager.getModuleByName("ReverseStep").disable();
         }

         this.playerPos = new BlockPos(Util.mc.player.posX, Util.mc.player.posY, Util.mc.player.posZ);
         if (this.setting.getValue() == SelfFill.Settings.Obsidian && Util.mc.world.getBlockState(this.playerPos).getBlock().equals(Blocks.OBSIDIAN)) {
            this.disable();
            return;
         }

         if (this.setting.getValue() == SelfFill.Settings.EnderChest && Util.mc.world.getBlockState(this.playerPos).getBlock().equals(Blocks.ENDER_CHEST)) {
            this.disable();
            return;
         }

         Util.mc.player.jump();
      }

      if (this.mode.getValue() == SelfFill.Modes.SuolOp) {
         mc.player.connection.sendPacket(new CPacketChatMessage("/setblock ~ ~ ~ obsidian"));
         this.disable();
      }

   }

   public void onDisable() {
      if (this.mode.getValue() == SelfFill.Modes.Silent) {
         if ((Boolean)this.toggleRStep.getValue()) {
            Charon.moduleManager.getModuleByName("ReverseStep").enable();
         }

         this.setTimer(1.0F);
      }

   }

   public void onUpdate() {
      if (this.mode.getValue() == SelfFill.Modes.Silent) {
         if (nullCheck()) {
            return;
         }

         if (Util.mc.player.posY > (double)this.playerPos.getY() + 1.04D) {
            if ((Boolean)this.test2.getValue()) {
               mc.getConnection().sendPacket(new Position(mc.player.posX, mc.player.posY + 3.0D, mc.player.posZ, false));
            }

            if (this.setting.getValue() == SelfFill.Settings.Obsidian) {
               WorldUtil.placeBlock(this.playerPos, InventoryUtil.findHotbarBlock(Blocks.OBSIDIAN));
            }

            if (this.setting.getValue() == SelfFill.Settings.EnderChest) {
               WorldUtil.placeBlock(this.playerPos, InventoryUtil.findHotbarBlock(Blocks.ENDER_CHEST));
            }

            if ((Boolean)this.test2.getValue()) {
               mc.getConnection().sendPacket(new Position(mc.player.posX, mc.player.posY - 3.0D, mc.player.posZ, true));
            }

            if (!(Boolean)this.test2.getValue()) {
               Util.mc.player.jump();
            }

            this.disable();
         }
      }

   }

   public String getDisplayInfo() {
      if (this.mode.getValue() == SelfFill.Modes.Silent) {
         return "SemiFast";
      } else {
         return this.mode.getValue() == SelfFill.Modes.SuolOp ? "SuolFill" : "Burrow";
      }
   }

   private void setTimer(float value) {
      try {
         Field timer = Minecraft.class.getDeclaredField(MappingUtil.timer);
         timer.setAccessible(true);
         Field tickLength = Timer.class.getDeclaredField(MappingUtil.tickLength);
         tickLength.setAccessible(true);
         tickLength.setFloat(timer.get(Util.mc), 50.0F / value);
      } catch (Exception var4) {
         var4.printStackTrace();
      }

   }

   public static enum Modes {
      Silent,
      SuolOp;
   }

   public static enum Settings {
      Obsidian,
      EnderChest;
   }
}
