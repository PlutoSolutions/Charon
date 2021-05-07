package cc.zip.charon.util.none;

public class WorldTimer {
   private float OverrideSpeed = 1.0F;

   private void useTimer() {
      if (this.OverrideSpeed != 1.0F && this.OverrideSpeed > 0.1F) {
         Wrapper.mc.timer.tickLength = 50.0F / this.OverrideSpeed;
      }

   }

   public void SetOverrideSpeed(float f) {
      this.OverrideSpeed = f;
      this.useTimer();
   }

   public void resetTime() {
      Wrapper.mc.timer.tickLength = 50.0F;
   }
}
