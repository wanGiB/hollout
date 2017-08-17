package com.wan.hollout.chat;

import com.hyphenate.chat.EMConversation;
import com.wan.hollout.utils.AppConstants;

/**
 * @author Wan Clem
 */

public class ChatUtils {
    /**
     * change the chat type to EMConversationType
     */
    public static EMConversation.EMConversationType getConversationType(int chatType) {
        if (chatType == AppConstants.CHAT_TYPE_SINGLE) {
            return EMConversation.EMConversationType.Chat;
        } else if (chatType == AppConstants.CHAT_TYPE_GROUP) {
            return EMConversation.EMConversationType.GroupChat;
        } else {
            return EMConversation.EMConversationType.ChatRoom;
        }
    }
}
