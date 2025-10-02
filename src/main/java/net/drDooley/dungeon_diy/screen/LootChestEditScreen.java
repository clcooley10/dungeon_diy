package net.drDooley.dungeon_diy.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.drDooley.dungeon_diy.DDIY;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.capabilities.ForgeCapabilities;

import java.util.List;


public class LootChestEditScreen extends AbstractContainerScreen<LootChestEditMenu> {

    private static final ResourceLocation POOL_TEXTURE = new ResourceLocation(DDIY.MODID, "textures/gui/loot_chest_edit.png");
    private static final ResourceLocation ITEM_TEXTURE = new ResourceLocation(DDIY.MODID, "textures/gui/loot_chest_edit_items.png");
    // Pool Screen
    private static final int POOLS_X = 16;
    private static final int POOLS_Y = 37;
    private static final int POOL_ITEMS_X = 7;
    private static final int POOL_ITEMS_Y = 105;
    private static final int POOL_CONF_X = 95;
    private static final int POOL_ICON_X = 107;
    private static final int POOL_ICON_Y = 41;
    private static final int POOL_RESET_X = 134;
    private static final int POOL_RESET_Y = 42;
    private static final int ROLLS_Y = 70;
    private static final int ROLLS_MIN_X = 104;
    private static final int ROLLS_MAX_X = 121;
    private static final int ROLLS_BONUS_X = 138;

    private static final int ARROW_BTN_W = 11;
    private static final int ARROW_BTN_H = 7;
    private static final int POOL_BTN_W = 16;
    private static final int POOL_BTN_H = 18;
    private static final int NUM_POOLS = 12;
    private static final int INT_BOX_W = 15;
    private static final int INT_BOX_H = 12;
    private static final int RESET_WH = 16;
    // If changing number of buttons (pools, arrows, reset) update here.
    private static final int POOL_BTNS = 19;

    // Item Screen
    private static final int COUNTS_Y = 136;
    private static final int COUNTS_MIN_X = 60;
    private static final int COUNTS_MAX_X = 78;
    private static final int COUNTS_WEIGHT_X = 96;
    private static final int BACK_X = 121;
    private static final int BACK_Y = 134;
    private static final int INV_RESET_X = 173;
    private static final int INV_RESET_Y = 134;
    private static final int SCROLLER_X = 175;
    private static final int SCROLLER_Y = 18;
    private static final int INV_X = 8;
    private static final int INV_Y = 17;
    private static final int SELECTED_ITEM_X = 32;
    private static final int SELECTED_ITEM_Y = 133;

    private static final int BACK_BTN_W = 24;
    private static final int BACK_BTN_H = 16;
    private static final int SCROLLER_W = 12;
    private static final int SCROLLER_H = 15;
    private static final int SCROLLER_FULL_HEIGHT = 106;
    // If changing number of buttons (pools, arrows, reset) update here.
    private static final int INV_BTNS = 8;

    private static final int ROWS = 6;
    private static final int COLS = 9;

    private int startIndex;
    private float scrollOffs;
    private boolean scrolling;

    public LootChestEditScreen(LootChestEditMenu pMenu, Inventory pPlayerInventory, Component pTitle) {
        super(pMenu, pPlayerInventory, pTitle);
    }

    @Override
    protected void renderBg(PoseStack pPoseStack, float delta, int mouseX, int mouseY) {
        this.renderBackground(pPoseStack);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        if(!this.menu.displayItemScreen()) {
            RenderSystem.setShaderTexture(0, POOL_TEXTURE);
            this.inventoryLabelY = this.imageHeight - 70;
            this.imageWidth = 176;
            this.imageHeight = 166;
        } else {
            RenderSystem.setShaderTexture(0, ITEM_TEXTURE);
            this.imageWidth = 195;
            this.imageHeight = 162;
        }
        this.leftPos = (width - imageWidth) / 2;
        this.topPos = (height - imageHeight) / 2;
        this.blit(pPoseStack, leftPos, topPos, 0, 0, imageWidth, imageHeight);
        this.renderButtons(pPoseStack, mouseX, mouseY);
    }

    @Override
    public void render(PoseStack pPoseStack, int mouseX, int mouseY, float delta) {
        super.render(pPoseStack, mouseX, mouseY, delta);
        this.renderTooltip(pPoseStack, mouseX, mouseY);
    }

    @Override
    protected void renderTooltip(PoseStack pPoseStack, int mouseX, int mouseY) {
        if (!this.menu.displayItemScreen()) {
            renderPoolTooltip(pPoseStack, mouseX, mouseY);
            return;
        } else {
            renderItemTooltip(pPoseStack, mouseX, mouseY);
            renderFakeSlotHighlight(pPoseStack, mouseX, mouseY);
        }
        super.renderTooltip(pPoseStack, mouseX, mouseY);
    }

    protected void renderButtons(PoseStack poseStack, int mouseX, int mouseY) {
        if (!this.menu.displayItemScreen()) {
            renderPoolButtons(poseStack, mouseX, mouseY);
            this.renderPoolIcons();
        } else {
            renderItemButtons(poseStack, mouseX, mouseY);
            renderLootItems();
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int pButton) {
        // Only relevant to ItemScreen, but don't want it to ever be true in Pool Screen
        this.scrolling = false;
        if (!this.menu.displayItemScreen()) {
            return mouseClickedPool(mouseX, mouseY, pButton);
        } else {
            return mouseClickedItem(mouseX, mouseY, pButton);
        }
    }

    @Override
    protected void slotClicked(Slot pSlot, int pSlotId, int pMouseButton, ClickType pType) {
        if (!this.menu.displayItemScreen()) {
            slotClickedPool(pSlot, pSlotId, pMouseButton, pType);
        } else {
            slotClickedItem(pSlot, pSlotId, pMouseButton, pType);
        }
    }

    @Override
    protected void renderLabels(PoseStack pPoseStack, int pMouseX, int pMouseY) {
        if (!this.menu.displayItemScreen()) {
            renderPoolLabels(pPoseStack);
        } else {
            renderItemLabels(pPoseStack);
        }
    }

    private void renderPoolTooltip(PoseStack pPoseStack, int mouseX, int mouseY) {
        // Pools space
        int x = this.leftPos + POOLS_X;
        int y = this.topPos + POOLS_Y;
        if (inRect(mouseX, mouseY, x, x + POOL_BTN_W * 4, y, y + POOL_BTN_H * 3)) {
            this.renderTooltip(pPoseStack, Component.translatable("gui.dungeon_diy.loot_chest.select_pool"), mouseX, mouseY);
        }
        if (!this.menu.hasActive()) {
            return;
        }
        // Inventory space
        x = this.leftPos + POOL_ITEMS_X;
        y = this.topPos + POOL_ITEMS_Y;
        if (inRect(mouseX, mouseY, x, x + 162, y, y + 54)) {
            this.renderTooltip(pPoseStack, Component.translatable("gui.dungeon_diy.loot_chest.add_edit_pool_item"), mouseX, mouseY);
        }

        // Pool Config space
        x = this.leftPos + POOL_ICON_X;
        y = this.topPos + POOL_ICON_Y;
        if (inRect(mouseX, mouseY, x, x + 18, y, y + 18)) {
            this.renderTooltip(pPoseStack, Component.translatable("gui.dungeon_diy.loot_chest.set_pool_icon"), mouseX, mouseY);
        }
        x = this.leftPos + POOL_RESET_X;
        y = this.topPos + POOL_RESET_Y;
        if (inRect(mouseX, mouseY, x, x + RESET_WH, y, y + RESET_WH)) {
            this.renderTooltip(pPoseStack, Component.translatable("gui.dungeon_diy.loot_chest.reset").withStyle(ChatFormatting.DARK_RED), mouseX, mouseY);
        }
        x = this.leftPos + ROLLS_MIN_X;
        y = this.topPos + ROLLS_Y;
        if (inRect(mouseX, mouseY, x, x + INT_BOX_W, y, y + INT_BOX_H)) {
            this.renderTooltip(pPoseStack, Component.translatable("gui.dungeon_diy.loot_chest.min_rolls"), mouseX, mouseY);
        }
        x = this.leftPos + ROLLS_MAX_X;
        if (inRect(mouseX, mouseY, x, x + INT_BOX_W, y, y + INT_BOX_H)) {
            this.renderTooltip(pPoseStack, Component.translatable("gui.dungeon_diy.loot_chest.max_rolls"), mouseX, mouseY);
        }
        x = this.leftPos + ROLLS_BONUS_X;
        if (inRect(mouseX, mouseY, x, x + INT_BOX_W, y, y + INT_BOX_H)) {
            this.renderTooltip(pPoseStack, Component.translatable("gui.dungeon_diy.loot_chest.bonus_rolls"), mouseX, mouseY);
        }

    }

    private void renderItemTooltip(PoseStack pPoseStack, int mouseX, int mouseY) {
        // Min item count, max item count, weight
        int x = this.leftPos + COUNTS_MIN_X;
        int y = this.topPos + COUNTS_Y;
        // NA for Pool Icon Selection
        if (this.menu.getActiveItemLocal() != 27) {
            if (inRect(mouseX, mouseY, x, x + INT_BOX_W, y, y + INT_BOX_H)) {
                this.renderTooltip(pPoseStack, Component.translatable("gui.dungeon_diy.loot_chest.min_count"), mouseX, mouseY);
            }
            x = this.leftPos + COUNTS_MAX_X;
            if (inRect(mouseX, mouseY, x, x + INT_BOX_W, y, y + INT_BOX_H)) {
                this.renderTooltip(pPoseStack, Component.translatable("gui.dungeon_diy.loot_chest.max_count"), mouseX, mouseY);
            }
            x = this.leftPos + COUNTS_WEIGHT_X;
            if (inRect(mouseX, mouseY, x, x + INT_BOX_W, y, y + INT_BOX_H)) {
                this.renderTooltip(pPoseStack, Component.translatable("gui.dungeon_diy.loot_chest.weight"), mouseX, mouseY);
            }
        }
        x = this.leftPos + BACK_X;
        y = this.topPos + BACK_Y;
        if (inRect(mouseX, mouseY, x, x + BACK_BTN_W, y, y + BACK_BTN_H)) {
            this.renderTooltip(pPoseStack, Component.translatable("gui.dungeon_diy.loot_chest.back"), mouseX, mouseY);
        }

        x = this.leftPos + INV_RESET_X;
        y = this.topPos + INV_RESET_Y;
        if (inRect(mouseX, mouseY, x, x + RESET_WH, y, y + RESET_WH)) {
            this.renderTooltip(pPoseStack, Component.translatable("gui.dungeon_diy.loot_chest.reset").withStyle(ChatFormatting.DARK_RED), mouseX, mouseY);
        }

        // LootItems
        List<ItemStack> lootItems = this.menu.lootItems;
        for (int i = this.startIndex; i < this.startIndex + ROWS * COLS && i < lootItems.size(); i++) {
            int localIndex = i - this.startIndex;
            x = this.leftPos + INV_X + localIndex % COLS * 18;
            int row = localIndex / COLS;
            y = this.topPos + INV_Y + row * 18;
            if (inRect(mouseX, mouseY, x, x + 18, y, y + 18)) {
                this.renderTooltip(pPoseStack, lootItems.get(i).getHoverName(), mouseX, mouseY);
            }
        }

        // Selected Item
        x = this.leftPos + SELECTED_ITEM_X;
        y = this.topPos + SELECTED_ITEM_Y;
        if (inRect(mouseX, mouseY, x, x + 18, y, y + 18)) {
            this.menu.blockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER).ifPresent(handler -> {
                ItemStack selected = handler.getStackInSlot(this.menu.getActiveItem());
                this.renderTooltip(pPoseStack, selected.getHoverName(), mouseX, mouseY);
            });
        }
    }

    private void renderFakeSlotHighlight(PoseStack poseStack, int mouseX, int mouseY) {
        // We don't have real slots, but I want it to look like we do- highlight when hovered
        for (int row = 0; row < ROWS; row++) {
            for (int col = 0; col < COLS; col++) {
                int x = this.leftPos + INV_X + 1 + col * 18;
                int y = this.topPos + INV_Y + 1 + row * 18;

                if (inRect(mouseX, mouseY, x, x + 18, y, y + 18)) {
                    renderSlotHighlight(poseStack, x, y, this.getBlitOffset());
                    return;
                }
            }
        }
    }

    private void renderPoolButtons(PoseStack poseStack, int mouseX, int mouseY) {
        // Button for each Pool
        for (int i = 0; i < NUM_POOLS; ++i) {
            boolean active = i == this.menu.getActivePool() && this.menu.hasActive();
            int x = this.leftPos + POOLS_X;
            int y = this.topPos + POOLS_Y;
            int row = i / 4;
            int col = i % 4;
            x += POOL_BTN_W * col;
            y += POOL_BTN_H * row;
            int bg_height = this.imageHeight;
            // If active, use the darker brown background
            if (active) { bg_height += POOL_BTN_H; }
            // If hovered, use the pink background
            else if (inRect(mouseX, mouseY, x, x + POOL_BTN_W, y, y + POOL_BTN_H)) { bg_height += POOL_BTN_H * 2; }
            this.blit(poseStack, x, y, 0, bg_height, POOL_BTN_W, POOL_BTN_H);
        }

        if (!this.menu.hasActive()) {
            return;
        }

        // arrows to inc/dec the number of (bonus) rolls
        int[] widths = {ROLLS_MIN_X, ROLLS_MAX_X, ROLLS_BONUS_X};
        for (int width : widths) {
            // Dec arrow
            int arrowX = this.leftPos + width + 2;
            int arrowY = this.topPos + ROLLS_Y + INT_BOX_H + 1;
            if (inRect(mouseX, mouseY, arrowX, arrowX + ARROW_BTN_W, arrowY, arrowY + ARROW_BTN_H)) {
                this.blit(poseStack, arrowX, arrowY, this.imageWidth, 0, ARROW_BTN_W, ARROW_BTN_H);
            }
            // Inc arrow
            arrowY = this.topPos + ROLLS_Y - 8;
            if (inRect(mouseX, mouseY, arrowX, arrowX + ARROW_BTN_W, arrowY, arrowY + ARROW_BTN_H)) {
                this.blit(poseStack, arrowX, arrowY, this.imageWidth + ARROW_BTN_W, 0, ARROW_BTN_W, ARROW_BTN_H);
            }
        }

        // Reset button
        int x = this.leftPos + POOL_RESET_X;
        int y = this.topPos + POOL_RESET_Y;
        if (inRect(mouseX, mouseY, x, x + RESET_WH, y, y + RESET_WH)) {
            this.blit(poseStack, x, y, this.imageWidth, ARROW_BTN_H, RESET_WH, RESET_WH);
        }
    }

    private void renderItemButtons(PoseStack poseStack, int mouseX, int mouseY) {
        // Don't highlight arrows when they're not meant to be active.
        if (this.menu.getActiveItemLocal() != 27) {
            // arrows to inc/dec the number of (bonus) rolls
            int[] widths = {COUNTS_MIN_X, COUNTS_MAX_X, COUNTS_WEIGHT_X};
            for (int width : widths) {
                // Dec arrow
                int arrowX = this.leftPos + width + 2;
                int arrowY = this.topPos + COUNTS_Y + INT_BOX_H + 1;
                if (inRect(mouseX, mouseY, arrowX, arrowX + ARROW_BTN_W, arrowY, arrowY + ARROW_BTN_H)) {
                    this.blit(poseStack, arrowX, arrowY, this.imageWidth, SCROLLER_H, ARROW_BTN_W, ARROW_BTN_H);
                }
                // Inc arrow
                arrowY = this.topPos + COUNTS_Y - 8;
                if (inRect(mouseX, mouseY, arrowX, arrowX + ARROW_BTN_W, arrowY, arrowY + ARROW_BTN_H)) {
                    this.blit(poseStack, arrowX, arrowY, this.imageWidth + ARROW_BTN_W, SCROLLER_H, ARROW_BTN_W, ARROW_BTN_H);
                }
            }
        }
        // BACK button
        int x = this.leftPos + BACK_X;
        int y = this.topPos + BACK_Y;
        if (inRect(mouseX, mouseY, x, x + BACK_BTN_W, y, y + BACK_BTN_H)) {
            this.blit(poseStack, x, y, 0, this.imageHeight, BACK_BTN_W, BACK_BTN_H);
        }

        // Reset button
        x = this.leftPos + INV_RESET_X;
        y = this.topPos + INV_RESET_Y;
        if (inRect(mouseX, mouseY, x, x + RESET_WH, y, y + RESET_WH)) {
            this.blit(poseStack, x, y, 0, this.imageHeight + BACK_BTN_H, RESET_WH, RESET_WH);
        }

        // SCROLLER
        x = this.leftPos + SCROLLER_X;
        y = this.topPos + SCROLLER_Y;
        int offset = (int)((SCROLLER_FULL_HEIGHT-SCROLLER_H) * this.scrollOffs);
        this.blit(poseStack, x, y + offset, this.imageWidth + (this.menu.canScroll() ? 0 : SCROLLER_W), 0, SCROLLER_W, SCROLLER_H);
    }

    private void renderPoolIcons() {
        //DDIY.LOGGER.info("Inside renderPoolIcons");
        // The config and Item space are slots which render automatically. This is for the Pools.
        this.menu.blockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER).ifPresent(handler -> {
            // Icon for each Pool
            for (int i = 0; i < NUM_POOLS; ++i) {
                int x = this.leftPos + POOLS_X;
                int y = this.topPos + POOLS_Y + 1;
                int row = i / 4;
                int col = i % 4;
                x += POOL_BTN_W * col;
                y += POOL_BTN_H * row;
                ItemStack icon = handler.getStackInSlot(i * 28 + 27);
                this.minecraft.getItemRenderer().renderAndDecorateItem(icon, x, y);
            }
        });
    }

    private void renderLootItems() {
        // Basically renderRecipes for stonecutter
        List<ItemStack> lootItems = this.menu.lootItems;
        for (int i = this.startIndex; i < this.startIndex + ROWS * COLS && i < lootItems.size(); i++) {
            int localIndex = i - this.startIndex;
            int x = this.leftPos + INV_X + 1 + localIndex % COLS * 18;
            int row = localIndex / COLS;
            int y = this.topPos + INV_Y + 1 + row * 18;
            this.minecraft.getItemRenderer().renderAndDecorateItem(lootItems.get(i), x, y);
            this.minecraft.getItemRenderer().renderGuiItemDecorations(this.font, lootItems.get(i), x, y);
        }

        // Also does the Selected Item at the bottom
        int x = this.leftPos + SELECTED_ITEM_X + 1;
        int y = this.topPos + SELECTED_ITEM_Y + 1;
        this.menu.blockEntity.getCapability(ForgeCapabilities.ITEM_HANDLER).ifPresent(handler -> {
            ItemStack selected = handler.getStackInSlot(this.menu.getActiveItem());
            this.minecraft.getItemRenderer().renderAndDecorateItem(selected, x, y);
            this.minecraft.getItemRenderer().renderGuiItemDecorations(this.font, selected, x, y);
        });
    }

    private boolean mouseClickedPool(double mouseX, double mouseY, int pButton) {
        // Button ID: 0-11 pools, 12/14/16 dec, 13/15/17 inc
        // Stonecutter for Pool click
        int x = this.leftPos + POOLS_X;
        int y = this.topPos + POOLS_Y;
        int buttonCounter = -1;
        // Setting active pool
        for(int i = 0; i < NUM_POOLS; ++i) {
            buttonCounter++;
            int x1 = (int)mouseX - (x + i % 4 * POOL_BTN_W);
            int y1 = (int)mouseY - (y + i / 4 * POOL_BTN_H);
            if (x1 >= 0 && y1 >= 0 && x1 < POOL_BTN_W && y1 < POOL_BTN_H && this.menu.clickMenuButton(this.minecraft.player, buttonCounter)) {
                Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_STONECUTTER_SELECT_RECIPE, 1.0F));
                this.minecraft.gameMode.handleInventoryButtonClick((this.menu).containerId, buttonCounter);
                return true;
            }
        }

        // Arrows
        int[] widths = {ROLLS_MIN_X, ROLLS_MAX_X, ROLLS_BONUS_X};
        for (int i = 0; i < widths.length; i++) {
            // Dec arrow
            buttonCounter++;
            int arrowX = this.leftPos + widths[i] + 3;
            int arrowY = this.topPos + ROLLS_Y + 13;
            if (inRect(mouseX, mouseY, arrowX, arrowX + ARROW_BTN_W, arrowY, arrowY + ARROW_BTN_H) && this.menu.clickMenuButton(this.minecraft.player, buttonCounter)) {
                Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_STONECUTTER_SELECT_RECIPE, 1.0F));
                this.minecraft.gameMode.handleInventoryButtonClick((this.menu).containerId, buttonCounter);
                return true;
            }
            // Inc arrow
            buttonCounter++;
            arrowY = this.topPos + ROLLS_Y - 8;
            if (inRect(mouseX, mouseY, arrowX, arrowX + ARROW_BTN_W, arrowY, arrowY + ARROW_BTN_H) && this.menu.clickMenuButton(this.minecraft.player, buttonCounter)) {
                Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_STONECUTTER_SELECT_RECIPE, 1.0F));
                this.minecraft.gameMode.handleInventoryButtonClick((this.menu).containerId, buttonCounter);
                return true;
            }
        }

        // Reset
        buttonCounter++;
        x = this.leftPos + POOL_RESET_X;
        y = this.topPos + POOL_RESET_Y;
        if (inRect(mouseX, mouseY, x, x + RESET_WH, y, y + RESET_WH) && this.menu.clickMenuButton(this.minecraft.player, buttonCounter)) {
            Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_STONECUTTER_SELECT_RECIPE, 1.0F));
            this.minecraft.gameMode.handleInventoryButtonClick((this.menu).containerId, buttonCounter);
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, pButton);
    }

    private boolean mouseClickedItem(double mouseX, double mouseY, int pButton) {
        // Button ID: 0/2/4 dec, 1/3/5 inc, 6 back button, 7 reset
        int buttonCounter = -1;
        // Icon Selection should not process arrow clicks
        if (this.menu.getActiveItemLocal() < 27) {
            // Arrows
            int[] widths = {COUNTS_MIN_X, COUNTS_MAX_X, COUNTS_WEIGHT_X};
            for (int i = 0; i < widths.length; i++) {
                // Dec arrow
                buttonCounter++;
                int arrowX = this.leftPos + widths[i] + 2;
                int arrowY = this.topPos + COUNTS_Y + INT_BOX_H + 1;
                if (inRect(mouseX, mouseY, arrowX, arrowX + ARROW_BTN_W, arrowY, arrowY + ARROW_BTN_H) && this.menu.clickMenuButton(this.minecraft.player, buttonCounter)) {
                    Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_STONECUTTER_SELECT_RECIPE, 1.0F));
                    this.minecraft.gameMode.handleInventoryButtonClick((this.menu).containerId, buttonCounter);
                    return true;
                }
                // Inc arrow
                buttonCounter++;
                arrowY = this.topPos + COUNTS_Y - 8;
                if (inRect(mouseX, mouseY, arrowX, arrowX + ARROW_BTN_W, arrowY, arrowY + ARROW_BTN_H) && this.menu.clickMenuButton(this.minecraft.player, buttonCounter)) {
                    Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_STONECUTTER_SELECT_RECIPE, 1.0F));
                    this.minecraft.gameMode.handleInventoryButtonClick((this.menu).containerId, buttonCounter);
                    return true;
                }
            }
        } else {
            // We just skipped clicks for buttons 0-5 above, catch the counter up before continuing.
            buttonCounter = 5;
        }

        // Back
        buttonCounter++;
        int x = this.leftPos + BACK_X;
        int y = this.topPos + BACK_Y;
        if (inRect(mouseX, mouseY, x, x + BACK_BTN_W, y, y + BACK_BTN_H) && this.menu.clickMenuButton(this.minecraft.player, buttonCounter)) {
            Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_STONECUTTER_SELECT_RECIPE, 1.0F));
            this.minecraft.gameMode.handleInventoryButtonClick((this.menu).containerId, buttonCounter);
            return true;
        }

        // Reset
        buttonCounter++;
        x = this.leftPos + INV_RESET_X;
        y = this.topPos + INV_RESET_Y;
        if (inRect(mouseX, mouseY, x, x + RESET_WH, y, y + RESET_WH) && this.menu.clickMenuButton(this.minecraft.player, buttonCounter)) {
            Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_STONECUTTER_SELECT_RECIPE, 1.0F));
            this.minecraft.gameMode.handleInventoryButtonClick((this.menu).containerId, buttonCounter);
            return true;
        }

        // Scroller
        x = this.leftPos + SCROLLER_X;
        y = this.topPos + SCROLLER_Y;
        if (inRect(mouseX, mouseY, x, x + SCROLLER_W, y, y + SCROLLER_FULL_HEIGHT)) {
            this.scrolling = true;
            return true;
        }

        // LootItem
        buttonCounter++;
        x = this.leftPos + INV_X;
        y = this.topPos + INV_Y;
        if (inRect(mouseX, mouseY, x, x + 18*COLS, y, y + 18*ROWS)) {
            List<ItemStack> lootItems = this.menu.lootItems;
            for (int i = this.startIndex; i < this.startIndex + ROWS * COLS && i < lootItems.size(); i++) {
                int localIndex = i - this.startIndex;
                x = this.leftPos + INV_X + localIndex % COLS * 18;
                int row = localIndex / COLS;
                y = this.topPos + INV_Y + row * 18;
                // ButtonCounter should always == 8 here. We will take this into account in the menu#clickMenuButton to get back to i
                if (inRect(mouseX, mouseY, x, x + 18, y, y + 18) && this.menu.clickMenuButton(this.minecraft.player, buttonCounter + i)) {
                    Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_STONECUTTER_SELECT_RECIPE, 1.0F));
                    this.minecraft.gameMode.handleInventoryButtonClick((this.menu).containerId, buttonCounter + i);
                    return true;
                }
            }
        }

        return super.mouseClicked(mouseX, mouseY, pButton);
    }

    private void slotClickedPool(Slot pSlot, int pSlotId, int pMouseButton, ClickType pType) {
        // Due to not knowing the number of slots in the scrolling item view,
        // we are putting the fixed buttons first. So 12 pools + 6 arrows + 1 reset before slots
        int buttonNum = pSlotId + POOL_BTNS;
        // Single click = PICKUP, Double click = PICKUP_ALL, Middle button = CLONE
        if (pType == ClickType.PICKUP || pType == ClickType.PICKUP_ALL || pType == ClickType.CLONE) {
            if (this.menu.clickMenuButton(this.minecraft.player, buttonNum)) {
                Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_STONECUTTER_SELECT_RECIPE, 1.0F));
                this.minecraft.gameMode.handleInventoryButtonClick((this.menu).containerId, buttonNum);
            }
            return;
        }
        super.slotClicked(pSlot, pSlotId, pMouseButton, pType);
    }

    private void slotClickedItem(Slot pSlot, int pSlotId, int pMouseButton, ClickType pType) {
        // Due to not knowing the number of slots in the scrolling item view,
        // we are putting the fixed buttons first. So 6 arrows + 1 back button + reset before slots
        int buttonNum = pSlotId + INV_BTNS;
        // Single click = PICKUP, Double click = PICKUP_ALL, Middle button = CLONE
        if (pType == ClickType.PICKUP || pType == ClickType.PICKUP_ALL || pType == ClickType.CLONE) {
            if (this.menu.clickMenuButton(this.minecraft.player, buttonNum)) {
                Minecraft.getInstance().getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_STONECUTTER_SELECT_RECIPE, 1.0F));
                this.minecraft.gameMode.handleInventoryButtonClick((this.menu).containerId, pSlotId);
            }
            return;
        }
        super.slotClicked(pSlot, pSlotId, pMouseButton, pType);
    }

    private void renderPoolLabels(PoseStack pPoseStack) {
        this.font.draw(pPoseStack, this.title, (float)this.titleLabelX, (float)this.titleLabelY, 4210752);
        this.font.draw(pPoseStack, Component.translatable("gui.dungeon_diy.loot_chest.pools"), (float)POOLS_X, (float)POOLS_Y - 10, 4210752);
        this.font.draw(pPoseStack, Component.translatable("gui.dungeon_diy.loot_chest.pool_config"), (float)POOL_CONF_X, (float)POOLS_Y - 10, 4210752);
        this.font.draw(pPoseStack, Component.translatable("gui.dungeon_diy.loot_chest.pool_items"), (float)POOL_ITEMS_X + 1, (float)POOL_ITEMS_Y - 10, 4210752);
        if (!this.menu.hasActive()) {
            return;
        }
        int[] rolls = this.menu.getActiveRolls();
        drawCenteredString(pPoseStack, this.font, Component.literal(String.valueOf(rolls[0])), ROLLS_MIN_X + 8, ROLLS_Y + 2, 373737);
        drawCenteredString(pPoseStack, this.font, Component.literal(String.valueOf(rolls[1])), ROLLS_MAX_X + 8, ROLLS_Y + 2, 373737);
        drawCenteredString(pPoseStack, this.font, Component.literal(String.valueOf(rolls[2])), ROLLS_BONUS_X + 8, ROLLS_Y + 2, 373737);
    }

    private void renderItemLabels(PoseStack pPoseStack) {
        if (this.menu.getActiveItemLocal() == 27) {
            this.font.draw(pPoseStack, Component.translatable("gui.dungeon_diy.loot_chest.select_icon"), (float) this.titleLabelX, (float) this.titleLabelY, 4210752);
        } else {
            this.font.draw(pPoseStack, Component.translatable("gui.dungeon_diy.loot_chest.select_item"), (float) this.titleLabelX, (float) this.titleLabelY, 4210752);
            int[] counts = this.menu.getActiveCounts();
            drawCenteredString(pPoseStack, this.font, Component.literal(String.valueOf(counts[0])), COUNTS_MIN_X + 8, COUNTS_Y + 2, 373737);
            drawCenteredString(pPoseStack, this.font, Component.literal(String.valueOf(counts[1])), COUNTS_MAX_X + 8, COUNTS_Y + 2, 373737);
            drawCenteredString(pPoseStack, this.font, Component.literal(String.valueOf(counts[2])), COUNTS_WEIGHT_X + 8, COUNTS_Y + 2, 373737);
        }
    }

    @Override
    public boolean mouseDragged(double pMouseX, double pMouseY, int pButton, double pDragX, double pDragY) {
        if (this.scrolling && this.menu.canScroll()) {
            int top = this.topPos + SCROLLER_Y;
            this.scrollOffs = ((float)pMouseY - (float)top - (float)(SCROLLER_H/2)) / ((float)(SCROLLER_FULL_HEIGHT - SCROLLER_H));
            this.scrollOffs = Mth.clamp(this.scrollOffs, 0.0F, 1.0F);
            this.startIndex = (int)((double)(this.scrollOffs * (float)this.menu.getOffscreenRows()) + 0.5D) * 9;
            return true;
        } else {
            return super.mouseDragged(pMouseX, pMouseY, pButton, pDragX, pDragY);
        }
    }

    @Override
    public boolean mouseScrolled(double pMouseX, double pMouseY, double pDelta) {
        if (this.menu.canScroll()) {
            int offScreenRows = this.menu.getOffscreenRows();
            float f = (float)pDelta / (float)offScreenRows;
            this.scrollOffs = Mth.clamp(this.scrollOffs - f, 0.0F, 1.0F);
            this.startIndex = (int)((double)(this.scrollOffs * (float)offScreenRows) + 0.5D) * 9;
        }
        return true;
    }

    // For methods that supply mouseXY as ints
    private boolean inRect(int mouseX, int mouseY, int x1, int x2, int y1, int y2) {
        return mouseX >= x1 && mouseX < x2 && mouseY >= y1 && mouseY < y2;
    }

    // For click methods that supply mouseXY as doubles
    private boolean inRect(double mouseX, double mouseY, int x1, int x2, int y1, int y2) {
        return inRect(mouseX, mouseY, (double) x1, (double) x2, (double) y1, (double) y2);
    }

    private boolean inRect(double mouseX, double mouseY, double x1, double x2, double y1, double y2) {
        return mouseX >= x1 && mouseX < x2 && mouseY >= y1 && mouseY < y2;
    }
}
