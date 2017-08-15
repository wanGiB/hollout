package com.wan.hollout.ui.widgets;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.support.design.widget.AppBarLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.Toolbar;
import android.text.method.ScrollingMovementMethod;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.parse.SubscriptionHandling;
import com.wan.hollout.R;
import com.wan.hollout.components.ApplicationLoader;
import com.wan.hollout.ui.activities.ChatActivity;
import com.wan.hollout.ui.activities.UserProfileActivity;
import com.wan.hollout.ui.widgets.dotloader.DotLoader;
import com.wan.hollout.utils.AppConstants;
import com.wan.hollout.utils.HolloutLogger;
import com.wan.hollout.utils.UiUtils;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.WordUtils;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * @author Wan Clem
 */

public class ChatToolbar extends AppBarLayout implements View.OnClickListener {

    @BindView(R.id.contact_photo)
    public CircleImageView contactPhotoView;

    @BindView(R.id.back_button)
    public ImageView goBack;

    @BindView(R.id.contact_name)
    public HolloutTextView contactNameView;

    @BindView(R.id.contact_subtitle)
    public HolloutTextView contactSubTitle;

    @BindView(R.id.launch_user_profile)
    public RelativeLayout launchUserProfile;

    @BindView(R.id.toolbar)
    public Toolbar toolbar;

    @BindView(R.id.contact_detail_layout)
    public LinearLayout contactDetailLayout;

    @BindView(R.id.contact_subtitle_layout)
    public LinearLayout contactSubtitleLayout;

    @BindView(R.id.typing_indicator)
    DotLoader typingIndicator;

    public ChatActivity mContext;

    public String recipientChatId;
    public int recipientType;

    public ParseUser signedInUserObject;
    private ParseUser recipientObject;
    private ParseQuery<ParseUser> recipientObjectStateQuery;

    public ChatToolbar(Context context) {
        this(context, null);
    }

    public ChatToolbar(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    protected void init(Context context) {
        LayoutInflater.from(context).inflate(R.layout.chat_tool_bar, this, true);
        ButterKnife.bind(this);
        if (!isInEditMode()) {
            if (context instanceof ChatActivity) {
                mContext = (ChatActivity) context;
            }
        }
    }

    public void setRecipientChatId(String recipientChatId) {
        this.recipientChatId = recipientChatId;
    }

    public void setRecipientType(int recipientType) {
        this.recipientType = recipientType;
    }

    public void initView(String recipientChatId, int recipientType) {
        this.signedInUserObject = ParseUser.getCurrentUser();
        setRecipientChatId(recipientChatId);
        setRecipientType(recipientType);
        attachCallbacks();
    }

    public void setLastSeenText(final long userLastSeen) {
        contactSubTitle.setTextColor(ContextCompat.getColor(getContext(),R.color.text_black));
        final String fullLastSeenTExt = getLastSeen(userLastSeen);
        contactSubTitle.setText(fullLastSeenTExt);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                contactSubTitle.setText(StringUtils.strip(StringUtils.remove(StringUtils.remove(fullLastSeenTExt, "Active"), "on")));
            }
        }, 2000);
    }

    public static String getLastSeen(long longTime) {
        try {
            Calendar rightNow = Calendar.getInstance();
            int day = rightNow.get(Calendar.DAY_OF_YEAR);
            int year = rightNow.get(Calendar.YEAR);

            Calendar dateCalendar = Calendar.getInstance();
            dateCalendar.setTimeInMillis(longTime);

            int dateDay = dateCalendar.get(Calendar.DAY_OF_YEAR);
            int dateYear = dateCalendar.get(Calendar.YEAR);

            if (dateDay == day && year == dateYear) {
                return "Active today at " + AppConstants.DATE_FORMATTER_IN_12HRS.format(new Date(longTime));
            } else if (dateDay + 1 == day && year == dateYear) {
                return "Active yesterday at " + AppConstants.DATE_FORMATTER_IN_12HRS.format(new Date(longTime));
            } else {
                String format = AppConstants.simpleDateFormat.format(new Date(longTime));
                return "Active on " + format + " at " + AppConstants.DATE_FORMATTER_IN_12HRS.format(new Date(longTime));
            }
        } catch (Exception e) {
            HolloutLogger.e("HolloutMessages", e);
        }
        return "Active";
    }

    public void attachCallbacks() {
        contactSubTitle.setMovementMethod(new ScrollingMovementMethod());
        goBack.setOnClickListener(this);
        launchUserProfile.setOnClickListener(this);
    }

    public void refreshToolbar(ParseUser recipientUser) {
        this.recipientObject = recipientUser;
        String username = recipientUser.getString(AppConstants.APP_USER_DISPLAY_NAME);
        String profilePhotoUrl = recipientUser.getString(AppConstants.APP_USER_PROFILE_PHOTO_URL);

        if (StringUtils.isNotEmpty(username)) {
            mContext.setRecipientName(username);
            contactNameView.setText(WordUtils.capitalize(username));
        }

        JSONObject chatStates = recipientUser.getJSONObject(AppConstants.APP_USER_CHAT_STATES);
        Long userLastSeen = recipientUser.getLong(AppConstants.APP_USER_LAST_SEEN);

        if (chatStates != null) {
            String chatStateToSignedInUser = chatStates.optString(signedInUserObject.getObjectId());
            String userOnlineStatus = recipientUser.getString(AppConstants.APP_USER_ONLINE_STATUS);
            if (chatStateToSignedInUser != null) {
                if (chatStateToSignedInUser.equals(mContext.getString(R.string.idle)) && userOnlineStatus.equals(AppConstants.ONLINE)) {
                    contactSubTitle.setText(mContext.getString(R.string.online));
                    contactSubTitle.setTextColor(ContextCompat.getColor(getContext(),R.color.colorGreen));
                    UiUtils.showView(typingIndicator, false);
                } else if (chatStateToSignedInUser.contains(mContext.getString(R.string.typing)) && userOnlineStatus.equals(AppConstants.ONLINE)) {
                    contactSubTitle.setText(StringUtils.strip(mContext.getString(R.string.typing_), "..."));
                    contactSubTitle.setTextColor(ContextCompat.getColor(getContext(),R.color.colorGreen));
                    UiUtils.showView(typingIndicator, true);
                } else {
                    if (userOnlineStatus.equals(AppConstants.ONLINE)) {
                        contactSubTitle.setText(mContext.getString(R.string.online));
                        contactSubTitle.setTextColor(ContextCompat.getColor(getContext(),R.color.colorGreen));
                        UiUtils.showView(typingIndicator, false);
                    } else {
                        UiUtils.showView(typingIndicator, false);
                        setLastSeenText(userLastSeen);
                    }
                }
            } else {
                if (userOnlineStatus.equals(AppConstants.ONLINE)) {
                    contactSubTitle.setText(mContext.getString(R.string.online));
                    contactSubTitle.setTextColor(ContextCompat.getColor(getContext(),R.color.colorGreen));
                    UiUtils.showView(typingIndicator, false);
                } else {
                    UiUtils.showView(typingIndicator, false);
                    setLastSeenText(userLastSeen);
                }
            }
        } else {
            String userOnlineStatus = recipientUser.getString(AppConstants.APP_USER_ONLINE_STATUS);
            if (userOnlineStatus != null && userOnlineStatus.equals(AppConstants.ONLINE)) {
                contactSubTitle.setText(mContext.getString(R.string.online));
                contactSubTitle.setTextColor(ContextCompat.getColor(getContext(),R.color.colorGreen));
            } else {
                setLastSeenText(userLastSeen);
            }
        }
        if (StringUtils.isNotEmpty(profilePhotoUrl)) {
            UiUtils.loadImage(mContext, profilePhotoUrl, contactPhotoView);
        }
    }

    private void subscribeToUserChanges() {
        if (recipientObject != null) {
            recipientObjectStateQuery = ParseUser.getQuery();
            recipientObjectStateQuery.whereEqualTo("objectId", recipientObject.getObjectId());
            SubscriptionHandling<ParseUser> subscriptionHandling = ApplicationLoader.getParseLiveQueryClient().subscribe(recipientObjectStateQuery);
            subscriptionHandling.handleEvent(SubscriptionHandling.Event.UPDATE, new SubscriptionHandling.HandleEventCallback<ParseUser>() {
                @Override
                public void onEvent(ParseQuery<ParseUser> query, final ParseUser object) {
                    post(new Runnable() {
                        @Override
                        public void run() {
                            refreshToolbar(object);
                        }
                    });
                }
            });
        }
    }

    public Toolbar getToolbar() {
        return toolbar;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.back_button:
                mContext.finish();
                break;
            case R.id.launch_user_profile:
                if (recipientObject != null) {
                    Intent userProfileIntent = new Intent(mContext, UserProfileActivity.class);
                    userProfileIntent.putExtra(AppConstants.USER_PROPERTIES, recipientObject);
                    mContext.startActivity(userProfileIntent);
                }
                break;
        }
    }

    public void openUserOrGroupProfile() {
        launchUserProfile.performClick();
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        subscribeToUserChanges();
    }

    private void unSubscribeFromUserChanges() {
        if (recipientObjectStateQuery != null) {
            try {
                ApplicationLoader.getParseLiveQueryClient().unsubscribe(recipientObjectStateQuery);
            } catch (NullPointerException ignored) {

            }
        }
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        unSubscribeFromUserChanges();
    }

}
