package com.wan.hollout.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;

import com.wan.hollout.components.ApplicationLoader;


/**
 * @author Wan Clem
 */
@SuppressLint("ApplySharedPref")
public class HolloutPreferences {

    public static SharedPreferences getHolloutPreferences() {
        return ApplicationLoader.getInstance().getSharedPreferences(AppConstants.HOLLOUT_PREFERENCES, Context.MODE_PRIVATE);
    }

    public static boolean canAccessLocation() {
        return getHolloutPreferences().getBoolean(AppConstants.CAN_ACCESS_LOCATION, false);
    }

    @SuppressLint("ApplySharedPref")
    public static void setCanAccessLocation(boolean value) {
        getHolloutPreferences().edit().putBoolean(AppConstants.CAN_ACCESS_LOCATION, value).commit();
    }

    public static String getAPreviousUploadFromPreference(String hashedPhotoPath) {
        SharedPreferences sharedPreferences = getHolloutPreferences();
        return sharedPreferences.getString(hashedPhotoPath, hashedPhotoPath);
    }

    public static void persistUploadedFile(String hashedPhotoPath, String returnedFileUrl) {
        SharedPreferences sharedPreferences = getHolloutPreferences();
        sharedPreferences.edit().putString(hashedPhotoPath, returnedFileUrl).apply();
    }

    public static void saveDocument(String documentUrl, String documentToString) {
        getHolloutPreferences().edit().putString(documentUrl, documentToString).commit();
    }

    public static String getDocumentString(String documentUrl) {
        return getHolloutPreferences().getString(documentUrl, null);
    }

    public static String getATEKey() {
        return getHolloutPreferences().getBoolean("dark_theme", false) ?
                "dark_theme" : "light_theme";
    }

    public static void setStartPageIndex(final int index) {
        getHolloutPreferences().edit().putInt(AppConstants.START_PAGE_INDEX, index).apply();
    }

    public static int getStartPageIndex() {
        return getHolloutPreferences().getInt(AppConstants.START_PAGE_INDEX, 0);
    }

    public static void saveCurrentPlaybackTime(String postId, int duration) {
        getHolloutPreferences().edit().putInt(postId + "_", duration).commit();
        HolloutLogger.d("SavedVideoPosition", duration + "");
    }

    public static int getLastPlaybackTime(String postId) {
        return getHolloutPreferences().getInt(postId + "_", 0);
    }

    public static void setUserWelcomed() {
        getHolloutPreferences().edit().putBoolean(AppConstants.USER_WELCOMED, true).commit();
    }

    public static boolean isUserWelcomed() {
        return getHolloutPreferences().getBoolean(AppConstants.USER_WELCOMED, false);
    }

    public static long getPostLikes(String globalPostId) {
        return getHolloutPreferences().getLong(globalPostId, 0);
    }

    public static void savePostLikes(String postId, int count) {
        getHolloutPreferences().edit().putLong(postId, count).commit();
    }

    public static boolean authenticated() {
        return getHolloutPreferences().getBoolean(AppConstants.AUTHENTICATED, false);
    }

    public static void setAuthenticated() {
        getHolloutPreferences().edit().putBoolean(AppConstants.AUTHENTICATED, true).commit();
    }

    public static String getAvailableUsername() {
        return getHolloutPreferences().getString(AppConstants.APP_USER_NAME, null);
    }

    public static String getAvailablePassword() {
        return getHolloutPreferences().getString(AppConstants.APP_USER_PASSWORD, null);
    }

    public static void persistCredentials(String username, String password) {
        SharedPreferences.Editor editor = getHolloutPreferences().edit();
        editor.putString(AppConstants.APP_USER_NAME, username);
        editor.putString(AppConstants.APP_USER_PASSWORD, password);
        editor.commit();
    }

    public static void clearPersistedCredentials() {
        SharedPreferences.Editor editor = getHolloutPreferences().edit();
        editor.putString(AppConstants.APP_USER_NAME, null);
        editor.putString(AppConstants.APP_USER_PASSWORD, null);
        editor.commit();
    }

}
