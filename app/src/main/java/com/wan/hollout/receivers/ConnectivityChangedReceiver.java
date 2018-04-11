package com.wan.hollout.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.app.JobIntentService;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.parse.ParseObject;
import com.wan.hollout.clients.CallClient;
import com.wan.hollout.clients.ChatClient;
import com.wan.hollout.ui.services.AppInstanceDetectionService;
import com.wan.hollout.utils.AppConstants;
import com.wan.hollout.utils.AuthUtil;
import com.wan.hollout.utils.FirebaseUtils;
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

                FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
                if (firebaseUser != null) {
                    String email = firebaseUser.getEmail();
                    if (email != null) {
                        if (email.equals("holloutdev@gmail.com") || email.equals("wannclem@gmail.com") || email.equals("wanaclem@gmail.com")) {
                            long currentTime = System.currentTimeMillis();
                            FirebaseUtils.updateServerUptime(currentTime);
                        }
                    }
                }

            }
        } catch (IllegalArgumentException | IllegalStateException | NullPointerException ignored) {

        }
    }

}
