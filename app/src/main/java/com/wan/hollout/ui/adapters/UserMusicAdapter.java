package com.wan.hollout.ui.adapters;

import android.annotation.SuppressLint;
import android.content.ContentUris;
import android.net.Uri;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.text.Spanned;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.LinearLayout;


import com.wan.hollout.R;
import com.wan.hollout.bean.AudioFile;
import com.wan.hollout.bean.HolloutFile;
import com.wan.hollout.eventbuses.ClearSelectedPhotos;
import com.wan.hollout.ui.activities.GalleryActivity;
import com.wan.hollout.ui.widgets.HolloutTextView;
import com.wan.hollout.ui.widgets.RoundedImageView;
import com.wan.hollout.utils.AppConstants;
import com.wan.hollout.utils.HolloutUtils;
import com.wan.hollout.utils.UiUtils;

import org.apache.commons.lang3.StringUtils;
import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;

/**
 * * Created by Wan on 6/5/2016.
 */
public class UserMusicAdapter extends RecyclerView.Adapter<UserMusicAdapter.AudioRowHolder> implements StickyRecyclerHeadersAdapter<UserMusicAdapter.AudioHeaderHolder>, Filterable {

    public ArrayList<AudioFile> audioFiles;
    public LayoutInflater layoutInflater;
    public ArrayList<AudioFile> filteredAudioFiles = null;
    public AudioItemsFilter audioItemsFilter;
    public String queryString;

    private GalleryActivity galleryActivity;
    public static SparseBooleanArray selectedIndices;

    public UserMusicAdapter(GalleryActivity galleryActivity, ArrayList<AudioFile> audioFiles) {
        this.galleryActivity = galleryActivity;
        this.audioFiles = audioFiles;
        this.layoutInflater = LayoutInflater.from(galleryActivity);
        this.filteredAudioFiles = audioFiles;
        selectedIndices = new SparseBooleanArray();

        audioItemsFilter = new AudioItemsFilter();
    }

    public void setQueryString(String queryString) {
        this.queryString = queryString;
    }

    public String getQueryString() {
        return queryString;
    }

    @Override
    public AudioRowHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View convertView = layoutInflater.inflate(R.layout.audio_list_item, parent, false);
        return new AudioRowHolder(convertView);
    }

    @Override
    public void onBindViewHolder(final AudioRowHolder audioRowHolder, @SuppressLint("RecyclerView") final int position) {
        final AudioFile audioFile = filteredAudioFiles.get(position);
        int audioId = audioFile.getAudioId();
        final String audioFileName = audioFile.getTitle();

        Uri uri = null;
        if (audioId != -1) {
            uri = ContentUris.withAppendedId(Uri.parse("content://media/external/audio/albumart"), audioId);
            UiUtils.loadMusicPreview(galleryActivity, audioRowHolder.audioIcon, uri);
        }

        if (isNotEmpty(audioFileName)) {
            if (isNotEmpty(getQueryString())) {
                Spanned highlightTextIfNecessary = UiUtils.highlightTextIfNecessary(queryString, audioFileName, ContextCompat.getColor(galleryActivity, R.color.colorPrimary));
                UiUtils.setTextOnView(audioRowHolder.audioTitle, highlightTextIfNecessary);
            } else {
                UiUtils.setTextOnView(audioRowHolder.audioTitle, audioFileName);
            }
        }

        UiUtils.setTextOnView(audioRowHolder.audioAlbum, audioFile.getGenre() + "-");
        UiUtils.setTextOnView(audioRowHolder.audioSize, UiUtils.getTimeString(audioFile.getDuration()));

        final Uri finalUri = uri;

        audioRowHolder.audioItemParent.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                File file = new File(audioFile.getPath());
                if (file.exists() && HolloutUtils.getFileSizeInMB(file.length()) <= 10) {

                    if (UserPhotosAdapter.selectedIndices != null) {
                        UserPhotosAdapter.selectedIndices.clear();
                        EventBus.getDefault().post(new ClearSelectedPhotos(true));
                    }

                    HolloutFile newHolloutFile = new HolloutFile();
                    newHolloutFile.setFileType(AppConstants.FILE_TYPE_AUDIO);
                    newHolloutFile.setLocalFilePath(audioFile.getPath());
                    newHolloutFile.setFileName(audioFile.getTitle());
                    if (finalUri != null) {
                        newHolloutFile.setFileUri(finalUri);
                    }

                    ArrayList<HolloutFile> selectedHolloutFiles = galleryActivity.getSelectedFiles();
                    if (selectedHolloutFiles.size() < 10 && !selectedHolloutFiles.contains(newHolloutFile)) {
                        galleryActivity.addFile(newHolloutFile);
                        selectedIndices.put(position, true);
                        audioRowHolder.audioCheck.setChecked(true);
                    } else {
                        if (selectedHolloutFiles.contains(newHolloutFile)) {
                            galleryActivity.removeFile(newHolloutFile);
                        }
                        selectedIndices.put(position, false);
                        audioRowHolder.audioCheck.setChecked(false);
                    }
                } else {
                    UiUtils.showSafeToast("Sorry, transferable files on Hollout are restricted to 10MB for now");
                    selectedIndices.put(position, false);
                    audioRowHolder.audioCheck.setChecked(false);
                }
            }

        });

        audioRowHolder.audioCheck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                audioRowHolder.audioItemParent.performClick();
            }
        });

        if (selectedIndices.get(position)) {
            audioRowHolder.audioCheck.setChecked(true);
        } else {
            audioRowHolder.audioCheck.setChecked(false);
        }

    }

    @Override
    public String getHeaderId(int position) {
        return filteredAudioFiles.get(position).author;
    }

    @Override
    public AudioHeaderHolder onCreateHeaderViewHolder(ViewGroup parent) {
        View convertView = layoutInflater.inflate(R.layout.adapter_header, parent, false);
        return new AudioHeaderHolder(convertView);
    }

    @Override
    public void onBindHeaderViewHolder(AudioHeaderHolder audioHeaderHolder, int position) {
        String audioHeader = filteredAudioFiles.get(position).author;
        if (audioHeader.equals("0") || audioHeader.equals("1")) {
            UiUtils.setTextOnView(audioHeaderHolder.header, StringUtils.capitalize("Others"));
        } else {
            UiUtils.setTextOnView(audioHeaderHolder.header, StringUtils.capitalize(audioHeader));
        }
    }

    @Override
    public int getItemCount() {
        return filteredAudioFiles.size();
    }

    public class AudioRowHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.audio_icon)
        public RoundedImageView audioIcon;

        @BindView(R.id.audio_name)
        public HolloutTextView audioTitle;

        @BindView(R.id.audio_album)
        public HolloutTextView audioAlbum;

        @BindView(R.id.audio_size)
        public HolloutTextView audioSize;

        @BindView(R.id.audio_check)
        public CheckBox audioCheck;

        @BindView(R.id.audio_item_parent)
        public LinearLayout audioItemParent;

        public AudioRowHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }

    public class AudioHeaderHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.header)
        public HolloutTextView header;

        public AudioHeaderHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }

    private class AudioItemsFilter extends Filter {

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            setQueryString(constraint.toString());
            String filterString = constraint.toString().toLowerCase();
            FilterResults filterResults = new FilterResults();
            final List<AudioFile> data = audioFiles;
            int count = data.size();

            final ArrayList<AudioFile> newList = new ArrayList<>(count);
            String filterableString;

            for (int i = 0; i < count; i++) {
                filterableString = data.get(i).getTitle();
                if (filterableString.toLowerCase().contains(filterString)) {
                    newList.add(data.get(i));
                }
            }
            filterResults.values = newList;
            filterResults.count = newList.size();
            return filterResults;
        }

        @Override
        @SuppressWarnings("unchecked")
        protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
            filteredAudioFiles = (ArrayList<AudioFile>) filterResults.values;
            notifyDataSetChanged();
        }
    }

    @Override
    public Filter getFilter() {
        return audioItemsFilter;
    }

}
