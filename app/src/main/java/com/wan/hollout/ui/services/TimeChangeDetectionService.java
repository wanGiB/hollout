package com.wan.hollout.ui.services;

import android.app.Service;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.wan.hollout.components.TimeChangedReceiver;

/**
 * @author Wan Clem
 */

public class TimeChangeDetectionService extends Service {

    private TimeChangedReceiver timeChangedReceiver;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (timeChangedReceiver != null) {
            unregisterReceiver(timeChangedReceiver);
            timeChangedReceiver = null;
        }
        timeChangedReceiver = new TimeChangedReceiver();
        IntentFilter timeFilters = new IntentFilter();
        timeFilters.addAction("android.intent.action.TIMEZONE_CHANGED");
        timeFilters.addAction("android.intent.action.TIME_SET");
        timeFilters.addAction("android.intent.action.BOOT_COMPLETED");
        timeFilters.addAction("android.intent.action.TIME_TICK");
        registerReceiver(timeChangedReceiver, timeFilters);
        return START_STICKY;
    }

}
