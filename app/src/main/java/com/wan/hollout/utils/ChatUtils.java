package com.wan.hollout.utils;

import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMConversation;
import com.hyphenate.exceptions.HyphenateException;
import com.wan.hollout.interfaces.DoneCallback;
import com.wan.hollout.managers.HolloutCommunicationsManager;

/**
 * @author Wan Clem
 */

@SuppressWarnings("unused")
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

    public static void acceptPrivateGroupInvite(final String groupId, final String inviter, final DoneCallback<Boolean> acceptedCallback) {
        HolloutCommunicationsManager.getInstance().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    EMClient.getInstance().groupManager().acceptInvitation(groupId, inviter);
                    acceptedCallback.done(true, null);
                } catch (HyphenateException e) {
                    acceptedCallback.done(null, e);
                }
            }
        });
    }

    public static void declinePrivateGroupInvite(final String groupId, final String inviter, final DoneCallback<Boolean> declineCallback) {
        HolloutCommunicationsManager.getInstance().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    EMClient.getInstance().groupManager().declineInvitation(groupId, inviter, "");
                    declineCallback.done(true, null);
                } catch (HyphenateException e) {
                    declineCallback.done(null, e);
                }
            }
        });
    }

    public static void acceptPublicGroupInvite(final String username, final String groupId, final DoneCallback<Boolean> acceptCallback) {
        HolloutCommunicationsManager.getInstance().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    EMClient.getInstance().groupManager().acceptApplication(username, groupId);
                    acceptCallback.done(true, null);
                } catch (HyphenateException e) {
                    acceptCallback.done(null, e);
                }
            }
        });
    }

    public static void declinePublicGroupInvite(final String username, final String groupId, final DoneCallback<Boolean> declineCallback) {
        HolloutCommunicationsManager.getInstance().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    EMClient.getInstance().groupManager().declineApplication(username, groupId, "");
                    declineCallback.done(true, null);
                } catch (HyphenateException e) {
                    declineCallback.done(false, e);
                }
            }
        });
    }

    public static void acceptChatInvitation(final String username, final DoneCallback<Boolean> acceptCallback) {
        HolloutCommunicationsManager.getInstance().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    EMClient.getInstance().contactManager().acceptInvitation(username);
                    acceptCallback.done(true, null);
                } catch (HyphenateException e) {
                    acceptCallback.done(false, e);
                }
            }
        });
    }

    public static void declineChatInvitation(final String username, final DoneCallback<Boolean> declineCallback) {
        HolloutCommunicationsManager.getInstance().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    EMClient.getInstance().contactManager().declineInvitation(username);
                    declineCallback.done(true, null);
                } catch (HyphenateException e) {
                    declineCallback.done(false, e);
                }
            }
        });
    }

}
