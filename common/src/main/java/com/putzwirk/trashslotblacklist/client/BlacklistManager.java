package com.putzwirk.trashslotblacklist.client;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonNull;
import com.google.gson.JsonObject;
import com.putzwirk.trashslotblacklist.Constants;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
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
import java.util.List;
import java.util.Map;

public final class BlacklistManager {

    private static final String FILE_NAME = "TrashslotBlacklist.json";
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private static final Map<Integer, List<BlacklistEntry>> profiles = new HashMap<>();
    private static int activeProfile = 1;
    private static boolean enabled = true;
    private static String currentWorldIdentifier = null;

    static {
        profiles.put(1, new ArrayList<>());
        profiles.put(2, new ArrayList<>());
        profiles.put(3, new ArrayList<>());
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

    private static List<BlacklistEntry> currentList() {
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
            Path legacy = legacyConfigPath();
            if (Files.exists(legacy)) {
                loadLegacy(legacy);
                save();
            }
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

            Constants.LOG.info("Loaded blacklist (enabled: {}, activeProfile: {})", enabled, activeProfile);
        } catch (IOException e) {
            Constants.LOG.error("Failed to load blacklist", e);
        }
    }

    private static void loadLegacy(Path legacy) {
        try (Reader reader = Files.newBufferedReader(legacy)) {
            JsonObject root = GSON.fromJson(reader, JsonObject.class);
            if (root == null) {
                return;
            }
            enabled = !root.has("enabled") || root.get("enabled").getAsBoolean();
            if (root.has("items")) {
                readItemList(root.getAsJsonArray("items"), profiles.get(1));
            }
            Constants.LOG.info("Migrated legacy blacklist file into per-world profile 1");
        } catch (IOException e) {
            Constants.LOG.error("Failed to load legacy blacklist", e);
        }
    }

    private static void readItemList(JsonArray array, List<BlacklistEntry> target) {
        for (JsonElement element : array) {
            if (!element.isJsonObject()) {
                continue;
            }
            JsonObject entry = element.getAsJsonObject();
            if (!entry.has("item")) {
                continue;
            }
            String itemId = entry.get("item").getAsString();
            ResourceLocation id = ResourceLocation.tryParse(itemId);
            if (id == null || !BuiltInRegistries.ITEM.containsKey(id)) {
                Constants.LOG.warn("Skipping invalid item: {}", itemId);
                continue;
            }
            Item item = BuiltInRegistries.ITEM.get(id);
            CompoundTag nbt = null;
            if (entry.has("nbt") && !entry.get("nbt").isJsonNull()) {
                String nbtString = entry.get("nbt").getAsString();
                if (!nbtString.isEmpty()) {
                    try {
                        nbt = TagParser.parseTag(nbtString);
                    } catch (Exception e) {
                        nbt = null;
                    }
                }
            }
            target.add(new BlacklistEntry(item, nbt));
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
            for (BlacklistEntry entry : profiles.get(p)) {
                JsonObject obj = new JsonObject();
                obj.addProperty("item", BuiltInRegistries.ITEM.getKey(entry.item).toString());
                if (entry.nbt != null && !entry.nbt.isEmpty()) {
                    obj.addProperty("nbt", entry.nbt.toString());
                } else {
                    obj.add("nbt", JsonNull.INSTANCE);
                }
                items.add(obj);
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

    private static Path legacyConfigPath() {
        return Minecraft.getInstance().gameDirectory.toPath().resolve(FILE_NAME);
    }

    public static void addToBlacklist(ItemStack stack) {
        if (stack.isEmpty()) {
            return;
        }
        checkAndReloadForCurrentWorld();
        Item item = stack.getItem();
        CompoundTag nbt = stack.hasTag() ? stack.getTag().copy() : null;
        for (BlacklistEntry entry : currentList()) {
            if (entry.matches(item, nbt)) {
                return;
            }
        }
        currentList().add(new BlacklistEntry(item, nbt));
        save();
    }

    public static void removeFromBlacklist(ItemStack stack) {
        if (stack.isEmpty()) {
            return;
        }
        checkAndReloadForCurrentWorld();
        Item item = stack.getItem();
        CompoundTag nbt = stack.getTag();
        if (currentList().removeIf(entry -> entry.matches(item, nbt))) {
            save();
        }
    }

    public static boolean isBlacklisted(ItemStack stack) {
        checkAndReloadForCurrentWorld();
        if (!enabled || stack.isEmpty()) {
            return false;
        }
        Item item = stack.getItem();
        CompoundTag nbt = stack.getTag();
        for (BlacklistEntry entry : currentList()) {
            if (entry.matches(item, nbt)) {
                return true;
            }
        }
        return false;
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
        for (BlacklistEntry entry : currentList()) {
            ItemStack stack = new ItemStack(entry.item);
            if (entry.nbt != null) {
                stack.setTag(entry.nbt.copy());
            }
            stacks.add(stack);
        }
        return stacks;
    }

    static final class BlacklistEntry {
        final Item item;
        final CompoundTag nbt;

        BlacklistEntry(Item item, CompoundTag nbt) {
            this.item = item;
            this.nbt = nbt;
        }

        boolean matches(Item otherItem, CompoundTag otherNbt) {
            if (this.item != otherItem) {
                return false;
            }
            boolean thisEmpty = this.nbt == null || this.nbt.isEmpty();
            boolean otherEmpty = otherNbt == null || otherNbt.isEmpty();
            if (thisEmpty || otherEmpty) {
                return thisEmpty && otherEmpty;
            }
            return this.nbt.equals(otherNbt);
        }
    }
}
