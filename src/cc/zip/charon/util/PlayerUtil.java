package cc.zip.charon.util;

import cc.zip.charon.features.command.Command;
import com.google.common.collect.Lists;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.util.UUIDTypeAdapter;
import java.io.BufferedInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.Scanner;
import java.util.UUID;
import javax.net.ssl.HttpsURLConnection;
import net.minecraft.advancements.AdvancementManager;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.potion.Potion;
import org.apache.commons.io.IOUtils;

public class PlayerUtil implements Util {
   private static final JsonParser PARSER = new JsonParser();

   public static String getNameFromUUID(UUID uuid) {
      try {
         PlayerUtil.lookUpName process = new PlayerUtil.lookUpName(uuid);
         Thread thread = new Thread(process);
         thread.start();
         thread.join();
         return process.getName();
      } catch (Exception var3) {
         return null;
      }
   }

   public static boolean isMoving(EntityLivingBase entity) {
      return entity.moveForward != 0.0F || entity.moveStrafing != 0.0F;
   }

   public static void setSpeed(EntityLivingBase entity, double speed) {
      double[] dir = forward(speed);
      entity.motionX = dir[0];
      entity.motionZ = dir[1];
   }

   public static double getBaseMoveSpeed() {
      double baseSpeed = 0.2873D;
      if (mc.player != null && mc.player.isPotionActive(Potion.getPotionById(1))) {
         int amplifier = mc.player.getActivePotionEffect(Potion.getPotionById(1)).getAmplifier();
         baseSpeed *= 1.0D + 0.2D * (double)(amplifier + 1);
      }

      return baseSpeed;
   }

   public static double[] forward(double speed) {
      float forward = mc.player.movementInput.moveForward;
      float side = mc.player.movementInput.moveStrafe;
      float yaw = mc.player.prevRotationYaw + (mc.player.rotationYaw - mc.player.prevRotationYaw) * mc.getRenderPartialTicks();
      if (forward != 0.0F) {
         if (side > 0.0F) {
            yaw += (float)(forward > 0.0F ? -45 : 45);
         } else if (side < 0.0F) {
            yaw += (float)(forward > 0.0F ? 45 : -45);
         }

         side = 0.0F;
         if (forward > 0.0F) {
            forward = 1.0F;
         } else if (forward < 0.0F) {
            forward = -1.0F;
         }
      }

      double sin = Math.sin(Math.toRadians((double)(yaw + 90.0F)));
      double cos = Math.cos(Math.toRadians((double)(yaw + 90.0F)));
      double posX = (double)forward * speed * cos + (double)side * speed * sin;
      double posZ = (double)forward * speed * sin - (double)side * speed * cos;
      return new double[]{posX, posZ};
   }

   public static String getNameFromUUID(String uuid) {
      try {
         PlayerUtil.lookUpName process = new PlayerUtil.lookUpName(uuid);
         Thread thread = new Thread(process);
         thread.start();
         thread.join();
         return process.getName();
      } catch (Exception var3) {
         return null;
      }
   }

   public static UUID getUUIDFromName(String name) {
      try {
         PlayerUtil.lookUpUUID process = new PlayerUtil.lookUpUUID(name);
         Thread thread = new Thread(process);
         thread.start();
         thread.join();
         return process.getUUID();
      } catch (Exception var3) {
         return null;
      }
   }

   public static String requestIDs(String data) {
      try {
         String query = "https://api.mojang.com/profiles/minecraft";
         URL url = new URL(query);
         HttpURLConnection conn = (HttpURLConnection)url.openConnection();
         conn.setConnectTimeout(5000);
         conn.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
         conn.setDoOutput(true);
         conn.setDoInput(true);
         conn.setRequestMethod("POST");
         OutputStream os = conn.getOutputStream();
         os.write(data.getBytes(StandardCharsets.UTF_8));
         os.close();
         InputStream in = new BufferedInputStream(conn.getInputStream());
         String res = convertStreamToString(in);
         in.close();
         conn.disconnect();
         return res;
      } catch (Exception var7) {
         return null;
      }
   }

   public static String convertStreamToString(InputStream is) {
      Scanner s = (new Scanner(is)).useDelimiter("\\A");
      return s.hasNext() ? s.next() : "/";
   }

   public static List<String> getHistoryOfNames(UUID id) {
      try {
         JsonArray array = getResources(new URL("https://api.mojang.com/user/profiles/" + getIdNoHyphens(id) + "/names"), "GET").getAsJsonArray();
         List<String> temp = Lists.newArrayList();
         Iterator var3 = array.iterator();

         while(var3.hasNext()) {
            JsonElement e = (JsonElement)var3.next();
            JsonObject node = e.getAsJsonObject();
            String name = node.get("name").getAsString();
            long changedAt = node.has("changedToAt") ? node.get("changedToAt").getAsLong() : 0L;
            temp.add(name + "Г‚В§8" + (new Date(changedAt)).toString());
         }

         Collections.sort(temp);
         return temp;
      } catch (Exception var9) {
         return null;
      }
   }

   public static String getIdNoHyphens(UUID uuid) {
      return uuid.toString().replaceAll("-", "");
   }

   private static JsonElement getResources(URL url, String request) throws Exception {
      return getResources(url, request, (JsonElement)null);
   }

   private static JsonElement getResources(URL url, String request, JsonElement element) throws Exception {
      HttpsURLConnection connection = null;

      try {
         connection = (HttpsURLConnection)url.openConnection();
         connection.setDoOutput(true);
         connection.setRequestMethod(request);
         connection.setRequestProperty("Content-Type", "application/json");
         if (element != null) {
            DataOutputStream output = new DataOutputStream(connection.getOutputStream());
            output.writeBytes(AdvancementManager.GSON.toJson(element));
            output.close();
         }

         Scanner scanner = new Scanner(connection.getInputStream());
         StringBuilder builder = new StringBuilder();

         while(scanner.hasNextLine()) {
            builder.append(scanner.nextLine());
            builder.append('\n');
         }

         scanner.close();
         String json = builder.toString();
         JsonElement data = PARSER.parse(json);
         JsonElement var8 = data;
         return var8;
      } finally {
         if (connection != null) {
            connection.disconnect();
         }

      }
   }

   public static class lookUpName implements Runnable {
      private final String uuid;
      private final UUID uuidID;
      private volatile String name;

      public lookUpName(String input) {
         this.uuid = input;
         this.uuidID = UUID.fromString(input);
      }

      public lookUpName(UUID input) {
         this.uuidID = input;
         this.uuid = input.toString();
      }

      public void run() {
         this.name = this.lookUpName();
      }

      public String lookUpName() {
         EntityPlayer player = null;
         if (Util.mc.world != null) {
            player = Util.mc.world.getPlayerEntityByUUID(this.uuidID);
         }

         if (player == null) {
            String url = "https://api.mojang.com/user/profiles/" + this.uuid.replace("-", "") + "/names";

            try {
               String nameJson = IOUtils.toString(new URL(url));
               if (nameJson.contains(",")) {
                  List<String> names = Arrays.asList(nameJson.split(","));
                  Collections.reverse(names);
                  return ((String)names.get(1)).replace("{\"name\":\"", "").replace("\"", "");
               } else {
                  return nameJson.replace("[{\"name\":\"", "").replace("\"}]", "");
               }
            } catch (IOException var5) {
               var5.printStackTrace();
               return null;
            }
         } else {
            return player.getName();
         }
      }

      public String getName() {
         return this.name;
      }
   }

   public static class lookUpUUID implements Runnable {
      private final String name;
      private volatile UUID uuid;

      public lookUpUUID(String name) {
         this.name = name;
      }

      public void run() {
         NetworkPlayerInfo profile;
         try {
            ArrayList<NetworkPlayerInfo> infoMap = new ArrayList(((NetHandlerPlayClient)Objects.requireNonNull(Util.mc.getConnection())).getPlayerInfoMap());
            profile = (NetworkPlayerInfo)infoMap.stream().filter((networkPlayerInfo) -> {
               return networkPlayerInfo.getGameProfile().getName().equalsIgnoreCase(this.name);
            }).findFirst().orElse(null);

            assert profile != null;

            this.uuid = profile.getGameProfile().getId();
         } catch (Exception var6) {
            profile = null;
         }

         if (profile == null) {
            Command.sendMessage("Player isn't online. Looking up UUID..");
            String s = PlayerUtil.requestIDs("[\"" + this.name + "\"]");
            if (s != null && !s.isEmpty()) {
               JsonElement element = (new JsonParser()).parse(s);
               if (element.getAsJsonArray().size() == 0) {
                  Command.sendMessage("Couldn't find player ID. (1)");
               } else {
                  try {
                     String id = element.getAsJsonArray().get(0).getAsJsonObject().get("id").getAsString();
                     this.uuid = UUIDTypeAdapter.fromString(id);
                  } catch (Exception var5) {
                     var5.printStackTrace();
                     Command.sendMessage("Couldn't find player ID. (2)");
                  }
               }
            } else {
               Command.sendMessage("Couldn't find player ID. Are you connected to the internet? (0)");
            }
         }

      }

      public UUID getUUID() {
         return this.uuid;
      }

      public String getName() {
         return this.name;
      }
   }
}
