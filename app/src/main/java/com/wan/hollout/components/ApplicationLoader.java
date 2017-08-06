package com.wan.hollout.components;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.support.annotation.NonNull;
import android.support.multidex.MultiDex;

import com.crashlytics.android.Crashlytics;
import com.facebook.appevents.AppEventsLogger;
import com.raizlabs.android.dbflow.config.FlowManager;
import com.wan.hollout.eventbuses.ConnectivityChangedAction;
import com.wan.hollout.utils.HolloutLogger;

import org.greenrobot.eventbus.EventBus;

import io.fabric.sdk.android.Fabric;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;

/**
 * @author Wan Clem
 */

public class ApplicationLoader extends Application {

    private static ApplicationLoader sInstance;
//    private static ParseLiveQueryClient parseLiveQueryClient;

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
//        initParse();
        Fabric.with(this, new Crashlytics());
        FlowManager.init(this);
    }

    public static synchronized ApplicationLoader getInstance() {
        return sInstance;
    }

//    public static ParseLiveQueryClient getParseLiveQueryClient() {
//        return parseLiveQueryClient;
//    }

    public static OkHttpClient.Builder getOkHttpClientBuilder() {
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor(new HttpLoggingInterceptor.Logger() {
            @Override
            public void log(@NonNull String message) {
                HolloutLogger.d("ClobbitNetworkCall", message);
            }
        });
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);
        return new OkHttpClient.Builder()
                .retryOnConnectionFailure(false)
                .addInterceptor(logging);
    }

//    private void initParse() {
//        Thread thread = new Thread() {
//            @Override
//            public void run() {
//                super.run();
//                Parse.initialize(new Parse.Configuration.Builder(ApplicationLoader.this)
//                        .applicationId(AppKeys.APPLICATION_ID) // should correspond to APP_ID env variable
//                        .clientKey(AppKeys.SERVER_CLIENT_KEY)  // set explicitly blank unless clientKey is configured on Parse server
//                        .server(AppKeys.SERVER_ENDPOINT)
//                        .enableLocalDataStore()
//                        .clientBuilder(getOkHttpClientBuilder())
//                        .build());
//                try {
//                    parseLiveQueryClient = ParseLiveQueryClient.Factory.getClient(new URI(AppKeys.SERVER_ENDPOINT));
//                } catch (URISyntaxException e) {
//                    HolloutLogger.d("ParseLiveQueryClient", "Exception = " + e.getMessage());
//                    e.printStackTrace();
//                }
//
//            }
//        };
//        thread.start();
//    }

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

}
