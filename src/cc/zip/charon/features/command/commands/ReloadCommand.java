package cc.zip.charon.features.command.commands;

import cc.zip.charon.Charon;
import cc.zip.charon.features.command.Command;

public class ReloadCommand extends Command {
   public ReloadCommand() {
      super("reload", new String[0]);
   }

   public void execute(String[] commands) {
      Charon.reload();
   }
}
