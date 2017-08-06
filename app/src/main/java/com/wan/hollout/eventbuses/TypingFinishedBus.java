package com.wan.hollout.eventbuses;

/**
 * @author Wan Clem
 */

public class TypingFinishedBus {

    private boolean typingFinished;

    public TypingFinishedBus(boolean typingFinished) {
        this.typingFinished = typingFinished;
    }

    public boolean isTypingFinished() {
        return typingFinished;
    }

}
