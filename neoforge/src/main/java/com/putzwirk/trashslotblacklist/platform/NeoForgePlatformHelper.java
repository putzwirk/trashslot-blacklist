package com.putzwirk.trashslotblacklist.platform;

import com.mojang.blaze3d.platform.InputConstants;
import com.putzwirk.trashslotblacklist.KeyBindings;
import com.putzwirk.trashslotblacklist.platform.services.IPlatformHelper;
import net.blay09.mods.trashslot.TrashHelper;
import net.blay09.mods.trashslot.client.TrashSlotGuiHandler;
import net.blay09.mods.trashslot.client.deletion.DeletionProvider;
import net.blay09.mods.trashslot.config.TrashSlotConfig;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.neoforged.fml.ModList;
import net.neoforged.fml.loading.FMLLoader;

public class NeoForgePlatformHelper implements IPlatformHelper {

    @Override
    public String getPlatformName() {
        return "NeoForge";
    }

    @Override
    public boolean isModLoaded(String modId) {
        return ModList.get().isLoaded(modId);
    }

    @Override
    public boolean isDevelopmentEnvironment() {
        return !FMLLoader.isProduction();
    }

    @Override
    public boolean isBlacklistKeyActiveAndMatches(int keyCode, int scanCode) {
        return KeyBindings.TRASH_AND_BLACKLIST.isActiveAndMatches(InputConstants.getKey(keyCode, scanCode));
    }

    @Override
    public void trashDeleteCarried(Player player, ItemStack carried) {
        DeletionProvider provider = TrashSlotConfig.getDeletionProvider();
        if (provider == null || !TrashHelper.canDelete(carried)) {
            return;
        }
        provider.deleteMouseItem(player, carried, TrashSlotGuiHandler.getTrashSlot(), false);
    }

    @Override
    public void trashDeleteContainerSlot(AbstractContainerMenu menu, int slotIndex, boolean simulate) {
        DeletionProvider provider = TrashSlotConfig.getDeletionProvider();
        if (provider == null) {
            return;
        }
        ItemStack stack = menu.getSlot(slotIndex).getItem();
        if (!TrashHelper.canDelete(stack)) {
            return;
        }
        Player player = net.minecraft.client.Minecraft.getInstance().player;
        if (player == null) {
            return;
        }
        provider.deleteContainerItem(player, menu, slotIndex, simulate, TrashSlotGuiHandler.getTrashSlot());
    }
}
