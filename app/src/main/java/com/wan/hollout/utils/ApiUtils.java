package com.wan.hollout.utils;

import android.support.annotation.NonNull;

import com.squareup.okhttp.HttpUrl;
import com.wan.hollout.callbacks.DoneCallback;
import com.wan.hollout.components.ApplicationLoader;
import com.wan.hollout.models.HolloutObject;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * @author Wan Clem
 */

public class ApiUtils {

    private static String BLOG_REQUESTS_TAG = "BlogRequest";

    private static OkHttpClient getOkHttpClient() {
        return ApplicationLoader.getOkHttpClientBuilder().build();
    }

    private static void withApiKey(HttpUrl.Builder builder) {
        builder.addQueryParameter("key", AppKeys.GOOGLE_API_KEY);
    }

    public static void fetchBlogPosts(String blogId, String pageToken, final DoneCallback<List<HolloutObject>> resultCallback) {
        OkHttpClient okHttpClient = getOkHttpClient();
        HttpUrl.Builder httpUrlBuilder = HttpUrl.parse(AppKeys.BASE_URL + "/" + blogId + "/posts").newBuilder();

        withApiKey(httpUrlBuilder);

        if (pageToken != null) {
            httpUrlBuilder.addQueryParameter("pageToken", pageToken);
        }

        Request request = new Request.Builder().url(httpUrlBuilder.build().toString()).get().build();

        okHttpClient.newCall(request).enqueue(new Callback() {

            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                HolloutLogger.d(BLOG_REQUESTS_TAG, e.getMessage());
                resultCallback.done(null, e);
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    ResponseBody responseBody = response.body();
                    if (responseBody != null) {
                        String responseBodyString = responseBody.string();
                        if (StringUtils.isNotEmpty(responseBodyString)) {
                            List<HolloutObject> blogPosts = new ArrayList<>();
                            try {
                                JSONObject jsonObject = new JSONObject(responseBodyString);
                                String receivedNextPageToken = jsonObject.optString("nextPageToken");
                                String receivedPreviousPageToken = jsonObject.optString("prevPageToken");
                                JSONArray items = jsonObject.optJSONArray("items");
                                if (items != null) {
                                    for (int i = 0; i < items.length(); i++) {
                                        JSONObject blogPost = items.optJSONObject(i);
                                        if (receivedNextPageToken != null) {
                                            blogPost.put(AppConstants.NEXT_PAGE_TOKEN, receivedNextPageToken);
                                        }
                                        if (receivedPreviousPageToken != null) {
                                            blogPost.put(AppConstants.PREVIOUS_PAGE_TOKEN, receivedPreviousPageToken);
                                        }
                                        HolloutObject holloutObject = new HolloutObject(blogPost);
                                        if (!blogPosts.contains(holloutObject)) {
                                            blogPosts.add(holloutObject);
                                        }
                                    }
                                    resultCallback.done(blogPosts, null);
                                } else {
                                    resultCallback.done(null, null);
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                                resultCallback.done(null, new Exception("Something Screwy happened"));
                            }
                        } else {
                            resultCallback.done(null, null);
                        }
                        HolloutLogger.d(BLOG_REQUESTS_TAG, responseBodyString);
                    } else {
                        HolloutLogger.d(BLOG_REQUESTS_TAG, "Something Screwy happened");
                        resultCallback.done(null, new Exception("Something Screwy happened"));
                    }
                } else {
                    HolloutLogger.d(BLOG_REQUESTS_TAG, response.message());
                    resultCallback.done(null, new Exception("Something Screwy happened"));
                }

            }

        });

    }

    public static void fetchPostComments(String blogId, String postId, String pageToken, final DoneCallback<List<HolloutObject>> resultCallback) {
        OkHttpClient okHttpClient = getOkHttpClient();
        HttpUrl.Builder httpUrlBuilder = HttpUrl.parse(AppKeys.BASE_URL + "/" + blogId + "/posts/" + postId + "/comments").newBuilder();

        withApiKey(httpUrlBuilder);

        if (pageToken != null) {
            httpUrlBuilder.addQueryParameter("pageToken", pageToken);
        }
        Request request = new Request.Builder().url(httpUrlBuilder.build().toString()).get().build();
        okHttpClient.newCall(request).enqueue(new Callback() {

            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                HolloutLogger.d(BLOG_REQUESTS_TAG, e.getMessage());
                resultCallback.done(null, e);
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    ResponseBody responseBody = response.body();
                    if (responseBody != null) {
                        String responseBodyString = responseBody.string();
                        if (StringUtils.isNotEmpty(responseBodyString)) {
                            List<HolloutObject> blogPosts = new ArrayList<>();
                            try {
                                JSONObject jsonObject = new JSONObject(responseBodyString);
                                String receivedNextPageToken = jsonObject.optString("nextPageToken");
                                String receivedPreviousPageToken = jsonObject.optString("prevPageToken");
                                JSONArray items = jsonObject.optJSONArray("items");
                                if (items != null) {
                                    for (int i = 0; i < items.length(); i++) {
                                        JSONObject blogPost = items.optJSONObject(i);
                                        if (receivedNextPageToken != null) {
                                            blogPost.put(AppConstants.NEXT_PAGE_TOKEN, receivedNextPageToken);
                                        }
                                        if (receivedPreviousPageToken != null) {
                                            blogPost.put(AppConstants.PREVIOUS_PAGE_TOKEN, receivedPreviousPageToken);
                                        }
                                        HolloutObject holloutObject = new HolloutObject(blogPost);
                                        if (!blogPosts.contains(holloutObject)) {
                                            blogPosts.add(holloutObject);
                                        }
                                    }
                                    resultCallback.done(blogPosts, null);
                                } else {
                                    resultCallback.done(null, null);
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                                resultCallback.done(null, new Exception("Something Screwy happened"));
                            }
                        } else {
                            resultCallback.done(null, null);
                        }
                        HolloutLogger.d(BLOG_REQUESTS_TAG, responseBody.string());
                    } else {
                        HolloutLogger.d(BLOG_REQUESTS_TAG, "Something Screwy happened");
                        resultCallback.done(null, new Exception("Something Screwy happened"));
                    }
                } else {
                    HolloutLogger.d(BLOG_REQUESTS_TAG, response.message());
                    resultCallback.done(null, new Exception("Something Screwy happened"));
                }

            }

        });

    }

    public static void searchForPost(String blogId, String searchString, String pageToken, final DoneCallback<List<HolloutObject>> resultCallback) {
        OkHttpClient okHttpClient = getOkHttpClient();
        HttpUrl.Builder httpUrlBuilder = HttpUrl.parse(AppKeys.BASE_URL + "/" + blogId + "/posts/search?q=" + searchString).newBuilder();
        withApiKey(httpUrlBuilder);
        if (pageToken != null) {
            httpUrlBuilder.addQueryParameter("pageToken", pageToken);
        }
        Request request = new Request.Builder().url(httpUrlBuilder.build().toString()).get().build();
        okHttpClient.newCall(request).enqueue(new Callback() {

            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                HolloutLogger.d(BLOG_REQUESTS_TAG, e.getMessage());
                resultCallback.done(null, e);
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    ResponseBody responseBody = response.body();
                    if (responseBody != null) {
                        String responseBodyString = responseBody.string();
                        if (StringUtils.isNotEmpty(responseBodyString)) {
                            List<HolloutObject> blogPosts = new ArrayList<>();
                            try {
                                JSONObject jsonObject = new JSONObject(responseBodyString);
                                String receivedNextPageToken = jsonObject.optString("nextPageToken");
                                String receivedPreviousPageToken = jsonObject.optString("prevPageToken");
                                JSONArray items = jsonObject.optJSONArray("items");
                                if (items != null) {
                                    for (int i = 0; i < items.length(); i++) {
                                        JSONObject blogPost = items.optJSONObject(i);
                                        if (receivedNextPageToken != null) {
                                            blogPost.put(AppConstants.NEXT_PAGE_TOKEN, receivedNextPageToken);
                                        }
                                        if (receivedPreviousPageToken != null) {
                                            blogPost.put(AppConstants.PREVIOUS_PAGE_TOKEN, receivedPreviousPageToken);
                                        }
                                        HolloutObject holloutObject = new HolloutObject(blogPost);
                                        if (!blogPosts.contains(holloutObject)) {
                                            blogPosts.add(holloutObject);
                                        }
                                    }
                                    resultCallback.done(blogPosts, null);
                                } else {
                                    resultCallback.done(null, null);
                                }
                            } catch (JSONException e) {
                                e.printStackTrace();
                                resultCallback.done(null, new Exception("Something Screwy happened"));
                            }
                        } else {
                            resultCallback.done(null, null);
                        }
                        HolloutLogger.d(BLOG_REQUESTS_TAG, responseBody.string());
                    } else {
                        HolloutLogger.d(BLOG_REQUESTS_TAG, "Something Screwy happened");
                        resultCallback.done(null, new Exception("Something Screwy happened"));
                    }
                } else {
                    HolloutLogger.d(BLOG_REQUESTS_TAG, response.message());
                    resultCallback.done(null, new Exception("Something Screwy happened"));
                }

            }

        });

    }

    public static void fetchBlogPost(String blogId, String postId, final DoneCallback<HolloutObject> doneCallback) {
        OkHttpClient okHttpClient = getOkHttpClient();
        HttpUrl.Builder httpUrlBuilder = HttpUrl.parse(AppKeys.BASE_URL + "/" + blogId + "/posts/" + postId).newBuilder();
        withApiKey(httpUrlBuilder);
        Request request = new Request.Builder().url(httpUrlBuilder.build().toString()).get().build();

        okHttpClient.newCall(request).enqueue(new Callback() {

            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                doneCallback.done(null, e);
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    ResponseBody responseBody = response.body();
                    if (responseBody != null) {
                        String responseBodyString = responseBody.string();
                        if (StringUtils.isNotEmpty(responseBodyString)) {
                            try {
                                JSONObject blogPost = new JSONObject(responseBodyString);
                                doneCallback.done(new HolloutObject(blogPost), null);
                            } catch (JSONException e) {
                                e.printStackTrace();
                                doneCallback.done(null, new Exception("Something screwy happened"));
                            }
                        } else {
                            doneCallback.done(null, new Exception("Something screwy happened"));
                        }
                    } else {
                        doneCallback.done(null, new Exception("Something screwy happened"));
                    }
                } else {
                    HolloutLogger.d(BLOG_REQUESTS_TAG, response.message());
                    doneCallback.done(null, new Exception("Something screwy happened"));
                }

            }

        });

    }

    public static void fetchBlogPostComment(String blogId, String postId, String commentId, final DoneCallback<HolloutObject> doneCallback) {
        OkHttpClient okHttpClient = getOkHttpClient();
        HttpUrl.Builder httpUrlBuilder = HttpUrl.parse(AppKeys.BASE_URL + "/" + blogId + "/posts/" + postId + "/comments/" + commentId).newBuilder();
        withApiKey(httpUrlBuilder);
        Request request = new Request.Builder().url(httpUrlBuilder.build().toString()).get().build();

        okHttpClient.newCall(request).enqueue(new Callback() {

            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                doneCallback.done(null, e);
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful()) {
                    ResponseBody responseBody = response.body();
                    if (responseBody != null) {
                        String responseBodyString = responseBody.string();
                        if (StringUtils.isNotEmpty(responseBodyString)) {
                            try {
                                JSONObject blogPost = new JSONObject(responseBodyString);
                                doneCallback.done(new HolloutObject(blogPost), null);
                            } catch (JSONException e) {
                                e.printStackTrace();
                                doneCallback.done(null, new Exception("Something screwy happened"));
                            }
                        } else {
                            doneCallback.done(null, new Exception("Something screwy happened"));
                        }
                    } else {
                        doneCallback.done(null, new Exception("Something screwy happened"));
                    }
                } else {
                    HolloutLogger.d(BLOG_REQUESTS_TAG, response.message());
                    doneCallback.done(null, new Exception("Something screwy happened"));
                }

            }

        });

    }

}
