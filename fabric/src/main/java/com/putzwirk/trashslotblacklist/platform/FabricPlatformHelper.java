package com.putzwirk.trashslotblacklist.platform;

import com.putzwirk.trashslotblacklist.KeyBindings;
import com.putzwirk.trashslotblacklist.platform.services.IPlatformHelper;
import net.blay09.mods.trashslot.TrashSlotConfig;
import net.blay09.mods.trashslot.client.TrashSlotGuiHandler;
import net.blay09.mods.trashslot.client.deletion.DeletionProvider;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.input.KeyEvent;
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
    public boolean isBlacklistKeyActiveAndMatches(KeyEvent event) {
        return KeyBindings.TRASH_AND_BLACKLIST != null
                && KeyBindings.TRASH_AND_BLACKLIST.isActiveAndMatchesKey(event.key(), event.scancode(), event.modifiers());
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
        Player player = net.minecraft.client.Minecraft.getInstance().player;
        if (player == null) {
            return;
        }
        provider.deleteContainerItem(player, menu, slotIndex, deleteAll, TrashSlotGuiHandler.getTrashSlot());
    }
}
