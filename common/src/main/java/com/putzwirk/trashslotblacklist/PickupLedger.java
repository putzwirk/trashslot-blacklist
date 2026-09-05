package com.putzwirk.trashslotblacklist;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public final class PickupLedger {

    public static final long DEFAULT_WINDOW_MILLIS = 3000;

    private final long windowMillis;
    private final List<Entry> entries = new ArrayList<>();

    private static final class Entry {
        final String signature;
        int remaining;
        final long recordedAt;

        Entry(String signature, int remaining, long recordedAt) {
            this.signature = signature;
            this.remaining = remaining;
            this.recordedAt = recordedAt;
        }
    }

    public PickupLedger() {
        this(DEFAULT_WINDOW_MILLIS);
    }

    public PickupLedger(long windowMillis) {
        this.windowMillis = Math.max(1, windowMillis);
    }

    public void record(String signature, int amount, long now) {
        if (signature == null || signature.isBlank() || amount <= 0) {
            return;
        }
        prune(now);
        entries.add(new Entry(signature, amount, now));
    }

    public int consume(String signature, int needed, long now) {
        if (signature == null || signature.isBlank() || needed <= 0) {
            return 0;
        }
        int consumed = 0;
        Iterator<Entry> iterator = entries.iterator();
        while (iterator.hasNext() && consumed < needed) {
            Entry entry = iterator.next();
            if (now - entry.recordedAt >= windowMillis) {
                iterator.remove();
                continue;
            }
            if (!entry.signature.equals(signature)) {
                continue;
            }
            int take = Math.min(entry.remaining, needed - consumed);
            entry.remaining -= take;
            consumed += take;
            if (entry.remaining <= 0) {
                iterator.remove();
            }
        }
        return consumed;
    }

    public void prune(long now) {
        entries.removeIf(entry -> now - entry.recordedAt >= windowMillis);
    }

    public void clear() {
        entries.clear();
    }

    public int size() {
        return entries.size();
    }
}
