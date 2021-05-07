package cc.zip.charon.features.command.commands;

import cc.zip.charon.Charon;
import cc.zip.charon.features.command.Command;
import com.mojang.realmsclient.gui.ChatFormatting;
import java.util.Iterator;

public class HelpCommand extends Command {
   public HelpCommand() {
      super("help");
   }

   public void execute(String[] commands) {
      sendMessage("Commands: ");
      Iterator var2 = Charon.commandManager.getCommands().iterator();

      while(var2.hasNext()) {
         Command command = (Command)var2.next();
         sendMessage(ChatFormatting.GRAY + Charon.commandManager.getPrefix() + command.getName());
      }

   }
}
