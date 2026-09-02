package com.putzwirk.trashslotblacklist.platform;

import com.putzwirk.trashslotblacklist.client.KeyBindings;
import com.putzwirk.trashslotblacklist.platform.services.IPlatformHelper;
import net.fabricmc.loader.api.FabricLoader;

public class FabricPlatformHelper implements IPlatformHelper {

    @Override
    public String getPlatformName() {
        return "Fabric";
    }

    @Override
    public boolean isModLoaded(String modId) {
        return FabricLoader.getInstance().isModLoaded(modId);
    }

    @Override
    public boolean isDevelopmentEnvironment() {
        return FabricLoader.getInstance().isDevelopmentEnvironment();
    }

    @Override
    public boolean isBlacklistKeyActiveAndMatches(int keyCode, int scanCode) {
        return KeyBindings.TRASH_AND_BLACKLIST != null
                && KeyBindings.TRASH_AND_BLACKLIST.isActiveAndMatchesKey(keyCode, scanCode, 0);
    }
}
