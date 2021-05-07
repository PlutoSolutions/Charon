package cc.zip.charon.features.modules.autocrystal;

import cc.zip.charon.features.modules.Module;
import cc.zip.charon.features.setting.Setting;
import cc.zip.charon.util.Timer;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.math.BlockPos;

public class WurstplusAutoCrystal extends Module {
   public Setting<Boolean> debug = this.register(new Setting("debug", false));
   public Setting<Boolean> place_crystal = this.register(new Setting("debug", true));
   public Setting<Boolean> break_crystal = this.register(new Setting("debug", true));
   private final Setting<Integer> break_trys = this.register(new Setting("BlocksPerTick", 8, 1, 30));
   public Setting<Boolean> anti_weakness = this.register(new Setting("debug", true));
   private final Setting<Integer> enemyRange = this.register(new Setting("BlocksPerTick", 8, 1, 30));
   private final Setting<Integer> hit_range = this.register(new Setting("BlocksPerTick", 8, 1, 30));
   private final Setting<Integer> place_range = this.register(new Setting("BlocksPerTick", 8, 1, 30));
   private final Setting<Integer> hit_range_wall = this.register(new Setting("BlocksPerTick", 8, 1, 30));
   private final Setting<Integer> wallPlaceRange = this.register(new Setting("BlocksPerTick", 8, 1, 30));
   private final Setting<Integer> place_delay = this.register(new Setting("BlocksPerTick", 8, 1, 30));
   private final Setting<Integer> break_delay = this.register(new Setting("BlocksPerTick", 8, 1, 30));
   private final Setting<Integer> min_player_place = this.register(new Setting("BlocksPerTick", 8, 1, 30));
   private final Setting<Integer> min_player_break = this.register(new Setting("BlocksPerTick", 8, 1, 30));
   private final Setting<Integer> max_self_damage = this.register(new Setting("BlocksPerTick", 8, 1, 30));
   public Setting<WurstplusAutoCrystal.RotateMode> rotate_mode;
   public Setting<Boolean> raytrace;
   public Setting<Boolean> auto_switch;
   public Setting<Boolean> anti_suicide;
   public Setting<Boolean> client_side;
   public Setting<Boolean> jumpy_mode;
   public Setting<Boolean> anti_stuck;
   private final Setting<Integer> antiStuckTries;
   public Setting<Boolean> endcrystal;
   public Setting<Boolean> faceplace_mode;
   private final Setting<Integer> faceplace_mode_damage;
   public Setting<Boolean> fuck_armor_mode;
   private final Setting<Integer> fuck_armor_mode_precent;
   public Setting<Boolean> stop_while_mining;
   public Setting<Boolean> faceplace_check;
   public Setting<WurstplusAutoCrystal.SwingMode> swing;
   public Setting<WurstplusAutoCrystal.SwingMode> render_mode;
   public Setting<Boolean> old_render;
   public Setting<Boolean> future_render;
   public Setting<Boolean> top_block;
   private final Setting<Integer> r;
   private final Setting<Integer> g;
   private final Setting<Integer> b;
   private final Setting<Integer> a;
   private final Setting<Integer> a_out;
   public Setting<Boolean> rainbow_mode;
   public Setting<Boolean> sat;
   private final Setting<Integer> brightness;
   private final Setting<Integer> height;
   public Setting<Boolean> render_damage;
   private final ConcurrentHashMap<EntityEnderCrystal, Integer> attacked_crystals;
   private final List<BlockPos> placePosList;
   private final Timer remove_visual_timer;
   private EntityPlayer autoez_target;
   private String detail_name;
   private int detail_hp;
   private BlockPos render_block_init;
   private BlockPos render_block_old;
   private double render_damage_value;
   private float yaw;
   private float pitch;
   private boolean already_attacking;
   private boolean is_rotating;
   private boolean did_anything;
   private boolean outline;
   private boolean solid;
   private int place_timeout;
   private int break_timeout;
   private int break_delay_counter;
   private int place_delay_counter;

   public WurstplusAutoCrystal() {
      super("WurstplusAutoCrystal", "?", Module.Category.MISC, true, false, false);
      this.rotate_mode = this.register(new Setting("Rotate", WurstplusAutoCrystal.RotateMode.Off));
      this.raytrace = this.register(new Setting("debug", true));
      this.auto_switch = this.register(new Setting("debug", true));
      this.anti_suicide = this.register(new Setting("debug", true));
      this.client_side = this.register(new Setting("debug", false));
      this.jumpy_mode = this.register(new Setting("debug", true));
      this.anti_stuck = this.register(new Setting("debug", true));
      this.antiStuckTries = this.register(new Setting("BlocksPerTick", 8, 1, 30));
      this.endcrystal = this.register(new Setting("debug", true));
      this.faceplace_mode = this.register(new Setting("debug", true));
      this.faceplace_mode_damage = this.register(new Setting("BlocksPerTick", 8, 1, 30));
      this.fuck_armor_mode = this.register(new Setting("debug", true));
      this.fuck_armor_mode_precent = this.register(new Setting("BlocksPerTick", 8, 1, 30));
      this.stop_while_mining = this.register(new Setting("debug", true));
      this.faceplace_check = this.register(new Setting("debug", true));
      this.swing = this.register(new Setting("Swing", WurstplusAutoCrystal.SwingMode.None));
      this.render_mode = this.register(new Setting("Swing", WurstplusAutoCrystal.SwingMode.None));
      this.old_render = this.register(new Setting("debug", true));
      this.future_render = this.register(new Setting("debug", true));
      this.top_block = this.register(new Setting("debug", true));
      this.r = this.register(new Setting("Red", 0, 0, 255));
      this.g = this.register(new Setting("Green", 255, 0, 255));
      this.b = this.register(new Setting("Blue", 0, 0, 255));
      this.a = this.register(new Setting("Alpha", 255, 0, 255));
      this.a_out = this.register(new Setting("Alpha", 255, 0, 255));
      this.rainbow_mode = this.register(new Setting("debug", true));
      this.sat = this.register(new Setting("debug", true));
      this.brightness = this.register(new Setting("BlocksPerTick", 8, 1, 30));
      this.height = this.register(new Setting("BlocksPerTick", 8, 1, 30));
      this.render_damage = this.register(new Setting("debug", true));
      this.attacked_crystals = new ConcurrentHashMap();
      this.placePosList = new CopyOnWriteArrayList();
      this.remove_visual_timer = new Timer();
      this.autoez_target = null;
      this.detail_name = null;
      this.detail_hp = 0;
      this.already_attacking = false;
   }

   public static enum RotateMode {
      Off,
      Old,
      Const,
      God;
   }

   public static enum RenderMode {
      Pretty,
      Solid,
      Outline,
      None;
   }

   public static enum SwingMode {
      Mainhand,
      Offhand,
      Both,
      None;
   }
}
