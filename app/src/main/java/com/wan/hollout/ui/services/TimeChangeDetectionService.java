package com.wan.hollout.ui.services;

import android.content.Intent;
import android.os.CountDownTimer;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.v4.app.JobIntentService;

import com.wan.hollout.utils.FirebaseUtils;

/**
 * @author Wan Clem
 */

public class TimeChangeDetectionService extends JobIntentService {

    private CountDownTimer countDownTimer;

    @Override
    protected void onHandleWork(@NonNull Intent intent) {
        FirebaseUtils.updateServerUptime(System.currentTimeMillis());
        startDummyTimer();
    }

    private void startDummyTimer() {
        Looper.prepare();
        countDownTimer = new CountDownTimer(60000, 1000) {

            @Override
            public void onTick(long millisUntilFinished) {

            }

            @Override
            public void onFinish() {
                FirebaseUtils.updateServerUptime(System.currentTimeMillis());
                countDownTimer.start();
            }

        };

        countDownTimer.start();
        Looper.loop();
    }

}
