package com.putzwirk.trashslotblacklist.gui;

public final class ScrollbarModel {

    public static final int MIN_THUMB_SIZE = 6;

    public enum TrackClick {
        THUMB,
        UP,
        DOWN
    }

    private int totalRows = 1;
    private int maxVisibleRows = 1;
    private int scroll;

    public void update(int totalRows, int maxVisibleRows) {
        this.totalRows = Math.max(1, totalRows);
        this.maxVisibleRows = Math.max(1, maxVisibleRows);
        clamp();
    }

    public int getScroll() {
        return scroll;
    }

    public int visibleRowCount() {
        return Math.min(totalRows, maxVisibleRows);
    }

    public int maxScroll() {
        return Math.max(0, totalRows - maxVisibleRows);
    }

    public boolean scrollBy(int delta) {
        return scrollTo(scroll + delta);
    }

    public boolean scrollTo(int value) {
        int clamped = clamp(value);
        if (clamped == scroll) {
            return false;
        }
        scroll = clamped;
        return true;
    }

    public void clamp() {
        scroll = clamp(scroll);
    }

    private int clamp(int value) {
        return Math.max(0, Math.min(maxScroll(), value));
    }

    public int thumbHeight(int trackHeight) {
        if (maxScroll() == 0) {
            return Math.max(0, trackHeight);
        }
        return Math.max(MIN_THUMB_SIZE, visibleRows() * trackHeight / totalRows);
    }

    private int visibleRows() {
        return Math.max(1, maxVisibleRows);
    }

    public int trackTravel(int trackHeight) {
        return Math.max(1, trackHeight - thumbHeight(trackHeight));
    }

    public int thumbY(int trackY, int trackHeight) {
        int maxScroll = maxScroll();
        if (maxScroll == 0) {
            return trackY;
        }
        return trackY + (int) (trackTravel(trackHeight) * (scroll / (float) maxScroll));
    }

    public TrackClick trackClick(double mouseY, int trackY, int trackHeight) {
        int thumbY = thumbY(trackY, trackHeight);
        int thumbHeight = thumbHeight(trackHeight);
        if (mouseY < thumbY) {
            return TrackClick.UP;
        }
        if (mouseY >= thumbY + thumbHeight) {
            return TrackClick.DOWN;
        }
        return TrackClick.THUMB;
    }

    public void dragTo(double mouseY, int trackY, int trackHeight) {
        int maxScroll = maxScroll();
        if (maxScroll == 0) {
            return;
        }
        int thumbHeight = thumbHeight(trackHeight);
        int travel = trackHeight - thumbHeight;
        if (travel <= 0) {
            return;
        }
        double clampedY = Math.max(trackY, Math.min(mouseY, trackY + trackHeight));
        double relative = Math.max(0, Math.min(clampedY - trackY - thumbHeight / 2.0, travel));
        scrollTo((int) Math.round((relative / travel) * maxScroll));
    }
}
