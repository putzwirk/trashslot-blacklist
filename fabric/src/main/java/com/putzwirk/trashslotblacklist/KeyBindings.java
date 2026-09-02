package com.putzwirk.trashslotblacklist;

import net.blay09.mods.kuma.api.InputBinding;
import net.blay09.mods.kuma.api.KeyConflictContext;
import net.blay09.mods.kuma.api.Kuma;
import net.blay09.mods.kuma.api.ManagedKeyMapping;
import net.minecraft.resources.ResourceLocation;
import org.lwjgl.glfw.GLFW;

public final class KeyBindings {

    public static ManagedKeyMapping TRASH_AND_BLACKLIST;

    public static void initialize() {
        TRASH_AND_BLACKLIST = Kuma.createKeyMapping(
                        ResourceLocation.fromNamespaceAndPath("trashslotblacklist", "trash_and_blacklist"))
                .withDefault(InputBinding.key(GLFW.GLFW_KEY_GRAVE_ACCENT))
                .withContext(KeyConflictContext.SCREEN)
                .build();
    }

    private KeyBindings() {
    }
}
