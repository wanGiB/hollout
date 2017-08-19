package com.wan.hollout.utils;

import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.ParsePush;
import com.parse.ParseQuery;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * @author Wan Clem
 */

public class NotificationCenter {

    public static void sendChatRequestNotification(final String from, final String to) {
        final ParseQuery<ParseInstallation> parseInstallationParseQuery = ParseInstallation.getQuery();
        parseInstallationParseQuery.whereEqualTo(AppConstants.APP_USER_ID, to.toLowerCase());
        parseInstallationParseQuery.getFirstInBackground(new GetCallback<ParseInstallation>() {
            @Override
            public void done(ParseInstallation object, ParseException e) {
                if (object != null) {
                    ParsePush newParsePush = new ParsePush();
                    newParsePush.setQuery(parseInstallationParseQuery);
                    newParsePush.setData(prepareChatRequestNotification(from, to));
                    newParsePush.sendInBackground();
                }
            }
        });
    }

    private static JSONObject prepareChatRequestNotification(String from, String to) {
        JSONObject chatRequestObject = new JSONObject();
        try {
            chatRequestObject.put(AppConstants.SENDER_ID, from);
            chatRequestObject.put(AppConstants.NOTIFICATION_TYPE,AppConstants.NOTIFICATION_TYPE_INDIVIDUAL_CHAT_REQUEST);
            chatRequestObject.put(AppConstants.RECIPIENT, to);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return chatRequestObject;
    }

}
