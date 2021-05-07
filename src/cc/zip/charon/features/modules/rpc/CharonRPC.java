package cc.zip.charon.features.modules.rpc;

import cc.zip.charon.discordutil.CharonRPCbig;
import cc.zip.charon.features.modules.Module;
import cc.zip.charon.features.setting.Setting;

public class CharonRPC extends Module {
   public static CharonRPC INSTANCE;
   public Setting<String> state;
   public Setting<Boolean> random;

   public CharonRPC() {
      super("CharonRPC", "Discord rich presence", Module.Category.MISC, false, false, false);
      INSTANCE = this;
      this.state = this.register(new Setting("State", "russian-elite", "Sets the state of the DiscordRPC."));
      this.random = this.register(new Setting("Random", true));
   }

   public void onEnable() {
      CharonRPCbig.start();
   }

   public void onDisable() {
      CharonRPCbig.stop();
   }
}
