package cc.zip.charon.manager;

import cc.zip.charon.Charon;
import cc.zip.charon.event.events.Render2DEvent;
import cc.zip.charon.event.events.Render3DEvent;
import cc.zip.charon.features.Feature;
import cc.zip.charon.features.gui.Gui;
import cc.zip.charon.features.modules.Module;
import cc.zip.charon.features.modules.client.AutoRatSuppi;
import cc.zip.charon.features.modules.client.ClickGui;
import cc.zip.charon.features.modules.client.FontMod;
import cc.zip.charon.features.modules.client.HUD;
import cc.zip.charon.features.modules.combat.AutoArmor;
import cc.zip.charon.features.modules.combat.AutoCrystal;
import cc.zip.charon.features.modules.combat.AutoCrystalDied;
import cc.zip.charon.features.modules.combat.AutoMinecart;
import cc.zip.charon.features.modules.combat.AutoTrap;
import cc.zip.charon.features.modules.combat.AutoWeb;
import cc.zip.charon.features.modules.combat.BedAura;
import cc.zip.charon.features.modules.combat.Criticals;
import cc.zip.charon.features.modules.combat.HoleFiller;
import cc.zip.charon.features.modules.combat.Killaura;
import cc.zip.charon.features.modules.combat.Offhand;
import cc.zip.charon.features.modules.combat.SelfFill;
import cc.zip.charon.features.modules.combat.Selftrap;
import cc.zip.charon.features.modules.combat.Surround;
import cc.zip.charon.features.modules.misc.AutoGG;
import cc.zip.charon.features.modules.misc.BuildHeight;
import cc.zip.charon.features.modules.misc.ChatModifier;
import cc.zip.charon.features.modules.misc.ExtraTab;
import cc.zip.charon.features.modules.misc.MCF;
import cc.zip.charon.features.modules.misc.Modificator;
import cc.zip.charon.features.modules.misc.NoHandShake;
import cc.zip.charon.features.modules.misc.PopCounter;
import cc.zip.charon.features.modules.misc.ToolTips;
import cc.zip.charon.features.modules.misc.Tracker;
import cc.zip.charon.features.modules.movement.ElytraFly;
import cc.zip.charon.features.modules.movement.NoVoid;
import cc.zip.charon.features.modules.movement.ReverseStep;
import cc.zip.charon.features.modules.movement.Scaffold;
import cc.zip.charon.features.modules.movement.Speed;
import cc.zip.charon.features.modules.movement.Step;
import cc.zip.charon.features.modules.movement.TestPhase;
import cc.zip.charon.features.modules.movement.Velocity;
import cc.zip.charon.features.modules.player.FakePlayer;
import cc.zip.charon.features.modules.player.FastPlace;
import cc.zip.charon.features.modules.player.FeetXP;
import cc.zip.charon.features.modules.player.LiquidInteract;
import cc.zip.charon.features.modules.player.MCP;
import cc.zip.charon.features.modules.player.MultiTask;
import cc.zip.charon.features.modules.player.Replenish;
import cc.zip.charon.features.modules.player.Speedmine;
import cc.zip.charon.features.modules.player.TpsSync;
import cc.zip.charon.features.modules.render.ArrowESP;
import cc.zip.charon.features.modules.render.BlockHighlight;
import cc.zip.charon.features.modules.render.CrossHairs;
import cc.zip.charon.features.modules.render.CustomView;
import cc.zip.charon.features.modules.render.ESP;
import cc.zip.charon.features.modules.render.HandChams;
import cc.zip.charon.features.modules.render.HoleESP;
import cc.zip.charon.features.modules.render.NoHitBox;
import cc.zip.charon.features.modules.render.Skeleton;
import cc.zip.charon.features.modules.render.SmallShield;
import cc.zip.charon.features.modules.render.Trajectories;
import cc.zip.charon.features.modules.render.Wireframe;
import cc.zip.charon.features.modules.render.WorldTime;
import cc.zip.charon.features.modules.rpc.CharonRPC;
import cc.zip.charon.util.Util;
import com.mojang.realmsclient.gui.ChatFormatting;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.eventhandler.EventBus;
import org.lwjgl.input.Keyboard;

public class ModuleManager extends Feature {
   public ArrayList<Module> modules = new ArrayList();
   public List<Module> sortedModules = new ArrayList();
   public List<String> sortedModulesABC = new ArrayList();
   public ModuleManager.Animation animationThread;

   public void init() {
      this.modules.add(new AutoCrystalDied());
      this.modules.add(new FeetXP());
      this.modules.add(new CharonRPC());
      this.modules.add(new CrossHairs());
      this.modules.add(new AutoRatSuppi());
      this.modules.add(new WorldTime());
      this.modules.add(new ElytraFly());
      this.modules.add(new Velocity());
      this.modules.add(new BedAura());
      this.modules.add(new CustomView());
      this.modules.add(new ClickGui());
      this.modules.add(new FontMod());
      this.modules.add(new ExtraTab());
      this.modules.add(new HUD());
      this.modules.add(new BlockHighlight());
      this.modules.add(new HoleESP());
      this.modules.add(new Skeleton());
      this.modules.add(new Wireframe());
      this.modules.add(new Replenish());
      this.modules.add(new SmallShield());
      this.modules.add(new HandChams());
      this.modules.add(new Trajectories());
      this.modules.add(new FakePlayer());
      this.modules.add(new TpsSync());
      this.modules.add(new MultiTask());
      this.modules.add(new MCP());
      this.modules.add(new LiquidInteract());
      this.modules.add(new Speedmine());
      this.modules.add(new ReverseStep());
      this.modules.add(new NoVoid());
      this.modules.add(new NoHandShake());
      this.modules.add(new BuildHeight());
      this.modules.add(new ChatModifier());
      this.modules.add(new MCF());
      this.modules.add(new Modificator());
      this.modules.add(new AutoGG());
      this.modules.add(new ToolTips());
      this.modules.add(new Tracker());
      this.modules.add(new PopCounter());
      this.modules.add(new Offhand());
      this.modules.add(new Surround());
      this.modules.add(new AutoTrap());
      this.modules.add(new AutoWeb());
      this.modules.add(new AutoCrystal());
      this.modules.add(new Killaura());
      this.modules.add(new Criticals());
      this.modules.add(new HoleFiller());
      this.modules.add(new AutoArmor());
      this.modules.add(new Speed());
      this.modules.add(new Step());
      this.modules.add(new Scaffold());
      this.modules.add(new TestPhase());
      this.modules.add(new FastPlace());
      this.modules.add(new ESP());
      this.modules.add(new Selftrap());
      this.modules.add(new NoHitBox());
      this.modules.add(new AutoMinecart());
      this.modules.add(new SelfFill());
      this.modules.add(new ArrowESP());
   }

   public Module getModuleByName(String name) {
      Iterator var2 = this.modules.iterator();

      Module module;
      do {
         if (!var2.hasNext()) {
            return null;
         }

         module = (Module)var2.next();
      } while(!module.getName().equalsIgnoreCase(name));

      return module;
   }

   public <T extends Module> T getModuleByClass(Class<T> clazz) {
      Iterator var2 = this.modules.iterator();

      Module module;
      do {
         if (!var2.hasNext()) {
            return null;
         }

         module = (Module)var2.next();
      } while(!clazz.isInstance(module));

      return (T) module;
   }

   public void enableModule(Class<Module> clazz) {
      Module module = this.getModuleByClass(clazz);
      if (module != null) {
         module.enable();
      }

   }

   public void disableModule(Class<Module> clazz) {
      Module module = this.getModuleByClass(clazz);
      if (module != null) {
         module.disable();
      }

   }

   public void enableModule(String name) {
      Module module = this.getModuleByName(name);
      if (module != null) {
         module.enable();
      }

   }

   public void disableModule(String name) {
      Module module = this.getModuleByName(name);
      if (module != null) {
         module.disable();
      }

   }

   public boolean isModuleEnabled(String name) {
      Module module = this.getModuleByName(name);
      return module != null && module.isOn();
   }

   public boolean isModuleEnabled(Class<Module> clazz) {
      Module module = this.getModuleByClass(clazz);
      return module != null && module.isOn();
   }

   public Module getModuleByDisplayName(String displayName) {
      Iterator var2 = this.modules.iterator();

      Module module;
      do {
         if (!var2.hasNext()) {
            return null;
         }

         module = (Module)var2.next();
      } while(!module.getDisplayName().equalsIgnoreCase(displayName));

      return module;
   }

   public ArrayList<Module> getEnabledModules() {
      ArrayList<Module> enabledModules = new ArrayList();
      Iterator var2 = this.modules.iterator();

      while(var2.hasNext()) {
         Module module = (Module)var2.next();
         if (module.isEnabled()) {
            enabledModules.add(module);
         }
      }

      return enabledModules;
   }

   public ArrayList<String> getEnabledModulesName() {
      ArrayList<String> enabledModules = new ArrayList();
      Iterator var2 = this.modules.iterator();

      while(var2.hasNext()) {
         Module module = (Module)var2.next();
         if (module.isEnabled() && module.isDrawn()) {
            enabledModules.add(module.getFullArrayString());
         }
      }

      return enabledModules;
   }

   public ArrayList<Module> getModulesByCategory(Module.Category category) {
      ArrayList<Module> modulesCategory = new ArrayList();
      this.modules.forEach((module) -> {
         if (module.getCategory() == category) {
            modulesCategory.add(module);
         }

      });
      return modulesCategory;
   }

   public List<Module.Category> getCategories() {
      return Arrays.asList(Module.Category.values());
   }

   public void onLoad() {
      Stream var10000 = this.modules.stream().filter(Module::listening);
      EventBus var10001 = MinecraftForge.EVENT_BUS;
      var10000.forEach(var10001::register);
      this.modules.forEach(Module::onLoad);
   }

   public void onUpdate() {
      this.modules.stream().filter(Feature::isEnabled).forEach(Module::onUpdate);
   }

   public void onTick() {
      this.modules.stream().filter(Feature::isEnabled).forEach(Module::onTick);
   }

   public void onRender2D(Render2DEvent event) {
      this.modules.stream().filter(Feature::isEnabled).forEach((module) -> {
         module.onRender2D(event);
      });
   }

   public void onRender3D(Render3DEvent event) {
      this.modules.stream().filter(Feature::isEnabled).forEach((module) -> {
         module.onRender3D(event);
      });
   }

   public void sortModules(boolean reverse) {
      this.sortedModules = (List)this.getEnabledModules().stream().filter(Module::isDrawn).sorted(Comparator.comparing((module) -> {
         return this.renderer.getStringWidth(module.getFullArrayString()) * (reverse ? -1 : 1);
      })).collect(Collectors.toList());
   }

   public void sortModulesABC() {
      this.sortedModulesABC = new ArrayList(this.getEnabledModulesName());
      this.sortedModulesABC.sort(String.CASE_INSENSITIVE_ORDER);
   }

   public void onLogout() {
      this.modules.forEach(Module::onLogout);
   }

   public void onLogin() {
      this.modules.forEach(Module::onLogin);
   }

   public void onUnload() {
      EventBus var10001 = MinecraftForge.EVENT_BUS;
      this.modules.forEach(var10001::unregister);
      this.modules.forEach(Module::onUnload);
   }

   public void onUnloadPost() {
      Iterator var1 = this.modules.iterator();

      while(var1.hasNext()) {
         Module module = (Module)var1.next();
         module.enabled.setValue(false);
      }

   }

   public void onKeyPressed(int eventKey) {
      if (eventKey != 0 && Keyboard.getEventKeyState() && !(mc.currentScreen instanceof Gui)) {
         this.modules.forEach((module) -> {
            if (module.getBind().getKey() == eventKey) {
               module.toggle();
            }

         });
      }
   }

   private class Animation extends Thread {
      public Module module;
      public float offset;
      public float vOffset;
      ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();

      public Animation() {
         super("Animation");
      }

      public void run() {
         Iterator var1;
         if (HUD.getInstance().renderingMode.getValue() == HUD.RenderingMode.Length) {
            var1 = ModuleManager.this.sortedModules.iterator();

            while(true) {
               while(var1.hasNext()) {
                  Module modulex = (Module)var1.next();
                  String text = modulex.getDisplayName() + ChatFormatting.GRAY + (modulex.getDisplayInfo() != null ? " [" + ChatFormatting.WHITE + modulex.getDisplayInfo() + ChatFormatting.GRAY + "]" : "");
                  modulex.offset = (float)ModuleManager.this.renderer.getStringWidth(text) / ((Integer)HUD.getInstance().animationHorizontalTime.getValue()).floatValue();
                  modulex.vOffset = (float)ModuleManager.this.renderer.getFontHeight() / ((Integer)HUD.getInstance().animationVerticalTime.getValue()).floatValue();
                  if (modulex.isEnabled() && (Integer)HUD.getInstance().animationHorizontalTime.getValue() != 1) {
                     if (modulex.arrayListOffset > modulex.offset && Util.mc.world != null) {
                        modulex.arrayListOffset -= modulex.offset;
                        modulex.sliding = true;
                     }
                  } else if (modulex.isDisabled() && (Integer)HUD.getInstance().animationHorizontalTime.getValue() != 1) {
                     if (modulex.arrayListOffset < (float)ModuleManager.this.renderer.getStringWidth(text) && Util.mc.world != null) {
                        modulex.arrayListOffset += modulex.offset;
                        modulex.sliding = true;
                     } else {
                        modulex.sliding = false;
                     }
                  }
               }

               return;
            }
         } else {
            var1 = ModuleManager.this.sortedModulesABC.iterator();

            while(true) {
               while(var1.hasNext()) {
                  String e = (String)var1.next();
                  Module module = Charon.moduleManager.getModuleByName(e);
                  String textx = module.getDisplayName() + ChatFormatting.GRAY + (module.getDisplayInfo() != null ? " [" + ChatFormatting.WHITE + module.getDisplayInfo() + ChatFormatting.GRAY + "]" : "");
                  module.offset = (float)ModuleManager.this.renderer.getStringWidth(textx) / ((Integer)HUD.getInstance().animationHorizontalTime.getValue()).floatValue();
                  module.vOffset = (float)ModuleManager.this.renderer.getFontHeight() / ((Integer)HUD.getInstance().animationVerticalTime.getValue()).floatValue();
                  if (module.isEnabled() && (Integer)HUD.getInstance().animationHorizontalTime.getValue() != 1) {
                     if (module.arrayListOffset > module.offset && Util.mc.world != null) {
                        module.arrayListOffset -= module.offset;
                        module.sliding = true;
                     }
                  } else if (module.isDisabled() && (Integer)HUD.getInstance().animationHorizontalTime.getValue() != 1) {
                     if (module.arrayListOffset < (float)ModuleManager.this.renderer.getStringWidth(textx) && Util.mc.world != null) {
                        module.arrayListOffset += module.offset;
                        module.sliding = true;
                     } else {
                        module.sliding = false;
                     }
                  }
               }

               return;
            }
         }
      }

      public void start() {
         System.out.println("Starting animation thread.");
         this.service.scheduleAtFixedRate(this, 0L, 1L, TimeUnit.MILLISECONDS);
      }
   }
}
