package com.putzwirk.trashslotblacklist.mixin;

import com.putzwirk.trashslotblacklist.BlacklistManager;
import com.putzwirk.trashslotblacklist.GroundPickupTracker;
import com.putzwirk.trashslotblacklist.platform.Services;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Mixin(Player.class)
public abstract class PlayerMixin {

    @Unique
    private static final int DELETE_RETRY_CHECKS = 5;

    @Unique
    private static final int DELETE_MAX_ATTEMPTS = 3;

    @Unique
    private final Map<Integer, ItemStack> trashslotblacklist$lastSeen = new HashMap<>();

    @Unique
    private final Map<Integer, ItemStack> trashslotblacklist$lastRequested = new HashMap<>();

    @Unique
    private final Map<Integer, Integer> trashslotblacklist$requestCooldown = new HashMap<>();

    @Unique
    private final Map<Integer, Integer> trashslotblacklist$requestAttempts = new HashMap<>();

    @Unique
    private final Set<Integer> trashslotblacklist$keptSlots = new HashSet<>();

    @Unique
    private AbstractContainerMenu trashslotblacklist$lastMenu;

    @Unique
    private int trashslotblacklist$tickCounter;

    @Inject(method = "tick", at = @At("TAIL"))
    private void trashslotblacklist$onTick(CallbackInfo ci) {
        Player self = (Player) (Object) this;
        Minecraft minecraft = Minecraft.getInstance();
        if (!self.level().isClientSide || self != minecraft.player || self.isCreative()) {
            return;
        }

        if (self.containerMenu != trashslotblacklist$lastMenu) {
            trashslotblacklist$lastMenu = self.containerMenu;
            trashslotblacklist$snapshotInventory(self);
            return;
        }

        if (++trashslotblacklist$tickCounter < 2) {
            return;
        }
        trashslotblacklist$tickCounter = 0;

        if (!BlacklistManager.isBlacklistEnabled()) {
            return;
        }

        AbstractContainerMenu menu = self.containerMenu;
        if (!menu.getCarried().isEmpty()) {
            return;
        }

        Inventory inventory = self.getInventory();
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            ItemStack current = inventory.getItem(i);

            if (current.isEmpty()) {
                trashslotblacklist$clearSlotState(i);
                trashslotblacklist$lastSeen.put(i, ItemStack.EMPTY);
                continue;
            }

            ItemStack previous = trashslotblacklist$lastSeen.get(i);
            boolean sameItem = previous != null && ItemStack.isSameItemSameComponents(current, previous);
            if (!sameItem) {
                trashslotblacklist$clearSlotState(i);
                trashslotblacklist$keptSlots.remove(i);
            }

            int delta = sameItem ? current.getCount() - previous.getCount() : current.getCount();
            trashslotblacklist$lastSeen.put(i, current.copy());

            if (!BlacklistManager.isBlacklisted(current)) {
                trashslotblacklist$keptSlots.remove(i);
                continue;
            }
            if (trashslotblacklist$keptSlots.contains(i)) {
                continue;
            }

            ItemStack requested = trashslotblacklist$lastRequested.get(i);
            boolean sameAsRequested = requested != null
                    && ItemStack.isSameItemSameComponents(current, requested)
                    && current.getCount() == requested.getCount();

            if (sameAsRequested) {
                int cooldown = trashslotblacklist$requestCooldown.getOrDefault(i, 0);
                if (cooldown > 0) {
                    trashslotblacklist$requestCooldown.put(i, cooldown - 1);
                    continue;
                }
                if (trashslotblacklist$requestAttempts.getOrDefault(i, 0) >= DELETE_MAX_ATTEMPTS) {
                    trashslotblacklist$keptSlots.add(i);
                    trashslotblacklist$clearSlotState(i);
                    continue;
                }
                trashslotblacklist$requestDelete(menu, inventory, i, current);
                continue;
            }

            if (delta <= 0) {
                continue;
            }

            int backed = GroundPickupTracker.consumeBacking(current, delta);
            if (backed < delta) {
                trashslotblacklist$keptSlots.add(i);
                trashslotblacklist$clearSlotState(i);
                continue;
            }

            trashslotblacklist$requestDelete(menu, inventory, i, current);
        }
    }

    @Unique
    private void trashslotblacklist$requestDelete(AbstractContainerMenu menu, Inventory inventory, int slotIndex, ItemStack stack) {
        int menuSlot = trashslotblacklist$findMenuSlot(menu, inventory, slotIndex);
        if (menuSlot < 0) {
            return;
        }
        Services.PLATFORM.trashDeleteContainerSlot(menu, menuSlot, false);
        trashslotblacklist$lastRequested.put(slotIndex, stack.copy());
        trashslotblacklist$requestCooldown.put(slotIndex, DELETE_RETRY_CHECKS);
        trashslotblacklist$requestAttempts.merge(slotIndex, 1, Integer::sum);
    }

    @Unique
    private void trashslotblacklist$clearSlotState(int slot) {
        trashslotblacklist$lastRequested.remove(slot);
        trashslotblacklist$requestCooldown.remove(slot);
        trashslotblacklist$requestAttempts.remove(slot);
    }

    @Unique
    private void trashslotblacklist$snapshotInventory(Player self) {
        trashslotblacklist$lastSeen.clear();
        trashslotblacklist$lastRequested.clear();
        trashslotblacklist$requestCooldown.clear();
        trashslotblacklist$requestAttempts.clear();
        trashslotblacklist$keptSlots.clear();
        Inventory inventory = self.getInventory();
        for (int i = 0; i < inventory.getContainerSize(); i++) {
            ItemStack stack = inventory.getItem(i);
            trashslotblacklist$lastSeen.put(i, stack.copy());
            if (BlacklistManager.isBlacklisted(stack)) {
                trashslotblacklist$keptSlots.add(i);
            }
        }
    }

    @Unique
    private static int trashslotblacklist$findMenuSlot(AbstractContainerMenu menu, Inventory inventory, int inventoryIndex) {
        for (Slot slot : menu.slots) {
            if (slot.container == inventory && slot.getContainerSlot() == inventoryIndex) {
                return slot.index;
            }
        }
        return -1;
    }
}
