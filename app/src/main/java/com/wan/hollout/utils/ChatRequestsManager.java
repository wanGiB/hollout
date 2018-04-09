package com.wan.hollout.utils;

import com.wan.hollout.models.ChatMessage;

import java.util.HashMap;
import java.util.List;

/**
 * @author Wan Clem
 */

public class ChatRequestsManager {

    public static void addToPendingChatRequests(ChatMessage chatMessage, boolean canBlowNotification) {
        HashMap<String, Object> existingChatRequests = HolloutPreferences.getExistingChatRequests();

        String conversationId = chatMessage.getFrom();
        String conversationName = chatMessage.getFromName();
        String conversationPhotoUrl = chatMessage.getFromPhotoUrl();

        HashMap<String, Object> conversationProps = new HashMap<>();
        conversationProps.put(AppConstants.REQUESTER_NAME, conversationName);
        conversationProps.put(AppConstants.REQUESTER_ID, conversationId);
        conversationProps.put(AppConstants.REQUESTER_PHOTO_URL, conversationPhotoUrl);

        existingChatRequests.put(conversationId, conversationProps);
        HolloutPreferences.updateChatRequests(existingChatRequests);

        if (canBlowNotification) {
            GeneralNotifier.blowChatRequestsNotification();
        }
    }

    public static void removeIdFromRequestIds(String conversationId) {
        HashMap<String, Object> existingChatRequests = HolloutPreferences.getExistingChatRequests();
        if (existingChatRequests != null && !existingChatRequests.isEmpty()) {
            if (existingChatRequests.containsKey(conversationId)) {
                existingChatRequests.remove(conversationId);
                HolloutPreferences.updateChatRequests(existingChatRequests);
            }
        }
    }

    public static void initLegacyChatRequestsGrabber() {
        List<ChatMessage> pendingChatRequests = DbUtils.fetchPendingChatRequests();
        if (pendingChatRequests != null) {
            if (!pendingChatRequests.isEmpty()) {
                for (ChatMessage chatMessage : pendingChatRequests) {
                    addToPendingChatRequests(chatMessage, false);
                    DbUtils.deleteMessage(chatMessage);
                }
            }
        }
    }

}
