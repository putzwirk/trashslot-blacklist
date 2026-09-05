package com.putzwirk.trashslotblacklist.gui;

public final class ColorLerp {

    private ColorLerp() {
    }

    public static int channel(int from, int to, float progress, int shift) {
        float clamped = Math.max(0f, Math.min(1f, progress));
        int a = (from >> shift) & 0xFF;
        int b = (to >> shift) & 0xFF;
        return Math.round(a + (b - a) * clamped);
    }

    public static int opaque(int from, int to, float progress) {
        int r = channel(from, to, progress, 16);
        int g = channel(from, to, progress, 8);
        int b = channel(from, to, progress, 0);
        return 0xFF000000 | (r << 16) | (g << 8) | b;
    }
}
