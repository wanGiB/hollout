package com.wan.hollout.ui.widgets;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
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
import com.bumptech.glide.load.resource.gif.GifDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;
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
import org.joda.time.DateTime;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * @author Wan Clem
 */
@SuppressWarnings({"FieldCanBeLocal", "SameParameterValue"})
@SuppressLint("SetTextI18n")
public class ConversationItemView extends RelativeLayout implements View.OnClickListener, View.OnLongClickListener {

    @BindView(R.id.from)
    HolloutTextView usernameEntryView;

    @BindView(R.id.txt_primary)
    HolloutTextView aboutPerson;

    @BindView(R.id.txt_secondary)
    ChatMessageTextView userStatusOrLastMessageView;

    @BindView(R.id.unread_message_indicator)
    TextView unreadMessagesCountView;

    @BindView(R.id.timestamp)
    TextView msgTimeStampView;

    @BindView(R.id.icon_profile)
    CircleImageView userPhotoView;

    @BindView(R.id.delivery_status_view)
    ImageView deliveryStatusView;

    @BindView(R.id.parent_layout)
    View parentView;

    @BindView(R.id.reactions_indicator)
    ImageView reactionsIndicatorView;

    @BindView(R.id.online_status)
    ImageView onlineStatusView;

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
        parentView.setOnLongClickListener(this);
    }

    public void bindData(Activity activity, String searchString, ParseObject parseObject) {
        this.searchString = searchString;
        this.activity = activity;
        this.signedInUserObject = AuthUtil.getCurrentUser();
        this.parseObject = parseObject;
        init();
        setupConversation(parseObject, searchString);
    }

    private void invalidateItemView(Activity activity, int objectHashCode) {
        parentView.setBackgroundColor(AppConstants.selectedPeoplePositions.get(objectHashCode)
                ? ContextCompat.getColor(activity, R.color.blue_100) : android.R.attr.selectableItemBackground);
    }

    private void applyProfilePicture(String profileUrl) {
        if (!TextUtils.isEmpty(profileUrl)) {
            UiUtils.loadImage(activity, profileUrl, userPhotoView);
        } else {
            userPhotoView.setImageResource(R.drawable.empty_profile);
        }
    }

    public int getMessageId() {
        return getCurrentConversationHashCode();
    }

    public void setupConversation(final ParseObject parseObject, String searchString) {
        if (parseObject != null) {
            this.signedInUserObject = AuthUtil.getCurrentUser();
            List<String> aboutUser = parseObject.getList(AppConstants.ABOUT_USER);
            List<String> aboutSignedInUser = signedInUserObject.getList(AppConstants.ABOUT_USER);
            if (aboutUser != null && aboutSignedInUser != null) {
                try {
                    String aboutUserString = TextUtils.join(",",aboutUser);
                    if (StringUtils.isNotEmpty(searchString)) {
                        aboutPerson.setText(UiUtils.highlightTextIfNecessary(searchString, WordUtils.capitalize(aboutUserString),
                                ContextCompat.getColor(activity, R.color.hollout_color_three)));
                    } else {
                        aboutPerson.setText(WordUtils.capitalize(aboutUserString));
                    }
                } catch (NullPointerException ignored) {

                }
            }
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
            }
            applyProfilePicture(userProfilePhoto);
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
                                && parseObject.getLong(AppConstants.USER_CURRENT_TIME_STAMP)
                                == signedInUserObject.getLong(AppConstants.USER_CURRENT_TIME_STAMP)) {
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
                    UiUtils.showView(onlineStatusView, false);
                    setupDefaults();
                }
            } else {
                setupDefaults();
            }
            listenToUserPresence(parseObject);
        }
        invalidateItemView(activity, getCurrentConversationHashCode());
        invalidateViewOnScroll();
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

    }

    private void listenToUserPresence(ParseObject parseObject) {
        String userOnlineStatus = parseObject.getString(AppConstants.APP_USER_ONLINE_STATUS);
        if (userOnlineStatus != null && UiUtils.canShowPresence(parseObject, AppConstants.ENTITY_TYPE_CHATS, new HashMap<String, Object>())) {
            if (parseObject.getLong(AppConstants.USER_CURRENT_TIME_STAMP)
                    == signedInUserObject.getLong(AppConstants.USER_CURRENT_TIME_STAMP)
                    && HolloutUtils.isNetWorkConnected(activity)) {
                UiUtils.showView(onlineStatusView, true);
                AppConstants.onlinePositions.put(getMessageId(), true);
            } else {
                UiUtils.showView(onlineStatusView, false);
                AppConstants.onlinePositions.put(getMessageId(), false);
            }
        } else {
            UiUtils.showView(onlineStatusView, false);
            AppConstants.onlinePositions.put(getMessageId(), false);
        }
    }

    public static boolean dayIsYesterday(DateTime day) {
        DateTime yesterday = new DateTime().withTimeAtStartOfDay().minusDays(1);
        DateTime inputDay = day.withTimeAtStartOfDay();
        return inputDay.isEqual(yesterday);
    }

    private void setupDefaults() {
        if (lastMessage != null) {
            HolloutLogger.d("LastMessageTracker", "Last Message in conversation is not null");
            UiUtils.showView(msgTimeStampView, true);
            long lastMessageTime = lastMessage.getTimeStamp();
            parseObject.put(AppConstants.LAST_CONVERSATION_TIME_WITH, lastMessageTime);
            Date msgDate = new Date(lastMessageTime);
            boolean isYesterday = dayIsYesterday(new DateTime(msgDate.getTime()));

            String todayString = AppConstants.DATE_FORMATTER_IN_BIRTHDAY_FORMAT.format(new Date());
            String messageDateString = AppConstants.DATE_FORMATTER_IN_BIRTHDAY_FORMAT.format(new Date(lastMessageTime));

            if (todayString.equals(messageDateString)) {
                //Msg received date = today
                String msgTime = AppConstants.DATE_FORMATTER_IN_12HRS.format(msgDate);
                msgTimeStampView.setText(msgTime);
            } else {
                if (isYesterday) {
                    msgTimeStampView.setText("YESTERDAY");
                } else {
                    String daysAgo = AppConstants.DATE_FORMATTER_IN_GEN_FORMAT.format(msgDate);
                    String yearsAgo = AppConstants.DATE_FORMATTER_IN_YEARS.format(msgDate);
                    String currentYear = AppConstants.DATE_FORMATTER_IN_YEARS.format(new Date());
                    if (yearsAgo.equals(currentYear)) {
                        daysAgo = daysAgo.replace("/" + yearsAgo, "");
                    }
                    msgTimeStampView.setText(daysAgo);
                }
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
                    userStatusOrLastMessageView.setText(UiUtils.fromHtml(userStatusString));
                } else {
                    userStatusOrLastMessageView.setText(activity.getString(R.string.hey_there_holla_me_on_hollout));
                }
            } else {
                String groupDescription = parseObject.getString(AppConstants.ROOM_DESCRIPTION);
                if (StringUtils.isNotEmpty(groupDescription)) {
                    userStatusOrLastMessageView.setText(UiUtils.fromHtml(groupDescription));
                } else {
                    userStatusOrLastMessageView.setText(activity.getString(R.string.conferencing_happens_here));
                }
            }
        }
    }

    private void invalidateViewOnScroll() {
        UiUtils.showView(unreadMessagesCountView, AppConstants.unreadMessagesPositions.get(getMessageId()));
        UiUtils.showView(msgTimeStampView, AppConstants.lastMessageAvailablePositions.get(getMessageId()));
        UiUtils.showView(onlineStatusView, AppConstants.onlinePositions.get(getMessageId()));
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
            objectStateQuery.cancel();
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
                userStatusOrLastMessageView.setText(UiUtils.fromHtml(messageBody));
            } else {
                userStatusOrLastMessageView.setText(activity.getString(R.string.photo));
            }
        }
        if (messageType == MessageType.VIDEO) {
            UiUtils.attachDrawableToTextView(activity, userStatusOrLastMessageView, R.drawable.msg_status_video, UiUtils.DrawableDirection.LEFT);
            String messageBody = message.getFileCaption();
            if (StringUtils.isNotEmpty(messageBody)) {
                userStatusOrLastMessageView.setText(UiUtils.fromHtml(messageBody));
            } else {
                userStatusOrLastMessageView.setText(activity.getString(R.string.video));
            }
        }
        if (messageType == MessageType.LOCATION) {
            UiUtils.attachDrawableToTextView(activity, userStatusOrLastMessageView, R.drawable.msg_status_location, UiUtils.DrawableDirection.LEFT);
            String messageBody = message.getLocationAddress();
            if (StringUtils.isNotEmpty(messageBody)) {
                userStatusOrLastMessageView.setText(UiUtils.fromHtml(messageBody));
            } else {
                userStatusOrLastMessageView.setText(activity.getString(R.string.location));
            }
        }
        if (messageType == MessageType.VOICE) {
            UiUtils.attachDrawableToTextView(activity, userStatusOrLastMessageView, R.drawable.msg_status_audio, UiUtils.DrawableDirection.LEFT);
            userStatusOrLastMessageView.setText(activity.getString(R.string.voice));
        }

        if (messageType == MessageType.CALL) {
            UiUtils.showView(reactionsIndicatorView, false);
            AppConstants.reactionsOpenPositions.put(getMessageId(), false);
            UiUtils.showView(userStatusOrLastMessageView, true);
            userStatusOrLastMessageView.setText(UiUtils.fromHtml(message.getMessageBody()));
        }

        String messageBody;
        if (messageType == MessageType.CONTACT) {
            messageBody = "Contact";
            UiUtils.attachDrawableToTextView(activity, userStatusOrLastMessageView, R.drawable.msg_contact, UiUtils.DrawableDirection.LEFT);
            userStatusOrLastMessageView.setText(UiUtils.fromHtml(messageBody));
        }
        if (messageType == MessageType.AUDIO) {
            messageBody = "Music";
            UiUtils.attachDrawableToTextView(activity, userStatusOrLastMessageView, R.drawable.msg_status_audio, UiUtils.DrawableDirection.LEFT);
            userStatusOrLastMessageView.setText(UiUtils.fromHtml(messageBody));
        }
        if (messageType == MessageType.DOCUMENT) {
            messageBody = "Document";
            UiUtils.attachDrawableToTextView(activity, userStatusOrLastMessageView, R.drawable.icon_file_doc_grey_mini, UiUtils.DrawableDirection.LEFT);
            userStatusOrLastMessageView.setText(UiUtils.fromHtml(messageBody));
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

    @Override
    protected void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
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
        if (getMessageDirection() == MessageDirection.OUTGOING || message.getMessageType() == MessageType.CALL && deliveryStatusView != null) {
            UiUtils.showView(deliveryStatusView, true);
            if (message.getMessageType() == MessageType.CALL) {
                IconicsDrawable iconicsDrawable = new IconicsDrawable(activity)
                        .icon(GoogleMaterial.Icon.gmd_call_missed)
                        .color(Color.LTGRAY);
                deliveryStatusView.setImageDrawable(iconicsDrawable);
            } else {
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
            }
        } else {
            UiUtils.showView(deliveryStatusView, false);
        }
    }

}
