package com.putzwirk.trashslotblacklist.client;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.settings.KeyConflictContext;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

public final class KeyBindings {

    public static final KeyMapping TRASH_AND_BLACKLIST = new KeyMapping(
            "key.trashslotblacklist.trash_and_blacklist",
            KeyConflictContext.GUI,
            InputConstants.Type.KEYSYM,
            InputConstants.KEY_GRAVE,
            "key.categories.inventory"
    );

    private static boolean registered = false;

    public static void initialize() {
        if (registered) {
            return;
        }
        registered = true;
        FMLJavaModLoadingContext.get().getModEventBus()
                .addListener(KeyBindings::onRegisterKeyMappings);
    }

    private static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(TRASH_AND_BLACKLIST);
    }

    private KeyBindings() {
    }
}
