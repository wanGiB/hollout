package com.wan.hollout.utils;

import com.parse.ParseObject;
import com.wan.hollout.models.ConversationItem;

import java.util.ArrayList;

/**
 * @author Wan Clem
 */

public class ConversationsList {

    private static ArrayList<ConversationItem> conversationItems = new ArrayList<>();

    public static ArrayList<ConversationItem> getConversationItems() {
        return conversationItems;
    }

    public static void checkAddToConversation(ParseObject parseObject) {
        final ConversationItem conversationItem = new ConversationItem(parseObject);
        if (!getConversationItems().contains(conversationItem)) {
            getConversationItems().add(0, conversationItem);
        }
    }

}
