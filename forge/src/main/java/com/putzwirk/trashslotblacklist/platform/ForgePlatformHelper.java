package com.putzwirk.trashslotblacklist.platform;

import com.mojang.blaze3d.platform.InputConstants;
import com.putzwirk.trashslotblacklist.client.KeyBindings;
import com.putzwirk.trashslotblacklist.platform.services.IPlatformHelper;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.loading.FMLLoader;

public class ForgePlatformHelper implements IPlatformHelper {

    @Override
    public String getPlatformName() {
        return "Forge";
    }

    @Override
    public boolean isModLoaded(String modId) {
        return ModList.get().isLoaded(modId);
    }

    @Override
    public boolean isDevelopmentEnvironment() {
        return !FMLLoader.isProduction();
    }

    @Override
    public boolean isBlacklistKeyActiveAndMatches(int keyCode, int scanCode) {
        return KeyBindings.TRASH_AND_BLACKLIST.isActiveAndMatches(InputConstants.getKey(keyCode, scanCode));
    }
}
