package com.wan.hollout.components;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.multidex.MultiDex;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.crashlytics.android.Crashlytics;
import com.google.android.gms.ads.MobileAds;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.mikepenz.iconics.IconicsDrawable;
import com.mikepenz.materialdrawer.util.AbstractDrawerImageLoader;
import com.mikepenz.materialdrawer.util.DrawerImageLoader;
import com.mikepenz.materialdrawer.util.DrawerUIUtils;
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
import com.wan.hollout.BuildConfig;
import com.wan.hollout.R;
import com.wan.hollout.clients.CallClient;
import com.wan.hollout.clients.ChatClient;
import com.wan.hollout.database.HolloutDb;
import com.wan.hollout.eventbuses.ConnectivityChangedAction;
import com.wan.hollout.ui.services.AppInstanceDetectionService;
import com.wan.hollout.utils.AppConstants;
import com.wan.hollout.utils.AppKeys;
import com.wan.hollout.utils.AuthUtil;
import com.wan.hollout.utils.FirebaseUtils;
import com.wan.hollout.utils.HolloutLogger;
import com.wan.hollout.utils.HolloutPreferences;

import org.greenrobot.eventbus.EventBus;

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
        initDrawer();
        initAdmob();
        Fabric.with(this, new Crashlytics());
        setupDatabase();
        startAppInstanceDetector();
        defaultSystemEmojiPref();
        listenForServerTimeChanges();
    }

    private void listenForServerTimeChanges() {
        FirebaseUtils.getServerUpTimeRef().addValueEventListener(new ValueEventListener() {

            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                if (dataSnapshot != null && dataSnapshot.exists()) {
                    Long currentTime = dataSnapshot.getValue(Long.class);
                    ParseObject signedInUser = AuthUtil.getCurrentUser();
                    if (signedInUser != null) {
                        signedInUser.put(AppConstants.USER_CURRENT_TIME_STAMP, currentTime);
                        AuthUtil.updateCurrentLocalUser(signedInUser, null);
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void initAdmob() {
        MobileAds.initialize(this,
                BuildConfig.DEBUG ? AppKeys.DEBUG_AD_APP_ID
                        : AppKeys.PRODUCTION_AD_APP_ID);
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

    private void initDrawer() {
        DrawerImageLoader.init(new AbstractDrawerImageLoader() {

            @Override
            public void set(ImageView imageView, Uri uri, Drawable placeholder, String tag) {
                Glide.with(imageView.getContext()).load(uri).placeholder(placeholder).into(imageView);
            }

            @Override
            public void cancel(ImageView imageView) {
                Glide.clear(imageView);
            }

            @Override
            public Drawable placeholder(Context ctx, String tag) {
                if (DrawerImageLoader.Tags.PROFILE.name().equals(tag)) {
                    return DrawerUIUtils.getPlaceHolder(ctx);
                } else if (DrawerImageLoader.Tags.ACCOUNT_HEADER.name().equals(tag)) {
                    return new IconicsDrawable(ctx).iconText(" ").backgroundColorRes(com.mikepenz.materialdrawer.R.color.primary).sizeDp(56);
                } else if ("customUrlItem".equals(tag)) {
                    return new IconicsDrawable(ctx).iconText(" ").backgroundColorRes(R.color.md_red_500).sizeDp(56);
                }
                return super.placeholder(ctx, tag);
            }
        });
    }

    private void startAppInstanceDetector() {
        try {
            Intent serviceIntent = new Intent(this, AppInstanceDetectionService.class);
            startService(serviceIntent);
        } catch (IllegalStateException ignored) {

        }

    }

    private void initParse() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                configureParse();
            }
        }).start();
    }

    private void configureParse() {
        Parse.enableLocalDatastore(this);
        Parse.initialize(new Parse.Configuration.Builder(ApplicationLoader.this)
                .applicationId(AppKeys.APPLICATION_ID)
                .clientKey(AppKeys.SERVER_CLIENT_KEY)
                .server(AppKeys.SERVER_ENDPOINT)
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
