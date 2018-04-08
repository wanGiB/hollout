package com.wan.hollout.ui.adapters;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;

import com.parse.ParseObject;
import com.wan.hollout.R;
import com.wan.hollout.ui.activities.SlidePagerActivity;
import com.wan.hollout.ui.utils.DateUtils;
import com.wan.hollout.utils.AppConstants;
import com.wan.hollout.utils.AuthUtil;
import com.wan.hollout.utils.HolloutUtils;
import com.wan.hollout.utils.UiUtils;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * @author Wan Clem
 */

public class PhotoLikesAdapter extends RecyclerView.Adapter<PhotoLikesAdapter.PhotoLikeHolder> implements StickyRecyclerHeadersAdapter<PhotoLikesAdapter.DateItemHolder> {

    private List<ParseObject> photoLikes;
    private Activity activity;
    private LayoutInflater layoutInflater;

    private Calendar calendar;

    public PhotoLikesAdapter(Activity activity, List<ParseObject> photoLikes) {
        this.activity = activity;
        this.photoLikes = photoLikes;
        this.layoutInflater = LayoutInflater.from(activity);
        this.calendar = Calendar.getInstance();
    }

    @NonNull
    @Override
    public PhotoLikeHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = layoutInflater.inflate(R.layout.photo_liker_recycler_item, parent, false);
        return new PhotoLikeHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull PhotoLikeHolder holder, int position) {
        ParseObject parseObject = photoLikes.get(position);
        holder.bindData(activity, parseObject);
    }

    @Override
    public String getHeaderId(int position) {
        ParseObject parseObject = photoLikes.get(position);
        Date createdAt = parseObject.getCreatedAt();
        calendar.setTime(createdAt);
        return String.valueOf(HolloutUtils.hashCode(calendar.get(Calendar.YEAR), calendar.get(Calendar.DAY_OF_YEAR)));
    }

    @Override
    public DateItemHolder onCreateHeaderViewHolder(ViewGroup parent) {
        View messageDatesHeaderView = layoutInflater.inflate(R.layout.activity_date_header_view, parent, false);
        return new DateItemHolder(messageDatesHeaderView);
    }

    @Override
    public void onBindHeaderViewHolder(DateItemHolder holder, int position) {
        ParseObject parseObject = photoLikes.get(position);
        if (parseObject != null) {
            Date createdAt = parseObject.getCreatedAt();
            String currentYear = AppConstants.DATE_FORMATTER_IN_YEARS.format(new Date());
            holder.bindData(DateUtils.getRelativeDate(activity, Locale.getDefault(), createdAt.getTime()).replace(", " + currentYear, ""));
        }
    }

    @Override
    public int getItemCount() {
        return photoLikes != null ? photoLikes.size() : 0;
    }

    static class PhotoLikeHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.photo_liker)
        ImageView photoLiker;

        @BindView(R.id.description_view)
        TextView descriptionView;

        @BindView(R.id.liked_photo_view)
        ImageView likedPhotoImageView;

        @BindView(R.id.time_view)
        TextView timeView;

        @BindView(R.id.item_view)
        FrameLayout parentView;

        @BindView(R.id.clickable_container)
        View clickableContainer;

        PhotoLikeHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        void bindData(final Activity activity, final ParseObject photoLikerObject) {
            final ParseObject originator = photoLikerObject.getParseObject(AppConstants.FEED_CREATOR);
            String displayName = originator.getString(AppConstants.APP_USER_DISPLAY_NAME);
            String profilePhotoUrl = originator.getString(AppConstants.APP_USER_PROFILE_PHOTO_URL);
            final String likedPhoto = photoLikerObject.getString(AppConstants.LIKED_PHOTO);
            boolean seenByOwner = photoLikerObject.getBoolean(AppConstants.SEEN_BY_OWNER);
            Date likeDate = photoLikerObject.getCreatedAt();

            descriptionView.setText(UiUtils.fromHtml("<font color=#000000><b>" + displayName + "</b></font> likes your photo"));
            String msgTime = AppConstants.DATE_FORMATTER_IN_12HRS.format(likeDate);
            timeView.setText(msgTime);

            if (StringUtils.isNotEmpty(profilePhotoUrl)) {
                UiUtils.loadImage(activity, profilePhotoUrl, photoLiker);
            } else {
                photoLiker.setImageResource(R.drawable.empty_profile);
            }
            UiUtils.loadImage(activity, likedPhoto, likedPhotoImageView);
            parentView.setBackgroundColor(seenByOwner ? Color.WHITE : ContextCompat.getColor(activity, R.color.unread_news_feed_background));
            clickableContainer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    parentView.setBackgroundColor(Color.WHITE);
                    photoLikerObject.put(AppConstants.SEEN_BY_OWNER, true);
                    photoLikerObject.saveInBackground();
                    ParseObject signedInUser = AuthUtil.getCurrentUser();
                    Intent photoViewIntent = new Intent(activity, SlidePagerActivity.class);
                    if (signedInUser != null) {
                        photoViewIntent.putExtra(AppConstants.EXTRA_USER_ID, signedInUser.getString(AppConstants.REAL_OBJECT_ID));
                        ArrayList<String> photos = new ArrayList<>();
                        photos.add(likedPhoto);
                        photoViewIntent.putStringArrayListExtra(AppConstants.EXTRA_PICTURES, photos);
                        activity.startActivity(photoViewIntent);
                    }
                }
            });
            photoLiker.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    UiUtils.loadUserData(activity, originator);
                }
            });
        }
    }

    static class DateItemHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.date_view)
        TextView dateView;

        DateItemHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        void bindData(String dateString) {
            dateView.setText(dateString);
        }

    }

    static boolean dayIsYesterday(DateTime day) {
        DateTime yesterday = new DateTime().withTimeAtStartOfDay().minusDays(1);
        DateTime inputDay = day.withTimeAtStartOfDay();
        return inputDay.isEqual(yesterday);
    }

}
