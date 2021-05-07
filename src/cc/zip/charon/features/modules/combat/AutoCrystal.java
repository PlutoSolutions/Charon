package cc.zip.charon.features.modules.combat;

import cc.zip.charon.Charon;
import cc.zip.charon.event.events.ClientEvent;
import cc.zip.charon.event.events.PacketEvent;
import cc.zip.charon.event.events.Render3DEvent;
import cc.zip.charon.event.events.UpdateWalkingPlayerEvent;
import cc.zip.charon.features.modules.Module;
import cc.zip.charon.features.modules.client.ClickGui;
import cc.zip.charon.features.setting.Bind;
import cc.zip.charon.features.setting.Setting;
import cc.zip.charon.util.ColorUtil;
import cc.zip.charon.util.EntityUtil;
import cc.zip.charon.util.MathUtil;
import cc.zip.charon.util.RenderUtil;
import cc.zip.charon.util.Timer;
import cc.zip.charon.util.Util;
import cc.zip.charon.util.none.BlockUtil2;
import cc.zip.charon.util.none.DamageUtil2;
import java.awt.Color;
import java.lang.Thread.State;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.ItemPickaxe;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.network.play.client.CPacketUseEntity;
import net.minecraft.network.play.client.CPacketUseEntity.Action;
import net.minecraft.network.play.server.SPacketDestroyEntities;
import net.minecraft.network.play.server.SPacketEntityStatus;
import net.minecraft.network.play.server.SPacketExplosion;
import net.minecraft.network.play.server.SPacketSpawnObject;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class AutoCrystal extends Module {
   private final Setting<AutoCrystal.Settings> setting;
   public Setting<AutoCrystal.Raytrace> raytrace;
   public Setting<Boolean> place;
   public Setting<Integer> placeDelay;
   public Setting<Float> placeRange;
   public Setting<Float> minDamage;
   public Setting<Integer> wasteAmount;
   public Setting<Boolean> wasteMinDmgCount;
   public Setting<Float> facePlace;
   public Setting<Float> placetrace;
   public Setting<Boolean> antiSurround;
   public Setting<Boolean> limitFacePlace;
   public Setting<Boolean> oneDot15;
   public Setting<Boolean> doublePop;
   public Setting<Float> popDamage;
   public Setting<Integer> popTime;
   public Setting<Boolean> explode;
   public Setting<AutoCrystal.Switch> switchMode;
   public Setting<Integer> breakDelay;
   public Setting<Float> breakRange;
   public Setting<Integer> packets;
   public Setting<Float> breaktrace;
   public Setting<Boolean> manual;
   public Setting<Boolean> manualMinDmg;
   public Setting<Integer> manualBreak;
   public Setting<Boolean> sync;
   public Setting<Boolean> instant;
   public Setting<Boolean> render;
   public Setting<Boolean> colorSync;
   public Setting<Boolean> box;
   public Setting<Boolean> outline;
   public Setting<Boolean> text;
   private final Setting<Integer> red;
   private final Setting<Integer> green;
   private final Setting<Integer> blue;
   private final Setting<Integer> alpha;
   private final Setting<Integer> boxAlpha;
   private final Setting<Float> lineWidth;
   public Setting<Boolean> customOutline;
   private final Setting<Integer> cRed;
   private final Setting<Integer> cGreen;
   private final Setting<Integer> cBlue;
   private final Setting<Integer> cAlpha;
   public Setting<Float> range;
   public Setting<AutoCrystal.Target> targetMode;
   public Setting<Integer> minArmor;
   private final Setting<Integer> switchCooldown;
   public Setting<AutoCrystal.AutoSwitch> autoSwitch;
   public Setting<Bind> switchBind;
   public Setting<Boolean> offhandSwitch;
   public Setting<Boolean> switchBack;
   public Setting<Boolean> lethalSwitch;
   public Setting<Boolean> mineSwitch;
   public Setting<AutoCrystal.Rotate> rotate;
   public Setting<Boolean> suicide;
   public Setting<Boolean> webAttack;
   public Setting<Boolean> fullCalc;
   public Setting<Boolean> extraSelfCalc;
   public Setting<AutoCrystal.Logic> logic;
   public Setting<Boolean> doubleMap;
   public Setting<AutoCrystal.DamageSync> damageSync;
   public Setting<Integer> damageSyncTime;
   public Setting<Float> dropOff;
   public Setting<Integer> confirm;
   public Setting<Boolean> syncedFeetPlace;
   public Setting<Boolean> fullSync;
   public Setting<Boolean> syncCount;
   public Setting<Boolean> hyperSync;
   public Setting<Boolean> gigaSync;
   public Setting<Boolean> syncySync;
   public Setting<Boolean> enormousSync;
   private final Setting<Integer> eventMode;
   private final Setting<AutoCrystal.ThreadMode> threadMode;
   public Setting<Integer> threadDelay;
   public Setting<Integer> syncThreads;
   private Queue<Entity> attackList;
   private Map<Entity, Float> crystalMap;
   private final Timer switchTimer;
   private final Timer manualTimer;
   private final Timer breakTimer;
   private final Timer placeTimer;
   private final Timer syncTimer;
   public static EntityPlayer target = null;
   private Entity efficientTarget;
   private double currentDamage;
   private double renderDamage;
   private double lastDamage;
   private boolean didRotation;
   private boolean switching;
   private BlockPos placePos;
   private BlockPos renderPos;
   private boolean mainHand;
   private boolean rotating;
   private boolean offHand;
   private int crystalCount;
   private int minDmgCount;
   private int lastSlot;
   private float yaw;
   private float pitch;
   private BlockPos webPos;
   private final Timer renderTimer;
   private BlockPos lastPos;
   public static Set<BlockPos> placedPos = new HashSet();
   public static Set<BlockPos> brokenPos = new HashSet();
   private boolean posConfirmed;
   private boolean foundDoublePop;
   private final AtomicBoolean shouldInterrupt;
   private ScheduledExecutorService executor;
   private final Timer syncroTimer;
   private Thread thread;
   private EntityPlayer currentSyncTarget;
   private BlockPos syncedPlayerPos;
   private BlockPos syncedCrystalPos;
   private static AutoCrystal instance;
   private final Map<EntityPlayer, Timer> totemPops;

   public AutoCrystal() {
      super("AutoCrystal", "Best CA on the market", Module.Category.COMBAT, true, false, false);
      this.setting = this.register(new Setting("Settings", AutoCrystal.Settings.PLACE));
      this.raytrace = this.register(new Setting("Raytrace", AutoCrystal.Raytrace.NONE, (v) -> {
         return this.setting.getValue() == AutoCrystal.Settings.MISC;
      }));
      this.place = this.register(new Setting("Place", true, (v) -> {
         return this.setting.getValue() == AutoCrystal.Settings.PLACE;
      }));
      this.placeDelay = this.register(new Setting("PlaceDelay", 0, 0, 1000, (v) -> {
         return this.setting.getValue() == AutoCrystal.Settings.PLACE && (Boolean)this.place.getValue();
      }));
      this.placeRange = this.register(new Setting("PlaceRange", 6.0F, 0.0F, 10.0F, (v) -> {
         return this.setting.getValue() == AutoCrystal.Settings.PLACE && (Boolean)this.place.getValue();
      }));
      this.minDamage = this.register(new Setting("MinDamage", 4.0F, 0.1F, 20.0F, (v) -> {
         return this.setting.getValue() == AutoCrystal.Settings.PLACE && (Boolean)this.place.getValue();
      }));
      this.wasteAmount = this.register(new Setting("WasteAmount", 1, 1, 5, (v) -> {
         return this.setting.getValue() == AutoCrystal.Settings.PLACE && (Boolean)this.place.getValue();
      }));
      this.wasteMinDmgCount = this.register(new Setting("CountMinDmg", true, (v) -> {
         return this.setting.getValue() == AutoCrystal.Settings.PLACE && (Boolean)this.place.getValue();
      }));
      this.facePlace = this.register(new Setting("FacePlace", 8.0F, 0.1F, 20.0F, (v) -> {
         return this.setting.getValue() == AutoCrystal.Settings.PLACE && (Boolean)this.place.getValue();
      }));
      this.placetrace = this.register(new Setting("Placetrace", 6.0F, 0.0F, 10.0F, (v) -> {
         return this.setting.getValue() == AutoCrystal.Settings.PLACE && (Boolean)this.place.getValue() && this.raytrace.getValue() != AutoCrystal.Raytrace.NONE && this.raytrace.getValue() != AutoCrystal.Raytrace.BREAK;
      }));
      this.antiSurround = this.register(new Setting("AntiSurround", false, (v) -> {
         return this.setting.getValue() == AutoCrystal.Settings.PLACE && (Boolean)this.place.getValue();
      }));
      this.limitFacePlace = this.register(new Setting("LimitFacePlace", true, (v) -> {
         return this.setting.getValue() == AutoCrystal.Settings.PLACE && (Boolean)this.place.getValue();
      }));
      this.oneDot15 = this.register(new Setting("1.15", false, (v) -> {
         return this.setting.getValue() == AutoCrystal.Settings.PLACE && (Boolean)this.place.getValue();
      }));
      this.doublePop = this.register(new Setting("AntiTotem", false, (v) -> {
         return this.setting.getValue() == AutoCrystal.Settings.PLACE && (Boolean)this.place.getValue();
      }));
      this.popDamage = this.register(new Setting("PopDamage", 4.0F, 0.0F, 6.0F, (v) -> {
         return this.setting.getValue() == AutoCrystal.Settings.PLACE && (Boolean)this.place.getValue() && (Boolean)this.doublePop.getValue();
      }));
      this.popTime = this.register(new Setting("PopTime", 500, 0, 1000, (v) -> {
         return this.setting.getValue() == AutoCrystal.Settings.PLACE && (Boolean)this.place.getValue() && (Boolean)this.doublePop.getValue();
      }));
      this.explode = this.register(new Setting("Break", true, (v) -> {
         return this.setting.getValue() == AutoCrystal.Settings.BREAK;
      }));
      this.switchMode = this.register(new Setting("Attack", AutoCrystal.Switch.BREAKSLOT, (v) -> {
         return this.setting.getValue() == AutoCrystal.Settings.BREAK && (Boolean)this.explode.getValue();
      }));
      this.breakDelay = this.register(new Setting("BreakDelay", 0, 0, 1000, (v) -> {
         return this.setting.getValue() == AutoCrystal.Settings.BREAK && (Boolean)this.explode.getValue();
      }));
      this.breakRange = this.register(new Setting("BreakRange", 6.0F, 0.0F, 10.0F, (v) -> {
         return this.setting.getValue() == AutoCrystal.Settings.BREAK && (Boolean)this.explode.getValue();
      }));
      this.packets = this.register(new Setting("Packets", 1, 1, 6, (v) -> {
         return this.setting.getValue() == AutoCrystal.Settings.BREAK && (Boolean)this.explode.getValue();
      }));
      this.breaktrace = this.register(new Setting("Breaktrace", 6.0F, 0.0F, 10.0F, (v) -> {
         return this.setting.getValue() == AutoCrystal.Settings.BREAK && (Boolean)this.explode.getValue() && this.raytrace.getValue() != AutoCrystal.Raytrace.NONE && this.raytrace.getValue() != AutoCrystal.Raytrace.PLACE;
      }));
      this.manual = this.register(new Setting("Manual", false, (v) -> {
         return this.setting.getValue() == AutoCrystal.Settings.BREAK;
      }));
      this.manualMinDmg = this.register(new Setting("ManMinDmg", false, (v) -> {
         return this.setting.getValue() == AutoCrystal.Settings.BREAK && (Boolean)this.manual.getValue();
      }));
      this.manualBreak = this.register(new Setting("ManualDelay", 500, 0, 1000, (v) -> {
         return this.setting.getValue() == AutoCrystal.Settings.BREAK && (Boolean)this.manual.getValue();
      }));
      this.sync = this.register(new Setting("Sync", true, (v) -> {
         return this.setting.getValue() == AutoCrystal.Settings.BREAK && ((Boolean)this.explode.getValue() || (Boolean)this.manual.getValue());
      }));
      this.instant = this.register(new Setting("Predict", false, (v) -> {
         return this.setting.getValue() == AutoCrystal.Settings.BREAK && (Boolean)this.explode.getValue() && (Boolean)this.place.getValue();
      }));
      this.render = this.register(new Setting("Render", true, (v) -> {
         return this.setting.getValue() == AutoCrystal.Settings.RENDER;
      }));
      this.colorSync = this.register(new Setting("Sync", false, (v) -> {
         return this.setting.getValue() == AutoCrystal.Settings.RENDER;
      }));
      this.box = this.register(new Setting("Box", true, (v) -> {
         return this.setting.getValue() == AutoCrystal.Settings.RENDER && (Boolean)this.render.getValue();
      }));
      this.outline = this.register(new Setting("Outline", true, (v) -> {
         return this.setting.getValue() == AutoCrystal.Settings.RENDER && (Boolean)this.render.getValue();
      }));
      this.text = this.register(new Setting("Text", false, (v) -> {
         return this.setting.getValue() == AutoCrystal.Settings.RENDER && (Boolean)this.render.getValue();
      }));
      this.red = this.register(new Setting("Red", 255, 0, 255, (v) -> {
         return this.setting.getValue() == AutoCrystal.Settings.RENDER && (Boolean)this.render.getValue();
      }));
      this.green = this.register(new Setting("Green", 255, 0, 255, (v) -> {
         return this.setting.getValue() == AutoCrystal.Settings.RENDER && (Boolean)this.render.getValue();
      }));
      this.blue = this.register(new Setting("Blue", 255, 0, 255, (v) -> {
         return this.setting.getValue() == AutoCrystal.Settings.RENDER && (Boolean)this.render.getValue();
      }));
      this.alpha = this.register(new Setting("Alpha", 255, 0, 255, (v) -> {
         return this.setting.getValue() == AutoCrystal.Settings.RENDER && (Boolean)this.render.getValue();
      }));
      this.boxAlpha = this.register(new Setting("BoxAlpha", 125, 0, 255, (v) -> {
         return this.setting.getValue() == AutoCrystal.Settings.RENDER && (Boolean)this.render.getValue() && (Boolean)this.box.getValue();
      }));
      this.lineWidth = this.register(new Setting("LineWidth", 1.5F, 0.1F, 5.0F, (v) -> {
         return this.setting.getValue() == AutoCrystal.Settings.RENDER && (Boolean)this.render.getValue() && (Boolean)this.outline.getValue();
      }));
      this.customOutline = this.register(new Setting("CustomLine", false, (v) -> {
         return this.setting.getValue() == AutoCrystal.Settings.RENDER && (Boolean)this.render.getValue() && (Boolean)this.outline.getValue();
      }));
      this.cRed = this.register(new Setting("OL-Red", 255, 0, 255, (v) -> {
         return this.setting.getValue() == AutoCrystal.Settings.RENDER && (Boolean)this.render.getValue() && (Boolean)this.customOutline.getValue() && (Boolean)this.outline.getValue();
      }));
      this.cGreen = this.register(new Setting("OL-Green", 255, 0, 255, (v) -> {
         return this.setting.getValue() == AutoCrystal.Settings.RENDER && (Boolean)this.render.getValue() && (Boolean)this.customOutline.getValue() && (Boolean)this.outline.getValue();
      }));
      this.cBlue = this.register(new Setting("OL-Blue", 255, 0, 255, (v) -> {
         return this.setting.getValue() == AutoCrystal.Settings.RENDER && (Boolean)this.render.getValue() && (Boolean)this.customOutline.getValue() && (Boolean)this.outline.getValue();
      }));
      this.cAlpha = this.register(new Setting("OL-Alpha", 255, 0, 255, (v) -> {
         return this.setting.getValue() == AutoCrystal.Settings.RENDER && (Boolean)this.render.getValue() && (Boolean)this.customOutline.getValue() && (Boolean)this.outline.getValue();
      }));
      this.range = this.register(new Setting("Range", 12.0F, 0.1F, 20.0F, (v) -> {
         return this.setting.getValue() == AutoCrystal.Settings.MISC;
      }));
      this.targetMode = this.register(new Setting("Target", AutoCrystal.Target.CLOSEST, (v) -> {
         return this.setting.getValue() == AutoCrystal.Settings.MISC;
      }));
      this.minArmor = this.register(new Setting("MinArmor", 0, 0, 125, (v) -> {
         return this.setting.getValue() == AutoCrystal.Settings.MISC;
      }));
      this.switchCooldown = this.register(new Setting("Cooldown", 500, 0, 1000, (v) -> {
         return this.setting.getValue() == AutoCrystal.Settings.MISC;
      }));
      this.autoSwitch = this.register(new Setting("Switch", AutoCrystal.AutoSwitch.TOGGLE, (v) -> {
         return this.setting.getValue() == AutoCrystal.Settings.MISC;
      }));
      this.switchBind = this.register(new Setting("SwitchBind", new Bind(-1), (v) -> {
         return this.setting.getValue() == AutoCrystal.Settings.MISC && this.autoSwitch.getValue() == AutoCrystal.AutoSwitch.TOGGLE;
      }));
      this.offhandSwitch = this.register(new Setting("Offhand", false, (v) -> {
         return this.setting.getValue() == AutoCrystal.Settings.MISC && this.autoSwitch.getValue() != AutoCrystal.AutoSwitch.NONE;
      }));
      this.switchBack = this.register(new Setting("Switchback", false, (v) -> {
         return this.setting.getValue() == AutoCrystal.Settings.MISC && this.autoSwitch.getValue() != AutoCrystal.AutoSwitch.NONE && (Boolean)this.offhandSwitch.getValue();
      }));
      this.lethalSwitch = this.register(new Setting("LethalSwitch", false, (v) -> {
         return this.setting.getValue() == AutoCrystal.Settings.MISC && this.autoSwitch.getValue() != AutoCrystal.AutoSwitch.NONE;
      }));
      this.mineSwitch = this.register(new Setting("MineSwitch", false, (v) -> {
         return this.setting.getValue() == AutoCrystal.Settings.MISC && this.autoSwitch.getValue() != AutoCrystal.AutoSwitch.NONE;
      }));
      this.rotate = this.register(new Setting("Rotate", AutoCrystal.Rotate.OFF, (v) -> {
         return this.setting.getValue() == AutoCrystal.Settings.MISC;
      }));
      this.suicide = this.register(new Setting("Suicide", false, (v) -> {
         return this.setting.getValue() == AutoCrystal.Settings.MISC;
      }));
      this.webAttack = this.register(new Setting("WebAttack", false, (v) -> {
         return this.setting.getValue() == AutoCrystal.Settings.MISC && this.targetMode.getValue() != AutoCrystal.Target.DAMAGE;
      }));
      this.fullCalc = this.register(new Setting("ExtraCalc", false, (v) -> {
         return this.setting.getValue() == AutoCrystal.Settings.MISC;
      }));
      this.extraSelfCalc = this.register(new Setting("MinSelfDmg", true, (v) -> {
         return this.setting.getValue() == AutoCrystal.Settings.MISC;
      }));
      this.logic = this.register(new Setting("Logic", AutoCrystal.Logic.BREAKPLACE, (v) -> {
         return this.setting.getValue() == AutoCrystal.Settings.DEV;
      }));
      this.doubleMap = this.register(new Setting("DoubleMap", false, (v) -> {
         return this.setting.getValue() == AutoCrystal.Settings.DEV && this.logic.getValue() == AutoCrystal.Logic.PLACEBREAK;
      }));
      this.damageSync = this.register(new Setting("DamageSync", AutoCrystal.DamageSync.NONE, (v) -> {
         return this.setting.getValue() == AutoCrystal.Settings.DEV;
      }));
      this.damageSyncTime = this.register(new Setting("SyncDelay", 500, 0, 1000, (v) -> {
         return this.setting.getValue() == AutoCrystal.Settings.DEV && this.damageSync.getValue() != AutoCrystal.DamageSync.NONE;
      }));
      this.dropOff = this.register(new Setting("DropOff", 5.0F, 0.0F, 10.0F, (v) -> {
         return this.setting.getValue() == AutoCrystal.Settings.DEV && this.damageSync.getValue() == AutoCrystal.DamageSync.BREAK;
      }));
      this.confirm = this.register(new Setting("Confirm", 250, 0, 1000, (v) -> {
         return this.setting.getValue() == AutoCrystal.Settings.DEV && this.damageSync.getValue() != AutoCrystal.DamageSync.NONE;
      }));
      this.syncedFeetPlace = this.register(new Setting("FeetSync", false, (v) -> {
         return this.setting.getValue() == AutoCrystal.Settings.DEV && this.damageSync.getValue() != AutoCrystal.DamageSync.NONE;
      }));
      this.fullSync = this.register(new Setting("FullSync", false, (v) -> {
         return this.setting.getValue() == AutoCrystal.Settings.DEV && this.damageSync.getValue() != AutoCrystal.DamageSync.NONE && (Boolean)this.syncedFeetPlace.getValue();
      }));
      this.syncCount = this.register(new Setting("SyncCount", true, (v) -> {
         return this.setting.getValue() == AutoCrystal.Settings.DEV && this.damageSync.getValue() != AutoCrystal.DamageSync.NONE && (Boolean)this.syncedFeetPlace.getValue();
      }));
      this.hyperSync = this.register(new Setting("HyperSync", false, (v) -> {
         return this.setting.getValue() == AutoCrystal.Settings.DEV && this.damageSync.getValue() != AutoCrystal.DamageSync.NONE && (Boolean)this.syncedFeetPlace.getValue();
      }));
      this.gigaSync = this.register(new Setting("GigaSync", false, (v) -> {
         return this.setting.getValue() == AutoCrystal.Settings.DEV && this.damageSync.getValue() != AutoCrystal.DamageSync.NONE && (Boolean)this.syncedFeetPlace.getValue();
      }));
      this.syncySync = this.register(new Setting("SyncySync", false, (v) -> {
         return this.setting.getValue() == AutoCrystal.Settings.DEV && this.damageSync.getValue() != AutoCrystal.DamageSync.NONE && (Boolean)this.syncedFeetPlace.getValue();
      }));
      this.enormousSync = this.register(new Setting("EnormousSync", false, (v) -> {
         return this.setting.getValue() == AutoCrystal.Settings.DEV && this.damageSync.getValue() != AutoCrystal.DamageSync.NONE && (Boolean)this.syncedFeetPlace.getValue();
      }));
      this.eventMode = this.register(new Setting("Updates", 3, 1, 3, (v) -> {
         return this.setting.getValue() == AutoCrystal.Settings.DEV;
      }));
      this.threadMode = this.register(new Setting("Thread", AutoCrystal.ThreadMode.NONE, (v) -> {
         return this.setting.getValue() == AutoCrystal.Settings.DEV;
      }));
      this.threadDelay = this.register(new Setting("ThreadDelay", 25, 1, 1000, (v) -> {
         return this.setting.getValue() == AutoCrystal.Settings.DEV && this.threadMode.getValue() != AutoCrystal.ThreadMode.NONE;
      }));
      this.syncThreads = this.register(new Setting("SyncThreads", 1000, 1, 10000, (v) -> {
         return this.setting.getValue() == AutoCrystal.Settings.DEV && this.threadMode.getValue() != AutoCrystal.ThreadMode.NONE;
      }));
      this.attackList = new ConcurrentLinkedQueue();
      this.crystalMap = new HashMap();
      this.switchTimer = new Timer();
      this.manualTimer = new Timer();
      this.breakTimer = new Timer();
      this.placeTimer = new Timer();
      this.syncTimer = new Timer();
      this.efficientTarget = null;
      this.currentDamage = 0.0D;
      this.renderDamage = 0.0D;
      this.lastDamage = 0.0D;
      this.didRotation = false;
      this.switching = false;
      this.placePos = null;
      this.renderPos = null;
      this.mainHand = false;
      this.rotating = false;
      this.offHand = false;
      this.crystalCount = 0;
      this.minDmgCount = 0;
      this.lastSlot = -1;
      this.yaw = 0.0F;
      this.pitch = 0.0F;
      this.webPos = null;
      this.renderTimer = new Timer();
      this.lastPos = null;
      this.posConfirmed = false;
      this.foundDoublePop = false;
      this.shouldInterrupt = new AtomicBoolean(false);
      this.syncroTimer = new Timer();
      this.totemPops = new ConcurrentHashMap();
      instance = this;
   }

   public static AutoCrystal getInstance() {
      if (instance == null) {
         instance = new AutoCrystal();
      }

      return instance;
   }

   public void onTick() {
      if (this.threadMode.getValue() == AutoCrystal.ThreadMode.NONE && (Integer)this.eventMode.getValue() == 3) {
         this.doAutoCrystal();
      }

   }

   @SubscribeEvent
   public void onUpdateWalkingPlayer(UpdateWalkingPlayerEvent event) {
      if (event.getStage() == 0) {
         if (this.threadMode.getValue() != AutoCrystal.ThreadMode.NONE) {
            this.processMultiThreading();
         } else if ((Integer)this.eventMode.getValue() == 2) {
            this.doAutoCrystal();
         }

      }
   }

   public void onUpdate() {
      if (this.threadMode.getValue() == AutoCrystal.ThreadMode.NONE && (Integer)this.eventMode.getValue() == 1) {
         this.doAutoCrystal();
      }

   }

   public void onToggle() {
      brokenPos.clear();
      placedPos.clear();
      this.totemPops.clear();
      this.rotating = false;
   }

   public void onDisable() {
      if (this.thread != null) {
         this.shouldInterrupt.set(true);
      }

      if (this.executor != null) {
         this.executor.shutdown();
      }

   }

   public void onEnable() {
      if (this.threadMode.getValue() != AutoCrystal.ThreadMode.NONE) {
         this.processMultiThreading();
      }

   }

   public String getDisplayInfo() {
      if (this.switching) {
         return "§aSwitch";
      } else {
         return target != null ? target.getName() + "| FastMode" : null;
      }
   }

   @SubscribeEvent
   public void onPacketSend(PacketEvent.Send event) {
      if (event.getStage() == 0 && this.rotate.getValue() != AutoCrystal.Rotate.OFF && this.rotating && (Integer)this.eventMode.getValue() != 2 && event.getPacket() instanceof CPacketPlayer) {
         CPacketPlayer packet = (CPacketPlayer)event.getPacket();
         packet.yaw = this.yaw;
         packet.pitch = this.pitch;
         this.rotating = false;
      }

   }

   @SubscribeEvent(
      priority = EventPriority.HIGHEST,
      receiveCanceled = true
   )
   public void onPacketReceive(PacketEvent.Receive event) {
      if ((Boolean)this.explode.getValue() && (Boolean)this.instant.getValue() && event.getPacket() instanceof SPacketSpawnObject && (this.syncedCrystalPos == null || !(Boolean)this.syncedFeetPlace.getValue() || this.damageSync.getValue() == AutoCrystal.DamageSync.NONE)) {
         SPacketSpawnObject packet2 = (SPacketSpawnObject)event.getPacket();
         if (packet2.getType() == 51 && placedPos.contains((new BlockPos(packet2.getX(), packet2.getY(), packet2.getZ())).down())) {
            CPacketUseEntity attackPacket = new CPacketUseEntity();
            attackPacket.entityId = packet2.getEntityID();
            attackPacket.action = Action.ATTACK;
            Util.mc.player.connection.sendPacket(attackPacket);
         }
      } else if (event.getPacket() instanceof SPacketExplosion) {
         SPacketExplosion packet3 = (SPacketExplosion)event.getPacket();
         BlockPos pos = (new BlockPos(packet3.getX(), packet3.getY(), packet3.getZ())).down();
         if (this.damageSync.getValue() == AutoCrystal.DamageSync.PLACE) {
            if (placedPos.contains(pos)) {
               placedPos.remove(pos);
               this.posConfirmed = true;
            }
         } else if (this.damageSync.getValue() == AutoCrystal.DamageSync.BREAK && brokenPos.contains(pos)) {
            brokenPos.remove(pos);
            this.posConfirmed = true;
         }
      } else if (event.getPacket() instanceof SPacketDestroyEntities) {
         SPacketDestroyEntities packet4 = (SPacketDestroyEntities)event.getPacket();
         int[] var11 = packet4.getEntityIDs();
         int var5 = var11.length;

         for(int var6 = 0; var6 < var5; ++var6) {
            int id = var11[var6];
            Entity entity = Util.mc.world.getEntityByID(id);
            if (entity instanceof EntityEnderCrystal) {
               brokenPos.remove((new BlockPos(entity.getPositionVector())).down());
               placedPos.remove((new BlockPos(entity.getPositionVector())).down());
            }
         }
      } else {
         SPacketEntityStatus packet;
         if (event.getPacket() instanceof SPacketEntityStatus && (packet = (SPacketEntityStatus)event.getPacket()).getOpCode() == 35 && packet.getEntity(Util.mc.world) instanceof EntityPlayer) {
            this.totemPops.put((EntityPlayer)packet.getEntity(Util.mc.world), (new Timer()).reset());
         }
      }

   }

   public void onRender3D(Render3DEvent event) {
      if ((this.offHand || this.mainHand || this.switchMode.getValue() == AutoCrystal.Switch.CALC) && this.renderPos != null && (Boolean)this.render.getValue() && ((Boolean)this.box.getValue() || (Boolean)this.text.getValue() || (Boolean)this.outline.getValue())) {
         RenderUtil.drawBoxESP(this.renderPos, (Boolean)ClickGui.getInstance().rainbow.getValue() ? ColorUtil.rainbow((Integer)ClickGui.getInstance().rainbowHue.getValue()) : new Color((Integer)this.red.getValue(), (Integer)this.green.getValue(), (Integer)this.blue.getValue(), (Integer)this.alpha.getValue()), (Boolean)this.outline.getValue(), (Boolean)ClickGui.getInstance().rainbow.getValue() ? ColorUtil.rainbow((Integer)ClickGui.getInstance().rainbowHue.getValue()) : new Color((Integer)this.cRed.getValue(), (Integer)this.cGreen.getValue(), (Integer)this.cBlue.getValue(), (Integer)this.cAlpha.getValue()), (Float)this.lineWidth.getValue(), (Boolean)this.outline.getValue(), (Boolean)this.box.getValue(), (Integer)this.boxAlpha.getValue(), true);
      }

   }

   @SubscribeEvent
   public void onSettingChange(ClientEvent event) {
      if (event.getStage() == 2 && event.getSetting() != null && event.getSetting().getFeature() != null && event.getSetting().getFeature().equals(this) && this.isEnabled() && (event.getSetting().equals(this.threadDelay) || event.getSetting().equals(this.threadMode))) {
         if (this.executor != null) {
            this.executor.shutdown();
         }

         if (this.thread != null) {
            this.shouldInterrupt.set(true);
         }
      }

   }

   private void processMultiThreading() {
      if (!this.isOff()) {
         if (this.threadMode.getValue() == AutoCrystal.ThreadMode.POOL) {
            this.handlePool();
         } else if (this.threadMode.getValue() == AutoCrystal.ThreadMode.WHILE) {
            this.handleWhile();
         }

      }
   }

   private void handlePool() {
      if (this.executor == null || this.executor.isTerminated() || this.executor.isShutdown() || this.syncroTimer.passedMs((long)(Integer)this.syncThreads.getValue())) {
         if (this.executor != null) {
            this.executor.shutdown();
         }

         this.executor = this.getExecutor();
         this.syncroTimer.reset();
      }

   }

   private void handleWhile() {
      if (this.thread == null || this.thread.isInterrupted() || !this.thread.isAlive() || this.syncroTimer.passedMs((long)(Integer)this.syncThreads.getValue())) {
         if (this.thread == null) {
            this.thread = new Thread(AutoCrystal.RAutoCrystal.getInstance(this));
         } else if (this.syncroTimer.passedMs((long)(Integer)this.syncThreads.getValue()) && !this.shouldInterrupt.get()) {
            this.shouldInterrupt.set(true);
            this.syncroTimer.reset();
            return;
         }

         if (this.thread != null && (this.thread.isInterrupted() || !this.thread.isAlive())) {
            this.thread = new Thread(AutoCrystal.RAutoCrystal.getInstance(this));
         }

         if (this.thread != null && this.thread.getState() == State.NEW) {
            try {
               this.thread.start();
            } catch (Exception var2) {
               var2.printStackTrace();
            }

            this.syncroTimer.reset();
         }
      }

   }

   private ScheduledExecutorService getExecutor() {
      ScheduledExecutorService service = Executors.newSingleThreadScheduledExecutor();
      service.scheduleAtFixedRate(AutoCrystal.RAutoCrystal.getInstance(this), 0L, (long)(Integer)this.threadDelay.getValue(), TimeUnit.MILLISECONDS);
      return service;
   }

   public void doAutoCrystal() {
      if (this.check()) {
         switch((AutoCrystal.Logic)this.logic.getValue()) {
         case PLACEBREAK:
            this.placeCrystal();
            if ((Boolean)this.doubleMap.getValue()) {
               this.mapCrystals();
            }

            this.breakCrystal();
            break;
         case BREAKPLACE:
            this.breakCrystal();
            this.placeCrystal();
         }

         this.manualBreaker();
      }

   }

   private boolean check() {
      if (fullNullCheck()) {
         return false;
      } else {
         if (this.syncTimer.passedMs((long)(Integer)this.damageSyncTime.getValue())) {
            this.currentSyncTarget = null;
            this.syncedCrystalPos = null;
            this.syncedPlayerPos = null;
         } else if ((Boolean)this.syncySync.getValue() && this.syncedCrystalPos != null) {
            this.posConfirmed = true;
         }

         this.foundDoublePop = false;
         if (this.renderTimer.passedMs(500L)) {
            this.renderPos = null;
            this.renderTimer.reset();
         }

         this.mainHand = Util.mc.player.getHeldItemMainhand().getItem() == Items.END_CRYSTAL;
         this.offHand = Util.mc.player.getHeldItemOffhand().getItem() == Items.END_CRYSTAL;
         this.currentDamage = 0.0D;
         this.placePos = null;
         if (this.lastSlot != Util.mc.player.inventory.currentItem || AutoTrap.isPlacing || Surround.isPlacing) {
            this.lastSlot = Util.mc.player.inventory.currentItem;
            this.switchTimer.reset();
         }

         if (this.offHand || this.mainHand) {
            this.switching = false;
         }

         if ((this.offHand || this.mainHand || this.switchMode.getValue() != AutoCrystal.Switch.BREAKSLOT || this.switching) && DamageUtil2.canBreakWeakness(Util.mc.player) && this.switchTimer.passedMs((long)(Integer)this.switchCooldown.getValue())) {
            if ((Boolean)this.mineSwitch.getValue() && Util.mc.gameSettings.keyBindAttack.isKeyDown() && (this.switching || this.autoSwitch.getValue() == AutoCrystal.AutoSwitch.ALWAYS) && Util.mc.gameSettings.keyBindUseItem.isKeyDown() && Util.mc.player.getHeldItemMainhand().getItem() instanceof ItemPickaxe) {
               this.switchItem();
            }

            this.mapCrystals();
            if (!this.posConfirmed && this.damageSync.getValue() != AutoCrystal.DamageSync.NONE && this.syncTimer.passedMs((long)(Integer)this.confirm.getValue())) {
               this.syncTimer.setMs((long)((Integer)this.damageSyncTime.getValue() + 1));
            }

            return true;
         } else {
            this.renderPos = null;
            target = null;
            this.rotating = false;
            return false;
         }
      }
   }

   private void mapCrystals() {
      this.efficientTarget = null;
      if ((Integer)this.packets.getValue() != 1) {
         this.attackList = new ConcurrentLinkedQueue();
         this.crystalMap = new HashMap();
      }

      this.crystalCount = 0;
      this.minDmgCount = 0;
      Entity maxCrystal = null;
      float maxDamage = 0.5F;
      Iterator var3 = Util.mc.world.loadedEntityList.iterator();

      Entity entity;
      while(var3.hasNext()) {
         entity = (Entity)var3.next();
         if (entity instanceof EntityEnderCrystal && this.isValid(entity)) {
            if ((Boolean)this.syncedFeetPlace.getValue() && entity.getPosition().down().equals(this.syncedCrystalPos) && this.damageSync.getValue() != AutoCrystal.DamageSync.NONE) {
               ++this.minDmgCount;
               ++this.crystalCount;
               if ((Boolean)this.syncCount.getValue()) {
                  this.minDmgCount = (Integer)this.wasteAmount.getValue() + 1;
                  this.crystalCount = (Integer)this.wasteAmount.getValue() + 1;
               }

               if ((Boolean)this.hyperSync.getValue()) {
                  maxCrystal = null;
                  break;
               }
            } else {
               boolean count = false;
               boolean countMin = false;
               float selfDamage = DamageUtil2.calculateDamage((Entity)entity, Util.mc.player);
               if ((double)selfDamage + 0.5D < (double)EntityUtil.getHealth(Util.mc.player) || !DamageUtil2.canTakeDamage((Boolean)this.suicide.getValue())) {
                  Iterator var8 = Util.mc.world.playerEntities.iterator();

                  label188:
                  while(true) {
                     while(true) {
                        EntityPlayer player;
                        float damage;
                        do {
                           do {
                              do {
                                 if (!var8.hasNext()) {
                                    break label188;
                                 }

                                 player = (EntityPlayer)var8.next();
                              } while(!(player.getDistanceSq(entity) < MathUtil.square((double)(Float)this.range.getValue())));
                           } while(!EntityUtil.isValid(player, (double)((Float)this.range.getValue() + (Float)this.breakRange.getValue())));
                        } while(!((damage = DamageUtil2.calculateDamage((Entity)entity, player)) > selfDamage) && (!(damage > (Float)this.minDamage.getValue()) || DamageUtil2.canTakeDamage((Boolean)this.suicide.getValue())) && !(damage > EntityUtil.getHealth(player)));

                        if (damage > maxDamage) {
                           maxDamage = damage;
                           maxCrystal = entity;
                        }

                        if ((Integer)this.packets.getValue() == 1) {
                           if (damage >= (Float)this.minDamage.getValue() || !(Boolean)this.wasteMinDmgCount.getValue()) {
                              count = true;
                           }

                           countMin = true;
                        } else if (this.crystalMap.get(entity) == null || (Float)this.crystalMap.get(entity) < damage) {
                           this.crystalMap.put(entity, damage);
                        }
                     }
                  }
               }

               if (countMin) {
                  ++this.minDmgCount;
                  if (count) {
                     ++this.crystalCount;
                  }
               }
            }
         }
      }

      if (this.damageSync.getValue() == AutoCrystal.DamageSync.BREAK && ((double)maxDamage > this.lastDamage || this.syncTimer.passedMs((long)(Integer)this.damageSyncTime.getValue()) || this.damageSync.getValue() == AutoCrystal.DamageSync.NONE)) {
         this.lastDamage = (double)maxDamage;
      }

      if ((Boolean)this.enormousSync.getValue() && (Boolean)this.syncedFeetPlace.getValue() && this.damageSync.getValue() != AutoCrystal.DamageSync.NONE && this.syncedCrystalPos != null) {
         if ((Boolean)this.syncCount.getValue()) {
            this.minDmgCount = (Integer)this.wasteAmount.getValue() + 1;
            this.crystalCount = (Integer)this.wasteAmount.getValue() + 1;
         }

      } else {
         if ((Boolean)this.webAttack.getValue() && this.webPos != null) {
            if (Util.mc.player.getDistanceSq(this.webPos.up()) > MathUtil.square((double)(Float)this.breakRange.getValue())) {
               this.webPos = null;
            } else {
               var3 = Util.mc.world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(this.webPos.up())).iterator();

               while(var3.hasNext()) {
                  entity = (Entity)var3.next();
                  if (entity instanceof EntityEnderCrystal) {
                     this.attackList.add(entity);
                     this.efficientTarget = entity;
                     this.webPos = null;
                     this.lastDamage = 0.5D;
                     return;
                  }
               }
            }
         }

         if ((Boolean)this.manual.getValue() && (Boolean)this.manualMinDmg.getValue() && Util.mc.gameSettings.keyBindUseItem.isKeyDown() && (this.offHand && Util.mc.player.getActiveHand() == EnumHand.OFF_HAND || this.mainHand && Util.mc.player.getActiveHand() == EnumHand.MAIN_HAND) && maxDamage < (Float)this.minDamage.getValue()) {
            this.efficientTarget = null;
         } else {
            if ((Integer)this.packets.getValue() == 1) {
               this.efficientTarget = maxCrystal;
            } else {
               this.crystalMap = MathUtil.sortByValue(this.crystalMap, true);

               for(var3 = this.crystalMap.entrySet().iterator(); var3.hasNext(); ++this.minDmgCount) {
                  Entry entry = (Entry)var3.next();
                  Entity crystal = (Entity)entry.getKey();
                  float damage = (Float)entry.getValue();
                  if (damage >= (Float)this.minDamage.getValue() || !(Boolean)this.wasteMinDmgCount.getValue()) {
                     ++this.crystalCount;
                  }

                  this.attackList.add(crystal);
               }
            }

         }
      }
   }

   private void placeCrystal() {
      int crystalLimit = (Integer)this.wasteAmount.getValue();
      if (this.placeTimer.passedMs((long)(Integer)this.placeDelay.getValue()) && (Boolean)this.place.getValue() && (this.offHand || this.mainHand || this.switchMode.getValue() == AutoCrystal.Switch.CALC || this.switchMode.getValue() == AutoCrystal.Switch.BREAKSLOT && this.switching)) {
         if ((this.offHand || this.mainHand || this.switchMode.getValue() != AutoCrystal.Switch.ALWAYS && !this.switching) && this.crystalCount >= crystalLimit && (!(Boolean)this.antiSurround.getValue() || this.lastPos == null || !this.lastPos.equals(this.placePos))) {
            return;
         }

         this.calculateDamage(this.getTarget(this.targetMode.getValue() == AutoCrystal.Target.UNSAFE));
         if (target != null && this.placePos != null) {
            if (!this.offHand && !this.mainHand && this.autoSwitch.getValue() != AutoCrystal.AutoSwitch.NONE && (this.currentDamage > (double)(Float)this.minDamage.getValue() || (Boolean)this.lethalSwitch.getValue() && EntityUtil.getHealth(target) < (Float)this.facePlace.getValue()) && !this.switchItem()) {
               return;
            }

            if (this.currentDamage < (double)(Float)this.minDamage.getValue() && (Boolean)this.limitFacePlace.getValue()) {
               crystalLimit = 1;
            }

            if ((this.offHand || this.mainHand || this.autoSwitch.getValue() != AutoCrystal.AutoSwitch.NONE) && (this.crystalCount < crystalLimit || (Boolean)this.antiSurround.getValue() && this.lastPos != null && this.lastPos.equals(this.placePos)) && (this.currentDamage > (double)(Float)this.minDamage.getValue() || this.minDmgCount < crystalLimit) && this.currentDamage >= 1.0D && (DamageUtil2.isArmorLow(target, (Integer)this.minArmor.getValue()) || EntityUtil.getHealth(target) < (Float)this.facePlace.getValue() || this.currentDamage > (double)(Float)this.minDamage.getValue())) {
               float damageOffset = this.damageSync.getValue() == AutoCrystal.DamageSync.BREAK ? (Float)this.dropOff.getValue() - 5.0F : 0.0F;
               boolean syncflag = false;
               if ((Boolean)this.syncedFeetPlace.getValue() && this.placePos.equals(this.lastPos) && !this.syncTimer.passedMs((long)(Integer)this.damageSyncTime.getValue()) && target.equals(this.currentSyncTarget) && target.getPosition().equals(this.syncedPlayerPos) && this.damageSync.getValue() != AutoCrystal.DamageSync.NONE) {
                  this.syncedCrystalPos = this.placePos;
                  this.lastDamage = this.currentDamage;
                  if ((Boolean)this.fullSync.getValue()) {
                     this.lastDamage = 100.0D;
                  }

                  syncflag = true;
               }

               if (syncflag || this.currentDamage - (double)damageOffset > this.lastDamage || this.syncTimer.passedMs((long)(Integer)this.damageSyncTime.getValue()) || this.damageSync.getValue() == AutoCrystal.DamageSync.NONE) {
                  if (!syncflag && this.damageSync.getValue() != AutoCrystal.DamageSync.BREAK) {
                     this.lastDamage = this.currentDamage;
                  }

                  this.renderPos = this.placePos;
                  this.renderDamage = this.currentDamage;
                  if (this.switchItem()) {
                     this.currentSyncTarget = target;
                     this.syncedPlayerPos = target.getPosition();
                     if (this.foundDoublePop) {
                        this.totemPops.put(target, (new Timer()).reset());
                     }

                     this.rotateToPos(this.placePos);
                     placedPos.add(this.placePos);
                     BlockUtil2.placeCrystalOnBlock(this.placePos, this.offHand ? EnumHand.OFF_HAND : EnumHand.MAIN_HAND);
                     this.lastPos = this.placePos;
                     this.placeTimer.reset();
                     this.posConfirmed = false;
                     if (this.syncTimer.passedMs((long)(Integer)this.damageSyncTime.getValue())) {
                        this.syncedCrystalPos = null;
                        this.syncTimer.reset();
                     }
                  }
               }
            }
         } else {
            this.renderPos = null;
         }
      }

   }

   private boolean switchItem() {
      if (!this.offHand && !this.mainHand) {
         switch((AutoCrystal.AutoSwitch)this.autoSwitch.getValue()) {
         case NONE:
            return false;
         case TOGGLE:
            if (!this.switching) {
               return false;
            }
         default:
            return false;
         }
      } else {
         return true;
      }
   }

   private void calculateDamage(EntityPlayer targettedPlayer) {
      if (targettedPlayer != null || this.targetMode.getValue() == AutoCrystal.Target.DAMAGE || (Boolean)this.fullCalc.getValue()) {
         float maxDamage = 0.5F;
         EntityPlayer currentTarget = null;
         BlockPos currentPos = null;
         float maxSelfDamage = 0.0F;
         this.foundDoublePop = false;
         BlockPos setToAir = null;
         IBlockState state = null;
         BlockPos playerPos;
         if ((Boolean)this.webAttack.getValue() && targettedPlayer != null && Util.mc.world.getBlockState(playerPos = new BlockPos(targettedPlayer.getPositionVector())).getBlock() == Blocks.WEB) {
            setToAir = playerPos;
            state = Util.mc.world.getBlockState(playerPos);
            Util.mc.world.setBlockToAir(playerPos);
         }

         Iterator var10 = BlockUtil2.possiblePlacePositions((Float)this.placeRange.getValue(), (Boolean)this.antiSurround.getValue(), (Boolean)this.oneDot15.getValue()).iterator();

         while(true) {
            while(true) {
               label118:
               while(true) {
                  BlockPos pos;
                  float selfDamage;
                  do {
                     do {
                        if (!var10.hasNext()) {
                           if (setToAir != null) {
                              Util.mc.world.setBlockState(setToAir, state);
                              this.webPos = currentPos;
                           }

                           target = currentTarget;
                           this.currentDamage = (double)maxDamage;
                           this.placePos = currentPos;
                           return;
                        }

                        pos = (BlockPos)var10.next();
                     } while(!BlockUtil2.rayTracePlaceCheck(pos, (this.raytrace.getValue() == AutoCrystal.Raytrace.PLACE || this.raytrace.getValue() == AutoCrystal.Raytrace.FULL) && Util.mc.player.getDistanceSq(pos) > MathUtil.square((double)(Float)this.placetrace.getValue()), 1.0F));

                     selfDamage = -1.0F;
                     if (DamageUtil2.canTakeDamage((Boolean)this.suicide.getValue())) {
                        selfDamage = DamageUtil2.calculateDamage((BlockPos)pos, Util.mc.player);
                     }
                  } while(!((double)selfDamage + 0.5D < (double)EntityUtil.getHealth(Util.mc.player)));

                  if (targettedPlayer != null) {
                     float playerDamage = DamageUtil2.calculateDamage((BlockPos)pos, targettedPlayer);
                     if (this.isDoublePoppable(targettedPlayer, playerDamage) && (currentPos == null || targettedPlayer.getDistanceSq(pos) < targettedPlayer.getDistanceSq(currentPos))) {
                        currentTarget = targettedPlayer;
                        maxDamage = playerDamage;
                        currentPos = pos;
                        this.foundDoublePop = true;
                     } else if (!this.foundDoublePop && (playerDamage > maxDamage || (Boolean)this.extraSelfCalc.getValue() && playerDamage >= maxDamage && selfDamage < maxSelfDamage) && (playerDamage > selfDamage || playerDamage > (Float)this.minDamage.getValue() && !DamageUtil2.canTakeDamage((Boolean)this.suicide.getValue()) || playerDamage > EntityUtil.getHealth(targettedPlayer))) {
                        maxDamage = playerDamage;
                        currentTarget = targettedPlayer;
                        currentPos = pos;
                        maxSelfDamage = selfDamage;
                     }
                  } else {
                     Iterator var13 = Util.mc.world.playerEntities.iterator();

                     while(true) {
                        EntityPlayer player;
                        float playerDamage;
                        do {
                           do {
                              do {
                                 if (!var13.hasNext()) {
                                    continue label118;
                                 }

                                 player = (EntityPlayer)var13.next();
                              } while(!EntityUtil.isValid(player, (double)((Float)this.placeRange.getValue() + (Float)this.range.getValue())));
                           } while(!((playerDamage = DamageUtil2.calculateDamage((BlockPos)pos, player)) > maxDamage) && (!(Boolean)this.extraSelfCalc.getValue() || !(playerDamage >= maxDamage) || !(selfDamage < maxSelfDamage)));
                        } while(!(playerDamage > selfDamage) && (!(playerDamage > (Float)this.minDamage.getValue()) || DamageUtil2.canTakeDamage((Boolean)this.suicide.getValue())) && !(playerDamage > EntityUtil.getHealth(player)));

                        maxDamage = playerDamage;
                        currentTarget = player;
                        currentPos = pos;
                        maxSelfDamage = selfDamage;
                     }
                  }
               }
            }
         }
      }
   }

   private EntityPlayer getTarget(boolean unsafe) {
      if (this.targetMode.getValue() == AutoCrystal.Target.DAMAGE) {
         return null;
      } else {
         EntityPlayer currentTarget = null;
         Iterator var3 = Util.mc.world.playerEntities.iterator();

         while(var3.hasNext()) {
            EntityPlayer player = (EntityPlayer)var3.next();
            if (!EntityUtil.isntValid(player, (double)((Float)this.placeRange.getValue() + (Float)this.range.getValue())) && (!unsafe || !EntityUtil.isSafe(player))) {
               if ((Integer)this.minArmor.getValue() > 0 && DamageUtil2.isArmorLow(player, (Integer)this.minArmor.getValue())) {
                  currentTarget = player;
                  break;
               }

               if (currentTarget == null) {
                  currentTarget = player;
               } else if (Util.mc.player.getDistanceSq(player) < Util.mc.player.getDistanceSq(currentTarget)) {
                  currentTarget = player;
               }
            }
         }

         return unsafe && currentTarget == null ? this.getTarget(false) : currentTarget;
      }
   }

   private void breakCrystal() {
      if ((Boolean)this.explode.getValue() && this.breakTimer.passedMs((long)(Integer)this.breakDelay.getValue()) && (this.switchMode.getValue() == AutoCrystal.Switch.ALWAYS || this.mainHand || this.offHand)) {
         if ((Integer)this.packets.getValue() == 1 && this.efficientTarget != null) {
            if ((Boolean)this.syncedFeetPlace.getValue() && (Boolean)this.gigaSync.getValue() && this.syncedCrystalPos != null && this.damageSync.getValue() != AutoCrystal.DamageSync.NONE) {
               return;
            }

            this.rotateTo(this.efficientTarget);
            EntityUtil.attackEntity(this.efficientTarget, (Boolean)this.sync.getValue(), true);
            brokenPos.add((new BlockPos(this.efficientTarget.getPositionVector())).down());
         } else if (!this.attackList.isEmpty()) {
            if ((Boolean)this.syncedFeetPlace.getValue() && (Boolean)this.gigaSync.getValue() && this.syncedCrystalPos != null && this.damageSync.getValue() != AutoCrystal.DamageSync.NONE) {
               return;
            }

            for(int i = 0; i < (Integer)this.packets.getValue(); ++i) {
               Entity entity = (Entity)this.attackList.poll();
               if (entity != null) {
                  this.rotateTo(entity);
                  EntityUtil.attackEntity(entity, (Boolean)this.sync.getValue(), true);
                  brokenPos.add((new BlockPos(entity.getPositionVector())).down());
               }
            }
         }

         this.breakTimer.reset();
      }

   }

   private void manualBreaker() {
      if (this.rotate.getValue() != AutoCrystal.Rotate.OFF && (Integer)this.eventMode.getValue() != 2 && this.rotating) {
         if (this.didRotation) {
            Util.mc.player.rotationPitch = (float)((double)Util.mc.player.rotationPitch + 4.0E-4D);
            this.didRotation = false;
         } else {
            Util.mc.player.rotationPitch = (float)((double)Util.mc.player.rotationPitch - 4.0E-4D);
            this.didRotation = true;
         }
      }

      RayTraceResult result;
      if ((this.offHand || this.mainHand) && (Boolean)this.manual.getValue() && this.manualTimer.passedMs((long)(Integer)this.manualBreak.getValue()) && Util.mc.gameSettings.keyBindUseItem.isKeyDown() && Util.mc.player.getHeldItemOffhand().getItem() != Items.GOLDEN_APPLE && Util.mc.player.inventory.getCurrentItem().getItem() != Items.GOLDEN_APPLE && Util.mc.player.inventory.getCurrentItem().getItem() != Items.BOW && Util.mc.player.inventory.getCurrentItem().getItem() != Items.EXPERIENCE_BOTTLE && (result = Util.mc.objectMouseOver) != null) {
         switch(result.typeOfHit) {
         case ENTITY:
            Entity entity = result.entityHit;
            if (entity instanceof EntityEnderCrystal) {
               EntityUtil.attackEntity(entity, (Boolean)this.sync.getValue(), true);
               this.manualTimer.reset();
            }
            break;
         case BLOCK:
            BlockPos mousePos = Util.mc.objectMouseOver.getBlockPos().up();
            Iterator var3 = Util.mc.world.getEntitiesWithinAABBExcludingEntity((Entity)null, new AxisAlignedBB(mousePos)).iterator();

            while(var3.hasNext()) {
               Entity target = (Entity)var3.next();
               if (target instanceof EntityEnderCrystal) {
                  EntityUtil.attackEntity(target, (Boolean)this.sync.getValue(), true);
                  this.manualTimer.reset();
               }
            }
         }
      }

   }

   private void rotateTo(Entity entity) {
      switch((AutoCrystal.Rotate)this.rotate.getValue()) {
      case OFF:
         this.rotating = false;
      case PLACE:
      default:
         break;
      case BREAK:
      case ALL:
         float[] angle = MathUtil.calcAngle(Util.mc.player.getPositionEyes(Util.mc.getRenderPartialTicks()), entity.getPositionVector());
         if ((Integer)this.eventMode.getValue() == 2) {
            Charon.rotationManager.setPlayerRotations(angle[0], angle[1]);
         } else {
            this.yaw = angle[0];
            this.pitch = angle[1];
            this.rotating = true;
         }
      }

   }

   private void rotateToPos(BlockPos pos) {
      switch((AutoCrystal.Rotate)this.rotate.getValue()) {
      case OFF:
         this.rotating = false;
         break;
      case PLACE:
      case ALL:
         float[] angle = MathUtil.calcAngle(Util.mc.player.getPositionEyes(Util.mc.getRenderPartialTicks()), new Vec3d((double)((float)pos.getX() + 0.5F), (double)((float)pos.getY() - 0.5F), (double)((float)pos.getZ() + 0.5F)));
         if ((Integer)this.eventMode.getValue() == 2) {
            Charon.rotationManager.setPlayerRotations(angle[0], angle[1]);
         } else {
            this.yaw = angle[0];
            this.pitch = angle[1];
            this.rotating = true;
         }
      case BREAK:
      }

   }

   private boolean isDoublePoppable(EntityPlayer player, float damage) {
      float health;
      if ((Boolean)this.doublePop.getValue() && (double)(health = EntityUtil.getHealth(player)) <= 1.0D && (double)damage > (double)health + 0.5D && damage <= (Float)this.popDamage.getValue()) {
         Timer timer = (Timer)this.totemPops.get(player);
         return timer == null || timer.passedMs((long)(Integer)this.popTime.getValue());
      } else {
         return false;
      }
   }

   private boolean isValid(Entity entity) {
      return entity != null && Util.mc.player.getDistanceSq(entity) <= MathUtil.square((double)(Float)this.breakRange.getValue()) && (this.raytrace.getValue() == AutoCrystal.Raytrace.NONE || this.raytrace.getValue() == AutoCrystal.Raytrace.PLACE || Util.mc.player.canEntityBeSeen(entity) || !Util.mc.player.canEntityBeSeen(entity) && Util.mc.player.getDistanceSq(entity) <= MathUtil.square((double)(Float)this.breaktrace.getValue()));
   }

   private static class RAutoCrystal implements Runnable {
      private static AutoCrystal.RAutoCrystal instance;
      private AutoCrystal autoCrystal;

      public static AutoCrystal.RAutoCrystal getInstance(AutoCrystal autoCrystal) {
         if (instance == null) {
            instance = new AutoCrystal.RAutoCrystal();
         }

         instance.autoCrystal = autoCrystal;
         return instance;
      }

      public void run() {
         if (this.autoCrystal.threadMode.getValue() == AutoCrystal.ThreadMode.POOL) {
            if (this.autoCrystal.isOn()) {
               this.autoCrystal.doAutoCrystal();
            }
         } else if (this.autoCrystal.threadMode.getValue() == AutoCrystal.ThreadMode.WHILE) {
            while(this.autoCrystal.isOn() && this.autoCrystal.threadMode.getValue() == AutoCrystal.ThreadMode.WHILE) {
               if (this.autoCrystal.shouldInterrupt.get()) {
                  this.autoCrystal.shouldInterrupt.set(false);
                  this.autoCrystal.syncroTimer.reset();
                  this.autoCrystal.thread.interrupt();
                  break;
               }

               this.autoCrystal.doAutoCrystal();

               try {
                  Thread.sleep((long)(Integer)this.autoCrystal.threadDelay.getValue());
               } catch (InterruptedException var2) {
                  this.autoCrystal.thread.interrupt();
                  var2.printStackTrace();
               }
            }
         }

      }
   }

   public static enum ThreadMode {
      NONE,
      WHILE,
      POOL;
   }

   public static enum DamageSync {
      NONE,
      PLACE,
      BREAK;
   }

   public static enum Logic {
      BREAKPLACE,
      PLACEBREAK;
   }

   public static enum Rotate {
      OFF,
      PLACE,
      BREAK,
      ALL;
   }

   public static enum AutoSwitch {
      NONE,
      TOGGLE,
      ALWAYS;
   }

   public static enum Target {
      CLOSEST,
      UNSAFE,
      DAMAGE;
   }

   public static enum Switch {
      ALWAYS,
      BREAKSLOT,
      CALC;
   }

   public static enum Raytrace {
      NONE,
      PLACE,
      BREAK,
      FULL;
   }

   public static enum Settings {
      PLACE,
      BREAK,
      RENDER,
      MISC,
      DEV;
   }
}
