package cc.zip.charon.features.modules.misc;

import cc.zip.charon.Charon;
import cc.zip.charon.features.command.Command;
import cc.zip.charon.features.modules.Module;
import com.mojang.realmsclient.gui.ChatFormatting;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.RayTraceResult.Type;
import org.lwjgl.input.Mouse;

public class MCF extends Module {
   private boolean clicked = false;

   public MCF() {
      super("MCF", "Middleclick Friends.", Module.Category.MISC, true, false, false);
   }

   public void onUpdate() {
      if (Mouse.isButtonDown(2)) {
         if (!this.clicked && mc.currentScreen == null) {
            this.onClick();
         }

         this.clicked = true;
      } else {
         this.clicked = false;
      }

   }

   private void onClick() {
      RayTraceResult result = mc.objectMouseOver;
      Entity entity;
      if (result != null && result.typeOfHit == Type.ENTITY && (entity = result.entityHit) instanceof EntityPlayer) {
         if (Charon.friendManager.isFriend(entity.getName())) {
            Charon.friendManager.removeFriend(entity.getName());
            Command.sendMessage(ChatFormatting.RED + entity.getName() + ChatFormatting.RED + " has been unfriended.");
         } else {
            Charon.friendManager.addFriend(entity.getName());
            Command.sendMessage(ChatFormatting.AQUA + entity.getName() + ChatFormatting.AQUA + " has been friended.");
         }
      }

      this.clicked = true;
   }
}
