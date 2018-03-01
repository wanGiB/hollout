package com.wan.hollout.ui.activities;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.view.View;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.sinch.android.rtc.calling.Call;
import com.sinch.android.rtc.calling.CallState;
import com.sinch.android.rtc.video.VideoController;
import com.wan.hollout.R;
import com.wan.hollout.clients.CallClient;
import com.wan.hollout.enums.CallType;
import com.wan.hollout.eventbuses.CallTerminationCause;
import com.wan.hollout.ui.widgets.CircleImageView;
import com.wan.hollout.utils.AppConstants;
import com.wan.hollout.utils.NotificationHelper;
import com.wan.hollout.utils.UiUtils;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.WordUtils;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * @author Wan Clem
 */
public class VideoCallActivity extends CallActivity implements ActivityCompat.OnRequestPermissionsResultCallback {

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

    @BindView(R.id.btn_change_camera_switch)
    ImageButton cameraSwitch;

    @BindView(R.id.fab_reject_call)
    FloatingActionButton mRejectCallFab;

    @BindView(R.id.fab_end_call)
    FloatingActionButton mEndCallFab;

    @BindView(R.id.fab_answer_call)
    FloatingActionButton mAnswerCallFab;

    @BindView(R.id.surface_view_local)
    RelativeLayout surfaceViewLocal;

    @BindView(R.id.user_details_component)
    View userDetailsComponent;

    @BindView(R.id.layout_calling)
    View bottomContainer;

    @BindView(R.id.surface_view_opposite)
    RelativeLayout surfaceViewRemote;

    @BindView(R.id.toggle_container)
    View toggleContainer;

    private NotificationManager mNotificationManager;
    private int callNotificationId = 342;

    /**
     * Call entrance
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_call);
        ButterKnife.bind(this);
        initView();
        if (isInComingCall) {
            mCallStatusView.setText(getString(R.string.incoming_video_call));
            UiUtils.showView(toggleContainer, false);
        } else {
            mCallStatusView.setText(getString(R.string.outgoing_video_call));
            UiUtils.showView(toggleContainer, true);
        }
        fetchCallerUserDetails();
    }

    @Override
    protected void onStop() {
        super.onStop();
        removeVideoViews();
    }

    private void fetchCallerUserDetails() {
        ParseQuery<ParseObject> callerQuery = ParseQuery.getQuery(AppConstants.PEOPLE_GROUPS_AND_ROOMS);
        callerQuery.whereEqualTo(AppConstants.REAL_OBJECT_ID, mCallerId);
        callerQuery.getFirstInBackground(new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject object, ParseException e) {
                if (e == null && object != null) {
                    String callerName = object.getString(AppConstants.APP_USER_DISPLAY_NAME);
                    String userPhotoUrl = object.getString(AppConstants.APP_USER_PROFILE_PHOTO_URL);
                    mUsernameView.setText(WordUtils.capitalize(callerName));
                    if (StringUtils.isNotEmpty(userPhotoUrl)) {
                        UiUtils.loadImage(VideoCallActivity.this, userPhotoUrl, mAvatarView);
                    }
                }
            }
        });
    }

    private void addLocalView() {
        final VideoController vc = CallClient.getInstance().getVideoController();
        if (vc != null) {
            surfaceViewLocal.addView(vc.getLocalView());
            surfaceViewLocal.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    vc.toggleCaptureDevicePosition();
                }
            });
        }
    }

    private void addRemoteView() {
        final VideoController vc = CallClient.getInstance().getVideoController();
        if (vc != null) {
            surfaceViewRemote.addView(vc.getRemoteView());
        }
        surfaceViewRemote.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (userDetailsComponent.getVisibility() == View.VISIBLE) {
                    UiUtils.showView(userDetailsComponent, false);
                    UiUtils.showView(bottomContainer, false);
                } else {
                    UiUtils.showView(userDetailsComponent, true);
                    UiUtils.showView(bottomContainer, true);
                }
            }
        });
    }

    private void removeVideoViews() {
        VideoController vc = CallClient.getInstance().getVideoController();
        if (vc != null) {
            surfaceViewRemote.removeView(vc.getRemoteView());
            surfaceViewLocal.removeView(vc.getLocalView());
        }
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
        // Check call state
        if (isInComingCall) {
            callId = getIntent().getStringExtra(AppConstants.CALL_ID);
            mRejectCallFab.setVisibility(View.VISIBLE);
            mEndCallFab.setVisibility(View.GONE);
            mAnswerCallFab.setVisibility(View.VISIBLE);
        } else {
            outgoingCallRinging(getString(R.string.em_call_connecting), View.GONE, View.VISIBLE);
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED
                    || ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO, Manifest.permission.CAMERA}, 10);
                return;
            }
            makeCall();
        }
        final Call call = CallClient.getInstance().getCall(callId);
        if (call.getDetails().isVideoOffered()) {
            addLocalView();
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
        callId = CallClient.getInstance().callUser(mCallerId, CallType.VIDEO);
    }

    /**
     * widget onClick
     */
    @OnClick({
            R.id.btn_mic_switch, R.id.btn_speaker_switch,
            R.id.fab_reject_call, R.id.fab_end_call, R.id.fab_answer_call, R.id.btn_change_camera_switch
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
            case R.id.btn_change_camera_switch:
                toggleCamera();
                break;
        }
    }

    private void toggleCamera() {
        vibrate();
        cameraSwitch.setActivated(!cameraSwitch.isActivated());
        CallClient.getInstance().getVideoController().toggleCaptureDevicePosition();
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
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.RECORD_AUDIO, Manifest.permission.CAMERA}, 10);
            return;
        }
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
                            addRemoteView();
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
