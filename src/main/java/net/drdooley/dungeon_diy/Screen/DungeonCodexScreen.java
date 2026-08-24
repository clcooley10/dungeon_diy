package net.drdooley.dungeon_diy.Screen;

import com.mojang.blaze3d.systems.RenderSystem;
import net.drdooley.dungeon_diy.Block.DDIYBlocks;
import net.drdooley.dungeon_diy.Dungeon.DungeonNode;
import net.drdooley.dungeon_diy.Dungeon.ReplacementEntry;
import net.drdooley.dungeon_diy.Dungeon.ReplacementPrefab;
import net.drdooley.dungeon_diy.DungeonDIY;
import net.drdooley.dungeon_diy.Network.*;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.ContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.Property;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.ArrayList;
import java.util.List;

public class DungeonCodexScreen extends AbstractContainerScreen<DungeonCodexMenu> {
    private static final ResourceLocation SCROLLER_SPRITE = ResourceLocation.withDefaultNamespace("container/villager/scroller");
    private static final ResourceLocation SCROLLER_DISABLED_SPRITE = ResourceLocation.withDefaultNamespace("container/villager/scroller_disabled");
    private static final ResourceLocation SCALE_ARMS = ResourceLocation.fromNamespaceAndPath(DungeonDIY.MOD_ID,"textures/gui/dungeon_codex/scale_arms.png");
    private static final ResourceLocation IMPORT_EXPORT = ResourceLocation.fromNamespaceAndPath(DungeonDIY.MOD_ID,"textures/gui/dungeon_codex/import_export.png");
    private static final ResourceLocation PLUS_MINUS = ResourceLocation.fromNamespaceAndPath(DungeonDIY.MOD_ID,"textures/gui/dungeon_codex/plus_minus.png");
    private static final ResourceLocation BACK = ResourceLocation.fromNamespaceAndPath(DungeonDIY.MOD_ID,"textures/gui/dungeon_codex/back.png");
    private static final ResourceLocation CHECK = ResourceLocation.fromNamespaceAndPath(DungeonDIY.MOD_ID,"textures/gui/dungeon_codex/checkmark.png");

    private static final int VISIBLE_SCROLL_BUTTONS = 7;
    private static final int SCALE_ARM_WIDTH = 9;
    private static final int SCALE_ARM_HEIGHT = 12;
    private static final int SCALE_LEFT_ARM_X = 167;
    private static final int SCALE_RIGHT_ARM_X = 200;
    private static final int SCALE_ARM_Y = 51;

    private CodexPage currentPage;
    private final NodeViewEditPage  nodeViewEditPage;
    private final ImportReplPage  importReplPage;
    private final AddReplEntryPage addReplEntryPage;
    private final PedestalEditPage pedestalEditPage;

    private final DungeonCodexMenu menu;

    public DungeonCodexScreen(DungeonCodexMenu menu, Inventory playerInventory, Component title) {
        super(menu, playerInventory, title);
        this.menu = menu;

        this.nodeViewEditPage = new NodeViewEditPage();
        this.importReplPage = new ImportReplPage();
        this.addReplEntryPage = new AddReplEntryPage();
        this.pedestalEditPage = new PedestalEditPage();

        this.currentPage = nodeViewEditPage;
    }

    @Override
    protected void init() {
        // These must be updated before super.init() to ensure the background renders properly
        if (currentPage == nodeViewEditPage || currentPage == importReplPage || currentPage == addReplEntryPage) {
            this.imageWidth = 276;
            this.imageHeight = 166;
        } else if(currentPage == pedestalEditPage) {
            this.imageWidth = 176;
            this.imageHeight = 222;
        }
        super.init();
        currentPage.init();
    }

    public void setPage(CodexPageEnum pageEnum) {
        CodexPage newPage = switch (pageEnum) {
            case REPL_PREFAB_IMPORT -> importReplPage;
            case ADD_REPL_ENTRY -> addReplEntryPage;
            case PEDESTAL_EDIT -> pedestalEditPage;
            default -> nodeViewEditPage;
        };
        setPage(newPage);
    }
    public void setPage(CodexPage page) {
        if (page == currentPage) { return; }

        if (currentPage != null) {
            currentPage.removed();
        }
        currentPage = page;
        menu.setActivePage(page.getPageEnum());
        this.init();
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        currentPage.renderLabels(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float pPartialTick, int pMouseX, int pMouseY) {
        currentPage.renderBg(guiGraphics, pPartialTick, pMouseX, pMouseY);
    }

    @Override
    public void render(GuiGraphics pGuiGraphics, int pMouseX, int pMouseY, float pPartialTick) {
        super.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
        currentPage.render(pGuiGraphics, pMouseX, pMouseY, pPartialTick);
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        currentPage.tick();
    }

    @Override
    protected void renderTooltip(GuiGraphics guiGraphics, int x, int y) {
        currentPage.renderTooltip(guiGraphics, x, y);
        super.renderTooltip(guiGraphics, x, y);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        return currentPage.mouseScrolled(mouseX, mouseY, scrollX, scrollY)
          || super.mouseScrolled(mouseX, mouseY, scrollX, scrollY);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        // No fallback to super
        return currentPage.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return currentPage.mouseClicked(mouseX, mouseY, button)
          || super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 256) { // ESC
            if (currentPage != nodeViewEditPage) {
                setPage(nodeViewEditPage);
                return true;
            }
        }
        return currentPage.keyPressed(keyCode, scanCode, modifiers)
          || super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        return currentPage.charTyped(codePoint, modifiers)
          || super.charTyped(codePoint, modifiers);
    }

    private void renderScroller(GuiGraphics guiGraphics, int posX, int posY, int itemCount, int scrollOffset) {
        int i = itemCount + 1 - VISIBLE_SCROLL_BUTTONS;
        if (i > 1) {
            int j = 139 - (27 + (i - 1) * 139 / i);
            int k = 1 + j / i + 139 / i;
            int l = 113;
            int i1 = Math.min(l, scrollOffset * k);
            if (scrollOffset == i - 1) {
                i1 = l;
            }
            guiGraphics.blitSprite(SCROLLER_SPRITE, posX + 94, posY + 18 + i1, 0, 6, 27);
        } else {
            guiGraphics.blitSprite(SCROLLER_DISABLED_SPRITE, posX + 94, posY + 18, 0, 6, 27);
        }
    }

    private void renderScaleHover(GuiGraphics graphics, boolean leftScaleHovered, boolean rightScaleHovered) {
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

    /*****************************************************************
     * Page classes, each defining their own buttons/slots/behaviors *
     *****************************************************************/

    private class NodeViewEditPage implements CodexPage {
        private static final ResourceLocation BACKGROUND = ResourceLocation.fromNamespaceAndPath(DungeonDIY.MOD_ID, "textures/gui/dungeon_codex/temp_codex_menu_base.png");

        private static final int TEXTURE_WIDTH = 512;
        private static final int TEXTURE_HEIGHT = 256;

        private static final int CENTERED_SCROLL_BUTTON_X = 54;

        private static final int REPLACEMENT_SLOT_SIZE = 18;
        private static final int REPLACEMENT_COLUMNS = 9;
        private static final int REPLACEMENT_START_X = 107;
        private static final int REPLACEMENT_START_Y = 71;

        private static final int SELECTED_SLOT_X = 179;
        private static final int SELECTED_SLOT_Y = 25;
        private static final int SELECTED_WEIGHT_X = 188;
        private static final int SELECTED_WEIGHT_Y = 52;

        private static final int IMPORT_REPL_PREFAB_X = 107;
        private static final int IMPORT_REPL_PREFAB_Y = 145;
        private static final int EXPORT_REPL_PREFAB_X = 126;
        private static final int EXPORT_REPL_PREFAB_Y = 145;
        private static final int EXPORT_REPL_EDITBOX_X = 145;
        private static final int ADD_REPL_X = 107;
        private static final int ADD_REPL_Y = 127;
        private static final int REMOVE_REPL_X = 126;
        private static final int REMOVE_REPL_Y = 127;
        private static final int SQUARE_BUTTON_LENGTH = 14;

        private static final int PEDESTAL_X = 255;
        private static final int PEDESTAL_Y = 5;
        private static final int PEDESTAL_W = 16;

        private boolean leftScaleHovered;
        private boolean rightScaleHovered;
        private boolean importHovered;
        private boolean exportHovered;
        private boolean addReplHovered;
        private boolean delReplHovered;
        private boolean pedestalHovered;

        private EditBox prefabNameBox;
        private int selectedNodeIndex;
        private final NodeButton[] nodeButtons = new NodeButton[VISIBLE_SCROLL_BUTTONS];
        private int scrollOff;
        // Shaking
        private int shakeTicks = 0;
        // Pattern of horizontal offsets
        private static final int[] SHAKE_OFFSETS = {-4, 4, -3, 3, -2, 2, -1, 1, 0, 0};
        // private final PieChartWidget pieChart = new PieChartWidget();

        @Override
        public CodexPageEnum getPageEnum() {
            return CodexPageEnum.NODE_VIEW_EDIT;
        }

        @Override
        public void init() {
            createNodeButtons();

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
            setFocused(prefabNameBox);
        }

        @Override
        public void removed() {
            for (NodeButton button : nodeButtons) {
                if (button != null) {
                    removeWidget(button);
                }
            }
            if (prefabNameBox != null) {
                removeWidget(prefabNameBox);
                prefabNameBox = null;
            }
            setFocused(null);
        }

        @Override
        public void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            RenderSystem.setShaderTexture(0, BACKGROUND);

            int x = (width - imageWidth) / 2;
            int y = (height - imageHeight) / 2;

            graphics.blit(BACKGROUND, x, y, 0, 0, imageWidth, imageHeight, TEXTURE_WIDTH, TEXTURE_HEIGHT);
            renderReplacementSlots(graphics, mouseX, mouseY);
            renderSelectedReplacement(graphics);
            renderScaleHover(graphics, leftScaleHovered, rightScaleHovered);
            renderSquareButtons(graphics);
            renderPedestal(graphics);
        }

        @Override
        public void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
            graphics.drawString(font, Component.translatable("gui.dungeon_diy.dungeon_codex.title_node_view_edit"), titleLabelX, titleLabelY, 4210752, false);
        }

        @Override
        public void renderTooltip(GuiGraphics graphics, int mouseX, int mouseY) {
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
            } else if (pedestalHovered) {
                tooltip.add(Component.translatable("gui.dungeon_diy.dungeon_codex.configure_pedestal"));
            }
            if (!tooltip.isEmpty()) {
                graphics.renderComponentTooltip(font, tooltip, mouseX, mouseY);
            }
        }

        @Override
        public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            updateNodeButtons();
            updateHoverBools(mouseX, mouseY);
            for (NodeButton button : nodeButtons) {
                if (button.isHoveredOrFocused()) {
                    button.renderToolTip(graphics, mouseX, mouseY);
                }
            }
            int i = (width - imageWidth) / 2;
            int j = (height - imageHeight) / 2;
            List<DungeonNode> nodes = menu.getNodes();
            renderScroller(graphics, i, j, nodes.size(), scrollOff);
            this.renderTooltip(graphics, mouseX, mouseY);
            DungeonNode node = nodes.get(menu.getSelectedNodeIndex());

            if (node != null) {
                // Future maybe, pie chart showing relative probability of each replacementEntry
                // pieChart.render(pGuiGraphics, leftPos + 145, topPos + 32, 20);
            }
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (button == 0) {
                // Scales
                if (leftScaleHovered) {
                    minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                    PacketDistributor.sendToServer(new ChangeReplacementEntryWeightPayload(menu.getDungeonId(), menu.getSelectedNode().getPos(), menu.getSelectedReplacementIndex(), false));
                    return true;
                }
                if (rightScaleHovered) {
                    minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                    PacketDistributor.sendToServer(new ChangeReplacementEntryWeightPayload(menu.getDungeonId(), menu.getSelectedNode().getPos(), menu.getSelectedReplacementIndex(), true));
                    return true;
                }
                // Replacement Entry Slots
                int index = getReplacementSlot(mouseX, mouseY);
                if (index >= 0) {
                    DungeonNode node = menu.getSelectedNode();
                    if (index < node.getReplacements().size()) {
                        minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                        menu.setSelectedReplacementIndex(index);
                        return true;
                    }
                }
                // Import/Export Add/Delete
                if (exportHovered) {
                    minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                    String prefabName = prefabNameBox.getValue();
                    if (!prefabName.isEmpty()) {
                        PacketDistributor.sendToServer(new ExportReplacementPrefabPayload(menu.getDungeonId(), menu.getSelectedNode().getPos(), prefabName));
                    } else {
                        shakeTicks = SHAKE_OFFSETS.length;
                    }
                    return true;
                }
                if (importHovered) {
                    minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                    PacketDistributor.sendToServer(new ChangeDungeonCodexPagePayload(CodexPageEnum.REPL_PREFAB_IMPORT));
                    return true;
                }
                if (addReplHovered) {
                    minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                    PacketDistributor.sendToServer(new ChangeDungeonCodexPagePayload(CodexPageEnum.ADD_REPL_ENTRY));
                    return true;
                }
                if (delReplHovered) {
                    minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                    PacketDistributor.sendToServer(new RemoveReplacementEntryPayload(menu.getDungeonId(), menu.getSelectedNode().getPos(), menu.getSelectedReplacementIndex()));
                    menu.setSelectedReplacementIndex(index);
                    return true;
                }
                // Pedestal
                if (pedestalHovered) {
                    minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                    PacketDistributor.sendToServer(new ChangeDungeonCodexPagePayload(CodexPageEnum.PEDESTAL_EDIT));
                    return true;
                }
            }
            return false;
        }

        @Override
        public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
            int max = Math.max(0, menu.getNodeCount() - VISIBLE_SCROLL_BUTTONS);
            scrollOff = Mth.clamp(scrollOff - (int)Math.signum(scrollY), 0, max);
            // Lose focus on scroll, not ideal, but prevent focus from staying on NodeButton after the containing node was scrolled to a diff index
            if ((getFocused() instanceof NodeButton)) {
                setFocused(null);
            }
            updateNodeButtons();
            return true;
        }

        @Override
        public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
            return false;
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
            return false;
        }

        @Override
        public boolean charTyped(char c, int modifiers) {
            return prefabNameBox != null && prefabNameBox.isFocused() && prefabNameBox.charTyped(c, modifiers);
        }

        @Override
        public void tick() {
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

        private void renderSquareButtons(GuiGraphics graphics) {
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

        private void renderPedestal(GuiGraphics graphics) {
            ItemStack stack = new ItemStack(DDIYBlocks.ANCIENT_PEDESTAL.get());
            graphics.renderFakeItem(stack, leftPos + PEDESTAL_X, topPos + PEDESTAL_Y - 5);
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

        private void createNodeButtons() {
            int i = (width - imageWidth) / 2;
            int j = (height - imageHeight) / 2;
            int k = j + 16 + 2;

            for (int l = 0; l < VISIBLE_SCROLL_BUTTONS; l++) {
                nodeButtons[l] = addRenderableWidget(
                  new NodeButton(i + 5, k, l, button -> {
                      if (button instanceof NodeButton nodeButton) {
                          selectedNodeIndex = nodeButton.getIndex() + scrollOff;
                          postNodeButtonClick();
                      }
                  })
                );
                k += 20;
            }
        }

        private void updateNodeButtons() {
            List<DungeonNode> nodes = menu.getNodes();
            for (int i = 0; i < nodeButtons.length; i++) {
                int index = scrollOff + i;
                if (index < nodes.size()) {
                    nodeButtons[i].visible = true;
                    nodeButtons[i].active = true;
                    nodeButtons[i].setNode(nodes.get(index));
                    nodeButtons[i].setSelected(index == selectedNodeIndex);
                } else {
                    nodeButtons[i].visible = false;
                }
            }
        }

        public void postNodeButtonClick() {
            menu.setSelectedNodeIndex(selectedNodeIndex);
            // DungeonNode node = menu.getNodes().get(selectedNode);
            // pieChart.rebuild(node.copyReplacements());
            // Eventually send a packet if the server needs to know immediately.
        }

        private void updateHoverBools(int mouseX, int mouseY) {
            leftScaleHovered = mouseX >= leftPos + SCALE_LEFT_ARM_X && mouseX < leftPos + SCALE_LEFT_ARM_X + SCALE_ARM_WIDTH && mouseY >= topPos + SCALE_ARM_Y && mouseY < topPos + SCALE_ARM_Y + SCALE_ARM_HEIGHT;
            rightScaleHovered = mouseX >= leftPos + SCALE_RIGHT_ARM_X && mouseX < leftPos + SCALE_RIGHT_ARM_X + SCALE_ARM_WIDTH && mouseY >= topPos + SCALE_ARM_Y && mouseY < topPos + SCALE_ARM_Y + SCALE_ARM_HEIGHT;
            importHovered = mouseX >= leftPos + IMPORT_REPL_PREFAB_X && mouseX < leftPos + IMPORT_REPL_PREFAB_X + SQUARE_BUTTON_LENGTH && mouseY >= topPos + IMPORT_REPL_PREFAB_Y && mouseY < topPos + IMPORT_REPL_PREFAB_Y + SQUARE_BUTTON_LENGTH;
            exportHovered = mouseX >= leftPos + EXPORT_REPL_PREFAB_X && mouseX < leftPos + EXPORT_REPL_PREFAB_X + SQUARE_BUTTON_LENGTH && mouseY >= topPos + EXPORT_REPL_PREFAB_Y && mouseY < topPos + EXPORT_REPL_PREFAB_Y + SQUARE_BUTTON_LENGTH;
            addReplHovered = mouseX >= leftPos + ADD_REPL_X && mouseX < leftPos + ADD_REPL_X + SQUARE_BUTTON_LENGTH && mouseY >= topPos + ADD_REPL_Y && mouseY < topPos + ADD_REPL_Y + SQUARE_BUTTON_LENGTH;
            delReplHovered = mouseX >= leftPos + REMOVE_REPL_X && mouseX < leftPos + REMOVE_REPL_X + SQUARE_BUTTON_LENGTH && mouseY >= topPos + REMOVE_REPL_Y && mouseY < topPos + REMOVE_REPL_Y + SQUARE_BUTTON_LENGTH;
            pedestalHovered = mouseX >= leftPos + PEDESTAL_X && mouseX < leftPos + PEDESTAL_X + PEDESTAL_W && mouseY >= topPos + PEDESTAL_Y && mouseY < topPos + PEDESTAL_Y + PEDESTAL_W;
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
                graphics.drawCenteredString(font, pos, leftPos + CENTERED_SCROLL_BUTTON_X, getY() + 6, 0xFFFFFF);
                if (selected) {
                    graphics.fill(getX(), getY(), getX() + width, getY() + height, 0x40FFFFFF);
                }
            }
        }
    }

    private class ImportReplPage implements CodexPage {
        private static final ResourceLocation BACKGROUND = ResourceLocation.fromNamespaceAndPath(DungeonDIY.MOD_ID, "textures/gui/dungeon_codex/temp_codex_menu_import_repl_prefab.png");

        private static final int TEXTURE_WIDTH = 512;
        private static final int TEXTURE_HEIGHT = 256;

        private static final int CENTERED_SCROLL_BUTTON_X = 54;

        private static final int REPLACEMENT_SLOT_SIZE = 18;
        private static final int REPLACEMENT_COLUMNS = 9;
        private static final int REPLACEMENT_START_X = 107;
        private static final int REPLACEMENT_START_Y = 71;

        private static final int SQUARE_BUTTON_LENGTH = 14;
        private static final int IMPORT_REPL_PREFAB_X = 107;
        private static final int IMPORT_REPL_PREFAB_Y = 145;
        private static final int BACK_BTN_X = 126;
        private static final int BACK_BTN_Y = 145;

        private boolean importHovered;
        private boolean backHovered;

        private int selectedReplPrefabIndex;
        private final ReplPrefabButton[] prefabButtons = new ReplPrefabButton[VISIBLE_SCROLL_BUTTONS];
        private int scrollOff;

        @Override
        public CodexPageEnum getPageEnum() {
            return CodexPageEnum.REPL_PREFAB_IMPORT;
        }

        @Override
        public void init() {
            createPrefabButtons();
        }

        @Override
        public void removed() {
            for (ReplPrefabButton button : prefabButtons) {
                if (button != null) {
                    removeWidget(button);
                }
            }
            setFocused(null);
        }

        @Override
        public void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            RenderSystem.setShaderTexture(0, BACKGROUND);

            int x = (width - imageWidth) / 2;
            int y = (height - imageHeight) / 2;

            graphics.blit(BACKGROUND, x, y, 0, 0, imageWidth, imageHeight, TEXTURE_WIDTH, TEXTURE_HEIGHT);
            renderReplacementSlots(graphics, mouseX, mouseY);
            renderSquareButtons(graphics);
        }

        @Override
        public void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
            graphics.drawString(font, Component.translatable("gui.dungeon_diy.dungeon_codex.title_import_repl"), titleLabelX, titleLabelY, 4210752, false);
        }

        @Override
        public void renderTooltip(GuiGraphics graphics, int mouseX, int mouseY) {
            List<Component> tooltip = new ArrayList<>();
            if (importHovered) {
                tooltip.add(Component.translatable("gui.dungeon_diy.dungeon_codex.import_repl_prefab"));
            } else if (backHovered) {
                tooltip.add(Component.translatable("gui.dungeon_diy.dungeon_codex.back"));
            }
            if (!tooltip.isEmpty()) {
                graphics.renderComponentTooltip(font, tooltip, mouseX, mouseY);
            }
        }

        @Override
        public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            updatePrefabButtons();
            updateHoverBools(mouseX, mouseY);
            for (ReplPrefabButton button : prefabButtons) {
                if (button.isHoveredOrFocused()) {
                    button.renderToolTip(graphics, mouseX, mouseY);
                }
            }
            int i = (width - imageWidth) / 2;
            int j = (height - imageHeight) / 2;
            renderScroller(graphics, i, j, menu.getReplacementPrefabs().size(), scrollOff);
            this.renderTooltip(graphics, mouseX, mouseY);
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (button == 0) {
                // Replacement Entry Slots
                int index = getReplacementSlot(mouseX, mouseY);
                if (index >= 0) {
                    ReplacementPrefab prefab = menu.getSelectedReplacementPrefab();
                    if (index < prefab.entries.size()) {
                        minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                        menu.setSelectedReplacementIndex(index);
                        return true;
                    }
                }
                // Square Buttons
                if (importHovered) {
                    minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                    PacketDistributor.sendToServer(new ImportReplacementPrefabPayload(menu.getDungeonId(), menu.getSelectedNode().getPos(), menu.getSelectedReplacementPrefabIndex()));
                    return true;
                }
                if (backHovered) {
                    minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                    setPage(nodeViewEditPage);
                }
            }
            return false;
        }

        @Override
        public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
            int max = Math.max(0, menu.getReplacementPrefabs().size() - VISIBLE_SCROLL_BUTTONS);
            if (max == 0) { return false; }

            scrollOff = Mth.clamp(scrollOff - (int) Math.signum(scrollY), 0, max);
            // Lose focus on scroll, not ideal, but prevent focus from staying on NodeButton after the containing node was scrolled to a diff index
            if ((getFocused() instanceof ReplPrefabButton)) {
                setFocused(null);
            }
            updatePrefabButtons();
            return true;
        }

        @Override
        public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
            return false;
        }

        @Override
        public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
            return false;
        }

        @Override
        public boolean charTyped(char c, int modifiers) {
            return false;
        }

        @Override
        public void tick() {

        }

        private void renderSquareButtons(GuiGraphics graphics) {
            int u_offset = 0;
            int v_offset = 0;

            if (importHovered) { v_offset = SQUARE_BUTTON_LENGTH; }
            graphics.blit(IMPORT_EXPORT, leftPos + IMPORT_REPL_PREFAB_X, topPos + IMPORT_REPL_PREFAB_Y, u_offset, v_offset, SQUARE_BUTTON_LENGTH, SQUARE_BUTTON_LENGTH, 28, 28);

            v_offset = 0;
            if (backHovered) { v_offset = SQUARE_BUTTON_LENGTH; }
            graphics.blit(BACK, leftPos + BACK_BTN_X, topPos + BACK_BTN_Y, u_offset, v_offset, SQUARE_BUTTON_LENGTH, SQUARE_BUTTON_LENGTH, 14, 28);
        }

        private void renderReplacementSlots(GuiGraphics graphics, int mouseX, int mouseY) {
            ReplacementPrefab prefab = menu.getReplacementPrefab(menu.getSelectedReplacementPrefabIndex());
            if (prefab == null) {
                return;
            }
            List<ReplacementEntry> replacements = prefab.entries;
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

        private void createPrefabButtons() {
            int i = (width - imageWidth) / 2;
            int j = (height - imageHeight) / 2;
            int k = j + 16 + 2;

            for (int l = 0; l < VISIBLE_SCROLL_BUTTONS; l++) {
                prefabButtons[l] = addRenderableWidget(
                  new ReplPrefabButton(i + 5, k, l, button -> {
                      if (button instanceof ReplPrefabButton prefabButton) {
                          selectedReplPrefabIndex = prefabButton.getIndex() + scrollOff;
                          postPrefabButtonClick();
                      }
                  })
                );
                k += 20;
            }
        }

        private void updatePrefabButtons() {
            List<ReplacementPrefab> prefabs = menu.getReplacementPrefabs();
            for (int i = 0; i < prefabButtons.length; i++) {
                int index = scrollOff + i;
                if (index < prefabs.size()) {
                    prefabButtons[i].visible = true;
                    prefabButtons[i].active = true;
                    prefabButtons[i].setPrefab(prefabs.get(index));
                    prefabButtons[i].setSelected(index == selectedReplPrefabIndex);
                } else {
                    prefabButtons[i].visible = false;
                }
            }
        }

        public void postPrefabButtonClick() {
            menu.setSelectedReplacementPrefabIndex(selectedReplPrefabIndex);
        }

        private void updateHoverBools(int mouseX, int mouseY) {
            importHovered = mouseX >= leftPos + IMPORT_REPL_PREFAB_X && mouseX < leftPos + IMPORT_REPL_PREFAB_X + SQUARE_BUTTON_LENGTH && mouseY >= topPos + IMPORT_REPL_PREFAB_Y && mouseY < topPos + IMPORT_REPL_PREFAB_Y + SQUARE_BUTTON_LENGTH;
            backHovered = mouseX >= leftPos + BACK_BTN_X && mouseX < leftPos + BACK_BTN_X + SQUARE_BUTTON_LENGTH && mouseY >= topPos + BACK_BTN_Y && mouseY < topPos + BACK_BTN_Y + SQUARE_BUTTON_LENGTH;
        }

        @OnlyIn(Dist.CLIENT)
        class ReplPrefabButton extends Button {
            final int index;
            private ReplacementPrefab prefab;
            private boolean selected;

            public ReplPrefabButton(int x, int y, int index, Button.OnPress onPress) {
                super(x, y, 88, 20, CommonComponents.EMPTY, onPress, DEFAULT_NARRATION);
                this.index = index;
                this.visible = false;
            }

            public int getIndex() {
                return this.index;
            }

            public void setPrefab(ReplacementPrefab prefab) {
                this.prefab = prefab;
            }

            public void setSelected(boolean selected) {
                this.selected = selected;
            }

            public void renderToolTip(GuiGraphics graphics, int mouseX, int mouseY) {
                if (!this.isHovered || this.prefab == null) {
                    return;
                }
                List<Component> tooltip = new ArrayList<>();
                tooltip.add(Component.literal(prefab.entries.size() + " replacement(s)"));
                graphics.renderComponentTooltip(font, tooltip, mouseX, mouseY);
            }

            @Override
            protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
                super.renderWidget(graphics, mouseX, mouseY, partialTick);
                if (prefab == null) return;
                graphics.renderFakeItem(prefab.heavyWeightReplacementStack(), getX() + 3, getY() + 2);
                graphics.drawCenteredString(font, font.plainSubstrByWidth(prefab.name, 60), leftPos + CENTERED_SCROLL_BUTTON_X, getY() + 6, 0xFFFFFF);
                if (selected) {
                    graphics.fill(getX(), getY(), getX() + width, getY() + height, 0x40FFFFFF);
                }
            }
        }
    }

    private class AddReplEntryPage implements CodexPage {
        private static final ResourceLocation BACKGROUND = ResourceLocation.fromNamespaceAndPath(DungeonDIY.MOD_ID, "textures/gui/dungeon_codex/temp_codex_menu_add_replacement_entry.png");

        private static final int TEXTURE_WIDTH = 512;
        private static final int TEXTURE_HEIGHT = 256;

        private static final int CENTERED_SCROLL_BUTTON_X = 49;

        private static final int SELECTED_SLOT_X = 179;
        private static final int SELECTED_SLOT_Y = 25;

        private static final int REPLACEMENT_SLOT_SIZE = 18;
        private static final int REPLACEMENT_COLUMNS = 9;
        private static final int REPLACEMENT_START_X = 107;
        private static final int REPLACEMENT_START_Y = 71;

        private static final int ADD_REPL_X = 107;
        private static final int ADD_REPL_Y = 127;
        private static final int BACK_BTN_X = 126;
        private static final int BACK_BTN_Y = 127;

        private static final int SQUARE_BUTTON_LENGTH = 14;

        private boolean addReplHovered;
        private boolean backHovered;

        private BlockState editingState;
        private final BSPropertyButton[] propertyButtons = new BSPropertyButton[VISIBLE_SCROLL_BUTTONS];
        private int scrollOff;

        @Override
        public CodexPageEnum getPageEnum() {
            return CodexPageEnum.ADD_REPL_ENTRY;
        }

        @Override
        public void init() {
            createBSPropertyButtons();
            updateBSPropertyButtons();
        }

        @Override
        public void removed() {
            for (BSPropertyButton button : propertyButtons) {
                if (button != null) {
                    removeWidget(button);
                }
            }
            setFocused(null);
        }

        @Override
        public void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            RenderSystem.setShaderTexture(0, BACKGROUND);

            int x = (width - imageWidth) / 2;
            int y = (height - imageHeight) / 2;

            graphics.blit(BACKGROUND, x, y, 0, 0, imageWidth, imageHeight, TEXTURE_WIDTH, TEXTURE_HEIGHT);
            renderVaultSlots(graphics, mouseX, mouseY);
            renderSelectedBlock(graphics);
            renderSquareButtons(graphics);
        }

        @Override
        public void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
            graphics.drawString(font, Component.translatable("gui.dungeon_diy.dungeon_codex.title_add_repl"), titleLabelX, titleLabelY, 4210752, false);
        }

        @Override
        public void renderTooltip(GuiGraphics graphics, int mouseX, int mouseY) {
            List<Component> tooltip = new ArrayList<>();
            if (addReplHovered) {
                tooltip.add(Component.translatable("gui.dungeon_diy.dungeon_codex.add_repl_entry"));
            } else if (backHovered) {
                tooltip.add(Component.translatable("gui.dungeon_diy.dungeon_codex.back"));
            }
            if (!tooltip.isEmpty()) {
                graphics.renderComponentTooltip(font, tooltip, mouseX, mouseY);
            }
        }

        @Override
        public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            updateHoverBools(mouseX, mouseY);
            for (BSPropertyButton button : propertyButtons) {
                if (button.isHoveredOrFocused()) {
                    button.renderToolTip(graphics, mouseX, mouseY);
                }
            }
            int i = (width - imageWidth) / 2;
            int j = (height - imageHeight) / 2;
            int items = 0;
            if (editingState != null) {
                items = editingState.getProperties().size();
            }
            renderScroller(graphics, i, j, items, scrollOff);
            this.renderTooltip(graphics, mouseX, mouseY);
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (button == 0) {
                // Vault Slots
                int index = getClickedSlot(mouseX, mouseY);
                if (index >= 0) {
                    List<ItemStack> vaultStacks = menu.getVaultStacks();
                    if (index < vaultStacks.size()) {
                        minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                        try {
                            // TODO: Validate in a better way for non-blocks.
                            Block block = Block.byItem(vaultStacks.get(index).getItem());
                            editingState = block.defaultBlockState();
                            scrollOff = 0;
                            removed();
                            createBSPropertyButtons();
                            updateBSPropertyButtons();
                        } catch (Exception e) {
                            DungeonDIY.LOGGER.info("Rejected {} as a possible Replacement Entry to add. ", vaultStacks.get(index).toString(), e);
                            return false;
                        }
                        return true;
                    }
                }
                // Add
                if (addReplHovered) {
                    minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                    PacketDistributor.sendToServer(new AddReplacementEntryPayload(menu.getDungeonId(), menu.getSelectedNode().getPos(), new ReplacementEntry(editingState, 1)));
                    return true;
                }
                // Go back
                if (backHovered) {
                    minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                    setPage(nodeViewEditPage);
                    return true;
                }
            }
            return false;
        }

        @Override
        public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
            int max = 0;
            if (editingState != null) {
                max = Math.max(0, editingState.getProperties().size() - VISIBLE_SCROLL_BUTTONS);
            }
            scrollOff = Mth.clamp(scrollOff - (int)Math.signum(scrollY), 0, max);
            // Lose focus on scroll, not ideal, but prevent focus from staying on NodeButton after the containing node was scrolled to a diff index
            if ((getFocused() instanceof BSPropertyButton)) {
                setFocused(null);
            }
            updateBSPropertyButtons();
            return true;
        }

        @Override
        public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
            return false;
        }

        @Override
        public boolean keyPressed(int keyCode, int scanCode, int modifiers) { return false; }

        @Override
        public boolean charTyped(char c, int modifiers) { return false; }

        @Override
        public void tick() {}


        private void renderSquareButtons(GuiGraphics graphics) {
            int u_offset = 0;
            int v_offset = 0;
            if (addReplHovered) { v_offset = SQUARE_BUTTON_LENGTH; }
            graphics.blit(PLUS_MINUS, leftPos + ADD_REPL_X, topPos + ADD_REPL_Y, u_offset, v_offset, SQUARE_BUTTON_LENGTH, SQUARE_BUTTON_LENGTH, 28, 28);

            v_offset = 0;
            if (backHovered) { v_offset = SQUARE_BUTTON_LENGTH; }
            graphics.blit(BACK, leftPos + BACK_BTN_X, topPos + BACK_BTN_Y, u_offset, v_offset, SQUARE_BUTTON_LENGTH, SQUARE_BUTTON_LENGTH, 14, 28);
        }

        // TODO: This eventually needs to be scrollable or display a larger vault inventory.
        private void renderVaultSlots(GuiGraphics graphics, int mouseX, int mouseY) {
            List<ItemStack> vaultStacks = menu.getVaultStacks();
            if (vaultStacks == null) { return; }
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

                if (i < vaultStacks.size()) {
                    graphics.renderFakeItem(vaultStacks.get(i), x + 1, y + 1);
                }
            }
        }

        private void renderSelectedBlock(GuiGraphics graphics) {
            if (editingState == null) { return; }
            ItemStack stack = new ItemStack(editingState.getBlock().asItem());
            graphics.renderFakeItem(stack, leftPos + SELECTED_SLOT_X + 1, topPos + SELECTED_SLOT_Y + 1);
        }

        private int getClickedSlot(double mouseX, double mouseY) {
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

        private void createBSPropertyButtons() {
            int i = (width - imageWidth) / 2;
            int j = (height - imageHeight) / 2;
            int k = j + 16 + 2;

            for (int l = 0; l < VISIBLE_SCROLL_BUTTONS; l++) {
                propertyButtons[l] = addRenderableWidget(
                  new BSPropertyButton(i + 5, k, l, button -> {
                      if (button instanceof BSPropertyButton propertyButton) {
                          propertyButton.toggleState();
                      }
                  })
                );
                k += 20;
            }
        }

        private void updateBSPropertyButtons() {
            if (editingState == null) { return; }
            List<Property<?>> properties = new ArrayList<>(editingState.getProperties());
            for (int i = 0; i < propertyButtons.length; i++) {
                int index = scrollOff + i;
                if (index < properties.size()) {
                    propertyButtons[i].visible = true;
                    propertyButtons[i].active = true;
                    propertyButtons[i].setProperty(properties.get(index));
                } else {
                    propertyButtons[i].visible = false;
                }
            }
        }

        private void updateHoverBools(int mouseX, int mouseY) {
            addReplHovered = mouseX >= leftPos + ADD_REPL_X && mouseX < leftPos + ADD_REPL_X + SQUARE_BUTTON_LENGTH && mouseY >= topPos + ADD_REPL_Y && mouseY < topPos + ADD_REPL_Y + SQUARE_BUTTON_LENGTH;
            backHovered = mouseX >= leftPos + BACK_BTN_X && mouseX < leftPos + BACK_BTN_X + SQUARE_BUTTON_LENGTH && mouseY >= topPos + BACK_BTN_Y && mouseY < topPos + BACK_BTN_Y + SQUARE_BUTTON_LENGTH;
        }

        private <T extends Comparable<T>> void setPropertyValue(Property<T> property, Comparable<?> value) {
            editingState = editingState.setValue(property, property.getValueClass().cast(value));
        }
        @OnlyIn(Dist.CLIENT)
        class BSPropertyButton extends Button {
            final int index;
            private Property<?> property;
            private Comparable<?> currentValue;
            private Comparable<?> nextValue;

            public BSPropertyButton(int x, int y, int index, Button.OnPress onPress) {
                super(x, y, 88, 20, CommonComponents.EMPTY, onPress, DEFAULT_NARRATION);
                this.index = index;
                this.visible = false;
            }

            public int getIndex() {
                return this.index;
            }

            public void setProperty(Property<?> property) {
                this.property = property;
                updateValues();
            }

            private void updateValues() {
                currentValue = editingState.getValue(property);
                List<? extends Comparable<?>> values = property.getPossibleValues().stream().toList();
                int currentIndex = values.indexOf(currentValue);
                int nextIndex = (currentIndex + 1) % values.size();
                nextValue = values.get(nextIndex);
            }

            public void toggleState() {
                Comparable<?> currentValue = editingState.getValue(property);
                List<? extends Comparable<?>> values = property.getPossibleValues().stream().toList();
                int index = values.indexOf(currentValue);
                index = (index + 1) % values.size();
                setPropertyValue(property, values.get(index));
                updateValues();
            }

            public void renderToolTip(GuiGraphics graphics, int mouseX, int mouseY) {
                if (!this.isHovered || this.property == null) {
                    return;
                }
                List<Component> tooltip = new ArrayList<>();
                tooltip.add(Component.literal("Toggle next value: " + nextValue.toString()));
                graphics.renderComponentTooltip(font, tooltip, mouseX, mouseY);
            }

            @Override
            protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
                super.renderWidget(graphics, mouseX, mouseY, partialTick);
                if (property == null) return;

                String text = font.plainSubstrByWidth(property.getName() + ": " + currentValue.toString(), 84);
                graphics.drawCenteredString(font, text, leftPos + CENTERED_SCROLL_BUTTON_X, getY() + 6, 0xFFFFFF);
            }
        }
    }

    private class PedestalEditPage implements CodexPage {
        private static final ResourceLocation BACKGROUND = ResourceLocation.fromNamespaceAndPath(DungeonDIY.MOD_ID, "textures/gui/dungeon_codex/temp_codex_menu_pedestal_edit.png");

        private static final int BACK_BTN_X = 153;
        private static final int BACK_BTN_Y = 9;
        private static final int CHECK_BTN_X = 135;
        private static final int CHECK_BTN_Y = 9;
        private static final int SQUARE_BUTTON_LENGTH = 14;

        private boolean backHovered;
        private boolean checkHovered;

        @Override
        public CodexPageEnum getPageEnum() {
            return CodexPageEnum.PEDESTAL_EDIT;
        }

        @Override
        public void init() {
        }

        @Override
        public void removed() {

        }

        @Override
        public void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            RenderSystem.setShaderTexture(0, BACKGROUND);

            int x = (width - imageWidth) / 2;
            int y = (height - imageHeight) / 2;
            graphics.blit(BACKGROUND, x, y, 0, 0, imageWidth, imageHeight);
            renderSquareButtons(graphics);
        }

        @Override
        public void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
            graphics.drawString(font, Component.translatable("gui.dungeon_diy.dungeon_codex.title_pedestal_edit"), titleLabelX, titleLabelY, 4210752, false);
        }

        @Override
        public void renderTooltip(GuiGraphics graphics, int mouseX, int mouseY) {
            List<Component> tooltip = new ArrayList<>();
            if (backHovered) {
                tooltip.add(Component.translatable("gui.dungeon_diy.dungeon_codex.back"));
            } else if (checkHovered) {
                tooltip.add(Component.translatable("gui.dungeon_diy.dungeon_codex.save_go_back"));
            }
            if (!tooltip.isEmpty()) {
                graphics.renderComponentTooltip(font, tooltip, mouseX, mouseY);
            }
        }

        @Override
        public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            updateHoverBools(mouseX, mouseY);
            this.renderTooltip(graphics, mouseX, mouseY);
        }

        @Override
        public boolean mouseClicked(double mouseX, double mouseY, int button) {
            if (button == 0) {
                // Go back / Save
                if (backHovered) {
                    minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                    setPage(nodeViewEditPage);
                    return true;
                }
                if (checkHovered) {
                    minecraft.getSoundManager().play(SimpleSoundInstance.forUI(SoundEvents.UI_BUTTON_CLICK, 1.0F));
                    PacketDistributor.sendToServer(new SavePedestalSettingsPayload(menu.getDungeonId(), menu.getAcceptedPedestalItems()));
                    return true;
                }
            }
            return false;
        }

        @Override
        public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
            return false;
        }

        @Override
        public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
            return false;
        }

        @Override
        public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
            return false;
        }

        @Override
        public boolean charTyped(char c, int modifiers) {
            return false;
        }

        @Override
        public void tick() {
        }

        private void renderSquareButtons(GuiGraphics graphics) {
            int u_offset = 0;
            int v_offset = 0;

            if (backHovered) { v_offset = SQUARE_BUTTON_LENGTH; }
            graphics.blit(BACK, leftPos + BACK_BTN_X, topPos + BACK_BTN_Y, u_offset, v_offset, SQUARE_BUTTON_LENGTH, SQUARE_BUTTON_LENGTH, 14, 28);

            v_offset = 0;
            if (checkHovered) { v_offset = SQUARE_BUTTON_LENGTH; }
            graphics.blit(CHECK, leftPos + CHECK_BTN_X, topPos + CHECK_BTN_Y, u_offset, v_offset, SQUARE_BUTTON_LENGTH, SQUARE_BUTTON_LENGTH, 14, 28);
        }

        private void updateHoverBools(int mouseX, int mouseY) {
            backHovered = mouseX >= leftPos + BACK_BTN_X && mouseX < leftPos + BACK_BTN_X + SQUARE_BUTTON_LENGTH && mouseY >= topPos + BACK_BTN_Y && mouseY < topPos + BACK_BTN_Y + SQUARE_BUTTON_LENGTH;
            checkHovered = mouseX >= leftPos + CHECK_BTN_X && mouseX < leftPos + CHECK_BTN_X + SQUARE_BUTTON_LENGTH && mouseY >= topPos + CHECK_BTN_Y && mouseY < topPos + CHECK_BTN_Y + SQUARE_BUTTON_LENGTH;
        }
    }
}
