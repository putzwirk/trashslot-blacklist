package com.putzwirk.trashslotblacklist.gui;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ColorLerpTest {

    private static final int BLACK = 0xFF000000;
    private static final int WHITE = 0xFFFFFFFF;
    private static final int OFF = 0xFF3D3D3D;
    private static final int ON = 0xFF2D5A2D;

    @Test
    void channelAtZeroIsFrom() {
        assertEquals(0, ColorLerp.channel(BLACK, WHITE, 0f, 0));
        assertEquals(0x3D, ColorLerp.channel(OFF, ON, 0f, 0));
    }

    @Test
    void channelAtOneIsTo() {
        assertEquals(255, ColorLerp.channel(BLACK, WHITE, 1f, 0));
        assertEquals(0x2D, ColorLerp.channel(OFF, ON, 1f, 0));
    }

    @Test
    void channelAtHalfRoundsNearest() {
        assertEquals(128, ColorLerp.channel(BLACK, WHITE, 0.5f, 0));
    }

    @Test
    void channelQuarter() {
        assertEquals(64, ColorLerp.channel(BLACK, WHITE, 0.25f, 0));
    }

    @Test
    void channelProgressAboveOneClampsViaMath() {
        assertEquals(255, ColorLerp.channel(BLACK, WHITE, 2f, 0));
    }

    @Test
    void channelProgressBelowZeroClampsViaMath() {
        assertEquals(0, ColorLerp.channel(BLACK, WHITE, -1f, 0));
    }

    @Test
    void opaqueAtZeroIsOffColor() {
        assertEquals(0xFF3D3D3D, ColorLerp.opaque(OFF, ON, 0f));
    }

    @Test
    void opaqueAtOneIsOnColor() {
        assertEquals(0xFF2D5A2D, ColorLerp.opaque(OFF, ON, 1f));
    }

    @Test
    void opaqueMidpointBlendsAllChannels() {
        int blended = ColorLerp.opaque(OFF, ON, 0.5f);
        assertEquals(0xFF000000, blended & 0xFF000000);
        assertEquals(0x35, (blended >> 16) & 0xFF);
        assertEquals(0x4C, (blended >> 8) & 0xFF);
        assertEquals(0x35, blended & 0xFF);
    }

    @Test
    void opaqueAlwaysHasFullAlpha() {
        assertEquals(0xFF000000, ColorLerp.opaque(BLACK, WHITE, 0.3f) & 0xFF000000);
        assertTrue((ColorLerp.opaque(BLACK, WHITE, 0.7f) >>> 24) == 0xFF);
    }

    @Test
    void opaqueBetweenBlackAndWhiteMidpoint() {
        int blended = ColorLerp.opaque(BLACK, WHITE, 0.5f);
        assertEquals(0x80, (blended >> 16) & 0xFF);
        assertEquals(0x80, (blended >> 8) & 0xFF);
        assertEquals(0x80, blended & 0xFF);
    }
}
