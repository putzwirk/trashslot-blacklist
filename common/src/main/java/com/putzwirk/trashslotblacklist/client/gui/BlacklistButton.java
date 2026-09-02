package com.putzwirk.trashslotblacklist.client.gui;

import com.putzwirk.trashslotblacklist.client.util.ButtonStateManager;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.network.chat.Component;

public class BlacklistButton extends AbstractWidget {

    private static final int SIZE = 9;

    private static final int BORDER_CLOSED = -6250336;
    private static final int BACKGROUND_CLOSED_HOVER = -2697514;
    private static final int BACKGROUND_CLOSED_PRESSED = -4802890;
    private static final int BORDER_OPEN = -5189377;
    private static final int BACKGROUND_OPEN_HOVER = -984833;
    private static final int BACKGROUND_OPEN_PRESSED = -2561793;
    private static final int LINE_HOVER = -12566464;
    private static final int LINE_PRESSED = -16777216;

    private final BlacklistPanel panel;
    private boolean pressed;

    public BlacklistButton(int x, int y, BlacklistPanel panel) {
        super(x, y, SIZE, SIZE, Component.empty());
        this.panel = panel;
    }

    @Override
    protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        if (!this.visible) {
            return;
        }

        boolean hovered = isMouseOver(mouseX, mouseY);
        ButtonStateManager.setButtonHovered(hovered);

        boolean open = panel.isExpanded();
        int borderColor = open ? BORDER_OPEN : BORDER_CLOSED;
        int backgroundColor;
        int lineColor;
        if (open) {
            backgroundColor = pressed ? BACKGROUND_OPEN_PRESSED : BACKGROUND_OPEN_HOVER;
        } else {
            backgroundColor = pressed ? BACKGROUND_CLOSED_PRESSED : BACKGROUND_CLOSED_HOVER;
        }
        lineColor = pressed ? LINE_PRESSED : LINE_HOVER;

        int x = getX();
        int y = getY();

        graphics.pose().pushPose();
        graphics.pose().translate(0.0F, 0.0F, 1000.0F);
        graphics.fill(x, y, x + SIZE, y + SIZE, borderColor);
        graphics.fill(x + 1, y + 1, x + SIZE - 1, y + SIZE - 1, backgroundColor);
        graphics.fill(x + 2, y + 2, x + SIZE - 2, y + 3, lineColor);
        graphics.fill(x + 2, y + 4, x + SIZE - 2, y + 5, lineColor);
        graphics.fill(x + 2, y + 6, x + SIZE - 2, y + 7, lineColor);
        graphics.pose().popPose();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.active && this.visible && button == 0 && isMouseOver(mouseX, mouseY)) {
            pressed = true;
            panel.toggleExpanded();
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        boolean wasPressed = pressed;
        pressed = false;
        return wasPressed && button == 0;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {
    }
}
