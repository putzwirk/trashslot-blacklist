package com.putzwirk.trashslotblacklist;

import net.fabricmc.api.ClientModInitializer;

public class TrashslotBlacklist implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        KeyBindings.initialize();
        BlacklistManager.load();
        Constants.LOG.info("TrashslotBlacklist initialized with {} blacklisted items",
                BlacklistManager.getBlacklistedItems().size());
    }
}
