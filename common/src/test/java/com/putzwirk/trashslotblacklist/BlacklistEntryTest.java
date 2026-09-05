package com.putzwirk.trashslotblacklist;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BlacklistEntryTest {

    @Test
    void anyEntryHasAnyModeAndNoEnchantments() {
        BlacklistEntry entry = BlacklistEntry.any("minecraft:golden_sword");
        assertEquals("minecraft:golden_sword", entry.itemId());
        assertEquals(BlacklistEntry.Mode.ANY, entry.mode());
        assertTrue(entry.enchantments().isEmpty());
        assertTrue(entry.enchantmentIds().isEmpty());
    }

    @Test
    void anyRejectsNullItemId() {
        assertThrows(NullPointerException.class, () -> BlacklistEntry.any(null));
    }

    @Test
    void enchantedBookEntryKeepsEnchantmentsAndLevels() {
        List<BlacklistEntry.Enchantment> enchantments = List.of(
                new BlacklistEntry.Enchantment("minecraft:soul_speed", 2),
                new BlacklistEntry.Enchantment("minecraft:mending", 1));
        BlacklistEntry entry = BlacklistEntry.enchantedBook("minecraft:enchanted_book", enchantments);

        assertEquals(BlacklistEntry.Mode.ENCHANT, entry.mode());
        assertEquals(2, entry.enchantments().size());
        assertEquals(1, entry.enchantments().get(0).level());
        assertEquals(Set.of("minecraft:soul_speed", "minecraft:mending"), entry.enchantmentIds());
    }

    @Test
    void enchantedBookWithNoEnchantmentsFallsBackToAny() {
        BlacklistEntry entry = BlacklistEntry.enchantedBook("minecraft:enchanted_book", List.of());
        assertEquals(BlacklistEntry.Mode.ANY, entry.mode());
        assertTrue(entry.enchantments().isEmpty());
    }

    @Test
    void enchantedBookWithNullListFallsBackToAny() {
        BlacklistEntry entry = BlacklistEntry.enchantedBook("minecraft:enchanted_book", null);
        assertEquals(BlacklistEntry.Mode.ANY, entry.mode());
    }

    @Test
    void enchantedBookRejectsNullItemId() {
        assertThrows(NullPointerException.class,
                () -> BlacklistEntry.enchantedBook(null, List.of(new BlacklistEntry.Enchantment("a", 1))));
    }

    @Test
    void enchantmentRejectsBlankId() {
        assertThrows(IllegalArgumentException.class, () -> new BlacklistEntry.Enchantment(" ", 1));
        assertThrows(IllegalArgumentException.class, () -> new BlacklistEntry.Enchantment(null, 1));
    }

    @Test
    void enchantmentLevelClampedToOneMinimum() {
        assertEquals(1, new BlacklistEntry.Enchantment("minecraft:mending", 0).level());
        assertEquals(1, new BlacklistEntry.Enchantment("minecraft:mending", -5).level());
        assertEquals(3, new BlacklistEntry.Enchantment("minecraft:soul_speed", 3).level());
    }

    @Test
    void enchantmentsAreSortedById() {
        BlacklistEntry entry = BlacklistEntry.enchantedBook("minecraft:enchanted_book", List.of(
                new BlacklistEntry.Enchantment("minecraft:z_b", 1),
                new BlacklistEntry.Enchantment("minecraft:a_a", 1)));
        assertEquals("minecraft:a_a", entry.enchantments().get(0).id());
        assertEquals("minecraft:z_b", entry.enchantments().get(1).id());
    }

    @Test
    void enchantmentOrderDoesNotAffectEquality() {
        BlacklistEntry first = BlacklistEntry.enchantedBook("minecraft:enchanted_book", List.of(
                new BlacklistEntry.Enchantment("minecraft:a", 1),
                new BlacklistEntry.Enchantment("minecraft:b", 2)));
        BlacklistEntry second = BlacklistEntry.enchantedBook("minecraft:enchanted_book", List.of(
                new BlacklistEntry.Enchantment("minecraft:b", 2),
                new BlacklistEntry.Enchantment("minecraft:a", 1)));
        assertEquals(first, second);
        assertEquals(first.hashCode(), second.hashCode());
    }

    @Test
    void differentLevelsAreNotEqual() {
        assertNotEquals(
                BlacklistEntry.enchantedBook("minecraft:enchanted_book",
                        List.of(new BlacklistEntry.Enchantment("minecraft:a", 1))),
                BlacklistEntry.enchantedBook("minecraft:enchanted_book",
                        List.of(new BlacklistEntry.Enchantment("minecraft:a", 2))));
    }

    @Test
    void differentModesAreNotEqual() {
        assertNotEquals(BlacklistEntry.any("minecraft:dirt"),
                BlacklistEntry.enchantedBook("minecraft:dirt",
                        List.of(new BlacklistEntry.Enchantment("minecraft:a", 1))));
    }

    @Test
    void differentItemsAreNotEqual() {
        assertNotEquals(BlacklistEntry.any("minecraft:dirt"), BlacklistEntry.any("minecraft:stone"));
    }

    @Test
    void anyEntriesWithSameItemAreEqual() {
        assertEquals(BlacklistEntry.any("minecraft:dirt"), BlacklistEntry.any("minecraft:dirt"));
        assertEquals(BlacklistEntry.any("minecraft:dirt").hashCode(),
                BlacklistEntry.any("minecraft:dirt").hashCode());
    }

    @Test
    void returnedCollectionsAreImmutable() {
        BlacklistEntry entry = BlacklistEntry.enchantedBook("minecraft:enchanted_book",
                List.of(new BlacklistEntry.Enchantment("minecraft:a", 1)));
        assertThrows(UnsupportedOperationException.class,
                () -> entry.enchantments().add(new BlacklistEntry.Enchantment("minecraft:b", 1)));
        assertThrows(UnsupportedOperationException.class, () -> entry.enchantmentIds().add("minecraft:c"));
    }

    @Test
    void deduplicatesInASetRegardlessOfOrder() {
        Set<BlacklistEntry> entries = new java.util.HashSet<>();
        entries.add(BlacklistEntry.enchantedBook("minecraft:enchanted_book", List.of(
                new BlacklistEntry.Enchantment("minecraft:a", 1),
                new BlacklistEntry.Enchantment("minecraft:b", 1))));
        entries.add(BlacklistEntry.enchantedBook("minecraft:enchanted_book", List.of(
                new BlacklistEntry.Enchantment("minecraft:b", 1),
                new BlacklistEntry.Enchantment("minecraft:a", 1))));
        assertEquals(1, entries.size());
    }

    @Test
    void toStringShowsItemAndEnchantmentsForEnchantMode() {
        BlacklistEntry entry = BlacklistEntry.enchantedBook("minecraft:enchanted_book",
                List.of(new BlacklistEntry.Enchantment("minecraft:a", 1)));
        assertTrue(entry.toString().contains("minecraft:a"));
    }
}
