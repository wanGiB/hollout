package com.wan.hollout.utils;

import android.support.annotation.NonNull;

import com.wan.hollout.BuildConfig;
import com.wan.hollout.callbacks.DoneCallback;
import com.wan.hollout.components.ApplicationLoader;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * @author Wan Clem
 */

public class ApiUtils {

    private static String TAG =ApiUtils.class.getSimpleName();

    private static final String GIPHY_BASE_END_POINT = "https://api.giphy.com/v1";

    public static void fetchTrendingGifs(DoneCallback<List<JSONObject>>gifsResultCallback) {

        OkHttpClient okHttpClient = ApplicationLoader.getOkHttpClientBuilder().build();

        HttpUrl httpUrl = HttpUrl.parse(GIPHY_BASE_END_POINT + "/gifs/trending");

        if (httpUrl!=null){

            HttpUrl.Builder urlBuilder = httpUrl.newBuilder();
            attachApiKey(urlBuilder);
            urlBuilder.addQueryParameter("limit","25");
            urlBuilder.addQueryParameter("rating","G");

            Request request = new Request.Builder().url(urlBuilder.build().toString()).get().build();

            okHttpClient.newCall(request).enqueue(new Callback() {

                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {

                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {

                    if (response.isSuccessful()){
                        ResponseBody responseBody = response.body();
                        if (responseBody!=null){
                            String responseBodyString = responseBody.string();
                            if (StringUtils.isNotEmpty(responseBodyString)){
                                HolloutLogger.d(TAG,"Giphy Trending  Success Response = "+responseBodyString);
                            }else{
                                HolloutLogger.d(TAG,"Giphy Trending  Failure Response = "+responseBodyString);
                            }
                        }
                    }else{
                        HolloutLogger.d(TAG,"Giphy Trending Null Response = "+response.message());
                    }

                }

            });

        }

    }

    private static void attachApiKey(HttpUrl.Builder builder) {
        builder.addQueryParameter("api_key", BuildConfig.DEBUG?AppKeys.GIPHY_DEV_KEY:AppKeys.GIPHY_PRODUCTION_KEY);
    }

}
