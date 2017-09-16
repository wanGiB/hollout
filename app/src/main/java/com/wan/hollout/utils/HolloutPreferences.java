package com.wan.hollout.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;

import com.wan.hollout.components.ApplicationLoader;

import java.util.HashSet;
import java.util.List;
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
    public static void setCanAccessLocation(boolean value) {
        getInstance().edit().putBoolean(AppConstants.CAN_ACCESS_LOCATION, value).commit();
    }

    public static String getAPreviousUploadFromPreference(String hashedPhotoPath) {
        SharedPreferences sharedPreferences = getInstance();
        return sharedPreferences.getString(hashedPhotoPath, hashedPhotoPath);
    }

    public static void persistUploadedFile(String hashedPhotoPath, String returnedFileUrl) {
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

    public static void saveCurrentPlaybackTime(String postId, int duration) {
        getInstance().edit().putInt(postId + "_", duration).commit();
        HolloutLogger.d("SavedVideoPosition", duration + "");
    }

    public static int getLastPlaybackTime(String postId) {
        return getInstance().getInt(postId + "_", 0);
    }

    public static void setUserWelcomed(boolean value) {
        getInstance().edit().putBoolean(AppConstants.USER_WELCOMED, value).commit();
    }

    public static boolean isUserWelcomed() {
        return getInstance().getBoolean(AppConstants.USER_WELCOMED, false);
    }

    public static long getPostLikes(String globalPostId) {
        return getInstance().getLong(globalPostId, 0);
    }

    public static void savePostLikes(String postId, int count) {
        getInstance().edit().putLong(postId, count).commit();
    }

    public static boolean authenticated() {
        return getInstance().getBoolean(AppConstants.AUTHENTICATED, false);
    }

    public static void setAuthenticated() {
        getInstance().edit().putBoolean(AppConstants.AUTHENTICATED, true).commit();
    }

    public static String getAvailableUsername() {
        return getInstance().getString(AppConstants.APP_USER_NAME, null);
    }

    public static String getAvailablePassword() {
        return getInstance().getString(AppConstants.APP_USER_PASSWORD, null);
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

    public static String getLanguage(String languagePref, String zz) {
        return getInstance().getString(languagePref, zz);
    }

    public static void defaultToSystemEmojis(String systemEmojiPref, boolean b) {
        getInstance().edit().putBoolean(systemEmojiPref, b).commit();
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
        return getInstance().getStringSet(AppConstants.TOTAL_UNREAD_CHATS, null);
    }

    public static void saveTotalUnreadChats(List<String> unreadMessages) {
        Set<String>unreadMessageSet=new HashSet<>();
        for (String s:unreadMessages){
            unreadMessageSet.add(s);
        }
        getInstance().edit().putStringSet(AppConstants.TOTAL_UNREAD_CHATS, unreadMessageSet).commit();
    }

    public static void saveTotalUnreadChats(Set<String>unreadMessageSet) {
        getInstance().edit().putStringSet(AppConstants.TOTAL_UNREAD_CHATS, unreadMessageSet).commit();
    }

    public static void clearUnreadMessagesCount() {
        getInstance().edit().putStringSet(AppConstants.TOTAL_UNREAD_CHATS, null).commit();
    }

    public static String getLastAttemptedMessage(String recipientId) {
        return getInstance().getString(AppConstants.LAST_ATTEMPTED_MESSAGE_FOR + recipientId, null);
    }

    public static void clearPreviousAttemptedMessage(String recipientId){
        getInstance().edit().putString(AppConstants.LAST_ATTEMPTED_MESSAGE_FOR + recipientId, null).apply();
    }

    public static void saveLastAttemptedMsg(String recipientId, String message) {
        getInstance().edit().putString(AppConstants.LAST_ATTEMPTED_MESSAGE_FOR + recipientId, message).apply();
    }

    public static Long getLastConversationTime(String recipient) {
        return getInstance().getLong(AppConstants.LAST_CONVERSATION_TIME_WITH +"_"+recipient,0);
    }

    public static void updateConversationTime(String recipient){
        getInstance().edit().putLong(AppConstants.LAST_CONVERSATION_TIME_WITH +"_"+recipient,System.currentTimeMillis()).commit();
    }

}
