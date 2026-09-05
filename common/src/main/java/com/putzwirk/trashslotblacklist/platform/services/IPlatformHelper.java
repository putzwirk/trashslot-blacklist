package com.putzwirk.trashslotblacklist.platform.services;

import net.minecraft.client.input.KeyEvent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

public interface IPlatformHelper {

    String getPlatformName();

    boolean isModLoaded(String modId);

    boolean isDevelopmentEnvironment();

    default String getEnvironmentName() {
        return isDevelopmentEnvironment() ? "development" : "production";
    }

    boolean isBlacklistKeyActiveAndMatches(KeyEvent event);

    void trashDeleteCarried(Player player, ItemStack carried);

    void trashDeleteContainerSlot(AbstractContainerMenu menu, int slotIndex, boolean simulate);
}
