package com.wan.hollout.utils;

import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;

import com.wan.hollout.ui.adapters.ImageAdapter;

/**
 * @author Wan Clem
 */
public class RecentImages {

    private static final String DESCENDING = " DESC";
    private static final String DATE_TAKEN = "datetaken";

    public ImageAdapter getAdapter(Context context) {
        return getAdapter(context, DATE_TAKEN, DESCENDING);
    }

    private ImageAdapter getAdapter(Context context, String columns, String sort) {
        Cursor mImageCursor = null;
        try {
            String[] projection = new String[]{MediaStore.Images.ImageColumns._ID, MediaStore.Images.ImageColumns.DATA,
                    MediaStore.Images.ImageColumns.BUCKET_DISPLAY_NAME, MediaStore.Images.ImageColumns.DATE_TAKEN, MediaStore.Images.ImageColumns.MIME_TYPE};
            mImageCursor = context.getContentResolver().query(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, projection, null, null, columns + sort);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ImageAdapter(context, mImageCursor);
    }

    public void setDrawable(int drawable) {
        ImageAdapter.DRAWABLE = drawable;
    }

    public void setHeight(int height) {
        ImageAdapter.IMAGE_HEIGHT = height;
    }

    public void setWidth(int width) {
        ImageAdapter.IMAGE_WIDTH = width;
    }

    public void setPadding(int padding) {
        ImageAdapter.IMAGE_PADDING = padding;
    }

    public void setSize(int size) {
        ImageAdapter.IN_SAMPLE_SIZE = size;
    }

}
