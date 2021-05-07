package cc.zip.charon.features.modules.misc;

import cc.zip.charon.Charon;
import cc.zip.charon.features.modules.Module;
import cc.zip.charon.features.setting.Setting;
import com.mojang.realmsclient.gui.ChatFormatting;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.scoreboard.ScorePlayerTeam;

public class ExtraTab extends Module {
   private static ExtraTab INSTANCE = new ExtraTab();
   public Setting<Integer> size = this.register(new Setting("Size", 250, 1, 1000));

   public ExtraTab() {
      super("ExtraTab", "Extends Tab.", Module.Category.MISC, false, false, false);
      this.setInstance();
   }

   public static String getPlayerName(NetworkPlayerInfo networkPlayerInfoIn) {
      String name = networkPlayerInfoIn.getDisplayName() != null ? networkPlayerInfoIn.getDisplayName().getFormattedText() : ScorePlayerTeam.formatPlayerName(networkPlayerInfoIn.getPlayerTeam(), networkPlayerInfoIn.getGameProfile().getName());
      return Charon.friendManager.isFriend(name) ? ChatFormatting.AQUA + name : name;
   }

   public static ExtraTab getINSTANCE() {
      if (INSTANCE == null) {
         INSTANCE = new ExtraTab();
      }

      return INSTANCE;
   }

   private void setInstance() {
      INSTANCE = this;
   }
}
