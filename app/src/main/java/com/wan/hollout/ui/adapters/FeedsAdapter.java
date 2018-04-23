package com.wan.hollout.ui.adapters;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
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
import com.wan.hollout.ui.activities.UserPhotoPreviewActivity;
import com.wan.hollout.ui.widgets.CircleImageView;
import com.wan.hollout.utils.AppConstants;
import com.wan.hollout.utils.AuthUtil;
import com.wan.hollout.utils.FontUtils;
import com.wan.hollout.utils.HolloutUtils;
import com.wan.hollout.utils.UiUtils;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.WordUtils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

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

    private final int ITEM_TYPE_PHOTO_LIKE = 0;
    private final int ITEM_TYPE_STORY = 1;
    private final int ITEM_TYPE_WORKOUT_REQUEST = 2;

    public FeedsAdapter(Activity activity, List<ParseObject> feeds) {
        this.activity = activity;
        this.feeds = feeds;
        this.layoutInflater = LayoutInflater.from(activity);
    }

    @Override
    public int getItemViewType(int position) {
        ParseObject activityObject = feeds.get(position);
        String feedType = activityObject.getString(AppConstants.FEED_TYPE);
        switch (feedType) {
            case AppConstants.FEED_TYPE_PHOTO_LIKE:
                return ITEM_TYPE_PHOTO_LIKE;
            case AppConstants.USER_STORIES:
                return ITEM_TYPE_STORY;
            default:
                return ITEM_TYPE_WORKOUT_REQUEST;
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
            case ITEM_TYPE_WORKOUT_REQUEST:
                break;
            case ITEM_TYPE_STORY:
                layoutRes = R.layout.recycler_item_user_story;
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
        boolean seen = parseObject.getBoolean(AppConstants.FEED_SEEN);
        return (seen ? "Viewed Updates " : "Recent Updates ");
    }

    @Override
    public DateItemHolder onCreateHeaderViewHolder(ViewGroup parent) {
        View messageDatesHeaderView = layoutInflater.inflate(R.layout.recent_updates_header, parent, false);
        return new DateItemHolder(messageDatesHeaderView);
    }

    @Override
    public void onBindHeaderViewHolder(DateItemHolder holder, int position) {
        ParseObject parseObject = feeds.get(position);
        if (parseObject != null) {
            boolean seen = parseObject.getBoolean(AppConstants.FEED_SEEN);
            holder.bindData((seen ? "Viewed Updates " : "Recent Updates "));
        }
    }

    @Override
    public int getItemCount() {
        return feeds != null ? feeds.size() : 0;
    }

    static class ActivityItemHolder extends RecyclerView.ViewHolder {

        @Nullable
        @BindView(R.id.status_avatar)
        CircleImageView statusAvatarView;

        @Nullable
        @BindView(R.id.status_display_name)
        TextView statusDisplayNameView;

        @Nullable
        @BindView(R.id.status_timestamp_info)
        TextView statusTimeStampInfoView;

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

        private void bindData(final Activity activity, final ParseObject activityObject) {
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
            } else if (feedType.equals(AppConstants.USER_STORIES)) {
                //Display Normal Feed View
                String originatorName = originator.getString(AppConstants.APP_USER_DISPLAY_NAME);
                Date updatedAt = activityObject.getUpdatedAt();
                if (StringUtils.isNotEmpty(originatorName)) {
                    if (statusDisplayNameView != null) {
                        if (originatorId.equals(signedInUserId)) {
                            statusDisplayNameView.setText("You");
                        } else {
                            statusDisplayNameView.setText(WordUtils.capitalize(originatorName));
                        }
                    }
                }
                if (statusTimeStampInfoView != null) {
                    statusTimeStampInfoView.setText(HolloutUtils.timeAgo(updatedAt.getTime()));
                }
                List<ParseObject> storyList = activityObject.getList(AppConstants.STORY_LIST);
                if (storyList != null && !storyList.isEmpty()) {
                    ParseObject lastStory = storyList.get(storyList.size() - 1);
                    String lastStoryType = lastStory.getString(AppConstants.FEED_TYPE);
                    if (lastStoryType.equals(AppConstants.FEED_TYPE_SIMPLE_TEXT)) {
                        String storyBody = lastStory.getString(AppConstants.POST_BODY);
                        if (StringUtils.isNotEmpty(storyBody)) {
                            int typeface = lastStory.getInt(AppConstants.POST_TYPEFACE);
                            Typeface storyTypeface = FontUtils.selectTypeface(activity, typeface);
                            int storyColor = lastStory.getInt(AppConstants.POST_COLOR);
                            int textColor = storyColor == android.R.color.white ? Color.BLACK : Color.WHITE;
                            Bitmap lastStoryBitmap = HolloutUtils.drawTextAsBitmap(storyBody, 2, textColor, storyTypeface);
                            if (statusAvatarView != null) {
                                statusAvatarView.setImageBitmap(lastStoryBitmap);
                            }
                        }
                    }
                }
                boolean postSeen = activityObject.getBoolean(AppConstants.FEED_SEEN);
                if (statusAvatarView != null) {
                    if (!postSeen) {
                        statusAvatarView.setBorderColor(ContextCompat.getColor(activity, R.color.colorAccent));
                    } else {
                        statusAvatarView.setBorderColor(ContextCompat.getColor(activity, R.color.ease_white));
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

        @BindView(R.id.recent_updates_header)
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
