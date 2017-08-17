package com.wan.hollout.chat;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

public class GCMPushReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(Context context, Intent intent) {
		Log.i("info", "gcmpush onreceive");
		String alert = intent.getStringExtra("alert");
//		DemoHelper.getInstance().getNotifier().onNewMsg(alert);
	}

}
