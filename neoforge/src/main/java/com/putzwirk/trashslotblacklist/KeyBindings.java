package com.putzwirk.trashslotblacklist;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.neoforged.neoforge.client.settings.KeyConflictContext;

public final class KeyBindings {

    public static final KeyMapping TRASH_AND_BLACKLIST = new KeyMapping(
            "key.trashslotblacklist.trash_and_blacklist",
            KeyConflictContext.GUI,
            InputConstants.Type.KEYSYM,
            InputConstants.KEY_GRAVE,
            "key.categories.inventory"
    );

    private KeyBindings() {
    }
}
