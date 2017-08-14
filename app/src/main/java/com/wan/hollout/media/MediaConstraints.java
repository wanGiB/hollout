package com.wan.hollout.media;

import android.content.Context;

public abstract class MediaConstraints {
    private static final String TAG = MediaConstraints.class.getSimpleName();

    public static MediaConstraints PUSH_CONSTRAINTS = new PushMediaConstraints();

    public abstract int getImageMaxWidth(Context context);

    public abstract int getImageMaxHeight(Context context);

    public abstract int getImageMaxSize();

    public abstract int getGifMaxSize();

    public abstract int getVideoMaxSize();

    public abstract int getAudioMaxSize();

  /*  public boolean isSatisfied(@NonNull Context context, @NonNull EMMessage message) {
        return false;
    }*/

}
