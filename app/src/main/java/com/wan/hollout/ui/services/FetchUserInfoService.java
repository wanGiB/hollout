package com.wan.hollout.ui.services;

import android.os.Bundle;
import android.support.annotation.Nullable;

import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.wan.hollout.models.ChatMessage;
import com.wan.hollout.models.PathEntity;
import com.wan.hollout.utils.AppConstants;
import com.wan.hollout.utils.ConversationsList;
import com.wan.hollout.utils.DbUtils;
import com.wan.hollout.utils.HolloutLogger;
import com.wan.hollout.utils.HolloutUtils;
import com.wan.hollout.utils.MessageNotifier;
import com.wan.hollout.utils.GeneralNotifier;

import org.apache.commons.lang3.StringUtils;

import java.util.List;

import static com.wan.hollout.utils.HolloutUtils.isAContact;

/**
 * @author Wan Clem
 */

public class FetchUserInfoService {

    public void onHandleWork(Bundle intentExtras) {
        if (intentExtras != null) {
            String userIdToFetch = intentExtras.getString(AppConstants.EXTRA_USER_ID);
            if (userIdToFetch != null) {
                HolloutLogger.d("UserIdTag", userIdToFetch);
                String notificationType = intentExtras.getString(AppConstants.NOTIFICATION_TYPE);
                String unreadMessageId = intentExtras.getString(AppConstants.UNREAD_MESSAGE_ID);
                ChatMessage chatMessage = null;
                if (unreadMessageId != null) {
                    chatMessage = DbUtils.getMessage(unreadMessageId);
                }
                List<ChatMessage> unreadMessagesFromSameSender = intentExtras.getParcelableArrayList(AppConstants.UNREAD_MESSAGES_FROM_SAME_SENDER);
                if (StringUtils.isNotEmpty(userIdToFetch)) {
                    fetchUserDetailsAndBlowNotification(userIdToFetch, notificationType, chatMessage, unreadMessagesFromSameSender);
                } else {
                    HolloutLogger.d("HolloutNotifTag", "Damn! the sender of the message was not found");
                }
            }
        }
    }

    private void fetchUserDetailsAndBlowNotification(final String idToFetch, final String notificationType, @Nullable final ChatMessage unreadMessage, @Nullable final List<ChatMessage> unreadMessagesFromSameSender) {
        final ParseQuery<ParseObject> userInfo = ParseQuery.getQuery(AppConstants.PEOPLE_GROUPS_AND_ROOMS);
        userInfo.whereEqualTo(AppConstants.REAL_OBJECT_ID, idToFetch.toLowerCase());
        userInfo.getFirstInBackground(new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject userObject, ParseException e) {
                if (e == null && userObject != null) {
                    if (notificationType.equals(AppConstants.NOTIFICATION_TYPE_AM_NEARBY)) {
                        String userLocation = HolloutUtils.resolveToBestLocation(userObject);
                        PathEntity pathEntity = DbUtils.getPathEntity(userLocation, userObject.getString(AppConstants.REAL_OBJECT_ID));
                        if (pathEntity == null) {
                            GeneralNotifier.displayKindIsNearbyNotification(userObject);
                            DbUtils.savePathEntity(userLocation, userObject.getString(AppConstants.REAL_OBJECT_ID));
                        }
                    } else if (notificationType.equals(AppConstants.NOTIFICATION_TYPE_NEW_MESSAGE) && unreadMessage != null) {
                        if (!isAContact(idToFetch.toLowerCase())) {
                            GeneralNotifier.displayChatRequestNotification(userObject);
                            return;
                        }
                        MessageNotifier.getInstance().sendSingleNotification(unreadMessage, userObject);
                        ConversationsList.checkAddToConversation(userObject);
                    } else if (notificationType.equals(AppConstants.NOTIFICATION_TYPE_NEW_MESSAGE) && unreadMessagesFromSameSender != null) {
                        if (!isAContact(idToFetch.toLowerCase())) {
                            GeneralNotifier.displayChatRequestNotification(userObject);
                            return;
                        }
                        MessageNotifier.getInstance().sendSameSenderNotification(unreadMessagesFromSameSender, userObject);
                        ConversationsList.checkAddToConversation(userObject);
                    }
                }
                userInfo.cancel();
            }
        });
    }
}
