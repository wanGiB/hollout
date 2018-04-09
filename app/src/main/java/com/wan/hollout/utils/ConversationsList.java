package com.wan.hollout.utils;

import com.parse.ParseObject;
import com.wan.hollout.models.ConversationItem;

import java.util.ArrayList;

/**
 * @author Wan Clem
 */

public class ConversationsList {

    private static ArrayList<ConversationItem> conversationItems = new ArrayList<>();

    public static ArrayList<ConversationItem> recentConversations() {
        return conversationItems;
    }

    public static void checkAddToConversation(ParseObject parseObject) {
        final ConversationItem conversationItem = new ConversationItem(parseObject);
        if (!recentConversations().contains(conversationItem)) {
            recentConversations().add(0, conversationItem);
        }
    }

}
