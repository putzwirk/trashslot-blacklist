package com.putzwirk.trashslotblacklist.mixin;

import com.putzwirk.trashslotblacklist.BoxDebug;
import com.putzwirk.trashslotblacklist.gui.BlacklistPanel;
import com.putzwirk.trashslotblacklist.gui.PanelHolder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Screen.class)
public abstract class ContainerScreenPanelMixin {

    @Inject(method = "extractTransparentBackground", at = @At("TAIL"))
    private void trashslotblacklist$drawPanelUnderBackground(GuiGraphicsExtractor graphics, CallbackInfo ci) {
        if (!((Object) this instanceof AbstractContainerScreen)) {
            return;
        }

        PanelHolder holder = (PanelHolder) (Object) this;
        BlacklistPanel panel = holder.trashslotblacklist$getPanel();
        if (panel == null || !panel.isExpanded()) {
            return;
        }

        AbstractContainerScreen<?> screen = (AbstractContainerScreen<?>) (Object) this;
        ContainerScreenAccessor accessor = (ContainerScreenAccessor) screen;
        panel.layout(
                accessor.getLeftPos(),
                accessor.getTopPos(),
                accessor.getImageWidth(),
                accessor.getImageHeight(),
                screen.height
        );
        BoxDebug.instance.log("panel bg draw y=" + panel.getY() + " h=" + panel.getHeight());

        Minecraft minecraft = Minecraft.getInstance();
        int mouseX = (int) minecraft.mouseHandler.getScaledXPos(minecraft.getWindow());
        int mouseY = (int) minecraft.mouseHandler.getScaledYPos(minecraft.getWindow());
        panel.extractRenderState(graphics, mouseX, mouseY, minecraft.getDeltaTracker().getGameTimeDeltaTicks());
    }
}