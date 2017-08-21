package com.wan.hollout.ui.services;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.hyphenate.chat.EMMessage;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.wan.hollout.chat.MessageNotifier;
import com.wan.hollout.chat.NotificationUtils;
import com.wan.hollout.models.PathEntity;
import com.wan.hollout.utils.AppConstants;
import com.wan.hollout.utils.DbUtils;
import com.wan.hollout.utils.HolloutUtils;

import org.apache.commons.lang3.StringUtils;

/**
 * @author Wan Clem
 */

public class FetchUserInfoService extends IntentService {

    public FetchUserInfoService() {
        super("FetchUserInfoService");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        if (intent != null) {
            Bundle intentExtras = intent.getExtras();
            if (intentExtras != null) {
                String userIdToFetch = intentExtras.getString(AppConstants.EXTRA_USER_ID);
                String notificationType = intentExtras.getString(AppConstants.NOTIFICATION_TYPE);
                EMMessage unreadMessage = intentExtras.getParcelable(AppConstants.UNREAD_MESSAGE);
                if (StringUtils.isNotEmpty(userIdToFetch)) {
                    fetchUserDetails(userIdToFetch, notificationType,unreadMessage);
                }
            }
        }
    }

    private void fetchUserDetails(String idToFetch, final String notificationType, final EMMessage unreadMessage) {
        ParseQuery<ParseUser> userInfo = ParseUser.getQuery();
        userInfo.whereEqualTo(AppConstants.APP_USER_ID, idToFetch.toLowerCase());
        userInfo.getFirstInBackground(new GetCallback<ParseUser>() {
            @Override
            public void done(ParseUser userObject, ParseException e) {
                if (e == null && userObject != null) {
                    if (notificationType.equals(AppConstants.NOTIFICATION_TYPE_INDIVIDUAL_CHAT_REQUEST)) {
                        NotificationUtils.displayIndividualChatRequestNotification(userObject);
                    }else if (notificationType.equals(AppConstants.NOTIFICATION_TYPE_AM_NEARBY)){
                        String userLocation = HolloutUtils.resolveToBestLocation(userObject);
                        PathEntity pathEntity = DbUtils.getPathEntity(userLocation,userObject.getString(AppConstants.APP_USER_ID));
                        if (pathEntity==null){
                            NotificationUtils.displayKindIsNearbyNotification(userObject);
                            DbUtils.savePathEntity(userLocation,userObject.getString(AppConstants.APP_USER_ID));
                        }
                    }else if (notificationType.equals(AppConstants.NOTIFICATION_TYPE_NEW_MESSAGE)){
                        MessageNotifier.getInstance().init(FetchUserInfoService.this).sendSingleNotification(unreadMessage,userObject);
                    }
                }
            }
        });
    }

}
