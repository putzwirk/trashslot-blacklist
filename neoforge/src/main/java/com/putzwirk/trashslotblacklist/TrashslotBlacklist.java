package com.putzwirk.trashslotblacklist;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;

@Mod(value = TrashslotBlacklist.MODID, dist = Dist.CLIENT)
public class TrashslotBlacklist {

    public static final String MODID = Constants.MOD_ID;

    public TrashslotBlacklist(IEventBus modEventBus) {
        if (FMLEnvironment.dist != Dist.CLIENT) {
            throw new IllegalStateException("TrashslotBlacklist is client-only");
        }
        Constants.LOG.info("TrashslotBlacklist loaded");
    }
}
