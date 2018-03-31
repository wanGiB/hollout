package com.wan.hollout.eventbuses;

import com.parse.ParseObject;

/**
 * @author Wan Clem
 */

public class ChatRequestNegotiationResult {

    private ParseObject chatRequest;
    private int position;
    private boolean remove;

    public ChatRequestNegotiationResult(ParseObject chatRequest, boolean remove, int position) {
        this.chatRequest = chatRequest;
        this.remove = remove;
        this.position = position;
    }

    public boolean canRemove() {
        return remove;
    }

    public ParseObject getChatRequest() {
        return chatRequest;
    }

    public int getPosition() {
        return position;
    }

}
