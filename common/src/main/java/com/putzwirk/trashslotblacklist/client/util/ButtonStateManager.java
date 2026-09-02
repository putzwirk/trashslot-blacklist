package com.putzwirk.trashslotblacklist.client.util;

public final class ButtonStateManager {

    private static boolean buttonHovered;

    private ButtonStateManager() {
    }

    public static void setButtonHovered(boolean hovered) {
        buttonHovered = hovered;
    }

    public static boolean isButtonHovered() {
        return buttonHovered;
    }
}
