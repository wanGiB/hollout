package com.wan.hollout.ui.activities;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.media.MediaRecorder;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Vibrator;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SimpleItemAnimator;
import android.text.Editable;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.style.ForegroundColorSpan;
import android.text.style.StrikethroughSpan;
import android.text.style.StyleSpan;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.gif.GifDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.parse.DeleteCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.SaveCallback;
import com.raizlabs.android.dbflow.runtime.DirectModelNotifier;
import com.raizlabs.android.dbflow.structure.BaseModel;
import com.vanniktech.emoji.EmojiPopup;
import com.vanniktech.emoji.listeners.OnEmojiPopupDismissListener;
import com.vanniktech.emoji.listeners.OnEmojiPopupShownListener;
import com.vanniktech.emoji.listeners.OnSoftKeyboardOpenListener;
import com.wan.hollout.R;
import com.wan.hollout.animations.KeyframesDrawable;
import com.wan.hollout.animations.KeyframesDrawableBuilder;
import com.wan.hollout.animations.deserializers.KFImageDeserializer;
import com.wan.hollout.animations.model.KFImage;
import com.wan.hollout.bean.HolloutFile;
import com.wan.hollout.clients.ChatClient;
import com.wan.hollout.enums.MessageDirection;
import com.wan.hollout.enums.MessageStatus;
import com.wan.hollout.enums.MessageType;
import com.wan.hollout.eventbuses.GifMessageEvent;
import com.wan.hollout.eventbuses.PlaceLocalCallEvent;
import com.wan.hollout.eventbuses.ReactionMessageEvent;
import com.wan.hollout.eventbuses.ScrollToMessageEvent;
import com.wan.hollout.eventbuses.SearchMessages;
import com.wan.hollout.interfaces.DoneCallback;
import com.wan.hollout.interfaces.EndlessRecyclerViewScrollListener;
import com.wan.hollout.language.DynamicLanguage;
import com.wan.hollout.listeners.OnVerticalScrollListener;
import com.wan.hollout.models.ChatMessage;
import com.wan.hollout.rendering.StickyRecyclerHeadersDecoration;
import com.wan.hollout.ui.adapters.MessagesAdapter;
import com.wan.hollout.ui.adapters.PickedMediaFilesAdapter;
import com.wan.hollout.ui.services.ContactService;
import com.wan.hollout.ui.utils.FileUploader;
import com.wan.hollout.ui.widgets.AnimatingToggle;
import com.wan.hollout.ui.widgets.AttachmentTypeSelector;
import com.wan.hollout.ui.widgets.ChatToolbar;
import com.wan.hollout.ui.widgets.CircleImageView;
import com.wan.hollout.ui.widgets.ComposeText;
import com.wan.hollout.ui.widgets.HolloutTextView;
import com.wan.hollout.ui.widgets.InputAwareLayout;
import com.wan.hollout.ui.widgets.InputPanel;
import com.wan.hollout.ui.widgets.KeyboardAwareLinearLayout;
import com.wan.hollout.ui.widgets.LinkPreview;
import com.wan.hollout.ui.widgets.RoundedImageView;
import com.wan.hollout.utils.AppConstants;
import com.wan.hollout.utils.AuthUtil;
import com.wan.hollout.utils.DateFormatter;
import com.wan.hollout.utils.DbUtils;
import com.wan.hollout.utils.FilePathFinder;
import com.wan.hollout.utils.FileUtils;
import com.wan.hollout.utils.GeneralNotifier;
import com.wan.hollout.utils.HolloutLogger;
import com.wan.hollout.utils.HolloutPermissions;
import com.wan.hollout.utils.HolloutPreferences;
import com.wan.hollout.utils.HolloutUtils;
import com.wan.hollout.utils.HolloutVCFParser;
import com.wan.hollout.utils.LocationUtils;
import com.wan.hollout.utils.PermissionsUtils;
import com.wan.hollout.utils.RequestCodes;
import com.wan.hollout.utils.SafeLayoutManager;
import com.wan.hollout.utils.UiUtils;
import com.wan.hollout.utils.VCFContactData;

import net.alhazmy13.mediapicker.Image.ImagePicker;
import net.alhazmy13.mediapicker.Video.VideoPicker;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Set;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.wan.hollout.ui.widgets.AttachmentTypeSelector.ADD_CONTACT;
import static com.wan.hollout.ui.widgets.AttachmentTypeSelector.ADD_DOCUMENT;
import static com.wan.hollout.ui.widgets.AttachmentTypeSelector.ADD_GIF;
import static com.wan.hollout.ui.widgets.AttachmentTypeSelector.ADD_IMAGE;
import static com.wan.hollout.ui.widgets.AttachmentTypeSelector.ADD_LOCATION;
import static com.wan.hollout.ui.widgets.AttachmentTypeSelector.ADD_REACTION;
import static com.wan.hollout.ui.widgets.AttachmentTypeSelector.ADD_VIDEO;
import static com.wan.hollout.ui.widgets.AttachmentTypeSelector.OPEN_GALLERY;

/***
 * @author Wan Clem
 * ***/
@SuppressWarnings({"StatementWithEmptyBody", "FieldCanBeLocal", "unused", "ConstantConditions"})
public class ChatActivity extends BaseActivity implements
        KeyboardAwareLinearLayout.OnKeyboardShownListener,
        ActivityCompat.OnRequestPermissionsResultCallback, InputPanel.Listener, View.OnClickListener {

    private static final int REQUEST_CODE_PICK_DOCUMENT = 9;
    private static final int REQUEST_CODE_CONTACT_SHARE = 15;
    private static final int PLACE_LOCATION_PICKER_REQUEST_CODE = 20;
    protected static final int MESSAGE_TYPE_RECEIVED_CALL = 1;
    protected static final int MESSAGE_TYPE_SENT_CALL = 2;

    private AttachmentTypeSelector attachmentTypeSelector;

    @BindView(R.id.bottom_panel)
    InputPanel inputPanel;

    @BindView(R.id.layout_container)
    InputAwareLayout container;

    @BindView(R.id.embedded_text_editor)
    ComposeText composeText;

    @BindView(R.id.record_message_button)
    FloatingActionButton recordAudioButton;

    @BindView(R.id.control_toggle)
    AnimatingToggle controlToggle;

    @BindView(R.id.send_button_container)
    View sendMessageContainer;

    @BindView(R.id.attach_button)
    ImageButton attachButton;

    @BindView(R.id.chat_tool_bar)
    ChatToolbar chatToolbar;

    @BindView(R.id.message_reply_view)
    LinearLayout messageReplyView;

    @BindView(R.id.reply_icon)
    ImageView replyIconView;

    @BindView(R.id.play_reply_msg_if_video)
    ImageView playReplyMessageIfVideo;

    @BindView(R.id.reply_subtitle)
    HolloutTextView replyMessageSubTitleView;

    @BindView(R.id.reply_title)
    HolloutTextView replyMessageTitleView;

    @BindView(R.id.close_reply_message_view)
    ImageView closeReplyMessageView;

    @BindView(R.id.compose_bubble)
    View composeBubble;

    @BindView(R.id.footerAd)
    LinearLayout footerAd;

    @BindView(R.id.link_preview_layout)
    RelativeLayout linkPreviewLayout;

    @BindView(R.id.link_preview)
    LinkPreview linkPreview;

    @BindView(R.id.single_media_frame)
    FrameLayout singleMediaFrame;

    @BindView(R.id.single_media_viewer)
    RoundedImageView singleMediaViewer;

    @BindView(R.id.play_single_media_if_video)
    ImageView playSingleMediaIfVideo;

    @BindView(R.id.cancel_picked_single_media)
    CircleImageView cancelPickedSingleMedia;

    @BindView(R.id.media_length_view)
    HolloutTextView mediaLengthView;

    @BindView(R.id.multiple_media_files_recycler_view)
    RecyclerView multiplePickedMediaFilesRecyclerView;

    @BindView(R.id.scroll_to_bottom_button)
    View scrollToBottomButton;

    @BindView(R.id.scroll_to_bottom_frame)
    View scrollToBottomFrame;

    @BindView(R.id.messages_empty_view)
    HolloutTextView messagesEmptyView;

    @BindView(R.id.unread_message_indicator)
    TextView unreadMessagesIndicator;

    @BindView(R.id.conversations_recycler_view)
    RecyclerView messagesRecyclerView;

    private ArrayList<String> unreadMessagesCount = new ArrayList<>();

    public static String recipientId;
    private String lastExecutablePermissionAction;

    private ArrayList<HolloutFile> pickedMediaFiles = new ArrayList<>();
    private PickedMediaFilesAdapter pickedMediaFilesAdapter;

    private ParseObject signedInUser;

    private String recipientName;
    private ParseObject recipientProperties;

    private DynamicLanguage dynamicLanguage = new DynamicLanguage();
    private HolloutPermissions holloutPermissions;

    private Vibrator vibrator;
    private MediaRecorder mediaRecorder;
    private boolean recordingInProgress = false;

    private File recorderAudioCaptureFilePath;

    private ArrayList<ChatMessage> messages = new ArrayList<>();
    private List<ChatMessage> tempReceivedMessages = new ArrayList<>();

    private MessagesAdapter messagesAdapter;
    private LinearLayoutManager messagesLayoutManager;

    private int chatType;
    private boolean onScrolledUp = true;
    /**
     * load 20 messages at one time
     */
    protected int pageSize = 20;
    protected boolean isLoading;
    protected boolean isFirstLoad = true;
    protected boolean haveMoreData = true;

    private Comparator<ChatMessage> messageComparator = new Comparator<ChatMessage>() {
        @Override
        public int compare(ChatMessage o1, ChatMessage o2) {
            if (o1 != null && o2 != null) {
                return Long.valueOf(o2.getTimeStamp()).compareTo(o1.getTimeStamp());
            }
            return 0;
        }
    };

    private static KeyframesDrawable imageDrawable;
    private static InputStream stream;
    private String phoneNumberToCall;
    private ProgressDialog deleteConversationProgressDialog;

    private MessageUpdateTask messageUpdateTask;
    private BangMessageSoundTask bangMessageSoundTask;

    private int callType = -1;
    private Intent callIntent;

    private int VOICE_CALL = 0;
    private int VIDEO_CALL = 1;

    private DirectModelNotifier.ModelChangedListener<ChatMessage> onModelStateChangedListener = new DirectModelNotifier.ModelChangedListener<ChatMessage>() {

        @Override
        public void onTableChanged(@Nullable Class<?> tableChanged, @NonNull BaseModel.Action action) {

        }

        @Override
        public void onModelChanged(@NonNull final ChatMessage model, @NonNull final BaseModel.Action action) {
            messagesRecyclerView.post(new Runnable() {
                @Override
                public void run() {
                    if (action == BaseModel.Action.INSERT || action == BaseModel.Action.SAVE) {
                        if (model.getConversationId().toLowerCase().equals(getRecipient())) {
                            checkAndAddNewMessage(model);
                            if (onScrolledUp) {
                                tempReceivedMessages.add(model);
                            } else {
                                messagesRecyclerView.smoothScrollToPosition(0);
                            }
                            clearAllUnreadMessagesFromRecipient();
                            prioritizeConversation();
                            refreshUnseenMessages();
                            if (model.getMessageDirection() == MessageDirection.INCOMING && model.getMessageStatus() != MessageStatus.READ) {
                                model.setMessageStatus(MessageStatus.READ);
                                DbUtils.updateMessage(model);
                                ChatClient.getInstance().markMessageAsRead(model);
                            }
                        }
                    } else if (action == BaseModel.Action.UPDATE) {
                        if (model.getConversationId().toLowerCase().equals(getRecipient())) {
                            int indexOfMessage = messages.indexOf(model);
                            if (indexOfMessage != -1) {
                                replaceMessageAtIndexAsync(indexOfMessage, model);
                                checkBangMessageReadSound(model);
                            }
                            clearAllUnreadMessagesFromRecipient();
                        }
                    } else if (action == BaseModel.Action.DELETE) {
                        if (model.getConversationId().toLowerCase().equals(getRecipient())) {
                            int indexOfMessage = messages.indexOf(model);
                            if (indexOfMessage != -1) {
                                messages.remove(indexOfMessage);
                                notifyDataSetChanged();
                            }
                            invalidateEmptyView();
                        }
                    }
                }
            });

        }

    };

    private void checkBangMessageReadSound(@NonNull ChatMessage model) {
        if (model.getMessageDirection() == MessageDirection.OUTGOING && model.getMessageStatus() == MessageStatus.READ) {
            if (!model.isReadSoundBanged()) {
                bangSoundAtIndexAsync(model);
            }
        }
    }

    public ArrayList<ChatMessage> getMessages() {
        return messages;
    }

    private boolean canGoBackToMain = false;
    private EmojiPopup emojiPopup;

    private List<String> boldTags;
    private List<String> italicTags;
    private List<String> strikeThroughTags;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
        setContentView(R.layout.activity_chat);
        dynamicLanguage.onCreate(this);
        ButterKnife.bind(this);
        setupEmojiPopup();
        setSupportActionBar(chatToolbar.getToolbar());
        Bundle intentExtras = getIntent().getExtras();
        initBasicComponents();
        signedInUser = AuthUtil.getCurrentUser();
        if (signedInUser == null) {
            Intent splashIntent = new Intent(ChatActivity.this, SplashActivity.class);
            startActivity(splashIntent);
            finish();
            return;
        }
        setupUIFromIntent(intentExtras);
    }

    private void setupUIFromIntent(Bundle intentExtras) {
        canGoBackToMain = intentExtras.getBoolean(AppConstants.CAN_LAUNCH_MAIN, false);
        recipientProperties = intentExtras.getParcelable(AppConstants.USER_PROPERTIES);
        if (recipientProperties != null) {
            chatType = recipientProperties.getInt(AppConstants.CHAT_TYPE);
            chatToolbar.initView(recipientId, chatType);
            recipientId = recipientProperties.getString(AppConstants.REAL_OBJECT_ID);
            setupChatRecipient(recipientProperties);
            if (chatType == AppConstants.CHAT_TYPE_ROOM) {
                messagesEmptyView.setText(getString(R.string.cleaning_up_room));
            }
        }
        initializeViews();
        setupAttachmentManager();
        setupMessagesAdapter();
        initConversation();
        clearAllUnreadMessagesFromRecipient();
        checkBlackListStatus();
        createDeleteConversationProgressDialog();
        setActiveChat();
    }

    private void setupEmojiPopup() {
        emojiPopup = EmojiPopup.Builder.fromRootView(container)
                .setOnEmojiPopupShownListener(new OnEmojiPopupShownListener() {
                    @Override
                    public void onEmojiPopupShown() {
                        inputPanel.setEmojiToggleToKeyboard();
                    }
                }).setOnSoftKeyboardOpenListener(new OnSoftKeyboardOpenListener() {
                    @Override
                    public void onKeyboardOpen(int keyBoardHeight) {
                        inputPanel.setEmojiToggleToEmoji();
                    }
                }).setOnEmojiPopupDismissListener(new OnEmojiPopupDismissListener() {
                    @Override
                    public void onEmojiPopupDismiss() {
                        inputPanel.setEmojiToggleToEmoji();
                    }
                })
                .build(composeText);
    }

    private void replaceMessageAtIndexAsync(final int indexOfMessage, final ChatMessage newMessage) {
        messageUpdateTask = new MessageUpdateTask(getCurrentActivityInstance(), messagesAdapter, messages, newMessage, indexOfMessage);
        messageUpdateTask.executeOnExecutor(ChatClient.getInstance().getExecutor());
    }

    private void bangSoundAtIndexAsync(ChatMessage message) {
        bangMessageSoundTask = new BangMessageSoundTask(getCurrentActivityInstance(), message);
        bangMessageSoundTask.executeOnExecutor(ChatClient.getInstance().getExecutor());
    }

    private void checkBlackListStatus() {
        checkDidIBlackListUser();
        checkDidUserBlackListMe();
    }

    private void setActiveChat() {
        AppConstants.activeChatId = getRecipient();
    }

    private void createDeleteConversationProgressDialog() {
        deleteConversationProgressDialog = new ProgressDialog(this);
        deleteConversationProgressDialog.setIndeterminate(false);
        deleteConversationProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        deleteConversationProgressDialog.setMax(100);
    }

    private void checkDismissConversationProgressDialog() {
        if (deleteConversationProgressDialog != null && deleteConversationProgressDialog.isShowing()) {
            deleteConversationProgressDialog.dismiss();
        }
    }

    public void setPhoneNumberToCall(String phoneNumberToCall) {
        this.phoneNumberToCall = phoneNumberToCall;
    }

    public String getPhoneNumberToCall() {
        return phoneNumberToCall;
    }

    private void clearAllUnreadMessagesFromRecipient() {
        HolloutPreferences.clearUnreadMessagesCountFrom(getRecipient());
        Set<String> totalUnreadMessages = HolloutPreferences.getTotalUnreadChats();
        if (totalUnreadMessages != null) {
            if (totalUnreadMessages.contains(getRecipient())) {
                totalUnreadMessages.remove(getRecipient());
                HolloutPreferences.saveTotalUnreadChats(totalUnreadMessages);
                GeneralNotifier.getNotificationManager().cancel(AppConstants.NEW_MESSAGE_NOTIFICATION_ID);
            }
        }
    }

    public ChatToolbar getChatToolbar() {
        return chatToolbar;
    }

    private void tryOffloadLastMessage() {
        String lastAttemptedMessageForRecipient = HolloutPreferences.getLastAttemptedMessage(recipientId);
        if (StringUtils.isNotEmpty(lastAttemptedMessageForRecipient)) {
            composeText.setText(lastAttemptedMessageForRecipient);
            HolloutPreferences.clearPreviousAttemptedMessage(recipientId);
        } else {
            if (!isAContact() && messages.isEmpty()) {
                composeText.setText(getString(R.string.nice_to_meet_you));
            }
        }
    }

    private void initConversation() {
        fetchMessages();
    }

    private void fetchMessages() {
        DbUtils.fetchMessagesInConversation(getRecipient(), new DoneCallback<List<ChatMessage>>() {
            @Override
            public void done(final List<ChatMessage> result, final Exception e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (e == null && result != null && !result.isEmpty()) {
                            if (!result.isEmpty()) {
                                for (ChatMessage emMessage : result) {
                                    if (!messages.contains(emMessage)) {
                                        messages.add(0, emMessage);
                                    }
                                }
                                sortMessages();
                                notifyDataSetChanged();
                                GeneralNotifier.getNotificationManager().cancel(AppConstants.NEW_MESSAGE_NOTIFICATION_ID);
                            }
                        }
                        tryOffloadLastMessage();
                        invalidateEmptyView();
                    }
                });
            }
        });
        GeneralNotifier.getNotificationManager().cancel(AppConstants.NEW_MESSAGE_NOTIFICATION_ID);
    }

    private void sortMessages() {
        Collections.sort(messages, messageComparator);
    }

    private void setupMessagesAdapter() {
        messagesAdapter = new MessagesAdapter(this, messages);
        messagesLayoutManager = new LinearLayoutManager(ChatActivity.this, LinearLayoutManager.VERTICAL, true);
        messagesRecyclerView.setLayoutManager(messagesLayoutManager);
        StickyRecyclerHeadersDecoration stickyRecyclerHeadersDecoration = new StickyRecyclerHeadersDecoration(messagesAdapter);
        messagesRecyclerView.addItemDecoration(stickyRecyclerHeadersDecoration);
        SimpleItemAnimator itemAnimator = new DefaultItemAnimator();
        itemAnimator.setSupportsChangeAnimations(false);
        messagesRecyclerView.setItemAnimator(itemAnimator);
        messagesRecyclerView.setAdapter(messagesAdapter);
        messagesRecyclerView.addOnScrollListener(new EndlessRecyclerViewScrollListener(messagesLayoutManager) {

            @Override
            public void onLoadMore(int page, int totalItemsCount) {
                UiUtils.showView(scrollToBottomFrame, !onScrolledUp && messages.size() >= 20);
                loadMoreMessages(null);
            }

        });

        messagesRecyclerView.addOnScrollListener(new OnVerticalScrollListener() {

            @Override
            public void onScrolledUp() {
                super.onScrolledUp();
                if (!messages.isEmpty()) {
                    onScrolledUp = true;
                }
            }

            @Override
            public void onScrolledToTop() {
                super.onScrolledToTop();
                if (!messages.isEmpty() && messages.size() >= 20) {
                    UiUtils.showView(scrollToBottomFrame, true);
                    onScrolledUp = true;
                }
                loadMoreMessages(null);
            }

            @Override
            public void onScrolledToBottom() {
                super.onScrolledToBottom();
                if (tempReceivedMessages.isEmpty()) {
                    UiUtils.showView(scrollToBottomFrame, false);
                    onScrolledUp = false;
                }
            }

        });

    }

    private void loadMoreMessages(final DoneCallback<Boolean> messageLoadDoneCallback) {
        int messageSize = messages.size();
        if (messageSize > 1) {
            ChatMessage lastMessage = messages.get(messages.size() - 1);
            if (lastMessage != null) {
                DbUtils.fetchMoreMessagesInConversation(getRecipient(), messages.size(), new DoneCallback<List<ChatMessage>>() {
                    @Override
                    public void done(final List<ChatMessage> moreMessages, final Exception e) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                if (e == null && moreMessages != null && !moreMessages.isEmpty()) {
                                    if (moreMessages != null && !moreMessages.isEmpty()) {
                                        for (ChatMessage emMessage : moreMessages) {
                                            if (!messages.contains(emMessage)) {
                                                messages.add(emMessage);
                                                messagesAdapter.notifyItemInserted(messages.size() - 1);
                                            }
                                        }
                                        if (messageLoadDoneCallback != null) {
                                            messageLoadDoneCallback.done(true, null);
                                        }
                                    }
                                }
                            }
                        });
                    }
                });
            } else {
                UiUtils.showSafeToast("Last Message is null");
            }
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        if (getRecipient() != null) {
            MenuItem placeCallMenuItem = menu.findItem(R.id.place_call);
            MenuItem viewProfileMenuItem = menu.findItem(R.id.view_profile_info);
            MenuItem blockUserMenuItem = menu.findItem(R.id.block_user);
            if (chatType == AppConstants.CHAT_TYPE_GROUP || chatType == AppConstants.CHAT_TYPE_ROOM) {
                placeCallMenuItem.setVisible(AppConstants.selectedMessages.isEmpty());
                viewProfileMenuItem.setVisible(false);
                blockUserMenuItem.setVisible(false);
            }
            if (chatType == AppConstants.CHAT_TYPE_SINGLE) {
                List<String> signedInUserChats = signedInUser.getList(AppConstants.APP_USER_CHATS);
                if (signedInUserChats != null && signedInUserChats.contains(recipientId)) {
                    placeCallMenuItem.setVisible(true);
                } else {
                    placeCallMenuItem.setVisible(false);
                }
                if (HolloutUtils.isUserBlocked(getRecipient())) {
                    blockUserMenuItem.setTitle(getString(R.string.unblock_user));
                }
            }
            supportInvalidateOptionsMenu();
        }
        return super.onPrepareOptionsMenu(menu);
    }

    private void initBasicComponents() {
        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        holloutPermissions = new HolloutPermissions(this, footerAd);
    }

    private void setupAttachmentManager() {
        pickedMediaFilesAdapter = new PickedMediaFilesAdapter(this, pickedMediaFiles);
        SafeLayoutManager safeLayoutManager = new SafeLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        multiplePickedMediaFilesRecyclerView.setLayoutManager(safeLayoutManager);
        multiplePickedMediaFilesRecyclerView.setAdapter(pickedMediaFilesAdapter);
    }

    private void releaseMediaRecorder() {
        if (mediaRecorder != null) {
            mediaRecorder.stop();
            mediaRecorder.reset();
            mediaRecorder.release();
            mediaRecorder = null;
            recordingInProgress = false;
        }
    }

    public void startRecorder() {
        resetRecorderAndStartRecording();
    }

    private void invalidateEmptyView() {
        UiUtils.showView(messagesEmptyView, messages.isEmpty());
    }

    private void resetRecorderAndStartRecording() {
        mediaRecorder = new MediaRecorder();
        recorderAudioCaptureFilePath = HolloutUtils.getOutputMediaFile(AppConstants.CAPTURE_MEDIA_TYPE_AUDIO);
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(
                MediaRecorder.OutputFormat.THREE_GPP);
        mediaRecorder.setAudioEncoder(
                MediaRecorder.AudioEncoder.DEFAULT);
        if (recorderAudioCaptureFilePath != null) {
            mediaRecorder.setOutputFile(recorderAudioCaptureFilePath.getAbsolutePath());
        }
        try {
            mediaRecorder.prepare();
            mediaRecorder.start();
            mediaRecorder.setOnErrorListener(new MediaRecorder.OnErrorListener() {
                @Override
                public void onError(MediaRecorder mr, int what, int extra) {
                    inputPanel.stopRecording();
                }
            });
            mediaRecorder.setOnInfoListener(new MediaRecorder.OnInfoListener() {
                @Override
                public void onInfo(MediaRecorder mr, int what, int extra) {
                    HolloutLogger.d("MediaRecordInfo", " What:" + what + ",Extra:" + extra);
                }
            });
            recordingInProgress = true;
        } catch (Exception e) {
            HolloutLogger.d("MediaRecordInfo", e.getMessage());
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        if (isFinishing()) {
            return;
        }
        if (!StringUtils.isNotEmpty(composeText.getText().toString().trim()) || !pickedMediaFiles.isEmpty()) {
            composeText.setText("");
        }
        messages.clear();
        messagesAdapter.notifyDataSetChanged();
        setupUIFromIntent(intent.getExtras());
        chatToolbar.unSubscribeFromUserChanges();
        chatToolbar.subscribeToObjectChanges();
        if (onModelStateChangedListener != null) {
            DirectModelNotifier.get().unregisterForModelChanges(ChatMessage.class, onModelStateChangedListener);
            DirectModelNotifier.get().registerForModelChanges(ChatMessage.class, onModelStateChangedListener);
        }
        canGoBackToMain = false;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.cancel_picked_single_media:
                if (!pickedMediaFiles.isEmpty()) {
                    hidePickedMedia();
                }
                break;
            case R.id.scroll_to_bottom_frame:
                scrollToBottom();
                break;
        }
    }

    private void hidePickedMedia() {
        pickedMediaFiles.clear();
        UiUtils.showView(singleMediaFrame, false);
        UiUtils.showView(singleMediaViewer, false);
        UiUtils.showView(playSingleMediaIfVideo, false);
        UiUtils.showView(mediaLengthView, false);
        UiUtils.showView(multiplePickedMediaFilesRecyclerView, false);
        if (StringUtils.isEmpty(composeText.getText().toString().trim())) {
            displayInactiveSendButton();
        }
        composeText.setHint("Compose message");
    }

    private class AttachmentTypeListener implements AttachmentTypeSelector.AttachmentClickedListener {

        @Override
        public void onClick(int type) {
            handleClickedAttachmentType(type);
        }

        @Override
        public void onQuickAttachment(Uri uri) {
            //Do nothing
            File file = FileUtils.getFile(ChatActivity.this, uri);
            if (file.exists()) {
                previewSinglePickedFile(AppConstants.FILE_TYPE_PHOTO, file.getPath());
            }
        }

    }

    private void handleClickedAttachmentType(int type) {
        HolloutLogger.d("ChatActivity", "Selected: " + type);
        switch (type) {
            case ADD_IMAGE:
                openCameraToTakePhoto();
                break;
            case ADD_VIDEO:
                openCameraToShootVideo();
                break;
            case OPEN_GALLERY:
                checkAccessToGalleryAndOpen();
                break;
            case ADD_CONTACT:
                checkContactAccessAndOpenContact();
                break;
            case ADD_DOCUMENT:
                checkAccessToDocumentAndOpenDocuments();
                break;
            case ADD_LOCATION:
                checkLocationAccessPermissionsAndOpenLocation();
                break;
            case ADD_GIF:
                attachmentTypeSelector.loadGifs(this, null, 0);
                break;
            case ADD_REACTION:
                attachmentTypeSelector.loadReactions();
                break;
        }
    }

    private void handleAddAttachment() {
        if (attachmentTypeSelector == null) {
            attachmentTypeSelector = new AttachmentTypeSelector(this, getSupportLoaderManager(), new AttachmentTypeListener());
        }
        attachmentTypeSelector.show(attachButton);
        attachmentTypeSelector.setOnDismissListener(new PopupWindow.OnDismissListener() {
            @Override
            public void onDismiss() {
                UiUtils.showView(inputPanel, true);
            }
        });
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initializeViews() {
        container.addOnKeyboardShownListener(this);
        inputPanel.setListener(this);

        attachmentTypeSelector = null;

        SendButtonListener sendButtonOnClickListener = new SendButtonListener();
        ComposeKeyPressedListener composeKeyPressedListener = new ComposeKeyPressedListener();

        composeText.setOnEditorActionListener(sendButtonOnClickListener);
        initTouchListener();
        attachButton.setOnClickListener(new AttachButtonListener());

        recordAudioButton.setOnClickListener(sendButtonOnClickListener);
        recordAudioButton.setEnabled(true);

        sendMessageContainer.setOnClickListener(sendButtonOnClickListener);

        composeText.setOnKeyListener(composeKeyPressedListener);
        composeText.addTextChangedListener(composeKeyPressedListener);
        composeText.setOnEditorActionListener(sendButtonOnClickListener);
        composeText.setOnClickListener(composeKeyPressedListener);
        composeText.setOnFocusChangeListener(composeKeyPressedListener);
        cancelPickedSingleMedia.setOnClickListener(this);
        scrollToBottomFrame.setOnClickListener(this);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void initTouchListener() {
        composeText.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (emojiPopup.isShowing()) {
                    emojiPopup.dismiss();
                }
                return false;
            }
        });
    }

    @Override
    public void onBackPressed() {
        if (chatToolbar.isSearchViewOpen()) {
            chatToolbar.closeSearch();
            return;
        }
        if (container.isInputOpen()) {
            container.hideCurrentInput(composeText);
            return;
        }
        if (chatToolbar.isActionModeActivated()) {
            chatToolbar.updateActionMode(0);
            notifyDataSetChanged();
            return;
        }
        if (messageReplyView.getVisibility() == View.VISIBLE) {
            snackOutMessageReplyView(messageReplyView);
            return;
        }
        AppConstants.activeChatId = null;
        messages.clear();
        recipientProperties = null;
        recipientId = null;
        DirectModelNotifier.get().unregisterForModelChanges(ChatMessage.class, onModelStateChangedListener);
        if (canGoBackToMain) {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        } else {
            super.onBackPressed();
        }
    }

    private class ComposeKeyPressedListener implements View.OnKeyListener,
            View.OnClickListener, TextWatcher,
            View.OnFocusChangeListener {

        private int beforeLength;

        @Override
        public boolean onKey(View v, int keyCode, KeyEvent event) {
            if (event.getAction() == KeyEvent.ACTION_DOWN) {
                if (keyCode == KeyEvent.KEYCODE_ENTER) {
                    sendMessageContainer.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER));
                    sendMessageContainer.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_ENTER));
                    return true;
                }
            }
            return false;
        }

        @Override
        public void onClick(View v) {
            container.showSoftKeyboard(composeText);
        }

        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            beforeLength = composeText.getText().length();
        }

        @Override
        public void afterTextChanged(Editable s) {
            if (composeText.getText().length() == 0 || beforeLength == 0) {
                composeText.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        updateToggleButtonState();
                    }
                }, 50);
            }
            String entireString = s.toString();
            applyRichTextFormatting(s, entireString);
        }

        @Override
        @SuppressWarnings("unchecked")
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            ArrayList<String> linksInMessage = UiUtils.pullLinks(s.toString());
            if (!linksInMessage.isEmpty()) {
                String firstUrl = linksInMessage.get(linksInMessage.size() - 1);
                UiUtils.showView(linkPreviewLayout, true);
                linkPreview.setData(firstUrl);
            } else {
                UiUtils.showView(linkPreviewLayout, false);
            }
        }

        @Override
        public void onFocusChange(View v, boolean hasFocus) {

        }

    }

    private void applyRichTextFormatting(Editable s, String entireString) {
        boldTags = UiUtils.pullBoldTags(entireString);
        italicTags = UiUtils.pullItalicTags(entireString);
        strikeThroughTags = UiUtils.pullStrikeThroughTags(entireString);

        if (!boldTags.isEmpty()) {
            for (String boldTag : boldTags) {
                int startOfWord = getStartOfWord(entireString, boldTag);
                int endOfWord = getEndOfWord(boldTag, startOfWord);
                StyleSpan bss = new StyleSpan(Typeface.BOLD);
                s.setSpan(bss, startOfWord, endOfWord, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                grayStartAndEndTags(s, startOfWord, endOfWord);
            }
        }

        if (!italicTags.isEmpty()) {
            for (String italicTag : italicTags) {
                int startOfWord = getStartOfWord(entireString, italicTag);
                int endOfWord = getEndOfWord(italicTag, startOfWord);
                StyleSpan bss = new StyleSpan(Typeface.ITALIC);
                s.setSpan(bss, startOfWord, endOfWord, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                grayStartAndEndTags(s, startOfWord, endOfWord);
            }
        }

        if (!strikeThroughTags.isEmpty()) {
            for (String strikeThrough : strikeThroughTags) {
                int startOfWord = getStartOfWord(entireString, strikeThrough);
                int endOfWord = getEndOfWord(strikeThrough, startOfWord);
                StrikethroughSpan bss = new StrikethroughSpan();
                s.setSpan(bss, startOfWord, endOfWord, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
                grayStartAndEndTags(s, startOfWord, endOfWord);
            }
        }
    }

    private void grayStartAndEndTags(Editable s, int startOfWord, int endOfWord) {
        s.setSpan(new ForegroundColorSpan(Color.GRAY), startOfWord - 1, startOfWord,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        s.setSpan(new ForegroundColorSpan(Color.GRAY), endOfWord, endOfWord + 1,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
    }

    private int getEndOfWord(String word, int startingPosition) {
        return startingPosition + word.length();
    }

    private int getStartOfWord(String sentence, String word) {
        return sentence.indexOf(word);
    }

    private void displayInactiveSendButton() {
        inputPanel.displayControl(recordAudioButton);
    }

    private void displayActiveSendButton() {
        inputPanel.displayControl(sendMessageContainer);
    }

    private void updateToggleButtonState() {
        if (composeText.getText().length() == 0) {
            displayInactiveSendButton();
            sendChatStateMsg(getString(R.string.idle));
        } else {
            displayActiveSendButton();
            sendChatStateMsg(getString(R.string.typing));
        }
    }

    public void vibrateVibrator() {
        vibrator.vibrate(100);
    }

    private class SendButtonListener implements View.OnClickListener, TextView.OnEditorActionListener {

        @Override
        public void onClick(View v) {
            if (inputPanel.canRecord()) {
                vibrateVibrator();
                inputPanel.startRecorder();
            } else {
                UiUtils.bangSound(ChatActivity.this, R.raw.message_sent);
                if (recordingInProgress) {
                    inputPanel.stopRecording();
                    long seconds = inputPanel.getElapsedTime(); //Get the Elapsed Time of this Voice Note
                    sendVoiceMessage(recorderAudioCaptureFilePath.getPath(), seconds);
                } else {
                    if (!pickedMediaFiles.isEmpty()) {
                        iterateThroughPickedMediaAndSendEach();
                    } else {
                        //Send message,considering our file paths
                        if (StringUtils.isNotEmpty(composeText.getText().toString().trim())) {
                            sendTextMessage(composeText.getText().toString().trim());
                        }
                    }
                }
            }
        }

        @Override
        public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
            if (actionId == EditorInfo.IME_ACTION_SEND) {
                sendMessageContainer.performClick();
                return true;
            }
            return false;
        }

    }

    public void iterateThroughPickedMediaAndSendEach() {
        for (HolloutFile holloutFile : pickedMediaFiles) {
            switch (holloutFile.getFileType()) {
                case AppConstants.FILE_TYPE_PHOTO:
                    sendImageMessage(holloutFile.getLocalFilePath(), StringUtils.isNotEmpty(composeText.getText().toString().trim())
                            ? processSpansIfAvailable(composeText.getText().toString().trim()) : "Photo");
                    break;
                case AppConstants.FILE_TYPE_AUDIO:
                    //Send file message with file type audio
                    ChatMessage audioMessage = new ChatMessage();
                    attachRequiredPropsToMessage(audioMessage, MessageType.AUDIO);
                    audioMessage.setAudioDuration(UiUtils.getTimeString(HolloutUtils.getVideoDuration(holloutFile.getLocalFilePath())));
                    audioMessage.setFileCaption(holloutFile.getFileName());
                    audioMessage.setLocalUrl(holloutFile.getLocalFilePath());
                    checkAndAddNewMessage(audioMessage);
                    DbUtils.createMessage(audioMessage);
                    emptyComposeText();
                    FileUploader.getInstance().uploadFile(holloutFile.getLocalFilePath(), AppConstants.AUDIOS_DIRECTORY, audioMessage.getMessageId(), recipientProperties);
                    break;
                case AppConstants.FILE_TYPE_VIDEO:
                    sendVideoMessage(holloutFile.getLocalFilePath(), holloutFile.getLocalFilePath(),
                            StringUtils.isNotEmpty(composeText.getText().toString().trim())
                                    ? processSpansIfAvailable(composeText.getText().toString().trim()) : "Video");
                    break;
            }
        }
        hidePickedMedia();
    }

    private class AttachButtonListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            UiUtils.dismissKeyboard(composeText);
            if (Build.VERSION.SDK_INT >= 23 && PermissionsUtils.checkSelfForStoragePermission(ChatActivity.this)) {
                holloutPermissions.requestStoragePermissions();
                setLastPermissionInitiationAction(AppConstants.REQUEST_STORAGE_ACCESS_FOR_GALLERY);
                return;
            }
            handleAddAttachment();
        }
    }

    public void snackOutMessageReplyView(final View view) {
        UiUtils.showView(view, false);
        AppConstants.selectedMessages.clear();
        AppConstants.selectedMessagesPositions.clear();
        composeText.setHint(getString(R.string.compose_message));
    }

    @SuppressLint("SetTextI18n")
    public void snackInMessageReplyView(View view) {
        UiUtils.showView(view, true);
        ChatMessage messageToReplyTo = AppConstants.selectedMessages.get(0);
        composeText.setHint("Reply this message");
        if (messageToReplyTo != null) {
            String senderName = messageToReplyTo.getFromName();
            String senderId = messageToReplyTo.getFrom();
            if (senderId != null && senderName != null) {
                replyMessageTitleView.setText(StringUtils.capitalize(senderId.equals(signedInUser.getString(AppConstants.REAL_OBJECT_ID))
                        ? getString(R.string.you) : senderName));
            } else {
                if (senderName != null) {
                    replyMessageTitleView.setText(StringUtils.capitalize(messageToReplyTo.getMessageDirection() == MessageDirection.OUTGOING
                            ? getString(R.string.you) : senderName));
                }
            }
            MessageType messageType = getMessageType(messageToReplyTo);
            if (messageType == MessageType.TXT) {
                replyMessageSubTitleView.setText(messageToReplyTo.getMessageBody());
            } else if (messageType == MessageType.GIF) {
                String gifUrl = messageToReplyTo.getGifUrl();
                UiUtils.showView(replyIconView, true);
                UiUtils.showView(playReplyMessageIfVideo, false);
                replyMessageSubTitleView.setText(UiUtils.fromHtml("<b>" + getString(R.string.gif) + "</b>"));
                loadGif(gifUrl);
            } else if (messageType == MessageType.REACTION) {
                String reactionValue = messageToReplyTo.getReactionValue();
                UiUtils.showView(playReplyMessageIfVideo, false);
                UiUtils.showView(replyIconView, true);
                loadDrawables(ChatActivity.this, replyIconView, reactionValue);
                replyMessageSubTitleView.setText(StringUtils.strip(reactionValue.split("/")[1], ".json"));
            } else if (messageType == MessageType.IMAGE) {
                String filePath = messageToReplyTo.getLocalUrl();
                File file = new File(filePath);
                if (file.exists()) {
                    filePath = messageToReplyTo.getLocalUrl();
                } else {
                    filePath = messageToReplyTo.getRemoteUrl();
                }
                UiUtils.showView(replyIconView, true);
                UiUtils.showView(playReplyMessageIfVideo, false);
                UiUtils.loadImage(ChatActivity.this, filePath, replyIconView);
                String fileCaption = messageToReplyTo.getFileCaption();
                if (StringUtils.isNotEmpty(fileCaption)) {
                    replyMessageSubTitleView.setText(fileCaption);
                } else {
                    replyMessageSubTitleView.setText(getString(R.string.photo));
                }
            } else if (messageType == MessageType.VIDEO) {
                String remoteVideoThumbnailUrl = messageToReplyTo.getThumbnailUrl();
                File localThumbFile = new File(messageToReplyTo.getLocalThumb());
                UiUtils.showView(replyIconView, true);
                UiUtils.showView(playReplyMessageIfVideo, true);
                if (StringUtils.isNotEmpty(remoteVideoThumbnailUrl)) {
                    HolloutLogger.d("VideoThumbnailPath", "Remote Video Thumb exists with value = " + remoteVideoThumbnailUrl);
                    UiUtils.loadImage(ChatActivity.this, messageToReplyTo.getThumbnailUrl(), replyIconView);
                    UiUtils.showView(playReplyMessageIfVideo, true);
                } else {
                    if (localThumbFile.exists()) {
                        HolloutLogger.d("VideoThumbnailPath", "Local Video Thumb exists with value = " + localThumbFile);
                        loadVideoFromPath(replyIconView, messageToReplyTo.getLocalThumb());
                    }
                }
                String videoCaption = messageToReplyTo.getFileCaption();
                if (videoCaption != null) {
                    replyMessageSubTitleView.setText(videoCaption);
                } else {
                    replyMessageSubTitleView.setText(getString(R.string.video));
                }
            } else if (messageType == MessageType.LOCATION) {
                String locationName = messageToReplyTo.getLocationAddress();
                if (StringUtils.isNotEmpty(locationName)) {
                    replyMessageSubTitleView.setText(locationName);
                } else {
                    replyMessageSubTitleView.setText(getString(R.string.location));
                }
                UiUtils.showView(playReplyMessageIfVideo, false);
                String locationStaticMap = LocationUtils.loadStaticMap(messageToReplyTo.getLatitude(),
                        messageToReplyTo.getLongitude());
                if (StringUtils.isNotEmpty(locationStaticMap)) {
                    UiUtils.showView(replyIconView, true);
                    UiUtils.loadImage(ChatActivity.this, locationStaticMap, replyIconView);
                }
            } else if (messageType == MessageType.CONTACT) {
                UiUtils.showView(playReplyMessageIfVideo, false);
                String contactName = messageToReplyTo.getContactName();
                String contactPhoneNumber = messageToReplyTo.getContactNumber();
                String purifiedPhoneNumber = StringUtils.stripEnd(contactPhoneNumber, ",");
                replyMessageSubTitleView.setText(contactName + ":" + purifiedPhoneNumber);
            } else if (messageType == MessageType.AUDIO) {
                UiUtils.showView(playReplyMessageIfVideo, false);
                replyMessageSubTitleView.setText(getString(R.string.audio));
            } else if (messageType == MessageType.DOCUMENT) {
                UiUtils.showView(playReplyMessageIfVideo, false);
                String documentName = messageToReplyTo.getDocumentName();
                String documentSize = messageToReplyTo.getDocumentSize();
                if (StringUtils.isNotEmpty(documentName)) {
                    replyMessageSubTitleView.setText(documentName + "\n" + documentSize);
                }
            } else if (messageType == MessageType.VOICE) {
                UiUtils.showView(playReplyMessageIfVideo, false);
                String audioDuration = messageToReplyTo.getAudioDuration();
                String fileCaption = "Voice Note";
                replyMessageSubTitleView.setText(audioDuration + ":" + fileCaption);
            }
            closeReplyMessageView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    snackOutMessageReplyView(messageReplyView);
                }
            });
        }
    }

    public void loadVideoFromPath(ImageView videoView, String videoPath) {
        if (videoView != null) {
            if (Build.VERSION.SDK_INT >= 17) {
                if (!this.isDestroyed()) {
                    Glide.with(this).load(videoPath).error(R.drawable.ex_completed_ic_video).placeholder(R.drawable.ex_completed_ic_video).crossFade().into(videoView);
                }
            } else {
                Glide.with(this).load(videoPath).error(R.drawable.ex_completed_ic_video).placeholder(R.drawable.ex_completed_ic_video).crossFade().into(videoView);
            }
        }
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

    private void loadGif(String gifUrl) {
        if (StringUtils.isNotEmpty(gifUrl)) {
            if (Build.VERSION.SDK_INT >= 17) {
                if (!this.isDestroyed()) {
                    if (StringUtils.isNotEmpty(gifUrl)) {
                        Glide.with(this).load(gifUrl).asGif().listener(new RequestListener<String, GifDrawable>() {

                            @Override
                            public boolean onException(Exception e, String model, Target<GifDrawable> target, boolean isFirstResource) {
                                return false;
                            }

                            @Override
                            public boolean onResourceReady(GifDrawable resource, String model, Target<GifDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                                return false;

                            }
                        }).into(replyIconView);
                    }
                }
            } else {
                if (StringUtils.isNotEmpty(gifUrl)) {
                    Glide.with(this).load(gifUrl).asGif().listener(new RequestListener<String, GifDrawable>() {

                        @Override
                        public boolean onException(Exception e, String model, Target<GifDrawable> target, boolean isFirstResource) {
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(GifDrawable resource, String model, Target<GifDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                            return false;
                        }
                    }).into(replyIconView);
                }
            }
        }
    }

    private MessageType getMessageType(ChatMessage message) {
        return message.getMessageType();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.chat_options_menu, menu);
        MenuItem searchMessages = menu.findItem(R.id.search_chats);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.view_profile_info:
                chatToolbar.openUserOrGroupProfile();
                break;
            case R.id.place_call:
                List<String> signedInUserChats = signedInUser.getList(AppConstants.APP_USER_CHATS);
                if (signedInUserChats != null && signedInUserChats.contains(recipientId)) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(ChatActivity.this);
                    builder.setItems(new CharSequence[]{"Voice Call", "Video Call"}, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            Intent intent = new Intent();
                            List<String> recipientBlackList = recipientProperties.getList(AppConstants.USER_BLACK_LIST);
                            switch (which) {
                                case 0:
                                    //Check to see if this user is trying to call has him in his contacts
                                    if (recipientBlackList == null) {
                                        //Is this user in the recipient contact list?
                                        checkAndStartOnlineVoiceCall(intent);
                                    } else {
                                        if (!recipientBlackList.isEmpty() && recipientBlackList.contains(signedInUser.getString(AppConstants.REAL_OBJECT_ID))) {
                                            //Sorry them don block you o...E don red o...Lolz
                                            UiUtils.showSafeToast("Sorry, you can't call this user at this point in time.");
                                        } else {
                                            checkAndStartOnlineVoiceCall(intent);
                                        }
                                    }
                                    break;
                                case 1:
                                    //Check to see if the user this user is trying to call has him in his contacts
                                    if (recipientBlackList == null) {
                                        //Is this user in the recipient contact list?
                                        checkAndStartOnlineVideoCall(intent);
                                    } else {
                                        if (!recipientBlackList.isEmpty() && recipientBlackList.contains(signedInUser.getString(AppConstants.REAL_OBJECT_ID))) {
                                            //Sorry them don block you o...E don red o...Lolz
                                            UiUtils.showSafeToast("Sorry, you can't call this user at this point in time.");
                                        } else {
                                            checkAndStartOnlineVideoCall(intent);
                                        }
                                    }
                                    break;
                            }
                        }
                    });
                    builder.create().show();
                }
                break;
            case R.id.delete_conversation:
                AlertDialog.Builder deleteConversationPrompt = new AlertDialog.Builder(ChatActivity.this);
                deleteConversationPrompt.setMessage("Delete conversation with this user?");
                deleteConversationPrompt.setPositiveButton("DELETE CONVERSATION", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        deleteConversationProgressDialog.show();
                        deleteConversationProgressDialog.setTitle("Deleting Conversation");
                        DbUtils.deleteConversation(getRecipient(), new DoneCallback<Long[]>() {
                            @Override
                            public void done(Long[] progressValues, Exception e) {
                                long current = progressValues[0];
                                long total = progressValues[1];
                                if (current != -1 && total != 0) {
                                    double percentage = (100.0 * (current + 1)) / total;
                                    deleteConversationProgressDialog.setProgress((int) percentage);
                                    if (percentage == 100) {
                                        checkDismissConversationProgressDialog();
                                        UiUtils.showSafeToast("Conversation cleared");
                                    }
                                } else {
                                    checkDismissConversationProgressDialog();
                                    UiUtils.showSafeToast("No conversation to clear.");
                                }
                            }
                        });
                    }
                });
                deleteConversationPrompt.setNegativeButton("NO", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                deleteConversationPrompt.create().show();
                break;
            case R.id.search_chats:
                chatToolbar.openSearchView();
                break;
            case R.id.block_user:
                if (HolloutUtils.isUserBlocked(getRecipient())) {
                    attemptToUnBlockUser();
                } else {
                    attemptToBlockUser();
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void checkAndStartOnlineVoiceCall(Intent intent) {
        List<String> recipientChatList = recipientProperties.getList(AppConstants.APP_USER_CHATS);
        if (recipientChatList != null && !recipientChatList.isEmpty()) {
            if (recipientChatList.contains(signedInUser.getString(AppConstants.REAL_OBJECT_ID))) {
                startOnlineVoiceCall(intent);
            } else {
                UiUtils.showSafeToast("Sorry, you can't call this user at this point in time.");
            }
        } else {
            UiUtils.showSafeToast("Sorry, you can't call this user at this point in time.");
        }
    }

    private void checkAndStartOnlineVideoCall(Intent intent) {
        List<String> recipientChatList = recipientProperties.getList(AppConstants.APP_USER_CHATS);
        if (recipientChatList != null && !recipientChatList.isEmpty()) {
            if (recipientChatList.contains(signedInUser.getString(AppConstants.REAL_OBJECT_ID))) {
                startOnlineVideoCall(intent);
            } else {
                UiUtils.showSafeToast("Sorry, you can't call this user at this point in time.");
            }
        } else {
            UiUtils.showSafeToast("Sorry, you can't call this user at this point in time.");
        }
    }

    private void startOnlineVoiceCall(Intent intent) {
        this.callIntent = intent;
        setCallInitiationType(VOICE_CALL, intent);
        if (!audioPermissionsMet()) {
            return;
        }
        intent.setClass(ChatActivity.this, VoiceCallActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(AppConstants.CALLER_ID, recipientId);
        intent.putExtra(AppConstants.EXTRA_IS_INCOMING_CALL, false);
        startActivity(intent);
    }

    private void startOnlineVideoCall(Intent intent) {
        this.callIntent = intent;
        setCallInitiationType(VIDEO_CALL, intent);
        if (!audioPermissionsMet()) {
            return;
        }
        setCallInitiationType(VIDEO_CALL, intent);
        intent.setClass(ChatActivity.this, VideoCallActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(AppConstants.CALLER_ID, recipientId);
        intent.putExtra(AppConstants.EXTRA_IS_INCOMING_CALL, false);
        startActivity(intent);
    }

    private boolean audioPermissionsMet() {
        if (PermissionsUtils.checkSelfPermissionForAudioRecording(this)) {
            holloutPermissions.requestAudio();
            setLastPermissionInitiationAction(AppConstants.REQUEST_AUDIO_ACCESS_FOR_RECORDING);
            return false;
        }
        return true;
    }

    private void setCallInitiationType(int type, Intent intent) {
        this.callType = type;
    }

    private int getCallInitiationType() {
        return callType;
    }

    private Intent getCallIntent() {
        return callIntent;
    }

    private void attemptToUnBlockUser() {
        final ProgressDialog progressDialog = UiUtils.showProgressDialog(ChatActivity.this, "Trying to unblock user. Please wait...");
        HolloutUtils.unBlockUser(getRecipient(), new DoneCallback<Boolean>() {
            @Override
            public void done(Boolean success, Exception e) {
                UiUtils.dismissProgressDialog(progressDialog);
                if (success) {
                    UiUtils.showSafeToast("User Unblocked successfully!");
                } else {
                    UiUtils.showSafeToast("Failed to Unblock user. Please try again.");
                }
                checkDidIBlackListUser();
            }
        });
    }

    private void attemptToBlockUser() {
        AlertDialog.Builder blockConsentDialog = new AlertDialog.Builder(this);
        blockConsentDialog.setMessage("Block User?");
        blockConsentDialog.setPositiveButton("BLOCK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                final ProgressDialog progressDialog = UiUtils.showProgressDialog(ChatActivity.this, "Trying to block user. Please wait...");
                HolloutUtils.blockUser(getRecipient(), new DoneCallback<Boolean>() {
                    @Override
                    public void done(Boolean success, Exception e) {
                        UiUtils.dismissProgressDialog(progressDialog);
                        if (success) {
                            UiUtils.showSafeToast("User Blocked successfully!");
                            checkDidIBlackListUser();
                        } else {
                            UiUtils.showSafeToast("Sorry, an error occurred while trying to block user. Please try again.");
                        }
                    }
                });
            }
        });
        blockConsentDialog.setNegativeButton("NO", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        blockConsentDialog.create().show();
    }

    private void checkDidIBlackListUser() {
        if (HolloutUtils.isUserBlocked(getRecipient())) {
            composeText.setHint("User Blocked!!!");
        } else {
            composeText.setHint("Compose Message");
        }
    }

    private void setupChatRecipient(ParseObject result) {
        if (StringUtils.isNotEmpty(recipientId)) {
            if (chatToolbar != null) {
                chatToolbar.refreshToolbar(result);
                chatToolbar.setChatToolbarUserChangeListener(new ChatToolbar.ChatToolbarUserChangeListener() {
                    @Override
                    public void onUserChanged(ParseObject newRecipientProps) {
                        //Boom, user details have changed
                        recipientProperties = newRecipientProps;
                        checkBlackListStatus();
                    }
                });
            }
        }
    }

    public void setRecipientName(String recipientName) {
        this.recipientName = recipientName;
    }

    public String getRecipientName() {
        return recipientName;
    }

    @Override
    public void onRecorderStarted() {
        if (HolloutUtils.hasMarshmallow()) {
            if (PermissionsUtils.checkSelfPermissionForAudioRecording(this)) {
                holloutPermissions.requestAudio();
                setLastPermissionInitiationAction(AppConstants.REQUEST_AUDIO_ACCESS_FOR_RECORDING);
            } else {
                startRecorder();
            }
        } else {
            startRecorder();
        }
        displayActiveSendButton();
    }

    @Override
    public void onRecorderFinished() {
        releaseMediaRecorder();
        displayInactiveSendButton();
    }

    @Override
    public void onEmojiToggle() {
        emojiPopup.toggle();
    }

    @Override
    public void onKeyboardShown() {
        inputPanel.onKeyboardShown();
    }

    @Override
    protected void onStart() {
        super.onStart();
        DirectModelNotifier.get().registerForModelChanges(ChatMessage.class, onModelStateChangedListener);
        setActiveChat();
        checkBlackListStatus();
    }

    private void checkDidUserBlackListMe() {
        boolean wasIBlackListed = HolloutUtils.checkDidUserBlockMe(recipientProperties);
        if (wasIBlackListed) {
            composeText.setEnabled(false);
            attachButton.setEnabled(false);
            recordAudioButton.setEnabled(false);
        } else {
            composeText.setEnabled(true);
            attachButton.setEnabled(true);
            recordAudioButton.setEnabled(true);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (isFinishing()) overridePendingTransition(R.anim.fade_scale_in, R.anim.slide_to_right);
        inputPanel.onPause();
    }

    public void sendChatStateMsg(final String chatState) {
        HolloutUtils.sendChatState(chatState, recipientId);
    }

    public void checkAccessToGalleryAndOpen() {
        if (HolloutUtils.hasMarshmallow()) {
            if (PermissionsUtils.checkSelfForStoragePermission(this)) {
                holloutPermissions.requestStoragePermissions();
                setLastPermissionInitiationAction(AppConstants.REQUEST_STORAGE_ACCESS_FOR_GALLERY);
            } else {
                openGallery();
            }
        } else {
            openGallery();
        }
    }

    public void setLastPermissionInitiationAction(String action) {
        this.lastExecutablePermissionAction = action;
    }

    public String getLastPermissionInitiationAction() {
        return lastExecutablePermissionAction;
    }

    public void openGallery() {
        Intent mGalleryIntent = new Intent(ChatActivity.this, GalleryActivity.class);
        mGalleryIntent.putExtra(AppConstants.RECIPIENT_NAME, getRecipientName());
        startActivityForResult(mGalleryIntent, AppConstants.REQUEST_CODE_PICK_FROM_GALLERY);
    }

    public void checkAccessToDocumentAndOpenDocuments() {
        if (HolloutUtils.hasMarshmallow()) {
            if (PermissionsUtils.checkSelfForStoragePermission(this)) {
                holloutPermissions.requestStoragePermissions();
                setLastPermissionInitiationAction(AppConstants.REQUEST_STORAGE_ACCESS_FOR_DOCUMENTS);
            } else {
                openDocuments();
            }
        } else {
            openDocuments();
        }
    }

    public void checkContactAccessAndOpenContact() {
        if (HolloutUtils.hasMarshmallow() && PermissionsUtils.checkSelfForContactPermission(this)) {
            holloutPermissions.requestContactPermission();
        } else {
            openContacts();
        }
    }

    public void openContacts() {
        Intent contactIntent = new Intent(Intent.ACTION_PICK, ContactsContract.Contacts.CONTENT_URI);
        contactIntent.setType(ContactsContract.Contacts.CONTENT_TYPE);
        startActivityForResult(contactIntent, REQUEST_CODE_CONTACT_SHARE);
    }

    public void checkLocationAccessPermissionsAndOpenLocation() {
        if (HolloutUtils.hasMarshmallow() && PermissionsUtils.checkSelfPermissionForLocation(this)) {
            holloutPermissions.requestLocationPermissions();
        } else {
            pickLocation();
        }
    }

    public void pickLocation() {
        PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();
        try {
            startActivityForResult(builder.build(this), PLACE_LOCATION_PICKER_REQUEST_CODE);
        } catch (Exception ignored) {
        }
    }

    public void openDocuments() {
        final Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("*/*");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(intent, REQUEST_CODE_PICK_DOCUMENT);
    }

    public void openCameraToTakePhoto() {
        new ImagePicker.Builder(ChatActivity.this)
                .mode(ImagePicker.Mode.CAMERA)
                .compressLevel(ImagePicker.ComperesLevel.NONE)
                .directory(ImagePicker.Directory.DEFAULT)
                .extension(ImagePicker.Extension.PNG)
                .allowMultipleImages(false)
                .build();
    }

    public void openCameraToShootVideo() {
        new VideoPicker.Builder(this).mode(VideoPicker.Mode.CAMERA).directory(VideoPicker.Directory.DEFAULT)
                .build();
    }

    @SuppressLint("CommitPrefEdits")
    public void previewSinglePickedFile(String fileType, final String pickedFilePath) {
        HolloutPreferences.setLastFileCaption();
        final HolloutFile pickedHolloutFile = new HolloutFile();
        pickedHolloutFile.setLocalFilePath(pickedFilePath);
        File file = new File(pickedFilePath);
        pickedHolloutFile.setFileName(file.getName());
        pickedHolloutFile.setFileType(fileType);
        if (!pickedMediaFiles.contains(pickedHolloutFile)) {
            pickedMediaFiles.add(pickedHolloutFile);
        }
        if (pickedMediaFiles.size() == 1) {
            UiUtils.showView(singleMediaFrame, true);
            UiUtils.showView(singleMediaViewer, true);
            UiUtils.showView(cancelPickedSingleMedia, true);
            switch (fileType) {
                case AppConstants.FILE_TYPE_PHOTO:
                    UiUtils.loadImage(this, pickedFilePath, singleMediaViewer);
                    UiUtils.showView(playSingleMediaIfVideo, false);
                    UiUtils.showView(mediaLengthView, false);
                    break;
                case AppConstants.FILE_TYPE_VIDEO:
                    if (Build.VERSION.SDK_INT >= 17) {
                        if (!this.isDestroyed()) {
                            Glide.with(this).load(pickedFilePath).error(R.drawable.ex_completed_ic_video).placeholder(R.drawable.ex_completed_ic_video).crossFade().into(singleMediaViewer);
                        }
                    } else {
                        Glide.with(this).load(pickedFilePath).error(R.drawable.ex_completed_ic_video).placeholder(R.drawable.ex_completed_ic_video).crossFade().into(singleMediaViewer);
                    }
                    String fileMime = FileUtils.getMimeType(pickedFilePath);
                    if (HolloutUtils.isVideo(fileMime)) {
                        UiUtils.showView(playSingleMediaIfVideo, true);
                        UiUtils.showView(mediaLengthView, true);
                        String videoLength = UiUtils.getTimeString(HolloutUtils.getVideoDuration(pickedFilePath));
                        if (StringUtils.isNotEmpty(videoLength)) {
                            mediaLengthView.setText(videoLength);
                        }
                    }
                    break;
                default:
                    UiUtils.showView(playSingleMediaIfVideo, true);
                    UiUtils.showView(mediaLengthView, true);
                    String videoLength = UiUtils.getTimeString(HolloutUtils.getVideoDuration(pickedFilePath));
                    if (StringUtils.isNotEmpty(videoLength)) {
                        mediaLengthView.setText(videoLength);
                    }
                    mediaLengthView.setTextColor(ContextCompat.getColor(this, R.color.hollout_color_one));
                    singleMediaViewer.setImageResource(R.drawable.attach_audio);
                    break;
            }

            singleMediaViewer.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View view) {
                    if (pickedHolloutFile.getFileType().equals(AppConstants.FILE_TYPE_PHOTO)) {
                        UiUtils.previewSelectedFile(ChatActivity.this, pickedHolloutFile);
                    } else if (pickedHolloutFile.getFileType().equals(AppConstants.FILE_TYPE_VIDEO)
                            || pickedHolloutFile.getFileType().equals(AppConstants.FILE_TYPE_AUDIO)) {
                        FileUtils.openFile(new File(pickedHolloutFile.getLocalFilePath()), ChatActivity.this);
                    }
                }
            });

        } else {
            UiUtils.showView(singleMediaFrame, false);
            UiUtils.showView(singleMediaViewer, false);
            UiUtils.showView(cancelPickedSingleMedia, false);
            UiUtils.showView(multiplePickedMediaFilesRecyclerView, true);
            multiplePickedMediaFilesRecyclerView.getRecycledViewPool().clear();
            pickedMediaFilesAdapter.notifyDataSetChanged();
        }
        composeText.setHint("Add caption");
        displayActiveSendButton();
    }

    public void previewMultiplePickedFiles(ArrayList<HolloutFile> galleryResults) {
        if (!pickedMediaFiles.containsAll(galleryResults)) {
            if (pickedMediaFiles.size() < 10) {
                pickedMediaFiles.addAll(galleryResults);
                UiUtils.showView(singleMediaFrame, false);
                UiUtils.showView(singleMediaViewer, false);
                UiUtils.showView(cancelPickedSingleMedia, false);
                UiUtils.showView(multiplePickedMediaFilesRecyclerView, true);
                multiplePickedMediaFilesRecyclerView.getRecycledViewPool().clear();
                pickedMediaFilesAdapter.notifyDataSetChanged();
                composeText.setHint("Add caption");
                displayActiveSendButton();
            }
        } else {
            UiUtils.showSafeToast("Maximum files for transfer reached");
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == ImagePicker.IMAGE_PICKER_REQUEST_CODE && resultCode == RESULT_OK) {
            List<String> mPaths = (List<String>) data.getSerializableExtra(ImagePicker.EXTRA_IMAGE_PATH);
            if (mPaths != null) {
                if (!mPaths.isEmpty()) {
                    String pickedPhotoPath = mPaths.get(0);
                    if (StringUtils.isNotEmpty(pickedPhotoPath)) {
                        previewSinglePickedFile(AppConstants.FILE_TYPE_PHOTO, pickedPhotoPath);
                    }
                }
            }
        } else if (requestCode == VideoPicker.VIDEO_PICKER_REQUEST_CODE && resultCode == RESULT_OK) {
            String mPath = data.getStringExtra(VideoPicker.EXTRA_VIDEO_PATH);
            if (StringUtils.isNotEmpty(mPath)) {
                previewSinglePickedFile(AppConstants.FILE_TYPE_VIDEO, mPath);
            }
            //Your Code
        } else if (requestCode == AppConstants.REQUEST_CODE_PICK_FROM_GALLERY && resultCode == RESULT_OK) {
            ArrayList<HolloutFile> galleryResults = data.getParcelableArrayListExtra(AppConstants.GALLERY_RESULTS);
            if (galleryResults != null) {
                if (!galleryResults.isEmpty()) {
                    if (galleryResults.size() == 1) {
                        HolloutFile singleFile = galleryResults.get(0);
                        previewSinglePickedFile(singleFile.getFileType(), singleFile.getLocalFilePath());
                    } else if (galleryResults.size() > 1) {
                        previewMultiplePickedFiles(galleryResults);
                    }
                }
            }
        } else if (requestCode == REQUEST_CODE_PICK_DOCUMENT && resultCode == RESULT_OK) {
            if (data != null) {
                Uri uri = data.getData();
                if (uri != null) {
                    sendDocument(uri);
                }
            }
        } else if (requestCode == REQUEST_CODE_CONTACT_SHARE && resultCode == RESULT_OK) {
            File vCardFile;
            try {
                vCardFile = new ContactService(ChatActivity.this).vCard(data.getData());
                if (vCardFile != null) {
                    HolloutFile contactFile = new HolloutFile();
                    contactFile.setLocalFilePath(vCardFile.getPath());
                    contactFile.setLocalFilePath(AppConstants.FILE_TYPE_CONTACT);

                    String filePath = FilePathFinder.getPath(ChatActivity.this, Uri.fromFile(vCardFile));

                    HolloutVCFParser parser = new HolloutVCFParser();
                    VCFContactData vcfContactData = parser.parseCVFContactData(filePath);

                    String contactName = vcfContactData.getName();
                    String contactPhoneNumber = vcfContactData.getTelephoneNumber();

                    String[] numberParts = contactPhoneNumber.split(",");

                    if (numberParts.length == 1) {
                        contactPhoneNumber = StringUtils.strip(numberParts[0], ",");
                    }

                    ChatMessage contactMessage = new ChatMessage();
                    attachRequiredPropsToMessage(contactMessage, MessageType.CONTACT);
                    if (StringUtils.isNotEmpty(contactName)) {
                        contactMessage.setContactName(contactName);
                    }
                    if (StringUtils.isNotEmpty(contactPhoneNumber)) {
                        contactMessage.setContactNumber(contactPhoneNumber);
                    }
                    checkAndAddNewMessage(contactMessage);
                    sendContactMessage(contactMessage);
                } else {
                    UiUtils.showSafeToast("Oops! Failed");
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if (requestCode == PLACE_LOCATION_PICKER_REQUEST_CODE && resultCode == RESULT_OK) {
            Place place = PlacePicker.getPlace(this, data);
            if (place != null) {
                double latitude = place.getLatLng().latitude;
                double longitude = place.getLatLng().longitude;
                String locationAddress = (String) place.getAddress();
                if (StringUtils.isNotEmpty(locationAddress)) {
                    sendLocationMessage(latitude, longitude, locationAddress);
                } else {
                    UiUtils.showSafeToast(getString(R.string.unable_to_get_locatio));
                }
            }
        } else if (requestCode == RequestCodes.FORWARD_MESSAGE) {
            if (resultCode == RESULT_OK) {
                chatToolbar.updateActionMode(0);
                notifyDataSetChanged();
            }
        }
    }

    public void notifyDataSetChanged() {
        try {
            messagesAdapter.notifyDataSetChanged();
        } catch (IllegalStateException ignored) {

        }
    }

    @SuppressLint("Recycle")
    protected void sendDocument(Uri uri) {
        ChatMessage documentMessage = new ChatMessage();
        attachRequiredPropsToMessage(documentMessage, MessageType.DOCUMENT);
        String filePath = FileUtils.getPath(this, uri);
        File file;
        if (filePath != null) {
            file = new File(filePath);
            if (!file.exists()) {
                UiUtils.showSafeToast(getString(R.string.file_does_not_exist));
            } else {
                //limit the size < 10M
                if (file.length() > 10 * 1024 * 1024) {
                    UiUtils.showSafeToast(getString(R.string.file_greater_than_max));
                } else {
                    String fileMime = FileUtils.getMimeType(filePath);
                    if (!HolloutUtils.isValidDocument(fileMime)) {
                        if (HolloutUtils.isImage(fileMime)) {
                            sendImageMessage(filePath, "Photo");
                        } else {
                            UiUtils.showSafeToast("Not a valid document");
                        }
                    } else {
                        documentMessage.setLocalUrl(filePath);
                        documentMessage.setFileMimeType(fileMime);
                        documentMessage.setDocumentName(file.getName());
                        documentMessage.setDocumentSize(HolloutUtils.getFileSize(ChatActivity.this, uri));
                        checkAndAddNewMessage(documentMessage);
                        DbUtils.createMessage(documentMessage);
                        emptyComposeText();
                        sendDocumentMessage(documentMessage);
                    }
                }
            }
        } else {
            UiUtils.showSafeToast("Oops! error fetching file. Please ensure the file is in your sd card.");
        }
    }

    @Override
    @SuppressWarnings("unused")
    @Subscribe(sticky = true, threadMode = ThreadMode.ASYNC)
    public void onEventAsync(final Object o) {
        UiUtils.runOnMain(new Runnable() {
            @Override
            public void run() {
                if (o instanceof String) {
                    String s = (String) o;
                    switch (s) {
                        case AppConstants.REPLY_MESSAGE:
                            snackInMessageReplyView(messageReplyView);
                            getChatToolbar().justHideActionMode();
                            notifyDataSetChanged();
                            break;
                        case AppConstants.HIDE_MESSAGE_REPLY_VIEW:
                            snackOutMessageReplyView(messageReplyView);
                            break;
                        case AppConstants.REFRESH_MESSAGES_ADAPTER:
                            try {
                                notifyDataSetChanged();
                            } catch (IllegalStateException ignored) {

                            }
                            break;
                        case AppConstants.SUSPEND_ALL_USE_OF_AUDIO_MANAGER:
                            break;
                        case AppConstants.DELETE_ALL_SELECTED_MESSAGES:
                            if (AppConstants.selectedMessages != null && !AppConstants.selectedMessages.isEmpty()) {
                                for (ChatMessage emMessage : AppConstants.selectedMessages) {
                                    DbUtils.deleteMessage(emMessage);
                                }
                            }
                            getChatToolbar().updateActionMode(0);
                            if (messages.isEmpty()) {
                                UiUtils.showView(messagesEmptyView, true);
                            }
                            break;
                        case AppConstants.COPY_MESSAGE:
                            if (AppConstants.selectedMessages != null && !AppConstants.selectedMessages.isEmpty()) {
                                copyMessageToClipBoard();
                            }
                            break;
                    }
                } else if (o instanceof ReactionMessageEvent) {
                    ReactionMessageEvent reactionMessageEvent = (ReactionMessageEvent) o;
                    String reaction = reactionMessageEvent.getReaction();
                    ChatMessage chatMessage = new ChatMessage();
                    attachRequiredPropsToMessage(chatMessage, MessageType.REACTION);
                    chatMessage.setReactionValue(reaction);
                    UiUtils.bangSound(ChatActivity.this, R.raw.message_sent);
                    checkAndAddNewMessage(chatMessage);
                    sendNewMessage(chatMessage);
                } else if (o instanceof GifMessageEvent) {
                    GifMessageEvent gifMessageEvent = (GifMessageEvent) o;
                    ChatMessage chatMessage = new ChatMessage();
                    attachRequiredPropsToMessage(chatMessage, MessageType.GIF);
                    chatMessage.setGifUrl(gifMessageEvent.getGifUrl());
                    UiUtils.bangSound(ChatActivity.this, R.raw.message_sent);
                    checkAndAddNewMessage(chatMessage);
                    sendNewMessage(chatMessage);
                } else if (o instanceof ScrollToMessageEvent) {
                    ScrollToMessageEvent scrollToMessageEvent = (ScrollToMessageEvent) o;
                    final ChatMessage emMessage = scrollToMessageEvent.getEmMessage();
                    if (emMessage != null) {
                        int indexOfMessage = messages.indexOf(emMessage);
                        if (indexOfMessage != -1) {
                            ((LinearLayoutManager) messagesRecyclerView.getLayoutManager()).scrollToPositionWithOffset(indexOfMessage, 5);
                            AppConstants.bounceablePositions.put(emMessage.getMessageId().hashCode(), true);
                            notifyDataSetChanged();
                        } else {
                            loadMoreMessages(new DoneCallback<Boolean>() {
                                @Override
                                public void done(Boolean done, Exception e) {
                                    if (done) {
                                        int indexOfMessage = messages.indexOf(emMessage);
                                        if (indexOfMessage != -1) {
                                            messagesLayoutManager.scrollToPositionWithOffset(indexOfMessage, 5);
                                            AppConstants.bounceablePositions.put(emMessage.getMessageId().hashCode(), true);
                                            notifyDataSetChanged();
                                        }
                                    }
                                }
                            });
                        }
                    }
                } else if (o instanceof PlaceLocalCallEvent) {
                    PlaceLocalCallEvent placeLocalCallEvent = (PlaceLocalCallEvent) o;
                    if (PermissionsUtils.checkSelfForCallPermission(ChatActivity.this)) {
                        setPhoneNumberToCall(placeLocalCallEvent.getPhoneNumber());
                        holloutPermissions.requestCallPermission();
                    } else {
                        placeLocalCall(placeLocalCallEvent.getPhoneNumber());
                    }
                } else if (o instanceof SearchMessages) {
                    SearchMessages searchMessages = (SearchMessages) o;
                    searchMessages(searchMessages.getSearchString());
                }
            }
        });
    }

    private void searchMessages(final String searchString) {
        if (StringUtils.isNotEmpty(searchString)) {
            DbUtils.searchMessages(searchString,
                    new DoneCallback<List<ChatMessage>>() {
                        @Override
                        public void done(final List<ChatMessage> result, final Exception e) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    if (e == null && result != null && !result.isEmpty()) {
                                        messages.clear();
                                        messages.addAll(result);
                                        messagesAdapter.setSearchString(searchString);
                                        notifyDataSetChanged();
                                    }
                                }
                            });
                        }
                    });
        } else {
            messages.clear();
            messagesAdapter.setSearchString(null);
            notifyDataSetChanged();
            fetchMessages();
        }
    }

    private void copyMessageToClipBoard() {
        ChatMessage message = AppConstants.selectedMessages.get(0);
        if (message.getMessageType() == MessageType.TXT) {
            if (copyToClipboard(message.getMessageBody())) {
                UiUtils.showSafeToast("Message successfully copied ");
                chatToolbar.updateActionMode(0);
                notifyDataSetChanged();
            } else {
                UiUtils.showSafeToast("Failed to copy message");
            }
        }
    }

    @SuppressLint("NewApi")
    @SuppressWarnings("deprecation")
    public boolean copyToClipboard(String text) {
        try {
            int sdk = android.os.Build.VERSION.SDK_INT;
            if (sdk < android.os.Build.VERSION_CODES.HONEYCOMB) {
                android.text.ClipboardManager clipboard = (android.text.ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                clipboard.setText(text);
            } else {
                android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
                android.content.ClipData clip = android.content.ClipData
                        .newPlainText(getResources().getString(
                                R.string.new_clip), text);
                clipboard.setPrimaryClip(clip);
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private void refreshUnseenMessages() {
        UiUtils.showView(unreadMessagesIndicator, !tempReceivedMessages.isEmpty());
        if (!tempReceivedMessages.isEmpty()) {
            unreadMessagesIndicator.setText(String.valueOf(tempReceivedMessages.size()));
        }
    }

    private void scrollToBottom() {
        messagesLayoutManager.scrollToPosition(0);
        UiUtils.showView(scrollToBottomFrame, false);
        onScrolledUp = false;
        tempReceivedMessages.clear();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == PermissionsUtils.REQUEST_STORAGE && holloutPermissions.verifyPermissions(grantResults)) {
            if (getLastPermissionInitiationAction().equals(AppConstants.REQUEST_STORAGE_ACCESS_FOR_GALLERY)) {
                openGallery();
            } else if (getLastPermissionInitiationAction().equals(AppConstants.REQUEST_STORAGE_ACCESS_FOR_DOCUMENTS)) {
                openDocuments();
            }
        } else if (requestCode == PermissionsUtils.REQUEST_CONTACT && holloutPermissions.verifyPermissions(grantResults)) {
            openContacts();
        } else if (requestCode == PLACE_LOCATION_PICKER_REQUEST_CODE && holloutPermissions.verifyPermissions(grantResults)) {
            pickLocation();
        } else if (requestCode == PermissionsUtils.REQUEST_AUDIO_RECORD && holloutPermissions.verifyPermissions(grantResults)) {
            if (getCallInitiationType() == -1) {
                onRecorderStarted();
            } else if (getCallInitiationType() == VOICE_CALL) {
                startOnlineVoiceCall(getCallIntent());
                callType = -1;
            } else {
                startOnlineVideoCall(getCallIntent());
                callType = -1;
            }
        } else if (requestCode == PermissionsUtils.REQUEST_CALL_PHONE && holloutPermissions.verifyPermissions(grantResults)) {
            placeLocalCall(getPhoneNumberToCall());
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        sendChatStateMsg(getString(R.string.idle));
        HolloutPreferences.clearPreviousAttemptedMessage(recipientId);
        if (StringUtils.isNotEmpty(composeText.getText().toString().trim())) {
            HolloutPreferences.saveLastAttemptedMsg(recipientId, composeText.getText().toString().trim());
        }
        DirectModelNotifier.get().unregisterForModelChanges(ChatMessage.class, onModelStateChangedListener);
    }

    @NonNull
    public String getRecipient() {
        return recipientId != null ? recipientId.toLowerCase() : null;
    }

    private void attachRequiredPropsToMessage(ChatMessage chatMessage, MessageType messageType) {
        chatMessage.setMessageId(RandomStringUtils.random(6, true, true) + System.currentTimeMillis());
        ParseObject signedInUser = AuthUtil.getCurrentUser();
        if (signedInUser != null) {
            String userRealObjectId = signedInUser.getString(AppConstants.REAL_OBJECT_ID);
            String userDisplayName = signedInUser.getString(AppConstants.APP_USER_DISPLAY_NAME);
            String signedInUserPhotoUrl = signedInUser.getString(AppConstants.APP_USER_PROFILE_PHOTO_URL);
            String signedInUserId = signedInUser.getString(AppConstants.REAL_OBJECT_ID);
            if (StringUtils.isNotEmpty(signedInUserPhotoUrl)) {
                chatMessage.setFromPhotoUrl(signedInUserPhotoUrl);
            }
            if (StringUtils.isNotEmpty(userDisplayName)) {
                chatMessage.setFromName(userDisplayName);
            }
            chatMessage.setFrom(userRealObjectId.toLowerCase());
            if (messageReplyView.getVisibility() == View.VISIBLE && !AppConstants.selectedMessages.isEmpty()) {
                chatMessage.setRepliedMessageId(AppConstants.selectedMessages.get(0).getMessageId());
            }
            chatMessage.setMessageDirection(MessageDirection.OUTGOING);
            chatMessage.setMessageType(messageType);
            chatMessage.setMessageStatus(MessageStatus.SENT);
            chatMessage.setTo(getRecipient());
            chatMessage.setTimeStamp(System.currentTimeMillis());
            chatMessage.setConversationId(getRecipient());
        }
    }

    protected void sendTextMessage(String content) {
        content = processSpansIfAvailable(content);
        ChatMessage message = new ChatMessage();
        attachRequiredPropsToMessage(message, MessageType.TXT);
        message.setMessageBody(content);
        checkAndAddNewMessage(message);
        sendNewMessage(message);
    }

    private String processSpansIfAvailable(String content) {
        if (boldTags != null && !boldTags.isEmpty()) {
            for (String s : boldTags) {
                content = content.replace(s, "<font color=#111111>" + "<b>" + s + "</b>" + "</font>").replace("*", "");
            }
        }
        if (italicTags != null && !italicTags.isEmpty()) {
            for (String s : italicTags) {
                content = content.replace(s, "<font color=#888888>" + "<i>" + s + "</i>" + "</font>").replace("_", "");
            }
        }

        if (strikeThroughTags != null && !strikeThroughTags.isEmpty()) {
            for (String s : strikeThroughTags) {
                content = content.replace(s, "<font color=#777777>" + "<strike>" + s + "</strike>" + "</font>").replace("-", "");
            }
        }
        return content;
    }

    protected void sendVoiceMessage(String filePath, long duration) {
        String voiceNoteDuration = UiUtils.getTimeString(duration);
        ChatMessage message = new ChatMessage();
        attachRequiredPropsToMessage(message, MessageType.VOICE);
        message.setAudioDuration(String.valueOf(voiceNoteDuration));
        message.setLocalUrl(filePath);
        checkAndAddNewMessage(message);
        DbUtils.createMessage(message);
        emptyComposeText();
        FileUploader.getInstance().uploadFile(filePath, AppConstants.VOICE_NOTES, message.getMessageId(), recipientProperties);
    }

    @SuppressLint("CommitPrefEdits")
    protected void sendImageMessage(String imagePath, String caption) {
        String fileCaption;
        ChatMessage chatMessage = new ChatMessage();
        attachRequiredPropsToMessage(chatMessage, MessageType.IMAGE);
        if (StringUtils.isNotEmpty(caption)) {
            HolloutPreferences.setLastFileCaption(caption);
            fileCaption = caption;
        } else {
            fileCaption = HolloutPreferences.getLastFileCaption();
        }
        chatMessage.setLocalUrl(imagePath);
        chatMessage.setFileCaption(fileCaption);
        checkAndAddNewMessage(chatMessage);
        DbUtils.createMessage(chatMessage);
        emptyComposeText();
        FileUploader.getInstance().uploadFile(imagePath, AppConstants.PHOTO_DIRECTORY, chatMessage.getMessageId(), recipientProperties);
    }

    @SuppressLint("CommitPrefEdits")
    protected void sendVideoMessage(String videoPath, String thumbPath, String caption) {
        String fileCaption;
        ChatMessage chatMessage = new ChatMessage();
        attachRequiredPropsToMessage(chatMessage, MessageType.VIDEO);
        if (StringUtils.isNotEmpty(caption)) {
            HolloutPreferences.setLastFileCaption(caption);
            fileCaption = caption;
        } else {
            fileCaption = HolloutPreferences.getLastFileCaption();
        }
        chatMessage.setFileCaption(fileCaption);
        chatMessage.setLocalUrl(videoPath);
        chatMessage.setVideoDuration(HolloutUtils.getVideoDuration(videoPath));
        Bitmap videoThumb = ThumbnailUtils.createVideoThumbnail(videoPath, MediaStore.Images.Thumbnails.MINI_KIND);
        try {
            File thumbOutPutFile = HolloutUtils.getFilePath(System.currentTimeMillis() + "VideoThumb", this, "thumb", true);
            OutputStream outputStream = new FileOutputStream(thumbOutPutFile);
            videoThumb.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
            chatMessage.setLocalThumb(thumbOutPutFile.getPath());
            checkAndAddNewMessage(chatMessage);
            DbUtils.createMessage(chatMessage);
            emptyComposeText();
            FileUploader.getInstance().uploadVideoThumbnailAndProceed(thumbOutPutFile.getPath(), videoPath, AppConstants.THUMBS_DIRECTORY,
                    AppConstants.VIDEOS_DIRECTORY, chatMessage.getMessageId(), recipientProperties);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    protected void sendLocationMessage(double latitude, double longitude, String locationAddress) {
        ChatMessage chatMessage = new ChatMessage();
        attachRequiredPropsToMessage(chatMessage, MessageType.LOCATION);
        chatMessage.setLatitude(String.valueOf(latitude));
        chatMessage.setLongitude(String.valueOf(longitude));
        chatMessage.setLocationAddress(locationAddress);
        checkAndAddNewMessage(chatMessage);
        sendNewMessage(chatMessage);
    }

    private void sendContactMessage(ChatMessage contactMessage) {
        sendNewMessage(contactMessage);
    }

    protected void sendDocumentMessage(ChatMessage documentMessage) {
        FileUploader.getInstance().uploadFile(documentMessage.getLocalUrl(), AppConstants.DOCUMENTS, documentMessage.getMessageId(), recipientProperties);
    }

    private void updateSignedInUserChats() {
        List<String> chatIds = signedInUser.getList(AppConstants.APP_USER_CHATS);
        if (chatIds != null) {
            if (!chatIds.contains(getRecipient())) {
                chatIds.add(getRecipient());
                signedInUser.put(AppConstants.APP_USER_CHATS, chatIds);
                AuthUtil.updateCurrentLocalUser(signedInUser, null);
            }
        } else {
            chatIds = new ArrayList<>();
            chatIds.add(getRecipient());
            signedInUser.put(AppConstants.APP_USER_CHATS, chatIds);
            AuthUtil.updateCurrentLocalUser(signedInUser, null);
        }
    }

    private void sendNewChatRequest() {
        if (signedInUser != null && getRecipient() != null) {
            final String signedInUserId = signedInUser.getString(AppConstants.REAL_OBJECT_ID);
            final ParseObject newChatRequestObject = new ParseObject(AppConstants.HOLLOUT_FEED);
            newChatRequestObject.put(AppConstants.FEED_CREATOR_ID, signedInUserId.toLowerCase());
            newChatRequestObject.put(AppConstants.FEED_RECIPIENT_ID, getRecipient());
            newChatRequestObject.put(AppConstants.FEED_TYPE, AppConstants.FEED_TYPE_CHAT_REQUEST);
            newChatRequestObject.put(AppConstants.FEED_CREATOR, signedInUser);
            newChatRequestObject.saveInBackground(new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    if (e == null) {
                        updateSignedInUserChats();
                    } else {
                        newChatRequestObject.saveEventually();
                    }
                }
            });
        }
    }

    private void reAuthenticate() {
        Intent splashIntent = new Intent(ChatActivity.this, SplashActivity.class);
        startActivity(splashIntent);
        finish();
    }

    /**
     * set message Extension attributes
     */
    protected void sendNewMessage(ChatMessage newMessage) {
        ChatClient.getInstance().sendMessage(newMessage, recipientProperties);
        emptyComposeText();
        HolloutPreferences.updateConversationTime(recipientId);
        snackOutMessageReplyView(messageReplyView);
        prioritizeConversation();
        if (isAContact()) {
            return;
        }
        checkAcceptPendingInvitation();
    }

    @SuppressWarnings("unchecked")
    private boolean isPreviousSameDate(int position, Date dateToCompare) {
        if (messages.size() <= position) return false;
        Date previousPositionDate = new Date(messages.get(position).getTimeStamp());
        return DateFormatter.isSameDay(dateToCompare, previousPositionDate);
    }

    private void checkAndAddNewMessage(ChatMessage newMessage) {
        if (!messages.contains(newMessage)) {
            messages.add(0, newMessage);
            if (newMessage.getConversationId().toLowerCase().equals(getRecipient())) {
                UiUtils.bangSound(ChatActivity.this, R.raw.iapetus);
            }
            notifyDataSetChanged();
        }
        invalidateEmptyView();
        messagesRecyclerView.scrollToPosition(0);
    }

    public void checkAcceptPendingInvitation() {
        final ParseQuery<ParseObject> chatRequestsQuery = ParseQuery.getQuery(AppConstants.HOLLOUT_FEED);
        chatRequestsQuery.whereEqualTo(AppConstants.FEED_TYPE, AppConstants.FEED_TYPE_CHAT_REQUEST);
        chatRequestsQuery.include(AppConstants.FEED_CREATOR);
        chatRequestsQuery.whereEqualTo(AppConstants.FEED_RECIPIENT_ID, signedInUser.getString(AppConstants.REAL_OBJECT_ID).toLowerCase());
        chatRequestsQuery.whereEqualTo(AppConstants.FEED_CREATOR_ID, getRecipient());
        chatRequestsQuery.getFirstInBackground(new GetCallback<ParseObject>() {
            @Override
            public void done(final ParseObject returnedFeedObject, ParseException e) {
                if (e == null && returnedFeedObject != null) {
                    //The user sent me a friend request...Accept invitation here
                    List<String> signedInUserChats = signedInUser.getList(AppConstants.APP_USER_CHATS);
                    if (signedInUserChats != null && !signedInUserChats.contains(getRecipient())) {
                        signedInUserChats.add(getRecipient());
                    }
                    if (signedInUserChats == null) {
                        signedInUserChats = new ArrayList<>();
                        signedInUserChats.add(getRecipient());
                    }
                    signedInUser.put(AppConstants.APP_USER_CHATS, signedInUserChats);
                    AuthUtil.updateCurrentLocalUser(signedInUser, new DoneCallback<Boolean>() {
                        @Override
                        public void done(Boolean result, Exception e) {
                            if (e == null) {
                                if (returnedFeedObject != null) {
                                    returnedFeedObject.deleteInBackground(new DeleteCallback() {
                                        @Override
                                        public void done(ParseException e) {
                                            if (e != null) {
                                                AppConstants.CHAT_INVITATION_ACCEPTED = true;
                                                returnedFeedObject.deleteEventually();
                                            }
                                        }
                                    });
                                }
                            }
                        }
                    });
                } else {
                    if (e != null) {
                        if (e.getCode() == ParseException.OBJECT_NOT_FOUND) {
                            //No request
                            if (chatType == AppConstants.CHAT_TYPE_SINGLE) {
                                sendNewChatRequest();
                            }
                        }
                    }
                }
                chatRequestsQuery.cancel();
            }
        });
    }

    private void prioritizeConversation() {
        AppConstants.recentConversations.add(0, recipientProperties);
    }

    private boolean isAContact() {
        List<String> signedInUserChats = signedInUser.getList(AppConstants.APP_USER_CHATS);
        return (signedInUserChats != null && signedInUserChats.contains(recipientId.toLowerCase()));
    }

    private void emptyComposeText() {
        displayInactiveSendButton();
        composeText.setText("");
        composeText.setHint("Compose message");
    }

    @SuppressLint("MissingPermission")
    private void placeLocalCall(String purifiedPhoneNumber) {
        Intent callIntent = new Intent(Intent.ACTION_CALL);
        callIntent.setData(Uri.parse("tel:" + StringUtils.substringBefore(purifiedPhoneNumber, ",")));
        startActivity(callIntent);
    }

    static class MessageUpdateTask extends AsyncTask<Void, Void, Boolean> {

        private MessagesAdapter messagesAdapter;
        private ChatMessage chatMessage;
        private int indexOfMessage;
        private List<ChatMessage> messages;
        private WeakReference<Activity> weakReference;

        MessageUpdateTask(Activity activity, MessagesAdapter messagesAdapter, List<ChatMessage> messages,
                          ChatMessage chatMessage, int indexOfMessage) {
            this.weakReference = new WeakReference<>(activity);
            this.messages = messages;
            this.messagesAdapter = messagesAdapter;
            this.chatMessage = chatMessage;
            this.indexOfMessage = indexOfMessage;
        }

        @Override
        protected Boolean doInBackground(Void... objects) {
            messages.set(indexOfMessage, chatMessage);
            return true;
        }

        @Override
        protected void onPostExecute(Boolean success) {
            super.onPostExecute(success);
            if (weakReference != null) {
                if (weakReference.get() != null) {
                    weakReference.get().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            messagesAdapter.notifyItemChanged(indexOfMessage);
                        }
                    });
                }
            }
        }
    }

    static class BangMessageSoundTask extends AsyncTask<Void, Void, Boolean> {

        private WeakReference<Activity> activityWeakReference;
        private ChatMessage message;

        BangMessageSoundTask(Activity activity, ChatMessage message) {
            this.activityWeakReference = new WeakReference<>(activity);
            this.message = message;
        }

        @Override
        protected Boolean doInBackground(Void... objects) {
            if (activityWeakReference != null) {
                if (activityWeakReference.get() != null) {
                    UiUtils.bangSound(activityWeakReference.get(), R.raw.pop);
                }
                message.setReadSoundBanged(true);
                DbUtils.updateMessage(message);
            }
            return true;
        }

    }

}
