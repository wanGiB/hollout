package com.wan.hollout.eventbuses;

import com.hyphenate.chat.EMMessage;

/**
 * @author Wan Clem
 */

public class ScrollToMessageEvent {

    private EMMessage emMessage;
    
    public ScrollToMessageEvent(EMMessage repliedMessage) {
        this.emMessage = repliedMessage;
    }

    public EMMessage getEmMessage() {
        return emMessage;
    }

}
