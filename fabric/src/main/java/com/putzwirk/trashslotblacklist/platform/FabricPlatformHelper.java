package com.putzwirk.trashslotblacklist.platform;

import com.putzwirk.trashslotblacklist.KeyBindings;
import com.putzwirk.trashslotblacklist.platform.services.IPlatformHelper;
import net.blay09.mods.trashslot.client.TrashSlotGuiHandler;
import net.blay09.mods.trashslot.client.deletion.DeletionProvider;
import net.blay09.mods.trashslot.config.TrashSlotConfig;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;

public class FabricPlatformHelper implements IPlatformHelper {

    @Override
    public String getPlatformName() {
        return "Fabric";
    }

    @Override
    public boolean isModLoaded(String modId) {
        return FabricLoader.getInstance().isModLoaded(modId);
    }

    @Override
    public boolean isDevelopmentEnvironment() {
        return FabricLoader.getInstance().isDevelopmentEnvironment();
    }

    @Override
    public boolean isBlacklistKeyActiveAndMatches(int keyCode, int scanCode) {
        return KeyBindings.TRASH_AND_BLACKLIST != null
                && KeyBindings.TRASH_AND_BLACKLIST.isActiveAndMatchesKey(keyCode, scanCode, 0);
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
