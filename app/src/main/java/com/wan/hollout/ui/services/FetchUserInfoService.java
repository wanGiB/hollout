package com.wan.hollout.ui.services;

import android.app.IntentService;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.wan.hollout.chat.NotificationUtils;
import com.wan.hollout.utils.AppConstants;

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
                if (StringUtils.isNotEmpty(userIdToFetch)) {
                    fetchUserDetails(userIdToFetch, notificationType);
                }
            }
        }
    }

    private void fetchUserDetails(String idToFetch, final String notificationType) {
        ParseQuery<ParseUser> userInfo = ParseUser.getQuery();
        userInfo.whereEqualTo(AppConstants.APP_USER_ID, idToFetch.toLowerCase());
        userInfo.getFirstInBackground(new GetCallback<ParseUser>() {
            @Override
            public void done(ParseUser object, ParseException e) {
                if (e == null && object != null) {
                    if (notificationType.equals(AppConstants.NOTIFICATION_TYPE_INDIVIDUAL_CHAT_REQUEST)) {
                        NotificationUtils.displayIndividualChatRequestNotification(object);
                    }else if (notificationType.equals(AppConstants.NOTIFICATION_TYPE_AM_NEARBY)){
                        NotificationUtils.displayKindIsNearbyNotification(object);
                    }
                }
            }
        });
    }

}
