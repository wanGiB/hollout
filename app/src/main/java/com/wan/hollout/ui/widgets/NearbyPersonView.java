package com.wan.hollout.ui.widgets;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.SubscriptionHandling;
import com.wan.hollout.R;
import com.wan.hollout.components.ApplicationLoader;
import com.wan.hollout.ui.activities.UserPhotoPreviewActivity;
import com.wan.hollout.ui.activities.UserProfileActivity;
import com.wan.hollout.utils.AppConstants;
import com.wan.hollout.utils.AuthUtil;
import com.wan.hollout.utils.HolloutLogger;
import com.wan.hollout.utils.HolloutUtils;
import com.wan.hollout.utils.UiUtils;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.WordUtils;

import java.util.HashMap;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * @author Wan Clem
 */
@SuppressWarnings("unused")
public class NearbyPersonView extends RelativeLayout implements View.OnClickListener, View.OnLongClickListener {

    @BindView(R.id.from)
    HolloutTextView usernameEntryView;

    @BindView(R.id.txt_primary)
    HolloutTextView aboutPerson;

    @BindView(R.id.txt_secondary)
    HolloutTextView userStatus;

    @BindView(R.id.distance_to_user)
    TextView distanceToUserView;

    @BindView(R.id.icon_profile)
    CircleImageView userPhotoView;

    @BindView(R.id.parent_layout)
    View parentView;

    @BindView(R.id.online_status)
    ImageView onlineStatusView;

    private ParseObject signedInUser;
    public Activity activity;

    private ParseQuery<ParseObject> userStateQuery;

    private String searchString;
    private ParseObject person;

    public NearbyPersonView(Context context) {
        this(context, null);
    }

    public NearbyPersonView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public NearbyPersonView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        inflate(context, R.layout.nearby_person_to_meet, this);
    }

    private void init() {
        setOnClickListener(this);
        setOnLongClickListener(this);
        parentView.setOnClickListener(this);
    }

    @Override
    protected void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    public void bindData(Activity activity, String searchString, ParseObject person) {
        this.searchString = searchString;
        this.activity = activity;
        signedInUser = AuthUtil.getCurrentUser();
        init();
        loadParseUser(person, searchString);
    }

    @NonNull
    private RequestOptions getRequestOptions() {
        return new RequestOptions().diskCacheStrategy(DiskCacheStrategy.ALL);
    }

    private void applyProfilePicture(String profileUrl, final String name) {
        if (!TextUtils.isEmpty(profileUrl)) {
            Glide.with(ApplicationLoader.getInstance()).load(profileUrl).listener(new RequestListener<Drawable>() {
                @Override
                public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                    UiUtils.loadName(userPhotoView, name);
                    return false;
                }

                @Override
                public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                    return false;
                }
            }).into(userPhotoView);
        } else {
            if (StringUtils.isNotEmpty(name)) {
                UiUtils.loadName(userPhotoView, name);
            } else {
                userPhotoView.setImageResource(R.drawable.empty_profile);
            }
        }
    }

    public void loadParseUser(final ParseObject person, String searchString) {
        if (person != null) {
            signedInUser = AuthUtil.getCurrentUser();
            this.person = person;
            String userName = person.getString(AppConstants.APP_USER_DISPLAY_NAME);
            String userProfilePhoto = person.getString(AppConstants.APP_USER_PROFILE_PHOTO_URL);
            ParseGeoPoint userGeoPoint = person.getParseGeoPoint(AppConstants.APP_USER_GEO_POINT);
            if (StringUtils.isNotEmpty(userName)) {
                if (StringUtils.isNotEmpty(searchString)) {
                    usernameEntryView.setText(UiUtils.highlightTextIfNecessary(searchString, WordUtils.capitalize(userName),
                            ContextCompat.getColor(activity, R.color.hollout_color_three)));
                } else {
                    if (StringUtils.isNotEmpty(userName)) {
                        usernameEntryView.setText(WordUtils.capitalize(userName));
                    } else {
                        usernameEntryView.setText(" ");
                    }
                }
                // displaying the first letter of From in icon text
            }
            // display profile image
            applyProfilePicture(userProfilePhoto, userName);

            ParseGeoPoint signedInUserGeoPoint = signedInUser.getParseGeoPoint(AppConstants.APP_USER_GEO_POINT);
            if (signedInUserGeoPoint != null && userGeoPoint != null) {
                double distanceInKills = signedInUserGeoPoint.distanceInKilometersTo(userGeoPoint);
                String value = HolloutUtils.formatDistance(distanceInKills);
                UiUtils.setTextOnView(distanceToUserView, HolloutUtils.formatDistanceToUser(value) + "KM");
            } else {
                UiUtils.setTextOnView(distanceToUserView, " ");
            }

            List<String> aboutUser = person.getList(AppConstants.ABOUT_USER);
            List<String> aboutSignedInUser = signedInUser.getList(AppConstants.ABOUT_USER);
            if (aboutUser != null && aboutSignedInUser != null) {
                try {
                    String aboutUserString = TextUtils.join(",", aboutUser);
                    if (StringUtils.isNotEmpty(searchString)) {
                        aboutPerson.setText(UiUtils.highlightTextIfNecessary(searchString, WordUtils.capitalize(aboutUserString),
                                ContextCompat.getColor(activity, R.color.hollout_color_three)));
                    } else {
                        if (StringUtils.isNotEmpty(aboutUserString)) {
                            aboutPerson.setText(WordUtils.capitalize(aboutUserString));
                        } else {
                            aboutPerson.setText(" ");
                        }
                    }
                } catch (NullPointerException ignored) {

                }
            }

            String userStatusString = person.getString(AppConstants.APP_USER_STATUS);
            if (StringUtils.isNotEmpty(userStatusString) && UiUtils.canShowStatus(person, AppConstants.ENTITY_TYPE_CLOSEBY, new HashMap<String, Object>())) {
                userStatus.setText(userStatusString);
            } else {
                userStatus.setText(activity.getString(R.string.hey_there_holla_me_on_hollout));
            }

            String userOnlineStatus = person.getString(AppConstants.APP_USER_ONLINE_STATUS);
            if (userOnlineStatus != null && UiUtils.canShowPresence(person, AppConstants.ENTITY_TYPE_CLOSEBY, new HashMap<String, Object>())) {
                if (timeStampsAreTheSame(person)
                        && HolloutUtils.isNetWorkConnected(activity)) {
                    UiUtils.showView(onlineStatusView, true);
                } else {
                    UiUtils.showView(onlineStatusView, false);
                }
            } else {
                UiUtils.showView(onlineStatusView, false);
            }
            userPhotoView.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View view) {
                    Intent intent = new Intent(activity, UserPhotoPreviewActivity.class);
                    intent.putExtra(AppConstants.EXTRA_USER, person);
                    intent.putExtra(activity.getResources().getString(R.string.view_info),
                            UiUtils.captureValues(activity, userPhotoView));
                    activity.startActivity(intent);
                    activity.overridePendingTransition(0, 0);
                }

            });

        }

    }

    private boolean timeStampsAreTheSame(ParseObject person) {
        return person.getLong(AppConstants.USER_CURRENT_TIME_STAMP)
                == signedInUser.getLong(AppConstants.USER_CURRENT_TIME_STAMP);
    }

    private void subscribeToUserChanges() {
        if (person != null) {
            userStateQuery = ParseQuery.getQuery(AppConstants.PEOPLE_GROUPS_AND_ROOMS);
            userStateQuery.whereEqualTo(AppConstants.REAL_OBJECT_ID, person.getString(AppConstants.REAL_OBJECT_ID));
            try {
                SubscriptionHandling<ParseObject> subscriptionHandling = ApplicationLoader.getParseLiveQueryClient().subscribe(userStateQuery);
                subscriptionHandling.handleEvent(SubscriptionHandling.Event.UPDATE, new SubscriptionHandling.HandleEventCallback<ParseObject>() {
                    @Override
                    public void onEvent(ParseQuery<ParseObject> query, final ParseObject object) {
                        post(new Runnable() {
                            @Override
                            public void run() {
                                String newObjectRealId = object.getString(AppConstants.REAL_OBJECT_ID);
                                String personId = person.getString(AppConstants.REAL_OBJECT_ID);
                                HolloutLogger.d("ObjectUpdate", "A new object has changed. Object Id = " + newObjectRealId + " RefObjectId = " + personId);
                                if (newObjectRealId.equals(personId)) {
                                    loadParseUser(object, searchString);
                                }
                            }
                        });
                    }
                });
            } catch (NullPointerException ignored) {

            }
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        subscribeToUserChanges();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        unSubscribeFromUserChanges();
    }

    private void unSubscribeFromUserChanges() {
        try {
            if (userStateQuery != null) {
                ApplicationLoader.getParseLiveQueryClient().unsubscribe(userStateQuery);
            }
        } catch (NullPointerException ignored) {

        }
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        ButterKnife.bind(this);
    }

    @Override
    public void onClick(View v) {
        Intent viewProfileIntent = new Intent(activity, UserProfileActivity.class);
        viewProfileIntent.putExtra(AppConstants.USER_PROPERTIES, person);
        activity.startActivity(viewProfileIntent);
    }

    @Override
    public boolean onLongClick(View v) {
        return false;
    }

}
