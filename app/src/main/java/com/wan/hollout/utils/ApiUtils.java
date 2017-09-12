package com.wan.hollout.utils;

import android.support.annotation.NonNull;

import com.wan.hollout.BuildConfig;
import com.wan.hollout.callbacks.DoneCallback;
import com.wan.hollout.components.ApplicationLoader;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
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

    private static String TAG = ApiUtils.class.getSimpleName();

    private static final String GIPHY_BASE_END_POINT = "https://api.giphy.com/v1";

    public static void fetchTrendingGifs(int page, final DoneCallback<List<JSONObject>> gifsResultCallback) {

        OkHttpClient okHttpClient = ApplicationLoader.getOkHttpClientBuilder().build();
        HttpUrl httpUrl = HttpUrl.parse(GIPHY_BASE_END_POINT + "/gifs/trending");

        if (httpUrl != null) {
            HttpUrl.Builder urlBuilder = httpUrl.newBuilder();
            attachApiKey(urlBuilder);
            urlBuilder.addQueryParameter("limit", "25");
            urlBuilder.addQueryParameter("rating", "G");
            urlBuilder.addQueryParameter("offset", String.valueOf(page));

            Request request = new Request.Builder().url(urlBuilder.build().toString()).get().build();

            okHttpClient.newCall(request).enqueue(new Callback() {

                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    gifsResultCallback.done(null, new Exception(e));
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    prepareResponse(response, gifsResultCallback);
                }
            });
        }
    }

    private static void prepareResponse(@NonNull Response response, DoneCallback<List<JSONObject>> gifsResultCallback) throws IOException {
        if (response.isSuccessful()) {
            ResponseBody responseBody = response.body();
            if (responseBody != null) {
                String responseBodyString = responseBody.string();
                if (StringUtils.isNotEmpty(responseBodyString)) {
                    HolloutLogger.d(TAG, "Giphy Trending  Success Response = " + responseBodyString);
                    try {
                        JSONObject responseObject = new JSONObject(responseBodyString);
                        JSONArray responseData = responseObject.optJSONArray("data");
                        if (responseData != null) {
                            List<JSONObject> responseDataList = new ArrayList<>();
                            for (int i = 0; i < responseData.length(); i++) {
                                JSONObject responseItemObject = responseData.optJSONObject(i);
                                if (responseItemObject != null) {
                                    String objectType = responseItemObject.optString("type");
                                    if (objectType.equals("gif")) {
                                        responseDataList.add(responseItemObject);
                                    }
                                }
                            }
                            gifsResultCallback.done(responseDataList, null);
                        } else {
                            gifsResultCallback.done(null, new Exception("No gifs found to load"));
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                } else {
                    HolloutLogger.d(TAG, "Giphy Trending  Failure Response = " + responseBodyString);
                }
            } else {
                gifsResultCallback.done(null, new Exception("No gifs were returned"));
            }
        } else {
            HolloutLogger.d(TAG, "Giphy Trending Null Response = " + response.message());
        }
    }

    private static void attachApiKey(HttpUrl.Builder builder) {
        builder.addQueryParameter("api_key", BuildConfig.DEBUG ? AppKeys.GIPHY_DEV_KEY : AppKeys.GIPHY_PRODUCTION_KEY);
    }

    public static void searchGif(String searchKey, int page, final DoneCallback<List<JSONObject>> doneCallback) {

        OkHttpClient okHttpClient = ApplicationLoader.getOkHttpClientBuilder().build();

        HttpUrl httpUrl = HttpUrl.parse(GIPHY_BASE_END_POINT + "/gifs/search");

        if (httpUrl != null) {
            HttpUrl.Builder urlBuilder = httpUrl.newBuilder();
            attachApiKey(urlBuilder);
            urlBuilder.addQueryParameter("q",searchKey);
            urlBuilder.addQueryParameter("limit", "25");
            urlBuilder.addQueryParameter("rating", "G");
            urlBuilder.addQueryParameter("lang","en");
            urlBuilder.addQueryParameter("offset", String.valueOf(page));

            Request request = new Request.Builder().url(urlBuilder.build().toString()).get().build();

            okHttpClient.newCall(request).enqueue(new Callback() {

                @Override
                public void onFailure(@NonNull Call call, @NonNull IOException e) {
                    doneCallback.done(null,e);
                }

                @Override
                public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                    prepareResponse(response, doneCallback);

                }

            });

        }

    }

}
