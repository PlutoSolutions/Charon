package cc.zip.charon.mixin;

import cc.zip.charon.Charon;
import java.util.Map;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;
import org.spongepowered.asm.launch.MixinBootstrap;
import org.spongepowered.asm.mixin.MixinEnvironment;
import org.spongepowered.asm.mixin.Mixins;

public class MixLoader implements IFMLLoadingPlugin {
   private static boolean isObfuscatedEnvironment = false;

   public MixLoader() {
      Charon.LOGGER.info("\n\nLoading mixins");
      MixinBootstrap.init();
      Mixins.addConfiguration("mixins.charonpw.json");
      MixinEnvironment.getDefaultEnvironment().setObfuscationContext("searge");
      Charon.LOGGER.info(MixinEnvironment.getDefaultEnvironment().getObfuscationContext());
   }

   public String[] getASMTransformerClass() {
      return new String[0];
   }

   public String getModContainerClass() {
      return null;
   }

   public String getSetupClass() {
      return null;
   }

   public void injectData(Map<String, Object> data) {
      isObfuscatedEnvironment = (Boolean)data.get("runtimeDeobfuscationEnabled");
   }

   public String getAccessTransformerClass() {
      return null;
   }
}
