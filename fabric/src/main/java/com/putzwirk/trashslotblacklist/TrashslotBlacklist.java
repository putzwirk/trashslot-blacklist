package com.putzwirk.trashslotblacklist;

import net.blay09.mods.balm.api.Balm;
import net.blay09.mods.balm.api.EmptyLoadContext;
import net.fabricmc.api.ClientModInitializer;

public class TrashslotBlacklist implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        Balm.initializeMod(Constants.MOD_ID, EmptyLoadContext.INSTANCE, () -> {
            KeyBindings.initialize();
            BlacklistManager.load();
            Constants.LOG.info("TrashslotBlacklist initialized with {} blacklisted items",
                    BlacklistManager.getBlacklistedItems().size());
        });
    }
}
