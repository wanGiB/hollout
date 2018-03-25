package com.wan.hollout.clients;

import android.content.ContextWrapper;
import android.content.Intent;
import android.os.Looper;

import com.parse.ParseObject;
import com.sinch.android.rtc.AudioController;
import com.sinch.android.rtc.ClientRegistration;
import com.sinch.android.rtc.PushPair;
import com.sinch.android.rtc.Sinch;
import com.sinch.android.rtc.SinchClient;
import com.sinch.android.rtc.SinchClientListener;
import com.sinch.android.rtc.SinchError;
import com.sinch.android.rtc.calling.Call;
import com.sinch.android.rtc.calling.CallClientListener;
import com.sinch.android.rtc.calling.CallListener;
import com.sinch.android.rtc.calling.CallState;
import com.sinch.android.rtc.video.VideoController;
import com.sinch.android.rtc.video.VideoScalingType;
import com.wan.hollout.BuildConfig;
import com.wan.hollout.components.ApplicationLoader;
import com.wan.hollout.enums.CallType;
import com.wan.hollout.eventbuses.CallTerminationCause;
import com.wan.hollout.ui.activities.VideoCallActivity;
import com.wan.hollout.ui.activities.VoiceCallActivity;
import com.wan.hollout.utils.AppConstants;
import com.wan.hollout.utils.AppKeys;
import com.wan.hollout.utils.AuthUtil;
import com.wan.hollout.utils.HolloutLogger;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.WordUtils;
import org.greenrobot.eventbus.EventBus;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Executors;

/**
 * @author Wan Clem
 */

public class CallClient extends ContextWrapper {

    private String TAG = "CallClient";

    private static CallClient instance;

    private SinchClient sinchClient;
    private SinchClientListener sinchClientListener;
    private CallClientListener callClientListener;
    private CallListener callListener;

    public static CallClient getInstance() {
        if (instance == null) {
            instance = new CallClient();
        }
        return instance;
    }

    private CallClient() {
        super(ApplicationLoader.getInstance());
        ParseObject signedInUser = AuthUtil.getCurrentUser();
        if (signedInUser != null) {
            final String userId = signedInUser.getString(AppConstants.REAL_OBJECT_ID);
            if (StringUtils.isNotEmpty(userId)) {
                // Specify the client capabilities.
                Thread sinchInitThread = new Thread() {
                    @Override
                    public void run() {
                        super.run();
                        Looper.prepare();
                        initSinchClient(userId);
                        Looper.loop();
                    }
                };
                Executors.newCachedThreadPool().execute(sinchInitThread);
            }
        }
    }

    private void initSinchClient(String userId) {
        sinchClient = Sinch.getSinchClientBuilder().context(ApplicationLoader.getInstance())
                .applicationKey(AppKeys.SINCH_APPLICATION_KEY)
                .applicationSecret(AppKeys.SINCH_APP_SECRET)
                .environmentHost(BuildConfig.DEBUG ? "sandbox.sinch.com" : "clientapi.sinch.com")
                .userId(userId)
                .build();
        sinchClient.getCallClient().setRespectNativeCalls(false);
        sinchClient.getVideoController().setResizeBehaviour(VideoScalingType.ASPECT_FILL);
        sinchClient.setSupportMessaging(false);
        sinchClient.setSupportCalling(true);
        sinchClient.setSupportManagedPush(true);
        sinchClient.setSupportActiveConnectionInBackground(true);
        sinchClient.startListeningOnActiveConnection();
        if (sinchClientListener != null && sinchClient != null) {
            sinchClient.removeSinchClientListener(sinchClientListener);
            sinchClientListener = null;
        }
        sinchClientListener = new SinchClientListener() {

            @Override
            public void onClientStarted(SinchClient sinchClient) {
                HolloutLogger.d(TAG, "Call Client Connected");
                checkAndRegisterCallClient();
            }

            @Override
            public void onClientStopped(SinchClient sinchClient) {
                HolloutLogger.d(TAG, "Call Client Stopped");
            }

            @Override
            public void onClientFailed(SinchClient sinchClient, SinchError sinchError) {
                HolloutLogger.d(TAG, "Call Client Failed to connect with error message");
            }

            @Override
            public void onRegistrationCredentialsRequired(SinchClient sinchClient, ClientRegistration clientRegistration) {
                HolloutLogger.d(TAG, "Call Client Registrations required");
            }

            @Override
            public void onLogMessage(int level, String s, String message) {
                String terminationCause = WordUtils.capitalize(StringUtils.substringBefore(StringUtils.substringAfter(message, "terminationCause="), ",")
                        .replace("_", " ").toLowerCase());
                HolloutLogger.d(TAG, terminationCause);
                EventBus.getDefault().post(new CallTerminationCause(terminationCause));
            }

        };
        sinchClient.addSinchClientListener(sinchClientListener);
        sinchClient.start();
    }

    private void checkAndRegisterCallClient() {
        if (callClientListener != null && sinchClient != null) {
            sinchClient.getCallClient().removeCallClientListener(callClientListener);
            callClientListener = null;
        }
        callClientListener = new CallClientListener() {

            @Override
            public void onIncomingCall(com.sinch.android.rtc.calling.CallClient callClient, Call call) {
                //Handle Incoming Calls Here
                HolloutLogger.d(TAG, "New Incoming Call detected");
                Map<String, String> callHeaders = call.getHeaders();
                boolean isVideo = Boolean.valueOf(callHeaders.get(AppConstants.IS_VIDEO));
                Intent intent = new Intent(ApplicationLoader.getInstance(), isVideo ? VideoCallActivity.class : VoiceCallActivity.class);
                intent.putExtra(AppConstants.CALLER_ID, callHeaders.get(AppConstants.CALLER_ID));
                intent.putExtra(AppConstants.CALL_ID, call.getCallId());
                intent.putExtra(AppConstants.EXTRA_IS_INCOMING_CALL, true);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                if (callListener != null) {
                    call.removeCallListener(callListener);
                }
                callListener = new CallListener() {

                    @Override
                    public void onCallProgressing(Call call) {
                        broadCastCallState(call.getState());
                    }

                    @Override
                    public void onCallEstablished(Call call) {
                        broadCastCallState(call.getState());
                    }

                    @Override
                    public void onCallEnded(Call call) {
                        broadCastCallState(call.getState());
                    }

                    @Override
                    public void onShouldSendPushNotification(Call call, List<PushPair> list) {
                        broadCastCallState(call.getState());
                    }

                };
                call.addCallListener(callListener);
                ApplicationLoader.getInstance().startActivity(intent);
            }

        };
        sinchClient.getCallClient().addCallClientListener(callClientListener);
    }

    public void startCallClient() {
        //DO Nothing...Client already started if not started
    }

    public Call getCall(String callId) {
        return sinchClient.getCallClient().getCall(callId);
    }

    public boolean isCallClientStarted() {
        return sinchClient != null && sinchClient.isStarted();
    }

    public VideoController getVideoController() {
        if (!isCallClientStarted()) {
            return null;
        }
        return sinchClient.getVideoController();
    }

    public AudioController getAudioController() {
        if (!isCallClientStarted()) {
            return null;
        }
        return sinchClient.getAudioController();
    }

    public String callUser(String userId, CallType callType) {
        Call call = null;
        ParseObject signedInUser = AuthUtil.getCurrentUser();
        if (sinchClient != null && signedInUser != null) {
            String signedInUserId = signedInUser.getString(AppConstants.REAL_OBJECT_ID);
            com.sinch.android.rtc.calling.CallClient callClient = sinchClient.getCallClient();
            Map<String, String> callHeaders = new HashMap<>();
            if (StringUtils.isNotEmpty(signedInUserId)) {
                callHeaders.put(AppConstants.CALLER_ID, signedInUserId);
            }
            if (callType == CallType.VOICE) {
                callHeaders.put(AppConstants.IS_VIDEO, String.valueOf(false));
                call = callClient.callUser(userId, callHeaders);
            } else {
                callHeaders.put(AppConstants.IS_VIDEO, String.valueOf(true));
                call = callClient.callUserVideo(userId, callHeaders);
            }
            if (call != null) {
                if (callListener != null) {
                    call.removeCallListener(callListener);
                }
                callListener = new CallListener() {
                    @Override
                    public void onCallProgressing(Call call) {
                        broadCastCallState(call.getState());
                    }

                    @Override
                    public void onCallEstablished(Call call) {
                        broadCastCallState(call.getState());
                    }

                    @Override
                    public void onCallEnded(Call call) {
                        broadCastCallState(call.getState());
                    }

                    @Override
                    public void onShouldSendPushNotification(Call call, List<PushPair> list) {
                        broadCastCallState(call.getState());
                    }
                };
                call.addCallListener(callListener);
            }
        }
        if (call != null) {
            return call.getCallId();
        }
        return null;
    }


    private void broadCastCallState(CallState callState) {
        EventBus.getDefault().post(callState);
    }

    public void rejectCall(String callId) {
        Call call = getCall(callId);
        if (call != null) {
            call.hangup();
        }
    }

    public void hangUp(String callId) {
        Call call = getCall(callId);
        if (call != null) {
            call.hangup();
        }
    }

    public void answerCall(String callId) {
        Call call = getCall(callId);
        if (call != null) {
            call.answer();
        }
    }

}
