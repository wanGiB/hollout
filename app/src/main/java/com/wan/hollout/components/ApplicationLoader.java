package com.wan.hollout.components;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.support.annotation.NonNull;
import android.support.multidex.MultiDex;

import com.afollestad.appthemeengine.ATE;
import com.crashlytics.android.Crashlytics;
import com.facebook.appevents.AppEventsLogger;
import com.parse.LiveQueryException;
import com.parse.Parse;
import com.parse.ParseLiveQueryClient;
import com.parse.ParseLiveQueryClientCallbacks;
import com.raizlabs.android.dbflow.config.FlowManager;
import com.wan.hollout.R;
import com.wan.hollout.chat.HolloutCommunicationsManager;
import com.wan.hollout.eventbuses.ConnectivityChangedAction;
import com.wan.hollout.ui.services.AppInstanceDetectionService;
import com.wan.hollout.utils.AppConstants;
import com.wan.hollout.utils.AppKeys;
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
            if (intent.getAction().equals(ConnectivityManager.CONNECTIVITY_ACTION)) {
                EventBus.getDefault().post(new ConnectivityChangedAction(true));
            }
        }

    };

    @Override
    public void onCreate() {
        super.onCreate();
        sInstance = this;
        AppEventsLogger.activateApp(this);
        initParse();
        Fabric.with(this, new Crashlytics());
        FlowManager.init(this);
        configureThemes();
        startAppInstanceDetector();
        defaultSystemEmojiPref();
        HolloutCommunicationsManager.getInstance().init(this);
    }

    private void startAppInstanceDetector() {
        Intent serviceIntent = new Intent(this, AppInstanceDetectionService.class);
        startService(serviceIntent);
    }

    private void initParse() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                initWebGuys();
            }
        }).start();
    }

    private void initWebGuys() {
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

    private void configureThemes() {
        if (!ATE.config(this, "light_theme").isConfigured()) {
            ATE.config(this, "light_theme")
                    .activityTheme(R.style.AppThemeLight)
                    .primaryColorRes(R.color.colorPrimaryLightDefault)
                    .accentColorRes(R.color.colorAccentLightDefault)
                    .coloredNavigationBar(false)
                    .usingMaterialDialogs(true)
                    .commit();
        }
        if (!ATE.config(this, "dark_theme").isConfigured()) {
            ATE.config(this, "dark_theme")
                    .activityTheme(R.style.AppThemeDark)
                    .primaryColorRes(R.color.colorPrimaryDarkDefault)
                    .accentColorRes(R.color.colorAccentDarkDefault)
                    .coloredNavigationBar(false)
                    .usingMaterialDialogs(true)
                    .commit();
        }
        if (!ATE.config(this, "light_theme_notoolbar").isConfigured()) {
            ATE.config(this, "light_theme_notoolbar")
                    .activityTheme(R.style.AppThemeLight)
                    .coloredActionBar(false)
                    .primaryColorRes(R.color.colorPrimaryLightDefault)
                    .accentColorRes(R.color.colorAccentLightDefault)
                    .coloredNavigationBar(false)
                    .usingMaterialDialogs(true)
                    .commit();
        }
        if (!ATE.config(this, "dark_theme_notoolbar").isConfigured()) {
            ATE.config(this, "dark_theme_notoolbar")
                    .activityTheme(R.style.AppThemeDark)
                    .coloredActionBar(false)
                    .primaryColorRes(R.color.colorPrimaryDarkDefault)
                    .accentColorRes(R.color.colorAccentDarkDefault)
                    .coloredNavigationBar(true)
                    .usingMaterialDialogs(true)
                    .commit();
        }
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
