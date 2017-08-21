package com.wan.hollout.eventbuses;

/**
 * @author Wan Clem
 */
public class UnreadFeedsBadge {

    private int unreadFeedsSize;

    public UnreadFeedsBadge(int unreadFeedsSize) {
        this.unreadFeedsSize = unreadFeedsSize;
    }

    public int getUnreadFeedsSize() {
        return unreadFeedsSize;
    }

}
