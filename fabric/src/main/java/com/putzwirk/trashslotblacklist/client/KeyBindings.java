package com.putzwirk.trashslotblacklist.client;

import com.putzwirk.trashslotblacklist.Constants;
import com.putzwirk.trashslotblacklist.mixin.ContainerScreenAccessor;
import net.blay09.mods.balm.api.Balm;
import net.blay09.mods.balm.api.event.client.screen.ScreenKeyEvent;
import net.blay09.mods.kuma.api.InputBinding;
import net.blay09.mods.kuma.api.KeyConflictContext;
import net.blay09.mods.kuma.api.Kuma;
import net.blay09.mods.kuma.api.ManagedKeyMapping;
import net.blay09.mods.trashslot.client.TrashSlotGuiHandler;
import net.blay09.mods.trashslot.client.deletion.DeletionProvider;
import net.blay09.mods.trashslot.config.TrashSlotConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.lwjgl.glfw.GLFW;

public class KeyBindings {
    public static ManagedKeyMapping TRASH_AND_BLACKLIST;

    public static void initialize() {
        TRASH_AND_BLACKLIST = Kuma.createKeyMapping(
                        new ResourceLocation("trashslotblacklist", "trash_and_blacklist"))
                .withDefault(InputBinding.key(GLFW.GLFW_KEY_GRAVE_ACCENT))
                .withContext(KeyConflictContext.SCREEN)
                .build();

        Balm.getEvents().onEvent(ScreenKeyEvent.Press.Post.class, KeyBindings::onKeyPress);
    }

    private static void onKeyPress(ScreenKeyEvent.Press.Post event) {
        if (TRASH_AND_BLACKLIST.isActiveAndMatchesKey(event.getKey(), event.getScanCode(), event.getModifiers())) {
            handleTrashAndBlacklist();
            event.setCanceled(true);
        }
    }

    private static void handleTrashAndBlacklist() {
        Minecraft client = Minecraft.getInstance();

        if (!(client.screen instanceof AbstractContainerScreen<?> screen)) {
            return;
        }

        Slot hoveredSlot = ((ContainerScreenAccessor) screen).getHoveredSlot();
        if (hoveredSlot == null || !hoveredSlot.hasItem()) {
            return;
        }

        ItemStack hoveredStack = hoveredSlot.getItem();
        DeletionProvider deletionProvider = TrashSlotConfig.getDeletionProvider();

        if (deletionProvider == null) {
            Constants.LOG.warn("TrashSlot deletion provider is null - cannot delete item");
            return;
        }

        BlacklistManager.addToBlacklist(hoveredStack.copy());

        assert client.player != null;
        ItemStack cursorStack = client.player.containerMenu.getCarried();

        if (!cursorStack.isEmpty()) {
            deletionProvider.deleteMouseItem(
                    client.player,
                    cursorStack,
                    TrashSlotGuiHandler.getTrashSlot(),
                    false
            );
        } else {
            deletionProvider.deleteContainerItem(
                    screen.getMenu(),
                    hoveredSlot.index,
                    true,
                    TrashSlotGuiHandler.getTrashSlot()
            );
        }
    }
}
