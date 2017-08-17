package com.wan.hollout.eventbuses;

import com.hyphenate.chat.EMMessage;

/**
 * @author Wan Clem
 */

public class MessageReadEvent {

    private EMMessage message;

    public MessageReadEvent(EMMessage message) {
        this.message = message;
    }

    public EMMessage getMessage() {
        return message;
    }

}
