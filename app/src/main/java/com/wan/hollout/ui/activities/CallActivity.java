package com.wan.hollout.ui.activities;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.media.SoundPool;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.Vibrator;
import android.view.WindowManager;
import android.widget.Chronometer;

import com.github.nisrulz.sensey.ProximityDetector;
import com.github.nisrulz.sensey.Sensey;
import com.wan.hollout.R;
import com.wan.hollout.utils.AppConstants;

import org.greenrobot.eventbus.EventBus;

/**
 * @author Wan Clem
 */
public abstract class CallActivity extends BaseActivity {

    protected Activity mActivity;

    // Call time view
    protected Chronometer mChronometer;

    // Call id
    protected String mCallerId;
    protected String callId;

    // Is incoming call
    protected boolean isInComingCall;

    // Call type, used to distinguish between voice and video calls, 0 video, 1 voice
    protected int mCallType;

    // AudioManager and SoundPool
    protected AudioManager mAudioManager;
    protected SoundPool mSoundPool;
    protected int streamID;
    protected int loadId;

    // Vibration
    protected Vibrator mVibrator;
    protected MediaPlayer mediaPlayer;

    private ProximityDetector.ProximityListener proximityListener;

    private PowerManager.WakeLock wakeLock;
    private int field = 0x00000020;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            finish();
            return;
        }
        keepScreeOn();
        EventBus.getDefault().post(AppConstants.SUSPEND_ALL_USE_OF_AUDIO_MANAGER);
        Sensey.getInstance().init(this);
        try {
            // Yeah, this is hidden field.
            field = PowerManager.class.getField("PROXIMITY_SCREEN_OFF_WAKE_LOCK").getInt(null);
        } catch (Throwable ignored) {

        }
        PowerManager powerManager = (PowerManager) getSystemService(POWER_SERVICE);
        if (powerManager != null) {
            wakeLock = powerManager.newWakeLock(field, getLocalClassName());
        }
        proximityListener = new ProximityDetector.ProximityListener() {

            @Override
            public void onNear() {
                turnOffScreen();
            }

            @Override
            public void onFar() {
                turnScreenOn();
            }
        };
        checkStartProximityDetector();
    }

    @Override
    protected void onResume() {
        super.onResume();
        checkStartProximityDetector();
    }

    @Override
    protected void onPause() {
        super.onPause();
        releaseProximityDetector();
    }

    protected void checkStartProximityDetector() {
        releaseProximityDetector();
        Sensey.getInstance().startProximityDetection(proximityListener);
    }

    @SuppressLint("WakelockTimeout")
    protected void turnScreenOn() {
        if (wakeLock != null && !wakeLock.isHeld()) {
            wakeLock.acquire();
        }
    }

    protected void turnOffScreen() {
        if (wakeLock != null && wakeLock.isHeld()) {
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Sensey.getInstance().stop();
        if (wakeLock != null && wakeLock.isHeld()) {
            wakeLock.release();
        }
    }

    private void keepScreeOn() {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
    }

    @Override
    protected void onStart() {
        super.onStart();
        checkStartProximityDetector();
    }

    @Override
    protected void onStop() {
        super.onStop();
        releaseProximityDetector();
    }

    private void releaseProximityDetector() {
        if (proximityListener != null) {
            Sensey.getInstance().stopProximityDetection(proximityListener);
        }
    }

    /**
     * Init layout view
     */
    protected void initView() {
        mActivity = this;
        // Get call id
        mCallerId = getIntent().getStringExtra(AppConstants.CALLER_ID);
        isInComingCall = getIntent().getBooleanExtra(AppConstants.EXTRA_IS_INCOMING_CALL, false);
        // Vibrator
        mVibrator = (Vibrator) mActivity.getSystemService(Context.VIBRATOR_SERVICE);
        // AudioManager
        mAudioManager = (AudioManager) mActivity.getSystemService(Context.AUDIO_SERVICE);
        // According to the different versions of the system to select different ways to initialize the audio playback tool
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            createSoundPoolWithBuilder();
        } else {
            createSoundPoolWithConstructor();
        }
        if (isInComingCall) {
            loadDefaultIncomingCallRingtone();
            return;
        }
        loadId = mSoundPool.load(mActivity, R.raw.progress_tone, 1);
        // Load SoundPool listener
        mSoundPool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
            @Override
            public void onLoadComplete(SoundPool soundPool, int i, int i1) {
                playCallSound();
            }
        });
    }

    public void loadDefaultIncomingCallRingtone() {
        Uri uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_RINGTONE);
        if (uri != null) {
            mediaPlayer = MediaPlayer.create(this, uri);
            mediaPlayer.setLooping(true);
            if (mediaPlayer != null) {
                mediaPlayer.start();
            }
        }
    }

    /**
     * Call end save message to local
     */
    protected void saveCallMessage() {

    }

    /**
     * Call vibration
     */
    protected void vibrate() {
        if (mVibrator == null) {
            mVibrator = (Vibrator) mActivity.getSystemService(Context.VIBRATOR_SERVICE);
        }
        if (mVibrator != null) {
            mVibrator.vibrate(60);
        }
    }

    /**
     * Play a call tone
     */
    protected void playCallSound() {
        if (!mAudioManager.isSpeakerphoneOn()) {
            mAudioManager.setSpeakerphoneOn(true);
        }
        // Set AudioManager MODE to ring the MODE_RINGTONE
        mAudioManager.setMode(AudioManager.MODE_RINGTONE);
        // Play the call tone, return to play the resource id, used to stop later
        if (mSoundPool != null) {
            streamID = mSoundPool.play(loadId,
                    // Resource id, the order in which audio resources are loaded into the SoundPool
                    0.5f,   // Left volume
                    0.5f,   // Right volume
                    1,      // Audio file priority
                    -1,     // Whether the cycle; 0 does not cycle, -1 cycle
                    1);     // Playback ratio; from 0.5-2, generally set to 1, that normal play
        }
    }

    /**
     * Turn off the audio playback, and release the resources
     */
    protected void stopCallSound() {
        if (mSoundPool != null) {
            // Stop SoundPool
            mSoundPool.stop(streamID);
            // release
            mSoundPool.release();
            mSoundPool = null;
        }
        if (mediaPlayer != null) {
            try {
                mediaPlayer.pause();
                mediaPlayer.stop();
                mediaPlayer.release();
            } catch (IllegalStateException ignored) {

            }
        }
    }

    /**
     * When the SDK version of the system is higher than 21, the SoundPool is instantiated in a
     * different way
     * Instantiate SoundPool using {@link SoundPool.Builder}
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    protected void createSoundPoolWithBuilder() {
        AudioAttributes attributes = new AudioAttributes.Builder()
                // Set the audio mode, This select USAGE_NOTIFICATION_RINGTONE
                .setUsage(AudioAttributes.USAGE_NOTIFICATION_RINGTONE)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build();
        mSoundPool =
                new SoundPool.Builder().setAudioAttributes(attributes).setMaxStreams(1).build();
    }

    /**
     * The old version uses the constructor function to instantiate the SoundPool, and the MODE to
     * ring the MODE_RINGTONE
     */
    protected void createSoundPoolWithConstructor() {
        mSoundPool = new SoundPool(1, AudioManager.MODE_RINGTONE, 0);
    }

    /**
     * finish activity
     */
    protected void onFinish() {
        // turn off call sound and release resources
        stopCallSound();
        finish();
    }

    /**
     * Overload Return key
     */
    @Override
    public void onBackPressed() {
        // super.onBackPressed();
    }

}
