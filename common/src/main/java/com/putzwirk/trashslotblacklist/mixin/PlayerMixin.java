package com.putzwirk.trashslotblacklist.mixin;

import com.putzwirk.trashslotblacklist.BlacklistManager;
import com.putzwirk.trashslotblacklist.platform.Services;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.InventoryMenu;
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
    private final Map<Integer, ItemStack> trashslotblacklist$lastSeen = new HashMap<>();

    @Unique
    private final Set<Integer> trashslotblacklist$persistentSlots = new HashSet<>();

    @Unique
    private AbstractContainerMenu trashslotblacklist$lastMenu = null;

    @Unique
    private int trashslotblacklist$tickCounter;

    @Inject(method = "tick", at = @At("TAIL"))
    private void trashslotblacklist$onTick(CallbackInfo ci) {
        Player self = (Player) (Object) this;
        Minecraft minecraft = Minecraft.getInstance();
        if (!self.level().isClientSide() || self != minecraft.player) {
            return;
        }

        if (self.isCreative()) {
            return;
        }
        if (self.containerMenu != trashslotblacklist$lastMenu) {
            trashslotblacklist$lastMenu = self.containerMenu;
            trashslotblacklist$lastSeen.clear();
            if (self.containerMenu instanceof InventoryMenu) {
                AbstractContainerMenu inventoryMenu = self.inventoryMenu;
                Inventory inventory = self.getInventory();
                for (Slot slot : inventoryMenu.slots) {
                    if (slot.container == inventory) {
                        ItemStack stack = slot.getItem();
                        trashslotblacklist$lastSeen.put(slot.index, stack.copy());
                        if (BlacklistManager.isBlacklisted(stack)) {
                            trashslotblacklist$persistentSlots.add(slot.index);
                        }
                    }
                }
            }
            return;
        }

        if (++trashslotblacklist$tickCounter < 2) {
            return;
        }
        trashslotblacklist$tickCounter = 0;

        if (!(self.containerMenu instanceof InventoryMenu)) {
            return;
        }

        if (!BlacklistManager.isBlacklistEnabled()) {
            return;
        }

        AbstractContainerMenu inventoryMenu = self.inventoryMenu;
        Inventory inventory = self.getInventory();

        for (Slot slot : inventoryMenu.slots) {
            if (slot.container != inventory) {
                continue;
            }

            ItemStack current = slot.getItem();
            ItemStack previous = trashslotblacklist$lastSeen.get(slot.index);
            if (current.isEmpty()) {
                trashslotblacklist$persistentSlots.remove(slot.index);
            }

            boolean isNewOrIncreased = !current.isEmpty() && (previous == null || previous.isEmpty()
                    || current.getCount() > previous.getCount()
                    || !ItemStack.isSameItemSameComponents(current, previous));

            trashslotblacklist$lastSeen.put(slot.index, current.copy());

            if (trashslotblacklist$persistentSlots.contains(slot.index)) {
                continue;
            }

            if (isNewOrIncreased && BlacklistManager.isBlacklisted(current)) {
                Services.PLATFORM.trashDeleteContainerSlot(inventoryMenu, slot.index, false);
            }
        }
    }
}
