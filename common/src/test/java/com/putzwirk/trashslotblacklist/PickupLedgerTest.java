package com.putzwirk.trashslotblacklist;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PickupLedgerTest {

    private static final long WINDOW = 1000;
    private static final String STICKS = "minecraft:stick";
    private static final String DIRT = "minecraft:dirt";

    private PickupLedger ledger;

    @BeforeEach
    void setUp() {
        ledger = new PickupLedger(WINDOW);
    }

    @Nested
    @DisplayName("record")
    class Record {

        @Test
        void recordAddsEntry() {
            ledger.record(STICKS, 5, 0);
            assertEquals(1, ledger.size());
        }

        @Test
        void recordNullSignatureIgnored() {
            ledger.record(null, 5, 0);
            assertEquals(0, ledger.size());
        }

        @Test
        void recordBlankSignatureIgnored() {
            ledger.record("  ", 5, 0);
            assertEquals(0, ledger.size());
        }

        @Test
        void recordZeroAmountIgnored() {
            ledger.record(STICKS, 0, 0);
            assertEquals(0, ledger.size());
        }

        @Test
        void recordNegativeAmountIgnored() {
            ledger.record(STICKS, -3, 0);
            assertEquals(0, ledger.size());
        }

        @Test
        void recordSameSignatureKeepsSeparateEntries() {
            ledger.record(STICKS, 2, 0);
            ledger.record(STICKS, 3, 10);
            assertEquals(2, ledger.size());
        }
    }

    @Nested
    @DisplayName("consume")
    class Consume {

        @Test
        void consumeWithinWindowReturnsFullAmount() {
            ledger.record(STICKS, 5, 0);
            assertEquals(5, ledger.consume(STICKS, 5, 500));
            assertEquals(0, ledger.size());
        }

        @Test
        void consumePartialLeavesRemainder() {
            ledger.record(STICKS, 10, 0);
            assertEquals(4, ledger.consume(STICKS, 4, 100));
            assertEquals(1, ledger.size());
            assertEquals(6, ledger.consume(STICKS, 10, 200));
        }

        @Test
        void consumeAcrossMultipleEntries() {
            ledger.record(STICKS, 3, 0);
            ledger.record(STICKS, 4, 10);
            assertEquals(7, ledger.consume(STICKS, 7, 20));
            assertEquals(0, ledger.size());
        }

        @Test
        void consumeMoreThanAvailableReturnsAvailable() {
            ledger.record(STICKS, 3, 0);
            assertEquals(3, ledger.consume(STICKS, 10, 100));
        }

        @Test
        void consumeDifferentSignatureReturnsZero() {
            ledger.record(STICKS, 5, 0);
            assertEquals(0, ledger.consume(DIRT, 5, 100));
            assertEquals(1, ledger.size());
        }

        @Test
        void consumeNullAndBlankSignatureReturnsZero() {
            assertEquals(0, ledger.consume(null, 5, 0));
            assertEquals(0, ledger.consume("", 5, 0));
        }

        @Test
        void consumeZeroOrNegativeReturnsZero() {
            ledger.record(STICKS, 5, 0);
            assertEquals(0, ledger.consume(STICKS, 0, 100));
            assertEquals(0, ledger.consume(STICKS, -1, 100));
            assertEquals(1, ledger.size());
        }

        @Test
        void consumeStopsAtNeeded() {
            ledger.record(STICKS, 3, 0);
            ledger.record(STICKS, 3, 10);
            assertEquals(5, ledger.consume(STICKS, 5, 20));
            assertEquals(1, ledger.size());
            assertEquals(1, ledger.consume(STICKS, 5, 30));
        }
    }

    @Nested
    @DisplayName("expiry")
    class Expiry {

        @Test
        void consumeAfterWindowReturnsZero() {
            ledger.record(STICKS, 5, 0);
            assertEquals(0, ledger.consume(STICKS, 5, WINDOW + 1));
            assertEquals(0, ledger.size());
        }

        @Test
        void consumeExactlyAtWindowBoundaryIsExpired() {
            ledger.record(STICKS, 5, 0);
            assertEquals(0, ledger.consume(STICKS, 5, WINDOW));
        }

        @Test
        void consumeAtWindowMinusOneIsValid() {
            ledger.record(STICKS, 5, 0);
            assertEquals(5, ledger.consume(STICKS, 5, WINDOW - 1));
        }

        @Test
        void freshEntrySurvivesWhileOldEntryExpires() {
            ledger.record(STICKS, 5, 0);
            ledger.record(STICKS, 8, WINDOW - 1);
            assertEquals(6, ledger.consume(STICKS, 6, WINDOW));
            assertEquals(1, ledger.size());
        }

        @Test
        void pruneRemovesExpiredEntries() {
            ledger.record(STICKS, 5, 0);
            ledger.prune(WINDOW + 1);
            assertEquals(0, ledger.size());
        }

        @Test
        void pruneKeepsFreshEntries() {
            ledger.record(STICKS, 5, 100);
            ledger.prune(WINDOW);
            assertEquals(1, ledger.size());
        }

        @Test
        void recordPrunesExpiredFirst() {
            ledger.record(STICKS, 5, 0);
            ledger.record(STICKS, 5, WINDOW + 10);
            assertEquals(1, ledger.size());
        }
    }

    @Nested
    @DisplayName("clear")
    class Clear {

        @Test
        void clearRemovesEverything() {
            ledger.record(STICKS, 5, 0);
            ledger.record(DIRT, 2, 10);
            ledger.clear();
            assertEquals(0, ledger.size());
            assertEquals(0, ledger.consume(STICKS, 5, 100));
        }
    }

    @Nested
    @DisplayName("default window")
    class DefaultWindow {

        @Test
        void defaultConstructorUsesThreeSecondWindow() {
            PickupLedger defaultLedger = new PickupLedger();
            defaultLedger.record(STICKS, 5, 0);
            assertTrue(defaultLedger.consume(STICKS, 5, PickupLedger.DEFAULT_WINDOW_MILLIS - 1) > 0);
        }
    }
}
