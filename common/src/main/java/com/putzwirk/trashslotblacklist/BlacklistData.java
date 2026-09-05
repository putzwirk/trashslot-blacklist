package com.putzwirk.trashslotblacklist;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class BlacklistData {

    public static final int MIN_PROFILE = 1;
    public static final int MAX_PROFILE = 3;

    private static final String KEY_ENABLED = "enabled";
    private static final String KEY_ACTIVE_PROFILE = "activeProfile";
    private static final String KEY_PROFILES = "profiles";
    private static final String KEY_LEGACY_ITEMS = "items";
    private static final String KEY_ITEM = "item";
    private static final String KEY_MODE = "mode";
    private static final String KEY_ENCHANTMENTS = "enchantments";
    private static final String KEY_ID = "id";
    private static final String KEY_LEVEL = "level";
    private static final String MODE_ENCHANT = "enchant";

    private final Map<Integer, LinkedHashSet<BlacklistEntry>> profiles = new HashMap<>();
    private boolean enabled = true;
    private int activeProfile = MIN_PROFILE;

    public BlacklistData() {
        for (int profile = MIN_PROFILE; profile <= MAX_PROFILE; profile++) {
            profiles.put(profile, new LinkedHashSet<>());
        }
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean value) {
        this.enabled = value;
    }

    public int getActiveProfile() {
        return activeProfile;
    }

    public boolean setActiveProfile(int profile) {
        if (profile < MIN_PROFILE || profile > MAX_PROFILE) {
            return false;
        }
        if (activeProfile == profile) {
            return false;
        }
        activeProfile = profile;
        return true;
    }

    public boolean addEntry(BlacklistEntry entry) {
        if (entry == null || !isValidItemId(entry.itemId())) {
            return false;
        }
        return profiles.get(activeProfile).add(entry);
    }

    public boolean removeEntry(BlacklistEntry entry) {
        if (entry == null) {
            return false;
        }
        return profiles.get(activeProfile).remove(entry);
    }

    public boolean containsEntry(BlacklistEntry entry) {
        if (entry == null) {
            return false;
        }
        return profiles.get(activeProfile).contains(entry);
    }

    public Set<BlacklistEntry> entriesOf(int profile) {
        Set<BlacklistEntry> entries = profiles.get(profile);
        return entries != null ? Collections.unmodifiableSet(entries) : Collections.emptySet();
    }

    public void clear() {
        enabled = true;
        activeProfile = MIN_PROFILE;
        for (Set<BlacklistEntry> entries : profiles.values()) {
            entries.clear();
        }
    }

    public JsonObject toJson() {
        JsonObject root = new JsonObject();
        root.addProperty(KEY_ENABLED, enabled);
        root.addProperty(KEY_ACTIVE_PROFILE, activeProfile);

        JsonObject profilesObject = new JsonObject();
        for (int profile = MIN_PROFILE; profile <= MAX_PROFILE; profile++) {
            JsonArray entries = new JsonArray();
            for (BlacklistEntry entry : profiles.get(profile)) {
                entries.add(entryToJson(entry));
            }
            profilesObject.add(String.valueOf(profile), entries);
        }
        root.add(KEY_PROFILES, profilesObject);
        return root;
    }

    private static JsonElement entryToJson(BlacklistEntry entry) {
        if (entry.mode() == BlacklistEntry.Mode.ANY) {
            return new JsonPrimitive(entry.itemId());
        }
        JsonObject entryObject = new JsonObject();
        entryObject.addProperty(KEY_ITEM, entry.itemId());
        entryObject.addProperty(KEY_MODE, MODE_ENCHANT);
        JsonArray enchantments = new JsonArray();
        for (BlacklistEntry.Enchantment enchantment : entry.enchantments()) {
            JsonObject enchantmentObject = new JsonObject();
            enchantmentObject.addProperty(KEY_ID, enchantment.id());
            enchantmentObject.addProperty(KEY_LEVEL, enchantment.level());
            enchantments.add(enchantmentObject);
        }
        entryObject.add(KEY_ENCHANTMENTS, enchantments);
        return entryObject;
    }

    public void fromJson(JsonObject root) {
        clear();
        if (root == null) {
            return;
        }

        Boolean enabledValue = readBoolean(root, KEY_ENABLED);
        if (enabledValue != null) {
            enabled = enabledValue;
        }

        Integer profileValue = readInt(root, KEY_ACTIVE_PROFILE);
        if (profileValue != null && profileValue >= MIN_PROFILE && profileValue <= MAX_PROFILE) {
            activeProfile = profileValue;
        }

        boolean loadedProfiles = false;
        if (root.has(KEY_PROFILES) && root.get(KEY_PROFILES).isJsonObject()) {
            JsonObject profilesObject = root.getAsJsonObject(KEY_PROFILES);
            for (int profile = MIN_PROFILE; profile <= MAX_PROFILE; profile++) {
                String key = String.valueOf(profile);
                if (profilesObject.has(key) && profilesObject.get(key).isJsonArray()) {
                    readEntryList(profilesObject.getAsJsonArray(key), profiles.get(profile));
                    loadedProfiles = true;
                }
            }
        }

        if (!loadedProfiles && root.has(KEY_LEGACY_ITEMS) && root.get(KEY_LEGACY_ITEMS).isJsonArray()) {
            readEntryList(root.getAsJsonArray(KEY_LEGACY_ITEMS), profiles.get(MIN_PROFILE));
        }
    }

    private static void readEntryList(JsonArray array, Set<BlacklistEntry> target) {
        for (JsonElement element : array) {
            BlacklistEntry entry = readEntry(element);
            if (entry != null) {
                target.add(entry);
            }
        }
    }

    private static BlacklistEntry readEntry(JsonElement element) {
        if (element.isJsonPrimitive()) {
            JsonPrimitive primitive = element.getAsJsonPrimitive();
            if (!primitive.isString()) {
                return null;
            }
            String itemId = primitive.getAsString().trim();
            return itemId.isEmpty() ? null : BlacklistEntry.any(itemId);
        }

        if (!element.isJsonObject()) {
            return null;
        }
        JsonObject entryObject = element.getAsJsonObject();

        String itemId = readString(entryObject, KEY_ITEM);
        if (itemId == null) {
            return null;
        }

        String mode = readString(entryObject, KEY_MODE);
        if (!MODE_ENCHANT.equals(mode)) {
            return BlacklistEntry.any(itemId);
        }

        if (!entryObject.has(KEY_ENCHANTMENTS) || !entryObject.get(KEY_ENCHANTMENTS).isJsonArray()) {
            return BlacklistEntry.any(itemId);
        }

        List<BlacklistEntry.Enchantment> enchantments = new ArrayList<>();
        for (JsonElement enchantmentElement : entryObject.getAsJsonArray(KEY_ENCHANTMENTS)) {
            BlacklistEntry.Enchantment enchantment = readEnchantment(enchantmentElement);
            if (enchantment == null) {
                return null;
            }
            enchantments.add(enchantment);
        }
        if (enchantments.isEmpty()) {
            return BlacklistEntry.any(itemId);
        }
        return BlacklistEntry.enchantedBook(itemId, enchantments);
    }

    private static BlacklistEntry.Enchantment readEnchantment(JsonElement element) {
        if (!element.isJsonObject()) {
            return null;
        }
        JsonObject enchantmentObject = element.getAsJsonObject();
        String id = readString(enchantmentObject, KEY_ID);
        if (id == null) {
            return null;
        }
        Integer level = readInt(enchantmentObject, KEY_LEVEL);
        return new BlacklistEntry.Enchantment(id, level != null ? level : 1);
    }

    private static String readString(JsonObject root, String key) {
        if (!root.has(key) || !root.get(key).isJsonPrimitive()) {
            return null;
        }
        JsonPrimitive primitive = root.getAsJsonPrimitive(key);
        if (!primitive.isString()) {
            return null;
        }
        String value = primitive.getAsString().trim();
        return value.isEmpty() ? null : value;
    }

    private static Boolean readBoolean(JsonObject root, String key) {
        if (!root.has(key) || !root.get(key).isJsonPrimitive()) {
            return null;
        }
        JsonPrimitive primitive = root.getAsJsonPrimitive(key);
        return primitive.isBoolean() ? primitive.getAsBoolean() : null;
    }

    private static Integer readInt(JsonObject root, String key) {
        if (!root.has(key) || !root.get(key).isJsonPrimitive()) {
            return null;
        }
        JsonPrimitive primitive = root.getAsJsonPrimitive(key);
        return primitive.isNumber() ? primitive.getAsInt() : null;
    }

    private static boolean isValidItemId(String itemId) {
        return itemId != null && !itemId.isBlank();
    }

    public static String sanitizeWorldIdentifier(String identifier) {
        if (identifier == null) {
            return "";
        }
        return identifier.replaceAll("[^a-zA-Z0-9._-]", "_");
    }
}
