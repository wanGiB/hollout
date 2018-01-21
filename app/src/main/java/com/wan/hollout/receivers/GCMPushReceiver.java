package com.wan.hollout.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.wan.hollout.managers.HolloutCommunicationsManager;
import com.wan.hollout.utils.HolloutLogger;

public class GCMPushReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		HolloutLogger.i("HyphenateGCM", "Hyphenate Message Push Notification received");
		HolloutCommunicationsManager.getInstance().init(context);
	}

}
