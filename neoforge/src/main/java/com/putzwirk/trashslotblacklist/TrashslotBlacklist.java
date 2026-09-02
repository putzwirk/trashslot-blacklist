package com.putzwirk.trashslotblacklist;

import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import org.slf4j.Logger;

@Mod(TrashslotBlacklist.MODID)
public class TrashslotBlacklist {

    public static final String MODID = "trashslotblacklist";
    public static final Logger LOGGER = LogUtils.getLogger();

    public TrashslotBlacklist(IEventBus modEventBus) {
        LOGGER.info("Trashslot Blacklist loaded");
    }
}
