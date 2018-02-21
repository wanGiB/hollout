package com.wan.hollout.eventbuses;

import com.wan.hollout.models.ChatMessage;

/**
 * @author Wan Clem
 */

public class ScrollToMessageEvent {

    private ChatMessage emMessage;
    
    public ScrollToMessageEvent(ChatMessage repliedMessage) {
        this.emMessage = repliedMessage;
    }

    public ChatMessage getEmMessage() {
        return emMessage;
    }

}
