package com.putzwirk.trashslotblacklist.mixin;

import com.putzwirk.trashslotblacklist.ButtonStateManager;
import net.blay09.mods.trashslot.client.gui.TrashSlotComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = TrashSlotComponent.class, remap = false)
public class TrashSlotComponentMixin {

    @Inject(method = "isInside", at = @At("HEAD"), cancellable = true)
    private void trashslotblacklist$onIsInside(int mouseX, int mouseY, CallbackInfoReturnable<Boolean> cir) {
        if (ButtonStateManager.isHovered()) {
            cir.setReturnValue(false);
        }
    }
}
