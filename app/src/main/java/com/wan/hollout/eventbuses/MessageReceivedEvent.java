package com.wan.hollout.eventbuses;

import com.hyphenate.chat.EMMessage;

/**
 * @author Wan Clem
 */

public class MessageReceivedEvent {

    private EMMessage message;

    public MessageReceivedEvent(EMMessage message) {
        this.message = message;
    }

    public EMMessage getMessage() {
        return message;
    }

}
