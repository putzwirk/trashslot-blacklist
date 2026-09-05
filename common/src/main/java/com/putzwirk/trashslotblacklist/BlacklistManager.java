package com.putzwirk.trashslotblacklist;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;

import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class BlacklistManager {

    private static final String FILE_NAME = "TrashslotBlacklist.json";
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    private static final BlacklistData data = new BlacklistData();
    private static final Map<BlacklistEntry, Item> itemCache = new HashMap<>();
    private static String currentWorldIdentifier = null;

    private BlacklistManager() {
    }

    public static int getActiveProfile() {
        return data.getActiveProfile();
    }

    public static void setActiveProfile(int profile) {
        checkAndReloadForCurrentWorld();
        if (data.setActiveProfile(profile)) {
            save();
        }
    }

    public static void checkAndReloadForCurrentWorld() {
        String activeWorld = getActiveWorldIdentifier();
        if (!activeWorld.equals(currentWorldIdentifier)) {
            currentWorldIdentifier = activeWorld;
            load();
        }
    }

    public static void load() {
        data.clear();
        itemCache.clear();
        Path path = configPath();
        if (!Files.exists(path)) {
            return;
        }

        try (Reader reader = Files.newBufferedReader(path)) {
            JsonObject root = GSON.fromJson(reader, JsonObject.class);
            data.fromJson(root);
        } catch (Exception e) {
            Constants.LOG.error("Failed to load blacklist", e);
        }
    }

    public static void save() {
        Path path = configPath();
        try {
            if (path.getParent() != null) {
                Files.createDirectories(path.getParent());
            }
        } catch (IOException e) {
            Constants.LOG.error("Failed to create blacklist directory", e);
        }

        try (Writer writer = Files.newBufferedWriter(path)) {
            GSON.toJson(data.toJson(), writer);
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
        String worldDir = BlacklistData.sanitizeWorldIdentifier(getActiveWorldIdentifier());
        return Minecraft.getInstance().gameDirectory.toPath()
                .resolve("saves_blacklist")
                .resolve(worldDir)
                .resolve(FILE_NAME);
    }

    public static void addToBlacklist(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return;
        }
        checkAndReloadForCurrentWorld();
        BlacklistEntry entry = entryFor(stack);
        if (entry != null && data.addEntry(entry)) {
            save();
        }
    }

    public static void removeEntry(BlacklistEntry entry) {
        checkAndReloadForCurrentWorld();
        if (entry != null && data.removeEntry(entry)) {
            save();
        }
    }

    public static boolean isBlacklisted(ItemStack stack) {
        checkAndReloadForCurrentWorld();
        return stack != null && !stack.isEmpty() && data.isEnabled() && findMatchingEntry(stack) != null;
    }

    public static boolean isBlacklistEnabled() {
        checkAndReloadForCurrentWorld();
        return data.isEnabled();
    }

    public static void setBlacklistEnabled(boolean value) {
        checkAndReloadForCurrentWorld();
        data.setEnabled(value);
        save();
    }

    public static List<BlacklistEntryView> getBlacklistedEntries() {
        checkAndReloadForCurrentWorld();
        List<BlacklistEntryView> views = new ArrayList<>();
        for (BlacklistEntry entry : data.entriesOf(data.getActiveProfile())) {
            ItemStack stack = displayStackOf(entry);
            if (stack != null) {
                views.add(new BlacklistEntryView(entry, stack));
            }
        }
        return views;
    }

    public static List<ItemStack> getBlacklistedItems() {
        return getBlacklistedEntries().stream().map(BlacklistEntryView::stack).toList();
    }

    private static BlacklistEntry entryFor(ItemStack stack) {
        String itemId = itemIdOf(stack.getItem());
        if (itemId == null) {
            return null;
        }
        if (stack.is(Items.ENCHANTED_BOOK)) {
            List<BlacklistEntry.Enchantment> enchantments = storedEnchantmentsOf(stack);
            if (!enchantments.isEmpty()) {
                return BlacklistEntry.enchantedBook(itemId, enchantments);
            }
        }
        return BlacklistEntry.any(itemId);
    }

    private static BlacklistEntry findMatchingEntry(ItemStack stack) {
        for (BlacklistEntry entry : data.entriesOf(data.getActiveProfile())) {
            if (matches(entry, stack)) {
                return entry;
            }
        }
        return null;
    }

    private static boolean matches(BlacklistEntry entry, ItemStack stack) {
        Item item = itemOf(entry);
        if (item == null || !stack.is(item)) {
            return false;
        }
        if (entry.mode() == BlacklistEntry.Mode.ANY) {
            return true;
        }
        return enchantmentIdsOf(stack).equals(entry.enchantmentIds());
    }

    private static Item itemOf(BlacklistEntry entry) {
        return itemCache.computeIfAbsent(entry, e -> {
            ResourceLocation id = ResourceLocation.tryParse(e.itemId());
            if (id == null || !BuiltInRegistries.ITEM.containsKey(id)) {
                return null;
            }
            return BuiltInRegistries.ITEM.get(id);
        });
    }

    private static ItemStack displayStackOf(BlacklistEntry entry) {
        Item item = itemOf(entry);
        if (item == null) {
            return null;
        }
        ItemStack stack = new ItemStack(item);
        if (entry.mode() == BlacklistEntry.Mode.ENCHANT && !entry.enchantments().isEmpty()) {
            for (BlacklistEntry.Enchantment enchantment : entry.enchantments()) {
                Enchantment resolved = enchantmentOf(enchantment.id());
                if (resolved != null) {
                    EnchantedBookItem.addEnchantment(stack, new EnchantmentInstance(resolved, enchantment.level()));
                }
            }
        }
        return stack;
    }

    private static List<BlacklistEntry.Enchantment> storedEnchantmentsOf(ItemStack stack) {
        if (stack.getTag() == null) {
            return List.of();
        }
        ListTag list = stack.getTag().getList(EnchantedBookItem.TAG_STORED_ENCHANTMENTS, Tag.TAG_COMPOUND);
        List<BlacklistEntry.Enchantment> result = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            CompoundTag entry = list.getCompound(i);
            String id = entry.getString("id");
            if (!id.isEmpty()) {
                int level = entry.contains("lvl") ? entry.getShort("lvl") : 1;
                result.add(new BlacklistEntry.Enchantment(id, level));
            }
        }
        return result;
    }

    public static Set<String> getEnchantmentIds(ItemStack stack) {
        return enchantmentIdsOf(stack);
    }

    private static Set<String> enchantmentIdsOf(ItemStack stack) {
        Set<String> ids = new HashSet<>();
        if (stack.getTag() == null) {
            return ids;
        }
        for (String key : new String[]{"Enchantments", EnchantedBookItem.TAG_STORED_ENCHANTMENTS}) {
            ListTag list = stack.getTag().getList(key, Tag.TAG_COMPOUND);
            for (int i = 0; i < list.size(); i++) {
                String id = list.getCompound(i).getString("id");
                if (!id.isEmpty()) {
                    ids.add(id);
                }
            }
        }
        return ids;
    }

    private static Enchantment enchantmentOf(String id) {
        ResourceLocation location = ResourceLocation.tryParse(id);
        return location == null ? null : BuiltInRegistries.ENCHANTMENT.get(location);
    }

    private static String itemIdOf(Item item) {
        ResourceLocation key = BuiltInRegistries.ITEM.getKey(item);
        return key != null ? key.toString() : null;
    }
}
