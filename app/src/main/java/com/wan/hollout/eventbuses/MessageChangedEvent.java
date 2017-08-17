package com.wan.hollout.eventbuses;

import com.hyphenate.chat.EMMessage;

/**
 * @author Wan Clem
 */

public class MessageChangedEvent {

    private EMMessage message;

    public MessageChangedEvent(EMMessage message) {
        this.message = message;
    }

    public EMMessage getMessage() {
        return message;
    }

}
