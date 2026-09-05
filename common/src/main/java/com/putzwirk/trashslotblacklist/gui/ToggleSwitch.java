package com.putzwirk.trashslotblacklist.gui;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;

public class ToggleSwitch extends AbstractWidget {

    public static final int WIDTH = 18;
    private static final int HEIGHT = 7;
    private static final int KNOB_SIZE = 5;
    private static final float ANIMATION_SPEED = 0.4f;
    private static final int TRACK_OFF = 0xFF3D3D3D;
    private static final int TRACK_OFF_HOVER = 0xFF4D4D4D;
    private static final int TRACK_ON = 0xFF2D5A2D;
    private static final int TRACK_ON_HOVER = 0xFF3D6A3D;
    private static final int KNOB_COLOR = 0xFFE0E0E0;

    public interface OnToggle {
        void onToggle(boolean enabled);
    }

    private final OnToggle onToggle;
    private boolean enabled;
    private float animationProgress;

    public ToggleSwitch(int x, int y, boolean initialState, OnToggle onToggle) {
        super(x, y, WIDTH, HEIGHT, Component.empty());
        this.enabled = initialState;
        this.onToggle = onToggle;
        this.animationProgress = initialState ? 1f : 0f;
    }

    public void setEnabled(boolean value) {
        this.enabled = value;
    }

    @Override
    protected void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        if (!visible) {
            return;
        }

        float target = enabled ? 1f : 0f;
        if (animationProgress < target) {
            animationProgress = Math.min(animationProgress + partialTick * ANIMATION_SPEED, target);
        } else if (animationProgress > target) {
            animationProgress = Math.max(animationProgress - partialTick * ANIMATION_SPEED, target);
        }

        boolean hovered = isMouseOver(mouseX, mouseY);
        int trackColor = interpolateTrackColor(animationProgress, hovered);

        int x = getX();
        int y = getY();
        graphics.fill(x, y, x + WIDTH, y + HEIGHT, trackColor);

        int travel = WIDTH - KNOB_SIZE - 2;
        int knobX = x + 1 + Math.round(travel * animationProgress);
        int knobY = y + 1;
        graphics.fill(knobX, knobY, knobX + KNOB_SIZE, knobY + KNOB_SIZE, KNOB_COLOR);
    }

    private int interpolateTrackColor(float progress, boolean hovered) {
        int off = hovered ? TRACK_OFF_HOVER : TRACK_OFF;
        int on = hovered ? TRACK_ON_HOVER : TRACK_ON;
        if (progress <= 0f) {
            return off;
        }
        if (progress >= 1f) {
            return on;
        }
        return ColorLerp.opaque(off, on, progress);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean allowHandlingWhenUnhandled) {
        if (active && visible && event.input() == 0 && isMouseOver(event.x(), event.y())) {
            enabled = !enabled;
            playDownSound(Minecraft.getInstance().getSoundManager());
            onToggle.onToggle(enabled);
            return true;
        }
        return false;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {
        output.add(NarratedElementType.TITLE, Component.translatable(
                enabled ? "trashslotblacklist.narration.enabled" : "trashslotblacklist.narration.disabled"));
    }
}
