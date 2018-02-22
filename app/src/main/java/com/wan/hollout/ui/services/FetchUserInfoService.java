package com.wan.hollout.ui.services;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.JobIntentService;

import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.wan.hollout.models.ChatMessage;
import com.wan.hollout.models.PathEntity;
import com.wan.hollout.utils.AppConstants;
import com.wan.hollout.utils.AuthUtil;
import com.wan.hollout.utils.DbUtils;
import com.wan.hollout.utils.HolloutLogger;
import com.wan.hollout.utils.HolloutUtils;
import com.wan.hollout.utils.MessageNotifier;
import com.wan.hollout.utils.NotificationUtils;

import org.apache.commons.lang3.StringUtils;

import java.util.List;

/**
 * @author Wan Clem
 */

public class FetchUserInfoService extends JobIntentService {

    @Override
    protected void onHandleWork(@NonNull Intent intent) {
        Bundle intentExtras = intent.getExtras();
        if (intentExtras != null) {
            String userIdToFetch = intentExtras.getString(AppConstants.EXTRA_USER_ID);
            if (userIdToFetch != null) {
                HolloutLogger.d("UserIdTag", userIdToFetch);
                String notificationType = intentExtras.getString(AppConstants.NOTIFICATION_TYPE);
                String unreadMessageId = intentExtras.getString(AppConstants.UNREAD_MESSAGE_ID);
                ChatMessage chatMessage = DbUtils.getMessage(unreadMessageId);
                if (chatMessage != null) {
                    List<ChatMessage> unreadMessagesFromSameSender = intentExtras.getParcelableArrayList(AppConstants.UNREAD_MESSAGES_FROM_SAME_SENDER);
                    if (StringUtils.isNotEmpty(userIdToFetch)) {
                        fetchUserDetails(userIdToFetch, notificationType, chatMessage, unreadMessagesFromSameSender);
                    }
                }
            }
        }
    }

    private void fetchUserDetails(final String idToFetch, final String notificationType, final ChatMessage unreadMessage, final List<ChatMessage> unreadMessagesFromSameSender) {
        ParseQuery<ParseObject> userInfo = ParseQuery.getQuery(AppConstants.PEOPLE_GROUPS_AND_ROOMS);
        userInfo.whereEqualTo(AppConstants.REAL_OBJECT_ID, idToFetch.toLowerCase());
        userInfo.getFirstInBackground(new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject userObject, ParseException e) {
                if (e == null && userObject != null) {
                    if (notificationType.equals(AppConstants.NOTIFICATION_TYPE_AM_NEARBY)) {
                        String userLocation = HolloutUtils.resolveToBestLocation(userObject);
                        PathEntity pathEntity = DbUtils.getPathEntity(userLocation, userObject.getString(AppConstants.REAL_OBJECT_ID));
                        if (pathEntity == null) {
                            NotificationUtils.displayKindIsNearbyNotification(userObject);
                            DbUtils.savePathEntity(userLocation, userObject.getString(AppConstants.REAL_OBJECT_ID));
                        }
                    } else if (notificationType.equals(AppConstants.NOTIFICATION_TYPE_NEW_MESSAGE) && unreadMessage != null) {
                        if (!isAContact(idToFetch.toLowerCase())) {
                            NotificationUtils.displayIndividualChatRequestNotification(userObject);
                            return;
                        }
                        MessageNotifier.getInstance().sendSingleNotification(unreadMessage, userObject);
                        AppConstants.recentConversations.add(0, userObject);
                    } else if (notificationType.equals(AppConstants.NOTIFICATION_TYPE_NEW_MESSAGE) && unreadMessagesFromSameSender != null) {
                        if (!isAContact(idToFetch.toLowerCase())) {
                            NotificationUtils.displayIndividualChatRequestNotification(userObject);
                            return;
                        }
                        MessageNotifier.getInstance().sendSameSenderNotification(unreadMessagesFromSameSender, userObject);
                        AppConstants.recentConversations.add(0, userObject);
                    }
                }
            }
        });
    }

    private boolean isAContact(String recipientId) {
        ParseObject signedInUser = AuthUtil.getCurrentUser();
        if (signedInUser != null) {
            List<String> signedInUserChats = signedInUser.getList(AppConstants.APP_USER_CHATS);
            return (signedInUserChats != null && signedInUserChats.contains(recipientId.toLowerCase()));
        }
        return false;
    }

}
