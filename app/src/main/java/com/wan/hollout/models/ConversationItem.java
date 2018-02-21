package com.wan.hollout.models;

import android.support.annotation.NonNull;

import com.parse.ParseObject;
import com.wan.hollout.utils.AppConstants;

/**
 * @author Wan Clem
 */

@SuppressWarnings("WeakerAccess")
public class ConversationItem implements Comparable<ConversationItem> {

    private ParseObject recipient;
    private Long lastUpdate;

    public ConversationItem(ParseObject recipient, Long lastUpdate) {
        this.recipient = recipient;
        this.lastUpdate = lastUpdate;
    }

    public Long getLastUpdate() {
        return lastUpdate;
    }

    public ParseObject getRecipient() {
        return recipient;
    }

    public String getObjectId() {
        return recipient.getString(AppConstants.REAL_OBJECT_ID);
    }

    @Override
    public int hashCode() {
        int result;
        result = getObjectId().hashCode();
        final String name = getClass().getName();
        result = 31 * result + (name != null ? name.hashCode() : 0);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (obj.getClass() != getClass()) {
            return false;
        }
        ConversationItem another = (ConversationItem) obj;
        return getObjectId().equals(another.getObjectId());
    }

    @Override
    public int compareTo(@NonNull ConversationItem another) {
        return another.getLastUpdate().compareTo(getLastUpdate());
    }

}