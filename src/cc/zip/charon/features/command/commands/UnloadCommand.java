package cc.zip.charon.features.command.commands;

import cc.zip.charon.Charon;
import cc.zip.charon.features.command.Command;

public class UnloadCommand extends Command {
   public UnloadCommand() {
      super("unload", new String[0]);
   }

   public void execute(String[] commands) {
      Charon.unload(true);
   }
}
