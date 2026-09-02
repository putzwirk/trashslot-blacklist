package com.putzwirk.trashslotblacklist;

import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;

@EventBusSubscriber(modid = TrashslotBlacklist.MODID, value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD)
public class TrashslotBlacklistClient {

    @SubscribeEvent
    static void onClientSetup(FMLClientSetupEvent event) {
        BlacklistManager.load();
    }

    @SubscribeEvent
    static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(KeyBindings.TRASH_AND_BLACKLIST);
    }
}
