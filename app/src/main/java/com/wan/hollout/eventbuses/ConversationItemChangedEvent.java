package com.wan.hollout.eventbuses;

import com.parse.ParseObject;

/**
 * @author Wan Clem
 */

public class ConversationItemChangedEvent {

    private ParseObject parseObject;
    private boolean deleted;

    public ConversationItemChangedEvent(ParseObject parseObject, boolean deleted) {
        this.parseObject = parseObject;
        this.deleted = deleted;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public ParseObject getParseObject() {
        return parseObject;
    }

}
