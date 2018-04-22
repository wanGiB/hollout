package com.wan.hollout.ui.adapters;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
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
import com.wan.hollout.ui.activities.UserPhotoPreviewActivity;
import com.wan.hollout.ui.utils.DateUtils;
import com.wan.hollout.ui.widgets.HolloutTextView;
import com.wan.hollout.utils.AppConstants;
import com.wan.hollout.utils.AuthUtil;
import com.wan.hollout.utils.HolloutUtils;
import com.wan.hollout.utils.UiUtils;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.WordUtils;

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
public class FeedsAdapter extends RecyclerView.Adapter<FeedsAdapter.ActivityItemHolder> implements
        StickyRecyclerHeadersAdapter<FeedsAdapter.DateItemHolder> {

    private List<ParseObject> feeds;
    private Activity activity;
    private LayoutInflater layoutInflater;
    private Calendar calendar;

    private final int ITEM_TYPE_PHOTO_LIKE = 0;
    private final int ITEM_TYPE_TEXT = 1;
    private final int ITEM_TYPE_PHOTO_OR_VIDEO = 2;

    public FeedsAdapter(Activity activity, List<ParseObject> feeds) {
        this.activity = activity;
        this.feeds = feeds;
        this.layoutInflater = LayoutInflater.from(activity);
        this.calendar = Calendar.getInstance();
    }

    @Override
    public int getItemViewType(int position) {
        ParseObject activityObject = feeds.get(position);
        String feedType = activityObject.getString(AppConstants.FEED_TYPE);
        switch (feedType) {
            case AppConstants.FEED_TYPE_PHOTO_LIKE:
                return ITEM_TYPE_PHOTO_LIKE;
            case AppConstants.FEED_TYPE_SIMPLE_TEXT:
                return ITEM_TYPE_TEXT;
            default:
                return ITEM_TYPE_PHOTO_OR_VIDEO;
        }
    }

    @NonNull
    @Override
    public ActivityItemHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        int layoutRes = 0;
        switch (viewType) {
            case ITEM_TYPE_PHOTO_LIKE:
                layoutRes = R.layout.photo_liker_recycler_item;
                break;
            case ITEM_TYPE_PHOTO_OR_VIDEO:
                break;
            case ITEM_TYPE_TEXT:
                layoutRes = R.layout.recycler_item_text_status;
                break;
        }
        View itemView = layoutInflater.inflate(layoutRes, parent, false);
        return new ActivityItemHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ActivityItemHolder holder, int position) {
        ParseObject parseObject = feeds.get(position);
        holder.bindData(activity, parseObject);
    }

    @Override
    public String getHeaderId(int position) {
        ParseObject parseObject = feeds.get(position);
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
        ParseObject parseObject = feeds.get(position);
        if (parseObject != null) {
            Date createdAt = parseObject.getCreatedAt();
            String currentYear = AppConstants.DATE_FORMATTER_IN_YEARS.format(new Date());
            holder.bindData(DateUtils.getRelativeDate(activity, Locale.getDefault(), createdAt.getTime()).replace(", " + currentYear, ""));
        }
    }

    @Override
    public int getItemCount() {
        return feeds != null ? feeds.size() : 0;
    }

    static class ActivityItemHolder extends RecyclerView.ViewHolder {

        @Nullable
        @BindView(R.id.status_avatar)
        ImageView statusAvatarView;

        @Nullable
        @BindView(R.id.status_display_name)
        TextView statusDisplayNameView;

        @Nullable
        @BindView(R.id.status_timestamp_info)
        TextView statusTimeStampInfoView;

        @Nullable
        @BindView(R.id.status_content)
        HolloutTextView statusContentView;

        @Nullable
        @BindView(R.id.status_content_container)
        CardView statusContentContainer;

        @Nullable
        @BindView(R.id.like_status)
        FloatingActionButton initiateChatCauseOfStatus;

        @Nullable
        @BindView(R.id.status_more)
        ImageView statusMoreOptionsView;

        @Nullable
        @BindView(R.id.photo_liker)
        ImageView photoLiker;

        @Nullable
        @BindView(R.id.description_view)
        TextView descriptionView;

        @Nullable
        @BindView(R.id.liked_photo_view)
        ImageView likedPhotoImageView;

        @Nullable
        @BindView(R.id.time_view)
        TextView timeView;

        @Nullable
        @BindView(R.id.item_view)
        FrameLayout parentView;

        @Nullable
        @BindView(R.id.clickable_container)
        View clickableContainer;

        ActivityItemHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        void bindData(final Activity activity, final ParseObject activityObject) {
            String feedType = activityObject.getString(AppConstants.FEED_TYPE);
            final ParseObject originator = activityObject.getParseObject(AppConstants.FEED_CREATOR);
            String originatorId = originator.getString(AppConstants.REAL_OBJECT_ID);

            String displayName = originator.getString(AppConstants.APP_USER_DISPLAY_NAME);
            ParseObject signedInUser = AuthUtil.getCurrentUser();

            String signedInUserId = null;
            if (signedInUser != null) {
                signedInUserId = signedInUser.getString(AppConstants.REAL_OBJECT_ID);
            }

            String profilePhotoUrl = originator.getString(AppConstants.APP_USER_PROFILE_PHOTO_URL);

            if (feedType.equals(AppConstants.FEED_TYPE_PHOTO_LIKE)) {
                setupPhotoLikeView(activity, activityObject, originator, displayName, profilePhotoUrl);
            } else if (feedType.equals(AppConstants.FEED_TYPE_SIMPLE_TEXT)) {
                UiUtils.tintImageViewNoMode(initiateChatCauseOfStatus, ContextCompat.getColor(activity, R.color.ease_gray));
                //Display Normal Feed View
                String originatorName = originator.getString(AppConstants.APP_USER_DISPLAY_NAME);
                String originatorPhotoUrl = originator.getString(AppConstants.APP_USER_PROFILE_PHOTO_URL);
                String postBody = activityObject.getString(AppConstants.POST_BODY);
                int postTypeface = activityObject.getInt(AppConstants.POST_TYPEFACE);
                int postColor = activityObject.getInt(AppConstants.POST_COLOR);

                Date dateCreated = activityObject.getCreatedAt();

                if (StringUtils.isNotEmpty(originatorName)) {
                    if (statusDisplayNameView != null) {
                        statusDisplayNameView.setText(WordUtils.capitalize(originatorName));
                    }
                }

                if (statusTimeStampInfoView != null) {
                    statusTimeStampInfoView.setText(AppConstants.DATE_FORMATTER_IN_12HRS.format(dateCreated));
                }

                if (StringUtils.isNotEmpty(originatorPhotoUrl)) {
                    UiUtils.loadImage(activity, originatorPhotoUrl, statusAvatarView);
                } else {
                    UiUtils.loadName(statusAvatarView, originatorName);
                }

                if (statusContentView != null) {
                    statusContentView.setText(postBody);
                    statusContentView.applyCustomFont(activity, postTypeface);
                    if (postColor != android.R.color.white) {
                        if (statusContentContainer != null) {
                            statusContentContainer.setCardBackgroundColor(ContextCompat.getColor(activity, postColor));
                        }
                        statusContentView.setTextColor(Color.WHITE);
                    } else {
                        if (statusContentContainer != null) {
                            statusContentContainer.setCardBackgroundColor(Color.WHITE);
                        }
                        statusContentView.setTextColor(Color.BLACK);
                    }
                }

                UiUtils.showView(statusMoreOptionsView, signedInUserId != null && signedInUserId.equals(originatorId));

            }

        }

        private void setupPhotoLikeView(final Activity activity, final ParseObject activityObject,
                                        final ParseObject originator,
                                        String displayName,
                                        String profilePhotoUrl) {

            ParseObject signedInUserObject = AuthUtil.getCurrentUser();
            String signedInUserPhotoUrl = null;
            String signedInUserCoverPhoto = null;
            List<String> userFeaturedPhotos = new ArrayList<>();

            if (signedInUserObject != null) {
                signedInUserPhotoUrl = signedInUserObject.getString(AppConstants.APP_USER_PROFILE_PHOTO_URL);
                signedInUserCoverPhoto = signedInUserObject.getString(AppConstants.APP_USER_COVER_PHOTO);
                userFeaturedPhotos = signedInUserObject.getList(AppConstants.APP_USER_FEATURED_PHOTOS);
            }

            final String likedPhoto = activityObject.getString(AppConstants.LIKED_PHOTO);

            String message = "your photo";

            if (signedInUserPhotoUrl != null) {
                if (signedInUserPhotoUrl.equals(likedPhoto)) {
                    message = "your profile photo";
                }
            }

            if (signedInUserCoverPhoto != null) {
                if (signedInUserCoverPhoto.equals(likedPhoto)) {
                    message = "your cover photo";
                }
            }

            if (userFeaturedPhotos != null && !userFeaturedPhotos.isEmpty()) {
                if (userFeaturedPhotos.contains(likedPhoto)) {
                    message = "your featured photo";
                }
            }

            boolean seenByOwner = activityObject.getBoolean(AppConstants.SEEN_BY_OWNER);
            Date likeDate = activityObject.getCreatedAt();
            if (descriptionView != null) {
                descriptionView.setText(UiUtils.fromHtml("<font color=#000000><b>" + displayName + "</b></font> likes " + message));
            }

            String msgTime = AppConstants.DATE_FORMATTER_IN_12HRS.format(likeDate);
            if (timeView != null) {
                timeView.setText(msgTime);
            }

            if (StringUtils.isNotEmpty(profilePhotoUrl)) {
                UiUtils.loadImage(activity, profilePhotoUrl, photoLiker);
            } else {
                if (photoLiker != null) {
                    photoLiker.setImageResource(R.drawable.empty_profile);
                }
            }

            UiUtils.loadImage(activity, likedPhoto, likedPhotoImageView);
            if (parentView != null) {
                parentView.setBackgroundColor(seenByOwner ? Color.WHITE : ContextCompat.getColor(activity, R.color.unread_news_feed_background));
            }

            if (clickableContainer != null) {
                clickableContainer.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        parentView.setBackgroundColor(Color.WHITE);
                        activityObject.put(AppConstants.SEEN_BY_OWNER, true);
                        activityObject.saveInBackground();
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
            }

            photoLiker.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(activity, UserPhotoPreviewActivity.class);
                    intent.putExtra(AppConstants.EXTRA_USER, originator);
                    intent.putExtra(activity.getResources().getString(R.string.view_info),
                            UiUtils.captureValues(activity, photoLiker));
                    activity.startActivity(intent);
                    activity.overridePendingTransition(0, 0);
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

}
