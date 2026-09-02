package com.putzwirk.trashslotblacklist;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class BlacklistManager {

    private static final String FILE_NAME = "TrashslotBlacklist.json";
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private static final Map<Integer, Set<Item>> profiles = new HashMap<>();
    private static int activeProfile = 1;
    private static boolean enabled = true;
    private static String currentWorldIdentifier = null;

    static {
        profiles.put(1, new LinkedHashSet<>());
        profiles.put(2, new LinkedHashSet<>());
        profiles.put(3, new LinkedHashSet<>());
    }

    private BlacklistManager() {
    }

    public static int getActiveProfile() {
        return activeProfile;
    }

    public static void setActiveProfile(int profile) {
        if (profile >= 1 && profile <= 3) {
            activeProfile = profile;
            save();
        }
    }

    private static Set<Item> currentList() {
        return profiles.get(activeProfile);
    }

    public static void checkAndReloadForCurrentWorld() {
        String activeWorld = getActiveWorldIdentifier();
        if (!activeWorld.equals(currentWorldIdentifier)) {
            currentWorldIdentifier = activeWorld;
            load();
        }
    }

    public static void load() {
        Path path = configPath();
        profiles.get(1).clear();
        profiles.get(2).clear();
        profiles.get(3).clear();
        enabled = true;
        activeProfile = 1;

        if (!Files.exists(path)) {
            return;
        }

        try (Reader reader = Files.newBufferedReader(path)) {
            JsonObject root = GSON.fromJson(reader, JsonObject.class);
            if (root == null) {
                return;
            }

            enabled = !root.has("enabled") || root.get("enabled").getAsBoolean();
            if (root.has("activeProfile")) {
                activeProfile = Math.max(1, Math.min(3, root.get("activeProfile").getAsInt()));
            }

            if (root.has("profiles")) {
                JsonObject profsObj = root.getAsJsonObject("profiles");
                for (int p = 1; p <= 3; p++) {
                    String pKey = String.valueOf(p);
                    if (profsObj.has(pKey)) {
                        readItemList(profsObj.getAsJsonArray(pKey), profiles.get(p));
                    }
                }
            } else if (root.has("items")) {
                readItemList(root.getAsJsonArray("items"), profiles.get(1));
            }
        } catch (IOException e) {
            Constants.LOG.error("Failed to load blacklist", e);
        }
    }

    private static void readItemList(JsonArray array, Set<Item> targetSet) {
        for (JsonElement element : array) {
            JsonObject entry = element.isJsonObject() ? element.getAsJsonObject() : null;
            String itemId = entry != null ? entry.get("item").getAsString() : element.getAsString();
            ResourceLocation id = ResourceLocation.tryParse(itemId);
            if (id != null && BuiltInRegistries.ITEM.containsKey(id)) {
                targetSet.add(BuiltInRegistries.ITEM.get(id));
            }
        }
    }

    public static void save() {
        Path path = configPath();
        try {
            if (path.getParent() != null) {
                Files.createDirectories(path.getParent());
            }
        } catch (IOException ignored) {
        }

        JsonObject root = new JsonObject();
        root.addProperty("enabled", enabled);
        root.addProperty("activeProfile", activeProfile);

        JsonObject profsObj = new JsonObject();
        for (int p = 1; p <= 3; p++) {
            JsonArray items = new JsonArray();
            for (Item item : profiles.get(p)) {
                items.add(BuiltInRegistries.ITEM.getKey(item).toString());
            }
            profsObj.add(String.valueOf(p), items);
        }
        root.add("profiles", profsObj);

        try (Writer writer = Files.newBufferedWriter(path)) {
            GSON.toJson(root, writer);
        } catch (IOException e) {
            Constants.LOG.error("Failed to save blacklist", e);
        }
    }

    private static String getActiveWorldIdentifier() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.isLocalServer() && mc.getSingleplayerServer() != null) {
            return "local_" + mc.getSingleplayerServer().getWorldData().getLevelName();
        }
        ServerData serverData = mc.getCurrentServer();
        if (serverData != null) {
            return "remote_" + serverData.ip;
        }
        return "global";
    }

    private static Path configPath() {
        String worldDir = getActiveWorldIdentifier().replaceAll("[^a-zA-Z0-9._-]", "_");
        return Minecraft.getInstance().gameDirectory.toPath()
                .resolve("saves_blacklist")
                .resolve(worldDir)
                .resolve(FILE_NAME);
    }

    public static void addToBlacklist(ItemStack stack) {
        if (stack.isEmpty()) {
            return;
        }
        checkAndReloadForCurrentWorld();
        if (currentList().add(stack.getItem())) {
            save();
        }
    }

    public static void removeFromBlacklist(ItemStack stack) {
        if (stack.isEmpty()) {
            return;
        }
        checkAndReloadForCurrentWorld();
        if (currentList().remove(stack.getItem())) {
            save();
        }
    }

    public static boolean isBlacklisted(ItemStack stack) {
        checkAndReloadForCurrentWorld();
        return enabled && !stack.isEmpty() && currentList().contains(stack.getItem());
    }

    public static boolean isBlacklistEnabled() {
        checkAndReloadForCurrentWorld();
        return enabled;
    }

    public static void setBlacklistEnabled(boolean value) {
        checkAndReloadForCurrentWorld();
        enabled = value;
        save();
    }

    public static List<ItemStack> getBlacklistedItems() {
        checkAndReloadForCurrentWorld();
        List<ItemStack> stacks = new ArrayList<>(currentList().size());
        for (Item item : currentList()) {
            stacks.add(new ItemStack(item));
        }
        return stacks;
    }
}
