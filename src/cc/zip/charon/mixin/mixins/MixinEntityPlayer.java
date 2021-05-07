package cc.zip.charon.mixin.mixins;

import cc.zip.charon.Charon;
import cc.zip.charon.features.modules.player.TpsSync;
import com.mojang.authlib.GameProfile;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin({EntityPlayer.class})
public abstract class MixinEntityPlayer extends EntityLivingBase {
   public MixinEntityPlayer(World worldIn, GameProfile gameProfileIn) {
      super(worldIn);
   }

   @Inject(
      method = {"getCooldownPeriod"},
      at = {@At("HEAD")},
      cancellable = true
   )
   private void getCooldownPeriodHook(CallbackInfoReturnable<Float> callbackInfoReturnable) {
      if (TpsSync.getInstance().isOn() && (Boolean)TpsSync.getInstance().attack.getValue()) {
         callbackInfoReturnable.setReturnValue((float)(1.0D / ((EntityPlayer)EntityPlayer.class.cast(this)).getEntityAttribute(SharedMonsterAttributes.ATTACK_SPEED).getBaseValue() * 20.0D * (double)Charon.serverManager.getTpsFactor()));
      }

   }
}
