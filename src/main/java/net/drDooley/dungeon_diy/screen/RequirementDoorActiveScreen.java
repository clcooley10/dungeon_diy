package net.drDooley.dungeon_diy.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.drDooley.dungeon_diy.DDIY;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.items.ItemStackHandler;
import net.minecraftforge.items.SlotItemHandler;

import java.util.ArrayList;

public class RequirementDoorActiveScreen extends AbstractContainerScreen<RequirementDoorActiveMenu> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(DDIY.MODID, "textures/gui/requirement_door_gui.png");
    private static final int REQ_STACKS_X = 43;
    private static final int REQ_STACKS_Y = 41;
    private static final int UNLOCK_X = 149;
    private static final int UNLOCK_Y = 18;
    ArrayList<ItemStack> editItems = new ArrayList<>();
    public RequirementDoorActiveScreen(RequirementDoorActiveMenu menu, Inventory inv, Component component) {
        super(menu, inv, component);
        this.imageHeight = 155;
        this.inventoryLabelY = this.imageHeight - 94;

        DDIY.LOGGER.info("End of Active Screen Constructor");
    }

    @Override
    protected void init() {
        super.init();
    }

    @Override
    protected void renderBg(PoseStack pPoseStack, float pPartialTick, int pMouseX, int pMouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
        RenderSystem.setShaderTexture(0, TEXTURE);
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        this.blit(pPoseStack, x, y, 0, 0, imageWidth, imageHeight);
    }

    @Override
    public void render(PoseStack pPoseStack, int pMouseX, int pMouseY, float pPartialTick) {
        renderBackground(pPoseStack);
        super.render(pPoseStack, pMouseX, pMouseY, pPartialTick);
        renderStacks(pPoseStack, pMouseX, pMouseY);
        renderTooltip(pPoseStack, pMouseX, pMouseY);
    }

    private void renderStacks(PoseStack poseStack, int pX, int pY) {
        ItemRenderer itemRenderer = this.itemRenderer;
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        this.menu.blockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER).ifPresent(handler -> {
            // Only render up to the first 5 items, which belong to the master items. 6-10 are the items currently
            // being inserted, used to match the first 5.
            int start_x = this.leftPos + REQ_STACKS_X;
            int start_y = this.topPos + REQ_STACKS_Y;
            for (int i = 0; i < 5; i++) {
                ItemStack stack = handler.getStackInSlot(i);
                if (!stack.is(Items.AIR)) {
                    itemRenderer.renderAndDecorateItem(stack, i * 18 + 43 + x, 40 + y);
                    itemRenderer.renderGuiItemDecorations(this.font, stack, i * 18 + 43 + x, 40 + y);
                    if (pX >= start_x + i * 18 && pX < start_x + i * 18 + 16 && pY >= start_y && pY < start_y + 16) {
                        this.renderTooltip(poseStack, stack, pX, pY);
                    }
                }
            }
        });

        // Renders the unlock button
        //this.blit(poseStack, this.leftPos + UNLOCK_X, this.topPos + UNLOCK_Y, this.imageWidth, 0, 20, 20);
//        int i = 0;
//        for (ItemStack stack : this.editItems) {
//            itemRenderer.renderAndDecorateItem(stack, i * 18 + 43 + x, 40 + y);
//            itemRenderer.renderGuiItemDecorations(this.font, stack, i * 18 + 43 + x, 40 + y);
//            i++;
//        }
//      Computing differences and rendering only the difference between the masterItems and the currently inserted items
//        this.menu.blockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER, Direction.UP).ifPresent(handler -> {
//            for (int i = 0; i < handler.getSlots(); i++) {
//                ItemStack activeStack = handler.getStackInSlot(i);
//
//            }
//        });
    }
}
