package com.wan.hollout.models;

import com.hyphenate.chat.EMConversation;
import com.parse.ParseObject;

/**
 * @author Wan Clem
 */
public class Chat {

    private ParseObject parseObject;
    private EMConversation emConversation;

    public Chat(ParseObject parseObject) {
        this.parseObject = parseObject;
    }

    public void setEmConversation(EMConversation emConversation) {
        this.emConversation = emConversation;
    }

    public EMConversation getEmConversation() {
        return emConversation;
    }

    public ParseObject getParseObject() {
        return parseObject;
    }

}
