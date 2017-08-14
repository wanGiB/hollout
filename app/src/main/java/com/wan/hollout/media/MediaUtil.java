package com.wan.hollout.media;

import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.wan.hollout.utils.BitmapUtil;

import java.io.InputStream;

public class MediaUtil {

    private static final String TAG = MediaUtil.class.getSimpleName();

    public static boolean isMms(String contentType) {
        return !TextUtils.isEmpty(contentType) && contentType.trim().equals("application/mms");
    }

    public static boolean isGif(String contentType) {
        return !TextUtils.isEmpty(contentType) && contentType.trim().equals("image/gif");
    }

    public static
    @Nullable
    String getDiscreteMimeType(@NonNull String mimeType) {
        final String[] sections = mimeType.split("/", 2);
        return sections.length > 1 ? sections[0] : null;
    }

    public static class ThumbnailData {
        Bitmap bitmap;
        float aspectRatio;

        public ThumbnailData(Bitmap bitmap) {
            this.bitmap = bitmap;
            this.aspectRatio = (float) bitmap.getWidth() / (float) bitmap.getHeight();
        }

        public Bitmap getBitmap() {
            return bitmap;
        }

        public float getAspectRatio() {
            return aspectRatio;
        }

        public InputStream toDataStream() {
            return BitmapUtil.toCompressedJpeg(bitmap);
        }
    }

}
