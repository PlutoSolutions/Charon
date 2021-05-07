package cc.zip.charon.features.modules.combat;

import cc.zip.charon.Charon;
import cc.zip.charon.event.events.PacketEvent;
import cc.zip.charon.event.events.Render3DEvent;
import cc.zip.charon.features.modules.Module;
import cc.zip.charon.features.modules.client.ClickGui;
import cc.zip.charon.features.modules.misc.AutoGG;
import cc.zip.charon.features.setting.Setting;
import cc.zip.charon.util.ColorUtil;
import cc.zip.charon.util.EntityUtil;
import cc.zip.charon.util.MathUtil;
import cc.zip.charon.util.RenderUtil;
import cc.zip.charon.util.Timer;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.SharedMonsterAttributes;
import net.minecraft.entity.item.EntityEnderCrystal;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.init.MobEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.network.play.client.CPacketPlayer;
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock;
import net.minecraft.network.play.client.CPacketUseEntity;
import net.minecraft.network.play.client.CPacketUseEntity.Action;
import net.minecraft.network.play.server.SPacketSpawnObject;
import net.minecraft.util.CombatRules;
import net.minecraft.util.DamageSource;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.NonNullList;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.math.RayTraceResult.Type;
import net.minecraft.world.Explosion;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class AutoCrystalDied extends Module {
   private final Timer placeTimer = new Timer();
   private final Timer breakTimer = new Timer();
   private final Timer preditTimer = new Timer();
   private final Timer manualTimer = new Timer();
   private final Setting<Integer> attackFactor = this.register(new Setting("PredictDelay", 0, 0, 200));
   private final Setting<Integer> red = this.register(new Setting("Red", 0, 0, 255));
   private final Setting<Integer> green = this.register(new Setting("Green", 255, 0, 255));
   private final Setting<Integer> blue = this.register(new Setting("Blue", 0, 0, 255));
   private final Setting<Integer> alpha = this.register(new Setting("Alpha", 255, 0, 255));
   private final Setting<Integer> boxAlpha = this.register(new Setting("BoxAlpha", 125, 0, 255));
   private final Setting<Float> lineWidth = this.register(new Setting("LineWidth", 1.0F, 0.1F, 5.0F));
   public Setting<Boolean> place = this.register(new Setting("Place", true));
   public Setting<Float> placeDelay = this.register(new Setting("PlaceDelay", 4.0F, 0.0F, 300.0F));
   public Setting<Float> placeRange = this.register(new Setting("PlaceRange", 4.0F, 0.1F, 7.0F));
   public Setting<Boolean> explode = this.register(new Setting("Break", true));
   public Setting<Boolean> packetBreak = this.register(new Setting("PacketBreak", true));
   public Setting<Boolean> predicts = this.register(new Setting("Predict", true));
   public Setting<Boolean> rotate = this.register(new Setting("Rotate", true));
   public Setting<Float> breakDelay = this.register(new Setting("BreakDelay", 4.0F, 0.0F, 300.0F));
   public Setting<Float> breakRange = this.register(new Setting("BreakRange", 4.0F, 0.1F, 7.0F));
   public Setting<Float> breakWallRange = this.register(new Setting("BreakWallRange", 4.0F, 0.1F, 7.0F));
   public Setting<Boolean> opPlace = this.register(new Setting("1.13 Place", true));
   public Setting<Boolean> suicide = this.register(new Setting("AntiSuicide", true));
   public Setting<Boolean> autoswitch = this.register(new Setting("AutoSwitch", true));
   public Setting<Boolean> ignoreUseAmount = this.register(new Setting("IgnoreUseAmount", true));
   public Setting<Integer> wasteAmount = this.register(new Setting("UseAmount", 4, 1, 5));
   public Setting<Boolean> facePlaceSword = this.register(new Setting("FacePlaceSword", true));
   public Setting<Float> targetRange = this.register(new Setting("TargetRange", 4.0F, 1.0F, 12.0F));
   public Setting<Float> minDamage = this.register(new Setting("MinDamage", 4.0F, 0.1F, 20.0F));
   public Setting<Float> facePlace = this.register(new Setting("FacePlaceHP", 4.0F, 0.0F, 36.0F));
   public Setting<Float> breakMaxSelfDamage = this.register(new Setting("BreakMaxSelf", 4.0F, 0.1F, 12.0F));
   public Setting<Float> breakMinDmg = this.register(new Setting("BreakMinDmg", 4.0F, 0.1F, 7.0F));
   public Setting<Float> minArmor = this.register(new Setting("MinArmor", 4.0F, 0.1F, 80.0F));
   public Setting<AutoCrystalDied.SwingMode> swingMode;
   public Setting<Boolean> render;
   public Setting<Boolean> renderDmg;
   public Setting<Boolean> box;
   public Setting<Boolean> outline;
   private final Setting<Integer> cRed;
   private final Setting<Integer> cGreen;
   private final Setting<Integer> cBlue;
   private final Setting<Integer> cAlpha;
   EntityEnderCrystal crystal;
   private EntityLivingBase target;
   private BlockPos pos;
   private int hotBarSlot;
   private boolean armor;
   private boolean armorTarget;
   private int crystalCount;
   private int predictWait;
   private int predictPackets;
   private boolean packetCalc;
   private float yaw;
   private EntityLivingBase realTarget;
   private int predict;
   private float pitch;
   private boolean rotating;

   public AutoCrystalDied() {
      super("AutoCrystalOyv", "NiggaHack ac best ac", Module.Category.COMBAT, true, false, false);
      this.swingMode = this.register(new Setting("Swing", AutoCrystalDied.SwingMode.MainHand));
      this.render = this.register(new Setting("Render", true));
      this.renderDmg = this.register(new Setting("RenderDmg", true));
      this.box = this.register(new Setting("Box", true));
      this.outline = this.register(new Setting("Outline", true));
      this.cRed = this.register(new Setting("OL-Red", 0, 0, 255, (v) -> {
         return (Boolean)this.outline.getValue();
      }));
      this.cGreen = this.register(new Setting("OL-Green", 0, 0, 255, (v) -> {
         return (Boolean)this.outline.getValue();
      }));
      this.cBlue = this.register(new Setting("OL-Blue", 255, 0, 255, (v) -> {
         return (Boolean)this.outline.getValue();
      }));
      this.cAlpha = this.register(new Setting("OL-Alpha", 255, 0, 255, (v) -> {
         return (Boolean)this.outline.getValue();
      }));
      this.yaw = 0.0F;
      this.pitch = 0.0F;
      this.rotating = false;
   }

   public static List<BlockPos> getSphere(BlockPos loc, float r, int h, boolean hollow, boolean sphere, int plus_y) {
      ArrayList<BlockPos> circleblocks = new ArrayList();
      int cx = loc.getX();
      int cy = loc.getY();
      int cz = loc.getZ();

      for(int x = cx - (int)r; (float)x <= (float)cx + r; ++x) {
         for(int z = cz - (int)r; (float)z <= (float)cz + r; ++z) {
            int y = sphere ? cy - (int)r : cy;

            while(true) {
               float f = sphere ? (float)cy + r : (float)(cy + h);
               if (!((float)y < f)) {
                  break;
               }

               double dist = (double)((cx - x) * (cx - x) + (cz - z) * (cz - z) + (sphere ? (cy - y) * (cy - y) : 0));
               if (dist < (double)(r * r) && (!hollow || !(dist < (double)((r - 1.0F) * (r - 1.0F))))) {
                  BlockPos l = new BlockPos(x, y + plus_y, z);
                  circleblocks.add(l);
               }

               ++y;
            }
         }
      }

      return circleblocks;
   }

   @SubscribeEvent
   public void onPacketSend(PacketEvent.Send event) {
      if (event.getStage() == 0 && (Boolean)this.rotate.getValue() && this.rotating && event.getPacket() instanceof CPacketPlayer) {
         CPacketPlayer packet = (CPacketPlayer)event.getPacket();
         packet.yaw = this.yaw;
         packet.pitch = this.pitch;
         this.rotating = false;
      }

   }

   private void rotateTo(Entity entity) {
      if ((Boolean)this.rotate.getValue()) {
         float[] angle = MathUtil.calcAngle(mc.player.getPositionEyes(mc.getRenderPartialTicks()), entity.getPositionVector());
         this.yaw = angle[0];
         this.pitch = angle[1];
         this.rotating = true;
      }

   }

   private void rotateToPos(BlockPos pos) {
      if ((Boolean)this.rotate.getValue()) {
         float[] angle = MathUtil.calcAngle(mc.player.getPositionEyes(mc.getRenderPartialTicks()), new Vec3d((double)((float)pos.getX() + 0.5F), (double)((float)pos.getY() - 0.5F), (double)((float)pos.getZ() + 0.5F)));
         this.yaw = angle[0];
         this.pitch = angle[1];
         this.rotating = true;
      }

   }

   public void onEnable() {
      this.placeTimer.reset();
      this.breakTimer.reset();
      this.predictWait = 0;
      this.hotBarSlot = -1;
      this.pos = null;
      this.crystal = null;
      this.predict = 0;
      this.predictPackets = 1;
      this.target = null;
      this.packetCalc = false;
      this.realTarget = null;
      this.armor = false;
      this.armorTarget = false;
   }

   public void onDisable() {
      this.rotating = false;
   }

   public void onTick() {
      this.onCrystal();
   }

   public String getDisplayInfo() {
      return this.realTarget != null ? this.realTarget.getName() : null;
   }

   public void onCrystal() {
      if (mc.world != null && mc.player != null) {
         this.realTarget = null;
         this.manualBreaker();
         this.crystalCount = 0;
         if (!(Boolean)this.ignoreUseAmount.getValue()) {
            Iterator var1 = mc.world.loadedEntityList.iterator();

            while(var1.hasNext()) {
               Entity crystal = (Entity)var1.next();
               if (crystal instanceof EntityEnderCrystal && this.IsValidCrystal(crystal)) {
                  boolean count = false;
                  double damage = (double)this.calculateDamage((double)this.target.getPosition().getX() + 0.5D, (double)this.target.getPosition().getY() + 1.0D, (double)this.target.getPosition().getZ() + 0.5D, this.target);
                  if (damage >= (double)(Float)this.minDamage.getValue()) {
                     count = true;
                  }

                  if (count) {
                     ++this.crystalCount;
                  }
               }
            }
         }

         this.hotBarSlot = -1;
         int crystalLimit;
         if (mc.player.getHeldItemOffhand().getItem() != Items.END_CRYSTAL) {
            int crystalSlot = mc.player.getHeldItemMainhand().getItem() == Items.END_CRYSTAL ? mc.player.inventory.currentItem : -1;
            if (crystalSlot == -1) {
               for(crystalLimit = 0; crystalLimit < 9; ++crystalLimit) {
                  if (mc.player.inventory.getStackInSlot(crystalLimit).getItem() == Items.END_CRYSTAL) {
                     crystalSlot = crystalLimit;
                     this.hotBarSlot = crystalLimit;
                     break;
                  }
               }
            }

            if (crystalSlot == -1) {
               this.pos = null;
               this.target = null;
               return;
            }
         }

         if (mc.player.getHeldItemOffhand().getItem() == Items.GOLDEN_APPLE && mc.player.getHeldItemMainhand().getItem() != Items.END_CRYSTAL) {
            this.pos = null;
            this.target = null;
         } else {
            if (this.target == null) {
               this.target = this.getTarget();
            }

            if (this.target == null) {
               this.crystal = null;
            } else {
               if (this.target.getDistance(mc.player) > 12.0F) {
                  this.crystal = null;
                  this.target = null;
               }

               this.crystal = (EntityEnderCrystal)mc.world.loadedEntityList.stream().filter(this::IsValidCrystal).map((p_Entity) -> {
                  return (EntityEnderCrystal)p_Entity;
               }).min(Comparator.comparing((p_Entity) -> {
                  return this.target.getDistance(p_Entity);
               })).orElse(null);
               if (this.crystal != null && (Boolean)this.explode.getValue() && this.breakTimer.passedMs(((Float)this.breakDelay.getValue()).longValue())) {
                  this.breakTimer.reset();
                  if ((Boolean)this.packetBreak.getValue()) {
                     this.rotateTo(this.crystal);
                     mc.player.connection.sendPacket(new CPacketUseEntity(this.crystal));
                  } else {
                     this.rotateTo(this.crystal);
                     mc.playerController.attackEntity(mc.player, this.crystal);
                  }

                  if (this.swingMode.getValue() == AutoCrystalDied.SwingMode.MainHand) {
                     mc.player.swingArm(EnumHand.MAIN_HAND);
                  } else if (this.swingMode.getValue() == AutoCrystalDied.SwingMode.OffHand) {
                     mc.player.swingArm(EnumHand.OFF_HAND);
                  }
               }

               if (this.placeTimer.passedMs(((Float)this.placeDelay.getValue()).longValue()) && (Boolean)this.place.getValue()) {
                  this.placeTimer.reset();
                  double damage = 0.5D;
                  Iterator var20 = this.placePostions((Float)this.placeRange.getValue()).iterator();

                  while(true) {
                     double selfDmg;
                     double targetDmg;
                     BlockPos blockPos;
                     do {
                        while(true) {
                           do {
                              do {
                                 do {
                                    do {
                                       do {
                                          do {
                                             if (!var20.hasNext()) {
                                                if (damage == 0.5D) {
                                                   this.pos = null;
                                                   this.target = null;
                                                   this.realTarget = null;
                                                   return;
                                                }

                                                this.realTarget = this.target;
                                                if (AutoGG.getINSTANCE().isOn()) {
                                                   AutoGG autoGG = (AutoGG)Charon.moduleManager.getModuleByName("AutoGG");
                                                   autoGG.addTargetedPlayer(this.target.getName());
                                                }

                                                if (this.hotBarSlot != -1 && (Boolean)this.autoswitch.getValue() && !mc.player.isPotionActive(MobEffects.WEAKNESS)) {
                                                   mc.player.inventory.currentItem = this.hotBarSlot;
                                                }

                                                if (!(Boolean)this.ignoreUseAmount.getValue()) {
                                                   crystalLimit = (Integer)this.wasteAmount.getValue();
                                                   if (this.crystalCount >= crystalLimit) {
                                                      return;
                                                   }

                                                   if (damage < (double)(Float)this.minDamage.getValue()) {
                                                      crystalLimit = 1;
                                                   }

                                                   if (this.crystalCount < crystalLimit && this.pos != null) {
                                                      this.rotateToPos(this.pos);
                                                      mc.player.connection.sendPacket(new CPacketPlayerTryUseItemOnBlock(this.pos, EnumFacing.UP, mc.player.getHeldItemOffhand().getItem() == Items.END_CRYSTAL ? EnumHand.OFF_HAND : EnumHand.MAIN_HAND, 0.0F, 0.0F, 0.0F));
                                                   }
                                                } else if (this.pos != null) {
                                                   this.rotateToPos(this.pos);
                                                   mc.player.connection.sendPacket(new CPacketPlayerTryUseItemOnBlock(this.pos, EnumFacing.UP, mc.player.getHeldItemOffhand().getItem() == Items.END_CRYSTAL ? EnumHand.OFF_HAND : EnumHand.MAIN_HAND, 0.0F, 0.0F, 0.0F));
                                                }

                                                return;
                                             }

                                             blockPos = (BlockPos)var20.next();
                                          } while(blockPos == null);
                                       } while(this.target == null);
                                    } while(!mc.world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(blockPos)).isEmpty());
                                 } while(this.target.getDistance((double)blockPos.getX(), (double)blockPos.getY(), (double)blockPos.getZ()) > (double)(Float)this.targetRange.getValue());
                              } while(this.target.isDead);
                           } while(this.target.getHealth() + this.target.getAbsorptionAmount() <= 0.0F);

                           targetDmg = (double)this.calculateDamage((double)blockPos.getX() + 0.5D, (double)blockPos.getY() + 1.0D, (double)blockPos.getZ() + 0.5D, this.target);
                           this.armor = false;
                           Iterator var11 = this.target.getArmorInventoryList().iterator();

                           while(var11.hasNext()) {
                              ItemStack is = (ItemStack)var11.next();
                              float green = ((float)is.getMaxDamage() - (float)is.getItemDamage()) / (float)is.getMaxDamage();
                              float red = 1.0F - green;
                              int dmg = 100 - (int)(red * 100.0F);
                              if ((float)dmg <= (Float)this.minArmor.getValue()) {
                                 this.armor = true;
                              }
                           }

                           if (!(targetDmg < (double)(Float)this.minDamage.getValue())) {
                              break;
                           }

                           if ((Boolean)this.facePlaceSword.getValue()) {
                              if (!(this.target.getAbsorptionAmount() + this.target.getHealth() > (Float)this.facePlace.getValue())) {
                                 break;
                              }
                           } else if (!(mc.player.getHeldItemMainhand().getItem() instanceof ItemSword) && !(this.target.getAbsorptionAmount() + this.target.getHealth() > (Float)this.facePlace.getValue())) {
                              break;
                           }

                           if ((Boolean)this.facePlaceSword.getValue()) {
                              if (!this.armor) {
                                 continue;
                              }
                              break;
                           } else if (!(mc.player.getHeldItemMainhand().getItem() instanceof ItemSword) && this.armor) {
                              break;
                           }
                        }
                     } while((selfDmg = (double)this.calculateDamage((double)blockPos.getX() + 0.5D, (double)blockPos.getY() + 1.0D, (double)blockPos.getZ() + 0.5D, mc.player)) + ((Boolean)this.suicide.getValue() ? 2.0D : 0.5D) >= (double)(mc.player.getHealth() + mc.player.getAbsorptionAmount()) && selfDmg >= targetDmg && targetDmg < (double)(this.target.getHealth() + this.target.getAbsorptionAmount()));

                     if (damage < targetDmg) {
                        this.pos = blockPos;
                        damage = targetDmg;
                     }
                  }
               }
            }
         }
      }
   }

   @SubscribeEvent(
      priority = EventPriority.HIGHEST,
      receiveCanceled = true
   )
   public void onPacketReceive(PacketEvent.Receive event) {
      SPacketSpawnObject packet;
      if (event.getPacket() instanceof SPacketSpawnObject && (packet = (SPacketSpawnObject)event.getPacket()).getType() == 51 && (Boolean)this.predicts.getValue() && this.preditTimer.passedMs(((Integer)this.attackFactor.getValue()).longValue()) && (Boolean)this.predicts.getValue() && (Boolean)this.explode.getValue() && (Boolean)this.packetBreak.getValue() && this.target != null) {
         if (!this.isPredicting(packet)) {
            return;
         }

         CPacketUseEntity predict = new CPacketUseEntity();
         predict.entityId = packet.getEntityID();
         predict.action = Action.ATTACK;
         mc.player.connection.sendPacket(predict);
      }

   }

   public void onRender3D(Render3DEvent event) {
      if (this.pos != null && (Boolean)this.render.getValue() && this.target != null) {
         RenderUtil.drawBoxESP(this.pos, (Boolean)ClickGui.getInstance().rainbow.getValue() ? ColorUtil.rainbow((Integer)ClickGui.getInstance().rainbowHue.getValue()) : new Color((Integer)this.red.getValue(), (Integer)this.green.getValue(), (Integer)this.blue.getValue(), (Integer)this.alpha.getValue()), (Boolean)this.outline.getValue(), (Boolean)ClickGui.getInstance().rainbow.getValue() ? ColorUtil.rainbow((Integer)ClickGui.getInstance().rainbowHue.getValue()) : new Color((Integer)this.cRed.getValue(), (Integer)this.cGreen.getValue(), (Integer)this.cBlue.getValue(), (Integer)this.cAlpha.getValue()), (Float)this.lineWidth.getValue(), (Boolean)this.outline.getValue(), (Boolean)this.box.getValue(), (Integer)this.boxAlpha.getValue(), true);
         if ((Boolean)this.renderDmg.getValue()) {
            double renderDamage = (double)this.calculateDamage((double)this.pos.getX() + 0.5D, (double)this.pos.getY() + 1.0D, (double)this.pos.getZ() + 0.5D, this.target);
            RenderUtil.drawText(this.pos, (Math.floor(renderDamage) == renderDamage ? (int)renderDamage : String.format("%.1f", renderDamage)) + "");
         }
      }

   }

   private boolean isPredicting(SPacketSpawnObject packet) {
      BlockPos packPos = new BlockPos(packet.getX(), packet.getY(), packet.getZ());
      if (mc.player.getDistance(packet.getX(), packet.getY(), packet.getZ()) > (double)(Float)this.breakRange.getValue()) {
         return false;
      } else if (!this.canSeePos(packPos) && mc.player.getDistance(packet.getX(), packet.getY(), packet.getZ()) > (double)(Float)this.breakWallRange.getValue()) {
         return false;
      } else {
         double targetDmg = (double)this.calculateDamage(packet.getX() + 0.5D, packet.getY() + 1.0D, packet.getZ() + 0.5D, this.target);
         if (EntityUtil.isInHole(mc.player) && targetDmg >= 1.0D) {
            return true;
         } else {
            double selfDmg = (double)this.calculateDamage(packet.getX() + 0.5D, packet.getY() + 1.0D, packet.getZ() + 0.5D, mc.player);
            double d = (Boolean)this.suicide.getValue() ? 2.0D : 0.5D;
            if (selfDmg + d < (double)(mc.player.getHealth() + mc.player.getAbsorptionAmount()) && targetDmg >= (double)(this.target.getAbsorptionAmount() + this.target.getHealth())) {
               return true;
            } else {
               this.armorTarget = false;
               Iterator var9 = this.target.getArmorInventoryList().iterator();

               while(var9.hasNext()) {
                  ItemStack is = (ItemStack)var9.next();
                  float green = ((float)is.getMaxDamage() - (float)is.getItemDamage()) / (float)is.getMaxDamage();
                  float red = 1.0F - green;
                  int dmg = 100 - (int)(red * 100.0F);
                  if ((float)dmg <= (Float)this.minArmor.getValue()) {
                     this.armorTarget = true;
                  }
               }

               if (targetDmg >= (double)(Float)this.breakMinDmg.getValue() && selfDmg <= (double)(Float)this.breakMaxSelfDamage.getValue()) {
                  return true;
               } else {
                  return EntityUtil.isInHole(this.target) && this.target.getHealth() + this.target.getAbsorptionAmount() <= (Float)this.facePlace.getValue();
               }
            }
         }
      }
   }

   private boolean IsValidCrystal(Entity p_Entity) {
      if (p_Entity == null) {
         return false;
      } else if (!(p_Entity instanceof EntityEnderCrystal)) {
         return false;
      } else if (this.target == null) {
         return false;
      } else if (p_Entity.getDistance(mc.player) > (Float)this.breakRange.getValue()) {
         return false;
      } else if (!mc.player.canEntityBeSeen(p_Entity) && p_Entity.getDistance(mc.player) > (Float)this.breakWallRange.getValue()) {
         return false;
      } else if (!this.target.isDead && !(this.target.getHealth() + this.target.getAbsorptionAmount() <= 0.0F)) {
         double targetDmg = (double)this.calculateDamage((double)p_Entity.getPosition().getX() + 0.5D, (double)p_Entity.getPosition().getY() + 1.0D, (double)p_Entity.getPosition().getZ() + 0.5D, this.target);
         if (EntityUtil.isInHole(mc.player) && targetDmg >= 1.0D) {
            return true;
         } else {
            double selfDmg = (double)this.calculateDamage((double)p_Entity.getPosition().getX() + 0.5D, (double)p_Entity.getPosition().getY() + 1.0D, (double)p_Entity.getPosition().getZ() + 0.5D, mc.player);
            double d = (Boolean)this.suicide.getValue() ? 2.0D : 0.5D;
            if (selfDmg + d < (double)(mc.player.getHealth() + mc.player.getAbsorptionAmount()) && targetDmg >= (double)(this.target.getAbsorptionAmount() + this.target.getHealth())) {
               return true;
            } else {
               this.armorTarget = false;
               Iterator var8 = this.target.getArmorInventoryList().iterator();

               while(var8.hasNext()) {
                  ItemStack is = (ItemStack)var8.next();
                  float green = ((float)is.getMaxDamage() - (float)is.getItemDamage()) / (float)is.getMaxDamage();
                  float red = 1.0F - green;
                  int dmg = 100 - (int)(red * 100.0F);
                  if ((float)dmg <= (Float)this.minArmor.getValue()) {
                     this.armorTarget = true;
                  }
               }

               if (targetDmg >= (double)(Float)this.breakMinDmg.getValue() && selfDmg <= (double)(Float)this.breakMaxSelfDamage.getValue()) {
                  return true;
               } else {
                  return EntityUtil.isInHole(this.target) && this.target.getHealth() + this.target.getAbsorptionAmount() <= (Float)this.facePlace.getValue();
               }
            }
         }
      } else {
         return false;
      }
   }

   EntityPlayer getTarget() {
      EntityPlayer closestPlayer = null;
      Iterator var2 = mc.world.playerEntities.iterator();

      while(true) {
         EntityPlayer entity;
         do {
            do {
               do {
                  do {
                     do {
                        do {
                           do {
                              if (!var2.hasNext()) {
                                 return closestPlayer;
                              }

                              entity = (EntityPlayer)var2.next();
                           } while(mc.player == null);
                        } while(mc.player.isDead);
                     } while(entity.isDead);
                  } while(entity == mc.player);
               } while(Charon.friendManager.isFriend(entity.getName()));
            } while(entity.getDistance(mc.player) > 12.0F);

            this.armorTarget = false;
            Iterator var4 = entity.getArmorInventoryList().iterator();

            while(var4.hasNext()) {
               ItemStack is = (ItemStack)var4.next();
               float green = ((float)is.getMaxDamage() - (float)is.getItemDamage()) / (float)is.getMaxDamage();
               float red = 1.0F - green;
               int dmg = 100 - (int)(red * 100.0F);
               if ((float)dmg <= (Float)this.minArmor.getValue()) {
                  this.armorTarget = true;
               }
            }
         } while(EntityUtil.isInHole(entity) && entity.getAbsorptionAmount() + entity.getHealth() > (Float)this.facePlace.getValue() && !this.armorTarget && (Float)this.minDamage.getValue() > 2.2F);

         if (closestPlayer == null) {
            closestPlayer = entity;
         } else if (closestPlayer.getDistance(mc.player) > entity.getDistance(mc.player)) {
            closestPlayer = entity;
         }
      }
   }

   private void manualBreaker() {
      RayTraceResult result;
      if (this.manualTimer.passedMs(200L) && mc.gameSettings.keyBindUseItem.isKeyDown() && mc.player.getHeldItemOffhand().getItem() != Items.GOLDEN_APPLE && mc.player.inventory.getCurrentItem().getItem() != Items.GOLDEN_APPLE && mc.player.inventory.getCurrentItem().getItem() != Items.BOW && mc.player.inventory.getCurrentItem().getItem() != Items.EXPERIENCE_BOTTLE && (result = mc.objectMouseOver) != null) {
         if (result.typeOfHit.equals(Type.ENTITY)) {
            Entity entity = result.entityHit;
            if (entity instanceof EntityEnderCrystal) {
               if ((Boolean)this.packetBreak.getValue()) {
                  mc.player.connection.sendPacket(new CPacketUseEntity(entity));
               } else {
                  mc.playerController.attackEntity(mc.player, entity);
               }

               this.manualTimer.reset();
            }
         } else if (result.typeOfHit.equals(Type.BLOCK)) {
            BlockPos mousePos = new BlockPos((double)mc.objectMouseOver.getBlockPos().getX(), (double)mc.objectMouseOver.getBlockPos().getY() + 1.0D, (double)mc.objectMouseOver.getBlockPos().getZ());
            Iterator var3 = mc.world.getEntitiesWithinAABBExcludingEntity((Entity)null, new AxisAlignedBB(mousePos)).iterator();

            while(var3.hasNext()) {
               Entity target = (Entity)var3.next();
               if (target instanceof EntityEnderCrystal) {
                  if ((Boolean)this.packetBreak.getValue()) {
                     mc.player.connection.sendPacket(new CPacketUseEntity(target));
                  } else {
                     mc.playerController.attackEntity(mc.player, target);
                  }

                  this.manualTimer.reset();
               }
            }
         }
      }

   }

   private boolean canSeePos(BlockPos pos) {
      return mc.world.rayTraceBlocks(new Vec3d(mc.player.posX, mc.player.posY + (double)mc.player.getEyeHeight(), mc.player.posZ), new Vec3d((double)pos.getX(), (double)pos.getY(), (double)pos.getZ()), false, true, false) == null;
   }

   private NonNullList<BlockPos> placePostions(float placeRange) {
      NonNullList positions = NonNullList.create();
      positions.addAll((Collection)getSphere(new BlockPos(Math.floor(mc.player.posX), Math.floor(mc.player.posY), Math.floor(mc.player.posZ)), placeRange, (int)placeRange, false, true, 0).stream().filter((pos) -> {
         return this.canPlaceCrystal(pos, true);
      }).collect(Collectors.toList()));
      return positions;
   }

   private boolean canPlaceCrystal(BlockPos blockPos, boolean specialEntityCheck) {
      BlockPos boost = blockPos.add(0, 1, 0);
      BlockPos boost2 = blockPos.add(0, 2, 0);

      try {
         Iterator var5;
         Entity entity;
         if (!(Boolean)this.opPlace.getValue()) {
            if (mc.world.getBlockState(blockPos).getBlock() != Blocks.BEDROCK && mc.world.getBlockState(blockPos).getBlock() != Blocks.OBSIDIAN) {
               return false;
            }

            if (mc.world.getBlockState(boost).getBlock() != Blocks.AIR || mc.world.getBlockState(boost2).getBlock() != Blocks.AIR) {
               return false;
            }

            if (!specialEntityCheck) {
               return mc.world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(boost)).isEmpty() && mc.world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(boost2)).isEmpty();
            }

            var5 = mc.world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(boost)).iterator();

            while(var5.hasNext()) {
               entity = (Entity)var5.next();
               if (!(entity instanceof EntityEnderCrystal)) {
                  return false;
               }
            }

            var5 = mc.world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(boost2)).iterator();

            while(var5.hasNext()) {
               entity = (Entity)var5.next();
               if (!(entity instanceof EntityEnderCrystal)) {
                  return false;
               }
            }
         } else {
            if (mc.world.getBlockState(blockPos).getBlock() != Blocks.BEDROCK && mc.world.getBlockState(blockPos).getBlock() != Blocks.OBSIDIAN) {
               return false;
            }

            if (mc.world.getBlockState(boost).getBlock() != Blocks.AIR) {
               return false;
            }

            if (!specialEntityCheck) {
               return mc.world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(boost)).isEmpty();
            }

            var5 = mc.world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(boost)).iterator();

            while(var5.hasNext()) {
               entity = (Entity)var5.next();
               if (!(entity instanceof EntityEnderCrystal)) {
                  return false;
               }
            }
         }

         return true;
      } catch (Exception var7) {
         return false;
      }
   }

   private float calculateDamage(double posX, double posY, double posZ, Entity entity) {
      float doubleExplosionSize = 12.0F;
      double distancedsize = entity.getDistance(posX, posY, posZ) / 12.0D;
      Vec3d vec3d = new Vec3d(posX, posY, posZ);
      double blockDensity = 0.0D;

      try {
         blockDensity = (double)entity.world.getBlockDensity(vec3d, entity.getEntityBoundingBox());
      } catch (Exception var19) {
      }

      double v = (1.0D - distancedsize) * blockDensity;
      float damage = (float)((int)((v * v + v) / 2.0D * 7.0D * 12.0D + 1.0D));
      double finald = 1.0D;
      if (entity instanceof EntityLivingBase) {
         finald = (double)this.getBlastReduction((EntityLivingBase)entity, this.getDamageMultiplied(damage), new Explosion(mc.world, (Entity)null, posX, posY, posZ, 6.0F, false, true));
      }

      return (float)finald;
   }

   private float getBlastReduction(EntityLivingBase entity, float damageI, Explosion explosion) {
      float damage;
      if (entity instanceof EntityPlayer) {
         EntityPlayer ep = (EntityPlayer)entity;
         DamageSource ds = DamageSource.causeExplosionDamage(explosion);
         damage = CombatRules.getDamageAfterAbsorb(damageI, (float)ep.getTotalArmorValue(), (float)ep.getEntityAttribute(SharedMonsterAttributes.ARMOR_TOUGHNESS).getAttributeValue());
         int k = 0;

         try {
            k = EnchantmentHelper.getEnchantmentModifierDamage(ep.getArmorInventoryList(), ds);
         } catch (Exception var9) {
         }

         float f = MathHelper.clamp((float)k, 0.0F, 20.0F);
         damage *= 1.0F - f / 25.0F;
         if (entity.isPotionActive(MobEffects.RESISTANCE)) {
            damage -= damage / 4.0F;
         }

         damage = Math.max(damage, 0.0F);
         return damage;
      } else {
         damage = CombatRules.getDamageAfterAbsorb(damageI, (float)entity.getTotalArmorValue(), (float)entity.getEntityAttribute(SharedMonsterAttributes.ARMOR_TOUGHNESS).getAttributeValue());
         return damage;
      }
   }

   private float getDamageMultiplied(float damage) {
      int diff = mc.world.getDifficulty().getId();
      return damage * (diff == 0 ? 0.0F : (diff == 2 ? 1.0F : (diff == 1 ? 0.5F : 1.5F)));
   }

   public static enum SwingMode {
      MainHand,
      OffHand,
      None;
   }
}
