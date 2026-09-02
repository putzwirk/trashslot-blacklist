package com.putzwirk.trashslotblacklist;

public final class ButtonStateManager {

    private static boolean hovered;

    private ButtonStateManager() {
    }

    public static void setHovered(boolean value) {
        hovered = value;
    }

    public static boolean isHovered() {
        return hovered;
    }
}
