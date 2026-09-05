package com.putzwirk.trashslotblacklist;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public final class GroundPickupTracker {

    private static final PickupLedger LEDGER = new PickupLedger();

    private GroundPickupTracker() {
    }

    public static void recordPickup(ItemStack stack, int amount) {
        if (stack == null || stack.isEmpty() || amount <= 0) {
            return;
        }
        LEDGER.record(signatureOf(stack), amount, System.currentTimeMillis());
    }

    public static int consumeBacking(ItemStack stack, int needed) {
        if (stack == null || stack.isEmpty() || needed <= 0) {
            return 0;
        }
        return LEDGER.consume(signatureOf(stack), needed, System.currentTimeMillis());
    }

    public static void clear() {
        LEDGER.clear();
    }

    public static String signatureOf(ItemStack stack) {
        Identifier itemId = BuiltInRegistries.ITEM.getKey(stack.getItem());
        StringBuilder signature = new StringBuilder(itemId != null ? itemId.toString() : "unknown");
        List<String> components = new ArrayList<>();
        stack.getComponents().forEach(typed -> {
            Identifier typeId = BuiltInRegistries.DATA_COMPONENT_TYPE.getKey(typed.type());
            components.add((typeId != null ? typeId.toString() : "?") + "=" + typed.value());
        });
        components.sort(String::compareTo);
        for (String component : components) {
            signature.append('|').append(component);
        }
        return signature.toString();
    }
}
