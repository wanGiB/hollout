package com.wan.hollout.ui.widgets.validation;

import java.io.Serializable;

public class BadgeItem implements Serializable {

    private static final int BADGE_TEXT_MAX_NUMBER = 9;

    private int badgeIndex;

    private long badgeText;

    private int badgeColor;

    public BadgeItem(int badgeIndex, long badgeText, int badgeColor) {
        this.badgeIndex = badgeIndex;
        this.badgeText = badgeText;
        this.badgeColor = badgeColor;
    }

    int getBadgeIndex() {
        return badgeIndex;
    }

    public int getBadgeColor() {
        return badgeColor;
    }

    public long getIntBadgeText() {
        return badgeText;
    }

    String getFullBadgeText() {
        return String.valueOf(badgeText);
    }

    String getBadgeText() {
        String badgeStringText;
        if (badgeText > BADGE_TEXT_MAX_NUMBER) {
            badgeStringText = BADGE_TEXT_MAX_NUMBER + "+";
        } else {
            badgeStringText = String.valueOf(badgeText);
        }

        return badgeStringText;
    }
}
