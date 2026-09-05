package com.putzwirk.trashslotblacklist;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;

public final class BlacklistEntry {

    public enum Mode {
        ANY,
        ENCHANT
    }

    public record Enchantment(String id, int level) {

        public Enchantment {
            if (id == null || id.isBlank()) {
                throw new IllegalArgumentException("Enchantment id must not be blank");
            }
            level = Math.max(1, level);
        }
    }

    private final String itemId;
    private final Mode mode;
    private final List<Enchantment> enchantments;
    private final Set<String> enchantmentIds;

    private BlacklistEntry(String itemId, Mode mode, List<Enchantment> enchantments) {
        this.itemId = itemId;
        this.mode = mode;
        List<Enchantment> sorted = new ArrayList<>(enchantments);
        sorted.sort(Comparator.comparing(Enchantment::id));
        this.enchantments = List.copyOf(sorted);
        Set<String> ids = new HashSet<>();
        for (Enchantment enchantment : this.enchantments) {
            ids.add(enchantment.id());
        }
        this.enchantmentIds = Set.copyOf(ids);
    }

    public static BlacklistEntry any(String itemId) {
        return new BlacklistEntry(Objects.requireNonNull(itemId), Mode.ANY, List.of());
    }

    public static BlacklistEntry enchantedBook(String itemId, List<Enchantment> enchantments) {
        Objects.requireNonNull(itemId);
        if (enchantments == null || enchantments.isEmpty()) {
            return any(itemId);
        }
        return new BlacklistEntry(itemId, Mode.ENCHANT, enchantments);
    }

    public String itemId() {
        return itemId;
    }

    public Mode mode() {
        return mode;
    }

    public List<Enchantment> enchantments() {
        return enchantments;
    }

    public Set<String> enchantmentIds() {
        return enchantmentIds;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof BlacklistEntry other)) {
            return false;
        }
        return itemId.equals(other.itemId) && mode == other.mode && enchantments.equals(other.enchantments);
    }

    @Override
    public int hashCode() {
        return Objects.hash(itemId, mode, enchantments);
    }

    @Override
    public String toString() {
        if (mode == Mode.ANY) {
            return itemId;
        }
        return itemId + enchantments;
    }
}
