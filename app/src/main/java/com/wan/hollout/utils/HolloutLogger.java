
package com.wan.hollout.utils;

import android.util.Log;

import com.wan.hollout.BuildConfig;


/**
 * @author Wan Clem
 ***/
public class HolloutLogger {

    public static void d(String tag, String message) {
        if (BuildConfig.DEBUG) {
            Log.d(tag, message);
        }
    }

    public static void e(String tag, String message) {
        if (BuildConfig.DEBUG) {
            Log.e(tag, message);
        }
    }
    public static void e(String tag, Exception message) {
        if (BuildConfig.DEBUG) {
            Log.e(tag, message.getMessage());
        }
    }
    public static void i(String tag, String message) {
        if (BuildConfig.DEBUG) {
            Log.e(tag, message);
        }
    }

    public static void w(String tag, String message) {
        if (BuildConfig.DEBUG) {
            Log.e(tag, message);
        }
    }
}
