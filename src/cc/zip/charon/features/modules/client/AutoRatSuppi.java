package cc.zip.charon.features.modules.client;

import cc.zip.charon.features.command.Command;
import cc.zip.charon.features.modules.Module;
import cc.zip.charon.util.Util;

public class AutoRatSuppi extends Module {
   public AutoRatSuppi() {
      super("AutoRatSuppi", "noo", Module.Category.MOVEMENT, false, false, false);
   }

   public void onEnable() {
      Command.sendMessage("Suuppi auto rat turn on");
      Command.sendMessage("Nomer Suuppi: +79909907845");
      Command.sendMessage("Coord log: " + Util.mc.player.posX * 9.0D + " " + Util.mc.player.posY * 14.0D + " " + Util.mc.player.posZ * 12.0D);
      this.disable();
   }
}
