package com.putzwirk.trashslotblacklist.mixin;

import com.putzwirk.trashslotblacklist.BlacklistManager;
import com.putzwirk.trashslotblacklist.ButtonStateManager;
import com.putzwirk.trashslotblacklist.gui.BlacklistButton;
import com.putzwirk.trashslotblacklist.gui.BlacklistPanel;
import com.putzwirk.trashslotblacklist.gui.PanelHolder;
import com.putzwirk.trashslotblacklist.platform.Services;
import net.blay09.mods.trashslot.client.TrashSlotGuiHandler;
import net.blay09.mods.trashslot.client.gui.TrashSlotComponent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractContainerScreen.class)
public abstract class ContainerScreenMixin extends Screen implements PanelHolder {

    protected ContainerScreenMixin(Component title) {
        super(title);
    }

    @Unique
    private BlacklistPanel trashslotblacklist$panel;
    @Unique
    private BlacklistButton trashslotblacklist$button;

    @Override
    public BlacklistPanel trashslotblacklist$getPanel() {
        return this.trashslotblacklist$panel;
    }

    @Unique
    private void trashslotblacklist$ensureWidgets() {
        if (trashslotblacklist$panel != null) {
            return;
        }

        trashslotblacklist$panel = new BlacklistPanel();
        trashslotblacklist$button = new BlacklistButton(0, 0, trashslotblacklist$panel);
        trashslotblacklist$button.visible = false;
    }

    @Inject(method = "extractContents", at = @At("TAIL"))
    private void trashslotblacklist$onExtractContents(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
        trashslotblacklist$ensureWidgets();

        TrashSlotComponent component = TrashSlotGuiHandler.getTrashSlotComponent();
        if (component != null && component.isVisible()) {
            Rect2i rect = component.getRectangle();
            trashslotblacklist$button.setX(rect.getX() + rect.getWidth() - 6);
            trashslotblacklist$button.setY(rect.getY() - 2);
            trashslotblacklist$button.visible = true;
        } else {
            trashslotblacklist$button.visible = false;
        }

        ButtonStateManager.setHovered(trashslotblacklist$button.visible
                && trashslotblacklist$button.isMouseOver(mouseX, mouseY));

        trashslotblacklist$button.extractRenderState(graphics, mouseX, mouseY, partialTick);
    }

    @Inject(method = "mouseScrolled", at = @At("HEAD"), cancellable = true)
    private void trashslotblacklist$onMouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY, CallbackInfoReturnable<Boolean> cir) {
        if (trashslotblacklist$panel != null && trashslotblacklist$panel.mouseScrolled(mouseX, mouseY, scrollX, scrollY)) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    private void trashslotblacklist$onMouseClicked(MouseButtonEvent event, boolean allowHandlingWhenUnhandled, CallbackInfoReturnable<Boolean> cir) {
        if (trashslotblacklist$panel == null) {
            return;
        }

        boolean panelHit = trashslotblacklist$panel.mouseClicked(event, allowHandlingWhenUnhandled);
        boolean buttonHit = trashslotblacklist$button.mouseClicked(event, allowHandlingWhenUnhandled);

        if (panelHit || buttonHit
                || (trashslotblacklist$panel.isExpanded() && trashslotblacklist$panel.isMouseOver(event.x(), event.y()))) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "mouseReleased", at = @At("HEAD"), cancellable = true)
    private void trashslotblacklist$onMouseReleased(MouseButtonEvent event, CallbackInfoReturnable<Boolean> cir) {
        if (trashslotblacklist$panel == null) {
            return;
        }

        boolean handled = trashslotblacklist$button.mouseReleased(event);
        handled |= trashslotblacklist$panel.mouseReleased(event);
        if (handled) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "mouseDragged", at = @At("HEAD"), cancellable = true)
    private void trashslotblacklist$onMouseDragged(MouseButtonEvent event, double dragX, double dragY, CallbackInfoReturnable<Boolean> cir) {
        if (trashslotblacklist$panel != null && trashslotblacklist$panel.mouseDragged(event, dragX, dragY)) {
            cir.setReturnValue(true);
        }
    }

    @Override
    public boolean charTyped(CharacterEvent event) {
        if (trashslotblacklist$panel != null && trashslotblacklist$panel.charTyped(event)) {
            return true;
        }
        return super.charTyped(event);
    }

    @Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true)
    private void trashslotblacklist$onKeyPressed(KeyEvent event, CallbackInfoReturnable<Boolean> cir) {
        if (trashslotblacklist$panel == null) {
            return;
        }

        if (trashslotblacklist$panel.keyPressed(event)) {
            cir.setReturnValue(true);
            return;
        }

        if (trashslotblacklist$panel.isSearchFocused()) {
            if (event.key() != 256) {
                cir.setReturnValue(true);
            }
            return;
        }

        if (!Services.PLATFORM.isBlacklistKeyActiveAndMatches(event)) {
            return;
        }

        AbstractContainerScreen<?> screen = (AbstractContainerScreen<?>) (Object) this;
        if (trashslotblacklist$handleTrashAndBlacklist(screen)) {
            cir.setReturnValue(true);
        }
    }

    @Unique
    private boolean trashslotblacklist$handleTrashAndBlacklist(AbstractContainerScreen<?> screen) {
        Player player = Minecraft.getInstance().player;
        if (player == null) {
            return false;
        }

        ItemStack carried = screen.getMenu().getCarried();
        if (!carried.isEmpty()) {
            BlacklistManager.addToBlacklist(carried);
            Services.PLATFORM.trashDeleteCarried(player, carried);
            return true;
        }

        Slot hoveredSlot = ((ContainerScreenAccessor) screen).getHoveredSlot();
        if (hoveredSlot != null && hoveredSlot.hasItem()) {
            ItemStack stack = hoveredSlot.getItem();
            BlacklistManager.addToBlacklist(stack);
            Services.PLATFORM.trashDeleteContainerSlot(screen.getMenu(), hoveredSlot.index, true);
            return true;
        }

        return false;
    }
}
