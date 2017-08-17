package com.wan.hollout.chat;

import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMConversation;
import com.hyphenate.exceptions.HyphenateException;
import com.wan.hollout.callbacks.DoneCallback;
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

    public static void acceptPrivateGroupInvite(String groupId, String inviter, DoneCallback<Boolean> acceptedCallback) {
        try {
            EMClient.getInstance().groupManager().acceptInvitation(groupId, inviter);
            acceptedCallback.done(true, null);
        } catch (HyphenateException e) {
            acceptedCallback.done(null, e);
        }
    }

    public static void declinePrivateGroupInvite(String groupId, String inviter, DoneCallback<Boolean> declineCallback) {
        try {
            EMClient.getInstance().groupManager().declineInvitation(groupId, inviter, "");
            declineCallback.done(true, null);
        } catch (HyphenateException e) {
            declineCallback.done(null, e);
        }
    }

    public static void acceptPublicGroupInvite(String username, String groupId, DoneCallback<Boolean> acceptCallback) {
        try {
            EMClient.getInstance().groupManager().acceptApplication(username, groupId);
            acceptCallback.done(true, null);
        } catch (HyphenateException e) {
            acceptCallback.done(null, e);
        }
    }

    public static void declinePublicGroupInvite(String username, String groupId, DoneCallback<Boolean> declineCallback) {
        try {
            EMClient.getInstance().groupManager().declineApplication(username, groupId,"");
            declineCallback.done(true, null);
        } catch (HyphenateException e) {
            declineCallback.done(null, e);
        }
    }

    public static void acceptChatInvitation(String username, DoneCallback<Boolean> acceptCallback) {
        try {
            EMClient.getInstance().contactManager().acceptInvitation(username);
            acceptCallback.done(true, null);
        } catch (HyphenateException e) {
            acceptCallback.done(null, e);
        }
    }

    public static void declineChatInvitation(String username, DoneCallback<Boolean> declineCallback) {
        try {
            EMClient.getInstance().contactManager().declineInvitation(username);
            declineCallback.done(true, null);
        } catch (HyphenateException e) {
            declineCallback.done(null, e);
        }
    }

}
