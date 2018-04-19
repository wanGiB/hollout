package com.wan.hollout.ui.adapters;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.provider.MediaStore;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.wan.hollout.R;
import com.wan.hollout.ui.widgets.BetterImageView;
import com.wan.hollout.ui.widgets.TwoWayAbsListView;

@SuppressWarnings("unused")
public class ImageAdapter extends CursorAdapter {

    private static final String TAG = "ImageAdapter";

    private static final int IMAGE_ID_COLUMN = 0;
    public static final boolean DEBUG = false;

    public static float IMAGE_WIDTH = 70;
    public static float IMAGE_HEIGHT = 70;
    public static float IMAGE_PADDING = 0;

    private final Context mContext;
    private Bitmap mDefaultBitmap;
    private int mImageWidth;
    private int mImageHeight;
    private int mImagePadding;

    private ImageView.ScaleType SCALE_TYPE = ImageView.ScaleType.CENTER_CROP;
    public static int IN_SAMPLE_SIZE = 3;

    public static int DRAWABLE = R.drawable.spinner_black_76;

    public ImageAdapter(Context context, Cursor c) {
        this(context, c, true);
    }

    private ImageAdapter(Context context, Cursor c, boolean autoRequery) {
        super(context, c, autoRequery);
        mContext = context;
        init(c);
    }

    private void init(Cursor c) {
        mDefaultBitmap = BitmapFactory.decodeResource(mContext.getResources(), DRAWABLE);
        float mScale = mContext.getResources().getDisplayMetrics().density;
        mImageWidth = (int) (IMAGE_WIDTH * mScale);
        mImageHeight = (int) (IMAGE_HEIGHT * mScale);
        mImagePadding = (int) (IMAGE_PADDING * mScale);
    }

    @Override
    public int getItemViewType(int position) {
        return 0;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        int id = cursor.getInt(IMAGE_ID_COLUMN);
        Uri uri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id);
        ImageView imageView = (ImageView) view;
        imageView.setImageBitmap(mDefaultBitmap);
        Glide.with(context).load(uri).diskCacheStrategy(DiskCacheStrategy.ALL).crossFade().into(imageView);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        ImageView imageView = new BetterImageView(mContext.getApplicationContext());
        imageView.setLayoutParams(new TwoWayAbsListView.LayoutParams(mImageWidth, mImageHeight));
        imageView.setPadding(mImagePadding, mImagePadding, mImagePadding, mImagePadding);
        imageView.setScaleType(SCALE_TYPE);
        return imageView;
    }

}

