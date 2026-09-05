package com.putzwirk.trashslotblacklist.mixin;

import com.putzwirk.trashslotblacklist.GroundPickupTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundTakeItemEntityPacket;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPacketListener.class)
public abstract class ClientPacketListenerMixin {

    @Inject(method = "handleTakeItemEntity", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;shrink(I)V"))
    private void trashslotblacklist$onTakeItemEntity(ClientboundTakeItemEntityPacket packet, CallbackInfo ci) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.level == null) {
            return;
        }
        if (packet.getPlayerId() != minecraft.player.getId()) {
            return;
        }
        Entity entity = minecraft.level.getEntity(packet.getItemId());
        if (entity instanceof ItemEntity itemEntity) {
            GroundPickupTracker.recordPickup(itemEntity.getItem(), packet.getAmount());
        }
    }
}
