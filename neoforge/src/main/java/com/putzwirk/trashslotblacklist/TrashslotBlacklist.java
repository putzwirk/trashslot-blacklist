package com.putzwirk.trashslotblacklist;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

@Mod(TrashslotBlacklist.MODID)
public class TrashslotBlacklist {

    public static final String MODID = Constants.MOD_ID;

    public TrashslotBlacklist(IEventBus modEventBus) {
        Constants.LOG.info("TrashslotBlacklist loaded");
    }
}
