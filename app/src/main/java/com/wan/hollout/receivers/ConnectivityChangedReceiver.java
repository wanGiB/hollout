package com.wan.hollout.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v4.app.JobIntentService;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.parse.ParseObject;
import com.wan.hollout.clients.CallClient;
import com.wan.hollout.clients.ChatClient;
import com.wan.hollout.ui.services.AppInstanceDetectionService;
import com.wan.hollout.utils.AppConstants;
import com.wan.hollout.utils.AuthUtil;
import com.wan.hollout.utils.FirebaseUtils;
import com.wan.hollout.utils.HolloutLogger;
import com.wan.hollout.utils.HolloutPreferences;

/**
 * @author Wan Clem
 */

@SuppressWarnings("StatementWithEmptyBody")
public class ConnectivityChangedReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        startAppDetectionService(context);
    }

    private void startAppDetectionService(Context context) {
        try {
            if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                ParseObject signedInUserObject = AuthUtil.getCurrentUser();
                if (signedInUserObject != null && HolloutPreferences.canAccessLocation()) {
                    Intent serviceIntent = new Intent();
                    JobIntentService.enqueueWork(context, AppInstanceDetectionService.class, AppConstants.FIXED_JOB_ID, serviceIntent);
                }
                ChatClient.getInstance().startChatClient();
                CallClient.getInstance().startCallClient();
                fetchNewConfigData();
            }
        } catch (IllegalArgumentException | IllegalStateException | NullPointerException ignored) {

        }
    }

    public void fetchNewConfigData() {
        long cacheExpiration = 3600; // 1 hour in seconds.
        // If your app is using developer mode, cacheExpiration is set to 0, so each fetch will
        // retrieve values from the service.
        if (FirebaseUtils.getRemoteConfig().getInfo().getConfigSettings().isDeveloperModeEnabled()) {
            cacheExpiration = 0;
        }
        FirebaseUtils.getRemoteConfig().fetch(cacheExpiration).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    // After config data is successfully fetched, it must be activated before newly fetched
                    // values are returned.
                    FirebaseUtils.getRemoteConfig().activateFetched();

                } else {
                    HolloutLogger.d("FirebaseRemoteConfig", "Failed to fetch remote config data");
                }
            }
        });
    }

}
