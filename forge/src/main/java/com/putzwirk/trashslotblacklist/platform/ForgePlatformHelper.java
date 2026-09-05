package com.putzwirk.trashslotblacklist.platform;

import com.mojang.blaze3d.platform.InputConstants;
import com.putzwirk.trashslotblacklist.client.KeyBindings;
import com.putzwirk.trashslotblacklist.platform.services.IPlatformHelper;
import net.blay09.mods.trashslot.client.TrashSlotGuiHandler;
import net.blay09.mods.trashslot.client.deletion.DeletionProvider;
import net.blay09.mods.trashslot.config.TrashSlotConfig;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.FMLLoader;

public class ForgePlatformHelper implements IPlatformHelper {

    @Override
    public String getPlatformName() {
        return "Forge";
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
        if (provider == null) {
            return;
        }
        provider.deleteMouseItem(player, carried, TrashSlotGuiHandler.getTrashSlot(), false);
    }

    @Override
    public void trashDeleteContainerSlot(AbstractContainerMenu menu, int slotIndex, boolean deleteAll) {
        DeletionProvider provider = TrashSlotConfig.getDeletionProvider();
        if (provider == null) {
            return;
        }
        provider.deleteContainerItem(menu, slotIndex, deleteAll, TrashSlotGuiHandler.getTrashSlot());
    }
}
