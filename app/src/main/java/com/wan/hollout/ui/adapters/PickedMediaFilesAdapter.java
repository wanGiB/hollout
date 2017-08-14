package com.wan.hollout.ui.adapters;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.support.graphics.drawable.VectorDrawableCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.util.SparseBooleanArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.wan.hollout.R;
import com.wan.hollout.bean.HolloutFile;
import com.wan.hollout.ui.widgets.CircleImageView;
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
 * @author Wan Clem
 */

public class PickedMediaFilesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private Activity activity;
    private LayoutInflater layoutInflater;
    private ArrayList<HolloutFile> holloutFiles;

    private VectorDrawableCompat cancelPickedMediaDrawable;
    private SparseBooleanArray sparseBooleanArray = new SparseBooleanArray();

    public PickedMediaFilesAdapter(Activity activity, ArrayList<HolloutFile> holloutFiles) {
        this.activity = activity;
        this.holloutFiles = holloutFiles;
        this.layoutInflater = LayoutInflater.from(activity);
        cancelPickedMediaDrawable = VectorDrawableCompat.create(activity.getResources(), R.drawable.ic_circled_cancel_accent, null);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View convertView = layoutInflater.inflate(R.layout.picked_media_item, parent, false);
        return new PickedMediaItemHolder(convertView);
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {

        PickedMediaItemHolder pickedMediaItemHolder = (PickedMediaItemHolder) holder;
        final HolloutFile holloutFile = holloutFiles.get(position);

        if (holloutFile != null) {

            String mediaPath = holloutFile.getLocalFilePath();

            if (holloutFile.getFileType().equals(AppConstants.FILE_TYPE_VIDEO)) {

                sparseBooleanArray.put(position, true);

                if (Build.VERSION.SDK_INT >= 17) {
                    if (!activity.isDestroyed()) {
                        Glide.with(activity).load(mediaPath).error(R.drawable.ex_completed_ic_video).placeholder(R.drawable.ex_completed_ic_video).crossFade().into(pickedMediaItemHolder.mediaView);
                        pickedMediaItemHolder.mediaView.invalidate();
                    }
                } else {
                    Glide.with(activity).load(mediaPath).error(R.drawable.ex_completed_ic_video).placeholder(R.drawable.ex_completed_ic_video).crossFade().into(pickedMediaItemHolder.mediaView);
                    pickedMediaItemHolder.mediaView.invalidate();
                }

                UiUtils.showView(pickedMediaItemHolder.playMediaIfVideoView, true);
                pickedMediaItemHolder.playMediaIfVideoView.invalidate();

                UiUtils.showView(pickedMediaItemHolder.mediaLengthView, true);
                //Calculate length of media now
                pickedMediaItemHolder.mediaLengthView.setText(UiUtils.getTimeString(HolloutUtils.getVideoDuration(mediaPath)));
                pickedMediaItemHolder.mediaLengthView.setTextColor(ContextCompat.getColor(activity, R.color.white));

            } else if (holloutFile.getFileType().equals(AppConstants.FILE_TYPE_AUDIO)) {

                sparseBooleanArray.put(position, true);
                pickedMediaItemHolder.mediaView.setImageResource(R.drawable.x_ic_folde_music);

                UiUtils.showView(pickedMediaItemHolder.playMediaIfVideoView, true);
                pickedMediaItemHolder.playMediaIfVideoView.invalidate();

                UiUtils.showView(pickedMediaItemHolder.mediaLengthView, true);

                //Calculate length of media now
                pickedMediaItemHolder.mediaLengthView.setText(UiUtils.getTimeString(HolloutUtils.getVideoDuration(mediaPath)));
                pickedMediaItemHolder.mediaLengthView.setTextColor(ContextCompat.getColor(activity, R.color.hollout_color_one));

            } else {

                sparseBooleanArray.put(position, false);

                if (StringUtils.isNotEmpty(mediaPath)) {
                    UiUtils.loadImage(activity, mediaPath, pickedMediaItemHolder.mediaView);
                }

                UiUtils.showView(pickedMediaItemHolder.mediaLengthView, false);
                pickedMediaItemHolder.mediaLengthView.invalidate();

                UiUtils.showView(pickedMediaItemHolder.playMediaIfVideoView, false);
                pickedMediaItemHolder.playMediaIfVideoView.invalidate();

            }

            pickedMediaItemHolder.cancelPickedMedia.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View view) {

                    if (holloutFiles.contains(holloutFile)) {
                        holloutFiles.remove(holloutFile);
                        notifyDataSetChanged();
                    }

                    checkAndNotifyObserversOfEmptiness();

                }
            });

        }

        pickedMediaItemHolder.cancelPickedMedia.setImageDrawable(cancelPickedMediaDrawable);

        pickedMediaItemHolder.parentView.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                if (holloutFile != null) {

                    if (holloutFile.getFileType().equals(AppConstants.FILE_TYPE_PHOTO)) {
                        UiUtils.previewSelectedFile(activity, holloutFile);
                    } else if (holloutFile.getFileType().equals(AppConstants.FILE_TYPE_VIDEO)
                            || holloutFile.getFileType().equals(AppConstants.FILE_TYPE_AUDIO)) {
                        Intent mViewVideoIntent = new Intent(Intent.ACTION_VIEW);
                        mViewVideoIntent.setDataAndType(Uri.parse(holloutFile.getLocalFilePath()), "video/*");
                        activity.startActivity(mViewVideoIntent);
                    }

                }

            }

        });

        if (sparseBooleanArray.get(position)) {
            UiUtils.showView(pickedMediaItemHolder.mediaLengthView, true);
        } else {
            UiUtils.showView(pickedMediaItemHolder.mediaLengthView, false);
        }

    }

    private void checkAndNotifyObserversOfEmptiness() {
        if (holloutFiles.isEmpty()) {
            EventBus.getDefault().post(AppConstants.PICKED_MEDIA_FILES_EMPTY_NOW);
        }
    }

    @Override
    public int getItemCount() {
        if (holloutFiles != null) {
            if (!holloutFiles.isEmpty()) {
                return holloutFiles.size();
            } else {
                return 0;
            }
        } else {
            return 0;
        }
    }

    static class PickedMediaItemHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.single_media_viewer)
        RoundedImageView mediaView;

        @BindView(R.id.play_single_media_if_video)
        ImageView playMediaIfVideoView;

        @BindView(R.id.cancel_picked_media)
        CircleImageView cancelPickedMedia;

        @BindView(R.id.media_length_view)
        HolloutTextView mediaLengthView;

        View parentView;

        PickedMediaItemHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
            parentView = itemView;
        }

    }

}
