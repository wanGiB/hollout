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
import com.facebook.appevents.AppEventsLogger;
import com.hyphenate.chat.EMMessage;
import com.mikepenz.iconics.IconicsDrawable;
import com.mikepenz.materialdrawer.util.AbstractDrawerImageLoader;
import com.mikepenz.materialdrawer.util.DrawerImageLoader;
import com.mikepenz.materialdrawer.util.DrawerUIUtils;
import com.parse.LiveQueryException;
import com.parse.Parse;
import com.parse.ParseLiveQueryClient;
import com.parse.ParseLiveQueryClientCallbacks;
import com.raizlabs.android.dbflow.config.FlowManager;
import com.wan.hollout.R;
import com.wan.hollout.callbacks.DoneCallback;
import com.wan.hollout.chat.HolloutCommunicationsManager;
import com.wan.hollout.eventbuses.ConnectivityChangedAction;
import com.wan.hollout.ui.services.AppInstanceDetectionService;
import com.wan.hollout.utils.AppConstants;
import com.wan.hollout.utils.AppKeys;
import com.wan.hollout.utils.HolloutLogger;
import com.wan.hollout.utils.HolloutPreferences;
import com.wan.hollout.utils.HolloutUtils;

import org.greenrobot.eventbus.EventBus;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

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
            if (intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
                EventBus.getDefault().post(new ConnectivityChangedAction(true));
                HolloutCommunicationsManager.getInstance().init(ApplicationLoader.getInstance());
            }
        }
    };

    @Override
    public void onCreate() {
        super.onCreate();
        sInstance = this;
        AppEventsLogger.activateApp(this);
        initParse();
        initDrawer();
        Fabric.with(this, new Crashlytics());
        FlowManager.init(this);
        startAppInstanceDetector();
        defaultSystemEmojiPref();
        HolloutCommunicationsManager.getInstance().init(this);

        HolloutUtils.deserializeMessages(AppConstants.UNREAD_MESSAGES, new DoneCallback<List<EMMessage>>() {

            @Override
            public void done(List<EMMessage> result, Exception e) {
                if (result != null && !result.isEmpty()) {
                    HolloutCommunicationsManager.getInstance().getNotifier().onNewMsg(result);
                }
            }

        });

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
                //define different placeholders for different imageView targets
                //default tags are accessible via the DrawerImageLoader.Tags
                //custom ones can be checked via string. see the CustomUrlBasePrimaryDrawerItem LINE 111
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
        Intent serviceIntent = new Intent(this, AppInstanceDetectionService.class);
        startService(serviceIntent);
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
        Parse.initialize(new Parse.Configuration.Builder(ApplicationLoader.this)
                .applicationId(AppKeys.APPLICATION_ID) // should correspond to APP_ID env variable
                .clientKey(AppKeys.SERVER_CLIENT_KEY)  // set explicitly blank unless clientKey is configured on Parse server
                .server(AppKeys.SERVER_ENDPOINT)
                .enableLocalDataStore()
                .clientBuilder(getOkHttpClientBuilder())
                .build());
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
                HolloutPreferences.defaultToSystemEmojis(AppConstants.SYSTEM_EMOJI_PREF, false);
            }
        }).start();
    }

    private void persistReactionsToLocalDatabase() {

    }

}
