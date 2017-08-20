package com.wan.hollout.receivers;

import android.content.Context;
import android.content.Intent;
import com.parse.ParsePushBroadcastReceiver;
import com.parse.ParseUser;
import com.wan.hollout.ui.services.FetchUserInfoService;
import com.wan.hollout.utils.AppConstants;
import com.wan.hollout.utils.HolloutLogger;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author Wan Clem
 */

public class NotificationsReceiver extends ParsePushBroadcastReceiver {

    private static final String TAG = "com.wan.hollout.NotificationsReceiver";

    @Override
    protected void onPushReceive(Context context, Intent intent) {
        String pushDataStr = intent.getStringExtra(KEY_PUSH_DATA);
        if (pushDataStr == null) {
            HolloutLogger.e(TAG, "Can not get push data from intent.");
            return;
        }
        HolloutLogger.d(TAG, "Received push data: " + pushDataStr);
        JSONObject pushData = null;
        try {
            pushData = new JSONObject(pushDataStr);
        } catch (JSONException e) {
            HolloutLogger.e(TAG, "Unexpected JSONException when receiving push data: " + e.getMessage());
        }
        if (pushData != null) {
            String pushType = pushData.optString(AppConstants.NOTIFICATION_TYPE);
            if (pushType.equals(AppConstants.NOTIFICATION_TYPE_INDIVIDUAL_CHAT_REQUEST)
                    || pushType.equals(AppConstants.NOTIFICATION_TYPE_AM_NEARBY)) {
                ParseUser signedInUser = ParseUser.getCurrentUser();
                if (signedInUser != null && !signedInUser.getString(AppConstants.APP_USER_ONLINE_STATUS).equals(AppConstants.ONLINE)) {
                    String senderId = pushData.optString(AppConstants.SENDER_ID);
                    Intent fetchUserInfoIntent = new Intent(context, FetchUserInfoService.class);
                    fetchUserInfoIntent.putExtra(AppConstants.EXTRA_USER_ID, senderId);
                    fetchUserInfoIntent.putExtra(AppConstants.NOTIFICATION_TYPE, pushType);
                    context.startService(fetchUserInfoIntent);
                }
            }
        }

    }

}
