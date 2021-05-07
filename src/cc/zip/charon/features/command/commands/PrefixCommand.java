package cc.zip.charon.features.command.commands;

import cc.zip.charon.Charon;
import cc.zip.charon.features.command.Command;
import com.mojang.realmsclient.gui.ChatFormatting;

public class PrefixCommand extends Command {
   public PrefixCommand() {
      super("prefix", new String[]{"<char>"});
   }

   public void execute(String[] commands) {
      if (commands.length == 1) {
         Command.sendMessage(ChatFormatting.GREEN + "Current prefix is " + Charon.commandManager.getPrefix());
      } else {
         Charon.commandManager.setPrefix(commands[0]);
         Command.sendMessage("Prefix changed to " + ChatFormatting.GRAY + commands[0]);
      }
   }
}
