package com.putzwirk.trashslotblacklist;

import net.blay09.mods.balm.api.Balm;
import net.blay09.mods.balm.api.EmptyLoadContext;
import net.fabricmc.api.ClientModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TrashslotBlacklist implements ClientModInitializer {

    public static final String MOD_ID = "trashslotblacklist";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitializeClient() {
        Balm.initializeMod(MOD_ID, EmptyLoadContext.INSTANCE, () -> {
            KeyBindings.initialize();
            BlacklistManager.load();
            LOGGER.info("TrashslotBlacklist initialized with {} blacklisted items",
                    BlacklistManager.getBlacklistedItems().size());
        });
    }
}
