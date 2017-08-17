package com.wan.hollout.chat;

import android.app.NotificationManager;
import android.content.Context;
import android.media.AudioManager;
import android.os.Vibrator;

import com.hyphenate.chat.EMMessage;

/**
 * @author Wan Clem
 */

public class MessageNotifier {


    private NotificationManager notificationManager = null;

    private Context appContext;
    private String packageName;
    private String[] msgs;
    private long lastNotifyTime;
    private AudioManager audioManager;
    private Vibrator vibrator;


    public MessageNotifier init(Context context){
        appContext = context;
        notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        packageName = appContext.getApplicationInfo().packageName;
        audioManager = (AudioManager) appContext.getSystemService(Context.AUDIO_SERVICE);
        vibrator = (Vibrator) appContext.getSystemService(Context.VIBRATOR_SERVICE);
        return this;
    }

    public  void onNewMsg(EMMessage emMessage){

    }

}
