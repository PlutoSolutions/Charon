package cc.zip.charon.manager;

import cc.zip.charon.features.Feature;
import com.mojang.realmsclient.gui.ChatFormatting;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.client.resources.I18n;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;

public class PotionManager extends Feature {
   private final Map<EntityPlayer, PotionManager.PotionList> potions = new ConcurrentHashMap();

   public List<PotionEffect> getOwnPotions() {
      return this.getPlayerPotions(mc.player);
   }

   public List<PotionEffect> getPlayerPotions(EntityPlayer player) {
      PotionManager.PotionList list = (PotionManager.PotionList)this.potions.get(player);
      List<PotionEffect> potions = new ArrayList();
      if (list != null) {
         potions = list.getEffects();
      }

      return (List)potions;
   }

   public PotionEffect[] getImportantPotions(EntityPlayer player) {
      PotionEffect[] array = new PotionEffect[3];
      Iterator var3 = this.getPlayerPotions(player).iterator();

      while(var3.hasNext()) {
         PotionEffect effect = (PotionEffect)var3.next();
         Potion potion = effect.getPotion();
         String var6 = I18n.format(potion.getName(), new Object[0]).toLowerCase();
         byte var7 = -1;
         switch(var6.hashCode()) {
         case -736186929:
            if (var6.equals("weakness")) {
               var7 = 1;
            }
            break;
         case 109641799:
            if (var6.equals("speed")) {
               var7 = 2;
            }
            break;
         case 1791316033:
            if (var6.equals("strength")) {
               var7 = 0;
            }
         }

         switch(var7) {
         case 0:
            array[0] = effect;
         case 1:
            array[1] = effect;
         case 2:
            array[2] = effect;
         }
      }

      return array;
   }

   public String getPotionString(PotionEffect effect) {
      Potion potion = effect.getPotion();
      return I18n.format(potion.getName(), new Object[0]) + " " + (effect.getAmplifier() + 1) + " " + ChatFormatting.WHITE + Potion.getPotionDurationString(effect, 1.0F);
   }

   public String getColoredPotionString(PotionEffect effect) {
      return this.getPotionString(effect);
   }

   public static class PotionList {
      private final List<PotionEffect> effects = new ArrayList();

      public void addEffect(PotionEffect effect) {
         if (effect != null) {
            this.effects.add(effect);
         }

      }

      public List<PotionEffect> getEffects() {
         return this.effects;
      }
   }
}
