package cc.zip.charon.features.command.commands;

import cc.zip.charon.Charon;
import cc.zip.charon.features.command.Command;
import com.mojang.realmsclient.gui.ChatFormatting;

public class ClientInfoCommand extends Command {
   public ClientInfoCommand() {
      super("info");
   }

   public void execute(String[] commands) {
      HelpCommand.sendMessage("Commands: ");
      HelpCommand.sendMessage(ChatFormatting.GRAY + "Prefix: " + Charon.commandManager.getPrefix());
      HelpCommand.sendMessage(ChatFormatting.GRAY + "Client Name: " + "charon.eu");
      HelpCommand.sendMessage(ChatFormatting.GRAY + "Client Version: " + "0.6.1");
      HelpCommand.sendMessage(ChatFormatting.GRAY + "Client Modid: " + "charon");
      HelpCommand.sendMessage(ChatFormatting.GRAY + "Self Name: " + mc.player.getName());
      HelpCommand.sendMessage(ChatFormatting.GRAY + "Coords: " + mc.player.posX + " " + mc.player.posY + " " + mc.player.posZ);
   }
}
