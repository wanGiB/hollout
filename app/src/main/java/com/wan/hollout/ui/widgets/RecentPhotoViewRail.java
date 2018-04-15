package com.wan.hollout.ui.widgets;


import android.content.ContentUris;
import android.content.Context;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.FrameLayout;

import com.wan.hollout.R;
import com.wan.hollout.ui.adapters.ImageAdapter;
import com.wan.hollout.utils.RecentImages;
import com.wan.hollout.utils.UiUtils;
import com.wan.hollout.utils.ViewUtil;

public class RecentPhotoViewRail extends FrameLayout {

    @NonNull
    private final TwoWayGridView twoWayGridView;

    @Nullable
    private OnItemClickedListener listener;

    private RecentImages recentImages;

    public RecentPhotoViewRail(Context context) {
        this(context, null);
    }

    public RecentPhotoViewRail(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public RecentPhotoViewRail(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        inflate(context, R.layout.recent_photo_view, this);
        this.twoWayGridView = ViewUtil.findById(this, R.id.gridview);
    }

    public void fetchRecentImages() {
        recentImages = new RecentImages();
        final ImageAdapter adapter = recentImages.getAdapter(getContext());
        twoWayGridView.setAdapter(adapter);
        if (adapter.getCount() > 0) {
            UiUtils.showView(this, true);
        }
        twoWayGridView.setOnItemClickListener(new TwoWayAdapterView.OnItemClickListener() {
            public void onItemClick(TwoWayAdapterView parent, View v, int position, long id) {
                Uri imageUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id);
                if (listener != null) {
                    listener.onItemClicked(imageUri);
                }
            }
        });
    }

    public void setListener(@Nullable OnItemClickedListener listener) {
        this.listener = listener;
    }

    public interface OnItemClickedListener {
        void onItemClicked(Uri uri);
    }

}
