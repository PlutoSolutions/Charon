package cc.zip.charon.features.modules.render;

import cc.zip.charon.event.events.Render3DEvent;
import cc.zip.charon.features.modules.Module;
import cc.zip.charon.features.setting.Setting;
import cc.zip.charon.util.BlockUtil;
import cc.zip.charon.util.RenderUtil;
import java.awt.Color;
import net.minecraft.init.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3i;

public class HoleESP extends Module {
   private static HoleESP INSTANCE = new HoleESP();
   private final Setting<Integer> range = this.register(new Setting("RangeX", 0, 0, 10));
   private final Setting<Integer> rangeY = this.register(new Setting("RangeY", 0, 0, 10));
   private final Setting<Integer> red = this.register(new Setting("Red", 0, 0, 255));
   private final Setting<Integer> green = this.register(new Setting("Green", 255, 0, 255));
   private final Setting<Integer> blue = this.register(new Setting("Blue", 0, 0, 255));
   private final Setting<Integer> alpha = this.register(new Setting("Alpha", 255, 0, 255));
   private final Setting<Integer> boxAlpha = this.register(new Setting("BoxAlpha", 125, 0, 255));
   private final Setting<Float> lineWidth = this.register(new Setting("LineWidth", 1.0F, 0.1F, 5.0F));
   private final Setting<Integer> safeRed = this.register(new Setting("BedrockRed", 0, 0, 255));
   private final Setting<Integer> safeGreen = this.register(new Setting("BedrockGreen", 255, 0, 255));
   private final Setting<Integer> safeBlue = this.register(new Setting("BedrockBlue", 0, 0, 255));
   private final Setting<Integer> safeAlpha = this.register(new Setting("BedrockAlpha", 255, 0, 255));
   public Setting<Boolean> future = this.register(new Setting("FutureRender", true));
   public Setting<Boolean> fov = this.register(new Setting("InFov", true));
   public Setting<Boolean> renderOwn = this.register(new Setting("RenderOwn", true));
   public Setting<Boolean> box = this.register(new Setting("Box", true));
   public Setting<Boolean> outline = this.register(new Setting("Outline", true));
   private final Setting<Integer> cRed = this.register(new Setting("OL-Red", 0, 0, 255, (v) -> {
      return (Boolean)this.outline.getValue();
   }));
   private final Setting<Integer> cGreen = this.register(new Setting("OL-Green", 0, 0, 255, (v) -> {
      return (Boolean)this.outline.getValue();
   }));
   private final Setting<Integer> cBlue = this.register(new Setting("OL-Blue", 255, 0, 255, (v) -> {
      return (Boolean)this.outline.getValue();
   }));
   private final Setting<Integer> cAlpha = this.register(new Setting("OL-Alpha", 255, 0, 255, (v) -> {
      return (Boolean)this.outline.getValue();
   }));
   private final Setting<Integer> safecRed = this.register(new Setting("OL-BedrockRed", 0, 0, 255, (v) -> {
      return (Boolean)this.outline.getValue();
   }));
   private final Setting<Integer> safecGreen = this.register(new Setting("OL-BedrockGreen", 255, 0, 255, (v) -> {
      return (Boolean)this.outline.getValue();
   }));
   private final Setting<Integer> safecBlue = this.register(new Setting("OL-BedrockBlue", 0, 0, 255, (v) -> {
      return (Boolean)this.outline.getValue();
   }));
   private final Setting<Integer> safecAlpha = this.register(new Setting("OL-BedrockAlpha", 255, 0, 255, (v) -> {
      return (Boolean)this.outline.getValue();
   }));

   public HoleESP() {
      super("HoleESP", "Shows safe spots.", Module.Category.RENDER, false, false, false);
      this.setInstance();
   }

   public static HoleESP getInstance() {
      if (INSTANCE == null) {
         INSTANCE = new HoleESP();
      }

      return INSTANCE;
   }

   private void setInstance() {
      INSTANCE = this;
   }

   public void onRender3D(Render3DEvent event) {
      assert mc.renderViewEntity != null;

      Vec3i playerPos = new Vec3i(mc.renderViewEntity.posX, mc.renderViewEntity.posY, mc.renderViewEntity.posZ);

      for(int x = playerPos.getX() - (Integer)this.range.getValue(); x < playerPos.getX() + (Integer)this.range.getValue(); ++x) {
         for(int z = playerPos.getZ() - (Integer)this.range.getValue(); z < playerPos.getZ() + (Integer)this.range.getValue(); ++z) {
            for(int y = playerPos.getY() + (Integer)this.rangeY.getValue(); y > playerPos.getY() - (Integer)this.rangeY.getValue(); --y) {
               BlockPos pos = new BlockPos(x, y, z);
               if (mc.world.getBlockState(pos).getBlock().equals(Blocks.AIR) && mc.world.getBlockState(pos.add(0, 1, 0)).getBlock().equals(Blocks.AIR) && mc.world.getBlockState(pos.add(0, 2, 0)).getBlock().equals(Blocks.AIR) && (!pos.equals(new BlockPos(mc.player.posX, mc.player.posY, mc.player.posZ)) || (Boolean)this.renderOwn.getValue()) && (BlockUtil.isPosInFov(pos) || !(Boolean)this.fov.getValue())) {
                  if (mc.world.getBlockState(pos.north()).getBlock() == Blocks.BEDROCK && mc.world.getBlockState(pos.east()).getBlock() == Blocks.BEDROCK && mc.world.getBlockState(pos.west()).getBlock() == Blocks.BEDROCK && mc.world.getBlockState(pos.south()).getBlock() == Blocks.BEDROCK && mc.world.getBlockState(pos.down()).getBlock() == Blocks.BEDROCK) {
                     RenderUtil.drawBoxESP((Boolean)this.future.getValue() ? pos.down() : pos, new Color((Integer)this.safeRed.getValue(), (Integer)this.safeGreen.getValue(), (Integer)this.safeBlue.getValue(), (Integer)this.safeAlpha.getValue()), (Boolean)this.outline.getValue(), new Color((Integer)this.safecRed.getValue(), (Integer)this.safecGreen.getValue(), (Integer)this.safecBlue.getValue(), (Integer)this.safecAlpha.getValue()), (Float)this.lineWidth.getValue(), (Boolean)this.outline.getValue(), (Boolean)this.box.getValue(), (Integer)this.boxAlpha.getValue(), true);
                  } else if (BlockUtil.isBlockUnSafe(mc.world.getBlockState(pos.down()).getBlock()) && BlockUtil.isBlockUnSafe(mc.world.getBlockState(pos.east()).getBlock()) && BlockUtil.isBlockUnSafe(mc.world.getBlockState(pos.west()).getBlock()) && BlockUtil.isBlockUnSafe(mc.world.getBlockState(pos.south()).getBlock()) && BlockUtil.isBlockUnSafe(mc.world.getBlockState(pos.north()).getBlock())) {
                     RenderUtil.drawBoxESP((Boolean)this.future.getValue() ? pos.down() : pos, new Color((Integer)this.red.getValue(), (Integer)this.green.getValue(), (Integer)this.blue.getValue(), (Integer)this.alpha.getValue()), (Boolean)this.outline.getValue(), new Color((Integer)this.cRed.getValue(), (Integer)this.cGreen.getValue(), (Integer)this.cBlue.getValue(), (Integer)this.cAlpha.getValue()), (Float)this.lineWidth.getValue(), (Boolean)this.outline.getValue(), (Boolean)this.box.getValue(), (Integer)this.boxAlpha.getValue(), true);
                  }
               }
            }
         }
      }

   }
}
