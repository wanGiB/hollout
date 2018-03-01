package com.wan.hollout.ui.services;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.app.JobIntentService;

import com.sinch.android.rtc.SinchClient;

/**
 * @author Wan Clem
 */

public class CallService extends JobIntentService {

    private SinchClient mCallClient;

    @Override
    protected void onHandleWork(@NonNull Intent intent) {

    }

}
