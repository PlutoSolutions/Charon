package cc.zip.charon.mixin.mixins;

import cc.zip.charon.features.modules.misc.Tracker;
import cc.zip.charon.util.none.PushEvents;
import com.google.common.base.Predicate;
import java.util.List;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.world.World;
import net.minecraft.world.chunk.Chunk;
import net.minecraftforge.common.MinecraftForge;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({World.class})
public class MixinWorld {
   @Redirect(
      method = {"getEntitiesWithinAABB(Ljava/lang/Class;Lnet/minecraft/util/math/AxisAlignedBB;Lcom/google/common/base/Predicate;)Ljava/util/List;"},
      at = @At(
   value = "INVOKE",
   target = "Lnet/minecraft/world/chunk/Chunk;getEntitiesOfTypeWithinAABB(Ljava/lang/Class;Lnet/minecraft/util/math/AxisAlignedBB;Ljava/util/List;Lcom/google/common/base/Predicate;)V"
)
   )
   public <T extends Entity> void getEntitiesOfTypeWithinAABBHook(Chunk chunk, Class<? extends T> entityClass, AxisAlignedBB aabb, List<T> listToFill, Predicate<? super T> filter) {
      try {
         chunk.getEntitiesOfTypeWithinAABB(entityClass, aabb, listToFill, filter);
      } catch (Exception var7) {
      }

   }

   @Inject(
      method = {"onEntityAdded"},
      at = {@At("HEAD")}
   )
   private void onEntityAdded(Entity entityIn, CallbackInfo ci) {
      if (Tracker.getInstance().isOn()) {
         Tracker.getInstance().onSpawnEntity(entityIn);
      }

   }

   @Redirect(
      method = {"handleMaterialAcceleration"},
      at = @At(
   value = "INVOKE",
   target = "Lnet/minecraft/entity/Entity;isPushedByWater()Z"
)
   )
   public boolean isPushedbyWaterHook(Entity entity) {
      PushEvents event = new PushEvents(2, entity);
      MinecraftForge.EVENT_BUS.post(event);
      return entity.isPushedByWater() && !event.isCanceled();
   }
}
