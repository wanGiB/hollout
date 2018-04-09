package com.wan.hollout.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;

import com.google.gson.reflect.TypeToken;
import com.wan.hollout.components.ApplicationLoader;
import com.wan.hollout.eventbuses.ActivityCountChangedEvent;

import org.greenrobot.eventbus.EventBus;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import static com.wan.hollout.utils.AppConstants.ENTER_SENDS_PREF;
import static com.wan.hollout.utils.AppConstants.SYSTEM_EMOJI_PREF;

/**
 * @author Wan Clem
 */
@SuppressLint("ApplySharedPref")
public class HolloutPreferences {

    public static SharedPreferences getInstance() {
        return ApplicationLoader.getInstance().getSharedPreferences(AppConstants.HOLLOUT_PREFERENCES, Context.MODE_PRIVATE);
    }

    public static boolean canAccessLocation() {
        return getInstance().getBoolean(AppConstants.CAN_ACCESS_LOCATION, false);
    }

    @SuppressLint("ApplySharedPref")
    public static void setCanAccessLocation() {
        getInstance().edit().putBoolean(AppConstants.CAN_ACCESS_LOCATION, true).commit();
    }

    static String getAPreviousUploadFromPreference(String hashedPhotoPath) {
        SharedPreferences sharedPreferences = getInstance();
        return sharedPreferences.getString(hashedPhotoPath, hashedPhotoPath);
    }

    static void persistUploadedFile(String hashedPhotoPath, String returnedFileUrl) {
        SharedPreferences sharedPreferences = getInstance();
        sharedPreferences.edit().putString(hashedPhotoPath, returnedFileUrl).apply();
    }

    public static void saveDocument(String documentUrl, String documentToString) {
        getInstance().edit().putString(documentUrl, documentToString).commit();
    }

    public static String getDocumentString(String documentUrl) {
        return getInstance().getString(documentUrl, null);
    }

    public static String getATEKey() {
        return getInstance().getBoolean("dark_theme", false) ?
                "dark_theme" : "light_theme";
    }

    public static void setStartPageIndex(final int index) {
        getInstance().edit().putInt(AppConstants.START_PAGE_INDEX, index).apply();
    }

    public static int getStartPageIndex() {
        return getInstance().getInt(AppConstants.START_PAGE_INDEX, 0);
    }

    public static void setUserWelcomed(boolean value) {
        getInstance().edit().putBoolean(AppConstants.USER_WELCOMED, value).commit();
    }

    public static boolean isUserWelcomed() {
        return getInstance().getBoolean(AppConstants.USER_WELCOMED, false);
    }

    public static void persistCredentials(String username, String password) {
        SharedPreferences.Editor editor = getInstance().edit();
        editor.putString(AppConstants.APP_USER_NAME, username);
        editor.putString(AppConstants.APP_USER_PASSWORD, password);
        editor.commit();
    }

    public static void clearPersistedCredentials() {
        SharedPreferences.Editor editor = getInstance().edit();
        editor.putString(AppConstants.APP_USER_NAME, null);
        editor.putString(AppConstants.APP_USER_PASSWORD, null);
        editor.commit();
    }

    public static boolean isSystemEmojiPreferred() {
        return getInstance().getBoolean(SYSTEM_EMOJI_PREF, false);
    }

    static String getLanguage(String languagePref) {
        return getInstance().getString(languagePref, "zz");
    }

    public static void defaultToSystemEmojis(String systemEmojiPref) {
        getInstance().edit().putBoolean(systemEmojiPref, false).commit();
    }

    public static boolean isEnterSendsEnabled() {
        return getInstance().getBoolean(ENTER_SENDS_PREF, false);
    }

    public static void setLastFileCaption() {
        getInstance().edit().putString(AppConstants.LAST_FILE_CAPTION, null).clear().commit();
    }

    public static void setLastFileCaption(String fileCaption) {
        getInstance().edit().putString(AppConstants.LAST_FILE_CAPTION, fileCaption).clear().commit();
    }

    public static String getLastFileCaption() {
        return getInstance().getString(AppConstants.LAST_FILE_CAPTION, "Photo");
    }

    public static Set<String> getTotalUnreadChats() {
        return getInstance().getStringSet(AppConstants.TOTAL_UNREAD_CHATS, new HashSet<String>());
    }

    public static void saveTotalUnreadChats(Set<String> unreadMessageSet) {
        getInstance().edit().putStringSet(AppConstants.TOTAL_UNREAD_CHATS, unreadMessageSet).commit();
    }

    public static String getLastAttemptedMessage(String recipientId) {
        return getInstance().getString(AppConstants.LAST_ATTEMPTED_MESSAGE_FOR + recipientId, null);
    }

    public static void clearPreviousAttemptedMessage(String recipientId) {
        getInstance().edit().putString(AppConstants.LAST_ATTEMPTED_MESSAGE_FOR + recipientId, null).apply();
    }

    public static void saveLastAttemptedMsg(String recipientId, String message) {
        getInstance().edit().putString(AppConstants.LAST_ATTEMPTED_MESSAGE_FOR + recipientId, message).apply();
    }

    public static Long getLastConversationTime(String recipient) {
        return getInstance().getLong(AppConstants.LAST_CONVERSATION_TIME_WITH + "_" + recipient, 0);
    }

    public static void updateConversationTime(String recipient) {
        getInstance().edit().putLong(AppConstants.LAST_CONVERSATION_TIME_WITH + "_" + recipient, System.currentTimeMillis()).commit();
    }

    public static void updateConversationTime(String recipient, long time) {
        getInstance().edit().putLong(AppConstants.LAST_CONVERSATION_TIME_WITH + "_" + recipient, time).commit();
    }

    public static int getViewWidth(String messageId) {
        return getInstance().getInt(messageId, 0);
    }

    public static void saveViewWidth(String messageId, int width) {
        getInstance().edit().putInt(messageId, width).commit();
    }

    public static void incrementUnreadMessagesFrom(String from) {
        int currentUnreadMsgsCountFromSender = getInstance().getInt(AppConstants.UNREAD_MESSAGES_COUNT_FROM + from, 0);
        getInstance().edit().putInt(AppConstants.UNREAD_MESSAGES_COUNT_FROM + from, currentUnreadMsgsCountFromSender + 1).commit();
    }

    public static int getUnreadMessagesCountFrom(String from) {
        return getInstance().getInt(AppConstants.UNREAD_MESSAGES_COUNT_FROM + from, 0);
    }

    public static void clearUnreadMessagesCountFrom(String from) {
        getInstance().edit().putInt(AppConstants.UNREAD_MESSAGES_COUNT_FROM + from, 0).commit();
    }

    static int getTotalUnreadMessagesCount() {
        return getInstance().getInt(AppConstants.TOTAL_UNREAD_MESSAGES_COUNT, 0);
    }

    public static void setTotalUnreadMessagesCount(int size) {
        getInstance().edit().putInt(AppConstants.TOTAL_UNREAD_MESSAGES_COUNT, size).commit();
    }

    public static void saveUserFirebaseToken(String token) {
        getInstance().edit().putString(AppConstants.USER_FIREBASE_TOKEN, token).commit();
    }

    public static String getUserFirebaseToken() {
        return getInstance().getString(AppConstants.USER_FIREBASE_TOKEN, null);
    }

    public static void incrementActivityCount() {
        int currentActivityCount = getInstance().getInt(AppConstants.ACTIVITY_COUNT, 0);
        getInstance().edit().putInt(AppConstants.ACTIVITY_COUNT, currentActivityCount + 1).commit();
        EventBus.getDefault().post(new ActivityCountChangedEvent());
    }

    public static void decrementActivityCount() {
        int currentActivityCount = getInstance().getInt(AppConstants.ACTIVITY_COUNT, 0);
        if (currentActivityCount != 0) {
            getInstance().edit().putInt(AppConstants.ACTIVITY_COUNT, currentActivityCount - 1).commit();
        }
        EventBus.getDefault().post(new ActivityCountChangedEvent());
    }

    public static void destroyActivityCount() {
        getInstance().edit().putInt(AppConstants.ACTIVITY_COUNT, 0).commit();
        EventBus.getDefault().post(new ActivityCountChangedEvent());
    }

    public static long getLastVibrateTime() {
        return getInstance().getLong(AppConstants.LAST_VIBRATE_TIME, 0);
    }

    public static void setLastVibrateTime(long lastVibrateTime) {
        getInstance().edit().putLong(AppConstants.LAST_VIBRATE_TIME, lastVibrateTime).commit();
    }

    public static HashMap<String, Object> getExistingChatRequests() {
        String existingChatRequestsString = getInstance().getString(AppConstants.EXISTING_CHAT_REQUESTS_STRING, null);
        if (existingChatRequestsString != null) {
            Type mapType = getMapType();
            HashMap<String, Object> chatRequestsMap = JsonUtils.getGSon().fromJson(existingChatRequestsString, mapType);
            if (chatRequestsMap != null) {
                return chatRequestsMap;
            }
        }
        return new HashMap<>();
    }

    public static void updateChatRequests(HashMap<String, Object> chatRequestsMap) {
        Type mapType = getMapType();
        String chatString = JsonUtils.getGSon().toJson(chatRequestsMap, mapType);
        getInstance().edit().putString(AppConstants.EXISTING_CHAT_REQUESTS_STRING, chatString).commit();
    }

    private static Type getMapType() {
        return new TypeToken<HashMap<String, Object>>() {
        }.getType();
    }

}
