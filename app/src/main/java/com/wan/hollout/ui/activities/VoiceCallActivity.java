package com.wan.hollout.ui.activities;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.PorterDuff;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.sinch.android.rtc.calling.CallState;
import com.wan.hollout.R;
import com.wan.hollout.clients.CallClient;
import com.wan.hollout.enums.CallType;
import com.wan.hollout.eventbuses.CallTerminationCause;
import com.wan.hollout.ui.widgets.CircleImageView;
import com.wan.hollout.utils.AppConstants;
import com.wan.hollout.utils.CallStateTracker;
import com.wan.hollout.utils.DbUtils;
import com.wan.hollout.utils.NotificationHelper;
import com.wan.hollout.utils.UiUtils;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.WordUtils;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * @author Wan Clem
 ***/
public class VoiceCallActivity extends CallActivity {

    @BindView(R.id.img_call_background)
    ImageView mCallBackgroundView;

    @BindView(R.id.text_call_status)
    TextView mCallStatusView;

    @BindView(R.id.img_call_avatar)
    CircleImageView mAvatarView;

    @BindView(R.id.text_call_username)
    TextView mUsernameView;

    @BindView(R.id.btn_mic_switch)
    ImageButton mMicSwitch;

    @BindView(R.id.btn_speaker_switch)
    ImageButton mSpeakerSwitch;

    @BindView(R.id.fab_reject_call)
    FloatingActionButton mRejectCallFab;

    @BindView(R.id.fab_end_call)
    FloatingActionButton mEndCallFab;

    @BindView(R.id.fab_answer_call)
    FloatingActionButton mAnswerCallFab;

    @BindView(R.id.toggle_container)
    View toggleContainer;

    @BindView(R.id.layout_calling)
    View layoutCalling;

    private NotificationManager mNotificationManager;
    private int callNotificationId = 342;

    private String callerName;

    /**
     * Call entrance
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_voice_call);
        ButterKnife.bind(this);
        initView();
        if (isInComingCall) {
            mCallStatusView.setText(getString(R.string.incoming_call));
            UiUtils.showView(toggleContainer, false);
            CallStateTracker.getInstance().setOutgoing(false);
        } else {
            mCallStatusView.setText(getString(R.string.outgoing_call));
            UiUtils.showView(toggleContainer, true);
            CallStateTracker.getInstance().setOutgoing(true);
        }
        fetchCallerUserDetails();
    }

    private void fetchCallerUserDetails() {
        CallStateTracker.getInstance().setCallerId(mCallerId);
        ParseQuery<ParseObject> callerQuery = ParseQuery.getQuery(AppConstants.PEOPLE_GROUPS_AND_ROOMS);
        callerQuery.whereEqualTo(AppConstants.REAL_OBJECT_ID, mCallerId);
        callerQuery.getFirstInBackground(new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject object, ParseException e) {
                if (e == null && object != null) {
                    callerName = object.getString(AppConstants.APP_USER_DISPLAY_NAME);
                    String userPhotoUrl = object.getString(AppConstants.APP_USER_PROFILE_PHOTO_URL);
                    String userCoverPhotoUrl = object.getString(AppConstants.APP_USER_COVER_PHOTO);
                    mUsernameView.setText(WordUtils.capitalize(callerName));
                    if (StringUtils.isNotEmpty(userPhotoUrl)) {
                        UiUtils.loadImage(VoiceCallActivity.this, userPhotoUrl, mAvatarView);
                    }
                    if (StringUtils.isNotEmpty(userCoverPhotoUrl)) {
                        UiUtils.loadImage(VoiceCallActivity.this, userCoverPhotoUrl, mCallBackgroundView);
                        mCallBackgroundView.setColorFilter(ContextCompat.getColor(VoiceCallActivity.this, R.color.black_transparent_70percent), PorterDuff.Mode.SRC_ATOP);
                    } else {
                        if (StringUtils.isNotEmpty(userPhotoUrl)) {
                            UiUtils.loadImage(VoiceCallActivity.this, userPhotoUrl, mCallBackgroundView);
                            mCallBackgroundView.setColorFilter(ContextCompat.getColor(VoiceCallActivity.this, R.color.black_transparent_70percent), PorterDuff.Mode.SRC_ATOP);
                        }
                    }
                }
            }
        });
        mCallBackgroundView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (layoutCalling.getVisibility() == View.VISIBLE) {
                    UiUtils.showView(layoutCalling, false);
                } else {
                    UiUtils.showView(layoutCalling, true);
                }
            }
        });
    }

    /**
     * Init layout view and call
     */
    @Override
    protected void initView() {
        super.initView();
        // Set call type
        mCallType = 1;
        mChronometer = findViewById(R.id.chronometer_call_time);
        // Set switch status
        UiUtils.showView(mCallStatusView, true);
        mCallStatusView.startAnimation(UiUtils.getBlinkingAnimation(getCurrentActivityInstance()));
        mMicSwitch.setActivated(true);
        mSpeakerSwitch.setActivated(true);
        mUsernameView.setText(getString(R.string.loading));
        // Check call state
        if (isInComingCall) {
            callId = getIntent().getStringExtra(AppConstants.CALL_ID);
            mRejectCallFab.setVisibility(View.VISIBLE);
            mEndCallFab.setVisibility(View.GONE);
            mAnswerCallFab.setVisibility(View.VISIBLE);
        } else {
            outgoingCallRinging(getString(R.string.em_call_connecting), View.GONE, View.VISIBLE);
            makeCall();
        }
    }

    private void outgoingCallRinging(String string, int gone, int visible) {
        mCallStatusView.setText(string);
        // Set button statue
        mRejectCallFab.setVisibility(gone);
        mEndCallFab.setVisibility(visible);
        mAnswerCallFab.setVisibility(gone);
    }

    /**
     * Make voice call
     */
    private void makeCall() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, 10);
            return;
        }
        callId = CallClient.getInstance().callUser(mCallerId, CallType.VOICE);
    }

    /**
     * widget onClick
     */
    @OnClick({
            R.id.btn_mic_switch, R.id.btn_speaker_switch,
            R.id.fab_reject_call, R.id.fab_end_call, R.id.fab_answer_call
    })
    void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_mic_switch:
                // Microphone switch
                onMicrophone();
                break;
            case R.id.btn_speaker_switch:
                // Speaker switch
                onSpeaker();
                break;
            case R.id.fab_reject_call:
                // Reject call
                rejectCall();
                break;
            case R.id.fab_end_call:
                // End call
                endCall();
                break;
            case R.id.fab_answer_call:
                // Answer call
                answerCall();
                break;
        }
    }

    /**
     * Microphone switch
     */
    private void onMicrophone() {
        // Vibrate
        vibrate();
        if (mMicSwitch.isActivated()) {
            // Pause voice transfer
            CallClient.getInstance().getAudioController().mute();
            mMicSwitch.setActivated(false);
        } else {
            // Resume voice transfer
            mMicSwitch.setActivated(true);
            CallClient.getInstance().getAudioController().unmute();
        }
    }

    /**
     * Speaker switch
     */
    private void onSpeaker() {
        // Vibrate
        vibrate();
        if (mSpeakerSwitch.isActivated()) {
            closeSpeaker();
        } else {
            openSpeaker();
        }
    }

    /**
     * Reject call
     */
    private void rejectCall() {
        vibrate();
        stopCallSound();
        // Call rejectCall();
        CallClient.getInstance().rejectCall(callId);
        if (isInComingCall) {
            CallStateTracker.getInstance().setWasRejected(true);
        }
        // Set call state
        // Save call message to
        saveCallMessage();
        // Finish activity
        onFinish();
    }

    /**
     * End call
     */
    private void endCall() {
        vibrate();
        stopCallSound();
        CallClient.getInstance().hangUp(callId);
        saveCallMessage();
        onFinish();
    }

    /**
     * Answer call
     */
    private void answerCall() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO}, 10);
            return;
        }
        CallStateTracker.getInstance().setWasRejected(false);
        CallStateTracker.getInstance().setWasAnswered(true);
        startChronometer();
        // Vibrate
        vibrate();
        // Set button state
        mRejectCallFab.setVisibility(View.GONE);
        mAnswerCallFab.setVisibility(View.GONE);
        mEndCallFab.setVisibility(View.VISIBLE);
        // Stop call sound
        stopCallSound();
        // Default open speaker
        openSpeaker();
        CallClient.getInstance().answerCall(callId);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            CallClient.getInstance().startCallClient();
            if (isInComingCall) {
                answerCall();
            } else {
                makeCall();
            }
        }
    }

    private void openSpeaker() {
        // Set button state
        mSpeakerSwitch.setActivated(true);
        if (!mAudioManager.isSpeakerphoneOn()) {
            // Open speaker
            mAudioManager.setSpeakerphoneOn(true);
        }
        // Set Audio mode
        mAudioManager.setMode(AudioManager.MODE_NORMAL);
        CallClient.getInstance().getAudioController().enableSpeaker();
    }

    /**
     * Close speaker
     * more see {@link #openSpeaker()}
     */
    private void closeSpeaker() {
        // Set button state
        mSpeakerSwitch.setActivated(false);
        if (mAudioManager.isSpeakerphoneOn()) {
            // Close speaker
            mAudioManager.setSpeakerphoneOn(false);
        }
        // Set Audio mode
        mAudioManager.setMode(AudioManager.MODE_IN_COMMUNICATION);
        CallClient.getInstance().getAudioController().disableSpeaker();
    }

    /**
     * send call notification
     */
    private void sendCallNotification() {
        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        NotificationHelper notificationHelper = new NotificationHelper(this, "OngoingCall", "Ongoing Call");
        NotificationCompat.Builder builder = new NotificationCompat.Builder(mActivity, "OngoingCall");

        builder.setSmallIcon(R.mipmap.ic_launcher);
        builder.setPriority(Notification.PRIORITY_HIGH);
        builder.setAutoCancel(true);
        builder.setDefaults(Notification.DEFAULT_VIBRATE | Notification.DEFAULT_LIGHTS);

        builder.setContentText("Tap here to return back to call");

        builder.setContentTitle(getString(R.string.app_name));
        Intent intent = new Intent(mActivity, VoiceCallActivity.class);
        PendingIntent pIntent =
                PendingIntent.getActivity(mActivity, 0, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        builder.setContentIntent(pIntent);
        builder.setOngoing(true);

        builder.setWhen(System.currentTimeMillis());

        notificationHelper.notify(callNotificationId, builder.build());
    }

    @Override
    protected void onUserLeaveHint() {
        sendCallNotification();
        super.onUserLeaveHint();
    }

    /**
     * Call end finish activity
     */
    @Override
    protected void onFinish() {
        // Call end release SurfaceView
        mAudioManager.setMode(AudioManager.MODE_NORMAL);
        if (mNotificationManager != null) {
            mNotificationManager.cancelAll();
        }
        super.onFinish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mNotificationManager != null) {
            mNotificationManager.cancel(callNotificationId);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mAudioManager.setMode(AudioManager.MODE_NORMAL);
    }

    @Override
    public void onEventAsync(final Object o) {
        super.onEventAsync(o);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (o instanceof CallTerminationCause) {
                    CallTerminationCause callTerminationCause = (CallTerminationCause) o;
                    String terminationMessage = callTerminationCause.getTerminationCause();
                    if (StringUtils.containsIgnoreCase(terminationMessage, "No Answer")
                            || StringUtils.containsIgnoreCase(terminationMessage, "Canceled")) {
                        UiUtils.showSafeToast(terminationMessage);
                        endCall();
                        if (StringUtils.containsIgnoreCase(terminationMessage, "Canceled")) {
                            if (isInComingCall) {
                                DbUtils.createNewMissedCallMessage(callerName, mCallerId, "Missed Voice Call");
                            }
                        }
                    } else if (StringUtils.containsIgnoreCase(terminationMessage, "Denied")) {
                        if (!isInComingCall) {
                            UiUtils.showSafeToast("User Busy!");
                            UiUtils.bangSound(getCurrentActivityInstance(), R.raw.redphone_busy);
                        }
                        endCall();
                    }
                } else if (o instanceof CallState) {
                    CallState callState = (CallState) o;
                    switch (callState) {
                        case INITIATING:
                            // Set call state view show content
                            mCallStatusView.setText(getString(R.string.em_call_connecting));
                            break;
                        case PROGRESSING:
                            // Set call state view show content
                            if (isInComingCall) {
                                mCallStatusView.setText(getString(R.string.incoming_call));
                                CallStateTracker.getInstance().setWasRinging(true);
                            } else {
                                mCallStatusView.setText(getString(R.string.ringing));
                            }
                            break;
                        case TRANSFERRING:
                            stopCallSound();
                            closeSpeaker();
                            // Set call state view show content
                            mCallStatusView.setText(R.string.em_call_accepted);
                            // Set call state
                            // Start time
                            UiUtils.showView(mChronometer, true);
                            startChronometer();
                            break;
                        case ENDED:
                            // Stop time
                            mChronometer.stop();
                            // Set call state view show content
                            mCallStatusView.setText(R.string.em_call_disconnected);
                            saveCallMessage();
                            // Remove call state listener
                            // Finish activity
                            onFinish();
                            break;
                        case ESTABLISHED:
                            stopCallSound();
                            UiUtils.showView(mChronometer, true);
                            startChronometer();
                            UiUtils.showView(toggleContainer, true);
                            break;
                        default:
                            break;
                    }
                }
            }
        });
    }

    private void startChronometer() {
        mCallStatusView.clearAnimation();
        UiUtils.showView(mCallStatusView, false);
        mChronometer.setBase(SystemClock.elapsedRealtime());
        mChronometer.start();
    }

}
