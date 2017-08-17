package com.wan.hollout.eventbuses;

import com.hyphenate.chat.EMMessage;

/**
 * @author Wan Clem
 */

public class MessageDeliveredEvent {

    private EMMessage message;

    public MessageDeliveredEvent(EMMessage message) {
        this.message = message;
    }

    public EMMessage getMessage() {
        return message;
    }

}
