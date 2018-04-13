package com.wan.hollout.components;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.support.annotation.NonNull;
import android.support.multidex.MultiDex;
import android.support.v4.app.JobIntentService;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.parse.LiveQueryException;
import com.parse.Parse;
import com.parse.ParseLiveQueryClient;
import com.parse.ParseLiveQueryClientCallbacks;
import com.parse.ParseObject;
import com.raizlabs.android.dbflow.config.DatabaseConfig;
import com.raizlabs.android.dbflow.config.FlowConfig;
import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.runtime.DirectModelNotifier;
import com.tonyodev.fetch2.Fetch;
import com.vanniktech.emoji.EmojiManager;
import com.vanniktech.emoji.ios.IosEmojiProvider;
import com.wan.hollout.clients.CallClient;
import com.wan.hollout.clients.ChatClient;
import com.wan.hollout.database.HolloutDb;
import com.wan.hollout.eventbuses.ActivityCountChangedEvent;
import com.wan.hollout.eventbuses.ConnectivityChangedAction;
import com.wan.hollout.ui.services.AppInstanceDetectionService;
import com.wan.hollout.utils.AppConstants;
import com.wan.hollout.utils.AppKeys;
import com.wan.hollout.utils.AuthUtil;
import com.wan.hollout.utils.FirebaseUtils;
import com.wan.hollout.utils.HolloutLogger;
import com.wan.hollout.utils.HolloutPreferences;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.net.URI;
import java.net.URISyntaxException;

import io.fabric.sdk.android.Fabric;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;

/**
 * @author Wan Clem
 */

@SuppressWarnings("unused")
public class ApplicationLoader extends Application {

    private static ApplicationLoader sInstance;
    private static ParseLiveQueryClient parseLiveQueryClient;

    BroadcastReceiver connectivityChangedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent != null) {
                if (intent.getAction() != null) {
                    if (intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
                        EventBus.getDefault().post(new ConnectivityChangedAction(true));
                        ChatClient.getInstance().startChatClient();
                        CallClient.getInstance().startCallClient();
                    }
                }
            }
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        sInstance = this;
        initParse();
        Fabric.with(this, new Crashlytics());
        setupDatabase();
        startAppInstanceDetector();
        defaultSystemEmojiPref();
        checkAndRegEventBus();
        setupEmoji();
        fetchNewConfigData();
    }

    private void setupEmoji() {
        EmojiManager.install(new IosEmojiProvider());
    }

    @SuppressWarnings("unused")
    @Subscribe(sticky = true, threadMode = ThreadMode.ASYNC)
    public void onEventAsync(final Object o) {
        if (o instanceof ActivityCountChangedEvent) {
            int currentActivityCount = HolloutPreferences.getInstance().getInt(AppConstants.ACTIVITY_COUNT, 0);
            if (currentActivityCount <= 0) {
                ParseObject signedInUser = AuthUtil.getCurrentUser();
                if (signedInUser != null) {
                    signedInUser.put(AppConstants.APP_USER_ONLINE_STATUS, AppConstants.OFFLINE);
                    signedInUser.put(AppConstants.APP_USER_LAST_SEEN, System.currentTimeMillis());
                    signedInUser.put(AppConstants.USER_CURRENT_TIME_STAMP, System.currentTimeMillis());
                    AuthUtil.updateCurrentLocalUser(signedInUser, null);
                }
            }
        }
    }

    private void checkAndRegEventBus() {
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
    }

    public Fetch getMainFetch() {
        return new Fetch.Builder(this, "Main")
                .setDownloadConcurrentLimit(10) // Allows Fetch to download 10 downloads in Parallel.
                .enableLogging(true)
                .build();
    }

    private void setupDatabase() {
        FlowManager.init(new FlowConfig.Builder(this)
                .addDatabaseConfig(new DatabaseConfig.Builder(HolloutDb.class)
                        .modelNotifier(DirectModelNotifier.get())
                        .build()).build());
    }

    private void startAppInstanceDetector() {
        ParseObject signedInUserObject = AuthUtil.getCurrentUser();
        if (signedInUserObject != null && HolloutPreferences.canAccessLocation()) {
            try {
                Intent serviceIntent = new Intent();
                JobIntentService.enqueueWork(this, AppInstanceDetectionService.class, AppConstants.FIXED_JOB_ID, serviceIntent);
            } catch (IllegalStateException ignored) {

            }
        }
    }

    private void initParse() {
        configureParse();
    }

    private void configureParse() {
        FirebaseRemoteConfig firebaseRemoteConfig = FirebaseUtils.getRemoteConfig();
        Parse.enableLocalDatastore(this);
        Parse.initialize(new Parse.Configuration.Builder(ApplicationLoader.this)
                .applicationId(firebaseRemoteConfig.getString(AppConstants.PARSE_APPLICATION_ID))
                .clientKey(firebaseRemoteConfig.getString(AppConstants.PARSE_SERVER_CLIENT_KEY))
                .server(firebaseRemoteConfig.getString(AppConstants.PARSE_SERVER_ENDPOINT))
                .enableLocalDataStore()
                .clientBuilder(getOkHttpClientBuilder())
                .build());
        ChatClient.getInstance().startChatClient();
        CallClient.getInstance().startCallClient();
        try {
            parseLiveQueryClient = ParseLiveQueryClient.Factory.getClient(new URI(AppKeys.SERVER_ENDPOINT));
            parseLiveQueryClient.registerListener(new ParseLiveQueryClientCallbacks() {

                @Override
                public void onLiveQueryClientConnected(ParseLiveQueryClient client) {
                    HolloutLogger.d("ParseLiveQueryClient", "Client Connected");
                }

                @Override
                public void onLiveQueryClientDisconnected(ParseLiveQueryClient client, boolean userInitiated) {
                    attemptLiveQueryReconnection();
                }

                @Override
                public void onLiveQueryError(ParseLiveQueryClient client, LiveQueryException reason) {
                    attemptLiveQueryReconnection();
                }

                @Override
                public void onSocketError(ParseLiveQueryClient client, Throwable reason) {
                    attemptLiveQueryReconnection();
                }

            });
        } catch (URISyntaxException e) {
            HolloutLogger.d("ParseLiveQueryClient", "Exception = " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void attemptLiveQueryReconnection() {
        if (parseLiveQueryClient != null) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        parseLiveQueryClient.reconnect();
                    } catch (NullPointerException ignored) {
                    }
                }
            }).start();
        }
    }

    public static void fetchNewConfigData() {
        long cacheExpiration = 3600; // 1 hour in seconds.
        // If your app is using developer mode, cacheExpiration is set to 0, so each fetch will
        // retrieve values from the service.
        if (FirebaseUtils.getRemoteConfig().getInfo().getConfigSettings().isDeveloperModeEnabled()) {
            cacheExpiration = 0;
        }
        FirebaseUtils.getRemoteConfig().fetch(cacheExpiration).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    // After config data is successfully fetched, it must be activated before newly fetched
                    // values are returned.
                    FirebaseUtils.getRemoteConfig().activateFetched();
                } else {
                    HolloutLogger.d("FirebaseRemoteConfig", "Failed to fetch remote config data");
                }
            }
        });
    }

    public static synchronized ApplicationLoader getInstance() {
        return sInstance;
    }

    public static ParseLiveQueryClient getParseLiveQueryClient() {
        return parseLiveQueryClient;
    }

    public static OkHttpClient.Builder getOkHttpClientBuilder() {
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor(new HttpLoggingInterceptor.Logger() {
            @Override
            public void log(@NonNull String message) {
                HolloutLogger.d("HolloutNetworkCall", message);
            }
        });
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);
        return new OkHttpClient.Builder()
                .retryOnConnectionFailure(false)
                .addInterceptor(logging);
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        unregisterReceiver(connectivityChangedReceiver);
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    private void defaultSystemEmojiPref() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                HolloutPreferences.defaultToSystemEmojis(AppConstants.SYSTEM_EMOJI_PREF);
            }
        }).start();
    }

}
