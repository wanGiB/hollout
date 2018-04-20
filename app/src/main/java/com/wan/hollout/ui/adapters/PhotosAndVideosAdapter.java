package com.wan.hollout.ui.adapters;

import android.app.Activity;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.wan.hollout.R;
import com.wan.hollout.components.ApplicationLoader;
import com.wan.hollout.ui.activities.CreatePostActivity;
import com.wan.hollout.utils.AppConstants;
import com.wan.hollout.utils.FileUtils;
import com.wan.hollout.utils.HolloutUtils;
import com.wan.hollout.utils.UiUtils;

import java.io.File;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * @author Wan Clem
 */

public class PhotosAndVideosAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<HolloutUtils.MediaEntry> photosAndVideos;
    private LayoutInflater layoutInflater;

    public PhotosAndVideosAdapter(Activity activity, List<HolloutUtils.MediaEntry> photosAndVideos) {
        this.photosAndVideos = photosAndVideos;
        layoutInflater = LayoutInflater.from(activity);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = layoutInflater.inflate(R.layout.all_media_recycler_item, parent, false);
        return new PhotosAndVideosHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        PhotosAndVideosHolder photosAndVideosHolder = (PhotosAndVideosHolder) holder;
        photosAndVideosHolder.bindData(photosAndVideos.get(position));
    }

    @Override
    public int getItemCount() {
        return !photosAndVideos.isEmpty() ? photosAndVideos.size() : 0;
    }

    static class PhotosAndVideosHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.image_icon_item)
        ImageView imageIconView;

        @BindView(R.id.play_media_if_video_icon)
        ImageView playMediaIfVideoIconView;

        @BindView(R.id.checked_indicator)
        ImageView checkedIndicator;

        @BindView(R.id.image_item_layout)
        View mainItemView;

        PhotosAndVideosHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        void bindData(final HolloutUtils.MediaEntry mediaEntry) {
            Glide.with(ApplicationLoader.getInstance()).load(mediaEntry.path).error(R.drawable.x_ic_blank_picture).placeholder(R.drawable.x_ic_blank_picture).crossFade().into(imageIconView);
            String fileMiMeType = FileUtils.getMimeType(mediaEntry.path);
            UiUtils.showView(playMediaIfVideoIconView, HolloutUtils.isVideo(fileMiMeType));
            mainItemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    File file = new File(mediaEntry.path);
                    if (file.exists()) {
                        Uri uri = Uri.fromFile(file);
                        if (AppConstants.selectedUris.size() == 5) {
                            UiUtils.showSafeToast("Maximum number of sharable media reached");
                            return;
                        }
                        if (!AppConstants.selectedUris.contains(uri)) {
                            AppConstants.selectedUris.add(uri);
                            UiUtils.showView(checkedIndicator, true);
                            mainItemView.setScaleY(0.9f);
                            mainItemView.setScaleX(0.9f);
                            CreatePostActivity.doneWithContentSelection.show();
                        } else {
                            AppConstants.selectedUris.remove(uri);
                            UiUtils.showView(checkedIndicator, false);
                            mainItemView.setScaleY(1);
                            mainItemView.setScaleX(1);
                            if (AppConstants.selectedUris.isEmpty()) {
                                CreatePostActivity.doneWithContentSelection.hide();
                            }
                        }
                    } else {
                        UiUtils.showSafeToast("Error retrieving image. Please pick another");
                    }
                }
            });
            imageIconView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mainItemView.performClick();
                }
            });
        }

    }

}
