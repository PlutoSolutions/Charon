package cc.zip.charon.util;

import cc.zip.charon.event.events.PacketEvent;
import cc.zip.charon.features.modules.Module;
import cc.zip.charon.features.setting.Setting;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import net.minecraft.network.play.client.CPacketChatMessage;
import net.minecraft.network.play.client.CPacketKeepAlive;
import net.minecraft.network.play.server.SPacketChat;
import net.minecraft.network.play.server.SPacketKeepAlive;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class ServerUtil extends Module {
   public Setting<String> ip = this.register(new Setting("PhobosIP", "0.0.0.0.0"));
   public Setting<String> serverIP = this.register(new Setting("ServerIP", "AnarchyHvH.eu"));
   public Setting<Boolean> noFML = this.register(new Setting("RemoveFML", false));
   public Setting<Boolean> getName = this.register(new Setting("GetName", false));
   public Setting<Boolean> average = this.register(new Setting("Average", false));
   public Setting<Boolean> clear = this.register(new Setting("ClearPings", false));
   public Setting<Boolean> oneWay = this.register(new Setting("OneWay", false));
   public Setting<Integer> delay = this.register(new Setting("KeepAlives", 10, 1, 50));
   private static ServerUtil instance;
   private final AtomicBoolean connected = new AtomicBoolean(false);
   private final Timer pingTimer = new Timer();
   private long currentPing = 0L;
   private long serverPing = 0L;
   private StringBuffer name = null;
   private long averagePing = 0L;
   private final List<Long> pingList = new ArrayList();
   private String serverPrefix = "idk";

   public ServerUtil() {
      super("PingBypass", "Manages Phobos`s internal Server", Module.Category.CLIENT, false, false, true);
      instance = this;
   }

   public String getPlayerName() {
      return this.name == null ? null : this.name.toString();
   }

   public String getServerPrefix() {
      return this.serverPrefix;
   }

   public static ServerUtil getInstance() {
      if (instance == null) {
         instance = new ServerUtil();
      }

      return instance;
   }

   public void onLogout() {
      this.averagePing = 0L;
      this.currentPing = 0L;
      this.serverPing = 0L;
      this.pingList.clear();
      this.connected.set(false);
      this.name = null;
   }

   @SubscribeEvent
   public void onReceivePacket(PacketEvent.Receive event) {
      if (event.getPacket() instanceof SPacketChat) {
         SPacketChat packet = (SPacketChat)event.getPacket();
         if (packet.chatComponent.getUnformattedText().startsWith("@Clientprefix")) {
            this.serverPrefix = packet.chatComponent.getFormattedText().replace("@Clientprefix", "");
         }
      }

   }

   public void onTick() {
      if (mc.getConnection() != null && this.isConnected()) {
         if ((Boolean)this.getName.getValue()) {
            mc.getConnection().sendPacket(new CPacketChatMessage("@Servername"));
            this.getName.setValue(false);
         }

         if (this.serverPrefix.equalsIgnoreCase("idk") && mc.world != null) {
            mc.getConnection().sendPacket(new CPacketChatMessage("@Servergetprefix"));
         }

         if (this.pingTimer.passedMs((long)((Integer)this.delay.getValue() * 1000))) {
            mc.getConnection().sendPacket(new CPacketKeepAlive(100L));
            this.pingTimer.reset();
         }

         if ((Boolean)this.clear.getValue()) {
            this.pingList.clear();
         }
      }

   }

   @SubscribeEvent
   public void onPacketReceive(PacketEvent.Receive event) {
      if (event.getPacket() instanceof SPacketChat) {
         SPacketChat packetChat = (SPacketChat)event.getPacket();
         if (packetChat.getChatComponent().getFormattedText().startsWith("@Client")) {
            this.name = new StringBuffer(TextUtil.stripColor(packetChat.getChatComponent().getFormattedText().replace("@Client", "")));
            event.setCanceled(true);
         }
      } else {
         SPacketKeepAlive alive;
         if (event.getPacket() instanceof SPacketKeepAlive && (alive = (SPacketKeepAlive)event.getPacket()).getId() > 0L && alive.getId() < 1000L) {
            this.serverPing = alive.getId();
            this.currentPing = (Boolean)this.oneWay.getValue() ? this.pingTimer.getPassedTimeMs() / 2L : this.pingTimer.getPassedTimeMs();
            this.pingList.add(this.currentPing);
            this.averagePing = this.getAveragePing();
         }
      }

   }

   public String getDisplayInfo() {
      return this.averagePing + "ms";
   }

   private long getAveragePing() {
      if ((Boolean)this.average.getValue() && !this.pingList.isEmpty()) {
         int full = 0;

         long i;
         for(Iterator var2 = this.pingList.iterator(); var2.hasNext(); full = (int)((long)full + i)) {
            i = (Long)var2.next();
         }

         return (long)(full / this.pingList.size());
      } else {
         return this.currentPing;
      }
   }

   public boolean isConnected() {
      return this.connected.get();
   }

   public long getServerPing() {
      return this.serverPing;
   }
}
