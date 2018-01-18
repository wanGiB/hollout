package com.wan.hollout.utils;

import com.parse.ParseInstallation;
import com.parse.ParsePush;
import com.parse.ParseQuery;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author Wan Clem
 */

public class NotificationCenter {

    public static void sendAmNearbyNotification(String senderId,ParseQuery<ParseInstallation>parseInstallationParseQuery){
        ParsePush newParsePush = new ParsePush();
        newParsePush.setQuery(parseInstallationParseQuery);
        JSONObject notificationObject = new JSONObject();
        try {
            notificationObject.put(AppConstants.SENDER_ID,senderId.toLowerCase());
            notificationObject.put(AppConstants.NOTIFICATION_TYPE,AppConstants.NOTIFICATION_TYPE_AM_NEARBY);
            newParsePush.setData(notificationObject);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        newParsePush.sendInBackground();
    }

}
