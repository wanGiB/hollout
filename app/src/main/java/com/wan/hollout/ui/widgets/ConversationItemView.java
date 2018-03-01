package com.wan.hollout.ui.widgets;

import android.annotation.SuppressLint;
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
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.gif.GifDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.SubscriptionHandling;
import com.wan.hollout.R;
import com.wan.hollout.animations.KeyframesDrawable;
import com.wan.hollout.animations.KeyframesDrawableBuilder;
import com.wan.hollout.animations.deserializers.KFImageDeserializer;
import com.wan.hollout.animations.model.KFImage;
import com.wan.hollout.components.ApplicationLoader;
import com.wan.hollout.enums.MessageDirection;
import com.wan.hollout.enums.MessageStatus;
import com.wan.hollout.enums.MessageType;
import com.wan.hollout.models.ChatMessage;
import com.wan.hollout.models.ConversationItem;
import com.wan.hollout.ui.activities.ChatActivity;
import com.wan.hollout.ui.activities.MainActivity;
import com.wan.hollout.ui.helpers.CircleTransform;
import com.wan.hollout.utils.AppConstants;
import com.wan.hollout.utils.AuthUtil;
import com.wan.hollout.utils.DbUtils;
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
import java.util.HashMap;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * @author Wan Clem
 */
@SuppressWarnings({"FieldCanBeLocal", "SameParameterValue"})
@SuppressLint("SetTextI18n")
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

    @BindView(R.id.icon_container)
    RelativeLayout iconContainer;

    @BindView(R.id.delivery_status_view)
    ImageView deliveryStatusView;

    @BindView(R.id.parent_layout)
    View parentView;

    @BindView(R.id.reactions_indicator)
    ImageView reactionsIndicatorView;

    public ParseObject parseObject;
    public Activity activity;

    private ParseQuery<ParseObject> objectStateQuery;

    private String searchString;

    private ChatMessage lastMessage;

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
                iconText.setText(WordUtils.capitalize(userName.substring(0, 1)));
            }
            applyProfilePicture(userProfilePhoto);
            applyIconAnimation();
            attachEventHandlers(parseObject);
            if (HolloutUtils.isUserBlocked(parseObject.getString(AppConstants.REAL_OBJECT_ID))) {
                userStatusOrLastMessageView.setText(activity.getString(R.string.user_blocked));
                userPhotoView.setColorFilter(ContextCompat.getColor(activity, R.color.black_transparent_70percent), PorterDuff.Mode.SRC_ATOP);
                listenToUserPresence(parseObject);
                return;
            }
            lastMessage = DbUtils.getLastMessageInConversation(getConversationId());
            if (lastMessage != null) {
                int unreadMessagesCount = HolloutPreferences.getUnreadMessagesCountFrom(getConversationId());
                if (unreadMessagesCount > 0 && lastMessage != null && lastMessage.getMessageDirection() == MessageDirection.INCOMING) {
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
                    UiUtils.showView(userOnlineStatusView, false);
                    AppConstants.parseUserAvailableOnlineStatusPositions.put(getMessageId(), false);
                    setupDefaults();
                }
                listenToUserPresence(parseObject);
            } else {
                setupDefaults();
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
    }

    private void listenToUserPresence(ParseObject parseObject) {
        UiUtils.showView(userOnlineStatusView, true);
        String userOnlineStatus = parseObject.getString(AppConstants.APP_USER_ONLINE_STATUS);
        if (userOnlineStatus != null && UiUtils.canShowPresence(parseObject, AppConstants.ENTITY_TYPE_CHATS, new HashMap<String, Object>())) {
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
            long lastMessageTime = lastMessage.getTimeStamp();
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
                if (StringUtils.isNotEmpty(userStatusString) && UiUtils.canShowStatus(parseObject, AppConstants.ENTITY_TYPE_CHATS, new HashMap<String, Object>())) {
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
        if (AppConstants.lastMessageAvailablePositions.get(getMessageId()) && lastMessage != null && lastMessage.getMessageDirection() == MessageDirection.OUTGOING) {
            setupMessageReadStatus(lastMessage);
        } else {
            UiUtils.showView(deliveryStatusView, false);
        }
        UiUtils.showView(reactionsIndicatorView, AppConstants.reactionsOpenPositions.get(getMessageId()));
    }

    private void subscribeToUserChanges() {
        if (parseObject != null) {
            objectStateQuery = ParseQuery.getQuery(AppConstants.PEOPLE_GROUPS_AND_ROOMS);
            objectStateQuery.whereEqualTo(AppConstants.REAL_OBJECT_ID, getConversationId());
            try {
                SubscriptionHandling<ParseObject> subscriptionHandling = ApplicationLoader.getParseLiveQueryClient().subscribe(objectStateQuery);
                subscriptionHandling.handleEvent(SubscriptionHandling.Event.UPDATE, new SubscriptionHandling.HandleEventCallback<ParseObject>() {
                    @Override
                    public void onEvent(ParseQuery<ParseObject> query, final ParseObject object) {
                        post(new Runnable() {
                            @Override
                            public void run() {
                                String newObjectRealId = object.getString(AppConstants.REAL_OBJECT_ID);
                                String personId = getConversationId();
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
        return new ConversationItem(parseObject, HolloutPreferences.getLastConversationTime(getConversationId()));
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
        return getConversationId().hashCode();
    }

    private String getConversationId() {
        return parseObject.getString(AppConstants.REAL_OBJECT_ID);
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

    public void setupLastMessage(ChatMessage message) {
        MessageType messageType = message.getMessageType();
        if (message.getMessageDirection() == MessageDirection.OUTGOING) {
            UiUtils.showView(deliveryStatusView, true);
            setupMessageReadStatus(message);
        } else {
            UiUtils.showView(deliveryStatusView, false);
        }
        if (messageType == MessageType.TXT) {
            setupMessageBodyOnlyMessage(message);
        }
        if (messageType == MessageType.REACTION) {
            String reaction = message.getReactionValue();
            UiUtils.showView(reactionsIndicatorView, true);
            UiUtils.showView(userStatusOrLastMessageView, true);
            AppConstants.reactionsOpenPositions.put(getMessageId(), true);
            userStatusOrLastMessageView.setText(reaction.replace(".json", "").replace("reactions/", ""));
            loadDrawables(activity, reactionsIndicatorView, reaction);
        }
        if (messageType == MessageType.GIF) {
            UiUtils.showView(reactionsIndicatorView, true);
            AppConstants.reactionsOpenPositions.put(getMessageId(), true);
            UiUtils.showView(userStatusOrLastMessageView, true);
            userStatusOrLastMessageView.setText(UiUtils.fromHtml("<b>" + activity.getString(R.string.gif) + "</b>"));
            String gifUrl = message.getGifUrl();
            loadLastMessageGif(gifUrl);
        }
        if (messageType == MessageType.IMAGE) {
            UiUtils.attachDrawableToTextView(activity, userStatusOrLastMessageView, R.drawable.msg_status_cam, UiUtils.DrawableDirection.LEFT);
            String messageBody = message.getFileCaption();
            if (StringUtils.isNotEmpty(messageBody)) {
                userStatusOrLastMessageView.setText(messageBody);
            } else {
                userStatusOrLastMessageView.setText(activity.getString(R.string.photo));
            }
        }
        if (messageType == MessageType.VIDEO) {
            UiUtils.attachDrawableToTextView(activity, userStatusOrLastMessageView, R.drawable.msg_status_video, UiUtils.DrawableDirection.LEFT);
            String messageBody = message.getFileCaption();
            if (StringUtils.isNotEmpty(messageBody)) {
                userStatusOrLastMessageView.setText(messageBody);
            } else {
                userStatusOrLastMessageView.setText(activity.getString(R.string.video));
            }
        }
        if (messageType == MessageType.LOCATION) {
            UiUtils.attachDrawableToTextView(activity, userStatusOrLastMessageView, R.drawable.msg_status_location, UiUtils.DrawableDirection.LEFT);
            String messageBody = message.getLocationAddress();
            if (StringUtils.isNotEmpty(messageBody)) {
                userStatusOrLastMessageView.setText(messageBody);
            } else {
                userStatusOrLastMessageView.setText(activity.getString(R.string.location));
            }
        }
        if (messageType == MessageType.VOICE) {
            UiUtils.attachDrawableToTextView(activity, userStatusOrLastMessageView, R.drawable.msg_status_audio, UiUtils.DrawableDirection.LEFT);
            userStatusOrLastMessageView.setText(activity.getString(R.string.voice));
        }

        if (messageType == MessageType.CALL) {
            UiUtils.showView(reactionsIndicatorView, true);
            AppConstants.reactionsOpenPositions.put(getMessageId(), true);
            UiUtils.showView(userStatusOrLastMessageView, true);
            userStatusOrLastMessageView.setText(message.getMessageBody());
            reactionsIndicatorView.setImageResource(R.drawable.ic_call_missed_black_48dp);
        }

        String messageBody;
        if (messageType == MessageType.CONTACT) {
            messageBody = "Contact";
            UiUtils.attachDrawableToTextView(activity, userStatusOrLastMessageView, R.drawable.msg_contact, UiUtils.DrawableDirection.LEFT);
            userStatusOrLastMessageView.setText(messageBody);
        }
        if (messageType == MessageType.AUDIO) {
            messageBody = "Music";
            UiUtils.attachDrawableToTextView(activity, userStatusOrLastMessageView, R.drawable.msg_status_audio, UiUtils.DrawableDirection.LEFT);
            userStatusOrLastMessageView.setText(messageBody);
        }
        if (messageType == MessageType.DOCUMENT) {
            messageBody = "Document";
            UiUtils.attachDrawableToTextView(activity, userStatusOrLastMessageView, R.drawable.icon_file_doc_grey_mini, UiUtils.DrawableDirection.LEFT);
            userStatusOrLastMessageView.setText(messageBody);
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

    private void setupMessageBodyOnlyMessage(ChatMessage message) {
        String messageBody = message.getMessageBody();
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

    public MessageDirection getMessageDirection() {
        return lastMessage != null ? lastMessage.getMessageDirection() : null;
    }

    private void setupMessageReadStatus(ChatMessage message) {
        if (getMessageDirection() == MessageDirection.OUTGOING && message.getMessageType() != MessageType.CALL && deliveryStatusView != null) {
            UiUtils.showView(deliveryStatusView, true);
            if (message.isAcknowledged()) {
                deliveryStatusView.setImageResource(R.drawable.msg_status_client_read);
            } else if (message.isListened()) {
                deliveryStatusView.setImageResource(R.drawable.msg_status_client_read);
            } else if (message.getMessageStatus() == MessageStatus.READ) {
                deliveryStatusView.setImageResource(R.drawable.msg_status_client_read);
            } else if (message.getMessageStatus() == MessageStatus.DELIVERED) {
                deliveryStatusView.setImageResource(R.drawable.msg_status_client_received);
            } else {
                deliveryStatusView.setImageResource(R.drawable.msg_status_server_receive);
            }
        } else {
            UiUtils.showView(deliveryStatusView, false);
        }
    }

}
