package com.putzwirk.trashslotblacklist;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public enum BoxDebug {
    instance;

    private static final Logger LOGGER = LoggerFactory.getLogger("trashslotblacklist-debug");

    public void log(String message) {
        LOGGER.info(message);
    }
}