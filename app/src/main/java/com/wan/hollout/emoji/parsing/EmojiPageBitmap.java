package com.wan.hollout.emoji.parsing;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.support.annotation.NonNull;
import android.util.Log;

import com.wan.hollout.emoji.EmojiPageModel;
import com.wan.hollout.emoji.concurrent.ListenableFutureTask;
import com.wan.hollout.exceptions.BitmapDecodingException;
import com.wan.hollout.utils.BitmapUtil;
import com.wan.hollout.utils.HolloutUtils;

import java.io.IOException;
import java.lang.ref.SoftReference;
import java.util.concurrent.Callable;

public class EmojiPageBitmap {

  private static final String TAG = EmojiPageBitmap.class.getName();

  private final Context        context;
  private final EmojiPageModel model;
  private final float          decodeScale;

  private SoftReference<Bitmap>        bitmapReference;
  private ListenableFutureTask<Bitmap> task;

  public EmojiPageBitmap(@NonNull Context context, @NonNull EmojiPageModel model, float decodeScale) {
    this.context     = context.getApplicationContext();
    this.model       = model;
    this.decodeScale = decodeScale;
  }

  public ListenableFutureTask<Bitmap> get() {
    HolloutUtils.assertMainThread();

    if (bitmapReference != null && bitmapReference.get() != null) {
      return new ListenableFutureTask<>(bitmapReference.get());
    } else if (task != null) {
      return task;
    } else {
      Callable<Bitmap> callable = new Callable<Bitmap>() {
        @Override public Bitmap call() throws Exception {
          try {
            Log.w(TAG, "loading page " + model.getSprite());
            return loadPage();
          } catch (IOException ioe) {
            Log.w(TAG, ioe);
          }
          return null;
        }
      };
      task = new ListenableFutureTask<>(callable);
      new AsyncTask<Void, Void, Void>() {
        @Override protected Void doInBackground(Void... params) {
          task.run();
          return null;
        }

        @Override protected void onPostExecute(Void aVoid) {
          task = null;
        }
      }.execute();
    }
    return task;
  }

  private Bitmap loadPage() throws IOException {
    if (bitmapReference != null && bitmapReference.get() != null) return bitmapReference.get();

    try {
      final Bitmap bitmap = BitmapUtil.createScaledBitmap(context,
                                                          "file:///android_asset/" + model.getSprite(),
                                                          decodeScale);
      bitmapReference = new SoftReference<>(bitmap);
      Log.w(TAG, "onPageLoaded(" + model.getSprite() + ")");
      return bitmap;
    } catch (BitmapDecodingException e) {
      Log.w(TAG, e);
      throw new IOException(e);
    }
  }

  @Override
  public String toString() {
    return model.getSprite();
  }
}
