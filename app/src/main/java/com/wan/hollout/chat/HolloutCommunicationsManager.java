package com.wan.hollout.chat;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import com.hyphenate.EMCallBack;
import com.hyphenate.EMConnectionListener;
import com.hyphenate.EMContactListener;
import com.hyphenate.EMError;
import com.hyphenate.EMGroupChangeListener;
import com.hyphenate.EMMessageListener;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMCmdMessageBody;
import com.hyphenate.chat.EMMessage;
import com.hyphenate.chat.EMMucSharedFile;
import com.hyphenate.chat.EMOptions;
import com.hyphenate.exceptions.HyphenateException;
import com.hyphenate.util.EMLog;
import com.parse.ParseUser;
import com.wan.hollout.R;
import com.wan.hollout.call.CallReceiver;
import com.wan.hollout.call.CallStateChangeListener;
import com.wan.hollout.callbacks.DoneCallback;
import com.wan.hollout.components.ApplicationLoader;
import com.wan.hollout.eventbuses.MessageChangedEvent;
import com.wan.hollout.eventbuses.MessageDeliveredEvent;
import com.wan.hollout.eventbuses.MessageReadEvent;
import com.wan.hollout.eventbuses.MessageReceivedEvent;
import com.wan.hollout.ui.activities.MainActivity;
import com.wan.hollout.utils.AppConstants;
import com.wan.hollout.utils.HolloutLogger;

import org.greenrobot.eventbus.EventBus;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @author Wan Clem
 */

@SuppressWarnings({"WeakerAccess", "unused"})
@SuppressLint("StaticFieldLeak")
public class HolloutCommunicationsManager {

    private static HolloutCommunicationsManager instance;

    protected static final String TAG = "HolloutCommunicationsManager";

    // Call broadcast receiver
    private CallReceiver mCallReceiver = null;

    // Call state listener
    private CallStateChangeListener mCallStateChangeListener = null;
    // Contacts listener
    private ContactsChangeListener mContactListener = null;
    private GroupChangeListener mGroupListener = null;
    private EMConnectionListener mConnectionListener;
    private EMMessageListener messageListener;

    private static Context mContext;
    private MessageNotifier mNotifier = new MessageNotifier();
    private ExecutorService executor = null;

    private HolloutCommunicationsManager() {
        this.executor = Executors.newCachedThreadPool();
    }

    public synchronized static HolloutCommunicationsManager getInstance() {
        mContext = ApplicationLoader.getInstance();
        if (instance == null) {
            instance = new HolloutCommunicationsManager();
        }
        return instance;
    }

    public void execute(Runnable runnable) {
        executor.execute(runnable);
    }

    /**
     * According to Pid to obtain the name of the current process, the general is the current app
     * package name,
     *
     * @param pID Process ID
     * @return Process name
     */
    private String getAppName(int pID) {
        String processName;
        ActivityManager am = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
        List l = am.getRunningAppProcesses();
        for (Object aL : l) {
            ActivityManager.RunningAppProcessInfo info =
                    (ActivityManager.RunningAppProcessInfo) (aL);
            try {
                if (info.pid == pID) {
                    processName = info.processName;
                    return processName;
                }
            } catch (Exception ignored) {

            }
        }
        return null;
    }

    /**
     * check the application process name if process name is not qualified, then we think it is a
     * service process and we will not init SDK
     */
    private boolean isMainProcess() {
        int pid = android.os.Process.myPid();
        String processAppName = getAppName(pid);
        HolloutLogger.d(TAG, "process app name : " + processAppName);
        // if there is application has remote service, application:onCreate() maybe called twice
        // this check is to make sure SDK will initialized only once
        // return if process name is not application's name since the package name is the default process name
        if (processAppName == null || !processAppName.equalsIgnoreCase(mContext.getPackageName())) {
            HolloutLogger.e(TAG, "enter the service process!");
            return false;
        }
        return true;
    }

    /**
     * init helper
     *
     * @param context application context
     */
    public void init(Context context) {
        mContext = context;
        if (isMainProcess()) {
            HolloutLogger.d(TAG, "------- init hyphenate start --------------");
            //init hyphenate sdk with options
            EMClient.getInstance().init(context, initOptions());
            // init call options
            initCallOptions();
            //init message notifier
            mNotifier.init(context);
            // set debug mode open:true, close:false
            EMClient.getInstance().setDebugMode(true);
            //set events listeners
            setGlobalListener();
            HolloutLogger.d(TAG, "------- init hyphenate end --------------");
        }
    }

    /**
     * init sdk options
     */
    private EMOptions initOptions() {
        // set init sdk options
        EMOptions options = new EMOptions();
        // change to need confirm contact invitation
        options.setAcceptInvitationAlways(false);
        // set if need read ack
        options.setRequireAck(true);
        // set if need delivery ack
        options.setRequireDeliveryAck(true);
        // set auto accept group invitation
        options.setAutoAcceptGroupInvitation(false);
        //set gcm project number
        options.setGCMNumber(mContext.getString(R.string.gcm_project_number));
        return options;
    }

    /**
     * init call options
     */
    private void initCallOptions() {
        // set video call bitrate, default(150)
        EMClient.getInstance().callManager().getCallOptions().setMaxVideoKbps(800);
        // set video call resolution, default(320, 240)
        EMClient.getInstance().callManager().getCallOptions().setVideoResolution(640, 480);
        // send push notification when user offline
        EMClient.getInstance().callManager().getCallOptions().setIsSendPushIfOffline(true);
    }

    public void signUpEMClient(final String account, final String password, final DoneCallback<Boolean> authenticationCallback) {
        execute(new Runnable() {
            @Override
            public void run() {
                try {
                    EMClient.getInstance().createAccount(account.trim().toLowerCase(), password.trim().toLowerCase());
                    authenticationCallback.done(true, null);
                } catch (HyphenateException e) {
                    authenticationCallback.done(false, e);
                }
            }
        });
    }

    public void logInEMClient(String account, String password, final DoneCallback<Boolean> authenticationCallback) {

        EMClient.getInstance().login(account.trim().toLowerCase(), password.trim().toLowerCase(), new EMCallBack() {

            @Override
            public void onSuccess() {
                authenticationCallback.done(true, null);
            }

            @Override
            public void onError(int code, String error) {
                authenticationCallback.done(true, null);
            }

            @Override
            public void onProgress(int progress, String status) {

            }

        });

    }

    /**
     * Set Connection Listener
     */
    private void setConnectionListener() {

        if (mConnectionListener != null) {
            EMClient.getInstance().removeConnectionListener(mConnectionListener);
            mConnectionListener = null;
        }

        mConnectionListener = new EMConnectionListener() {

            /**
             * The connection to the server is successful
             */
            @Override
            public void onConnected() {
                HolloutLogger.d(TAG, "Connected to chat server");
            }

            /**
             * Disconnected from the server
             *
             * @param errorCode Disconnected error code
             */
            @Override
            public void onDisconnected(int errorCode) {
                HolloutLogger.d(TAG, "Disconnected from chat server because of error code = " + errorCode);
                if (errorCode == EMError.USER_LOGIN_ANOTHER_DEVICE) {
                    onConnectionConflict();
                }
            }

        };

        EMClient.getInstance().addConnectionListener(mConnectionListener);

    }

    private MessageNotifier getNotifier() {
        return mNotifier;
    }

    /**
     * user has logged into another device
     */
    private void onConnectionConflict() {
        Intent intent = new Intent(mContext, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(AppConstants.ACCOUNT_CONFLICT, true);
        mContext.startActivity(intent);
    }

    /**
     * init global listener
     */
    private void setGlobalListener() {
        // set call listener
        setCallReceiverListener();
        // set connection listener
        setConnectionListener();
        // register message listener
        registerMessageListener();
        // register contacts listener
        registerContactsListener();
        registerGroupListener();
        addCallStateChangeListener();
    }

    /**
     * Sign out account
     *
     * @param callback to receive the result of the logout
     */
    public void signOut(boolean unbindDeviceToken, final EMCallBack callback) {
        HolloutLogger.d(TAG, "Sign out: " + unbindDeviceToken);
        EMClient.getInstance().logout(unbindDeviceToken, new EMCallBack() {

            @Override
            public void onSuccess() {
                HolloutLogger.d(TAG, "Sign out: onSuccess");
                if (callback != null) {
                    callback.onSuccess();
                }
            }

            @Override
            public void onProgress(int progress, String status) {
                if (callback != null) {
                    callback.onProgress(progress, status);
                }
            }

            @Override
            public void onError(int code, String error) {
                HolloutLogger.d(TAG, "Sign out: onSuccess");
                if (callback != null) {
                    callback.onError(code, error);
                }
            }
        });
    }

    /**
     * new messages listener
     * If this event already handled by an activity, you don't need handle it again
     * activityList.size() <= 0 means all activities already in background or not in Activity Stack
     */
    private void registerMessageListener() {

        if (messageListener != null) {
            EMClient.getInstance().chatManager().removeMessageListener(messageListener);
            messageListener = null;
        }

        messageListener = new EMMessageListener() {

            @Override
            public void onMessageReceived(List<EMMessage> messages) {
                for (EMMessage message : messages) {
                    HolloutLogger.d(TAG, "onMessageReceived id : " + message.getMsgId());
                    // in background, do not refresh UI, notify it in notification bar
                    ParseUser signedInUser = ParseUser.getCurrentUser();
                    if (signedInUser != null) {
                        String signedInUserStatus = signedInUser.getString(AppConstants.APP_USER_ONLINE_STATUS);
                        if (!signedInUserStatus.equals(AppConstants.ONLINE)) {
                            getNotifier().onNewMsg(message);
                        } else {
                            EventBus.getDefault().post(new MessageReceivedEvent(message));
                        }
                    }
                }
            }

            @Override
            public void onCmdMessageReceived(List<EMMessage> messages) {
                for (EMMessage message : messages) {
                    EMLog.d(TAG, "onCmdMessageReceived");
                    //get message body
                    EMCmdMessageBody cmdMsgBody = (EMCmdMessageBody) message.getBody();
                    final String action = cmdMsgBody.action();
                    //get extension attribute if you need
                    //message.getStringAttribute("");
                    EMLog.d(TAG, String.format("CmdMessage：action:%s,message:%s", action,
                            message.toString()));
                }
            }

            @Override
            public void onMessageRead(List<EMMessage> messages) {
                for (EMMessage emMessage : messages) {
                    EventBus.getDefault().post(new MessageReadEvent(emMessage));
                }
            }

            @Override
            public void onMessageDelivered(List<EMMessage> messages) {
                for (EMMessage emMessage : messages) {
                    EventBus.getDefault().post(new MessageDeliveredEvent(emMessage));
                }
            }

            @Override
            public void onMessageChanged(EMMessage message, Object change) {
                EventBus.getDefault().post(new MessageChangedEvent(message));
            }

        };

        EMClient.getInstance().chatManager().addMessageListener(messageListener);
    }

    /**
     * Register contacts listener
     * Listen for changes to contacts
     */
    private void registerContactsListener() {
        if (mContactListener != null) {
            EMClient.getInstance().contactManager().removeContactListener(mContactListener);
            mContactListener = null;
        }
        mContactListener = new ContactsChangeListener();

        EMClient.getInstance().contactManager().setContactListener(mContactListener);
    }

    private class ContactsChangeListener implements EMContactListener {

        @Override
        public void onContactAdded(String username) {

        }

        @Override
        public void onContactDeleted(String username) {

        }

        @Override
        public void onContactInvited(String username, String reason) {

        }

        @Override
        public void onFriendRequestAccepted(String username) {

        }

        @Override
        public void onFriendRequestDeclined(String username) {

        }

    }

    private void registerGroupListener() {
        if (mGroupListener != null) {
            EMClient.getInstance().groupManager().removeGroupChangeListener(mGroupListener);
            mGroupListener = null;
        }
        mGroupListener = new GroupChangeListener();
        EMClient.getInstance().groupManager().addGroupChangeListener(mGroupListener);
    }

    public class GroupChangeListener implements EMGroupChangeListener {

        @Override
        public void onInvitationReceived(String groupId, String groupName, String inviter, String reason) {

        }

        @Override
        public void onRequestToJoinReceived(String groupId, String groupName, String applicant, String reason) {

        }

        @Override
        public void onRequestToJoinAccepted(String groupId, String groupName, String accepter) {

        }

        @Override
        public void onRequestToJoinDeclined(String groupId, String groupName, String decliner, String reason) {

        }

        @Override
        public void onInvitationAccepted(String groupId, String invitee, String reason) {

        }

        @Override
        public void onInvitationDeclined(String groupId, String invitee, String reason) {

        }

        @Override
        public void onUserRemoved(String groupId, String groupName) {

        }

        @Override
        public void onGroupDestroyed(String groupId, String groupName) {

        }

        @Override
        public void onAutoAcceptInvitationFromGroup(String groupId, String inviter, String inviteMessage) {

        }

        @Override
        public void onMuteListAdded(String groupId, List<String> mutes, long muteExpire) {

        }

        @Override
        public void onMuteListRemoved(String groupId, List<String> mutes) {

        }

        @Override
        public void onAdminAdded(String groupId, String administrator) {

        }

        @Override
        public void onAdminRemoved(String groupId, String administrator) {

        }

        @Override
        public void onOwnerChanged(String groupId, String newOwner, String oldOwner) {

        }

        @Override
        public void onMemberJoined(String groupId, String member) {

        }

        @Override
        public void onMemberExited(String groupId, String member) {

        }

        @Override
        public void onAnnouncementChanged(String groupId, String announcement) {

        }

        @Override
        public void onSharedFileAdded(String groupId, EMMucSharedFile sharedFile) {

        }

        @Override
        public void onSharedFileDeleted(String groupId, String fileId) {

        }

    }

    /**
     * Set call broadcast listener
     */
    private void setCallReceiverListener() {
        // Set the call broadcast listener to filter the action
        IntentFilter callFilter = new IntentFilter(
                EMClient.getInstance().callManager().getIncomingCallBroadcastAction());
        if (mCallReceiver != null) {
            mContext.unregisterReceiver(mCallReceiver);
            mCallReceiver = null;
        }
        mCallReceiver = new CallReceiver();
        // Register the call receiver
        mContext.registerReceiver(mCallReceiver, callFilter);
    }

    /**
     * Add call state listener
     */
    public void addCallStateChangeListener() {
        if (mCallStateChangeListener != null) {
            EMClient.getInstance().callManager().removeCallStateChangeListener(mCallStateChangeListener);
            mCallStateChangeListener = null;
        }
        mCallStateChangeListener = new CallStateChangeListener(mContext);
        EMClient.getInstance().callManager().addCallStateChangeListener(mCallStateChangeListener);
    }

}
