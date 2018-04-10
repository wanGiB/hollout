package com.wan.hollout.utils;

import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.wan.hollout.interfaces.DoneCallback;
import com.wan.hollout.models.ChatMessage;

import java.util.List;

/**
 * @author Wan Clem
 */

public class ChatRequestsManager {

    public static void initLegacyChatRequestsGrabber() {
        List<ChatMessage> pendingChatRequests = DbUtils.fetchPendingChatRequests();
        if (pendingChatRequests != null) {
            if (!pendingChatRequests.isEmpty()) {
                for (ChatMessage chatMessage : pendingChatRequests) {
                    DbUtils.deleteMessage(chatMessage);
                }
            }
        }
    }

    public static void fetchChatRequests(final DoneCallback<List<ParseObject>> doneCallback) {
        ParseObject signedInUser = AuthUtil.getCurrentUser();
        if (signedInUser != null) {
            final ParseQuery<ParseObject> chatRequestsQuery = ParseQuery.getQuery(AppConstants.HOLLOUT_FEED);
            chatRequestsQuery.whereEqualTo(AppConstants.FEED_TYPE, AppConstants.FEED_TYPE_CHAT_REQUEST);
            chatRequestsQuery.include(AppConstants.FEED_CREATOR);
            chatRequestsQuery.whereEqualTo(AppConstants.FEED_RECIPIENT_ID,
                    signedInUser.getString(AppConstants.REAL_OBJECT_ID).toLowerCase());
            chatRequestsQuery.findInBackground(new FindCallback<ParseObject>() {
                @Override
                public void done(List<ParseObject> objects, ParseException e) {
                    if (e == null && objects != null && !objects.isEmpty()) {
                        doneCallback.done(objects, null);
                    } else {
                        doneCallback.done(null, e);
                    }
                    chatRequestsQuery.cancel();
                }
            });
        }
    }

}
