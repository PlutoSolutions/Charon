package cc.zip.charon.features.modules.misc;

import cc.zip.charon.event.events.Render2DEvent;
import cc.zip.charon.features.modules.Module;
import cc.zip.charon.features.modules.client.ClickGui;
import cc.zip.charon.util.ColorUtil;
import cc.zip.charon.util.RenderUtil;
import cc.zip.charon.util.Timer;
import java.awt.Color;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.GlStateManager.DestFactor;
import net.minecraft.client.renderer.GlStateManager.SourceFactor;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ItemStackHelper;
import net.minecraft.item.Item;
import net.minecraft.item.ItemShulkerBox;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntityShulkerBox;
import net.minecraft.util.NonNullList;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.fml.common.eventhandler.EventPriority;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

public class ToolTips extends Module {
   private static final ResourceLocation SHULKER_GUI_TEXTURE = new ResourceLocation("textures/gui/container/shulker_box.png");
   private static ToolTips INSTANCE = new ToolTips();
   public Map<EntityPlayer, ItemStack> spiedPlayers = new ConcurrentHashMap();
   public Map<EntityPlayer, Timer> playerTimers = new ConcurrentHashMap();
   private int textRadarY = 0;

   public ToolTips() {
      super("ShulkerViewer", "Several tweaks for tooltips.", Module.Category.MISC, true, false, false);
      this.setInstance();
   }

   public static ToolTips getInstance() {
      if (INSTANCE == null) {
         INSTANCE = new ToolTips();
      }

      return INSTANCE;
   }

   public static void displayInv(ItemStack stack, String name) {
      try {
         Item item = stack.getItem();
         TileEntityShulkerBox entityBox = new TileEntityShulkerBox();
         ItemShulkerBox shulker = (ItemShulkerBox)item;
         entityBox.blockType = shulker.getBlock();
         entityBox.setWorld(mc.world);
         ItemStackHelper.loadAllItems(stack.getTagCompound().getCompoundTag("BlockEntityTag"), entityBox.items);
         entityBox.readFromNBT(stack.getTagCompound().getCompoundTag("BlockEntityTag"));
         entityBox.setCustomName(name == null ? stack.getDisplayName() : name);
         (new Thread(() -> {
            try {
               Thread.sleep(200L);
            } catch (InterruptedException var2) {
            }

            mc.player.displayGUIChest(entityBox);
         })).start();
      } catch (Exception var5) {
      }

   }

   private void setInstance() {
      INSTANCE = this;
   }

   public void onUpdate() {
      if (!fullNullCheck()) {
         Iterator var1 = mc.world.playerEntities.iterator();

         while(var1.hasNext()) {
            EntityPlayer player = (EntityPlayer)var1.next();
            if (player != null && player.getHeldItemMainhand().getItem() instanceof ItemShulkerBox && mc.player != player) {
               ItemStack stack = player.getHeldItemMainhand();
               this.spiedPlayers.put(player, stack);
            }
         }

      }
   }

   public void onRender2D(Render2DEvent event) {
      if (!fullNullCheck()) {
         int x = -3;
         int y = 124;
         this.textRadarY = 0;
         Iterator var4 = mc.world.playerEntities.iterator();

         while(true) {
            EntityPlayer player;
            Timer playerTimer;
            do {
               do {
                  if (!var4.hasNext()) {
                     return;
                  }

                  player = (EntityPlayer)var4.next();
               } while(this.spiedPlayers.get(player) == null);

               if (player.getHeldItemMainhand() != null && player.getHeldItemMainhand().getItem() instanceof ItemShulkerBox) {
                  if (player.getHeldItemMainhand().getItem() instanceof ItemShulkerBox && (playerTimer = (Timer)this.playerTimers.get(player)) != null) {
                     playerTimer.reset();
                     this.playerTimers.put(player, playerTimer);
                  }
                  break;
               }

               playerTimer = (Timer)this.playerTimers.get(player);
               if (playerTimer == null) {
                  Timer timer = new Timer();
                  timer.reset();
                  this.playerTimers.put(player, timer);
                  break;
               }
            } while(playerTimer.passedS(3.0D));

            ItemStack stack = (ItemStack)this.spiedPlayers.get(player);
            this.renderShulkerToolTip(stack, x, y, player.getName());
            y += 78;
            this.textRadarY = y - 10 - 114 + 2;
         }
      }
   }

   @SubscribeEvent(
      priority = EventPriority.HIGHEST
   )
   public void makeTooltip(ItemTooltipEvent event) {
   }

   public void renderShulkerToolTip(ItemStack stack, int x, int y, String name) {
      NBTTagCompound tagCompound = stack.getTagCompound();
      NBTTagCompound blockEntityTag;
      if (tagCompound != null && tagCompound.hasKey("BlockEntityTag", 10) && (blockEntityTag = tagCompound.getCompoundTag("BlockEntityTag")).hasKey("Items", 9)) {
         GlStateManager.enableTexture2D();
         GlStateManager.disableLighting();
         GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
         GlStateManager.enableBlend();
         GlStateManager.tryBlendFuncSeparate(SourceFactor.SRC_ALPHA, DestFactor.ONE_MINUS_SRC_ALPHA, SourceFactor.ONE, DestFactor.ZERO);
         mc.getTextureManager().bindTexture(SHULKER_GUI_TEXTURE);
         RenderUtil.drawTexturedRect(x, y, 0, 0, 176, 16, 500);
         RenderUtil.drawTexturedRect(x, y + 16, 0, 16, 176, 57, 500);
         RenderUtil.drawTexturedRect(x, y + 16 + 54, 0, 160, 176, 8, 500);
         GlStateManager.disableDepth();
         Color color = new Color((Integer)ClickGui.getInstance().red.getValue(), (Integer)ClickGui.getInstance().green.getValue(), (Integer)ClickGui.getInstance().blue.getValue(), 200);
         this.renderer.drawStringWithShadow(name == null ? stack.getDisplayName() : name, (float)(x + 8), (float)(y + 6), ColorUtil.toRGBA(color));
         GlStateManager.enableDepth();
         RenderHelper.enableGUIStandardItemLighting();
         GlStateManager.enableRescaleNormal();
         GlStateManager.enableColorMaterial();
         GlStateManager.enableLighting();
         NonNullList nonnulllist = NonNullList.withSize(27, ItemStack.EMPTY);
         ItemStackHelper.loadAllItems(blockEntityTag, nonnulllist);

         for(int i = 0; i < nonnulllist.size(); ++i) {
            int iX = x + i % 9 * 18 + 8;
            int iY = y + i / 9 * 18 + 18;
            ItemStack itemStack = (ItemStack)nonnulllist.get(i);
            mc.getItemRenderer().itemRenderer.zLevel = 501.0F;
            RenderUtil.itemRender.renderItemAndEffectIntoGUI(itemStack, iX, iY);
            RenderUtil.itemRender.renderItemOverlayIntoGUI(mc.fontRenderer, itemStack, iX, iY, (String)null);
            mc.getItemRenderer().itemRenderer.zLevel = 0.0F;
         }

         GlStateManager.disableLighting();
         GlStateManager.disableBlend();
         GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
      }

   }
}
