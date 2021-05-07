package cc.zip.charon.features.modules.player;

import cc.zip.charon.Charon;
import cc.zip.charon.event.events.BlockEvent;
import cc.zip.charon.event.events.Render3DEvent;
import cc.zip.charon.features.modules.Module;
import cc.zip.charon.features.setting.Setting;
import cc.zip.charon.util.BlockUtil;
import cc.zip.charon.util.InventoryUtil;
import cc.zip.charon.util.RenderUtil;
import cc.zip.charon.util.Timer;
import java.awt.Color;
import net.minecraft.block.state.IBlockState;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemPickaxe;
import net.minecraft.item.ItemSword;
import net.minecraft.network.play.client.CPacketPlayerDigging;
import net.minecraft.network.play.client.CPacketPlayerDigging.Action;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class Speedmine extends Module {
   private static Speedmine INSTANCE = new Speedmine();
   private final Timer timer = new Timer();
   public Setting<Speedmine.Mode> mode;
   public Setting<Float> damage;
   public Setting<Boolean> webSwitch;
   public Setting<Boolean> doubleBreak;
   public Setting<Boolean> render;
   public Setting<Boolean> box;
   private final Setting<Integer> boxAlpha;
   public Setting<Boolean> outline;
   private final Setting<Float> lineWidth;
   public BlockPos currentPos;
   public IBlockState currentBlockState;

   public Speedmine() {
      super("Speedmine", "Speeds up mining.", Module.Category.PLAYER, true, false, false);
      this.mode = this.register(new Setting("Mode", Speedmine.Mode.PACKET));
      this.damage = this.register(new Setting("Damage", 0.7F, 0.0F, 1.0F, (v) -> {
         return this.mode.getValue() == Speedmine.Mode.DAMAGE;
      }));
      this.webSwitch = this.register(new Setting("WebSwitch", false));
      this.doubleBreak = this.register(new Setting("DoubleBreak", false));
      this.render = this.register(new Setting("Render", false));
      this.box = this.register(new Setting("Box", false, (v) -> {
         return (Boolean)this.render.getValue();
      }));
      this.boxAlpha = this.register(new Setting("BoxAlpha", 85, 0, 255, (v) -> {
         return (Boolean)this.box.getValue() && (Boolean)this.render.getValue();
      }));
      this.outline = this.register(new Setting("Outline", true, (v) -> {
         return (Boolean)this.render.getValue();
      }));
      this.lineWidth = this.register(new Setting("Width", 1.0F, 0.1F, 5.0F, (v) -> {
         return (Boolean)this.outline.getValue() && (Boolean)this.render.getValue();
      }));
      this.setInstance();
   }

   public static Speedmine getInstance() {
      if (INSTANCE == null) {
         INSTANCE = new Speedmine();
      }

      return INSTANCE;
   }

   private void setInstance() {
      INSTANCE = this;
   }

   public void onTick() {
      if (this.currentPos != null) {
         if (mc.world.getBlockState(this.currentPos).equals(this.currentBlockState) && mc.world.getBlockState(this.currentPos).getBlock() != Blocks.AIR) {
            if ((Boolean)this.webSwitch.getValue() && this.currentBlockState.getBlock() == Blocks.WEB && mc.player.getHeldItemMainhand().getItem() instanceof ItemPickaxe) {
               InventoryUtil.switchToHotbarSlot(ItemSword.class, false);
            }
         } else {
            this.currentPos = null;
            this.currentBlockState = null;
         }
      }

   }

   public void onUpdate() {
      if (!fullNullCheck()) {
         mc.playerController.blockHitDelay = 0;
      }
   }

   public void onRender3D(Render3DEvent event) {
      if ((Boolean)this.render.getValue() && this.currentPos != null && this.currentBlockState.getBlock() == Blocks.OBSIDIAN) {
         Color color = new Color(this.timer.passedMs((long)((int)(2000.0F * Charon.serverManager.getTpsFactor()))) ? 0 : 255, this.timer.passedMs((long)((int)(2000.0F * Charon.serverManager.getTpsFactor()))) ? 255 : 0, 0, 255);
         RenderUtil.drawBoxESP(this.currentPos, color, false, color, (Float)this.lineWidth.getValue(), (Boolean)this.outline.getValue(), (Boolean)this.box.getValue(), (Integer)this.boxAlpha.getValue(), false);
      }

   }

   @SubscribeEvent
   public void onBlockEvent(BlockEvent event) {
      if (!fullNullCheck()) {
         if (event.getStage() == 3 && mc.playerController.curBlockDamageMP > 0.1F) {
            mc.playerController.isHittingBlock = true;
         }

         if (event.getStage() == 4) {
            if (BlockUtil.canBreak(event.pos)) {
               mc.playerController.isHittingBlock = false;
               switch((Speedmine.Mode)this.mode.getValue()) {
               case PACKET:
                  if (this.currentPos == null) {
                     this.currentPos = event.pos;
                     this.currentBlockState = mc.world.getBlockState(this.currentPos);
                     this.timer.reset();
                  }

                  mc.player.swingArm(EnumHand.MAIN_HAND);
                  mc.player.connection.sendPacket(new CPacketPlayerDigging(Action.START_DESTROY_BLOCK, event.pos, event.facing));
                  mc.player.connection.sendPacket(new CPacketPlayerDigging(Action.STOP_DESTROY_BLOCK, event.pos, event.facing));
                  event.setCanceled(true);
                  break;
               case DAMAGE:
                  if (mc.playerController.curBlockDamageMP >= (Float)this.damage.getValue()) {
                     mc.playerController.curBlockDamageMP = 1.0F;
                  }
                  break;
               case INSTANT:
                  mc.player.swingArm(EnumHand.MAIN_HAND);
                  mc.player.connection.sendPacket(new CPacketPlayerDigging(Action.START_DESTROY_BLOCK, event.pos, event.facing));
                  mc.player.connection.sendPacket(new CPacketPlayerDigging(Action.STOP_DESTROY_BLOCK, event.pos, event.facing));
                  mc.playerController.onPlayerDestroyBlock(event.pos);
                  mc.world.setBlockToAir(event.pos);
               }
            }

            BlockPos above;
            if ((Boolean)this.doubleBreak.getValue() && BlockUtil.canBreak(above = event.pos.add(0, 1, 0)) && mc.player.getDistance((double)above.getX(), (double)above.getY(), (double)above.getZ()) <= 5.0D) {
               mc.player.swingArm(EnumHand.MAIN_HAND);
               mc.player.connection.sendPacket(new CPacketPlayerDigging(Action.START_DESTROY_BLOCK, above, event.facing));
               mc.player.connection.sendPacket(new CPacketPlayerDigging(Action.STOP_DESTROY_BLOCK, above, event.facing));
               mc.playerController.onPlayerDestroyBlock(above);
               mc.world.setBlockToAir(above);
            }
         }

      }
   }

   public String getDisplayInfo() {
      return this.mode.currentEnumName();
   }

   public static enum Mode {
      PACKET,
      DAMAGE,
      INSTANT;
   }
}
