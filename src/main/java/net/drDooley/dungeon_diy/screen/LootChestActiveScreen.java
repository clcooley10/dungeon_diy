package net.drDooley.dungeon_diy.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.drDooley.dungeon_diy.DDIY;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.AbstractContainerMenu;

public class LootChestActiveScreen extends AbstractContainerScreen<LootChestActiveMenu> {
    private static final ResourceLocation POOL_TEXTURE = new ResourceLocation(DDIY.MODID, "textures/gui/loot_chest_active.png");
    public LootChestActiveScreen(LootChestActiveMenu pMenu, Inventory pPlayerInventory, Component pTitle) {
        super(pMenu, pPlayerInventory, pTitle);
    }

    @Override
    protected void renderBg(PoseStack pPoseStack, float pPartialTick, int pMouseX, int pMouseY) {
        this.renderBackground(pPoseStack);
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, POOL_TEXTURE);
//        this.inventoryLabelY = this.imageHeight - 70;
//        this.imageWidth = 176;
//        this.imageHeight = 166;

        this.leftPos = (width - imageWidth) / 2;
        this.topPos = (height - imageHeight) / 2;
        this.blit(pPoseStack, leftPos, topPos, 0, 0, imageWidth, imageHeight);
    }
}
