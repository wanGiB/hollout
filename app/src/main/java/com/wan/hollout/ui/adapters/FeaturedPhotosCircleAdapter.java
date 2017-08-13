package com.wan.hollout.ui.adapters;

import android.app.Activity;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.parse.ParseUser;
import com.wan.hollout.R;
import com.wan.hollout.ui.activities.SlidePagerActivity;
import com.wan.hollout.utils.AppConstants;
import com.wan.hollout.utils.UiUtils;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * @author Wan Clem
 */

public class FeaturedPhotosCircleAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private LayoutInflater layoutInflater;
    private Activity activity;
    private List<String> photos;
    private String username;

    public FeaturedPhotosCircleAdapter(Activity activity, List<String> photos, String username) {
        this.activity = activity;
        this.photos = photos;
        this.username = username;
        this.layoutInflater = LayoutInflater.from(activity);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View photoView = layoutInflater.inflate(R.layout.circle_featured_photo_item, parent, false);
        return new PhotoItemHolder(photoView);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, final int position) {
        final ParseUser signedInUserObject = ParseUser.getCurrentUser();
        if (signedInUserObject != null) {
            final FeaturedPhotosCircleAdapter.PhotoItemHolder photoItemHolder = (PhotoItemHolder) holder;
            final String photo = photos.get(position);
            if (StringUtils.isNotEmpty(photo)) {
                if (photoItemHolder.userPhotoView != null) {
                    UiUtils.loadImage(activity, photo, photoItemHolder.userPhotoView);

                    photoItemHolder.userPhotoView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            UiUtils.blinkView(v);
                            Intent mProfilePhotoViewIntent = new Intent(activity, SlidePagerActivity.class);
                            mProfilePhotoViewIntent.putExtra(AppConstants.EXTRA_TITLE, username);
                            ArrayList<String> photoExtras = new ArrayList<>();
                            photoExtras.add(0, photo);
                            for (String photoItem : photos) {
                                if (!photoExtras.contains(photoItem) && !photoItem.equals(photo)) {
                                    photoExtras.add(photoItem);
                                }
                            }
                            mProfilePhotoViewIntent.putStringArrayListExtra(AppConstants.EXTRA_PICTURES, photoExtras);
                            activity.startActivity(mProfilePhotoViewIntent);
                        }
                    });
                }
            }
        }
    }

    @Override
    public int getItemCount() {
        return photos != null ? photos.size() : 0;
    }

    @SuppressWarnings("WeakerAccess")
    static class PhotoItemHolder extends RecyclerView.ViewHolder {

        @Nullable
        @BindView(R.id.featured_photo_item)
        ImageView userPhotoView;

        public PhotoItemHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

    }

}
