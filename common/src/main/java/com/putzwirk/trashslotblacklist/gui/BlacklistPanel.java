package com.putzwirk.trashslotblacklist.gui;

import com.putzwirk.trashslotblacklist.BlacklistData;
import com.putzwirk.trashslotblacklist.BlacklistEntryView;
import com.putzwirk.trashslotblacklist.BlacklistManager;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.narration.NarratedElementType;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class BlacklistPanel extends AbstractWidget {

    private static final int SLOT = 18;
    private static final int COLS = 9;
    private static final int PAD = 3;
    private static final int HEADER_HEIGHT = 14;
    private static final int SEARCH_HEIGHT = 12;
    private static final int SCROLLBAR_WIDTH = 5;
    private static final int TOGGLE_GAP = 15;
    private static final int PROFILE_COUNT = BlacklistData.MAX_PROFILE;
    private static final int PROFILE_SIZE = 9;
    private static final int PROFILE_SPACING = 11;

    private static final int BACKGROUND_COLOR = -3750202;
    private static final int SLOT_BACKGROUND = -10855846;
    private static final int SLOT_BORDER_DARK = -13158601;
    private static final int TITLE_COLOR = -12632257;
    private static final int EMPTY_COLOR = -7829368;
    private static final int TRACK_COLOR = -16777216;
    private static final int THUMB_COLOR = -3750202;
    private static final int THUMB_COLOR_HOVER = -5592406;
    private static final int BUTTON_COLOR = 0xFFE0E0E0;
    private static final int BUTTON_BORDER = 0xFF8B8B8B;
    private static final int BUTTON_BORDER_HOVER = 0xFF373737;
    private static final int BUTTON_ICON = 0xFF555555;

    private static final Component REMOVE_HINT = Component.translatable("trashslotblacklist.remove_hint").withStyle(ChatFormatting.GRAY);

    private int maxRows = 1;
    private final ToggleSwitch toggleSwitch;
    private final EditBox searchBox;
    private final ScrollbarModel scrollbar = new ScrollbarModel();

    private boolean expanded;
    private boolean draggingScrollbar;
    private List<BlacklistEntryView> filteredEntries = List.of();

    public BlacklistPanel() {
        super(0, 0, panelWidth(), 0, Component.empty());
        this.toggleSwitch = new ToggleSwitch(0, 0, BlacklistManager.isBlacklistEnabled(), BlacklistManager::setBlacklistEnabled);

        var font = Minecraft.getInstance().font;
        this.searchBox = new EditBox(font, 0, 0, panelWidth() - (PAD * 2), SEARCH_HEIGHT, Component.translatable("trashslotblacklist.search_hint"));
        this.searchBox.setHint(Component.translatable("trashslotblacklist.search_hint"));
        this.searchBox.setMaxLength(32);
        this.searchBox.setTextColor(0xFFFFFFFF);
        this.searchBox.setBordered(true);
        this.searchBox.setCanLoseFocus(true);
        this.searchBox.setResponder(s -> scrollbar.scrollTo(0));

        updateHeight();
    }

    public void layout(int guiLeft, int guiTop, int imageWidth, int imageHeight, int screenHeight) {
        int targetY = guiTop + imageHeight + 2;
        int availableSpace = screenHeight - targetY - PAD;

        int rowsPossible = (availableSpace - topOffset() - PAD) / SLOT;
        maxRows = Math.max(1, rowsPossible);

        setX(guiLeft + (imageWidth - getWidth()) / 2);
        setY(targetY);

        searchBox.setX(getX() + PAD);
        searchBox.setY(getY() + HEADER_HEIGHT + PAD);
        searchBox.setWidth(getWidth() - (PAD * 2));

        refreshEntries();
    }

    private static int topOffset() {
        return HEADER_HEIGHT + SEARCH_HEIGHT + (PAD * 2);
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
            scrollbar.scrollTo(0);
            searchBox.setValue("");
        } else {
            searchBox.setFocused(false);
        }
        draggingScrollbar = false;
        refreshEntries();
    }

    private void refreshEntries() {
        List<BlacklistEntryView> entries = BlacklistManager.getBlacklistedEntries();
        List<BlacklistEntryView> filtered = filterEntries(entries);
        int totalRows = Math.max(1, (filtered.size() + COLS - 1) / COLS);
        scrollbar.update(totalRows, maxRows);
        this.filteredEntries = filtered;
        updateHeight();
    }

    private int contentTop() {
        return getY() + topOffset();
    }

    private int trackHeight() {
        return maxRows * SLOT;
    }

    private boolean isOverScrollbar(double mouseX, double mouseY) {
        int sbX = getX() + getWidth() - SCROLLBAR_WIDTH - PAD;
        return mouseX >= sbX && mouseX < sbX + SCROLLBAR_WIDTH
                && mouseY >= contentTop() && mouseY < contentTop() + trackHeight();
    }

    private static int titleWidth() {
        return Minecraft.getInstance().font.width(Component.translatable("trashslotblacklist.panel_name"));
    }

    private int profileButtonX(int profile) {
        return getX() + PAD + titleWidth() + 3 + ToggleSwitch.WIDTH + TOGGLE_GAP + (profile - 1) * PROFILE_SPACING;
    }

    private int profileButtonY() {
        return getY() + 3;
    }

    private int profileAt(double mouseX, double mouseY) {
        int buttonY = profileButtonY();
        if (mouseY < buttonY || mouseY >= buttonY + PROFILE_SIZE) {
            return -1;
        }
        for (int profile = 1; profile <= PROFILE_COUNT; profile++) {
            int buttonX = profileButtonX(profile);
            if (mouseX >= buttonX && mouseX < buttonX + PROFILE_SIZE) {
                return profile;
            }
        }
        return -1;
    }

    private void updateHeight() {
        setHeight(panelHeight());
    }

    private int panelHeight() {
        if (!expanded) {
            return 0;
        }
        return scrollbar.visibleRowCount() * SLOT + HEADER_HEIGHT + SEARCH_HEIGHT + (PAD * 3);
    }

    private List<BlacklistEntryView> filterEntries(List<BlacklistEntryView> entries) {
        String query = searchBox.getValue().trim().toLowerCase(Locale.ROOT);
        if (query.isEmpty()) {
            return entries;
        }
        return entries.stream()
                .filter(view -> matches(view, query))
                .toList();
    }

    private static boolean matches(BlacklistEntryView view, String query) {
        ItemStack stack = view.stack();
        if (stack.getHoverName().getString().toLowerCase(Locale.ROOT).contains(query)) {
            return true;
        }
        Identifier key = BuiltInRegistries.ITEM.getKey(stack.getItem());
        if (key != null && key.toString().toLowerCase(Locale.ROOT).contains(query)) {
            return true;
        }
        return enchantmentNameMatches(stack, query);
    }

    private static boolean enchantmentNameMatches(ItemStack stack, String query) {
        for (String enchantmentId : BlacklistManager.getEnchantmentIds(stack)) {
            Holder<Enchantment> holder = BlacklistManager.getEnchantmentHolder(enchantmentId);
            if (holder != null && Enchantment.getFullname(holder, 1).getString().toLowerCase(Locale.ROOT).contains(query)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double scrollY) {
        if (!expanded || !isMouseOver(mouseX, mouseY) || scrollbar.maxScroll() == 0) {
            return false;
        }
        return scrollbar.scrollBy(-(int) Math.signum(scrollY));
    }

    @Override
    public boolean isMouseOver(double mouseX, double mouseY) {
        if (!expanded) {
            return false;
        }
        int x = getX();
        int y = getY();
        return mouseX >= x && mouseX < x + getWidth() && mouseY >= y && mouseY < y + getHeight();
    }

    @Override
    protected void extractWidgetRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
        if (!expanded) {
            return;
        }
        refreshEntries();

        int x = getX();
        int y = getY();
        int width = getWidth();
        int height = getHeight();

        graphics.fill(x, y, x + width, y + height, BACKGROUND_COLOR);
        renderVanillaBorder(graphics, x, y, width, height);

        var font = Minecraft.getInstance().font;
        Component title = Component.translatable("trashslotblacklist.panel_name");
        graphics.text(font, title, x + PAD, y + 4, TITLE_COLOR, false);

        int toggleX = x + PAD + titleWidth() + 3;
        toggleSwitch.setX(toggleX);
        toggleSwitch.setY(y + 4);
        toggleSwitch.visible = true;
        toggleSwitch.setEnabled(BlacklistManager.isBlacklistEnabled());
        toggleSwitch.extractRenderState(graphics, mouseX, mouseY, partialTick);

        renderProfileButtons(graphics);
        searchBox.extractRenderState(graphics, mouseX, mouseY, partialTick);

        List<BlacklistEntryView> entries = filteredEntries;
        int startX = x + PAD;
        int top = contentTop();

        if (entries.isEmpty()) {
            renderEmptyMessage(graphics, x, width, top);
            return;
        }

        int visibleRows = scrollbar.visibleRowCount();
        for (int row = 0; row < visibleRows; row++) {
            for (int col = 0; col < COLS; col++) {
                int index = (row + scrollbar.getScroll()) * COLS + col;
                if (index >= entries.size()) {
                    continue;
                }
                renderSlot(graphics, entries.get(index).stack(), startX + col * SLOT, top + row * SLOT, mouseX, mouseY);
            }
        }

        if (scrollbar.maxScroll() > 0) {
            renderScrollbar(graphics, x + width - SCROLLBAR_WIDTH - PAD, top, mouseX, mouseY);
        }
    }

    private void renderProfileButtons(GuiGraphicsExtractor graphics) {
        var font = Minecraft.getInstance().font;
        int activeProfile = BlacklistManager.getActiveProfile();
        int buttonY = profileButtonY();

        for (int profile = 1; profile <= PROFILE_COUNT; profile++) {
            int bx = profileButtonX(profile);
            boolean isActive = (profile == activeProfile);

            int bgColor = isActive ? 0xFFFFFFFF : BUTTON_COLOR;
            int textColor = isActive ? 0xFF000000 : BUTTON_ICON;
            int borderColor = isActive ? BUTTON_BORDER_HOVER : BUTTON_BORDER;

            renderPanelButton(graphics, bx, buttonY, bgColor, borderColor);
            graphics.text(font, String.valueOf(profile), bx + 2, buttonY + 1, textColor, false);
        }
    }

    private static void renderPanelButton(GuiGraphicsExtractor graphics, int x, int y, int bgColor, int borderColor) {
        graphics.fill(x, y, x + PROFILE_SIZE, y + PROFILE_SIZE, bgColor);
        graphics.fill(x, y, x + PROFILE_SIZE, y + 1, borderColor);
        graphics.fill(x, y, x + 1, y + PROFILE_SIZE, borderColor);
        graphics.fill(x + PROFILE_SIZE - 1, y, x + PROFILE_SIZE, y + PROFILE_SIZE, borderColor);
        graphics.fill(x, y + PROFILE_SIZE - 1, x + PROFILE_SIZE, y + PROFILE_SIZE, borderColor);
    }

    private void renderEmptyMessage(GuiGraphicsExtractor graphics, int x, int width, int top) {
        Component message = Component.translatable("trashslotblacklist.empty");
        var font = Minecraft.getInstance().font;
        int messageWidth = font.width(message);
        graphics.text(font, message, x + width / 2 - messageWidth / 2, top + 4, EMPTY_COLOR, false);
    }

    private void renderVanillaBorder(GuiGraphicsExtractor graphics, int x, int y, int width, int height) {
        graphics.fill(x, y, x + width, y + 1, -16777216);
        graphics.fill(x, y, x + 1, y + height, -16777216);
        graphics.fill(x + width - 1, y, x + width, y + height, -16777216);
        graphics.fill(x, y + height - 1, x + width, y + height, -16777216);
        graphics.fill(x + 1, y + 1, x + width - 1, y + 2, -1);
        graphics.fill(x + 1, y + 1, x + 2, y + height - 1, -1);
        graphics.fill(x + width - 2, y + 1, x + width - 1, y + height - 1, -11184811);
        graphics.fill(x + 1, y + height - 2, x + width - 1, y + height - 1, -11184811);
    }

    private void renderSlot(GuiGraphicsExtractor graphics, ItemStack stack, int x, int y, int mouseX, int mouseY) {
        graphics.fill(x, y, x + SLOT, y + SLOT, SLOT_BACKGROUND);
        graphics.fill(x, y, x + SLOT, y + 1, -13158601);
        graphics.fill(x, y, x + 1, y + SLOT, -13158601);
        graphics.fill(x, y + SLOT - 1, x + SLOT, y + SLOT, -1);
        graphics.fill(x + SLOT - 1, y, x + SLOT, y + SLOT, -1);

        graphics.item(stack, x + 1, y + 1);
        graphics.itemDecorations(Minecraft.getInstance().font, stack, x, y);

        if (mouseX >= x && mouseX < x + SLOT && mouseY >= y && mouseY < y + SLOT) {
            var minecraft = Minecraft.getInstance();
            List<Component> tooltip = new ArrayList<>(Screen.getTooltipFromItem(minecraft, stack));
            tooltip.add(REMOVE_HINT);
            graphics.setComponentTooltipForNextFrame(minecraft.font, tooltip, mouseX, mouseY);
        }
    }

    private void renderScrollbar(GuiGraphicsExtractor graphics, int sbX, int sbY, int mouseX, int mouseY) {
        int trackHeight = trackHeight();
        graphics.fill(sbX, sbY, sbX + SCROLLBAR_WIDTH, sbY + trackHeight, TRACK_COLOR);

        int thumbHeight = scrollbar.thumbHeight(trackHeight);
        int thumbY = scrollbar.thumbY(sbY, trackHeight);

        boolean hovered = mouseX >= sbX && mouseX < sbX + SCROLLBAR_WIDTH && mouseY >= thumbY && mouseY < thumbY + thumbHeight;
        int color = draggingScrollbar || hovered ? THUMB_COLOR_HOVER : THUMB_COLOR;

        graphics.fill(sbX, thumbY, sbX + SCROLLBAR_WIDTH, thumbY + thumbHeight, color);
        graphics.fill(sbX, thumbY, sbX + SCROLLBAR_WIDTH, thumbY + 1, -1);
        graphics.fill(sbX, thumbY, sbX + 1, thumbY + thumbHeight, -1);
        graphics.fill(sbX + SCROLLBAR_WIDTH - 1, thumbY, sbX + SCROLLBAR_WIDTH, thumbY + thumbHeight, -11184811);
        graphics.fill(sbX, thumbY + thumbHeight - 1, sbX + SCROLLBAR_WIDTH, thumbY + thumbHeight, -11184811);
    }

    @Override
    public boolean charTyped(CharacterEvent event) {
        if (expanded && searchBox.isFocused()) {
            return searchBox.charTyped(event);
        }
        return false;
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        if (expanded && searchBox.isFocused()) {
            return searchBox.keyPressed(event);
        }
        return false;
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean allowHandlingWhenUnhandled) {
        double mouseX = event.x();
        double mouseY = event.y();
        int button = event.input();

        if (!expanded || !isMouseOver(mouseX, mouseY)) {
            searchBox.setFocused(false);
            return false;
        }
        refreshEntries();

        if (toggleSwitch.isMouseOver(mouseX, mouseY)) {
            searchBox.setFocused(false);
            return toggleSwitch.mouseClicked(event, allowHandlingWhenUnhandled);
        }

        int profile = profileAt(mouseX, mouseY);
        if (profile > 0) {
            BlacklistManager.setActiveProfile(profile);
            searchBox.setFocused(false);
            refreshEntries();
            return true;
        }

        if (searchBox.isMouseOver(mouseX, mouseY)) {
            if (button == 0) {
                searchBox.mouseClicked(event, allowHandlingWhenUnhandled);
                searchBox.setFocused(true);
            }
            return true;
        }
        searchBox.setFocused(false);

        if (button == 0 && scrollbar.maxScroll() > 0 && isOverScrollbar(mouseX, mouseY)) {
            switch (scrollbar.trackClick(mouseY, contentTop(), trackHeight())) {
                case THUMB -> draggingScrollbar = true;
                case UP -> scrollbar.scrollBy(-1);
                case DOWN -> scrollbar.scrollBy(1);
            }
            return true;
        }

        if (button == 1) {
            BlacklistEntryView clicked = entryAt(mouseX, mouseY);
            if (clicked != null) {
                BlacklistManager.removeEntry(clicked.entry());
                refreshEntries();
                return true;
            }
        }

        return false;
    }

    private BlacklistEntryView entryAt(double mouseX, double mouseY) {
        int startX = getX() + PAD;
        int top = contentTop();
        for (int row = 0; row < scrollbar.visibleRowCount(); row++) {
            for (int col = 0; col < COLS; col++) {
                int index = (row + scrollbar.getScroll()) * COLS + col;
                if (index >= filteredEntries.size()) {
                    return null;
                }
                int sx = startX + col * SLOT;
                int sy = top + row * SLOT;
                if (mouseX >= sx && mouseX < sx + SLOT && mouseY >= sy && mouseY < sy + SLOT) {
                    return filteredEntries.get(index);
                }
            }
        }
        return null;
    }

    @Override
    public boolean mouseDragged(MouseButtonEvent event, double dragX, double dragY) {
        if (expanded && draggingScrollbar) {
            scrollbar.dragTo(event.y(), contentTop(), trackHeight());
            return true;
        }
        return false;
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        if (expanded && draggingScrollbar) {
            draggingScrollbar = false;
            return true;
        }
        return false;
    }

    @Override
    protected void updateWidgetNarration(NarrationElementOutput output) {
        output.add(NarratedElementType.TITLE, Component.translatable("trashslotblacklist.panel_name"));
    }

    public boolean isSearchFocused() {
        return expanded && searchBox.isFocused();
    }
}
