package com.wan.hollout.ui.widgets;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.os.Build;
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
import com.bumptech.glide.load.resource.gif.GifDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.hyphenate.EMCallBack;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMConversation;
import com.hyphenate.chat.EMLocationMessageBody;
import com.hyphenate.chat.EMMessage;
import com.hyphenate.chat.EMTextMessageBody;
import com.hyphenate.exceptions.HyphenateException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.SubscriptionHandling;
import com.wan.hollout.R;
import com.wan.hollout.animations.KeyframesDrawable;
import com.wan.hollout.animations.KeyframesDrawableBuilder;
import com.wan.hollout.animations.deserializers.KFImageDeserializer;
import com.wan.hollout.animations.model.KFImage;
import com.wan.hollout.utils.ChatUtils;
import com.wan.hollout.components.ApplicationLoader;
import com.wan.hollout.models.ConversationItem;
import com.wan.hollout.ui.activities.ChatActivity;
import com.wan.hollout.ui.activities.MainActivity;
import com.wan.hollout.ui.helpers.CircleTransform;
import com.wan.hollout.utils.AppConstants;
import com.wan.hollout.utils.AuthUtil;
import com.wan.hollout.utils.HolloutLogger;
import com.wan.hollout.utils.HolloutPreferences;
import com.wan.hollout.utils.HolloutUtils;
import com.wan.hollout.utils.UiUtils;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.WordUtils;
import org.greenrobot.eventbus.EventBus;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * @author Wan Clem
 */
@SuppressWarnings("FieldCanBeLocal")
public class ConversationItemView extends RelativeLayout implements View.OnClickListener, View.OnLongClickListener {

    @BindView(R.id.user_online_status)
    ImageView userOnlineStatusView;

    @BindView(R.id.from)
    HolloutTextView usernameEntryView;

    @BindView(R.id.txt_secondary)
    ChatMessageTextView userStatusOrLastMessageView;

    @BindView(R.id.unread_message_indicator)
    TextView unreadMessagesCountView;

    @BindView(R.id.icon_text)
    TextView iconText;

    @BindView(R.id.timestamp)
    TextView msgTimeStampView;

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

    @BindView(R.id.delivery_status_view)
    ImageView deliveryStatusView;

    @BindView(R.id.parent_layout)
    View parentView;

    @BindView(R.id.reactions_indicator)
    ImageView reactionsIndicatorView;

    protected EMCallBack messageSendCallback;
    protected EMCallBack messageReceiveCallback;

    public ParseObject parseObject;
    public Activity activity;

    private ParseQuery<ParseObject> objectStateQuery;

    private String searchString;

    private EMConversation emConversation;
    private EMMessage lastMessage;

    private ParseObject signedInUserObject;

    private static KeyframesDrawable imageDrawable;
    private static InputStream stream;

    public ConversationItemView(Context context) {
        this(context, null);
    }

    public ConversationItemView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ConversationItemView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        inflate(context, R.layout.conversation_item, this);
    }

    private void init() {
        setOnClickListener(this);
        setOnLongClickListener(this);
        parentView.setOnClickListener(this);
    }

    public void bindData(Activity activity, String searchString, ParseObject parseObject) {
        this.searchString = searchString;
        this.activity = activity;
        this.signedInUserObject = AuthUtil.getCurrentUser();
        this.parseObject = parseObject;
        this.emConversation = EMClient.getInstance()
                .chatManager().getConversation(parseObject.getString(AppConstants.REAL_OBJECT_ID),
                        ChatUtils.getConversationType(parseObject.getString(AppConstants.OBJECT_TYPE).equals(AppConstants.OBJECT_TYPE_INDIVIDUAL)
                                ? AppConstants.CHAT_TYPE_SINGLE
                                : (parseObject.getString(AppConstants.OBJECT_TYPE).equals(AppConstants.OBJECT_TYPE_GROUP))
                                ? AppConstants.CHAT_TYPE_GROUP : AppConstants.CHAT_TYPE_ROOM), true);

        init();
        setupConversation(parseObject, searchString);
        invalidateViewOnScroll();
    }

    private void invalidateItemView(Activity activity, int objectHashCode) {
        parentView.setBackgroundColor(AppConstants.selectedPeoplePositions.get(objectHashCode)
                ? ContextCompat.getColor(activity, R.color.blue_100) : android.R.attr.selectableItemBackground);
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

    public int getMessageId() {
        return getCurrentConversationHashCode();
    }

    public void setupConversation(final ParseObject parseObject, String searchString) {
        if (parseObject != null) {
            this.parseObject = parseObject;
            String userName = parseObject.getString(AppConstants.OBJECT_TYPE).equals(AppConstants.OBJECT_TYPE_INDIVIDUAL)
                    ? parseObject.getString(AppConstants.APP_USER_DISPLAY_NAME)
                    : parseObject.getString(AppConstants.GROUP_OR_CHAT_ROOM_NAME);

            String userProfilePhoto = parseObject.getString(AppConstants.OBJECT_TYPE).equals(AppConstants.OBJECT_TYPE_INDIVIDUAL)
                    ? parseObject.getString(AppConstants.APP_USER_PROFILE_PHOTO_URL)
                    : parseObject.getString(AppConstants.GROUP_OR_CHAT_ROOM_PHOTO_URL);

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

            attachEventHandlers(parseObject);

            if (HolloutUtils.isUserBlocked(parseObject.getString(AppConstants.REAL_OBJECT_ID))) {
                userStatusOrLastMessageView.setText(activity.getString(R.string.user_blocked));
                userPhotoView.setColorFilter(ContextCompat.getColor(activity, R.color.black_transparent_70percent), PorterDuff.Mode.SRC_ATOP);
                listenToUserPresence(parseObject);
                return;
            }

            if (emConversation != null) {
                int unreadMessagesCount = emConversation.getUnreadMsgCount();
                if (unreadMessagesCount > 0 && lastMessage != null && lastMessage.direct() == EMMessage.Direct.RECEIVE) {
                    UiUtils.showView(unreadMessagesCountView, true);
                    unreadMessagesCountView.setText(String.valueOf(unreadMessagesCount));
                    AppConstants.unreadMessagesPositions.put(getMessageId(), true);
                    userStatusOrLastMessageView.setTextColor(Color.BLACK);
                    userStatusOrLastMessageView.setTypeface(null, Typeface.BOLD);
                } else {
                    UiUtils.showView(unreadMessagesCountView, false);
                    AppConstants.unreadMessagesPositions.put(getMessageId(), false);
                    userStatusOrLastMessageView.setTypeface(null, Typeface.NORMAL);
                    userStatusOrLastMessageView.setTextColor(ContextCompat.getColor(activity, R.color.message));
                }
                lastMessage = emConversation.getLastMessage();
                if (parseObject.getString(AppConstants.OBJECT_TYPE).equals(AppConstants.OBJECT_TYPE_INDIVIDUAL)) {
                    JSONObject chatStates = parseObject.getJSONObject(AppConstants.APP_USER_CHAT_STATES);
                    if (chatStates != null) {
                        String chatStateToSignedInUser = chatStates.optString(signedInUserObject.getString(AppConstants.REAL_OBJECT_ID));
                        if (chatStateToSignedInUser.contains(activity.getString(R.string.typing))
                                && parseObject.getString(AppConstants.APP_USER_ONLINE_STATUS)
                                .equals(AppConstants.ONLINE)) {
                            userStatusOrLastMessageView.setTypeface(null, Typeface.BOLD);
                            userStatusOrLastMessageView.setText(activity.getString(R.string.typing));
                            userStatusOrLastMessageView.setTextColor(ContextCompat.getColor(getContext(), R.color.hollout_color_one));
                            UiUtils.showView(deliveryStatusView, false);
                            AppConstants.lastMessageAvailablePositions.put(getMessageId(), false);
                        } else {
                            setupDefaults();
                        }
                    } else {
                        setupDefaults();
                    }
                } else {
                    setupDefaults();
                }
            }

            if (parseObject.getString(AppConstants.OBJECT_TYPE).equals(AppConstants.OBJECT_TYPE_INDIVIDUAL)) {
                listenToUserPresence(parseObject);
            } else {
                UiUtils.showView(userOnlineStatusView, false);
                AppConstants.parseUserAvailableOnlineStatusPositions.put(getMessageId(), false);
            }

        }

        invalidateItemView(activity, getCurrentConversationHashCode());

    }

    private void attachEventHandlers(final ParseObject parseObject) {
        userPhotoView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                UiUtils.blinkView(view);
                if (parseObject.getString(AppConstants.OBJECT_TYPE).equals(AppConstants.OBJECT_TYPE_INDIVIDUAL)) {
                    UiUtils.loadUserData(activity, parseObject);
                }
            }
        });

        messageContainer.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                ConversationItemView.this.performClick();
            }
        });

        iconContainer.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                ConversationItemView.this.performClick();
            }
        });

        usernameEntryView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                ConversationItemView.this.performClick();
            }
        });
        userStatusOrLastMessageView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                ConversationItemView.this.performClick();
            }
        });

        OnLongClickListener onLongClickListener = new OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                switch (v.getId()) {
                    case R.id.icon_profile:
                    case R.id.message_container:
                    case R.id.icon_container:
                    case R.id.from:
                    case R.id.txt_secondary:
                        ConversationItemView.this.performLongClick();
                        break;
                }
                return true;
            }
        };

        userPhotoView.setOnLongClickListener(onLongClickListener);
        usernameEntryView.setOnLongClickListener(onLongClickListener);
        userStatusOrLastMessageView.setOnLongClickListener(onLongClickListener);
        iconContainer.setOnLongClickListener(onLongClickListener);
        messageContainer.setOnLongClickListener(onLongClickListener);
    }

    private void listenToUserPresence(ParseObject parseObject) {
        UiUtils.showView(userOnlineStatusView, true);
        String userOnlineStatus = parseObject.getString(AppConstants.APP_USER_ONLINE_STATUS);
        if (userOnlineStatus != null && UiUtils.canShowPresence(parseObject, AppConstants.ENTITY_TYPE_CHATS, null)) {
            if (parseObject.getString(AppConstants.APP_USER_ONLINE_STATUS).equals(AppConstants.ONLINE)
                    && HolloutUtils.isNetWorkConnected(activity)) {
                userOnlineStatusView.setImageResource(R.drawable.ic_online);
                AppConstants.onlinePositions.put(getMessageId(), true);
            } else {
                userOnlineStatusView.setImageResource(R.drawable.ic_offline_grey);
                AppConstants.onlinePositions.put(getMessageId(), false);
            }
        } else {
            userOnlineStatusView.setImageResource(R.drawable.ic_offline_grey);
            AppConstants.onlinePositions.put(getMessageId(), false);
        }
        AppConstants.parseUserAvailableOnlineStatusPositions.put(getMessageId(), true);
    }

    private void setupDefaults() {
        if (lastMessage != null) {
            HolloutLogger.d("LastMessageTracker", "Last Message in conversation is not null");
            UiUtils.showView(msgTimeStampView, true);
            long lastMessageTime = lastMessage.getMsgTime();
            parseObject.put(AppConstants.LAST_CONVERSATION_TIME_WITH, lastMessageTime);
            Date msgDate = new Date(lastMessageTime);
            if (msgDate.equals(new Date())) {
                //Msg received date = today
                String msgTime = AppConstants.DATE_FORMATTER_IN_12HRS.format(msgDate);
                msgTimeStampView.setText(msgTime);
            } else {
                msgTimeStampView.setText(UiUtils.getDaysAgo(AppConstants.DATE_FORMATTER_IN_BIRTHDAY_FORMAT.format(msgDate)) + " at " + AppConstants.DATE_FORMATTER_IN_12HRS.format(msgDate));
            }
            AppConstants.lastMessageAvailablePositions.put(getMessageId(), true);
            setupLastMessage(lastMessage);
        } else {
            HolloutLogger.d("LastMessageTracker", "Last Message in conversation is null");
            parseObject.put(AppConstants.LAST_CONVERSATION_TIME_WITH, 0);
            UiUtils.showView(msgTimeStampView, false);
            AppConstants.lastMessageAvailablePositions.put(getMessageId(), false);
            if (parseObject.getString(AppConstants.OBJECT_TYPE).equals(AppConstants.OBJECT_TYPE_INDIVIDUAL)) {
                String userStatusString = parseObject.getString(AppConstants.APP_USER_STATUS);
                if (StringUtils.isNotEmpty(userStatusString) && UiUtils.canShowStatus(parseObject, AppConstants.ENTITY_TYPE_CHATS, null)) {
                    userStatusOrLastMessageView.setText(userStatusString);
                } else {
                    userStatusOrLastMessageView.setText(activity.getString(R.string.hey_there_holla_me_on_hollout));
                }
            } else {
                String groupDescription = parseObject.getString(AppConstants.ROOM_DESCRIPTION);
                if (StringUtils.isNotEmpty(groupDescription)) {
                    userStatusOrLastMessageView.setText(groupDescription);
                } else {
                    userStatusOrLastMessageView.setText(activity.getString(R.string.conferencing_happens_here));
                }
            }
        }
    }

    private void invalidateViewOnScroll() {
        UiUtils.showView(unreadMessagesCountView, AppConstants.unreadMessagesPositions.get(getMessageId()));
        UiUtils.showView(msgTimeStampView, AppConstants.lastMessageAvailablePositions.get(getMessageId()));
        UiUtils.showView(userOnlineStatusView, AppConstants.parseUserAvailableOnlineStatusPositions.get(getMessageId()));
        userOnlineStatusView.setImageResource(AppConstants.onlinePositions.get(getMessageId()) ? R.drawable.ic_online : R.drawable.ic_offline_grey);
        if (AppConstants.lastMessageAvailablePositions.get(getMessageId()) && lastMessage != null && lastMessage.direct() == EMMessage.Direct.SEND) {
            setupMessageReadStatus(lastMessage);
        } else {
            UiUtils.showView(deliveryStatusView, false);
        }
        UiUtils.showView(userStatusOrLastMessageView, !AppConstants.reactionsOpenPositions.get(getMessageId()));
        UiUtils.showView(reactionsIndicatorView, AppConstants.reactionsOpenPositions.get(getMessageId()));
    }

    private void subscribeToUserChanges() {
        if (parseObject != null) {
            objectStateQuery = ParseQuery.getQuery(AppConstants.PEOPLE_GROUPS_AND_ROOMS);
            objectStateQuery.whereEqualTo(AppConstants.REAL_OBJECT_ID, parseObject.getString(AppConstants.REAL_OBJECT_ID));
            try {
                SubscriptionHandling<ParseObject> subscriptionHandling = ApplicationLoader.getParseLiveQueryClient().subscribe(objectStateQuery);
                subscriptionHandling.handleEvent(SubscriptionHandling.Event.UPDATE, new SubscriptionHandling.HandleEventCallback<ParseObject>() {
                    @Override
                    public void onEvent(ParseQuery<ParseObject> query, final ParseObject object) {
                        post(new Runnable() {
                            @Override
                            public void run() {
                                String newObjectRealId = object.getString(AppConstants.REAL_OBJECT_ID);
                                String personId = parseObject.getString(AppConstants.REAL_OBJECT_ID);
                                HolloutLogger.d("ObjectUpdate", "A new object has changed. Object Id = " + newObjectRealId + " RefObjectId = " + personId);
                                if (newObjectRealId.equals(personId)) {
                                    setupConversation(object, searchString);
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
            if (objectStateQuery != null) {
                ApplicationLoader.getParseLiveQueryClient().unsubscribe(objectStateQuery);
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
        if (MainActivity.isActionModeActivated()) {
            if (!AppConstants.selectedPeople.contains(getConversationItem())) {
                AppConstants.selectedPeople.add(getConversationItem());
                AppConstants.selectedPeoplePositions.put(getCurrentConversationHashCode(), true);
            } else {
                AppConstants.selectedPeople.remove(getConversationItem());
                AppConstants.selectedPeoplePositions.put(getCurrentConversationHashCode(), false);
            }
            EventBus.getDefault().post(AppConstants.CHECK_SELECTED_CONVERSATIONS);
        } else {
            initChat();
        }
    }

    public ConversationItem getConversationItem() {
        return new ConversationItem(parseObject, HolloutPreferences.getLastConversationTime(parseObject.getString(AppConstants.REAL_OBJECT_ID)));
    }

    @Override
    public boolean onLongClick(View v) {
        if (!MainActivity.isActionModeActivated()) {
            AppConstants.selectedPeople.add(getConversationItem());
            AppConstants.selectedPeoplePositions.put(getCurrentConversationHashCode(), true);
            MainActivity.activateActionMode();
            MainActivity.vibrateVibrator();
        } else {
            if (AppConstants.selectedPeople.contains(getConversationItem())) {
                AppConstants.selectedPeople.remove(getConversationItem());
                AppConstants.selectedPeoplePositions.put(getCurrentConversationHashCode(), false);
            }
        }
        EventBus.getDefault().post(AppConstants.CHECK_SELECTED_CONVERSATIONS);
        return true;
    }

    private int getCurrentConversationHashCode() {
        return parseObject.getString(AppConstants.REAL_OBJECT_ID).hashCode();
    }

    private void initChat() {
        Intent viewProfileIntent = new Intent(activity, ChatActivity.class);
        parseObject.put(AppConstants.CHAT_TYPE, (getObjectType().equals(AppConstants.OBJECT_TYPE_INDIVIDUAL)
                ? AppConstants.CHAT_TYPE_SINGLE : getObjectType().equals(AppConstants.OBJECT_TYPE_GROUP)
                ? AppConstants.CHAT_TYPE_GROUP : AppConstants.CHAT_TYPE_ROOM));
        viewProfileIntent.putExtra(AppConstants.USER_PROPERTIES, parseObject);
        activity.startActivity(viewProfileIntent);
    }

    private String getObjectType() {
        return parseObject.getString(AppConstants.OBJECT_TYPE);
    }

    public void setupLastMessage(EMMessage message) {
        EMMessage.Type messageType = message.getType();
        if (message.direct() == EMMessage.Direct.SEND) {
            UiUtils.showView(deliveryStatusView, true);
            setMessageSendCallback();
            setupMessageReadStatus(message);
        } else {
            setMessageReceiveCallback();
            UiUtils.showView(deliveryStatusView, false);
        }
        if (messageType == EMMessage.Type.TXT) {
            try {
                String messageAttrType = message.getStringAttribute(AppConstants.MESSAGE_ATTR_TYPE);
                if (messageAttrType != null) {
                    switch (messageAttrType) {
                        case AppConstants.MESSAGE_ATTR_TYPE_REACTION:
                            String reaction = message.getStringAttribute(AppConstants.REACTION_VALUE);
                            if (reaction != null) {
                                UiUtils.showView(reactionsIndicatorView, true);
                                AppConstants.reactionsOpenPositions.put(getMessageId(), true);
                                UiUtils.showView(userStatusOrLastMessageView, false);
                                loadDrawables(activity, reactionsIndicatorView, reaction);
                            } else {
                                setupMessageBodyOnlyMessage(message);
                            }
                            break;
                        case AppConstants.MESSAGE_ATTR_TYPE_GIF:
                            UiUtils.showView(reactionsIndicatorView, true);
                            AppConstants.reactionsOpenPositions.put(getMessageId(), true);
                            UiUtils.showView(userStatusOrLastMessageView, true);
                            userStatusOrLastMessageView.setText(UiUtils.fromHtml("<b>" + activity.getString(R.string.gif) + "</b>"));
                            String gifUrl = message.getStringAttribute(AppConstants.GIF_URL);
                            loadLastMessageGif(gifUrl);
                            break;
                        default:
                            setupMessageBodyOnlyMessage(message);
                            break;
                    }
                } else {
                    setupMessageBodyOnlyMessage(message);
                }
            } catch (HyphenateException e) {
                e.printStackTrace();
                setupMessageBodyOnlyMessage(message);
            }
        }
        if (messageType == EMMessage.Type.IMAGE) {
            UiUtils.attachDrawableToTextView(activity, userStatusOrLastMessageView, R.drawable.msg_status_cam, UiUtils.DrawableDirection.LEFT);
            try {
                String messageBody = message.getStringAttribute(AppConstants.FILE_CAPTION);
                if (StringUtils.isNotEmpty(messageBody)) {
                    userStatusOrLastMessageView.setText(messageBody);
                } else {
                    userStatusOrLastMessageView.setText(activity.getString(R.string.photo));
                }
            } catch (HyphenateException e) {
                e.printStackTrace();
            }
        }
        if (messageType == EMMessage.Type.VIDEO) {
            UiUtils.attachDrawableToTextView(activity, userStatusOrLastMessageView, R.drawable.msg_status_video, UiUtils.DrawableDirection.LEFT);
            try {
                String messageBody = message.getStringAttribute(AppConstants.FILE_CAPTION);
                if (StringUtils.isNotEmpty(messageBody)) {
                    userStatusOrLastMessageView.setText(messageBody);
                } else {
                    userStatusOrLastMessageView.setText(activity.getString(R.string.video));
                }
            } catch (HyphenateException e) {
                e.printStackTrace();
            }
        }
        if (messageType == EMMessage.Type.LOCATION) {
            UiUtils.attachDrawableToTextView(activity, userStatusOrLastMessageView, R.drawable.msg_status_location, UiUtils.DrawableDirection.LEFT);
            EMLocationMessageBody emLocationMessageBody = (EMLocationMessageBody) message.getBody();
            String messageBody = emLocationMessageBody.getAddress();
            if (StringUtils.isNotEmpty(messageBody)) {
                userStatusOrLastMessageView.setText(messageBody);
            } else {
                userStatusOrLastMessageView.setText(activity.getString(R.string.location));
            }
        }
        if (messageType == EMMessage.Type.VOICE) {
            UiUtils.attachDrawableToTextView(activity, userStatusOrLastMessageView, R.drawable.msg_status_audio, UiUtils.DrawableDirection.LEFT);
            userStatusOrLastMessageView.setText(activity.getString(R.string.voice));
        }
        if (messageType == EMMessage.Type.FILE) {
            String messageBody;
            try {
                String fileType = message.getStringAttribute(AppConstants.FILE_TYPE);
                switch (fileType) {
                    case AppConstants.FILE_TYPE_CONTACT:
                        messageBody = "Contact";
                        UiUtils.attachDrawableToTextView(activity, userStatusOrLastMessageView, R.drawable.msg_contact, UiUtils.DrawableDirection.LEFT);
                        userStatusOrLastMessageView.setText(messageBody);
                        break;
                    case AppConstants.FILE_TYPE_AUDIO:
                        messageBody = "Music";
                        UiUtils.attachDrawableToTextView(activity, userStatusOrLastMessageView, R.drawable.msg_status_audio, UiUtils.DrawableDirection.LEFT);
                        userStatusOrLastMessageView.setText(messageBody);
                        break;
                    case AppConstants.FILE_TYPE_DOCUMENT:
                        messageBody = "Document";
                        UiUtils.attachDrawableToTextView(activity, userStatusOrLastMessageView, R.drawable.icon_file_doc_grey_mini, UiUtils.DrawableDirection.LEFT);
                        userStatusOrLastMessageView.setText(messageBody);
                        break;
                }
            } catch (HyphenateException e) {
                e.printStackTrace();
            }
        }
    }

    private void loadLastMessageGif(String gifUrl) {
        if (Build.VERSION.SDK_INT >= 17) {
            if (!activity.isDestroyed()) {
                if (StringUtils.isNotEmpty(gifUrl)) {
                    Glide.with(activity).load(gifUrl).asGif().listener(new RequestListener<String, GifDrawable>() {

                        @Override
                        public boolean onException(Exception e, String model, Target<GifDrawable> target, boolean isFirstResource) {
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(GifDrawable resource, String model, Target<GifDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                            return false;

                        }

                    }).into(reactionsIndicatorView);
                }
            }
        } else {
            if (StringUtils.isNotEmpty(gifUrl)) {
                Glide.with(activity).load(gifUrl).asGif().listener(new RequestListener<String, GifDrawable>() {

                    @Override
                    public boolean onException(Exception e, String model, Target<GifDrawable> target, boolean isFirstResource) {
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(GifDrawable resource, String model, Target<GifDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                        return false;
                    }
                }).into(reactionsIndicatorView);
            }
        }
    }

    private void setupMessageBodyOnlyMessage(EMMessage message) {
        EMTextMessageBody emTextMessageBody = (EMTextMessageBody) message.getBody();
        String messageBody = emTextMessageBody.getMessage();
        if (messageBody.equals("You have an incoming call")) {
            messageBody = "&#128222; You had a miss call";
        }
        userStatusOrLastMessageView.setText(UiUtils.fromHtml(messageBody));
        UiUtils.removeAllDrawablesFromTextView(userStatusOrLastMessageView);
        UiUtils.showView(reactionsIndicatorView, false);
        AppConstants.reactionsOpenPositions.put(getMessageId(), false);
    }

    private void loadDrawables(Context context, ImageView emojiView, String reactionTag) {
        imageDrawable = new KeyframesDrawableBuilder().withImage(getKFImage(context, reactionTag)).build();
        emojiView.setImageDrawable(imageDrawable);
        imageDrawable.startAnimation();
    }

    private KFImage getKFImage(Context context, String fileName) {
        AssetManager assetManager = context.getAssets();
        KFImage kfImage = null;
        try {
            stream = assetManager.open(fileName);
            kfImage = KFImageDeserializer.deserialize(stream);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return kfImage;
    }

    protected void setMessageSendCallback() {
        if (messageSendCallback == null) {
            messageSendCallback = new EMCallBack() {

                @Override
                public void onSuccess() {
                    setupConversation(parseObject, searchString);
                }

                @Override
                public void onProgress(final int progress, String status) {

                }

                @Override
                public void onError(int code, String error) {

                }

            };

        }

        lastMessage.setMessageStatusCallback(messageSendCallback);

    }

    /**
     * set callback for receiving message
     */
    protected void setMessageReceiveCallback() {
        if (messageReceiveCallback == null) {

            messageReceiveCallback = new EMCallBack() {

                @Override
                public void onSuccess() {
                    setupConversation(parseObject, searchString);
                }

                @Override
                public void onProgress(final int progress, String status) {
                }

                @Override
                public void onError(int code, String error) {

                }

            };

        }

        lastMessage.setMessageStatusCallback(messageReceiveCallback);
    }

    public EMMessage.Direct getMessageDirection() {
        return lastMessage != null ? lastMessage.direct() : null;
    }

    private void setupMessageReadStatus(EMMessage message) {
        if (getMessageDirection() != null && getMessageDirection() == EMMessage.Direct.SEND && deliveryStatusView != null) {
            UiUtils.showView(deliveryStatusView, true);
            if (message.isAcked()) {
                deliveryStatusView.setImageResource(R.drawable.msg_status_client_read);
            } else if (message.isListened()) {
                deliveryStatusView.setImageResource(R.drawable.msg_status_client_read);
            } else if (message.isDelivered()) {
                deliveryStatusView.setImageResource(R.drawable.msg_status_client_received);
            } else {
                deliveryStatusView.setImageResource(R.drawable.msg_status_server_receive);
            }
        } else {
            UiUtils.showView(deliveryStatusView, false);
        }
    }

}
