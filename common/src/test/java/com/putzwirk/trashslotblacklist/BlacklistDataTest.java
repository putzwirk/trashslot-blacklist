package com.putzwirk.trashslotblacklist;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BlacklistDataTest {

    private BlacklistData data;

    @BeforeEach
    void setUp() {
        data = new BlacklistData();
    }

    private static JsonObject parse(String json) {
        return JsonParser.parseString(json).getAsJsonObject();
    }

    private static BlacklistEntry any(String itemId) {
        return BlacklistEntry.any(itemId);
    }

    private static BlacklistEntry book(String enchantmentId, int level) {
        return BlacklistEntry.enchantedBook("minecraft:enchanted_book",
                List.of(new BlacklistEntry.Enchantment(enchantmentId, level)));
    }

    private boolean addItem(String itemId) {
        return data.addEntry(any(itemId));
    }

    private boolean containsItem(String itemId) {
        return data.containsEntry(any(itemId));
    }

    private boolean removeItem(String itemId) {
        return data.removeEntry(any(itemId));
    }

    @Nested
    @DisplayName("defaults")
    class Defaults {

        @Test
        void defaultsAreEnabledProfileOneAndEmpty() {
            assertTrue(data.isEnabled());
            assertEquals(1, data.getActiveProfile());
            assertTrue(data.entriesOf(1).isEmpty());
            assertTrue(data.entriesOf(2).isEmpty());
            assertTrue(data.entriesOf(3).isEmpty());
        }
    }

    @Nested
    @DisplayName("enabled")
    class Enabled {

        @Test
        void setEnabledChangesValue() {
            data.setEnabled(false);
            assertFalse(data.isEnabled());
            data.setEnabled(true);
            assertTrue(data.isEnabled());
        }
    }

    @Nested
    @DisplayName("setActiveProfile")
    class ActiveProfile {

        @Test
        void switchesWithinValidRange() {
            assertTrue(data.setActiveProfile(2));
            assertEquals(2, data.getActiveProfile());
            assertTrue(data.setActiveProfile(3));
            assertEquals(3, data.getActiveProfile());
            assertTrue(data.setActiveProfile(1));
            assertEquals(1, data.getActiveProfile());
        }

        @Test
        void switchingToSameProfileReturnsFalse() {
            assertFalse(data.setActiveProfile(1));
            assertEquals(1, data.getActiveProfile());
        }

        @Test
        void rejectsZero() {
            assertFalse(data.setActiveProfile(0));
            assertEquals(1, data.getActiveProfile());
        }

        @Test
        void rejectsAboveMax() {
            assertFalse(data.setActiveProfile(4));
            assertEquals(1, data.getActiveProfile());
        }

        @Test
        void rejectsNegative() {
            assertFalse(data.setActiveProfile(-1));
            assertEquals(1, data.getActiveProfile());
        }
    }

    @Nested
    @DisplayName("entry operations")
    class Entries {

        @Test
        void addNewEntryReturnsTrue() {
            assertTrue(addItem("minecraft:dirt"));
            assertTrue(containsItem("minecraft:dirt"));
        }

        @Test
        void addDuplicateReturnsFalse() {
            addItem("minecraft:dirt");
            assertFalse(addItem("minecraft:dirt"));
        }

        @Test
        void addRejectsNull() {
            assertFalse(data.addEntry(null));
        }

        @Test
        void addRejectsBlankItemId() {
            assertFalse(data.addEntry(BlacklistEntry.any(" ")));
        }

        @Test
        void addGoesToActiveProfileOnly() {
            data.setActiveProfile(3);
            addItem("minecraft:dirt");
            assertTrue(containsItem("minecraft:dirt"));
            data.setActiveProfile(1);
            assertFalse(containsItem("minecraft:dirt"));
        }

        @Test
        void removeExistingReturnsTrue() {
            addItem("minecraft:dirt");
            assertTrue(removeItem("minecraft:dirt"));
            assertFalse(containsItem("minecraft:dirt"));
        }

        @Test
        void removeMissingReturnsFalse() {
            assertFalse(removeItem("minecraft:dirt"));
        }

        @Test
        void removeRejectsNull() {
            assertFalse(data.removeEntry(null));
        }

        @Test
        void containsRejectsNull() {
            assertFalse(data.containsEntry(null));
        }

        @Test
        void sameEntryDifferentFormIsDuplicate() {
            data.addEntry(any("minecraft:dirt"));
            assertFalse(data.addEntry(BlacklistEntry.any("minecraft:dirt")));
        }

        @Test
        void enchantEntryDoesNotCollideWithAnyEntry() {
            addItem("minecraft:enchanted_book");
            assertTrue(data.addEntry(book("minecraft:soul_speed", 1)));
            assertTrue(containsItem("minecraft:enchanted_book"));
            assertTrue(data.containsEntry(book("minecraft:soul_speed", 1)));
        }

        @Test
        void removeEnchantEntryOnlyRemovesThatEntry() {
            data.addEntry(book("minecraft:soul_speed", 1));
            data.addEntry(book("minecraft:mending", 1));
            assertTrue(data.removeEntry(book("minecraft:soul_speed", 1)));
            assertFalse(data.containsEntry(book("minecraft:soul_speed", 1)));
            assertTrue(data.containsEntry(book("minecraft:mending", 1)));
        }

        @Test
        void entriesOfPreservesInsertionOrder() {
            addItem("minecraft:c");
            addItem("minecraft:a");
            addItem("minecraft:b");
            addItem("minecraft:a");
            assertEquals(List.of("minecraft:c", "minecraft:a", "minecraft:b"),
                    data.entriesOf(1).stream().map(BlacklistEntry::itemId).toList());
        }

        @Test
        void entriesOfUnknownProfileIsEmpty() {
            assertTrue(data.entriesOf(0).isEmpty());
            assertTrue(data.entriesOf(99).isEmpty());
        }

        @Test
        void entriesOfIsUnmodifiable() {
            addItem("minecraft:dirt");
            assertThrows(UnsupportedOperationException.class,
                    () -> data.entriesOf(1).add(any("minecraft:stone")));
        }
    }

    @Nested
    @DisplayName("clear")
    class Clear {

        @Test
        void clearResetsEverything() {
            data.setEnabled(false);
            data.setActiveProfile(2);
            addItem("minecraft:dirt");
            data.addEntry(book("minecraft:soul_speed", 1));
            data.clear();

            assertTrue(data.isEnabled());
            assertEquals(1, data.getActiveProfile());
            assertTrue(data.entriesOf(1).isEmpty());
            assertTrue(data.entriesOf(2).isEmpty());
        }
    }

    @Nested
    @DisplayName("fromJson")
    class FromJson {

        @Test
        void nullRootKeepsDefaults() {
            data.setEnabled(false);
            data.setActiveProfile(2);
            addItem("minecraft:dirt");
            data.fromJson(null);

            assertTrue(data.isEnabled());
            assertEquals(1, data.getActiveProfile());
            assertTrue(data.entriesOf(1).isEmpty());
        }

        @Test
        void emptyObjectKeepsDefaults() {
            data.fromJson(new JsonObject());
            assertTrue(data.isEnabled());
            assertEquals(1, data.getActiveProfile());
            assertTrue(data.entriesOf(1).isEmpty());
        }

        @Test
        void enabledFalseIsRead() {
            data.fromJson(parse("{\"enabled\": false}"));
            assertFalse(data.isEnabled());
        }

        @Test
        void enabledTrueIsRead() {
            data.fromJson(parse("{\"enabled\": false, \"enabled\": true}"));
            assertTrue(data.isEnabled());
        }

        @Test
        void enabledMissingDefaultsToTrue() {
            data.fromJson(parse("{\"profiles\": {}}"));
            assertTrue(data.isEnabled());
        }

        @Test
        void enabledNonBooleanIsIgnored() {
            data.fromJson(parse("{\"enabled\": \"yes\"}"));
            assertTrue(data.isEnabled());
        }

        @Test
        void activeProfileValidIsRead() {
            data.fromJson(parse("{\"activeProfile\": 2}"));
            assertEquals(2, data.getActiveProfile());
        }

        @Test
        void activeProfileAtMaxBoundaryIsRead() {
            data.fromJson(parse("{\"activeProfile\": 3}"));
            assertEquals(3, data.getActiveProfile());
        }

        @Test
        void activeProfileBelowRangeIsIgnored() {
            data.fromJson(parse("{\"activeProfile\": 0}"));
            assertEquals(1, data.getActiveProfile());
        }

        @Test
        void activeProfileAboveRangeIsIgnored() {
            data.fromJson(parse("{\"activeProfile\": 4}"));
            assertEquals(1, data.getActiveProfile());
        }

        @Test
        void activeProfileNegativeIsIgnored() {
            data.fromJson(parse("{\"activeProfile\": -2}"));
            assertEquals(1, data.getActiveProfile());
        }

        @Test
        void activeProfileNonNumberIsIgnored() {
            data.fromJson(parse("{\"activeProfile\": \"two\"}"));
            assertEquals(1, data.getActiveProfile());
        }

        @Test
        void stringEntriesLoadAsAnyMode() {
            data.fromJson(parse("{\"profiles\": {\"1\": [\"minecraft:dirt\", \"minecraft:stone\"]}}"));
            assertEquals(Set.of("minecraft:dirt", "minecraft:stone"),
                    data.entriesOf(1).stream().map(BlacklistEntry::itemId).collect(java.util.stream.Collectors.toSet()));
            assertTrue(data.entriesOf(1).stream().allMatch(e -> e.mode() == BlacklistEntry.Mode.ANY));
        }

        @Test
        void legacyObjectEntriesLoadAsAnyMode() {
            data.fromJson(parse("{\"profiles\": {\"1\": [{\"item\": \"minecraft:dirt\"}]}}"));
            assertEquals(Set.of("minecraft:dirt"),
                    data.entriesOf(1).stream().map(BlacklistEntry::itemId).collect(java.util.stream.Collectors.toSet()));
            assertTrue(data.entriesOf(1).stream().allMatch(e -> e.mode() == BlacklistEntry.Mode.ANY));
        }

        @Test
        void legacyItemsArrayFillsProfileOne() {
            data.fromJson(parse("{\"items\": [\"minecraft:dirt\", \"minecraft:stone\"]}"));
            assertEquals(2, data.entriesOf(1).size());
            assertTrue(data.entriesOf(2).isEmpty());
            assertTrue(data.entriesOf(3).isEmpty());
        }

        @Test
        void profilesTakePrecedenceOverLegacyItems() {
            data.fromJson(parse("{\"items\": [\"minecraft:dirt\"], \"profiles\": {\"1\": [\"minecraft:stone\"]}}"));
            assertEquals(1, data.entriesOf(1).size());
            assertTrue(containsItem("minecraft:stone"));
        }

        @Test
        void profilesNonObjectFallsBackToLegacyItems() {
            data.fromJson(parse("{\"profiles\": \"broken\", \"items\": [\"minecraft:dirt\"]}"));
            assertEquals(1, data.entriesOf(1).size());
        }

        @Test
        void profilesAreReadPerProfile() {
            data.fromJson(parse("{\"profiles\": {\"1\": [\"minecraft:dirt\"], \"2\": [\"minecraft:stone\"], \"3\": [\"minecraft:sand\"]}}"));
            assertTrue(containsItem("minecraft:dirt"));
            data.setActiveProfile(2);
            assertTrue(containsItem("minecraft:stone"));
            data.setActiveProfile(3);
            assertTrue(containsItem("minecraft:sand"));
        }

        @Test
        void unknownProfileKeysAreIgnored() {
            data.fromJson(parse("{\"profiles\": {\"0\": [\"minecraft:dirt\"], \"4\": [\"minecraft:stone\"], \"x\": [\"minecraft:cobblestone\"]}}"));
            assertTrue(data.entriesOf(1).isEmpty());
            assertTrue(data.entriesOf(2).isEmpty());
            assertTrue(data.entriesOf(3).isEmpty());
        }

        @Test
        void malformedProfileValuesAreIgnored() {
            data.fromJson(parse("{\"profiles\": {\"1\": {\"not\": \"an array\"}, \"2\": \"also not\", \"3\": 5}}"));
            assertTrue(data.entriesOf(1).isEmpty());
            assertTrue(data.entriesOf(2).isEmpty());
            assertTrue(data.entriesOf(3).isEmpty());
        }

        @Test
        void enchantEntryIsParsed() {
            data.fromJson(parse("{\"profiles\": {\"1\": [{\"item\": \"minecraft:enchanted_book\", \"mode\": \"enchant\","
                    + " \"enchantments\": [{\"id\": \"minecraft:soul_speed\", \"level\": 2}]}]}}"));
            assertEquals(1, data.entriesOf(1).size());
            BlacklistEntry entry = data.entriesOf(1).iterator().next();
            assertEquals(BlacklistEntry.Mode.ENCHANT, entry.mode());
            assertEquals(List.of(new BlacklistEntry.Enchantment("minecraft:soul_speed", 2)), entry.enchantments());
        }

        @Test
        void enchantEntryWithMissingLevelDefaultsToOne() {
            data.fromJson(parse("{\"profiles\": {\"1\": [{\"item\": \"minecraft:enchanted_book\", \"mode\": \"enchant\","
                    + " \"enchantments\": [{\"id\": \"minecraft:soul_speed\"}]}]}}"));
            BlacklistEntry entry = data.entriesOf(1).iterator().next();
            assertEquals(List.of(new BlacklistEntry.Enchantment("minecraft:soul_speed", 1)), entry.enchantments());
        }

        @Test
        void enchantEntryWithNegativeLevelIsClamped() {
            data.fromJson(parse("{\"profiles\": {\"1\": [{\"item\": \"minecraft:enchanted_book\", \"mode\": \"enchant\","
                    + " \"enchantments\": [{\"id\": \"minecraft:soul_speed\", \"level\": -3}]}]}}"));
            BlacklistEntry entry = data.entriesOf(1).iterator().next();
            assertEquals(1, entry.enchantments().get(0).level());
        }

        @Test
        void enchantEntryWithEmptyEnchantmentsFallsBackToAny() {
            data.fromJson(parse("{\"profiles\": {\"1\": [{\"item\": \"minecraft:enchanted_book\", \"mode\": \"enchant\","
                    + " \"enchantments\": []}]}}"));
            assertEquals(1, data.entriesOf(1).size());
            assertEquals(BlacklistEntry.Mode.ANY, data.entriesOf(1).iterator().next().mode());
        }

        @Test
        void enchantEntryWithMissingEnchantmentsArrayFallsBackToAny() {
            data.fromJson(parse("{\"profiles\": {\"1\": [{\"item\": \"minecraft:enchanted_book\", \"mode\": \"enchant\"}]}}"));
            assertEquals(BlacklistEntry.Mode.ANY, data.entriesOf(1).iterator().next().mode());
        }

        @Test
        void enchantEntryWithInvalidEnchantmentIsDropped() {
            data.fromJson(parse("{\"profiles\": {\"1\": [{\"item\": \"minecraft:enchanted_book\", \"mode\": \"enchant\","
                    + " \"enchantments\": [{\"level\": 2}, \"junk\", 5]}]}}"));
            assertTrue(data.entriesOf(1).isEmpty());
        }

        @Test
        void enchantEntryWithBlankEnchantmentIdIsDropped() {
            data.fromJson(parse("{\"profiles\": {\"1\": [{\"item\": \"minecraft:enchanted_book\", \"mode\": \"enchant\","
                    + " \"enchantments\": [{\"id\": \"  \", \"level\": 2}]}]}}"));
            assertTrue(data.entriesOf(1).isEmpty());
        }

        @Test
        void enchantEntryWithNonStringItemIsDropped() {
            data.fromJson(parse("{\"profiles\": {\"1\": [{\"item\": 5, \"mode\": \"enchant\","
                    + " \"enchantments\": [{\"id\": \"minecraft:soul_speed\"}]}]}}"));
            assertTrue(data.entriesOf(1).isEmpty());
        }

        @Test
        void unknownModeIsAny() {
            data.fromJson(parse("{\"profiles\": {\"1\": [{\"item\": \"minecraft:dirt\", \"mode\": \"weird\"}]}}"));
            assertEquals(BlacklistEntry.Mode.ANY, data.entriesOf(1).iterator().next().mode());
        }

        @Test
        void objectWithoutItemIsSkipped() {
            data.fromJson(parse("{\"profiles\": {\"1\": [{\"name\": \"dirt\"}, {}, \"minecraft:stone\"]}}"));
            assertEquals(1, data.entriesOf(1).size());
            assertTrue(containsItem("minecraft:stone"));
        }

        @Test
        void nonStringPrimitivesAreSkipped() {
            data.fromJson(parse("{\"profiles\": {\"1\": [5, 1.5, true, null]}}"));
            assertTrue(data.entriesOf(1).isEmpty());
        }

        @Test
        void blankIdsAreSkipped() {
            data.fromJson(parse("{\"profiles\": {\"1\": [\"\", \"   \", \"minecraft:dirt\"]}}"));
            assertEquals(1, data.entriesOf(1).size());
        }

        @Test
        void entriesAreTrimmed() {
            data.fromJson(parse("{\"profiles\": {\"1\": [\"  minecraft:dirt  \"]}}"));
            assertTrue(containsItem("minecraft:dirt"));
        }

        @Test
        void duplicateEntriesAreDeduplicatedAcrossForms() {
            data.fromJson(parse("{\"profiles\": {\"1\": [\"minecraft:dirt\", {\"item\": \"minecraft:dirt\"}]}}"));
            assertEquals(1, data.entriesOf(1).size());
        }

        @Test
        void duplicateEnchantEntriesAreDeduplicatedRegardlessOfOrder() {
            data.fromJson(parse("{\"profiles\": {\"1\": ["
                    + "{\"item\": \"minecraft:enchanted_book\", \"mode\": \"enchant\", \"enchantments\": [{\"id\": \"minecraft:a\", \"level\": 1}, {\"id\": \"minecraft:b\", \"level\": 2}]},"
                    + "{\"item\": \"minecraft:enchanted_book\", \"mode\": \"enchant\", \"enchantments\": [{\"id\": \"minecraft:b\", \"level\": 2}, {\"id\": \"minecraft:a\", \"level\": 1}]}]}}"));
            assertEquals(1, data.entriesOf(1).size());
        }

        @Test
        void nestedArraysAreSkipped() {
            data.fromJson(parse("{\"profiles\": {\"1\": [[\"minecraft:dirt\"]]}}"));
            assertTrue(data.entriesOf(1).isEmpty());
        }

        @Test
        void loadReplacesPreviousState() {
            data.setEnabled(false);
            data.setActiveProfile(3);
            addItem("minecraft:stone");
            data.fromJson(parse("{\"enabled\": true, \"activeProfile\": 1, \"profiles\": {\"1\": [\"minecraft:dirt\"]}}"));

            assertTrue(data.isEnabled());
            assertEquals(1, data.getActiveProfile());
            assertEquals(1, data.entriesOf(1).size());
            assertFalse(containsItem("minecraft:stone"));
        }
    }

    @Nested
    @DisplayName("toJson")
    class ToJson {

        @Test
        void writesEnabledActiveProfileAndProfiles() {
            data.setActiveProfile(2);
            addItem("minecraft:dirt");

            JsonObject root = data.toJson();
            assertTrue(root.get("enabled").getAsBoolean());
            assertEquals(2, root.get("activeProfile").getAsInt());
            JsonObject profiles = root.getAsJsonObject("profiles");
            assertEquals(0, profiles.getAsJsonArray("1").size());
            assertEquals("minecraft:dirt", profiles.getAsJsonArray("2").get(0).getAsString());
            assertEquals(0, profiles.getAsJsonArray("3").size());
        }

        @Test
        void emptyDataHasAllProfileKeys() {
            JsonObject root = data.toJson();
            JsonObject profiles = root.getAsJsonObject("profiles");
            assertTrue(profiles.getAsJsonArray("1").isEmpty());
            assertTrue(profiles.getAsJsonArray("2").isEmpty());
            assertTrue(profiles.getAsJsonArray("3").isEmpty());
        }

        @Test
        void anyEntriesSerializeAsPlainStrings() {
            addItem("minecraft:dirt");
            JsonArray entries = data.toJson().getAsJsonObject("profiles").getAsJsonArray("1");
            assertEquals(1, entries.size());
            assertTrue(entries.get(0).isJsonPrimitive());
            assertEquals("minecraft:dirt", entries.get(0).getAsString());
        }

        @Test
        void enchantEntriesSerializeWithModeAndLevels() {
            data.addEntry(book("minecraft:soul_speed", 2));
            JsonArray entries = data.toJson().getAsJsonObject("profiles").getAsJsonArray("1");
            assertEquals(1, entries.size());
            JsonObject entryObject = entries.get(0).getAsJsonObject();
            assertEquals("minecraft:enchanted_book", entryObject.get("item").getAsString());
            assertEquals("enchant", entryObject.get("mode").getAsString());
            JsonObject enchantment = entryObject.getAsJsonArray("enchantments").get(0).getAsJsonObject();
            assertEquals("minecraft:soul_speed", enchantment.get("id").getAsString());
            assertEquals(2, enchantment.get("level").getAsInt());
        }

        @Test
        void roundTripPreservesState() {
            data.setEnabled(false);
            data.setActiveProfile(3);
            addItem("minecraft:dirt");
            addItem("minecraft:stone");
            data.addEntry(book("minecraft:soul_speed", 2));

            BlacklistData loaded = new BlacklistData();
            loaded.fromJson(data.toJson());

            assertFalse(loaded.isEnabled());
            assertEquals(3, loaded.getActiveProfile());
            assertEquals(data.entriesOf(1), loaded.entriesOf(1));
            assertTrue(loaded.containsEntry(book("minecraft:soul_speed", 2)));
        }

        @Test
        void roundTripOfLegacyFormatNormalizesToProfiles() {
            BlacklistData loaded = new BlacklistData();
            loaded.fromJson(parse("{\"items\": [\"minecraft:dirt\"]}"));
            JsonObject root = loaded.toJson();
            assertNull(root.get("items"));
            assertTrue(root.has("profiles"));
            JsonElement profiles = root.get("profiles");
            assertTrue(profiles.isJsonObject());
        }

        @Test
        void roundTripIsStableAcrossRepeatedCycles() {
            data.addEntry(book("minecraft:soul_speed", 1));
            addItem("minecraft:dirt");

            BlacklistData first = new BlacklistData();
            first.fromJson(data.toJson());
            BlacklistData second = new BlacklistData();
            second.fromJson(first.toJson());

            assertEquals(first.entriesOf(1), second.entriesOf(1));
        }
    }

    @Nested
    @DisplayName("sanitizeWorldIdentifier")
    class Sanitize {

        @Test
        void nullBecomesEmpty() {
            assertEquals("", BlacklistData.sanitizeWorldIdentifier(null));
        }

        @Test
        void emptyStaysEmpty() {
            assertEquals("", BlacklistData.sanitizeWorldIdentifier(""));
        }

        @Test
        void simpleNameUnchanged() {
            assertEquals("NewWorld", BlacklistData.sanitizeWorldIdentifier("NewWorld"));
        }

        @Test
        void spacesBecomeUnderscores() {
            assertEquals("New_World", BlacklistData.sanitizeWorldIdentifier("New World"));
        }

        @Test
        void allowedCharactersKept() {
            assertEquals("a-b_c.d9", BlacklistData.sanitizeWorldIdentifier("a-b_c.d9"));
        }

        @Test
        void parenthesesAndSpecialsReplaced() {
            assertEquals("New_World__1_", BlacklistData.sanitizeWorldIdentifier("New World (1)"));
        }

        @Test
        void nonAsciiReplaced() {
            assertEquals("____", BlacklistData.sanitizeWorldIdentifier("мир!"));
        }

        @Test
        void everyIllegalCharacterReplaced() {
            String input = "a/b\\c:d*e?f\"g<h>i|j*k";
            String result = BlacklistData.sanitizeWorldIdentifier(input);
            assertFalse(result.chars().anyMatch(c -> "/\\:*?\"<>|".indexOf(c) >= 0));
            assertEquals(input.length(), result.length());
        }
    }
}
