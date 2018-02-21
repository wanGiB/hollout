package com.wan.hollout.ui.adapters;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.LinearLayout;

import com.bumptech.glide.Glide;
import com.wan.hollout.BuildConfig;
import com.wan.hollout.R;
import com.wan.hollout.bean.HolloutFile;
import com.wan.hollout.ui.widgets.HolloutTextView;
import com.wan.hollout.ui.widgets.RoundedImageView;
import com.wan.hollout.utils.AppConstants;
import com.wan.hollout.utils.HolloutUtils;
import com.wan.hollout.utils.UiUtils;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

import static org.apache.commons.lang3.StringUtils.isNotEmpty;


/**
 * * Created by Wan on 6/5/2016.
 */
public class UserVideosAdapter extends RecyclerView.Adapter<UserVideosAdapter.VideoRowHolder> implements
        StickyRecyclerHeadersAdapter<UserVideosAdapter.VideoHeaderHolder>, Filterable {

    private ArrayList<HolloutUtils.MediaEntry> userVideoEntries;
    private Activity appCompatActivity;
    public LayoutInflater layoutInflater;
    private String queryString;
    private ArrayList<HolloutUtils.MediaEntry> filteredVideoEntries = null;
    private VideoItemsFilter videoFilter;

    public UserVideosAdapter(Activity appCompatActivity, ArrayList<HolloutUtils.MediaEntry> userVideoEntries) {
        this.appCompatActivity = appCompatActivity;
        this.userVideoEntries = userVideoEntries;
        this.layoutInflater = LayoutInflater.from(appCompatActivity);
        filteredVideoEntries = userVideoEntries;
        videoFilter = new VideoItemsFilter();
    }

    private void setQueryString(String queryString) {
        this.queryString = queryString;
    }

    private String getQueryString() {
        return queryString;
    }

    @Override
    public VideoRowHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View convertView = layoutInflater.inflate(R.layout.video_list_item, parent, false);
        return new VideoRowHolder(convertView);
    }

    @Override
    public void onBindViewHolder(final VideoRowHolder videoRowHolder, int position) {

        final HolloutUtils.MediaEntry videoEntry = filteredVideoEntries.get(position);
        String videoFileName = videoEntry.fileName;

        final String videoPath = videoEntry.path;

        if (Build.VERSION.SDK_INT >= 17) {
            if (!appCompatActivity.isDestroyed()) {
                Glide.with(appCompatActivity).load(videoPath).error(R.drawable.ex_completed_ic_video).placeholder(R.drawable.ex_completed_ic_video).crossFade().into(videoRowHolder.randomVideoFrame);
                videoRowHolder.randomVideoFrame.invalidate();
            }
        } else {
            Glide.with(appCompatActivity).load(videoPath).error(R.drawable.ex_completed_ic_video).placeholder(R.drawable.ex_completed_ic_video).crossFade().into(videoRowHolder.randomVideoFrame);
            videoRowHolder.randomVideoFrame.invalidate();
        }

        if (isNotEmpty(videoFileName)) {
            if (isNotEmpty(getQueryString())) {
                Spanned highlightTextIfNecessary = UiUtils.highlightTextIfNecessary(queryString, videoFileName, ContextCompat.getColor(appCompatActivity, R.color.colorPrimary));
                UiUtils.setTextOnView(videoRowHolder.videoName, highlightTextIfNecessary);
            } else {
                UiUtils.setTextOnView(videoRowHolder.videoName, videoFileName);
            }
        }

        UiUtils.setTextOnView(videoRowHolder.videoSize, String.valueOf(HolloutUtils.getFileSizeInMB(videoEntry.fileSize) + "MB"));
        UiUtils.setTextOnView(videoRowHolder.videoDurationTime, UiUtils.getTimeString(videoEntry.fileDuration));

        videoRowHolder.videoCheck.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (!BuildConfig.DEBUG) {
                    if (HolloutUtils.getFileSizeInMB(videoEntry.fileSize) <= 20) {
                        sendBackSelectedVideo(videoEntry);
                    } else {
                        UiUtils.showSafeToast("Sorry, transferable files on Hollout are restricted to 20MB for now");
                        videoRowHolder.videoCheck.setChecked(false);
                    }
                } else {
                    sendBackSelectedVideo(videoEntry);
                }
            }

        });

        videoRowHolder.videoParent.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent mViewVideoIntent = new Intent(Intent.ACTION_VIEW);
                mViewVideoIntent.setDataAndType(Uri.parse(videoPath), "video/*");
                appCompatActivity.startActivity(mViewVideoIntent);
            }

        });

    }

    private void sendBackSelectedVideo(HolloutUtils.MediaEntry videoEntry) {
        HolloutFile newHolloutFile = new HolloutFile();
        newHolloutFile.setLocalFilePath(videoEntry.path);
        newHolloutFile.setFileType(AppConstants.FILE_TYPE_VIDEO);
        newHolloutFile.setFileKey(videoEntry.path);
        newHolloutFile.setFileDuration(videoEntry.fileDuration);

        //Start the chat activity with this video
        ArrayList<HolloutFile> results = new ArrayList<>();
        results.add(newHolloutFile);

        Intent resultIntent = new Intent();
        resultIntent.putParcelableArrayListExtra(AppConstants.GALLERY_RESULTS, results);
        appCompatActivity.setResult(Activity.RESULT_OK, resultIntent);
        appCompatActivity.finish();
    }

    @Override
    public String getHeaderId(int position) {
        return filteredVideoEntries.get(position).bucketName;
    }

    @Override
    public VideoHeaderHolder onCreateHeaderViewHolder(ViewGroup parent) {
        View convertView = layoutInflater.inflate(R.layout.adapter_header, parent, false);
        return new VideoHeaderHolder(convertView);
    }

    @Override
    public void onBindHeaderViewHolder(VideoHeaderHolder holder, int position) {
        HolloutUtils.MediaEntry mediaEntry = filteredVideoEntries.get(position);
        if (mediaEntry.bucketName.equals("0") || mediaEntry.bucketName.equals("1")) {
            UiUtils.setTextOnView(holder.headerView, StringUtils.capitalize("Others"));
        } else {
            UiUtils.setTextOnView(holder.headerView, StringUtils.capitalize(mediaEntry.bucketName));
        }
    }

    @Override
    public int getItemCount() {
        return filteredVideoEntries.size();
    }

    static class VideoRowHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.random_video_frame)
        RoundedImageView randomVideoFrame;

        @BindView(R.id.video_duration_item)
        HolloutTextView videoDurationTime;

        @BindView(R.id.video_name)
        HolloutTextView videoName;

        @BindView(R.id.video_size)
        HolloutTextView videoSize;

        @BindView(R.id.video_check)
        CheckBox videoCheck;

        @BindView(R.id.video_parent_view)
        LinearLayout videoParent;

        VideoRowHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }

    static class VideoHeaderHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.header)
        HolloutTextView headerView;

        VideoHeaderHolder(View view) {
            super(view);
            ButterKnife.bind(this, view);
        }
    }

    private class VideoItemsFilter extends Filter {

        @Override
        protected FilterResults performFiltering(CharSequence constraint) {
            setQueryString(constraint.toString());
            String filterString = constraint.toString().toLowerCase();
            FilterResults filterResults = new FilterResults();
            final List<HolloutUtils.MediaEntry> data = userVideoEntries;
            int count = data.size();

            final ArrayList<HolloutUtils.MediaEntry> newList = new ArrayList<>(count);
            String filterableString;

            for (int i = 0; i < count; i++) {
                filterableString = data.get(i).fileName;
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
            filteredVideoEntries = (ArrayList<HolloutUtils.MediaEntry>) filterResults.values;
            notifyDataSetChanged();
        }
    }

    @Override
    public Filter getFilter() {
        return videoFilter;
    }
}
