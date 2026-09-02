package com.putzwirk.trashslotblacklist;

import com.putzwirk.trashslotblacklist.client.BlacklistManager;
import com.putzwirk.trashslotblacklist.client.KeyBindings;
import net.blay09.mods.balm.api.Balm;
import net.fabricmc.api.ClientModInitializer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class TrashslotBlacklist implements ClientModInitializer {
    public static final String MOD_ID = "trashslotblacklist";
    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

    @Override
    public void onInitializeClient() {
        LOGGER.info("TrashslotBlacklist initializing...");
        Balm.initialize(MOD_ID, this::initializeMod);
    }

    private void initializeMod() {
        LOGGER.info("Balm initialized for TrashslotBlacklist");

        KeyBindings.initialize();
        BlacklistManager.load();

        LOGGER.info("TrashslotBlacklist initialization complete with {} blacklisted items",
                BlacklistManager.getBlacklistedItems().size());
    }
}