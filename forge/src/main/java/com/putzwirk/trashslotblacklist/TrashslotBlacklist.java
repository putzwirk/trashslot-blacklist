package com.putzwirk.trashslotblacklist;

import com.putzwirk.trashslotblacklist.client.BlacklistManager;
import com.putzwirk.trashslotblacklist.client.KeyBindings;
import net.blay09.mods.balm.api.Balm;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod("trashslotblacklist")
public class TrashslotBlacklist {

    public static final String MOD_ID = "trashslotblacklist";
    public static final Logger LOGGER = LogManager.getLogger(MOD_ID);

    public TrashslotBlacklist() {
        LOGGER.info("TrashslotBlacklist initializing...");
        if (FMLEnvironment.dist == Dist.CLIENT) {
            Balm.initialize(MOD_ID, this::initializeMod);
            FMLJavaModLoadingContext.get().getModEventBus().addListener(this::clientSetup);
        }
    }

    private void initializeMod() {
        KeyBindings.initialize();
    }

    private void clientSetup(FMLClientSetupEvent event) {
        BlacklistManager.load();
        LOGGER.info("TrashslotBlacklist initialization complete with {} blacklisted items", BlacklistManager.getBlacklistedItems().size());
    }
}
