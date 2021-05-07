package cc.zip.charon.discordutil;

import cc.zip.charon.features.modules.rpc.CharonRPC;
import club.minnced.discord.rpc.DiscordEventHandlers;
import club.minnced.discord.rpc.DiscordRPC;
import club.minnced.discord.rpc.DiscordRichPresence;

public class CharonRPCbig {
   private static final DiscordRPC rpc;
   public static DiscordRichPresence presence;
   private static Thread thread;
   private static int index = 1;

   public static void start() {
      DiscordEventHandlers handlers = new DiscordEventHandlers();
      rpc.Discord_Initialize("818218548588838962", handlers, true, "");
      presence.startTimestamp = System.currentTimeMillis() / 1000L;
      presence.state = (String)CharonRPC.INSTANCE.state.getValue();
      presence.largeImageKey = "charon";
      presence.largeImageText = "charon-beta 0.6.1";
      rpc.Discord_UpdatePresence(presence);
      thread = new Thread(() -> {
         while(!Thread.currentThread().isInterrupted()) {
            rpc.Discord_RunCallbacks();
            if ((Boolean)CharonRPC.INSTANCE.random.getValue()) {
               if (index == 4) {
                  index = 1;
               }

               presence.largeImageKey = "charon" + index;
               ++index;
            }

            rpc.Discord_UpdatePresence(presence);

            try {
               Thread.sleep(3000L);
            } catch (InterruptedException var1) {
            }
         }

      }, "RPC-Callback-Handler");
      thread.start();
   }

   public static void stop() {
      if (thread != null && !thread.isInterrupted()) {
         thread.interrupt();
      }

      rpc.Discord_Shutdown();
   }

   static {
      rpc = DiscordRPC.INSTANCE;
      presence = new DiscordRichPresence();
   }
}
