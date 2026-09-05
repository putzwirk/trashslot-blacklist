package com.putzwirk.trashslotblacklist;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ButtonStateManagerTest {

    @Test
    void defaultsToNotHovered() {
        ButtonStateManager.setHovered(false);
        assertFalse(ButtonStateManager.isHovered());
    }

    @Test
    void setHoveredTrueIsVisible() {
        ButtonStateManager.setHovered(true);
        assertTrue(ButtonStateManager.isHovered());
    }

    @Test
    void setHoveredFalseIsVisible() {
        ButtonStateManager.setHovered(true);
        ButtonStateManager.setHovered(false);
        assertFalse(ButtonStateManager.isHovered());
    }
}
