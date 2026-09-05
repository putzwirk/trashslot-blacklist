package com.putzwirk.trashslotblacklist;

import com.putzwirk.trashslotblacklist.client.KeyBindings;
import net.blay09.mods.balm.api.Balm;
import net.fabricmc.api.ClientModInitializer;

public class TrashslotBlacklist implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        Balm.initialize(Constants.MOD_ID, this::initializeMod);
    }

    private void initializeMod() {
        KeyBindings.initialize();
        BlacklistManager.load();
        Constants.LOG.info("TrashslotBlacklist initialized with {} blacklisted items",
                BlacklistManager.getBlacklistedItems().size());
    }
}
