package com.putzwirk.trashslotblacklist.mixin;

import com.putzwirk.trashslotblacklist.client.BlacklistManager;
import com.putzwirk.trashslotblacklist.client.gui.BlacklistButton;
import com.putzwirk.trashslotblacklist.client.gui.BlacklistPanel;
import com.putzwirk.trashslotblacklist.platform.Services;
import net.blay09.mods.trashslot.client.TrashSlotGuiHandler;
import net.blay09.mods.trashslot.client.deletion.DeletionProvider;
import net.blay09.mods.trashslot.client.gui.TrashSlotComponent;
import net.blay09.mods.trashslot.config.TrashSlotConfig;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.Rect2i;
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
public abstract class HandledScreenMixin extends Screen {

    @Unique
    private BlacklistPanel trashslotblacklist$panel;
    @Unique
    private BlacklistButton trashslotblacklist$button;

    protected HandledScreenMixin() {
        super(null);
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

    @Inject(method = "render", at = @At("TAIL"))
    private void trashslotblacklist$onRender(GuiGraphics graphics, int mouseX, int mouseY, float partialTick, CallbackInfo ci) {
        trashslotblacklist$ensureWidgets();

        AbstractContainerScreen<?> screen = (AbstractContainerScreen<?>) (Object) this;
        ContainerScreenAccessor accessor = (ContainerScreenAccessor) screen;

        trashslotblacklist$panel.layout(
                accessor.getLeftPos(),
                accessor.getTopPos(),
                accessor.getImageWidth(),
                accessor.getImageHeight(),
                this.height
        );

        TrashSlotComponent component = TrashSlotGuiHandler.getTrashSlotComponent();
        if (component != null && component.isVisible()) {
            Rect2i rect = component.getRectangle();
            trashslotblacklist$button.setPosition(rect.getX() + rect.getWidth() - 6, rect.getY() - 2);
            trashslotblacklist$button.visible = true;
        } else {
            trashslotblacklist$button.visible = false;
        }

        trashslotblacklist$button.render(graphics, mouseX, mouseY, partialTick);
        trashslotblacklist$panel.render(graphics, mouseX, mouseY, partialTick);
    }

    @Inject(method = "mouseClicked", at = @At("HEAD"), cancellable = true)
    private void trashslotblacklist$onMouseClicked(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
        if (trashslotblacklist$panel == null) {
            return;
        }

        trashslotblacklist$panel.unfocusSearch();

        if (trashslotblacklist$panel.mouseClicked(mouseX, mouseY, button)) {
            cir.setReturnValue(true);
            return;
        }

        if (trashslotblacklist$button.visible && trashslotblacklist$button.mouseClicked(mouseX, mouseY, button)) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "mouseReleased", at = @At("HEAD"), cancellable = true)
    private void trashslotblacklist$onMouseReleased(double mouseX, double mouseY, int button, CallbackInfoReturnable<Boolean> cir) {
        if (trashslotblacklist$panel == null) {
            return;
        }

        boolean handled = trashslotblacklist$button.mouseReleased(mouseX, mouseY, button);
        handled |= trashslotblacklist$panel.mouseReleased(mouseX, mouseY, button);
        if (handled) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "mouseDragged", at = @At("HEAD"), cancellable = true)
    private void trashslotblacklist$onMouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY, CallbackInfoReturnable<Boolean> cir) {
        if (trashslotblacklist$panel != null && trashslotblacklist$panel.mouseDragged(mouseX, mouseY, button, dragX, dragY)) {
            cir.setReturnValue(true);
        }
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        if (trashslotblacklist$panel != null && trashslotblacklist$panel.mouseScrolledInPanel(mouseX, mouseY, delta)) {
            return true;
        }
        return super.mouseScrolled(mouseX, mouseY, delta);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        if (trashslotblacklist$panel != null && trashslotblacklist$panel.charTyped(codePoint, modifiers)) {
            return true;
        }
        return super.charTyped(codePoint, modifiers);
    }

    @Inject(method = "keyPressed", at = @At("HEAD"), cancellable = true)
    private void trashslotblacklist$onKeyPressed(int keyCode, int scanCode, int modifiers, CallbackInfoReturnable<Boolean> cir) {
        if (trashslotblacklist$panel != null && trashslotblacklist$panel.keyPressed(keyCode, scanCode, modifiers)) {
            cir.setReturnValue(true);
            return;
        }

        if (Services.PLATFORM.isBlacklistKeyActiveAndMatches(keyCode, scanCode)) {
            AbstractContainerScreen<?> screen = (AbstractContainerScreen<?>) (Object) this;
            if (trashslotblacklist$handleTrashAndBlacklist(screen)) {
                cir.setReturnValue(true);
            }
        }
    }

    @Unique
    private boolean trashslotblacklist$handleTrashAndBlacklist(AbstractContainerScreen<?> screen) {
        Player player = Minecraft.getInstance().player;
        if (player == null) {
            return false;
        }

        DeletionProvider deletionProvider = TrashSlotConfig.getDeletionProvider();
        if (deletionProvider == null) {
            return false;
        }

        ItemStack carried = screen.getMenu().getCarried();
        if (!carried.isEmpty()) {
            BlacklistManager.addToBlacklist(carried);
            deletionProvider.deleteMouseItem(player, carried, TrashSlotGuiHandler.getTrashSlot(), false);
            return true;
        }

        Slot hoveredSlot = ((ContainerScreenAccessor) screen).getHoveredSlot();
        if (hoveredSlot != null && hoveredSlot.hasItem()) {
            ItemStack stack = hoveredSlot.getItem();
            BlacklistManager.addToBlacklist(stack);
            deletionProvider.deleteContainerItem(screen.getMenu(), hoveredSlot.index, true, TrashSlotGuiHandler.getTrashSlot());
            return true;
        }

        return false;
    }
}
