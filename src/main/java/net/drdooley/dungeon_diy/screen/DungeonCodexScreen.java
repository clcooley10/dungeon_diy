package net.drdooley.dungeon_diy.screen;

import com.mojang.blaze3d.systems.RenderSystem;
import net.drdooley.dungeon_diy.Dungeon.DungeonNode;
import net.drdooley.dungeon_diy.Dungeon.ReplacementEntry;
import net.drdooley.dungeon_diy.DungeonDIY;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;

import java.util.List;

public class DungeonCodexScreen extends AbstractContainerScreen<DungeonCodexMenu> {
    private final ResourceLocation GUI_TEXTURE;

    public DungeonCodexScreen(DungeonCodexMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 192;
        this.imageHeight = 192;
        GUI_TEXTURE = ResourceLocation.fromNamespaceAndPath(DungeonDIY.MOD_ID, "textures/gui/dungeon_codex/" + menu.activePage + ".png");
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        // Draw nothing.
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float pPartialTick, int pMouseX, int pMouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, GUI_TEXTURE);

        int x = (width - imageWidth) / 2;
        int y = (height - imageHeight) / 2;

        guiGraphics.blit(GUI_TEXTURE, x, y, 0, 0, imageWidth, imageHeight);

        x = this.leftPos + 10;
        y = this.topPos + 10;

        guiGraphics.drawString(
          this.font,
          "Dungeon Nodes:",
          x,
          y,
          0xFFFFFF
        );

        y += 15;

        for (DungeonNode node : menu.getNodes()) {

            guiGraphics.drawString(
              this.font,
              node.getPos().toShortString(),
              x,
              y,
              0xFFFFFF
            );
            List<ReplacementEntry> entries = node.copyReplacements();
            for (ReplacementEntry e : entries) {
                guiGraphics.renderItem(new ItemStack(e.getState().getBlock().asItem()), x, y);
                y += 12;
            }
        }
    }

    @Override
    public void render(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        super.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
        this.renderTooltip(pGuiGraphics, pMouseX, pMouseY);
    }
}
