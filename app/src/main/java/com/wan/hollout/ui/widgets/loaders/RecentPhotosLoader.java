package com.wan.hollout.ui.widgets.loaders;


import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v4.content.CursorLoader;

import java.lang.ref.WeakReference;

public class RecentPhotosLoader extends CursorLoader {

    public static Uri EXTERNAL_DIRECTORY_IMAGES_URI = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

    private WeakReference<Context> contextWeakReference;

    private static final String[] PROJECTION = new String[]{
            MediaStore.Images.ImageColumns._ID,
            MediaStore.Images.ImageColumns.DATE_TAKEN,
            MediaStore.Images.ImageColumns.DATE_MODIFIED,
            MediaStore.Images.ImageColumns.ORIENTATION,
            MediaStore.Images.ImageColumns.MIME_TYPE
    };

    public RecentPhotosLoader(Context context) {
        super(context);
        contextWeakReference = new WeakReference<>(context);
    }

    @Override
    public Cursor loadInBackground() {
        Context context = contextWeakReference.get();
        if (context == null) {
            return null;
        }
        Cursor cursor = context.getContentResolver().query(EXTERNAL_DIRECTORY_IMAGES_URI,
                PROJECTION, null, null,
                MediaStore.Images.ImageColumns.DATE_TAKEN + " DESC");
        if (cursor != null) {
            cursor.setNotificationUri(context.getContentResolver(), EXTERNAL_DIRECTORY_IMAGES_URI);
            return cursor;
        }
        return null;
    }

}
