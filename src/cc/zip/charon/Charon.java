package cc.zip.charon;

import cc.zip.charon.manager.ColorManager;
import cc.zip.charon.manager.CommandManager;
import cc.zip.charon.manager.ConfigManager;
import cc.zip.charon.manager.EventManager;
import cc.zip.charon.manager.FileManager;
import cc.zip.charon.manager.FriendManager;
import cc.zip.charon.manager.HoleManager;
import cc.zip.charon.manager.InventoryManager;
import cc.zip.charon.manager.ModuleManager;
import cc.zip.charon.manager.PacketManager;
import cc.zip.charon.manager.PositionManager;
import cc.zip.charon.manager.PotionManager;
import cc.zip.charon.manager.ReloadManager;
import cc.zip.charon.manager.RotationManager;
import cc.zip.charon.manager.ServerManager;
import cc.zip.charon.manager.SpeedManager;
import cc.zip.charon.manager.TextManager;
import cc.zip.charon.manager.TimerManager;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.Mod.Instance;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.opengl.Display;

@Mod(
   modid = "charon",
   name = "charon.eu",
   version = "0.6"
)
public class Charon {
   public static final String MODID = "charon";
   public static final String MODNAME = "charon.eu";
   public static final String MODVER = "0.6.1";
   public static final Logger LOGGER = LogManager.getLogger("charon.eu");
   public static CommandManager commandManager;
   public static FriendManager friendManager;
   public static ModuleManager moduleManager;
   public static PacketManager packetManager;
   public static ColorManager colorManager;
   public static HoleManager holeManager;
   public static InventoryManager inventoryManager;
   public static PotionManager potionManager;
   public static RotationManager rotationManager;
   public static PositionManager positionManager;
   public static SpeedManager speedManager;
   public static ReloadManager reloadManager;
   public static FileManager fileManager;
   public static ConfigManager configManager;
   public static ServerManager serverManager;
   public static EventManager eventManager;
   public static TextManager textManager;
   public static TimerManager timerManager;
   @Instance
   public static Charon INSTANCE;
   private static boolean unloaded = false;

   public static void load() {
      LOGGER.info("\n\nLoading charon.eu");
      LOGGER.info("\n\nUnloading charon.eu");
      LOGGER.info("\n\nUnloading charon.eu");
      unloaded = false;
      if (reloadManager != null) {
         reloadManager.unload();
         reloadManager = null;
      }

      textManager = new TextManager();
      commandManager = new CommandManager();
      friendManager = new FriendManager();
      moduleManager = new ModuleManager();
      rotationManager = new RotationManager();
      packetManager = new PacketManager();
      eventManager = new EventManager();
      speedManager = new SpeedManager();
      potionManager = new PotionManager();
      inventoryManager = new InventoryManager();
      serverManager = new ServerManager();
      fileManager = new FileManager();
      colorManager = new ColorManager();
      positionManager = new PositionManager();
      configManager = new ConfigManager();
      holeManager = new HoleManager();
      LOGGER.info("Managers loaded.");
      moduleManager.init();
      LOGGER.info("Modules loaded.");
      configManager.init();
      eventManager.init();
      LOGGER.info("EventManager loaded.");
      textManager.init(true);
      moduleManager.onLoad();
      LOGGER.info("charon.eu successfully loaded!\n");
   }

   public static void unload(boolean unload) {
      LOGGER.info("\n\nUnloading charon.eu");
      if (unload) {
         reloadManager = new ReloadManager();
         reloadManager.init(commandManager != null ? commandManager.getPrefix() : ".");
      }

      onUnload();
      eventManager = null;
      friendManager = null;
      speedManager = null;
      holeManager = null;
      positionManager = null;
      rotationManager = null;
      configManager = null;
      commandManager = null;
      colorManager = null;
      serverManager = null;
      fileManager = null;
      potionManager = null;
      inventoryManager = null;
      moduleManager = null;
      textManager = null;
      LOGGER.info("charon.eu unloaded!\n");
   }

   public static void reload() {
      unload(false);
      load();
   }

   public static void onUnload() {
      if (!unloaded) {
         eventManager.onUnload();
         moduleManager.onUnload();
         configManager.saveConfig(configManager.config.replaceFirst("charon/", ""));
         moduleManager.onUnloadPost();
         unloaded = true;
      }

   }

   @EventHandler
   public void init(FMLInitializationEvent event) {
      Display.setTitle("team cattyn owns u");
      load();
   }
}
