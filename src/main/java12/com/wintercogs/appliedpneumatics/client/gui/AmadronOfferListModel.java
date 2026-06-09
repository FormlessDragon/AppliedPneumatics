package com.wintercogs.appliedpneumatics.client.gui;

final class AmadronOfferListModel {
    static final int OFFER_LIST_LEFT = 8;
    static final int OFFER_LIST_TOP = 20;
    static final int OFFER_LIST_WIDTH = 160;
    static final int OFFER_ROW_HEIGHT = 10;
    static final int MAX_VISIBLE_OFFERS = 6;
    static final int UNITS_LEFT = 8;
    static final int UNITS_TOP = 84;
    static final int UNITS_WIDTH = 130;
    static final int UNITS_HEIGHT = 10;

    private int offerScrollOffset;

    int getOfferScrollOffset() {
        return this.offerScrollOffset;
    }

    int offerIndexAt(int localX, int localY, int offerCount) {
        if (!isOverOfferList(localX, localY)) {
            return -1;
        }
        int offerIndex = this.offerScrollOffset + (localY - OFFER_LIST_TOP) / OFFER_ROW_HEIGHT;
        return offerIndex >= 0 && offerIndex < offerCount ? offerIndex : -1;
    }

    boolean isOverOfferList(int localX, int localY) {
        return localX >= OFFER_LIST_LEFT && localX < OFFER_LIST_LEFT + OFFER_LIST_WIDTH
            && localY >= OFFER_LIST_TOP && localY < OFFER_LIST_TOP + MAX_VISIBLE_OFFERS * OFFER_ROW_HEIGHT;
    }

    boolean isOverUnits(int localX, int localY) {
        return localX >= UNITS_LEFT && localX < UNITS_LEFT + UNITS_WIDTH
            && localY >= UNITS_TOP && localY < UNITS_TOP + UNITS_HEIGHT;
    }

    boolean scrollOffers(int offerCount, int wheelDelta, boolean pageScroll) {
        int previousOffset = this.offerScrollOffset;
        int step = pageScroll ? MAX_VISIBLE_OFFERS : 1;
        this.offerScrollOffset += wheelDelta > 0 ? -step : step;
        clampOfferScrollOffset(offerCount);
        return this.offerScrollOffset != previousOffset;
    }

    void resetScroll() {
        this.offerScrollOffset = 0;
    }

    void clampOfferScrollOffset(int offerCount) {
        int maxOffset = Math.max(0, offerCount - MAX_VISIBLE_OFFERS);
        if (this.offerScrollOffset < 0) {
            this.offerScrollOffset = 0;
        } else if (this.offerScrollOffset > maxOffset) {
            this.offerScrollOffset = maxOffset;
        }
    }
}
