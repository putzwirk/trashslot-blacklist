package com.putzwirk.trashslotblacklist.client.gui;

import com.putzwirk.trashslotblacklist.client.BlacklistManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.item.ItemStack;

import java.util.List;
import java.util.Locale;

public class BlacklistPanel extends AbstractWidget {

    private static final int SLOT = 18;
    private static final int COLS = 9;
    private static final int PAD = 3;
    private static final int HEADER_HEIGHT = 14;
    private static final int SEARCH_HEIGHT = 12;
    private static final int SCROLLBAR_WIDTH = 5;
    private static final int TOGGLE_WIDTH = 12;

    private int maxRows = 1;
    private final ToggleSwitch toggleSwitch;
    private final EditBox searchBox;

    private boolean expanded;
    private int scroll;
    private boolean draggingScrollbar;

    public BlacklistPanel() {
        super(0, 0, panelWidth(), 0, Component.empty());
        this.toggleSwitch = new ToggleSwitch(0, 0, BlacklistManager.isBlacklistEnabled(), BlacklistManager::setBlacklistEnabled);

        Font font = Minecraft.getInstance().font;
        this.searchBox = new EditBox(font, 0, 0, panelWidth() - (PAD * 2), SEARCH_HEIGHT, Component.translatable("trashslotblacklist.search_hint"));
        this.searchBox.setHint(Component.translatable("trashslotblacklist.search_hint"));
        this.searchBox.setMaxLength(32);
        this.searchBox.setTextColor(0xFFFFFFFF);
        this.searchBox.setBordered(true);
        this.searchBox.setResponder(s -> this.scroll = 0);

        updateHeight();
    }

    public void layout(int guiLeft, int guiTop, int imageWidth, int imageHeight, int screenHeight) {
        int targetY = guiTop + imageHeight + 2;
        int availableSpace = screenHeight - targetY - PAD;

        int topOffset = HEADER_HEIGHT + SEARCH_HEIGHT + (PAD * 2);
        int rowsPossible = (availableSpace - topOffset - PAD) / SLOT;
        maxRows = Math.max(1, rowsPossible);

        int panelX = guiLeft + (imageWidth - this.width) / 2;
        setPosition(panelX, targetY);

        searchBox.setPosition(getX() + PAD, getY() + HEADER_HEIGHT + PAD);

        updateHeight();
    }

    public static int panelWidth() {
        return PAD + COLS * SLOT + SCROLLBAR_WIDTH + PAD;
    }

    public boolean isExpanded() {
        return expanded;
    }

    public void toggleExpanded() {
        expanded = !expanded;
        if (expanded) {
            scroll = 0;
            searchBox.setValue("");
        }
        draggingScrollbar = false;
        updateHeight();
    }

    public void scroll(int amount) {
        setScroll(scroll - amount);
    }

    public boolean mouseScrolledInPanel(double mouseX, double mouseY, double delta) {
        if (expanded && isMouseOver(mouseX, mouseY) && totalRows() > maxRows) {
            scroll((int) Math.signum(delta));
            return true;
        }
        return false;
    }

    private void updateHeight() {
        this.height = panelHeight();
    }

    private int panelHeight() {
        if (!expanded) {
            return 0;
        }
        return getVisibleRows() * SLOT + HEADER_HEIGHT + SEARCH_HEIGHT + (PAD * 3);
    }

    private List<ItemStack> getFilteredItems() {
        List<ItemStack> items = BlacklistManager.getBlacklistedItems();
        String query = searchBox.getValue().trim().toLowerCase(Locale.ROOT);
        if (query.isEmpty()) {
            return items;
        }
        return items.stream()
                .filter(stack -> stack.getHoverName().getString().toLowerCase(Locale.ROOT).contains(query))
                .toList();
    }

    private int totalRows() {
        int size = getFilteredItems().size();
        return Math.max(1, (size + COLS - 1) / COLS);
    }

    private int getVisibleRows() {
        return Math.min(totalRows(), maxRows);
    }

    private int maxScroll() {
        return Math.max(0, totalRows() - maxRows);
    }

    private void setScroll(int newScroll) {
        scroll = Math.max(0, Math.min(maxScroll(), newScroll));
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        if (!expanded) {
            return false;
        }
        int x = getX();
        int y = getY();
        return mouseX >= x && mouseX < x + this.width && mouseY >= y && mouseY < y + this.height;
    }

    @Override
    protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        if (!expanded) {
            return;
        }
        updateHeight();

        int x = getX();
        int y = getY();
        int width = this.width;
        int height = this.height;
        int visibleRows = getVisibleRows();

        graphics.fill(x, y, x + width, y + height, -3750202);
        renderVanillaBorder(graphics, x, y, width, height);

        Font font = Minecraft.getInstance().font;

        Component title = Component.translatable("trashslotblacklist.panel_name");
        graphics.drawString(font, title, x + PAD, y + 4, -12632257, false);

        int titleWidth = font.width(title);
        int toggleX = x + PAD + titleWidth + 3;

        toggleSwitch.setPosition(toggleX, y + 4);
        toggleSwitch.visible = true;
        toggleSwitch.setEnabled(BlacklistManager.isBlacklistEnabled());
        toggleSwitch.render(graphics, mouseX, mouseY, partialTick);

        int activeProf = BlacklistManager.getActiveProfile();
        int profBtnX = toggleX + TOGGLE_WIDTH + 15;
        int profBtnY = y + 3;

        for (int i = 1; i <= 3; i++) {
            int bx = profBtnX + (i - 1) * 11;
            boolean isActive = (i == activeProf);

            int bgColor = isActive ? 0xFFFFFFFF : 0xFFE0E0E0;
            int textColor = isActive ? 0xFF000000 : 0xFF555555;
            int borderColor = isActive ? 0xFF373737 : 0xFF8B8B8B;

            graphics.fill(bx, profBtnY, bx + 9, profBtnY + 9, bgColor);
            graphics.fill(bx, profBtnY, bx + 9, profBtnY + 1, borderColor);
            graphics.fill(bx, profBtnY, bx + 1, profBtnY + 9, borderColor);
            graphics.fill(bx + 8, profBtnY, bx + 9, profBtnY + 9, borderColor);
            graphics.fill(bx, profBtnY + 8, bx + 9, profBtnY + 9, borderColor);

            graphics.drawString(font, String.valueOf(i), bx + 2, profBtnY + 1, textColor, false);
        }

        searchBox.render(graphics, mouseX, mouseY, partialTick);

        List<ItemStack> items = getFilteredItems();
        int startX = x + PAD;
        int startY = y + HEADER_HEIGHT + SEARCH_HEIGHT + (PAD * 2);

        if (items.isEmpty()) {
            renderEmptyMessage(graphics, x, width, startY);
            return;
        }

        for (int row = 0; row < visibleRows; row++) {
            int rowIndex = row + scroll;
            for (int col = 0; col < COLS; col++) {
                int itemIndex = rowIndex * COLS + col;
                if (itemIndex >= items.size()) {
                    continue;
                }
                renderSlot(graphics, items.get(itemIndex), startX + col * SLOT, startY + row * SLOT, mouseX, mouseY);
            }
        }

        if (totalRows() > maxRows) {
            renderScrollbar(graphics, x + width - SCROLLBAR_WIDTH - PAD, startY, mouseX, mouseY);
        }
    }

    private void renderEmptyMessage(GuiGraphics graphics, int x, int width, int startY) {
        MutableComponent message = Component.translatable("trashslotblacklist.empty");
        Font font = Minecraft.getInstance().font;
        int messageWidth = font.width(message);
        graphics.drawString(font, message, x + width / 2 - messageWidth / 2, startY + 4, -7829368, false);
    }

    private void renderVanillaBorder(GuiGraphics graphics, int x, int y, int width, int height) {
        graphics.fill(x, y, x + width, y + 1, -16777216);
        graphics.fill(x, y, x + 1, y + height, -16777216);
        graphics.fill(x + width - 1, y, x + width, y + height, -16777216);
        graphics.fill(x, y + height - 1, x + width, y + height, -16777216);
        graphics.fill(x + 1, y + 1, x + width - 1, y + 2, -1);
        graphics.fill(x + 1, y + 1, x + 2, y + height - 1, -1);
        graphics.fill(x + width - 2, y + 1, x + width - 1, y + height - 1, -11184811);
        graphics.fill(x + 1, y + height - 2, x + width - 1, y + height - 1, -11184811);
    }

    private void renderSlot(GuiGraphics graphics, ItemStack stack, int x, int y, int mouseX, int mouseY) {
        graphics.fill(x, y, x + SLOT, y + SLOT, -10855846);
        graphics.fill(x, y, x + SLOT, y + 1, -13158601);
        graphics.fill(x, y, x + 1, y + SLOT, -13158601);
        graphics.fill(x, y + SLOT - 1, x + SLOT, y + SLOT, -1);
        graphics.fill(x + SLOT - 1, y, x + SLOT, y + SLOT, -1);

        graphics.renderItem(stack, x + 1, y + 1);
        graphics.renderItemDecorations(Minecraft.getInstance().font, stack, x, y);
        graphics.fill(x, y, x + SLOT - 1, y + SLOT - 1, -2130706433);

        if (mouseX >= x && mouseX < x + SLOT && mouseY >= y && mouseY < y + SLOT) {
            graphics.renderTooltip(
                    Minecraft.getInstance().font,
                    Screen.getTooltipFromItem(Minecraft.getInstance(), stack),
                    stack.getTooltipImage(),
                    mouseX,
                    mouseY
            );
        }
    }

    private void renderScrollbar(GuiGraphics graphics, int sbX, int sbY, int mouseX, int mouseY) {
        int maxScroll = maxScroll();
        if (maxScroll == 0) {
            return;
        }

        int trackHeight = maxRows * SLOT;
        graphics.fill(sbX, sbY, sbX + SCROLLBAR_WIDTH, sbY + trackHeight, -16777216);

        int thumbHeight = Math.max(6, maxRows * trackHeight / totalRows());
        int trackTravel = Math.max(1, trackHeight - thumbHeight);
        int thumbY = sbY + (int) (trackTravel * ((float) scroll / maxScroll));

        boolean hovered = mouseX >= sbX && mouseX < sbX + SCROLLBAR_WIDTH && mouseY >= thumbY && mouseY < thumbY + thumbHeight;
        int color = draggingScrollbar || hovered ? -5592406 : -3750202;

        graphics.fill(sbX, thumbY, sbX + SCROLLBAR_WIDTH, thumbY + thumbHeight, color);
        graphics.fill(sbX, thumbY, sbX + SCROLLBAR_WIDTH, thumbY + 1, -1);
        graphics.fill(sbX, thumbY, sbX + 1, thumbY + thumbHeight, -1);
        graphics.fill(sbX + SCROLLBAR_WIDTH - 1, thumbY, sbX + SCROLLBAR_WIDTH, thumbY + thumbHeight, -11184811);
        graphics.fill(sbX, thumbY + thumbHeight - 1, sbX + SCROLLBAR_WIDTH, thumbY + thumbHeight, -11184811);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        if (expanded && searchBox.isFocused()) {
            return searchBox.charTyped(codePoint, modifiers);
        }
        return false;
    }

    public void unfocusSearch() {
        searchBox.setFocused(false);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (expanded && searchBox.isFocused()) {
            if (searchBox.keyPressed(keyCode, scanCode, modifiers)) {
                return true;
            }
            if (keyCode == 256) {
                searchBox.setFocused(false);
                return true;
            }
            return false;
        }
        return false;
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (!expanded || !isMouseOver(mouseX, mouseY)) {
            searchBox.setFocused(false);
            return false;
        }

        if (toggleSwitch.isMouseOver(mouseX, mouseY)) {
            return toggleSwitch.mouseClicked(mouseX, mouseY, button);
        }

        Font font = Minecraft.getInstance().font;
        Component title = Component.translatable("trashslotblacklist.panel_name");
        int titleWidth = font.width(title);
        int toggleX = getX() + PAD + titleWidth + 3;

        int profBtnX = toggleX + TOGGLE_WIDTH + 15;
        int profBtnY = getY() + 3;

        if (mouseY >= profBtnY && mouseY < profBtnY + 9) {
            for (int i = 1; i <= 3; i++) {
                int bx = profBtnX + (i - 1) * 11;
                if (mouseX >= bx && mouseX < bx + 9) {
                    BlacklistManager.setActiveProfile(i);
                    scroll = 0;
                    return true;
                }
            }
        }

        if (searchBox.isMouseOver(mouseX, mouseY)) {
            searchBox.setFocused(true);
            return searchBox.mouseClicked(mouseX, mouseY, button);
        } else {
            searchBox.setFocused(false);
        }

        int sbX = getX() + this.width - SCROLLBAR_WIDTH - PAD;
        int sbY = getY() + HEADER_HEIGHT + SEARCH_HEIGHT + (PAD * 2);
        int trackHeight = maxRows * SLOT;

        if (totalRows() > maxRows && button == 0
                && mouseX >= sbX && mouseX < sbX + SCROLLBAR_WIDTH
                && mouseY >= sbY && mouseY < sbY + trackHeight) {
            int maxScrollValue = maxScroll();
            int thumbHeight = Math.max(6, maxRows * trackHeight / totalRows());
            int trackTravel = Math.max(1, trackHeight - thumbHeight);
            int thumbY = sbY + (int) (trackTravel * ((float) scroll / maxScrollValue));

            if (mouseY >= thumbY && mouseY < thumbY + thumbHeight) {
                draggingScrollbar = true;
            } else if (mouseY < thumbY) {
                scroll(1);
            } else {
                scroll(-1);
            }
            return true;
        }

        if (button != 1) {
            return true;
        }

        int startX = getX() + PAD;
        List<ItemStack> items = getFilteredItems();
        for (int row = 0; row < getVisibleRows(); row++) {
            for (int col = 0; col < COLS; col++) {
                int index = (row + scroll) * COLS + col;
                if (index >= items.size()) {
                    continue;
                }
                int sx = startX + col * SLOT;
                int sy = sbY + row * SLOT;
                if (mouseX >= sx && mouseX < sx + SLOT && mouseY >= sy && mouseY < sy + SLOT) {
                    BlacklistManager.removeFromBlacklist(items.get(index));
                    return true;
                }
            }
        }

        return true;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (expanded && draggingScrollbar) {
            updateScrollFromMouse(mouseY);
            return true;
        }
        return false;
    }

    private void updateScrollFromMouse(double mouseY) {
        int sbY = getY() + HEADER_HEIGHT + SEARCH_HEIGHT + (PAD * 2);
        int trackHeight = maxRows * SLOT;
        int maxScroll = maxScroll();
        if (maxScroll <= 0) {
            return;
        }

        int thumbHeight = Math.max(6, maxRows * trackHeight / totalRows());
        int trackTravel = trackHeight - thumbHeight;
        if (trackTravel <= 0) {
            return;
        }

        double clampedMouseY = Math.max(sbY, Math.min(mouseY, sbY + trackHeight));
        double relativeMouseY = Math.max(0, Math.min(clampedMouseY - sbY - thumbHeight / 2.0, trackTravel));
        float scrollPercent = (float) (relativeMouseY / trackTravel);
        setScroll(Math.round(scrollPercent * maxScroll));
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        boolean wasDragging = draggingScrollbar;
        draggingScrollbar = false;
        return wasDragging && button == 0;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {
        output.add(NarratedElementType.TITLE, Component.translatable("trashslotblacklist.panel_name"));
    }
}
