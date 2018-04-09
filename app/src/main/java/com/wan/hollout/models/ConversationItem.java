package com.wan.hollout.models;

import android.support.annotation.NonNull;

import com.parse.ParseObject;
import com.wan.hollout.utils.AppConstants;
import com.wan.hollout.utils.DbUtils;
import com.wan.hollout.utils.HolloutPreferences;

/**
 * @author Wan Clem
 */

@SuppressWarnings("WeakerAccess")
public class ConversationItem implements Comparable<ConversationItem> {

    private ParseObject recipient;

    public ConversationItem(ParseObject recipient) {
        this.recipient = recipient;
    }

    public Long getLastUpdate() {
        return HolloutPreferences.getLastConversationTime(getObjectId());
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

    @Override
    public String toString() {
        String string = getRecipient().getString(AppConstants.APP_USER_DISPLAY_NAME);
        return string + "=" + getLastUpdate();
    }

}