package com.wan.hollout.ui.widgets;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SubscriptionHandling;
import com.wan.hollout.R;
import com.wan.hollout.components.ApplicationLoader;
import com.wan.hollout.ui.activities.UserProfileActivity;
import com.wan.hollout.ui.helpers.CircleTransform;
import com.wan.hollout.utils.AppConstants;
import com.wan.hollout.utils.HolloutUtils;
import com.wan.hollout.utils.UiUtils;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.WordUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * @author Wan Clem
 */
@SuppressWarnings("unused")
public class PersonView extends RelativeLayout implements View.OnClickListener, View.OnLongClickListener {

    @BindView(R.id.user_online_status)
    ImageView userOnlineStatusView;

    @BindView(R.id.from)
    HolloutTextView usernameEntryView;

    @BindView(R.id.txt_primary)
    HolloutTextView aboutPerson;

    @BindView(R.id.txt_secondary)
    HolloutTextView userStatus;

    @BindView(R.id.distance_to_user)
    TextView distanceToUserView;

    @BindView(R.id.icon_text)
    TextView iconText;

    @BindView(R.id.timestamp)
    TextView userLocationView;

    @BindView(R.id.icon_back)
    RelativeLayout iconBack;

    @BindView(R.id.icon_front)
    RelativeLayout iconFront;

    @BindView(R.id.icon_profile)
    ImageView userPhotoView;

    @BindView(R.id.message_container)
    LinearLayout messageContainer;

    @BindView(R.id.icon_container)
    RelativeLayout iconContainer;

    public ParseUser person;

    private ParseObject signedInUser;
    public Activity activity;

    private ParseQuery<ParseUser> userStateQuery;

    private String searchString;

    public PersonView(Context context) {
        this(context, null);
    }

    public PersonView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PersonView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        inflate(context, R.layout.person_to_meet, this);
    }

    private void init() {
        setOnClickListener(this);
        setOnLongClickListener(this);
    }

    @Override
    protected void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    public void bindData(Activity activity, String searchString, ParseUser person) {
        this.searchString = searchString;
        this.activity = activity;
        this.person = person;
        signedInUser = ParseUser.getCurrentUser();
        init();
        loadParseUser(searchString);
    }

    private void applyProfilePicture(String profileUrl) {
        if (!TextUtils.isEmpty(profileUrl)) {
            Glide.with(activity).load(profileUrl)
                    .thumbnail(0.5f)
                    .crossFade()
                    .transform(new CircleTransform(activity))
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(userPhotoView);
            userPhotoView.setColorFilter(null);
            iconText.setVisibility(View.GONE);
        } else {
            userPhotoView.setImageResource(R.drawable.bg_circle);
            userPhotoView.setColorFilter(getRandomMaterialColor("400"));
            iconText.setVisibility(View.VISIBLE);
        }
    }

    private void applyIconAnimation() {
        iconBack.setVisibility(View.GONE);
        resetIconYAxis(iconFront);
        iconFront.setVisibility(View.VISIBLE);
        iconFront.setAlpha(1);
    }

    private void resetIconYAxis(View view) {
        if (view.getRotationY() != 0) {
            view.setRotationY(0);
        }
    }

    /**
     * chooses a random color from array.xml
     */
    private int getRandomMaterialColor(String typeColor) {
        int returnColor = Color.GRAY;
        int arrayId = getResources().getIdentifier("mdcolor_" + typeColor, "array", activity.getPackageName());

        if (arrayId != 0) {
            TypedArray colors = getResources().obtainTypedArray(arrayId);
            int index = (int) (Math.random() * colors.length());
            returnColor = colors.getColor(index, Color.GRAY);
            colors.recycle();
        }
        return returnColor;
    }

    public void loadParseUser(String searchString) {
        if (person != null) {
            String userName = person.getString(AppConstants.APP_USER_DISPLAY_NAME);
            String userProfilePhoto = person.getString(AppConstants.APP_USER_PROFILE_PHOTO_URL);
            ParseGeoPoint userGeoPoint = person.getParseGeoPoint(AppConstants.APP_USER_GEO_POINT);
            if (StringUtils.isNotEmpty(userName)) {
                if (StringUtils.isNotEmpty(searchString)) {
                    usernameEntryView.setText(UiUtils.highlightTextIfNecessary(searchString, WordUtils.capitalize(userName),
                            ContextCompat.getColor(activity, R.color.hollout_color_three)));
                } else {
                    usernameEntryView.setText(WordUtils.capitalize(userName));
                }
                // displaying the first letter of From in icon text
                iconText.setText(WordUtils.capitalize(userName.substring(0, 1)));
            }
            // display profile image
            applyProfilePicture(userProfilePhoto);
            applyIconAnimation();

            if (UiUtils.canShowLocation(person, AppConstants.ENTITY_TYPE_CLOSEBY, null) && userName.length() <= 15) {
                String userCurrentLocation = HolloutUtils.resolveToBestLocation(person);
                if (userCurrentLocation != null) {
                    userLocationView.setText(userCurrentLocation.toUpperCase(Locale.getDefault()));
                } else {
                    UiUtils.setTextOnView(userLocationView, " ");
                    userLocationView.invalidate();
                }
                userLocationView.invalidate();
            } else {
                UiUtils.setTextOnView(userLocationView, " ");
                userLocationView.invalidate();
            }

            ParseGeoPoint signedInUserGeoPoint = signedInUser.getParseGeoPoint(AppConstants.APP_USER_GEO_POINT);
            if (signedInUserGeoPoint != null && userGeoPoint != null) {
                double distanceInKills = signedInUserGeoPoint.distanceInKilometersTo(userGeoPoint);
                String value = HolloutUtils.formatDistance(distanceInKills);
                UiUtils.setTextOnView(distanceToUserView, value + "KM");
            } else {
                UiUtils.setTextOnView(distanceToUserView, " ");
            }

            List<String> aboutUser = person.getList(AppConstants.ABOUT_USER);
            List<String> aboutSignedInUser = signedInUser.getList(AppConstants.ABOUT_USER);
            if (aboutUser != null && aboutSignedInUser != null) {
                try {
                    List<String> common = new ArrayList<>(aboutUser);
                    common.retainAll(aboutSignedInUser);
                    String firstInterest = !common.isEmpty() ? common.get(0) : aboutUser.get(0);
                    if (StringUtils.isNotEmpty(searchString)) {
                        aboutPerson.setText(UiUtils.highlightTextIfNecessary(searchString, WordUtils.capitalize(firstInterest),
                                ContextCompat.getColor(activity, R.color.hollout_color_three)));
                    } else {
                        aboutPerson.setText(WordUtils.capitalize(firstInterest));
                    }
                } catch (NullPointerException ignored) {

                }
            }

            String userStatusString = person.getString(AppConstants.APP_USER_STATUS);
            if (userStatusString != null) {
                userStatus.setText(userStatusString);
            } else {
                userStatus.setText(activity.getString(R.string.hey_there_holla_me_on_hollout));
            }

            String userOnlineStatus = person.getString(AppConstants.APP_USER_ONLINE_STATUS);
            if (userOnlineStatus != null) {
                if (person.getString(AppConstants.APP_USER_ONLINE_STATUS).equals(AppConstants.ONLINE)
                        && HolloutUtils.isNetWorkConnected(activity)) {
                    userOnlineStatusView.setImageResource(R.drawable.ic_online);
                } else {
                    userOnlineStatusView.setImageResource(R.drawable.ic_offline_grey);
                }
            } else {
                userOnlineStatusView.setImageResource(R.drawable.ic_offline_grey);
            }

            userPhotoView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View view) {
                    UiUtils.blinkView(view);
                    UiUtils.loadUserData(activity, person);
                }
            });

            messageContainer.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View view) {
                    PersonView.this.performClick();
                }

            });

            iconContainer.setOnClickListener(new OnClickListener() {

                @Override
                public void onClick(View view) {
                    PersonView.this.performClick();
                }

            });

        }

    }

    private void subscribeToUserChanges() {
        if (person != null) {
            userStateQuery = ParseUser.getQuery();
            userStateQuery.whereEqualTo("objectId", person.getObjectId());
            SubscriptionHandling<ParseUser> subscriptionHandling = ApplicationLoader.getParseLiveQueryClient().subscribe(userStateQuery);
            subscriptionHandling.handleEvent(SubscriptionHandling.Event.UPDATE, new SubscriptionHandling.HandleEventCallback<ParseUser>() {
                @Override
                public void onEvent(ParseQuery<ParseUser> query, final ParseUser object) {
                    post(new Runnable() {
                        @Override
                        public void run() {
                            person = object;
                            loadParseUser(searchString);
                        }
                    });
                }
            });
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
