package com.wan.hollout.ui.adapters;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
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

    private final int ITEM_TYPE_CAPTURE_PHOTO = 0;
    private final int ITEM_TYPE_SHOOT_VIDEO = 1;
    private final int ITEM_TYPE_NORMAL = 2;

    private CreatePostActivity createPostActivity;

    public PhotosAndVideosAdapter(CreatePostActivity activity, List<HolloutUtils.MediaEntry> photosAndVideos) {
        this.createPostActivity = activity;
        this.photosAndVideos = photosAndVideos;
        layoutInflater = LayoutInflater.from(activity);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView;
        int layoutRes = 0;
        switch (viewType) {
            case ITEM_TYPE_NORMAL:
                layoutRes = R.layout.all_media_recycler_item;
                break;
            case ITEM_TYPE_CAPTURE_PHOTO:
                layoutRes = R.layout.capture_photo;
                break;
            case ITEM_TYPE_SHOOT_VIDEO:
                layoutRes = R.layout.capture_video;
                break;
        }
        itemView = layoutInflater.inflate(layoutRes, parent, false);
        return new PhotosAndVideosHolder(itemView);
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return ITEM_TYPE_SHOOT_VIDEO;
        } else if (position == 1) {
            return ITEM_TYPE_CAPTURE_PHOTO;
        } else {
            return ITEM_TYPE_NORMAL;
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        PhotosAndVideosHolder photosAndVideosHolder = (PhotosAndVideosHolder) holder;
        try {
            photosAndVideosHolder.bindData(createPostActivity, photosAndVideos.get(position), position);
        } catch (IndexOutOfBoundsException ignore) {

        }
    }

    @Override
    public int getItemCount() {
        return !photosAndVideos.isEmpty() ? photosAndVideos.size() + 2 : 0;
    }

    static class PhotosAndVideosHolder extends RecyclerView.ViewHolder {

        @Nullable
        @BindView(R.id.image_icon_item)
        ImageView imageIconView;

        @Nullable
        @BindView(R.id.capture_video_or_photo)
        ImageView captureVideoOrPhoto;

        @Nullable
        @BindView(R.id.play_media_if_video_icon)
        ImageView playMediaIfVideoIconView;

        @Nullable
        @BindView(R.id.checked_indicator)
        ImageView checkedIndicator;

        @Nullable
        @BindView(R.id.image_item_layout)
        View mainItemView;

        PhotosAndVideosHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        void bindData(final CreatePostActivity createPostActivity, final HolloutUtils.MediaEntry mediaEntry, final int position) {
            if (mediaEntry != null) {
                if (imageIconView != null) {
                    Glide.with(ApplicationLoader.getInstance()).load(mediaEntry.path).error(R.drawable.x_ic_blank_picture).placeholder(R.drawable.x_ic_blank_picture).crossFade().into(imageIconView);
                }
                String fileMiMeType = FileUtils.getMimeType(mediaEntry.path);
                UiUtils.showView(playMediaIfVideoIconView, HolloutUtils.isVideo(fileMiMeType));
                if (mainItemView != null && imageIconView != null) {
                    UiUtils.tintImageView(checkedIndicator, ContextCompat.getColor(createPostActivity, R.color.hollout_color_one));
                    mainItemView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            File file = new File(mediaEntry.path);
                            if (file.exists()) {
                                Uri uri = Uri.fromFile(file);
                                if (!AppConstants.selectedUris.contains(uri)) {
                                    if (AppConstants.selectedUris.size() == 5) {
                                        UiUtils.showSafeToast("Maximum number of sharable media reached");
                                        return;
                                    }
                                    AppConstants.selectedUris.add(uri);
                                    UiUtils.showView(checkedIndicator, true);
                                    mainItemView.setScaleY(0.9f);
                                    mainItemView.setScaleX(0.9f);
                                    UiUtils.showView(CreatePostActivity.doneWithContentSelection, true);
                                } else {
                                    AppConstants.selectedUris.remove(uri);
                                    UiUtils.showView(checkedIndicator, false);
                                    mainItemView.setScaleY(1);
                                    mainItemView.setScaleX(1);
                                    if (AppConstants.selectedUris.isEmpty()) {
                                        UiUtils.showView(CreatePostActivity.doneWithContentSelection, false);
                                    }
                                }
                            } else {
                                UiUtils.showSafeToast("Error retrieving image. Please pick another");
                            }
                        }
                    });
                }

                if (imageIconView != null) {
                    imageIconView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (mainItemView != null) {
                                mainItemView.performClick();
                            }
                        }
                    });
                }

                File file = new File(mediaEntry.path);
                Uri uri = Uri.fromFile(file);
                if (checkedIndicator != null && mainItemView != null) {
                    UiUtils.showView(checkedIndicator, AppConstants.selectedUris.contains(uri));
                    if (AppConstants.selectedUris.contains(uri)) {
                        mainItemView.setScaleY(0.9f);
                        mainItemView.setScaleX(0.9f);
                    } else {
                        mainItemView.setScaleY(1);
                        mainItemView.setScaleX(1);
                    }
                }
            }

            if (position == 0 || position == 1) {
                if (mainItemView != null) {
                    mainItemView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if (position == 0) {
                                createPostActivity.shootVideo();
                            } else {
                                createPostActivity.initPhotoCapture();
                            }
                        }
                    });
                }
                if (captureVideoOrPhoto != null) {
                    captureVideoOrPhoto.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            mainItemView.performClick();
                        }
                    });
                }

            }

        }

    }

}
