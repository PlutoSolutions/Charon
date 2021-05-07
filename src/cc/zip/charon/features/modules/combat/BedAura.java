package cc.zip.charon.features.modules.combat;

import cc.zip.charon.Charon;
import cc.zip.charon.features.command.Command;
import cc.zip.charon.features.modules.Module;
import cc.zip.charon.features.setting.Setting;
import cc.zip.charon.util.Timer;
import com.mojang.realmsclient.gui.ChatFormatting;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.inventory.ClickType;
import net.minecraft.item.ItemBed;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.CPacketEntityAction;
import net.minecraft.network.play.client.CPacketPlayerTryUseItemOnBlock;
import net.minecraft.network.play.client.CPacketEntityAction.Action;
import net.minecraft.network.play.client.CPacketPlayer.Rotation;
import net.minecraft.tileentity.TileEntityBed;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraftforge.common.MinecraftForge;

public class BedAura extends Module {
   private final Setting<Integer> packets = this.register(new Setting("Packets", 2, 1, 4, "Amount of packets you want to send."));
   private final Timer timer = new Timer();
   private final boolean resetTimer = false;
   public Setting<Integer> range = this.register(new Setting("Range", 6, 0, 9));
   public Setting<Integer> placedelay = this.register(new Setting("Place Delay", 15, 8, 20));
   public Setting<Boolean> announceUsage = this.register(new Setting("Message", true));
   public Setting<Boolean> placeesp = this.register(new Setting("ESP", true));
   private int playerHotbarSlot = -1;
   private int lastHotbarSlot = -1;
   private EntityPlayer closestTarget;
   private String lastTickTargetName;
   private int bedSlot = -1;
   private BlockPos placeTarget;
   private float rotVar;
   private int blocksPlaced;
   private double diffXZ;
   private boolean firstRun;
   private boolean nowTop = false;

   public BedAura() {
      super("BedAura", "Auto Bed", Module.Category.COMBAT, true, false, false);
   }

   public void onEnable() {
      if (mc.player == null) {
         this.toggle();
      } else {
         MinecraftForge.EVENT_BUS.register(this);
         this.firstRun = true;
         this.blocksPlaced = 0;
         this.playerHotbarSlot = mc.player.inventory.currentItem;
         this.lastHotbarSlot = -1;
      }
   }

   public void onDisable() {
      if (mc.player != null) {
         MinecraftForge.EVENT_BUS.unregister(this);
         if (this.lastHotbarSlot != this.playerHotbarSlot && this.playerHotbarSlot != -1) {
            mc.player.inventory.currentItem = this.playerHotbarSlot;
         }

         this.playerHotbarSlot = -1;
         this.lastHotbarSlot = -1;
         this.blocksPlaced = 0;
      }
   }

   public void onUpdate() {
      if (mc.player != null) {
         if (mc.player.dimension == 0) {
            Command.sendMessage("You are in the overworld!");
            this.toggle();
         }

         try {
            this.findClosestTarget();
         } catch (NullPointerException var10) {
         }

         if (this.closestTarget == null && mc.player.dimension != 0 && this.firstRun) {
            this.firstRun = false;
            if ((Boolean)this.announceUsage.getValue()) {
               Command.sendMessage(ChatFormatting.RED + "targen when?");
            }
         }

         if (this.firstRun && this.closestTarget != null && mc.player.dimension != 0) {
            this.firstRun = false;
            this.lastTickTargetName = this.closestTarget.getName();
            if ((Boolean)this.announceUsage.getValue()) {
               Command.sendMessage(ChatFormatting.RED + "target: " + ChatFormatting.WHITE.toString() + this.lastTickTargetName);
            }
         }

         if (this.closestTarget != null && this.lastTickTargetName != null && !this.lastTickTargetName.equals(this.closestTarget.getName())) {
            this.lastTickTargetName = this.closestTarget.getName();
            if ((Boolean)this.announceUsage.getValue()) {
               Command.sendMessage(ChatFormatting.RED + " New target: " + ChatFormatting.WHITE.toString() + this.lastTickTargetName);
            }
         }

         try {
            this.diffXZ = mc.player.getPositionVector().distanceTo(this.closestTarget.getPositionVector());
         } catch (NullPointerException var9) {
         }

         try {
            if (this.closestTarget != null) {
               this.placeTarget = new BlockPos(this.closestTarget.getPositionVector().add(1.0D, 1.0D, 0.0D));
               this.nowTop = false;
               this.rotVar = 90.0F;
               BlockPos block1 = this.placeTarget;
               if (!this.canPlaceBed(block1)) {
                  this.placeTarget = new BlockPos(this.closestTarget.getPositionVector().add(-1.0D, 1.0D, 0.0D));
                  this.rotVar = -90.0F;
                  this.nowTop = false;
               }

               BlockPos block2 = this.placeTarget;
               if (!this.canPlaceBed(block2)) {
                  this.placeTarget = new BlockPos(this.closestTarget.getPositionVector().add(0.0D, 1.0D, 1.0D));
                  this.rotVar = 180.0F;
                  this.nowTop = false;
               }

               BlockPos block3 = this.placeTarget;
               if (!this.canPlaceBed(block3)) {
                  this.placeTarget = new BlockPos(this.closestTarget.getPositionVector().add(0.0D, 1.0D, -1.0D));
                  this.rotVar = 0.0F;
                  this.nowTop = false;
               }

               BlockPos block4 = this.placeTarget;
               if (!this.canPlaceBed(block4)) {
                  this.placeTarget = new BlockPos(this.closestTarget.getPositionVector().add(0.0D, 2.0D, -1.0D));
                  this.rotVar = 0.0F;
                  this.nowTop = true;
               }

               BlockPos blockt1 = this.placeTarget;
               if (this.nowTop && !this.canPlaceBed(blockt1)) {
                  this.placeTarget = new BlockPos(this.closestTarget.getPositionVector().add(-1.0D, 2.0D, 0.0D));
                  this.rotVar = -90.0F;
               }

               BlockPos blockt2 = this.placeTarget;
               if (this.nowTop && !this.canPlaceBed(blockt2)) {
                  this.placeTarget = new BlockPos(this.closestTarget.getPositionVector().add(0.0D, 2.0D, 1.0D));
                  this.rotVar = 180.0F;
               }

               BlockPos blockt3 = this.placeTarget;
               if (this.nowTop && !this.canPlaceBed(blockt3)) {
                  this.placeTarget = new BlockPos(this.closestTarget.getPositionVector().add(1.0D, 2.0D, 0.0D));
                  this.rotVar = 90.0F;
               }
            }

            mc.world.loadedTileEntityList.stream().filter((e) -> {
               return e instanceof TileEntityBed;
            }).filter((e) -> {
               return mc.player.getDistance((double)e.getPos().getX(), (double)e.getPos().getY(), (double)e.getPos().getZ()) <= (double)(Integer)this.range.getValue();
            }).sorted(Comparator.comparing((e) -> {
               return mc.player.getDistance((double)e.getPos().getX(), (double)e.getPos().getY(), (double)e.getPos().getZ());
            })).forEach((bed) -> {
               if (mc.player.dimension != 0) {
                  mc.player.connection.sendPacket(new CPacketPlayerTryUseItemOnBlock(bed.getPos(), EnumFacing.UP, EnumHand.OFF_HAND, 0.0F, 0.0F, 0.0F));
               }

            });
            if (mc.player.ticksExisted % (Integer)this.placedelay.getValue() == 0 && this.closestTarget != null) {
               this.findBeds();
               ++mc.player.ticksExisted;
               this.doDaMagic();
            }
         } catch (NullPointerException var8) {
            var8.printStackTrace();
         }

      }
   }

   private void doDaMagic() {
      if (this.diffXZ <= (double)(Integer)this.range.getValue()) {
         for(int i = 0; i < 9 && this.bedSlot == -1; ++i) {
            ItemStack stack = mc.player.inventory.getStackInSlot(i);
            if (stack.getItem() instanceof ItemBed) {
               this.bedSlot = i;
               if (i != -1) {
                  mc.player.inventory.currentItem = this.bedSlot;
               }
               break;
            }
         }

         this.bedSlot = -1;
         if (this.blocksPlaced == 0 && mc.player.inventory.getStackInSlot(mc.player.inventory.currentItem).getItem() instanceof ItemBed) {
            mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, Action.START_SNEAKING));
            mc.player.connection.sendPacket(new Rotation(this.rotVar, 0.0F, mc.player.onGround));
            this.placeBlock(new BlockPos(this.placeTarget), EnumFacing.DOWN);
            mc.player.connection.sendPacket(new CPacketEntityAction(mc.player, Action.STOP_SNEAKING));
            this.blocksPlaced = 1;
            this.nowTop = false;
         }

         this.blocksPlaced = 0;
      }

   }

   private void findBeds() {
      if ((mc.currentScreen == null || !(mc.currentScreen instanceof GuiContainer)) && mc.player.inventory.getStackInSlot(0).getItem() != Items.BED) {
         for(int i = 9; i < 36; ++i) {
            if (mc.player.inventory.getStackInSlot(i).getItem() == Items.BED) {
               mc.playerController.windowClick(mc.player.inventoryContainer.windowId, i, 0, ClickType.SWAP, mc.player);
               break;
            }
         }
      }

   }

   private boolean canPlaceBed(BlockPos pos) {
      return (mc.world.getBlockState(pos).getBlock() == Blocks.AIR || mc.world.getBlockState(pos).getBlock() == Blocks.BED) && mc.world.getEntitiesWithinAABB(Entity.class, new AxisAlignedBB(pos)).isEmpty();
   }

   private void findClosestTarget() {
      List<EntityPlayer> playerList = mc.world.playerEntities;
      this.closestTarget = null;
      Iterator var2 = playerList.iterator();

      while(var2.hasNext()) {
         EntityPlayer target = (EntityPlayer)var2.next();
         if (target != mc.player && !Charon.friendManager.isFriend(target.getName()) && isLiving(target) && !(target.getHealth() <= 0.0F)) {
            if (this.closestTarget == null) {
               this.closestTarget = target;
            } else if (mc.player.getDistance(target) < mc.player.getDistance(this.closestTarget)) {
               this.closestTarget = target;
            }
         }
      }

   }

   private void placeBlock(BlockPos pos, EnumFacing side) {
      BlockPos neighbour = pos.offset(side);
      EnumFacing opposite = side.getOpposite();
      Vec3d hitVec = (new Vec3d(neighbour)).add(0.5D, 0.5D, 0.5D).add((new Vec3d(opposite.getDirectionVec())).scale(0.5D));
      mc.playerController.processRightClickBlock(mc.player, mc.world, neighbour, opposite, hitVec, EnumHand.MAIN_HAND);
   }

   public static boolean isLiving(Entity e) {
      return e instanceof EntityLivingBase;
   }
}
