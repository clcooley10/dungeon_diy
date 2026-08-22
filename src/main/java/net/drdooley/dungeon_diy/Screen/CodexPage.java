package net.drdooley.dungeon_diy.Screen;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

public interface CodexPage {

    CodexPageEnum getPageEnum();

    void init();

    void removed();

    void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY);

    void renderLabels(GuiGraphics graphics, int mouseX, int mouseY);

    void renderTooltip(GuiGraphics graphics, int mouseX, int mouseY);

    void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick);

    boolean mouseClicked(double mouseX, double mouseY, int button);

    boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY);

    boolean keyPressed(int keyCode, int scanCode, int modifiers);

    boolean charTyped(char c, int modifiers);

    void tick();
}