package com.wan.hollout.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.wan.hollout.utils.AppConstants;

/**
 * @author Wan Clem
 */
@SuppressWarnings("FieldCanBeLocal")
public class CallReceiver extends BroadcastReceiver {

    private String TYPE_VIDEO = "video";
    private String TYPE_VOICE = "voice";

    public CallReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String callFrom = intent.getStringExtra(AppConstants.EXTRA_FROM);
        String callType = intent.getStringExtra(AppConstants.EXTRA_TYPE);
        String callTo = intent.getStringExtra(AppConstants.EXTRA_TO);

//        if (callTo.equals(EMClient.getInstance().getCurrentUser())) {
//            Intent callIntent = new Intent();
//            // Check call type
//            if (callType.equals(TYPE_VIDEO)) {
//                callIntent.setClass(context, VideoCallActivity.class);
//                CallStatus.getInstance().setCallType(CallStatus.CALL_TYPE_VIDEO);
//            } else if (callType.equals(TYPE_VOICE)) {
//                callIntent.setClass(context, VoiceCallActivity.class);
//                CallStatus.getInstance().setCallType(CallStatus.CALL_TYPE_VOICE);
//            }
//            // Set activity flag
//            callIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//            callIntent.putExtra(AppConstants.EXTRA_USER_ID, callFrom);
//            callIntent.putExtra(AppConstants.EXTRA_IS_INCOMING_CALL, true);
//            context.startActivity(callIntent);
//        }
    }
}