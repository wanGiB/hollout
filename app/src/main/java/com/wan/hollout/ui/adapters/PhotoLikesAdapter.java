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
import com.wan.hollout.utils.AppConstants;
import com.wan.hollout.utils.AuthUtil;
import com.wan.hollout.utils.UiUtils;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * @author Wan Clem
 */

public class PhotoLikesAdapter extends RecyclerView.Adapter<PhotoLikesAdapter.PhotoLikeHolder> {

    private List<ParseObject> photoLikes;
    private Activity activity;
    private LayoutInflater layoutInflater;

    public PhotoLikesAdapter(Activity activity, List<ParseObject> photoLikes) {
        this.activity = activity;
        this.photoLikes = photoLikes;
        this.layoutInflater = LayoutInflater.from(activity);
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
            ParseObject originator = photoLikerObject.getParseObject(AppConstants.FEED_CREATOR);
            String displayName = originator.getString(AppConstants.APP_USER_DISPLAY_NAME);
            String profilePhotoUrl = originator.getString(AppConstants.APP_USER_PROFILE_PHOTO_URL);
            final String likedPhoto = photoLikerObject.getString(AppConstants.LIKED_PHOTO);
            boolean seenByOwner = photoLikerObject.getBoolean(AppConstants.SEEN_BY_OWNER);
            Date likeDate = photoLikerObject.getDate("createdAt");

            boolean isYesterday = dayIsYesterday(new DateTime(likeDate.getTime()));
            String todayString = AppConstants.DATE_FORMATTER_IN_BIRTHDAY_FORMAT.format(new Date());
            String photoLikeDateString = AppConstants.DATE_FORMATTER_IN_BIRTHDAY_FORMAT.format(likeDate);
            descriptionView.setText(UiUtils.fromHtml("<font color=#000000><b>" + displayName + "</b></font> likes your photo"));
            if (todayString.equals(photoLikeDateString)) {
                //Photo Liked date = today
                String msgTime = AppConstants.DATE_FORMATTER_IN_12HRS.format(likeDate);
                timeView.setText(msgTime);
            } else {
                if (isYesterday) {
                    timeView.setText("yesterday");
                } else {
                    String daysAgo = AppConstants.DATE_FORMATTER_IN_BIRTHDAY_FORMAT.format(likeDate);
                    String currentYear = AppConstants.DATE_FORMATTER_IN_YEARS.format(new Date());
                    daysAgo = daysAgo.replace(currentYear, "");
                    timeView.setText(daysAgo);
                }
            }
            if (StringUtils.isNotEmpty(profilePhotoUrl)) {
                UiUtils.loadImage(activity, profilePhotoUrl, photoLiker);
            } else {
                photoLiker.setImageResource(R.drawable.empty_profile);
            }
            UiUtils.loadImage(activity, likedPhoto, likedPhotoImageView);
            parentView.setBackgroundColor(seenByOwner ? Color.WHITE : ContextCompat.getColor(activity, R.color.lighter_blue));
            clickableContainer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
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
                    UiUtils.loadUserData(activity, photoLikerObject);
                }
            });
        }
    }

    static boolean dayIsYesterday(DateTime day) {
        DateTime yesterday = new DateTime().withTimeAtStartOfDay().minusDays(1);
        DateTime inputDay = day.withTimeAtStartOfDay();
        return inputDay.isEqual(yesterday);
    }

}
