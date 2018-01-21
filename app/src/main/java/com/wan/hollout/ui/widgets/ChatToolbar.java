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
import android.widget.TextView;

import com.hyphenate.chat.EMMessage;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.SubscriptionHandling;
import com.wan.hollout.R;
import com.wan.hollout.components.ApplicationLoader;
import com.wan.hollout.eventbuses.SearchMessages;
import com.wan.hollout.ui.activities.ChatActivity;
import com.wan.hollout.ui.activities.SelectPeopleToForwardMessageActivity;
import com.wan.hollout.ui.activities.UserProfileActivity;
import com.wan.hollout.utils.AppConstants;
import com.wan.hollout.utils.AuthUtil;
import com.wan.hollout.utils.HolloutLogger;
import com.wan.hollout.utils.HolloutUtils;
import com.wan.hollout.utils.RequestCodes;
import com.wan.hollout.utils.UiUtils;
import com.wang.avi.AVLoadingIndicatorView;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.WordUtils;
import org.greenrobot.eventbus.EventBus;
import org.json.JSONObject;

import java.util.Calendar;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * @author Wan Clem
 */

@SuppressWarnings("StatementWithEmptyBody")
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
    AVLoadingIndicatorView typingIndicator;

    @BindView(R.id.action_mode_bar)
    RelativeLayout actionModeBar;

    @BindView(R.id.reply_to_a_message)
    ImageView replyToMessageView;

    @BindView(R.id.delete_message)
    ImageView deleteMessageView;

    @BindView(R.id.copy_message)
    ImageView copyMessageView;

    @BindView(R.id.forward_message)
    ImageView forwardMessageView;

    @BindView(R.id.destroy_action_mode)
    ImageView destroyActionModeView;

    @BindView(R.id.action_item_selection_count)
    TextView selectedItemCountView;

    @BindView(R.id.main_search_view)
    MaterialSearchView searchView;

    @BindView(R.id.bottom_shadow)
    View bottomShadow;

    public ChatActivity mContext;

    public String recipientChatId;
    public int recipientType;

    public ParseObject signedInUserObject;
    private ParseObject recipientObject;

    private ParseQuery<ParseObject> recipientObjectStateQuery;

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
        this.signedInUserObject = AuthUtil.getCurrentUser();
        setRecipientChatId(recipientChatId);
        setRecipientType(recipientType);
        attachCallbacks();
    }

    public void setLastSeenText(final long userLastSeen) {
        if (UiUtils.canShowPresence(recipientObject, AppConstants.ENTITY_TYPE_CHATS, null)) {
            UiUtils.showView(contactSubtitleLayout, true);
            contactSubTitle.setTextColor(ContextCompat.getColor(getContext(), R.color.white));
            final String fullLastSeenTExt = getLastSeen(userLastSeen);
            contactSubTitle.setText(fullLastSeenTExt);
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    contactSubTitle.setText(StringUtils.strip(StringUtils.remove(StringUtils.remove(fullLastSeenTExt, "Active"), "on")));
                }
            }, 2000);
        } else {
            UiUtils.showView(contactSubtitleLayout, false);
        }
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

    public String getRecipientName() {
        if (recipientObject.getString(AppConstants.OBJECT_TYPE).equals(AppConstants.OBJECT_TYPE_INDIVIDUAL)) {
            return recipientObject.getString(AppConstants.APP_USER_DISPLAY_NAME);
        } else {
            recipientObject.getString(AppConstants.GROUP_OR_CHAT_ROOM_NAME);
        }
        return "Unknown User";
    }

    public String getRecipientPhotoUrl() {
        if (recipientObject.getString(AppConstants.OBJECT_TYPE).equals(AppConstants.OBJECT_TYPE_INDIVIDUAL)) {
            return recipientObject.getString(AppConstants.APP_USER_PROFILE_PHOTO_URL);
        } else {
            return recipientObject.getString(AppConstants.GROUP_OR_CHAT_ROOM_PHOTO_URL);
        }
    }

    private boolean userConnected() {
        return HolloutUtils.isNetWorkConnected(getContext());
    }

    public void refreshToolbar(ParseObject recipientUser) {
        this.recipientObject = recipientUser;
        String recipientName = getRecipientName();
        String recipientPhotoUrl = getRecipientPhotoUrl();
        if (StringUtils.isNotEmpty(recipientName)) {
            mContext.setRecipientName(recipientName);
            contactNameView.setText(WordUtils.capitalize(recipientName));
        }
        if (recipientObject.getString(AppConstants.OBJECT_TYPE).equals(AppConstants.OBJECT_TYPE_INDIVIDUAL)) {
            JSONObject chatStates = recipientUser.getJSONObject(AppConstants.APP_USER_CHAT_STATES);
            Long userLastSeen = recipientUser.getLong(AppConstants.APP_USER_LAST_SEEN);

            if (chatStates != null) {
                String chatStateToSignedInUser = chatStates.optString(signedInUserObject.getString(AppConstants.REAL_OBJECT_ID));
                String userOnlineStatus = recipientUser.getString(AppConstants.APP_USER_ONLINE_STATUS);
                if (chatStateToSignedInUser != null) {
                    if (chatStateToSignedInUser.equals(mContext.getString(R.string.idle)) && userOnlineStatus.equals(AppConstants.ONLINE) && userConnected()) {
                        UiUtils.showView(contactSubtitleLayout, true);
                        contactSubTitle.setText(mContext.getString(R.string.online));
                        UiUtils.showView(typingIndicator, false);
                    } else if (chatStateToSignedInUser.contains(mContext.getString(R.string.typing)) && userOnlineStatus.equals(AppConstants.ONLINE)) {
                        UiUtils.showView(contactSubtitleLayout, true);
                        UiUtils.showView(typingIndicator, true);
                        contactSubTitle.setText(StringUtils.strip(mContext.getString(R.string.typing_), "…"));
                        UiUtils.bangSound(getContext(), R.raw.typing);
                    } else {
                        if (userOnlineStatus.equals(AppConstants.ONLINE) && userConnected()) {
                            UiUtils.showView(contactSubtitleLayout, true);
                            contactSubTitle.setText(mContext.getString(R.string.online));
                            UiUtils.showView(typingIndicator, false);
                        } else {
                            UiUtils.showView(typingIndicator, false);
                            setLastSeenText(userLastSeen);
                        }
                    }
                } else {
                    if (userOnlineStatus.equals(AppConstants.ONLINE) && userConnected()) {
                        UiUtils.showView(contactSubtitleLayout, true);
                        contactSubTitle.setText(mContext.getString(R.string.online));
                        UiUtils.showView(typingIndicator, false);
                    } else {
                        UiUtils.showView(typingIndicator, false);
                        setLastSeenText(userLastSeen);
                    }
                }
            } else {
                String userOnlineStatus = recipientUser.getString(AppConstants.APP_USER_ONLINE_STATUS);
                if (userOnlineStatus != null && userOnlineStatus.equals(AppConstants.ONLINE) && userConnected()) {
                    UiUtils.showView(contactSubtitleLayout, true);
                    contactSubTitle.setText(mContext.getString(R.string.online));
                } else {
                    setLastSeenText(userLastSeen);
                }
            }
        }
        if (StringUtils.isNotEmpty(recipientPhotoUrl)) {
            UiUtils.loadImage(mContext, recipientPhotoUrl, contactPhotoView);
        }
    }

    private void subscribeToObjectChanges() {
        if (recipientObject != null) {
            recipientObjectStateQuery = ParseQuery.getQuery(AppConstants.PEOPLE_GROUPS_AND_ROOMS);
            recipientObjectStateQuery.whereEqualTo(AppConstants.REAL_OBJECT_ID, recipientObject.getString(AppConstants.REAL_OBJECT_ID));
            try {
                SubscriptionHandling<ParseObject> subscriptionHandling = ApplicationLoader.getParseLiveQueryClient().subscribe(recipientObjectStateQuery);
                subscriptionHandling.handleEvent(SubscriptionHandling.Event.UPDATE, new SubscriptionHandling.HandleEventCallback<ParseObject>() {
                    @Override
                    public void onEvent(ParseQuery<ParseObject> query, final ParseObject object) {
                        post(new Runnable() {
                            @Override
                            public void run() {
                                refreshToolbar(object);
                            }
                        });
                    }
                });
            } catch (NullPointerException ignored) {

            }

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
                if (recipientObject != null && recipientObject.getString(AppConstants.OBJECT_TYPE).equals(AppConstants.OBJECT_TYPE_INDIVIDUAL)) {
                    if (actionModeBar.getVisibility() == GONE) {
                        Intent userProfileIntent = new Intent(mContext, UserProfileActivity.class);
                        userProfileIntent.putExtra(AppConstants.USER_PROPERTIES, recipientObject);
                        mContext.startActivity(userProfileIntent);
                    }
                } else if (recipientObject != null) {
                    //Start Group info activity here
                }
                break;
        }
    }

    public void openUserOrGroupProfile() {
        launchUserProfile.performClick();
    }

    public void openSearchView() {
        searchView.showSearch(true);
        UiUtils.showView(bottomShadow, false);
        searchView.setHint(getContext().getString(R.string.search_messages));
        searchView.setOnQueryTextListener(new MaterialSearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                EventBus.getDefault().post(new SearchMessages(newText));
                return false;
            }
        });
        searchView.setOnSearchViewListener(new MaterialSearchView.SearchViewListener() {
            @Override
            public void onSearchViewShown() {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        UiUtils.showKeyboard(searchView);
                    }
                }, 500);
            }

            @Override
            public void onSearchViewClosed() {

            }

        });

    }

    public boolean isSearchViewOpen() {
        return searchView.isSearchOpen();
    }

    public void closeSearch() {
        searchView.closeSearch();
        UiUtils.showView(bottomShadow, true);
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        subscribeToObjectChanges();
    }

    private void unSubscribeFromUserChanges() {
        if (recipientObjectStateQuery != null) {
            try {
                ApplicationLoader.getParseLiveQueryClient().unsubscribe(recipientObjectStateQuery);
            } catch (NullPointerException ignored) {

            }
        }
    }

    public void justHideActionMode() {
        UiUtils.showView(actionModeBar, false);
        AppConstants.selectedMessagesPositions.clear();
    }

    public void updateActionMode(int selectionCount) {
        UiUtils.showView(actionModeBar, selectionCount > 0);
        if (selectionCount > 1) {
            selectedItemCountView.setText(String.valueOf(selectionCount));
            UiUtils.showView(replyToMessageView, false);
            UiUtils.showView(copyMessageView, false);
            UiUtils.showView(forwardMessageView, false);
        } else {
            selectedItemCountView.setText(" ");
            UiUtils.showView(replyToMessageView, true);
            UiUtils.showView(forwardMessageView, true);
            if (!AppConstants.selectedMessages.isEmpty()) {
                EMMessage emMessage = AppConstants.selectedMessages.get(0);
                if (emMessage != null) {
                    UiUtils.showView(copyMessageView, emMessage.getType() == EMMessage.Type.TXT);
                }
            }
        }
        destroyActionModeView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                UiUtils.showView(actionModeBar, false);
                AppConstants.selectedMessages.clear();
                AppConstants.selectedMessagesPositions.clear();
            }
        });
        if (selectionCount == 0) {
            destroyActionModeView.performClick();
        }
        View.OnClickListener actionModeItemClickListener = new OnClickListener() {
            @Override
            public void onClick(View view) {
                UiUtils.blinkView(view);
                switch (view.getId()) {
                    case R.id.delete_message:
                        EventBus.getDefault().post(AppConstants.DELETE_ALL_SELECTED_MESSAGES);
                        break;
                    case R.id.reply_to_a_message:
                        EventBus.getDefault().post(AppConstants.REPLY_MESSAGE);
                        break;
                    case R.id.copy_message:
                        EventBus.getDefault().post(AppConstants.COPY_MESSAGE);
                        break;
                    case R.id.forward_message:
                        forwardMessage();
                        break;
                }
            }
        };
        replyToMessageView.setOnClickListener(actionModeItemClickListener);
        deleteMessageView.setOnClickListener(actionModeItemClickListener);
        copyMessageView.setOnClickListener(actionModeItemClickListener);
        forwardMessageView.setOnClickListener(actionModeItemClickListener);
    }

    private void forwardMessage() {
        ChatActivity chatActivity = ((ChatActivity) getContext());
        Intent messageIntent = new Intent(chatActivity, SelectPeopleToForwardMessageActivity.class);
        messageIntent.putExtra(AppConstants.EXTRA_FROM, chatActivity.getRecipient());
        chatActivity.startActivityForResult(messageIntent, RequestCodes.FORWARD_MESSAGE);
    }

    public boolean isActionModeActivated() {
        return actionModeBar.getVisibility() == VISIBLE;
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        unSubscribeFromUserChanges();
    }

}
