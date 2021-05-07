package cc.zip.charon.features.modules.movement;

import cc.zip.charon.Charon;
import cc.zip.charon.event.events.ClientEvent;
import cc.zip.charon.event.events.MoveEvent;
import cc.zip.charon.features.modules.Module;
import cc.zip.charon.features.setting.Setting;
import cc.zip.charon.util.EntityUtil;
import cc.zip.charon.util.EntityUtilTwo;
import cc.zip.charon.util.MotionUtil;
import cc.zip.charon.util.Timer;
import cc.zip.charon.util.none.WorldTimer;
import net.minecraft.util.MovementInput;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class Speed extends Module {
   private static Speed INSTANCE = new Speed();
   public Setting<Speed.Mode> modes;
   public Setting<Boolean> timersr;
   public Setting<Boolean> strafeJump;
   public Setting<Boolean> noShake;
   public Setting<Boolean> useTimer;
   private final Setting<Double> yPortSpeed;
   public Setting<Boolean> motionyonoff;
   public Setting<Boolean> stepyport;
   private Timer timer;
   private float stepheight;
   public double startY;
   public boolean antiShake;
   public double minY;
   public boolean changeY;
   private double highChainVal;
   private double lowChainVal;
   private boolean oneTime;
   private double bounceHeight;
   private float move;
   private int vanillaCounter;
   private final WorldTimer timers;

   public Speed() {
      super("Speed", "Speed.", Module.Category.MOVEMENT, true, false, false);
      this.modes = this.register(new Setting("Mode", Speed.Mode.STRAFE));
      this.timersr = this.register(new Setting("Timer", true));
      this.strafeJump = this.register(new Setting("Jump", true, (v) -> {
         return this.modes.getValue() == Speed.Mode.INSTANT;
      }));
      this.noShake = this.register(new Setting("NoShake", true));
      this.useTimer = this.register(new Setting("UseTimer", true, (v) -> {
         return this.modes.getValue() == Speed.Mode.INSTANT;
      }));
      this.yPortSpeed = this.register(new Setting("YPort Speed", 0.1D, 0.0D, 1.0D, (v) -> {
         return this.modes.getValue() == Speed.Mode.YPORT;
      }));
      this.motionyonoff = this.register(new Setting("My- on off", true));
      this.stepyport = this.register(new Setting("OnStep 2", true));
      this.timer = new Timer();
      this.stepheight = 2.0F;
      this.startY = 0.0D;
      this.antiShake = false;
      this.minY = 0.0D;
      this.changeY = false;
      this.highChainVal = 0.0D;
      this.lowChainVal = 0.0D;
      this.oneTime = false;
      this.bounceHeight = 0.4D;
      this.move = 0.26F;
      this.vanillaCounter = 0;
      this.timers = new WorldTimer();
      this.setInstance();
   }

   public static Speed getInstance() {
      if (INSTANCE == null) {
         INSTANCE = new Speed();
      }

      return INSTANCE;
   }

   private void setInstance() {
      INSTANCE = this;
   }

   private boolean shouldReturn() {
      return Charon.moduleManager.isModuleEnabled("ElytraFlight") || Charon.moduleManager.isModuleEnabled("Flight");
   }

   public String getDisplayInfo() {
      return this.modes.currentEnumName();
   }

   public void onDisable() {
      this.timer.reset();
      EntityUtilTwo.resetTimer();
   }

   public void onUpdate() {
      if (mc.player != null && mc.world != null) {
         if (this.modes.getValue() == Speed.Mode.YPORT) {
            this.handleYPortSpeed();
         }

         if (!mc.player.isOnLadder() || mc.player.isInWater() || mc.player.isInLava() && (Boolean)this.stepyport.getValue()) {
            Step.mc.player.stepHeight = this.stepheight;
         }
      } else {
         this.disable();
      }
   }

   public void onToggle() {
      Step.mc.player.stepHeight = 0.6F;
      if (this.modes.getValue() == Speed.Mode.YPORT && (Boolean)this.motionyonoff.getValue()) {
         mc.player.motionY = -3.0D;
      }

   }

   private void handleYPortSpeed() {
      if (MotionUtil.isMoving(mc.player) && (!mc.player.isInWater() || !mc.player.isInLava()) && !mc.player.collidedHorizontally) {
         if (mc.player.onGround) {
            EntityUtilTwo.setTimer(1.15F);
            mc.player.jump();
            MotionUtil.setSpeed(mc.player, MotionUtil.getBaseMoveSpeed() + (Double)this.yPortSpeed.getValue());
         } else {
            mc.player.motionY = -1.0D;
            EntityUtilTwo.resetTimer();
         }

      }
   }

   @SubscribeEvent
   public void onSettingChange(ClientEvent event) {
      if (event.getStage() == 2 && event.getSetting().equals(this.modes) && this.modes.getPlannedValue() == Speed.Mode.INSTANT) {
         mc.player.motionY = -0.1D;
      }

   }

   @SubscribeEvent
   public void onMode(MoveEvent event) {
      if (!this.shouldReturn() && event.getStage() == 0 && this.modes.getValue() == Speed.Mode.INSTANT && !nullCheck() && !mc.player.isSneaking() && !mc.player.isInWater() && !mc.player.isInLava() && (mc.player.movementInput.moveForward != 0.0F || mc.player.movementInput.moveStrafe != 0.0F)) {
         if (mc.player.onGround && (Boolean)this.strafeJump.getValue()) {
            mc.player.motionY = 0.4D;
            event.setY(0.4D);
         }

         MovementInput movementInput = mc.player.movementInput;
         float moveForward = movementInput.moveForward;
         float moveStrafe = movementInput.moveStrafe;
         float rotationYaw = mc.player.rotationYaw;
         if ((double)moveForward == 0.0D && (double)moveStrafe == 0.0D) {
            event.setX(0.0D);
            event.setZ(0.0D);
         } else {
            if ((double)moveForward != 0.0D) {
               if ((double)moveStrafe > 0.0D) {
                  rotationYaw += (float)((double)moveForward > 0.0D ? -45 : 45);
               } else if ((double)moveStrafe < 0.0D) {
                  rotationYaw += (float)((double)moveForward > 0.0D ? 45 : -45);
               }

               moveStrafe = 0.0F;
               if (moveForward != 0.0F) {
                  moveForward = (double)moveForward > 0.0D ? 1.0F : -1.0F;
               }
            }

            moveStrafe = moveStrafe == 0.0F ? moveStrafe : ((double)moveStrafe > 0.0D ? 1.0F : -1.0F);
            event.setX((double)moveForward * EntityUtil.getMaxSpeed() * Math.cos(Math.toRadians((double)(rotationYaw + 90.0F))) + (double)moveStrafe * EntityUtil.getMaxSpeed() * Math.sin(Math.toRadians((double)(rotationYaw + 90.0F))));
            event.setZ((double)moveForward * EntityUtil.getMaxSpeed() * Math.sin(Math.toRadians((double)(rotationYaw + 90.0F))) - (double)moveStrafe * EntityUtil.getMaxSpeed() * Math.cos(Math.toRadians((double)(rotationYaw + 90.0F))));
         }
      }

   }

   public static enum Mode {
      STRAFE,
      YPORT,
      INSTANT;
   }
}
