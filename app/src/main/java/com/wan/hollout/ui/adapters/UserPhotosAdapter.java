package com.wan.hollout.ui.adapters;

import android.support.v7.widget.RecyclerView;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.wan.hollout.R;
import com.wan.hollout.bean.HolloutFile;
import com.wan.hollout.components.ApplicationLoader;
import com.wan.hollout.eventbuses.ClearSelectedAudio;
import com.wan.hollout.ui.activities.GalleryActivity;
import com.wan.hollout.ui.widgets.HolloutTextView;
import com.wan.hollout.ui.widgets.RoundedImageView;
import com.wan.hollout.utils.AppConstants;
import com.wan.hollout.utils.HolloutUtils;
import com.wan.hollout.utils.UiUtils;

import org.apache.commons.lang3.StringUtils;
import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * * Created by Wan on 3/19/2016.
 */
public class UserPhotosAdapter extends SectionedRecyclerAdapter<UserPhotosAdapter.PhotoHeaderHolder, UserPhotosAdapter.PhotoEntriesHolder> {

    private GalleryActivity galleryActivity;
    private ArrayList<HolloutUtils.MediaEntry> photoEntries;
    public LayoutInflater layoutInflater;
    static SparseBooleanArray selectedIndices;

    public UserPhotosAdapter(GalleryActivity galleryActivity, ArrayList<HolloutUtils.MediaEntry> photoEntries) {
        this.galleryActivity = galleryActivity;
        this.photoEntries = photoEntries;
        layoutInflater = LayoutInflater.from(galleryActivity);
        selectedIndices = new SparseBooleanArray();
    }

    @Override
    public boolean onPlaceSubHeaderBetweenItems(int itemPosition, int nextItemPosition) {
        HolloutUtils.MediaEntry mediaEntry1 = photoEntries.get(itemPosition);
        HolloutUtils.MediaEntry mediaEntry2 = photoEntries.get(nextItemPosition);
        return !mediaEntry1.bucketName.equals(mediaEntry2.bucketName);
    }

    @Override
    public PhotoHeaderHolder onCreateSubHeaderViewHolder(ViewGroup parent, int viewType) {
        View convertView = layoutInflater.inflate(R.layout.adapter_header, parent, false);
        return new PhotoHeaderHolder(convertView);
    }

    @Override
    public PhotoEntriesHolder onCreateItemViewHolder(ViewGroup parent, int viewType) {
        View convertView = layoutInflater.inflate(R.layout.image_grid_item, parent, false);
        return new PhotoEntriesHolder(convertView);
    }

    @Override
    public void onBindSubHeaderViewHolder(PhotoHeaderHolder subHeaderHolder, int nextItemPosition) {
        HolloutUtils.MediaEntry mediaEntry = photoEntries.get(nextItemPosition);
        if (mediaEntry.bucketName.equals("0") || mediaEntry.bucketName.equals("1")) {
            UiUtils.setTextOnView(subHeaderHolder.photoHeader, StringUtils.capitalize("Others"));
        } else {
            UiUtils.setTextOnView(subHeaderHolder.photoHeader, StringUtils.capitalize(mediaEntry.bucketName));
        }
    }

    @Override
    public void onBindItemViewHolder(final PhotoEntriesHolder holder, final int itemPosition) {
        final HolloutUtils.MediaEntry mediaEntry = photoEntries.get(itemPosition);
        RequestOptions requestOptions = UiUtils.getRequestOptions().error(R.drawable.x_ic_blank_picture)
                .placeholder(R.drawable.x_ic_blank_picture);

        Glide.with(ApplicationLoader.getInstance()).setDefaultRequestOptions(requestOptions)
                .load(mediaEntry.path)
                .into(holder.imageIconItem);

        holder.imageIconItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (HolloutUtils.getFileSizeInMB(mediaEntry.fileSize) <= 10) {
                    if (UserMusicAdapter.selectedIndices != null) {
                        UserMusicAdapter.selectedIndices.clear();
                        EventBus.getDefault().post(new ClearSelectedAudio(true));
                    }
                    HolloutFile newHolloutFile = new HolloutFile();
                    newHolloutFile.setFileType(AppConstants.FILE_TYPE_PHOTO);
                    newHolloutFile.setLocalFilePath(mediaEntry.path);
                    ArrayList<HolloutFile> selectedHolloutFiles = galleryActivity.getSelectedFiles();
                    if (selectedHolloutFiles.size() < 10 && !selectedHolloutFiles.contains(newHolloutFile)) {
                        galleryActivity.addFile(newHolloutFile);
                        selectedIndices.put(itemPosition, true);
                        holder.photoCheck.setChecked(true);
                        holder.imageIconItem.setScaleX(0.9f);
                        holder.imageIconItem.setScaleY(0.9f);
                    } else {
                        if (selectedHolloutFiles.contains(newHolloutFile)) {
                            galleryActivity.removeFile(newHolloutFile);
                        }
                        selectedIndices.put(itemPosition, false);
                        holder.photoCheck.setChecked(false);
                        holder.imageIconItem.setScaleX(1f);
                        holder.imageIconItem.setScaleY(1f);
                    }
                    if (galleryActivity.getSelectedFiles().size() == 10) {
                        UiUtils.showSafeToast("Maximum number of files to transfer reached");
                    }
                } else {
                    UiUtils.showSafeToast("File size is too large. You can only send 10MB files for now.");
                    holder.photoCheck.setChecked(false);
                    selectedIndices.put(itemPosition, false);
                }
            }
        });
        holder.photoCheck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                holder.imageIconItem.performClick();
            }
        });
        if (selectedIndices.get(itemPosition)) {
            holder.photoCheck.setChecked(true);
            holder.imageIconItem.setScaleX(0.9f);
            holder.imageIconItem.setScaleY(0.9f);
        } else {
            holder.photoCheck.setChecked(false);
            holder.imageIconItem.setScaleX(1f);
            holder.imageIconItem.setScaleY(1f);
        }
    }

    @Override
    public int getCount() {
        if (photoEntries != null) {
            if (!photoEntries.isEmpty()) {
                return photoEntries.size();
            } else {
                return 0;
            }
        } else {
            return 0;
        }
    }

    static class PhotoEntriesHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.image_icon_item)
        RoundedImageView imageIconItem;

        @BindView(R.id.image_click_item)
        CheckBox photoCheck;

        PhotoEntriesHolder(View mView) {
            super(mView);
            ButterKnife.bind(this, mView);
        }

    }

    static class PhotoHeaderHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.header)
        HolloutTextView photoHeader;

        PhotoHeaderHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }

    }

}
