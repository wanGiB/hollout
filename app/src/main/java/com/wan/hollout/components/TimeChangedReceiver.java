package com.wan.hollout.components;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.wan.hollout.utils.FirebaseUtils;

/**
 * @author Wan Clem
 */

public class TimeChangedReceiver extends BroadcastReceiver {

    @SuppressLint("UnsafeProtectedBroadcastReceiver")
    @Override
    public void onReceive(Context context, Intent intent) {
        long currentTime = System.currentTimeMillis();
        FirebaseUtils.updateServerUptime(currentTime);
    }

}