package cc.zip.charon.mixin.mixins;

import cc.zip.charon.event.events.ChatEvent;
import cc.zip.charon.event.events.PushEvent;
import cc.zip.charon.event.events.UpdateWalkingPlayerEvent;
import cc.zip.charon.util.none.PlayerMoveEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.entity.MoverType;
import net.minecraft.stats.RecipeBook;
import net.minecraft.stats.StatisticsManager;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(
   value = {EntityPlayerSP.class},
   priority = 9998
)
public abstract class MixinEntityPlayerSP extends AbstractClientPlayer {
   public MixinEntityPlayerSP(Minecraft p_i47378_1_, World p_i47378_2_, NetHandlerPlayClient p_i47378_3_, StatisticsManager p_i47378_4_, RecipeBook p_i47378_5_) {
      super(p_i47378_2_, p_i47378_3_.getGameProfile());
   }

   @Inject(
      method = {"sendChatMessage"},
      at = {@At("HEAD")},
      cancellable = true
   )
   public void sendChatMessage(String message, CallbackInfo callback) {
      ChatEvent chatEvent = new ChatEvent(message);
      MinecraftForge.EVENT_BUS.post(chatEvent);
   }

   @Redirect(
      method = {"move"},
      at = @At(
   value = "INVOKE",
   target = "Lnet/minecraft/client/entity/AbstractClientPlayer;move(Lnet/minecraft/entity/MoverType;DDD)V"
)
   )
   public void move(AbstractClientPlayer player, MoverType type, double x, double y, double z) {
      PlayerMoveEvents moveEvent = new PlayerMoveEvents(type, x, y, z);
      super.move(type, moveEvent.x, moveEvent.y, moveEvent.z);
   }

   @Inject(
      method = {"onUpdateWalkingPlayer"},
      at = {@At("HEAD")}
   )
   private void preMotion(CallbackInfo info) {
      UpdateWalkingPlayerEvent event = new UpdateWalkingPlayerEvent(0);
      MinecraftForge.EVENT_BUS.post(event);
   }

   @Inject(
      method = {"onUpdateWalkingPlayer"},
      at = {@At("RETURN")}
   )
   private void postMotion(CallbackInfo info) {
      UpdateWalkingPlayerEvent event = new UpdateWalkingPlayerEvent(1);
      MinecraftForge.EVENT_BUS.post(event);
   }

   @Inject(
      method = {"pushOutOfBlocks"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void pushOutOfBlocksHook(double x, double y, double z, CallbackInfoReturnable<Boolean> info) {
      PushEvent event = new PushEvent(1);
      MinecraftForge.EVENT_BUS.post(event);
      if (event.isCanceled()) {
         info.setReturnValue(false);
      }

   }
}
