package com.wan.hollout.ui.activities;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.database.Cursor;
import android.media.MediaRecorder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Vibrator;
import android.provider.ContactsContract;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.afollestad.appthemeengine.ATE;
import com.afollestad.appthemeengine.customizers.ATEActivityThemeCustomizer;
import com.bumptech.glide.Glide;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.hyphenate.EMError;
import com.hyphenate.EMValueCallBack;
import com.hyphenate.chat.EMChatRoom;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMConversation;
import com.hyphenate.chat.EMMessage;
import com.hyphenate.exceptions.HyphenateException;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.SaveCallback;
import com.wan.hollout.R;
import com.wan.hollout.bean.HolloutFile;
import com.wan.hollout.call.VideoCallActivity;
import com.wan.hollout.call.VoiceCallActivity;
import com.wan.hollout.callbacks.DoneCallback;
import com.wan.hollout.chat.ChatUtils;
import com.wan.hollout.chat.HolloutCommunicationsManager;
import com.wan.hollout.emoji.EmojiDrawer;
import com.wan.hollout.eventbuses.MessageDeliveredEvent;
import com.wan.hollout.eventbuses.MessageReadEvent;
import com.wan.hollout.eventbuses.MessageReceivedEvent;
import com.wan.hollout.language.DynamicLanguage;
import com.wan.hollout.rendering.StickyRecyclerHeadersDecoration;
import com.wan.hollout.ui.adapters.MessagesAdapter;
import com.wan.hollout.ui.adapters.PickedMediaFilesAdapter;
import com.wan.hollout.ui.services.ContactService;
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
import com.wan.hollout.ui.widgets.Stub;
import com.wan.hollout.utils.AppConstants;
import com.wan.hollout.utils.AuthUtil;
import com.wan.hollout.utils.DbUtils;
import com.wan.hollout.utils.FilePathFinder;
import com.wan.hollout.utils.FileUtils;
import com.wan.hollout.utils.HolloutLogger;
import com.wan.hollout.utils.HolloutPermissions;
import com.wan.hollout.utils.HolloutPreferences;
import com.wan.hollout.utils.HolloutUtils;
import com.wan.hollout.utils.HolloutVCFParser;
import com.wan.hollout.utils.NotificationCenter;
import com.wan.hollout.utils.PermissionsUtils;
import com.wan.hollout.utils.SafeLayoutManager;
import com.wan.hollout.utils.UiUtils;
import com.wan.hollout.utils.VCFContactData;
import com.wan.hollout.utils.ViewUtil;

import net.alhazmy13.mediapicker.Image.ImagePicker;
import net.alhazmy13.mediapicker.Video.VideoPicker;

import org.apache.commons.lang3.StringUtils;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.wan.hollout.ui.widgets.AttachmentTypeSelector.ADD_CONTACT;
import static com.wan.hollout.ui.widgets.AttachmentTypeSelector.ADD_DOCUMENT;
import static com.wan.hollout.ui.widgets.AttachmentTypeSelector.ADD_GIF;
import static com.wan.hollout.ui.widgets.AttachmentTypeSelector.ADD_IMAGE;
import static com.wan.hollout.ui.widgets.AttachmentTypeSelector.ADD_LOCATION;
import static com.wan.hollout.ui.widgets.AttachmentTypeSelector.ADD_VIDEO;
import static com.wan.hollout.ui.widgets.AttachmentTypeSelector.OPEN_GALLERY;

/***
 * @author Wan Clem
 * ***/

@SuppressWarnings({"StatementWithEmptyBody", "FieldCanBeLocal", "unused"})
public class ChatActivity extends BaseActivity implements ATEActivityThemeCustomizer,
        KeyboardAwareLinearLayout.OnKeyboardShownListener,
        ActivityCompat.OnRequestPermissionsResultCallback, InputPanel.Listener, View.OnClickListener {

    private boolean isDarkTheme;

    private static final int REQUEST_CODE_PICK_FILE = 9;
    private static final int REQUEST_CODE_CONTACT_SHARE = 15;
    private static final int PLACE_LOCATION_PICKER_REQUEST_CODE = 20;
    protected static final int MESSAGE_TYPE_RECEIVED_CALL = 1;
    protected static final int MESSAGE_TYPE_SENT_CALL = 2;

    private AttachmentTypeSelector attachmentTypeSelector;
    private Stub<EmojiDrawer> emojiDrawerStub;

    @BindView(R.id.bottom_panel)
    InputPanel inputPanel;

    @BindView(R.id.layout_container)
    InputAwareLayout container;

    @BindView(R.id.embedded_text_editor)
    ComposeText composeText;

    @BindView(R.id.record_or_send_message_button)
    FloatingActionButton sendOrRecordAudioButton;

    @BindView(R.id.attach_button)
    ImageButton attachButton;

    @BindView(R.id.chat_tool_bar)
    ChatToolbar chatToolbar;

    @BindView(R.id.message_reply_view)
    LinearLayout messageReplyView;

    @BindView(R.id.reply_icon)
    RoundedImageView replyIconView;

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

    private LinearLayoutManager linearLayoutManager;
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

    private List<EMMessage> messages = new ArrayList<>();
    private MessagesAdapter messagesAdapter;

    private RecyclerView.LayoutManager messagesLayoutManager;
    protected EMConversation mConversation;
    private int chatType;

    /**
     * load 20 messages at one time
     */
    protected int pageSize = 20;
    protected boolean isLoading;
    protected boolean isFirstLoad = true;
    protected boolean haveMoreData = true;

    private Comparator<EMMessage>messageComparator = new Comparator<EMMessage>() {

        @Override
        public int compare(EMMessage o1, EMMessage o2) {
            if (o1!=null && o2!=null){
                return Long.valueOf(o2.getMsgTime()).compareTo(o1.getMsgTime());
            }
            return 0;
        }

    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        isDarkTheme = HolloutPreferences.getInstance().getBoolean("dark_theme", false);
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
        setContentView(R.layout.activity_chat);
        dynamicLanguage.onCreate(this);
        ButterKnife.bind(this);
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
        recipientProperties = intentExtras.getParcelable(AppConstants.USER_PROPERTIES);
        if (recipientProperties != null) {
            chatType = recipientProperties.getInt(AppConstants.CHAT_TYPE);
            chatToolbar.initView(recipientId, chatType);
            recipientId = recipientProperties.getString(AppConstants.REAL_OBJECT_ID);
            setupChatRecipient(recipientProperties);
            if (chatType == AppConstants.CHAT_TYPE_ROOM) {
                messagesEmptyView.setText(getString(R.string.cleaning_up_room));
                joinRoom();
            }
        }
        if (HolloutPreferences.getInstance().getBoolean("dark_theme", false)) {
            ATE.apply(this, "dark_theme");
        } else {
            ATE.apply(this, "light_theme");
        }
        initializeViews();
        setupAttachmentManager();
        setupMessagesAdapter();
        initConversation();
        tryOffloadLastMessage();
    }

    private void tryOffloadLastMessage() {
        String lastAttemptedMessageForRecipient = HolloutPreferences.getLastAttemptedMessage(recipientId);
        if (StringUtils.isNotEmpty(lastAttemptedMessageForRecipient)) {
            composeText.setText(lastAttemptedMessageForRecipient);
        } else {
            if (!isAContact()) {
                composeText.setText(getString(R.string.nice_to_meet_you));
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        checkAndRegEventBus();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        checkAndRegEventBus();
    }

    private void joinRoom() {
        EMClient.getInstance().chatroomManager().joinChatRoom(recipientId, new EMValueCallBack<EMChatRoom>() {

            @Override
            public void onSuccess(EMChatRoom value) {
                initConversation();
            }

            @Override
            public void onError(final int error, String errorMsg) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        switch (error) {
                            case EMError.CHATROOM_INVALID_ID:
                                UiUtils.showSafeToast("Sorry, we lost the key to this room. Please try again");
                                break;
                            case EMError.CHATROOM_PERMISSION_DENIED:
                                UiUtils.showSafeToast("Sorry, the admin would have to grant you permissions before you can join this room");
                                break;
                            case EMError.CHATROOM_MEMBERS_FULL:
                                UiUtils.showSafeToast("Sorry, the room is already congested");
                                break;
                        }
                        finish();
                    }
                });
            }
        });
    }

    /**
     * init conversation
     * If it is a chat room, Need to join the chat room after the success of initialization
     */
    private void initConversation() {
        //get the mConversation
        mConversation = EMClient.getInstance()
                .chatManager()
                .getConversation(recipientId, ChatUtils.getConversationType(chatType), true);
        mConversation.markAllMessagesAsRead();
        // the number of messages loaded into mConversation is getChatOptions().getNumberOfMessagesLoaded
        // you can change this number
        final List<EMMessage> msgs = new ArrayList<>(mConversation.getAllMessages());
        int msgCount = msgs.size();
        if (msgCount < mConversation.getAllMsgCount() && msgCount < pageSize) {
            String msgId = null;
            if (msgs.size() > 0) {
                msgId = msgs.get(0).getMsgId();
            }
            List<EMMessage> moreMessages = mConversation.loadMoreMsgFromDB(msgId, pageSize - msgCount);
            if (moreMessages!=null && !moreMessages.isEmpty()){
                msgs.addAll(moreMessages);
            }
        }
        if (!msgs.isEmpty()) {
            for (EMMessage emMessage : msgs) {
                if (!messages.contains(emMessage)) {
                    messages.add(0, emMessage);
                }
            }
            sortMessages();
            messagesAdapter.notifyDataSetChanged();
        }
        invalidateEmptyView();
    }

    private void sortMessages() {
        Collections.sort(messages,messageComparator);
    }

    private void setupMessagesAdapter() {
        messagesAdapter = new MessagesAdapter(this, messages);
        messagesLayoutManager = new LinearLayoutManager(ChatActivity.this, LinearLayoutManager.VERTICAL, true);
        messagesRecyclerView.setLayoutManager(messagesLayoutManager);
        StickyRecyclerHeadersDecoration stickyRecyclerHeadersDecoration = new StickyRecyclerHeadersDecoration(messagesAdapter);
        messagesRecyclerView.addItemDecoration(stickyRecyclerHeadersDecoration);
        messagesRecyclerView.setAdapter(messagesAdapter);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem placeCallMenuItem = menu.findItem(R.id.place_call);
        MenuItem viewProfileMenuItem = menu.findItem(R.id.view_profile_info);
        MenuItem blockUserMenuItem = menu.findItem(R.id.block_user);
        if (chatType == AppConstants.CHAT_TYPE_GROUP || chatType == AppConstants.CHAT_TYPE_ROOM) {
            placeCallMenuItem.setVisible(false);
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
        }
        supportInvalidateOptionsMenu();
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
        mediaRecorder.stop();
        mediaRecorder.reset();
        mediaRecorder.release();
        mediaRecorder = null;
        recordingInProgress = false;
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
        setIntent(intent);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.cancel_picked_single_media:
                if (!pickedMediaFiles.isEmpty()) {
                    hidePickedMedia();
                }
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
            File file = FileUtils.getFile(ChatActivity.this,uri);
            if (file.exists()) {
                previewSinglePickedFile(AppConstants.FILE_TYPE_PHOTO,file.getPath());
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
            case ADD_CONTACT:
                checkContactAccessAndOpenContact();
                break;
            case ADD_DOCUMENT:
                checkAccessToDocumentAndOpenDocuments();
                break;
            case ADD_LOCATION:
                checkLocationAccessPermissionsAndOpenLocation();
                break;
            case OPEN_GALLERY:
                checkAccessToGalleryAndOpen();
                break;
            case ADD_GIF:
                //TODO: Look for a way to add gif support here
                break;
        }
    }

    private void handleAddAttachment() {
        if (attachmentTypeSelector == null) {
            attachmentTypeSelector = new AttachmentTypeSelector(this, getSupportLoaderManager(), new AttachmentTypeListener());
        }
        attachmentTypeSelector.show(this, attachButton);
    }

    private void initializeViews() {

        emojiDrawerStub = ViewUtil.findStubById(this, R.id.emoji_drawer_stub);

        container.addOnKeyboardShownListener(this);
        inputPanel.setListener(this);

        attachmentTypeSelector = null;

        SendButtonListener sendButtonOnClickListener = new SendButtonListener();
        ComposeKeyPressedListener composeKeyPressedListener = new ComposeKeyPressedListener();

        composeText.setOnEditorActionListener(sendButtonOnClickListener);
        attachButton.setOnClickListener(new AttachButtonListener());
        sendOrRecordAudioButton.setOnClickListener(sendButtonOnClickListener);
        sendOrRecordAudioButton.setEnabled(true);

        composeText.setOnKeyListener(composeKeyPressedListener);
        composeText.addTextChangedListener(composeKeyPressedListener);
        composeText.setOnEditorActionListener(sendButtonOnClickListener);
        composeText.setOnClickListener(composeKeyPressedListener);
        composeText.setOnFocusChangeListener(composeKeyPressedListener);
        cancelPickedSingleMedia.setOnClickListener(this);
    }

    @Override
    public void onBackPressed() {
        if (container.isInputOpen()) container.hideCurrentInput(composeText);
        else {
            super.onBackPressed();
        }
    }

    private class ComposeKeyPressedListener implements View.OnKeyListener, View.OnClickListener, TextWatcher, View.OnFocusChangeListener {

        int beforeLength;

        @Override
        public boolean onKey(View v, int keyCode, KeyEvent event) {
            if (event.getAction() == KeyEvent.ACTION_DOWN) {
                if (keyCode == KeyEvent.KEYCODE_ENTER) {
                    sendOrRecordAudioButton.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER));
                    sendOrRecordAudioButton.dispatchKeyEvent(new KeyEvent(KeyEvent.ACTION_UP, KeyEvent.KEYCODE_ENTER));
                    return true;
                }
            }
            return false;
        }

        @Override
        public void onClick(View v) {
            container.showSoftkey(composeText);
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
        }

        @Override
        @SuppressWarnings("unchecked")
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            ArrayList<String> linksInMessage = UiUtils.pullLinks(s.toString());
            if (!linksInMessage.isEmpty()) {
                String firstUrl = linksInMessage.get(0);
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

    private void displayInactiveSendButton() {
        sendOrRecordAudioButton.setImageResource(R.drawable.send_inactive_icon);
        tintSendOrRecordAudioButton(R.color.grey_200);
    }

    private void displayActiveSendButton() {
        sendOrRecordAudioButton.setImageResource(R.drawable.ic_ami_send_24dp);
        tintSendOrRecordAudioButton(R.color.news_feed_indicator_icons);
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

    private void tintSendOrRecordAudioButton(int color) {
        sendOrRecordAudioButton.setBackgroundTintList(new ColorStateList(new int[][]{new int[]{0}}, new int[]{ContextCompat.getColor(this, color)}));
    }

    private class SendButtonListener implements View.OnClickListener, TextView.OnEditorActionListener {

        @Override
        public void onClick(View v) {
            if (inputPanel.canRecord()) {
                vibrator.vibrate(100);
                inputPanel.startRecorder();
            } else {
                UiUtils.bangSound(ChatActivity.this, R.raw.message_sent);
                if (recordingInProgress) {
                    inputPanel.stopRecording();
                    int seconds = (int) inputPanel.getStartTime().get();//Send voice note
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
                sendOrRecordAudioButton.performClick();
                return true;
            }
            return false;
        }
    }


    public void iterateThroughPickedMediaAndSendEach() {
        for (HolloutFile holloutFile : pickedMediaFiles) {
            switch (holloutFile.getFileType()) {
                case AppConstants.FILE_TYPE_PHOTO:
                    sendImageMessage(holloutFile.getLocalFilePath(), composeText.getText().toString().trim());
                    break;
                case AppConstants.FILE_TYPE_AUDIO:
                    //Send file message with file type audio
                    HashMap<String, String> moreMessageProps = new HashMap<>();
                    moreMessageProps.put(AppConstants.FILE_TYPE, AppConstants.FILE_TYPE_AUDIO);
                    moreMessageProps.put(AppConstants.AUDIO_DURATION, String.valueOf(HolloutUtils.getVideoDuration(holloutFile.getLocalFilePath())));
                    sendFileMessage(holloutFile.getLocalFilePath(), moreMessageProps);
                    break;
                case AppConstants.FILE_TYPE_VIDEO:
                    sendVideoMessage(holloutFile.getLocalFilePath(), holloutFile.getLocalFilePath(), (int) (new File(holloutFile.getLocalFilePath()).length() / 1000),
                            composeText.getText().toString().trim());
                    break;
            }
        }
        hidePickedMedia();
    }

    private class AttachButtonListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
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
    }

    public void snackInMessageReplyView(View view) {
        UiUtils.showView(view, true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.chat_options_menu, menu);
        ATE.applyMenu(this, getATEKey(), menu);
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
                            switch (which) {
                                case 0:
                                    intent.setClass(ChatActivity.this, VoiceCallActivity.class);
                                    intent.putExtra(AppConstants.EXTRA_USER_ID, recipientId);
                                    intent.putExtra(AppConstants.EXTRA_IS_INCOMING_CALL, false);
                                    startActivity(intent);
                                    break;
                                case 1:
                                    intent.setClass(ChatActivity.this, VideoCallActivity.class);
                                    intent.putExtra(AppConstants.EXTRA_USER_ID, recipientId);
                                    intent.putExtra(AppConstants.EXTRA_IS_INCOMING_CALL, false);
                                    startActivity(intent);
                                    break;
                            }
                        }
                    });
                    builder.create().show();
                }
                break;
            case R.id.view_shared_media:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void setupChatRecipient(ParseObject result) {
        if (StringUtils.isNotEmpty(recipientId)) {
            if (chatToolbar != null) {
                chatToolbar.refreshToolbar(result);
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
        if (!emojiDrawerStub.resolved()) {
            inputPanel.setEmojiDrawer(emojiDrawerStub.get());
            emojiDrawerStub.get().setEmojiEventListener(inputPanel);
        }
        if (container.getCurrentInput() == emojiDrawerStub.get()) {
            container.showSoftkey(composeText);
        } else {
            container.show(composeText, emojiDrawerStub.get());
        }
    }

    @Override
    public void onKeyboardShown() {
        inputPanel.onKeyboardShown();
    }

    private void checkAndRegEventBus() {
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
    }

    private void checkAnUnRegEventBus() {
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        checkAndRegEventBus();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (isFinishing()) overridePendingTransition(R.anim.fade_scale_in, R.anim.slide_to_right);
        inputPanel.onPause();
        checkAnUnRegEventBus();
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
        startActivityForResult(intent, REQUEST_CODE_PICK_FILE);
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
        } else if (requestCode == REQUEST_CODE_PICK_FILE && resultCode == RESULT_OK) {
            if (data != null) {
                Uri uri = data.getData();
                if (uri != null) {
                    sendFileByUri(uri);
                }
            }
        } else if (requestCode == REQUEST_CODE_CONTACT_SHARE && resultCode == RESULT_OK) {

            File vCradFile;

            try {

                vCradFile = new ContactService(ChatActivity.this).vCard(data.getData());

                if (vCradFile != null) {

                    HolloutFile contactFile = new HolloutFile();
                    contactFile.setLocalFilePath(vCradFile.getPath());
                    contactFile.setLocalFilePath(AppConstants.FILE_TYPE_CONTACT);

                    String filePath = FilePathFinder.getPath(ChatActivity.this, Uri.fromFile(vCradFile));

                    HolloutVCFParser parser = new HolloutVCFParser();
                    VCFContactData vcfContactData = parser.parseCVFContactData(filePath);

                    String contactName = vcfContactData.getName();
                    String contactPhoneNumber = vcfContactData.getTelephoneNumber();

                    String[] numberParts = contactPhoneNumber.split(",");

                    if (numberParts.length == 1) {
                        contactPhoneNumber = StringUtils.strip(numberParts[0], ",");
                    }

                    HashMap<String, String> contactProps = new HashMap<>();

                    contactProps.put(AppConstants.FILE_TYPE, AppConstants.FILE_TYPE_CONTACT);

                    if (StringUtils.isNotEmpty(contactName)) {
                        contactProps.put(AppConstants.CONTACT_NAME, contactName);
                    }

                    if (StringUtils.isNotEmpty(contactPhoneNumber)) {
                        contactProps.put(AppConstants.CONTACT_NUMBER, contactPhoneNumber);
                    }

                    sendFileMessage(filePath, contactProps);

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
        }

    }

    @SuppressLint("Recycle")
    protected void sendFileByUri(Uri uri) {
        String filePath = null;
        if ("content".equalsIgnoreCase(uri.getScheme())) {
            String[] filePathColumn = {MediaStore.Images.Media.DATA};
            Cursor cursor;

            try {
                cursor = this.getContentResolver().query(uri, filePathColumn, null, null, null);
                int column_index;
                if (cursor != null) {
                    column_index = cursor.getColumnIndexOrThrow("_data");
                    if (cursor.moveToFirst()) {
                        filePath = cursor.getString(column_index);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else if ("file".equalsIgnoreCase(uri.getScheme())) {
            filePath = uri.getPath();
        }
        HashMap<String, String> moreMessageProps = new HashMap<>();
        File file;
        if (filePath != null) {
            file = new File(filePath);
            if (!file.exists()) {
                UiUtils.showSafeToast(getString(R.string.File_does_not_exist));
            } else {
                //limit the size < 10M
                if (file.length() > 10 * 1024 * 1024) {
                    UiUtils.showSafeToast(getString(R.string.file_greater_than_max));
                } else {
                    String fileMime = FileUtils.getMimeType(filePath);
                    if (!HolloutUtils.isValidDocument(fileMime)) {
                        UiUtils.showSafeToast("Not a valid document");
                    } else {
                        moreMessageProps.put(AppConstants.FILE_TYPE, AppConstants.FILE_TYPE_DOCUMENT);
                        moreMessageProps.put(AppConstants.FILE_MIME_TYPE, fileMime);
                        sendFileMessage(filePath, moreMessageProps);
                    }
                }
            }
        } else {
            UiUtils.showSafeToast("Oops! error fetching file. Please ensure the file is in your sd card.");
        }
    }

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
                            break;
                        case AppConstants.HIDE_MESSAGE_REPLY_VIEW:
                            snackOutMessageReplyView(messageReplyView);
                            break;
                        case AppConstants.REFRESH_MESSAGES_ADAPTER:
                            try {
                                messagesAdapter.notifyDataSetChanged();
                            } catch (IllegalStateException ignored) {

                            }
                            break;
                        case AppConstants.SUSPEND_ALL_USE_OF_AUDIO_MANAGER:
                            break;
                    }
                } else if (o instanceof MessageReceivedEvent) {
                    MessageReceivedEvent messageReceivedEvent = (MessageReceivedEvent) o;
                    EMMessage emMessage = messageReceivedEvent.getMessage();
                    if (emMessage != null) {
                        messages.add(emMessage);
                        messagesAdapter.notifyDataSetChanged();
                    }
                } else if (o instanceof MessageDeliveredEvent) {
                    MessageDeliveredEvent messageDeliveredEvent = (MessageDeliveredEvent) o;
                    EMMessage emMessage = messageDeliveredEvent.getMessage();
                    int indexOfMessage = messages.indexOf(emMessage);
                    if (indexOfMessage != -1) {
                        messages.set(indexOfMessage, emMessage);
                        messagesAdapter.notifyDataSetChanged();
                    }
                } else if (o instanceof MessageReadEvent) {
                    MessageReadEvent messageReadEvent = (MessageReadEvent) o;
                    EMMessage emMessage = messageReadEvent.getMessage();
                    int indexOfMessage = messages.indexOf(emMessage);
                    if (indexOfMessage != -1) {
                        messages.set(indexOfMessage, emMessage);
                        messagesAdapter.notifyDataSetChanged();
                    }
                }
            }
        });
    }

    private void scrollToBottom() {

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
            onRecorderStarted();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        checkAnUnRegEventBus();
        sendChatStateMsg(getString(R.string.idle));
        HolloutPreferences.clearPreviousAttemptedMessage(recipientId);
        if (StringUtils.isNotEmpty(composeText.getText().toString().trim())) {
            HolloutPreferences.saveLastAttemptedMsg(recipientId, composeText.getText().toString().trim());
        }
    }

    @Override
    protected void onDestroy() {
        checkAnUnRegEventBus();
        super.onDestroy();
    }

    public String getMeetPointWithUser() {
        JSONObject signedInUserMeetPoints = signedInUser.getJSONObject(AppConstants.MEET_POINTS);
        if (signedInUserMeetPoints == null) {
            return DbUtils.getMeetPoint(recipientId);
        } else {
            String meetPoint = signedInUserMeetPoints.optString(AppConstants.MEET_POINT_WITH + recipientId);
            if (meetPoint != null) {
                return meetPoint;
            } else {
                return DbUtils.getMeetPoint(recipientId);
            }
        }
    }

    @NonNull
    private String getRecipient() {
        return recipientId.toLowerCase();
    }

    protected void sendTextMessage(String content) {
        // create a message
        EMMessage message = EMMessage.createTxtSendMessage(content, getRecipient());
        // send message
        sendMessage(message);
    }

    protected void sendVoiceMessage(String filePath, int length) {
        EMMessage message = EMMessage.createVoiceSendMessage(filePath, length, getRecipient());
        sendMessage(message);
    }

    @SuppressLint("CommitPrefEdits")
    protected void sendImageMessage(String imagePath, String caption) {
        String fileCaption;
        EMMessage message = EMMessage.createImageSendMessage(imagePath, false, getRecipient());
        if (StringUtils.isNotEmpty(caption)) {
            HolloutPreferences.setLastFileCaption(caption);
            fileCaption = caption;
        } else {
            fileCaption = HolloutPreferences.getLastFileCaption();
        }
        message.setAttribute(AppConstants.FILE_CAPTION, fileCaption);
        sendMessage(message);
    }

    protected void sendLocationMessage(double latitude, double longitude, String locationAddress) {
        EMMessage message = EMMessage.createLocationSendMessage(latitude, longitude, locationAddress, getRecipient());
        sendMessage(message);
    }

    @SuppressLint("CommitPrefEdits")
    protected void sendVideoMessage(String videoPath, String thumbPath, int videoLength, String caption) {
        EMMessage message = EMMessage.createVideoSendMessage(videoPath, thumbPath, videoLength, getRecipient());
        sendMessage(message);
    }

    protected void sendFileMessage(String filePath, HashMap<String, String> moreMessageProps) {
        EMMessage message = EMMessage.createFileSendMessage(filePath, getRecipient());
        if (moreMessageProps != null && !moreMessageProps.isEmpty()) {
            for (String key : moreMessageProps.keySet()) {
                message.setAttribute(key, moreMessageProps.get(key));
            }
        }
        sendMessage(message);
    }

    private void checkAndSendChatRequest() {
        if (signedInUser != null) {
            String signedInUserId = signedInUser.getString(AppConstants.REAL_OBJECT_ID);
            ParseQuery<ParseObject> pendingChatQuery = ParseQuery.getQuery(AppConstants.HOLLOUT_FEED);
            pendingChatQuery.whereEqualTo(AppConstants.FEED_CREATOR_ID, signedInUserId.toLowerCase());
            pendingChatQuery.whereEqualTo(AppConstants.FEED_TYPE, AppConstants.FEED_TYPE_CHAT_REQUEST);
            pendingChatQuery.whereEqualTo(AppConstants.FEED_RECIPIENT_ID, getRecipient());
            pendingChatQuery.getFirstInBackground(new GetCallback<ParseObject>() {
                @Override
                public void done(ParseObject object, ParseException e) {
                    if (object == null) {
                        sendNewChatRequest();
                    }
                }
            });
        }
    }

    private void updateSignedInUserChats() {
        List<String> chatIds = signedInUser.getList(AppConstants.APP_USER_CHATS);
        if (chatIds != null) {
            if (!chatIds.contains(getRecipient())) {
                chatIds.add(getRecipient());
                signedInUser.put(AppConstants.APP_USER_CHATS, chatIds);
                AuthUtil.updateCurrentLocalUser(signedInUser, new DoneCallback<Boolean>() {
                    @Override
                    public void done(Boolean result, Exception e) {

                    }
                });
            }
        } else {
            chatIds = new ArrayList<>();
            chatIds.add(getRecipient());
            signedInUser.put(AppConstants.APP_USER_CHATS, chatIds);
            AuthUtil.updateCurrentLocalUser(signedInUser, new DoneCallback<Boolean>() {
                @Override
                public void done(Boolean result, Exception e) {

                }
            });
        }
    }

    private void sendNewChatRequest() {
        if (signedInUser != null) {
            final String signedInUserId = signedInUser.getString(AppConstants.REAL_OBJECT_ID);
            ParseObject newChatRequestObject = new ParseObject(AppConstants.HOLLOUT_FEED);
            newChatRequestObject.put(AppConstants.FEED_CREATOR_ID, signedInUserId.toLowerCase());
            newChatRequestObject.put(AppConstants.FEED_RECIPIENT_ID, getRecipient());
            newChatRequestObject.put(AppConstants.FEED_TYPE, AppConstants.FEED_TYPE_CHAT_REQUEST);
            newChatRequestObject.put(AppConstants.FEED_CREATOR, signedInUser);
            newChatRequestObject.saveEventually(new SaveCallback() {
                @Override
                public void done(ParseException e) {
                    if (e == null) {
                        NotificationCenter.sendChatRequestNotification(signedInUserId, recipientId);
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
    protected void sendMessage(EMMessage newMessage) {
        String signedInUserDisplayName = signedInUser.getString(AppConstants.APP_USER_DISPLAY_NAME);
        String signedInUserPhotoUrl = signedInUser.getString(AppConstants.APP_USER_PROFILE_PHOTO_URL);
        newMessage.setAttribute(AppConstants.APP_USER_DISPLAY_NAME, signedInUserDisplayName);
        if (StringUtils.isNotEmpty(signedInUserPhotoUrl)) {
            newMessage.setAttribute(AppConstants.APP_USER_PROFILE_PHOTO_URL, signedInUserPhotoUrl);
        }
        EMClient.getInstance().chatManager().sendMessage(newMessage);
        //Send message here
        messages.add(0, newMessage);
        messagesAdapter.notifyDataSetChanged();
        invalidateEmptyView();
        messagesRecyclerView.smoothScrollToPosition(0);
        emptyComposeText();
        updateSignedInUserChats();
        HolloutPreferences.updateConversationTime(recipientId);
        if (!isAContact()) {
            HolloutCommunicationsManager.getInstance().execute(new Runnable() {
                @Override
                public void run() {
                    try {
                        EMClient.getInstance().contactManager().addContact(recipientId, "Hi, let's connect");
                        if (chatType == AppConstants.CHAT_TYPE_SINGLE) {
                            checkAndSendChatRequest();
                        }
                    } catch (HyphenateException e) {
                        e.printStackTrace();
                    }
                }
            });
        }
    }

    private boolean isAContact() {
        List<String> signedInUserChats = signedInUser.getList(AppConstants.APP_USER_CHATS);
        return (signedInUserChats != null && signedInUserChats.contains(recipientId));
    }

    private void emptyComposeText() {
        displayInactiveSendButton();
        composeText.setText("");
        composeText.setHint("Compose message");
    }

    @SuppressWarnings("unused")
    public void resendMessage(EMMessage message) {
        message.setStatus(EMMessage.Status.CREATE);
        EMClient.getInstance().chatManager().sendMessage(message);
        messagesAdapter.notifyDataSetChanged();
    }

    @Override
    public int getActivityTheme() {
        return isDarkTheme ? R.style.AppThemeNormalDark : R.style.AppThemeNormalLight;
    }

}
