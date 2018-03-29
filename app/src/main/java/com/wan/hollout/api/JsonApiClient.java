package com.wan.hollout.api;

import android.support.annotation.NonNull;

import com.wan.hollout.interfaces.DoneCallback;
import com.wan.hollout.utils.AppConstants;
import com.wan.hollout.utils.AuthUtil;
import com.wan.hollout.utils.HolloutLogger;
import com.wan.hollout.utils.UiUtils;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.logging.HttpLoggingInterceptor;

/**
 * @author Wan Clem
 */

@SuppressWarnings({"unchecked", "ConstantConditions"})
public class JsonApiClient {

    private static MediaType CONTENT_TYPE = MediaType.parse("application/json; charset=utf-8");

    private static OkHttpClient.Builder getOkHttpClientBuilder() {
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor(new HttpLoggingInterceptor.Logger() {
            @Override
            public void log(@NonNull String message) {
                HolloutLogger.d("HolloutNetworkCallLogger", message);
            }
        });
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);
        return new OkHttpClient.Builder()
                .retryOnConnectionFailure(false)
                .connectTimeout(60000, TimeUnit.MILLISECONDS)
                .readTimeout(60000, TimeUnit.MILLISECONDS)
                .addInterceptor(logging);
    }

    private static OkHttpClient getOkHttpClient() {
        return getOkHttpClientBuilder().build();
    }

    private static Request.Builder getRequestBuilder(HashMap<String, String> headers) {
        final Request.Builder requestBuilder = new Request.Builder();
        if (headers != null) {
            for (String key : headers.keySet()) {
                requestBuilder.addHeader(key, headers.get(key));
            }
        }
        return requestBuilder;
    }

    public static void sendFirebasePushNotification(String recipientToken, String category) {
        HttpUrl.Builder httpUrlBuilder = HttpUrl.parse("https://fcm.googleapis.com/fcm/send").newBuilder();
        JSONObject message = new JSONObject();
        JSONObject data = new JSONObject();
        try {
            data.put(AppConstants.NOTIFICATION_TYPE, category);
            data.put("title", "Hollout");
            if (AuthUtil.getCurrentUser() != null) {
                data.put(AppConstants.REAL_OBJECT_ID, AuthUtil.getCurrentUser().getString(AppConstants.REAL_OBJECT_ID));
            }
            message.put("to", recipientToken);
            message.put("data", data);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        String pushUrl = httpUrlBuilder.build().toString();
        HashMap<String, String> authParams = new HashMap<>();
        authParams.put("Content-Type", "application/json");
        authParams.put("Authorization", "key= " + getZZAKey());
        Request pushRequest = getRequestBuilder(authParams)
                .url(pushUrl)
                .post(RequestBody.create(CONTENT_TYPE, message.toString()))
                .build();
        getOkHttpClient().newCall(pushRequest).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                logResponse(e.getMessage(), e.hashCode());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                logResponse(getResponseString(response), getResponseCode(response));
            }
        });
    }

    private static void callBackOnMainThread(final DoneCallback doneCallback, final Object result, final Exception e) {
        UiUtils.runOnMain(new Runnable() {
            @Override
            public void run() {
                if (e != null) {
                    if (e.getMessage() != null) {
                        if (StringUtils.containsIgnoreCase(e.getMessage(), "host") || StringUtils.containsIgnoreCase(e.getMessage(), "ssl")) {
                            doneCallback.done(result, new Exception("Network Error! Please review your data connection and try again"));
                        } else {
                            HolloutLogger.d("MajorException", e.getMessage());
                            doneCallback.done(result, e);
                        }
                    } else {
                        doneCallback.done(result, new Exception("Error in operation!"));
                    }
                } else {
                    doneCallback.done(result, null);
                }
            }
        });
    }

    private static String getZZAKey() {
        return "AAAAvc3IQns:APA91bGJrm56rf3_LCdFnRgFrDdCB0FL41Ecl66HxueLOl5JxhQg3rLsdaeoqbgTApBIYxV9JJWl_9MYG9q3vCueJGcjxgtLbq_6pFMOWiObps6ULo7lp4RSECytMzEKU2LVBtYWq5dk";
    }

    private static int getResponseCode(@NonNull Response response) {
        return response.code();
    }

    private static String getResponseString(@NonNull Response response) throws IOException {
        try {
            ResponseBody responseBody = response.body();
            return responseBody.string();
        } catch (IllegalStateException ignored) {
            return "";
        }
    }

    private static void logResponse(String responseBodyString, int code) {
        HolloutLogger.d("FetchChats", responseBodyString + ", Response Code =" + code);
    }

}
