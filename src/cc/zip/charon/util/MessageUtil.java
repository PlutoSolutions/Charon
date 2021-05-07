package cc.zip.charon.util;

import cc.zip.charon.mixin.mixins.MixinInterface;
import net.minecraft.util.text.TextComponentString;
import net.minecraft.util.text.TextFormatting;

public class MessageUtil implements MixinInterface {
   public static void sendClientMessage(String message) {
      mc.ingameGUI.getChatGUI().printChatMessageWithOptionalDeletion(new TextComponentString(TextFormatting.DARK_PURPLE + "[Momentum] " + TextFormatting.RESET + message), 69);
   }

   public static void sendRawClientMessage(String message) {
      mc.ingameGUI.getChatGUI().printChatMessageWithOptionalDeletion(new TextComponentString(message), 69);
   }

   public static void sendPublicMessage(String message) {
      mc.player.sendChatMessage(message);
   }

   public static void addOutput(String message) {
   }

   public static void usageException(String usage, String specialUsage) {
   }

   public static String toUnicode(String s) {
      return s.toLowerCase().replace("a", "ᴀ").replace("b", "ʙ").replace("c", "ᴄ").replace("d", "ᴅ").replace("e", "ᴇ").replace("f", "ꜰ").replace("g", "ɢ").replace("h", "ʜ").replace("i", "ɪ").replace("j", "ᴊ").replace("k", "ᴋ").replace("l", "ʟ").replace("m", "ᴍ").replace("n", "ɴ").replace("o", "ᴏ").replace("p", "ᴘ").replace("q", "ǫ").replace("r", "ʀ").replace("s", "ꜱ").replace("t", "ᴛ").replace("u", "ᴜ").replace("v", "ᴠ").replace("w", "ᴡ").replace("x", "ˣ").replace("y", "ʏ").replace("z", "ᴢ");
   }
}
