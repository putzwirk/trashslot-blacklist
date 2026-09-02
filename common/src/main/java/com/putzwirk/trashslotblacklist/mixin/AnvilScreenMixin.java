package com.putzwirk.trashslotblacklist.mixin;

import com.putzwirk.trashslotblacklist.gui.BlacklistPanel;
import com.putzwirk.trashslotblacklist.gui.PanelHolder;
import net.minecraft.client.gui.screens.inventory.AnvilScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AnvilScreen.class)
public abstract class AnvilScreenMixin {

    @Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true)
    private void trashslotblacklist$onAnvilKeyPressed(int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> cir) {
        if (this instanceof PanelHolder holder) {
            BlacklistPanel panel = holder.trashslotblacklist$getPanel();

            if (panel != null && panel.isSearchFocused()) {
                if (keyCode == 256) {
                    panel.setSearchFocused(false);
                    cir.setReturnValue(true);
                    return;
                }

                panel.keyPressed(keyCode, scanCode, modifiers);

                cir.setReturnValue(true);
            }
        }
    }
}
