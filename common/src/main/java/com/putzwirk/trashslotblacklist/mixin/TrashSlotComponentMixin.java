package com.putzwirk.trashslotblacklist.mixin;

import com.putzwirk.trashslotblacklist.client.util.ButtonStateManager;
import net.blay09.mods.trashslot.client.gui.TrashSlotComponent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(value = TrashSlotComponent.class, remap = false)
public class TrashSlotComponentMixin {

    @Inject(method = "isInside", at = @At("HEAD"), cancellable = true, remap = false)
    private void onIsInside(int mouseX, int mouseY, CallbackInfoReturnable<Boolean> cir) {
        if (ButtonStateManager.isButtonHovered()) {
            cir.setReturnValue(false);
        }
    }
}
