package com.wan.hollout.eventbuses;

import com.wan.hollout.models.ChatMessage;

/**
 * @author Wan Clem
 */

public class MessageReceivedEvent {

    private ChatMessage message;

    public MessageReceivedEvent(ChatMessage message) {
        this.message = message;
    }

    public ChatMessage getMessage() {
        return message;
    }

}
