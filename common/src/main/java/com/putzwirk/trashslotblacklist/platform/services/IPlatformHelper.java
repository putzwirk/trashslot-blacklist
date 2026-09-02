package com.putzwirk.trashslotblacklist.platform.services;

public interface IPlatformHelper {

    String getPlatformName();

    boolean isModLoaded(String modId);

    boolean isDevelopmentEnvironment();

    default String getEnvironmentName() {
        return isDevelopmentEnvironment() ? "development" : "production";
    }

    boolean isBlacklistKeyActiveAndMatches(int keyCode, int scanCode);
}
