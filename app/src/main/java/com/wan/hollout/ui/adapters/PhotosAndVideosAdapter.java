package com.wan.hollout.ui.adapters;

import android.app.Activity;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.wan.hollout.R;
import com.wan.hollout.utils.FileUtils;
import com.wan.hollout.utils.HolloutUtils;
import com.wan.hollout.utils.UiUtils;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * @author Wan Clem
 */

public class PhotosAndVideosAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<HolloutUtils.MediaEntry> photosAndVideos;
    private Activity activity;
    private LayoutInflater layoutInflater;

    public PhotosAndVideosAdapter(Activity activity, List<HolloutUtils.MediaEntry> photosAndVideos) {
        this.activity = activity;
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
        photosAndVideosHolder.bindData(activity, photosAndVideos.get(position));
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

        PhotosAndVideosHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        void bindData(Activity activity, HolloutUtils.MediaEntry mediaEntry) {
            if (Build.VERSION.SDK_INT >= 17) {
                if (!activity.isDestroyed()) {
                    Glide.with(activity).load(mediaEntry.path).error(R.drawable.x_ic_blank_picture).placeholder(R.drawable.x_ic_blank_picture).crossFade().into(imageIconView);
                }
            } else {
                Glide.with(activity).load(mediaEntry.path).error(R.drawable.x_ic_blank_picture).placeholder(R.drawable.x_ic_blank_picture).crossFade().into(imageIconView);
            }
            String fileMiMeType = FileUtils.getMimeType(mediaEntry.path);
            UiUtils.showView(playMediaIfVideoIconView, HolloutUtils.isVideo(fileMiMeType));
        }

    }

}
