package com.putzwirk.trashslotblacklist;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;

public final class KeyBindings {

    public static final KeyMapping TRASH_AND_BLACKLIST = new KeyMapping(
            "key.trashslotblacklist.trash_and_blacklist",
            InputConstants.Type.KEYSYM,
            InputConstants.KEY_GRAVE,
            KeyMapping.Category.INVENTORY
    );

    private KeyBindings() {
    }
}