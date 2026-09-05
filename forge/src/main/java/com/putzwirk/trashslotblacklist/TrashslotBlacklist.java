package com.putzwirk.trashslotblacklist;

import com.putzwirk.trashslotblacklist.client.KeyBindings;
import net.blay09.mods.balm.api.Balm;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;

@Mod("trashslotblacklist")
public class TrashslotBlacklist {

    public TrashslotBlacklist() {
        if (FMLEnvironment.dist == Dist.CLIENT) {
            Balm.initialize(Constants.MOD_ID, this::initializeMod);
            FMLJavaModLoadingContext.get().getModEventBus().addListener(this::clientSetup);
        }
    }

    private void initializeMod() {
        KeyBindings.initialize();
    }

    private void clientSetup(FMLClientSetupEvent event) {
        BlacklistManager.load();
        Constants.LOG.info("TrashslotBlacklist initialized with {} blacklisted items",
                BlacklistManager.getBlacklistedItems().size());
    }
}
