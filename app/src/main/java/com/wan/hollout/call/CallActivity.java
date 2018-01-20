package com.wan.hollout.call;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.view.WindowManager;
import android.widget.Chronometer;

import com.wan.hollout.R;
import com.wan.hollout.callbacks.DoneCallback;
import com.wan.hollout.utils.AppConstants;
import com.wan.hollout.utils.DbUtils;

import org.greenrobot.eventbus.EventBus;

/**
 * @author Wan Clem
 */
public abstract class CallActivity extends AppCompatActivity {

    protected Activity mActivity;

    // Call time view
    protected Chronometer mChronometer;

    // Call id
    protected String mCallerId;

    // Is incoming call
    protected boolean isInComingCall;

    // Call end state, used to save the message after the end of the call tips
    protected int mCallStatus;

    // Call type, used to distinguish between voice and video calls, 0 video, 1 voice
    protected int mCallType;

    // AudioManager and SoundPool
    protected AudioManager mAudioManager;
    protected SoundPool mSoundPool;
    protected int streamID;
    protected int loadId;

    // Vibration
    protected Vibrator mVibrator;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
        if (savedInstanceState != null) {
            finish();
            return;
        }
        // keep the screen lit, close the input method, and unlock the device
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                | WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);

        EventBus.getDefault().post(AppConstants.SUSPEND_ALL_USE_OF_AUDIO_MANAGER);

    }

    /**
     * Init layout view
     */
    protected void initView() {
        mActivity = this;
        // Get call id
        mCallerId = getIntent().getStringExtra(AppConstants.EXTRA_USER_ID);
        isInComingCall = getIntent().getBooleanExtra(AppConstants.EXTRA_IS_INCOMING_CALL, false);
        // Set default call end status
        mCallStatus = CallStatus.CALL_CANCEL;
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
        if (CallStatus.getInstance().getCallState() == CallStatus.CALL_STATUS_NORMAL) {
            // load sound
            if (isInComingCall) {
                loadId = mSoundPool.load(mActivity, R.raw.sound_call_incoming, 1);
            } else {
                loadId = mSoundPool.load(mActivity, R.raw.sound_calling, 1);
            }
            // Load SoundPool listener
            mSoundPool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
                @Override
                public void onLoadComplete(SoundPool soundPool, int i, int i1) {
                    playCallSound();
                }
            });
        }
    }

    /**
     * Call end save message to local
     */
    protected void saveCallMessage() {
        String content;
        switch (mCallStatus) {
            case CallStatus.CALL_ACCEPTED:
                content = mChronometer.getText().toString();
                break;
            case CallStatus.CALL_CANCEL:
                content = mActivity.getString(R.string.em_call_cancel);
                break;
            case CallStatus.CALL_CANCEL_INCOMING_CALL:
                content = mActivity.getString(R.string.em_call_cancel_incoming_call);
                break;
            case CallStatus.CALL_BUSY:
                content = String.format(mActivity.getString(R.string.em_call_busy), mCallerId);
                break;
            case CallStatus.CALL_OFFLINE:
                content = String.format(mActivity.getString(R.string.em_call_not_online), mCallerId);
                break;
            case CallStatus.CALL_REJECT_INCOMING_CALL:
                content = mActivity.getString(R.string.em_call_reject_incoming_call);
                break;
            case CallStatus.CALL_REJECT:
                content = String.format(mActivity.getString(R.string.em_call_reject), mCallerId);
                break;
            case CallStatus.CALL_NO_RESPONSE:
                content = String.format(mActivity.getString(R.string.em_call_no_response), mCallerId);
                break;
            case CallStatus.CALL_TRANSPORT:
                content = mActivity.getString(R.string.em_call_connection_fail);
                break;
            case CallStatus.CALL_VERSION_DIFFERENT:
                content = String.format(mActivity.getString(R.string.em_call_not_online), mCallerId);
                break;
            default:
                content = mActivity.getString(R.string.em_call_cancel);
                break;
        }

        final String finalContent = content;
        DbUtils.getEntityName(AppConstants.ENTITY_TYPE_INDIVIDUAL, mCallerId, new DoneCallback<String>() {
            @Override
            public void done(String result, Exception e) {
                if (e == null && result != null) {
                    DbUtils.createCallLog(mCallerId, result, finalContent, isInComingCall, mCallType == 0);
                }
            }
        });
    }

    /**
     * Call vibration
     */
    protected void vibrate() {
        if (mVibrator == null) {
            mVibrator = (Vibrator) mActivity.getSystemService(Context.VIBRATOR_SERVICE);
        }
        mVibrator.vibrate(60);
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
