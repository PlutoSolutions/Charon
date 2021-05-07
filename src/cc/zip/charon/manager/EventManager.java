package cc.zip.charon.manager;

import cc.zip.charon.Charon;
import cc.zip.charon.event.events.ConnectionEvent;
import cc.zip.charon.event.events.DeathEvent;
import cc.zip.charon.event.events.PacketEvent;
import cc.zip.charon.event.events.Render2DEvent;
import cc.zip.charon.event.events.Render3DEvent;
import cc.zip.charon.event.events.TotemPopEvent;
import cc.zip.charon.event.events.UpdateWalkingPlayerEvent;
import cc.zip.charon.features.Feature;
import cc.zip.charon.features.command.Command;
import cc.zip.charon.features.modules.client.HUD;
import cc.zip.charon.features.modules.misc.PopCounter;
import cc.zip.charon.util.Timer;
import com.google.common.base.Strings;
import com.mojang.realmsclient.gui.ChatFormatting;
import java.util.Iterator;
import java.util.Objects;
import java.util.UUID;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.play.server.SPacketEntityStatus;
import net.minecraft.network.play.server.SPacketPlayerListItem;
import net.minecraft.network.play.server.SPacketTimeUpdate;
import net.minecraft.network.play.server.SPacketPlayerListItem.Action;
import net.minecraftforge.client.event.ClientChatEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent.ElementType;
import net.minecraftforge.client.event.RenderGameOverlayEvent.Post;
import net.minecraftforge.client.event.RenderGameOverlayEvent.Text;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.InputEvent.KeyInputEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent.ClientTickEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent.ClientConnectedToServerEvent;
import net.minecraftforge.fml.common.network.FMLNetworkEvent.ClientDisconnectionFromServerEvent;
import org.lwjgl.input.Keyboard;

public class EventManager extends Feature {
   private final Timer logoutTimer = new Timer();

   public void init() {
      MinecraftForge.EVENT_BUS.register(this);
   }

   public void onUnload() {
      MinecraftForge.EVENT_BUS.unregister(this);
   }

   @SubscribeEvent
   public void onUpdate(LivingUpdateEvent event) {
      if (!fullNullCheck() && event.getEntity().getEntityWorld().isRemote && event.getEntityLiving().equals(mc.player)) {
         Charon.inventoryManager.update();
         Charon.moduleManager.onUpdate();
         if (HUD.getInstance().renderingMode.getValue() == HUD.RenderingMode.Length) {
            Charon.moduleManager.sortModules(true);
         } else {
            Charon.moduleManager.sortModulesABC();
         }
      }

   }

   @SubscribeEvent
   public void onClientConnect(ClientConnectedToServerEvent event) {
      this.logoutTimer.reset();
      Charon.moduleManager.onLogin();
   }

   @SubscribeEvent
   public void onClientDisconnect(ClientDisconnectionFromServerEvent event) {
      Charon.moduleManager.onLogout();
   }

   @SubscribeEvent
   public void onTick(ClientTickEvent event) {
      if (!fullNullCheck()) {
         Charon.moduleManager.onTick();
         Iterator var2 = mc.world.playerEntities.iterator();

         while(var2.hasNext()) {
            EntityPlayer player = (EntityPlayer)var2.next();
            if (player != null && !(player.getHealth() > 0.0F)) {
               MinecraftForge.EVENT_BUS.post(new DeathEvent(player));
               PopCounter.getInstance().onDeath(player);
            }
         }

      }
   }

   @SubscribeEvent
   public void onUpdateWalkingPlayer(UpdateWalkingPlayerEvent event) {
      if (!fullNullCheck()) {
         if (event.getStage() == 0) {
            Charon.speedManager.updateValues();
            Charon.rotationManager.updateRotations();
            Charon.positionManager.updatePosition();
         }

         if (event.getStage() == 1) {
            Charon.rotationManager.restoreRotations();
            Charon.positionManager.restorePosition();
         }

      }
   }

   @SubscribeEvent
   public void onPacketReceive(PacketEvent.Receive event) {
      if (event.getStage() == 0) {
         Charon.serverManager.onPacketReceived();
         if (event.getPacket() instanceof SPacketEntityStatus) {
            SPacketEntityStatus packet = (SPacketEntityStatus)event.getPacket();
            if (packet.getOpCode() == 35 && packet.getEntity(mc.world) instanceof EntityPlayer) {
               EntityPlayer player = (EntityPlayer)packet.getEntity(mc.world);
               MinecraftForge.EVENT_BUS.post(new TotemPopEvent(player));
               PopCounter.getInstance().onTotemPop(player);
            }
         }

         if (event.getPacket() instanceof SPacketPlayerListItem && !fullNullCheck() && this.logoutTimer.passedS(1.0D)) {
            SPacketPlayerListItem packet = (SPacketPlayerListItem)event.getPacket();
            if (!Action.ADD_PLAYER.equals(packet.getAction()) && !Action.REMOVE_PLAYER.equals(packet.getAction())) {
               return;
            }

            packet.getEntries().stream().filter(Objects::nonNull).filter((data) -> {
               return !Strings.isNullOrEmpty(data.getProfile().getName()) || data.getProfile().getId() != null;
            }).forEach((data) -> {
               UUID id = data.getProfile().getId();
               switch(packet.getAction()) {
               case ADD_PLAYER:
                  String name = data.getProfile().getName();
                  MinecraftForge.EVENT_BUS.post(new ConnectionEvent(0, id, name));
                  break;
               case REMOVE_PLAYER:
                  EntityPlayer entity = mc.world.getPlayerEntityByUUID(id);
                  if (entity != null) {
                     String logoutName = entity.getName();
                     MinecraftForge.EVENT_BUS.post(new ConnectionEvent(1, entity, id, logoutName));
                  } else {
                     MinecraftForge.EVENT_BUS.post(new ConnectionEvent(2, id, (String)null));
                  }
               }

            });
         }

         if (event.getPacket() instanceof SPacketTimeUpdate) {
            Charon.serverManager.update();
         }

      }
   }

   @SubscribeEvent
   public void onWorldRender(RenderWorldLastEvent event) {
      if (!event.isCanceled()) {
         mc.profiler.startSection("oyvey");
         GlStateManager.disableTexture2D();
         GlStateManager.enableBlend();
         GlStateManager.disableAlpha();
         GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
         GlStateManager.shadeModel(7425);
         GlStateManager.disableDepth();
         GlStateManager.glLineWidth(1.0F);
         Render3DEvent render3dEvent = new Render3DEvent(event.getPartialTicks());
         Charon.moduleManager.onRender3D(render3dEvent);
         GlStateManager.glLineWidth(1.0F);
         GlStateManager.shadeModel(7424);
         GlStateManager.disableBlend();
         GlStateManager.enableAlpha();
         GlStateManager.enableTexture2D();
         GlStateManager.enableDepth();
         GlStateManager.enableCull();
         GlStateManager.enableCull();
         GlStateManager.depthMask(true);
         GlStateManager.enableTexture2D();
         GlStateManager.enableBlend();
         GlStateManager.enableDepth();
         mc.profiler.endSection();
      }
   }

   @SubscribeEvent
   public void renderHUD(Post event) {
      if (event.getType() == ElementType.HOTBAR) {
         Charon.textManager.updateResolution();
      }

   }

   @SubscribeEvent(
      priority = EventPriority.LOW
   )
   public void onRenderGameOverlayEvent(Text event) {
      if (event.getType().equals(ElementType.TEXT)) {
         ScaledResolution resolution = new ScaledResolution(mc);
         Render2DEvent render2DEvent = new Render2DEvent(event.getPartialTicks(), resolution);
         Charon.moduleManager.onRender2D(render2DEvent);
         GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
      }

   }

   @SubscribeEvent(
      priority = EventPriority.NORMAL,
      receiveCanceled = true
   )
   public void onKeyInput(KeyInputEvent event) {
      if (Keyboard.getEventKeyState()) {
         Charon.moduleManager.onKeyPressed(Keyboard.getEventKey());
      }

   }

   @SubscribeEvent(
      priority = EventPriority.HIGHEST
   )
   public void onChatSent(ClientChatEvent event) {
      if (event.getMessage().startsWith(Command.getCommandPrefix())) {
         event.setCanceled(true);

         try {
            mc.ingameGUI.getChatGUI().addToSentMessages(event.getMessage());
            if (event.getMessage().length() > 1) {
               Charon.commandManager.executeCommand(event.getMessage().substring(Command.getCommandPrefix().length() - 1));
            } else {
               Command.sendMessage("Please enter a command.");
            }
         } catch (Exception var3) {
            var3.printStackTrace();
            Command.sendMessage(ChatFormatting.RED + "An error occurred while running this command. Check the log!");
         }
      }

   }
}
