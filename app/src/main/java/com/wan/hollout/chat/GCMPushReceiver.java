package com.wan.hollout.chat;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.wan.hollout.utils.HolloutLogger;

public class GCMPushReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		HolloutLogger.i("info", "gcmpush onreceive");
		HolloutCommunicationsManager.getInstance().init(context);
	}

}
