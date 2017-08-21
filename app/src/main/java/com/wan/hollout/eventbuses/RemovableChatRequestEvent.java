package com.wan.hollout.eventbuses;

import com.parse.ParseObject;

/**
 * @author Wan Clem
 */

public class RemovableChatRequestEvent {

    private ParseObject removableChatRequest;

    public RemovableChatRequestEvent(ParseObject removableChatRequest) {
        this.removableChatRequest = removableChatRequest;
    }

    public ParseObject getRemovableChatRequest() {
        return removableChatRequest;
    }

}
