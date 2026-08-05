package net.drdooley.dungeon_diy.Screen;

import com.mojang.blaze3d.systems.RenderSystem;
import net.drdooley.dungeon_diy.Dungeon.DungeonNode;
import net.drdooley.dungeon_diy.Dungeon.ReplacementEntry;
import net.drdooley.dungeon_diy.DungeonDIY;
import net.drdooley.dungeon_diy.Network.ChangeReplacementEntryWeightPayload;
import net.drdooley.dungeon_diy.Network.ExportReplacementPrefabPayload;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CreativeModeInventoryScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.List;

public class DungeonCodexScreen extends AbstractContainerScreen<DungeonCodexMenu> {
    private final ResourceLocation GUI_TEXTURE;
    private static final ResourceLocation SCROLLER_SPRITE = ResourceLocation.withDefaultNamespace("container/villager/scroller");
    private static final ResourceLocation SCROLLER_DISABLED_SPRITE = ResourceLocation.withDefaultNamespace("container/villager/scroller_disabled");
    private static final ResourceLocation SCALE_ARMS = ResourceLocation.fromNamespaceAndPath(DungeonDIY.MOD_ID,"textures/gui/dungeon_codex/scale_arms.png");
    private static final ResourceLocation IMPORT_EXPORT = ResourceLocation.fromNamespaceAndPath(DungeonDIY.MOD_ID,"textures/gui/dungeon_codex/import_export.png");
    private static final ResourceLocation PLUS_MINUS = ResourceLocation.fromNamespaceAndPath(DungeonDIY.MOD_ID,"textures/gui/dungeon_codex/plus_minus.png");

    private static final int TEXTURE_WIDTH = 512;
    private static final int TEXTURE_HEIGHT = 256;
    private static final int VISIBLE_NODES = 7;
    private static final int NODE_BLOCKPOS_X = 54;
    private static final int REPLACEMENT_SLOT_SIZE = 18;
    private static final int REPLACEMENT_COLUMNS = 9;

    private static final int REPLACEMENT_START_X = 107;
    private static final int REPLACEMENT_START_Y = 71;

    private static final int SELECTED_SLOT_X = 179;
    private static final int SELECTED_SLOT_Y = 25;
    private static final int SELECTED_WEIGHT_X = 188;
    private static final int SELECTED_WEIGHT_Y = 52;

    private static final int SCALE_ARM_WIDTH = 9;
    private static final int SCALE_ARM_HEIGHT = 12;
    private static final int SCALE_LEFT_ARM_X = 167;
    private static final int SCALE_RIGHT_ARM_X = 200;
    private static final int SCALE_ARM_Y = 51;

    private static final int SQUARE_BUTTON_LENGTH = 14;
    private static final int IMPORT_REPL_PREFAB_X = 107;
    private static final int IMPORT_REPL_PREFAB_Y = 145;
    private static final int EXPORT_REPL_PREFAB_X = 126;
    private static final int EXPORT_REPL_PREFAB_Y = 145;
    private static final int EXPORT_REPL_EDITBOX_X = 145;
    private static final int ADD_REPL_X = 107;
    private static final int ADD_REPL_Y = 127;
    private static final int REMOVE_REPL_X = 126;
    private static final int REMOVE_REPL_Y = 127;

    private final NodeButton[] nodeButtons = new NodeButton[VISIBLE_NODES];

    private int scrollOff;
    private int selectedNode;
    private boolean leftScaleHovered;
    private boolean rightScaleHovered;
    private boolean importHovered;
    private boolean exportHovered;
    private boolean addReplHovered;
    private boolean delReplHovered;

    private EditBox prefabNameBox;
    // Shaking
    private int shakeTicks = 0;
    // Pattern of horizontal offsets
    private static final int[] SHAKE_OFFSETS = {-4, 4, -3, 3, -2, 2, -1, 1, 0, 0};
    // private final PieChartWidget pieChart = new PieChartWidget();

    public DungeonCodexScreen(DungeonCodexMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.imageWidth = 276;
        GUI_TEXTURE = ResourceLocation.fromNamespaceAndPath(DungeonDIY.MOD_ID, "textures/gui/dungeon_codex/" + menu.activePage + ".png");
    }

    @Override
    protected void init() {
        super.init();

        int i = (this.width - this.imageWidth) / 2;
        int j = (this.height - this.imageHeight) / 2;
        int k = j + 16 + 2;

        for (int l = 0; l < VISIBLE_NODES; l++) {
            this.nodeButtons[l] = this.addRenderableWidget(
              new DungeonCodexScreen.NodeButton(i + 5, k, l, button -> {
                  if (button instanceof DungeonCodexScreen.NodeButton nodeButton) {
                      this.selectedNode = nodeButton.getIndex() + this.scrollOff;
                      this.postButtonClick();
                  }
              })
            );
            k += 20;
        }

        prefabNameBox = new EditBox(
          font,
          leftPos + EXPORT_REPL_EDITBOX_X,
          topPos + EXPORT_REPL_PREFAB_Y,
          80,
          SQUARE_BUTTON_LENGTH,
          Component.literal("Prefab Name")
        );
        prefabNameBox.setMaxLength(32);
        prefabNameBox.setCanLoseFocus(true);
        prefabNameBox.setVisible(true);
        prefabNameBox.setHint(Component.literal("Name"));

        addRenderableWidget(prefabNameBox);
        prefabNameBox.setFocused(true);
        this.setFocused(prefabNameBox);
    }

    private void postButtonClick() {
        this.menu.setSelectedNodeIndex(this.selectedNode);
        DungeonNode node = menu.getNodes().get(selectedNode);
        // pieChart.rebuild(node.copyReplacements());
        // Eventually send a packet if the server needs to know immediately.
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

        guiGraphics.blit(GUI_TEXTURE, x, y, 0, 0, imageWidth, imageHeight, TEXTURE_WIDTH, TEXTURE_HEIGHT);
        renderReplacementSlots(guiGraphics, pMouseX, pMouseY);
        renderSelectedReplacement(guiGraphics);
        renderScaleHover(guiGraphics);
        renderSquareButtons(guiGraphics, pMouseX, pMouseY);
    }

    private void renderReplacementSlots(GuiGraphics graphics, int mouseX, int mouseY) {
        DungeonNode node = menu.getNodes().get(menu.getSelectedNodeIndex());
        if (node == null) {
            return;
        }
        List<ReplacementEntry> replacements = node.copyReplacements();
        for (int i = 0; i < 27; i++) {
            int row = i / REPLACEMENT_COLUMNS;
            int col = i % REPLACEMENT_COLUMNS;
            int x = leftPos + REPLACEMENT_START_X + col * REPLACEMENT_SLOT_SIZE;
            int y = topPos + REPLACEMENT_START_Y + row * REPLACEMENT_SLOT_SIZE;

            boolean hovered = mouseX >= x && mouseX < x + REPLACEMENT_SLOT_SIZE && mouseY >= y && mouseY < y + REPLACEMENT_SLOT_SIZE;

            // Highlight hovered fake slot
            if (hovered) {
                graphics.fill(x + 1, y + 1, x + REPLACEMENT_SLOT_SIZE - 1, y + REPLACEMENT_SLOT_SIZE - 1, 0x80FFFFFF);
            }

            if (i < replacements.size()) {
                ReplacementEntry entry = replacements.get(i);
                ItemStack stack = new ItemStack(entry.getState().getBlock().asItem());
                stack.setCount(entry.getWeight());
                graphics.renderFakeItem(stack, x + 1, y + 1);
                graphics.renderItemDecorations(font, stack, x + 1, y + 1);
            }
        }
    }
    private void renderSelectedReplacement(GuiGraphics graphics) {
        int x = leftPos + SELECTED_SLOT_X;
        int y = topPos + SELECTED_SLOT_Y;
        ReplacementEntry selected = menu.getSelectedReplacement();
        if (selected == null) {
            return;
        }

        ItemStack stack = new ItemStack(selected.getState().getBlock().asItem());
        graphics.renderFakeItem(stack, x + 1, y + 1);
        String weight = String.valueOf(selected.getWeight());
        graphics.drawCenteredString(font, weight, leftPos + SELECTED_WEIGHT_X, topPos + SELECTED_WEIGHT_Y, 0xFFFFFF);
    }

    private int getReplacementSlot(double mouseX, double mouseY) {
        for (int i = 0; i < 27; i++) {
            int row = i / REPLACEMENT_COLUMNS;
            int col = i % REPLACEMENT_COLUMNS;
            int x = leftPos + REPLACEMENT_START_X + col * REPLACEMENT_SLOT_SIZE;
            int y = topPos + REPLACEMENT_START_Y + row * REPLACEMENT_SLOT_SIZE;

            if (mouseX >= x && mouseX < x + REPLACEMENT_SLOT_SIZE && mouseY >= y && mouseY < y + REPLACEMENT_SLOT_SIZE) {
                return i;
            }
        }
        return -1;
    }

    private void updateHoverBools(int mouseX, int mouseY) {
        leftScaleHovered = mouseX >= leftPos + SCALE_LEFT_ARM_X && mouseX < leftPos + SCALE_LEFT_ARM_X + SCALE_ARM_WIDTH && mouseY >= topPos + SCALE_ARM_Y && mouseY < topPos + SCALE_ARM_Y + SCALE_ARM_HEIGHT;
        rightScaleHovered = mouseX >= leftPos + SCALE_RIGHT_ARM_X && mouseX < leftPos + SCALE_RIGHT_ARM_X + SCALE_ARM_WIDTH && mouseY >= topPos + SCALE_ARM_Y && mouseY < topPos + SCALE_ARM_Y + SCALE_ARM_HEIGHT;
        importHovered = mouseX >= leftPos + IMPORT_REPL_PREFAB_X && mouseX < leftPos + IMPORT_REPL_PREFAB_X + SQUARE_BUTTON_LENGTH && mouseY >= topPos + IMPORT_REPL_PREFAB_Y && mouseY < topPos + IMPORT_REPL_PREFAB_Y + SQUARE_BUTTON_LENGTH;
        exportHovered = mouseX >= leftPos + EXPORT_REPL_PREFAB_X && mouseX < leftPos + EXPORT_REPL_PREFAB_X + SQUARE_BUTTON_LENGTH && mouseY >= topPos + EXPORT_REPL_PREFAB_Y && mouseY < topPos + EXPORT_REPL_PREFAB_Y + SQUARE_BUTTON_LENGTH;
        addReplHovered = mouseX >= leftPos + ADD_REPL_X && mouseX < leftPos + ADD_REPL_X + SQUARE_BUTTON_LENGTH && mouseY >= topPos + ADD_REPL_Y && mouseY < topPos + ADD_REPL_Y + SQUARE_BUTTON_LENGTH;
        delReplHovered = mouseX >= leftPos + REMOVE_REPL_X && mouseX < leftPos + REMOVE_REPL_X + SQUARE_BUTTON_LENGTH && mouseY >= topPos + REMOVE_REPL_Y && mouseY < topPos + REMOVE_REPL_Y + SQUARE_BUTTON_LENGTH;
    }

    private void renderScaleHover(GuiGraphics graphics) {
        if (!leftScaleHovered && !rightScaleHovered) {
            return;
        }
        int x;
        int offset = 0;
        if (leftScaleHovered) {
            x = leftPos + SCALE_LEFT_ARM_X;
        } else {
            x = leftPos + SCALE_RIGHT_ARM_X;
            offset = 9;
        }
        int y = topPos + SCALE_ARM_Y;
        graphics.blit(SCALE_ARMS, x, y, offset, 0, SCALE_ARM_WIDTH, SCALE_ARM_HEIGHT, 18, 18);
    }

    private void renderSquareButtons(GuiGraphics graphics, int mouseX, int mouseY) {
        int u_offset = 0;
        int v_offset = 0;

        if (importHovered) { v_offset = SQUARE_BUTTON_LENGTH; }
        graphics.blit(IMPORT_EXPORT, leftPos + IMPORT_REPL_PREFAB_X, topPos + IMPORT_REPL_PREFAB_Y, u_offset, v_offset, SQUARE_BUTTON_LENGTH, SQUARE_BUTTON_LENGTH, 28, 28);

        v_offset = 0;
        if (addReplHovered) { v_offset = SQUARE_BUTTON_LENGTH; }
        graphics.blit(PLUS_MINUS, leftPos + ADD_REPL_X, topPos + ADD_REPL_Y, u_offset, v_offset, SQUARE_BUTTON_LENGTH, SQUARE_BUTTON_LENGTH, 28, 28);

        u_offset = SQUARE_BUTTON_LENGTH;
        v_offset = 0;

        if (exportHovered) { v_offset = SQUARE_BUTTON_LENGTH; }
        graphics.blit(IMPORT_EXPORT, leftPos + EXPORT_REPL_PREFAB_X, topPos + EXPORT_REPL_PREFAB_Y, u_offset, v_offset, SQUARE_BUTTON_LENGTH, SQUARE_BUTTON_LENGTH, 28, 28);

        v_offset = 0;
        if (delReplHovered) { v_offset = SQUARE_BUTTON_LENGTH; }
        graphics.blit(PLUS_MINUS, leftPos + REMOVE_REPL_X, topPos + REMOVE_REPL_Y, u_offset, v_offset, SQUARE_BUTTON_LENGTH, SQUARE_BUTTON_LENGTH, 28, 28);
    }

    private void updateNodeButtons() {
        List<DungeonNode> nodes = menu.getNodes();
        for (int i = 0; i < nodeButtons.length; i++) {
            int index = scrollOff + i;
            if (index < nodes.size()) {
                nodeButtons[i].visible = true;
                nodeButtons[i].active = true;
                nodeButtons[i].setNode(nodes.get(index));
                nodeButtons[i].setSelected(index == selectedNode);
            } else {
                nodeButtons[i].visible = false;
            }
        }
    }

    @Override
    public void render(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        updateNodeButtons();
        updateHoverBools(pMouseX, pMouseY);
        super.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
        for (NodeButton button : nodeButtons) {
            if (button.isHoveredOrFocused()) {
                button.renderToolTip(pGuiGraphics, pMouseX, pMouseY);
            }
        }
        int i = (this.width - this.imageWidth) / 2;
        int j = (this.height - this.imageHeight) / 2;
        List<DungeonNode> nodes = menu.getNodes();
        this.renderScroller(pGuiGraphics, i, j, nodes);
        this.renderTooltip(pGuiGraphics, pMouseX, pMouseY);
        DungeonNode node = nodes.get(menu.getSelectedNodeIndex());

        if (node != null) {
            // Future maybe, pie chart showing relative probability of each replacementEntry
            // pieChart.render(pGuiGraphics, leftPos + 145, topPos + 32, 20);
        }
    }

    @Override
    protected void containerTick() {
        super.containerTick();

        if (this.shakeTicks > 0) {
            this.shakeTicks--;

            // Map the remaining ticks to our offset array index
            int index = SHAKE_OFFSETS.length - 1 - this.shakeTicks;
            int currentOffset = SHAKE_OFFSETS[index];

            // Mutate the X coordinate of the existing widget directly
            this.prefabNameBox.setX(leftPos + EXPORT_REPL_EDITBOX_X + currentOffset);
        } else {
            // Ensure it snaps perfectly back to center when done shaking
            this.prefabNameBox.setX(leftPos + EXPORT_REPL_EDITBOX_X);
        }
    }

    @Override
    protected void renderTooltip(GuiGraphics guiGraphics, int x, int y) {
        List<Component> tooltip = new ArrayList<>();
        if (leftScaleHovered) {
            tooltip.add(Component.translatable("gui.dungeon_diy.dungeon_codex.decrease_weight"));
        } else if (rightScaleHovered) {
            tooltip.add(Component.translatable("gui.dungeon_diy.dungeon_codex.increase_weight"));
        } else if (importHovered) {
            tooltip.add(Component.translatable("gui.dungeon_diy.dungeon_codex.import_repl_prefab"));
        } else if (exportHovered) {
            tooltip.add(Component.translatable("gui.dungeon_diy.dungeon_codex.export_repl_prefab"));
        } else if (addReplHovered) {
            tooltip.add(Component.translatable("gui.dungeon_diy.dungeon_codex.add_repl_entry"));
        } else if (delReplHovered) {
            tooltip.add(Component.translatable("gui.dungeon_diy.dungeon_codex.del_repl_entry"));
        }
        if (!tooltip.isEmpty()) {
            guiGraphics.renderComponentTooltip(font, tooltip, x, y);
        }
        super.renderTooltip(guiGraphics, x, y);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        int max = Math.max(0, menu.getNodeCount() - VISIBLE_NODES);
        scrollOff = Mth.clamp(scrollOff - (int)Math.signum(scrollY), 0, max);
        // Lose focus on scroll, not ideal, but prevent focus from staying on NodeButton after the containing node was scrolled to a diff index
        if ((this.getFocused() instanceof NodeButton)) {
            this.setFocused(null);
        }
        updateNodeButtons();
        return true;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            // Scales
            if (leftScaleHovered) {
                this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                PacketDistributor.sendToServer(new ChangeReplacementEntryWeightPayload(menu.getDungeonId(), menu.getSelectedNode().getPos(), menu.getSelectedReplacementIndex(), false));
                return true;
            }
            if (rightScaleHovered) {
                this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                PacketDistributor.sendToServer(new ChangeReplacementEntryWeightPayload(menu.getDungeonId(), menu.getSelectedNode().getPos(), menu.getSelectedReplacementIndex(), true));
                return true;
            }
            // Replacement Entry Slots
            int index = getReplacementSlot(mouseX, mouseY);
            if (index >= 0) {
                DungeonNode node = menu.getSelectedNode();
                if (index < node.getReplacements().size()) {
                    this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                    menu.setSelectedReplacementIndex(index);
                    return true;
                }
            }
            // Import/Export Add/Delete
            if (exportHovered) {
                this.minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                String prefabName = prefabNameBox.getValue();
                if (!prefabName.isEmpty()) {
                    PacketDistributor.sendToServer(new ExportReplacementPrefabPayload(menu.getDungeonId(), menu.getSelectedNode().getPos(), prefabName));
                } else {
                    shakeTicks = SHAKE_OFFSETS.length;
                }
                return true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (prefabNameBox != null && prefabNameBox.isFocused()) {
            if (prefabNameBox.keyPressed(keyCode, scanCode, modifiers)) {
                return true;
            }
            // Allow escape to close the screen
            if (keyCode != 256) {
                return true;
            }
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        if (prefabNameBox != null && prefabNameBox.isFocused()) {
            if (prefabNameBox.charTyped(codePoint, modifiers)) {
                return true;
            }
        }
        return super.charTyped(codePoint, modifiers);
    }

    private void renderScroller(GuiGraphics guiGraphics, int posX, int posY, List<DungeonNode> nodes) {
        int i = nodes.size() + 1 - VISIBLE_NODES;
        if (i > 1) {
            int j = 139 - (27 + (i - 1) * 139 / i);
            int k = 1 + j / i + 139 / i;
            int l = 113;
            int i1 = Math.min(l, this.scrollOff * k);
            if (this.scrollOff == i - 1) {
                i1 = l;
            }
            guiGraphics.blitSprite(SCROLLER_SPRITE, posX + 94, posY + 18 + i1, 0, 6, 27);
        } else {
            guiGraphics.blitSprite(SCROLLER_DISABLED_SPRITE, posX + 94, posY + 18, 0, 6, 27);
        }
    }

    @OnlyIn(Dist.CLIENT)
    class NodeButton extends Button {
        final int index;
        private DungeonNode node;
        private boolean selected;

        public NodeButton(int x, int y, int index, Button.OnPress onPress) {
            super(x, y, 88, 20, CommonComponents.EMPTY, onPress, DEFAULT_NARRATION);
            this.index = index;
            this.visible = false;
        }

        public int getIndex() {
            return this.index;
        }

        public void setNode(DungeonNode node) {
            this.node = node;
        }

        public void setSelected(boolean selected) {
            this.selected = selected;
        }

        public void renderToolTip(GuiGraphics graphics, int mouseX, int mouseY) {
            if (!this.isHovered || this.node == null) {
                return;
            }
            List<Component> tooltip = new ArrayList<>();
            tooltip.add(Component.literal(node.getPos().toShortString()));
            tooltip.add(Component.literal(node.sizeReplacements() + " replacement(s)"));
            graphics.renderComponentTooltip(font, tooltip, mouseX, mouseY);
        }

        @Override
        protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            super.renderWidget(graphics, mouseX, mouseY, partialTick);
            if (node == null) return;
            graphics.renderFakeItem(node.heavyWeightReplacementStack(), getX() + 3, getY() + 2);
            String pos = node.getPos().getX() + ", " + node.getPos().getZ();
            pos = font.plainSubstrByWidth(pos, 60);
            graphics.drawCenteredString(font, pos, leftPos + NODE_BLOCKPOS_X, getY() + 6, 0xFFFFFF);
            if (selected) {
                graphics.fill(getX(), getY(), getX() + width, getY() + height, 0x40FFFFFF);
            }
        }
    }
}
