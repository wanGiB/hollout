package com.wan.hollout.ui.adapters;

import android.app.Activity;
import android.content.Intent;
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
import com.wan.hollout.utils.AppConstants;
import com.wan.hollout.utils.FileUtils;
import com.wan.hollout.utils.HolloutUtils;
import com.wan.hollout.utils.UiUtils;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * @author Wan Clem
 */

public class SelectedFilesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Activity activity;
    private LayoutInflater layoutInflater;

    public SelectedFilesAdapter(Activity activity) {
        this.activity = activity;
        layoutInflater = LayoutInflater.from(activity);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = layoutInflater.inflate(R.layout.selected_file_for_upload, parent, false);
        return new SelectedFilesHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        SelectedFilesHolder selectedFilesHolder = (SelectedFilesHolder) holder;
        selectedFilesHolder.bindData(activity, position, this);
    }

    @Override
    public int getItemCount() {
        return AppConstants.selectedUris.size();
    }

    static class SelectedFilesHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.image_icon_item)
        ImageView imageIconView;

        @BindView(R.id.play_media_if_video_icon)
        ImageView playMediaIfVideoIconView;

        @BindView(R.id.image_item_layout)
        View mainItemView;

        @BindView(R.id.remove_file)
        ImageView removeFileView;

        SelectedFilesHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        void bindData(final Activity activity, final int position, final SelectedFilesAdapter selectedFilesAdapter) {
            final Uri uri = AppConstants.selectedUris.get(position);
            String filePath = uri.getPath();
            String fileMiMeType = FileUtils.getMimeType(uri.getPath());
            boolean isVideo = HolloutUtils.isVideo(fileMiMeType);
            Glide.with(ApplicationLoader.getInstance())
                    .load(filePath)
                    .error(R.drawable.x_ic_blank_picture)
                    .placeholder(R.drawable.x_ic_blank_picture)
                    .crossFade()
                    .into(imageIconView);
            UiUtils.showView(playMediaIfVideoIconView, isVideo);
            mainItemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String fileMiMeType = FileUtils.getMimeType(uri.toString());
                    boolean isVideo = HolloutUtils.isVideo(fileMiMeType);
                    if (isVideo) {
                        Intent videoIntent = new Intent(Intent.ACTION_VIEW);
                        videoIntent.setDataAndType(Uri.parse(uri.getPath()), "video/*");
                        activity.startActivity(videoIntent);
                    } else {
                        UiUtils.previewSelectedFile(activity, uri);
                    }
                }
            });
            imageIconView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mainItemView.performClick();
                }
            });
            removeFileView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AppConstants.selectedUris.remove(uri);
                    selectedFilesAdapter.notifyDataSetChanged();
                }
            });

        }

    }

}
