package cc.zip.charon.features.modules.player;

import cc.zip.charon.features.command.Command;
import cc.zip.charon.features.modules.Module;
import cc.zip.charon.features.setting.Setting;
import cc.zip.charon.util.WorldUtil;
import net.minecraft.client.entity.EntityOtherPlayerMP;

public class FakePlayer extends Module {
   private EntityOtherPlayerMP _fakePlayer;
   private EntityOtherPlayerMP fakeplayers;
   public Setting<String> name = this.register(new Setting("name", "CHARON.EU"));
   public Setting<Boolean> inventory = this.register(new Setting("Copy Inventory", true));
   public Setting<Boolean> angles = this.register(new Setting("Copy Angles", true));

   public FakePlayer() {
      super("FakePlayer", "Spawns a FakePlayer for testing", Module.Category.PLAYER, false, false, false);
   }

   public void onUpdate() {
      if (mc.world == null) {
         this.disable();
      }

   }

   public void onEnable() {
      if (!nullCheck()) {
         WorldUtil.createFakePlayer((String)this.name.getValue(), (Boolean)this.inventory.getValue(), (Boolean)this.angles.getValue(), true, false, -6640);
         Command.sendMessage("done");
      }
   }

   public void onDisable() {
      mc.world.removeEntityFromWorld(-6640);
   }
}
