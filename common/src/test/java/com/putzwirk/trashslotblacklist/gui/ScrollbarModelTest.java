package com.putzwirk.trashslotblacklist.gui;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ScrollbarModelTest {

    private ScrollbarModel model;

    @BeforeEach
    void setUp() {
        model = new ScrollbarModel();
        model.update(1, 1);
    }

    @Nested
    @DisplayName("update")
    class Update {

        @Test
        void negativeTotalRowsClampedToOne() {
            model.update(-5, 3);
            assertEquals(1, model.visibleRowCount());
            assertEquals(0, model.maxScroll());
        }

        @Test
        void zeroVisibleRowsClampedToOne() {
            model.update(10, 0);
            assertEquals(1, model.visibleRowCount());
        }

        @Test
        void negativeVisibleRowsClampedToOne() {
            model.update(10, -2);
            assertEquals(1, model.visibleRowCount());
        }

        @Test
        void rowsFitNoScroll() {
            model.update(5, 5);
            assertEquals(5, model.visibleRowCount());
            assertEquals(0, model.maxScroll());
        }

        @Test
        void rowsOverflowHasScroll() {
            model.update(10, 4);
            assertEquals(4, model.visibleRowCount());
            assertEquals(6, model.maxScroll());
        }

        @Test
        void updateClampsExistingScroll() {
            model.update(20, 5);
            model.scrollTo(15);
            model.update(6, 5);
            assertEquals(1, model.getScroll());
        }
    }

    @Nested
    @DisplayName("scrolling")
    class Scrolling {

        @BeforeEach
        void overflow() {
            model.update(10, 3);
        }

        @Test
        void scrollByPositiveMovesDown() {
            assertTrue(model.scrollBy(1));
            assertEquals(1, model.getScroll());
        }

        @Test
        void scrollByNegativeMovesUp() {
            model.scrollBy(2);
            assertTrue(model.scrollBy(-1));
            assertEquals(1, model.getScroll());
        }

        @Test
        void scrollByClampsAtMax() {
            assertTrue(model.scrollBy(999));
            assertEquals(7, model.getScroll());
            assertFalse(model.scrollBy(1));
            assertEquals(7, model.getScroll());
        }

        @Test
        void scrollByClampsAtZero() {
            assertFalse(model.scrollBy(-1));
            assertEquals(0, model.getScroll());
        }

        @Test
        void scrollToClampsBelowZero() {
            model.scrollBy(2);
            assertTrue(model.scrollTo(-5));
            assertEquals(0, model.getScroll());
        }

        @Test
        void scrollToSameValueReturnsFalse() {
            assertFalse(model.scrollTo(0));
        }

        @Test
        void maxScrollZeroNoMovement() {
            model.update(3, 3);
            assertFalse(model.scrollBy(1));
            assertFalse(model.scrollBy(-1));
            assertFalse(model.scrollTo(5));
            assertEquals(0, model.getScroll());
        }
    }

    @Nested
    @DisplayName("thumb geometry")
    class ThumbGeometry {

        @Test
        void noScrollReturnsFullTrack() {
            model.update(3, 3);
            assertEquals(100, model.thumbHeight(100));
            assertEquals(10, model.thumbY(10, 100));
        }

        @Test
        void thumbHeightIsProportional() {
            model.update(10, 5);
            assertEquals(50, model.thumbHeight(100));
        }

        @Test
        void thumbHeightHasMinimum() {
            model.update(1000, 1);
            assertEquals(ScrollbarModel.MIN_THUMB_SIZE, model.thumbHeight(18));
        }

        @Test
        void negativeTrackHeightClamped() {
            model.update(3, 3);
            assertEquals(0, model.thumbHeight(-10));
        }

        @Test
        void trackTravelAtLeastOne() {
            model.update(1000, 1);
            assertTrue(model.trackTravel(10) >= 1);
        }

        @Test
        void thumbYAtTop() {
            model.update(10, 5);
            assertEquals(0, model.thumbY(0, 100));
        }

        @Test
        void thumbYAtBottom() {
            model.update(10, 5);
            model.scrollTo(5);
            assertEquals(50, model.thumbY(0, 100));
        }

        @Test
        void thumbYAtMiddle() {
            model.update(11, 5);
            model.scrollTo(3);
            int thumbY = model.thumbY(0, 100);
            assertTrue(thumbY > 0 && thumbY < 50);
        }
    }

    @Nested
    @DisplayName("track clicks")
    class TrackClicks {

        @BeforeEach
        void overflow() {
            model.update(20, 10);
            model.scrollTo(0);
        }

        @Test
        void clickAboveThumbScrollsUp() {
            int thumbY = model.thumbY(0, 180);
            assertEquals(ScrollbarModel.TrackClick.UP, model.trackClick(thumbY - 1, 0, 180));
        }

        @Test
        void clickOnThumbDrags() {
            int thumbY = model.thumbY(0, 180);
            assertEquals(ScrollbarModel.TrackClick.THUMB, model.trackClick(thumbY + 1, 0, 180));
        }

        @Test
        void clickBelowThumbScrollsDown() {
            int thumbY = model.thumbY(0, 180);
            int thumbHeight = model.thumbHeight(180);
            assertEquals(ScrollbarModel.TrackClick.DOWN, model.trackClick(thumbY + thumbHeight + 1, 0, 180));
        }

        @Test
        void clickJustAboveThumbBoundaryIsThumb() {
            int thumbY = model.thumbY(0, 180);
            assertEquals(ScrollbarModel.TrackClick.THUMB, model.trackClick(thumbY, 0, 180));
        }
    }

    @Nested
    @DisplayName("drag")
    class Drag {

        @BeforeEach
        void overflow() {
            model.update(20, 4);
        }

        @Test
        void dragAboveTrackScrollsToZero() {
            model.scrollTo(16);
            model.dragTo(-100, 0, 72);
            assertEquals(0, model.getScroll());
        }

        @Test
        void dragBelowTrackScrollsToMax() {
            model.dragTo(9999, 0, 72);
            assertEquals(16, model.getScroll());
        }

        @Test
        void dragToMiddleScrollsToHalf() {
            model.dragTo(36, 0, 72);
            assertTrue(model.getScroll() > 0 && model.getScroll() < 16);
        }

        @Test
        void dragIgnoredWhenNoOverflow() {
            model.update(4, 4);
            model.dragTo(999, 0, 72);
            assertEquals(0, model.getScroll());
        }

        @Test
        void dragWithThumbAtMinimumStillMoves() {
            model.update(200, 1);
            model.dragTo(17, 0, 18);
            assertTrue(model.getScroll() >= 0 && model.getScroll() <= model.maxScroll());
        }
    }
}
