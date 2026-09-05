package com.putzwirk.trashslotblacklist;

import net.minecraft.world.item.ItemStack;

public record BlacklistEntryView(BlacklistEntry entry, ItemStack stack) {
}
