package net.drDooley.dungeon_diy.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.drDooley.dungeon_diy.DDIY;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;


public class LootChestScreen extends AbstractContainerScreen<LootChestEditMenu> {

    private static final ResourceLocation TEXTURE = new ResourceLocation(DDIY.MODID, "textures/gui/loot_chest_edit_menu.png");
    private static final Component POOLS = Component.translatable("gui.dungeon_diy.loot_chest.pools");
    private static final Component POOL_CONFIG = Component.translatable("gui.dungeon_diy.loot_chest.pool_config");
    private static final Component POOL_ITEMS = Component.translatable("gui.dungeon_diy.loot_chest.pool_items");
    private static final int POOLS_X = 16;
    private static final int POOL_CONFIG_X = 95;
    private static final int POOLS_Y = 28;

    public LootChestScreen(LootChestEditMenu pMenu, Inventory pPlayerInventory, Component pTitle) {
        super(pMenu, pPlayerInventory, pTitle);
    }

    @Override
    protected void renderBg(PoseStack matrices, float delta, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, TEXTURE);
        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        this.inventoryLabelY = this.imageHeight - 70;
        this.blit(matrices, x, y, 0, 0, imageWidth, imageHeight);
    }

    @Override
    public void render(PoseStack pPoseStack, int mouseX, int mouseY, float delta) {
        this.renderBackground(pPoseStack);
        super.render(pPoseStack, mouseX, mouseY, delta);
        //drawMouseoverTooltip(matrices, mouseX, mouseY);
    }

    @Override
    protected void renderLabels(PoseStack pPoseStack, int pMouseX, int pMouseY) {
        this.font.draw(pPoseStack, this.title, (float)this.titleLabelX, (float)this.titleLabelY, 4210752);
        this.font.draw(pPoseStack, POOL_ITEMS, (float)this.inventoryLabelX, (float)this.inventoryLabelY, 4210752);
        this.font.draw(pPoseStack, POOLS, (float)POOLS_X, (float)POOLS_Y, 4210752);
        this.font.draw(pPoseStack, POOL_CONFIG, (float)POOL_CONFIG_X, (float)POOLS_Y, 4210752);
    }
}
