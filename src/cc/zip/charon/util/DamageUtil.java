package cc.zip.charon.util;

import java.util.Iterator;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.init.MobEffects;
import net.minecraft.item.Item;
import net.minecraft.item.ItemPickaxe;
import net.minecraft.item.ItemSpade;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.util.CombatRules;
import net.minecraft.util.DamageSource;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.Explosion;

public class DamageUtil implements Util {
   public static int getRoundedDamage(ItemStack stack) {
      return (int)getDamageInPercent(stack);
   }

   public static float getDamageInPercent(ItemStack stack) {
      return (float)(getItemDamage(stack) / stack.getMaxDamage()) * 100.0F;
   }

   public static boolean isArmorLow(EntityPlayer player, int durability) {
      Iterator var2 = player.inventory.armorInventory.iterator();

      ItemStack piece;
      do {
         if (!var2.hasNext()) {
            return false;
         }

         piece = (ItemStack)var2.next();
         if (piece == null) {
            return true;
         }
      } while(getItemDamage(piece) >= durability);

      return true;
   }

   public static int getItemDamage(ItemStack stack) {
      return stack.getMaxDamage() - stack.getItemDamage();
   }

   public static boolean canTakeDamage(boolean suicide) {
      return !mc.player.capabilities.isCreativeMode && !suicide;
   }

   public static float calculateDamage(double posX, double posY, double posZ, Entity entity) {
      float doubleExplosionSize = 12.0F;
      double distancedsize = entity.getDistance(posX, posY, posZ) / (double)doubleExplosionSize;
      Vec3d vec3d = new Vec3d(posX, posY, posZ);
      double blockDensity = 0.0D;

      try {
         blockDensity = (double)entity.world.getBlockDensity(vec3d, entity.getEntityBoundingBox());
      } catch (Exception var18) {
      }

      double v = (1.0D - distancedsize) * blockDensity;
      float damage = (float)((int)((v * v + v) / 2.0D * 7.0D * (double)doubleExplosionSize + 1.0D));
      double finald = 1.0D;
      if (entity instanceof EntityLivingBase) {
         finald = (double)getBlastReduction((EntityLivingBase)entity, getDamageMultiplied(damage), new Explosion(mc.world, (Entity)null, posX, posY, posZ, 6.0F, false, true));
      }

      return (float)finald;
   }

   public static float getBlastReduction(EntityLivingBase entity, float damageI, Explosion explosion) {
      float damage;
      if (entity instanceof EntityPlayer) {
         EntityPlayer ep = (EntityPlayer)entity;
         DamageSource ds = DamageSource.causeExplosionDamage(explosion);
         damage = CombatRules.getDamageAfterAbsorb(damageI, (float)ep.getTotalArmorValue(), (float)ep.getEntityAttribute(SharedMonsterAttributes.ARMOR_TOUGHNESS).getAttributeValue());
         int k = 0;

         try {
            k = EnchantmentHelper.getEnchantmentModifierDamage(ep.getArmorInventoryList(), ds);
         } catch (Exception var8) {
         }

         float f = MathHelper.clamp((float)k, 0.0F, 20.0F);
         damage *= 1.0F - f / 25.0F;
         if (entity.isPotionActive(MobEffects.RESISTANCE)) {
            damage -= damage / 4.0F;
         }

         damage = Math.max(damage, 0.0F);
         return damage;
      } else {
         damage = CombatRules.getDamageAfterAbsorb(damageI, (float)entity.getTotalArmorValue(), (float)entity.getEntityAttribute(SharedMonsterAttributes.ARMOR_TOUGHNESS).getAttributeValue());
         return damage;
      }
   }

   public static float getDamageMultiplied(float damage) {
      int diff = mc.world.getDifficulty().getId();
      return damage * (diff == 0 ? 0.0F : (diff == 2 ? 1.0F : (diff == 1 ? 0.5F : 1.5F)));
   }

   public static float calculateDamage(BlockPos pos, Entity entity) {
      return calculateDamage((double)pos.getX() + 0.5D, (double)(pos.getY() + 1), (double)pos.getZ() + 0.5D, entity);
   }

   public static int getCooldownByWeapon(EntityPlayer player) {
      Item item = player.getHeldItemMainhand().getItem();
      if (item instanceof ItemSword) {
         return 600;
      } else if (item instanceof ItemPickaxe) {
         return 850;
      } else if (item == Items.IRON_AXE) {
         return 1100;
      } else if (item == Items.STONE_HOE) {
         return 500;
      } else if (item == Items.IRON_HOE) {
         return 350;
      } else if (item != Items.WOODEN_AXE && item != Items.STONE_AXE) {
         return !(item instanceof ItemSpade) && item != Items.GOLDEN_AXE && item != Items.DIAMOND_AXE && item != Items.WOODEN_HOE && item != Items.GOLDEN_HOE ? 250 : 1000;
      } else {
         return 1250;
      }
   }
}
