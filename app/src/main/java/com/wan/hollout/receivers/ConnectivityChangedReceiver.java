package com.wan.hollout.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.wan.hollout.clients.CallClient;
import com.wan.hollout.clients.ChatClient;
import com.wan.hollout.ui.services.AppInstanceDetectionService;
import com.wan.hollout.utils.FirebaseUtils;

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
                Intent mAppInstanceDetectIntent = new Intent(context, AppInstanceDetectionService.class);
                context.startService(mAppInstanceDetectIntent);
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
