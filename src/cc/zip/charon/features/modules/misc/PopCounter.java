package cc.zip.charon.features.modules.misc;

import cc.zip.charon.features.command.Command;
import cc.zip.charon.features.modules.Module;
import com.mojang.realmsclient.gui.ChatFormatting;
import java.util.HashMap;
import net.minecraft.entity.player.EntityPlayer;

public class PopCounter extends Module {
   public static HashMap<String, Integer> TotemPopContainer = new HashMap();
   private static PopCounter INSTANCE = new PopCounter();

   public PopCounter() {
      super("PopNotify", "Counts other players totem pops.", Module.Category.MISC, true, false, false);
      this.setInstance();
   }

   public static PopCounter getInstance() {
      if (INSTANCE == null) {
         INSTANCE = new PopCounter();
      }

      return INSTANCE;
   }

   private void setInstance() {
      INSTANCE = this;
   }

   public void onEnable() {
      TotemPopContainer.clear();
   }

   public void onDeath(EntityPlayer player) {
      if (TotemPopContainer.containsKey(player.getName())) {
         int l_Count = (Integer)TotemPopContainer.get(player.getName());
         TotemPopContainer.remove(player.getName());
         if (l_Count == 1) {
            Command.sendMessage(ChatFormatting.WHITE + player.getName() + ChatFormatting.GRAY + " died after popping " + ChatFormatting.WHITE + l_Count + ChatFormatting.GRAY + " Totem! Charon.eu :)");
         } else {
            Command.sendMessage(ChatFormatting.WHITE + player.getName() + ChatFormatting.GRAY + " died after popping " + ChatFormatting.WHITE + l_Count + ChatFormatting.GRAY + " Totems! Charon.eu :>");
         }
      }

   }

   public void onTotemPop(EntityPlayer player) {
      if (!fullNullCheck()) {
         if (!mc.player.equals(player)) {
            int l_Count = 1;
            if (TotemPopContainer.containsKey(player.getName())) {
               l_Count = (Integer)TotemPopContainer.get(player.getName());
               HashMap var10000 = TotemPopContainer;
               String var10001 = player.getName();
               ++l_Count;
               var10000.put(var10001, l_Count);
            } else {
               TotemPopContainer.put(player.getName(), l_Count);
            }

            if (l_Count == 1) {
               Command.sendMessage(ChatFormatting.WHITE + player.getName() + ChatFormatting.GRAY + " popped " + ChatFormatting.WHITE + l_Count + ChatFormatting.GRAY + " Totem. Charon.eu :s");
            } else {
               Command.sendMessage(ChatFormatting.WHITE + player.getName() + ChatFormatting.GRAY + " popped " + ChatFormatting.WHITE + l_Count + ChatFormatting.GRAY + " Totems. Charon.eu :p");
            }

         }
      }
   }
}
