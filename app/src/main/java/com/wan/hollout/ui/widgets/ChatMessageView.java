package com.wan.hollout.ui.widgets;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.AssetManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.provider.ContactsContract;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.text.Html;
import android.text.SpannableString;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.gif.GifDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.john.waveview.WaveView;
import com.tonyodev.fetch2.Download;
import com.tonyodev.fetch2.Error;
import com.tonyodev.fetch2.Fetch;
import com.tonyodev.fetch2.FetchListener;
import com.tonyodev.fetch2.Func;
import com.tonyodev.fetch2.NetworkType;
import com.tonyodev.fetch2.Priority;
import com.tonyodev.fetch2.Request;
import com.wan.hollout.R;
import com.wan.hollout.animations.KeyframesDrawable;
import com.wan.hollout.animations.KeyframesDrawableBuilder;
import com.wan.hollout.animations.deserializers.KFImageDeserializer;
import com.wan.hollout.animations.model.KFImage;
import com.wan.hollout.clients.ChatClient;
import com.wan.hollout.components.ApplicationLoader;
import com.wan.hollout.enums.MessageDirection;
import com.wan.hollout.enums.MessageStatus;
import com.wan.hollout.enums.MessageType;
import com.wan.hollout.eventbuses.FileUploadProgressEvent;
import com.wan.hollout.eventbuses.PlaceLocalCallEvent;
import com.wan.hollout.models.ChatMessage;
import com.wan.hollout.ui.activities.ChatActivity;
import com.wan.hollout.ui.activities.EaseShowImageActivity;
import com.wan.hollout.utils.AppConstants;
import com.wan.hollout.utils.DbUtils;
import com.wan.hollout.utils.FileUtils;
import com.wan.hollout.utils.HolloutLogger;
import com.wan.hollout.utils.HolloutPreferences;
import com.wan.hollout.utils.LocationUtils;
import com.wan.hollout.utils.UiUtils;

import org.apache.commons.lang3.StringUtils;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * @author Wan Clem
 */
@SuppressWarnings({"FieldCanBeLocal", "ConstantConditions"})
public class ChatMessageView extends RelativeLayout implements View.OnClickListener, View.OnLongClickListener {

    public static String TAG = "ChatMessageView";

    @BindView(R.id.message_container)
    View messageBubbleLayout;

    @Nullable
    @BindView(R.id.attached_photo_or_video_thumbnail)
    ImageView attachedPhotoOrVideoThumbnailView;

    @Nullable
    @BindView(R.id.play_media_if_video_icon)
    ImageView playMediaIfVideoIcon;

    @Nullable
    @BindView(R.id.file_size_or_duration)
    HolloutTextView fileSizeDurationView;

    @Nullable
    @BindView(R.id.contact_data_layout)
    LinearLayout contactLayout;

    @Nullable
    @BindView(R.id.contact_icon)
    CircleColorImageView contactIconView;

    @Nullable
    @BindView(R.id.contact_name)
    HolloutTextView contactNameView;

    @Nullable
    @BindView(R.id.contact_phone_numbers)
    TextView contactPhoneNumbersView;

    @Nullable
    @BindView(R.id.document_data_layout)
    LinearLayout documentDataLayout;

    @Nullable
    @BindView(R.id.document_icon)
    CircleColorImageView documentIconView;

    @Nullable
    @BindView(R.id.document_name_and_size)
    HolloutTextView documentNameAndSizeView;

    @Nullable
    @BindView(R.id.audio_view)
    AudioView audioView;

    @Nullable
    @BindView(R.id.upload_progress_wave_view)
    WaveView photoVideoProgressView;

    @Nullable
    @BindView(R.id.message_body)
    TextView messageBodyView;

    @Nullable
    @BindView(R.id.textview_time)
    TextView timeTextView;

    @Nullable
    @BindView(R.id.delivery_status_view)
    ImageView deliveryStatusView;

    @Nullable
    @BindView(R.id.link_preview)
    LinkPreview linkPreview;

    @Nullable
    @BindView(R.id.message_reply_recycler_item_view)
    MessageReplyRecyclerItemView messageReplyRecyclerItemView;

    @Nullable
    @BindView(R.id.content_view)
    ViewGroup contentView;

    @Nullable
    @BindView(R.id.missed_call_view)
    TextView missedCallView;

    private String searchString;
    private ChatMessage message;

    private Activity activity;

    protected Handler handler = new Handler(Looper.getMainLooper());

    private static KeyframesDrawable imageDrawable;
    private static InputStream stream;

    public ChatMessageView(Context context) {
        super(context);
    }

    public ChatMessageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ChatMessageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void bindData(Activity context, ChatMessage messageObject, String searchString) {
        this.activity = context;
        this.message = messageObject;
        this.searchString = searchString;
        setupMessageBubble();
        setupMessageBody(searchString);
        setupMessageTimeAndDeliveryStatus();
        refreshViews();
        setOnClickListener(this);
        setOnLongClickListener(this);
        messageBubbleLayout.setOnClickListener(this);
        messageBubbleLayout.setOnLongClickListener(this);
        HolloutLogger.d("MessageInAdapterProps", messageObject.toString());
        checkAndRegEventBus();
        UiUtils.showView(timeTextView, false);
        UiUtils.showView(deliveryStatusView, false);
    }

    private void setupMessageBubble() {
        if (message.getMessageDirection() == MessageDirection.OUTGOING) {
            if (message.getMessageType() != MessageType.CALL) {
                messageBubbleLayout.setBackground(ContextCompat.getDrawable(activity, R.drawable.outgoing_chat_bubble));
            }
        } else {
            messageBubbleLayout.setBackground(ContextCompat.getDrawable(activity, R.drawable.incoming_chat_bubble));
        }
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        ButterKnife.bind(this);
    }

    private MessageType getMessageType() {
        return message.getMessageType();
    }

    public MessageDirection getMessageDirection() {
        return message.getMessageDirection();
    }

    private void setupMessageBody(String searchString) {

        MessageType messageType = getMessageType();

        if (messageType == MessageType.TXT) {
            setupTxtMessage(searchString);
        }

        if (messageType == MessageType.GIF) {
            setUpGifMessage(message.getGifUrl());
        }

        if (messageType == MessageType.REACTION) {
            setupReactionMessage();
        }

        if (messageType == MessageType.IMAGE) {
            setupImageMessage(searchString);
        }

        if (messageType == MessageType.VIDEO) {
            setupVideoMessage(searchString);
        }

        if (messageType == MessageType.CONTACT) {
            setupContactMessage(searchString);
        }

        if (messageType == MessageType.DOCUMENT) {
            setupDocumentMessage(searchString);
        }

        if (messageType == MessageType.VOICE) {
            setupVoiceMessage(searchString);
        }

        if (messageType == MessageType.AUDIO) {
            setupAudioMessage(searchString);
        }

        if (messageType == MessageType.LOCATION) {
            setupLocationMessage(searchString);
        }

        if (messageType == MessageType.CALL) {
            setupMissedCallMessage(searchString);
        }

        setMessageStatusCallback(message.getFileUploadProgress());
        handleRepliedMessageIfAvailable();
        registerViewTreeObserver();
        handleCommonalities();
        refreshViews();
        if (attachedPhotoOrVideoThumbnailView != null) {
            attachedPhotoOrVideoThumbnailView.setOnClickListener(this);
            attachedPhotoOrVideoThumbnailView.setOnLongClickListener(this);
        }
        if (audioView != null) {
            audioView.setOnClickListener(this);
            audioView.setOnLongClickListener(this);
        }
    }

    private void setupMissedCallMessage(String searchString) {
        if (StringUtils.isNotEmpty(searchString)) {
            missedCallView.setText(UiUtils.highlightTextIfNecessary(searchString, message.getMessageBody(), ContextCompat.getColor(activity, R.color.colorAccent)));
        } else {
            missedCallView.setText(message.getMessageBody());
        }
    }

    //Setup message reply
    private void handleRepliedMessageIfAvailable() {
        String repliedMessageId = message.getRepliedMessageId();
        if (repliedMessageId != null) {
            ChatMessage repliedMessage = ChatClient.getInstance().getMessage(repliedMessageId);
            if (repliedMessage != null) {
                UiUtils.showView(messageReplyRecyclerItemView, true);
                AppConstants.repliedMessagePositions.put(getMessageHash(), true);
                messageReplyRecyclerItemView.bindMessageReply(activity, repliedMessage);
            } else {
                UiUtils.showView(messageReplyRecyclerItemView, false);
                AppConstants.repliedMessagePositions.put(getMessageHash(), false);
            }
        } else {
            UiUtils.showView(messageReplyRecyclerItemView, false);
            AppConstants.repliedMessagePositions.put(getMessageHash(), false);
        }
    }

    private void registerViewTreeObserver() {
        if (message.getRepliedMessageId() != null) {
            int viewWidth = HolloutPreferences.getViewWidth(message.getMessageId());
            if (viewWidth != 0) {
                checkAndRedrawSomeViews(viewWidth);
            } else {
                ViewTreeObserver viewTreeObserver = getViewTreeObserver();
                if (viewTreeObserver.isAlive()) {
                    viewTreeObserver.addOnPreDrawListener(new ViewTreeObserver.OnPreDrawListener() {
                        @Override
                        public boolean onPreDraw() {
                            getViewTreeObserver().removeOnPreDrawListener(this);
                            int messageBubbleWidth = messageBubbleLayout.getWidth();
                            checkAndRedrawSomeViews(messageBubbleWidth);
                            HolloutPreferences.saveViewWidth(message.getMessageId(), messageBubbleWidth);
                            return true;
                        }
                    });
                }
            }
        }
    }

    private void checkAndRedrawSomeViews(int messageBubbleWidth) {
        if (contentView != null && messageReplyRecyclerItemView != null && messageReplyRecyclerItemView.getVisibility() == VISIBLE) {
            HolloutLogger.d("ViewParams", "BubbleWidth for " + message.getMessageId() + " is = " + messageBubbleWidth);
            ViewGroup.LayoutParams layoutParams = contentView.getLayoutParams();
            layoutParams.width = messageBubbleWidth;
            contentView.setLayoutParams(layoutParams);

            ViewGroup.LayoutParams messageReplyViewLayoutParams =
                    messageReplyRecyclerItemView.getLayoutParams();
            messageReplyViewLayoutParams.width = messageBubbleWidth;
            messageReplyRecyclerItemView.setLayoutParams(messageReplyViewLayoutParams);
            messageReplyRecyclerItemView.reMeasureMessageReplyView(messageReplyViewLayoutParams);
        }
    }

    private void setupLocationMessage(String searchString) {
        String locationName = message.getLocationAddress();
        if (StringUtils.isNotEmpty(locationName)) {
            UiUtils.showView(messageBodyView, true);
            messageBodyView.setText(StringUtils.isNotEmpty(searchString)
                    ? UiUtils.highlightTextIfNecessary(searchString, locationName,
                    ContextCompat.getColor(activity, R.color.colorAccent)) : locationName);
            AppConstants.messageBodyPositions.put(getMessageHash(), true);
        } else {
            UiUtils.showView(messageBodyView, false);
            AppConstants.messageBodyPositions.put(getMessageHash(), false);
        }
        String locationStaticMap = LocationUtils.loadStaticMap(String.valueOf(message.getLatitude()),
                String.valueOf(message.getLongitude()));
        if (StringUtils.isNotEmpty(locationStaticMap)) {
            UiUtils.showView(fileSizeDurationView, false);
            AppConstants.fileSizeOrDurationPositions.put(getMessageHash(), false);
            UiUtils.loadImage(activity, locationStaticMap, attachedPhotoOrVideoThumbnailView);
        }
        AppConstants.wavePositions.put(getMessageHash(), false);
        UiUtils.showView(photoVideoProgressView, false);
    }

    private void setupVoiceMessage(String searchString) {
        String fileCaption = "Voice Note";
        File localFilePath = new File(message.getLocalUrl());
        String voiceContentDuration = message.getAudioDuration();
        if (localFilePath.exists()) {
            audioView.setAudio(message.getLocalUrl(), StringUtils.isNotEmpty(searchString) ? UiUtils.highlightTextIfNecessary(searchString, fileCaption, ContextCompat.getColor(activity, R.color.colorAccent)) : fileCaption, voiceContentDuration);
        } else {
            audioView.setAudio(message.getRemoteUrl(), StringUtils.isNotEmpty(searchString) ? UiUtils.highlightTextIfNecessary(searchString, fileCaption, ContextCompat.getColor(activity, R.color.colorAccent)) : fileCaption, voiceContentDuration);
        }
        audioView.checkAndDownload(message.getLocalUrl(), message.getRemoteUrl());
    }

    @SuppressLint("SetTextI18n")
    private void setupDocumentMessage(String searchString) {
        String documentName = message.getDocumentName();
        String documentSize = message.getDocumentSize();
        if (StringUtils.isNotEmpty(documentName)) {
            documentNameAndSizeView.setText(StringUtils.isNotEmpty(searchString) ? UiUtils.highlightTextIfNecessary(searchString, documentName, ContextCompat.getColor(activity, R.color.colorAccent)) : documentName + "\n" + documentSize);
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

    private void setupReactionMessage() {
        UiUtils.showView(fileSizeDurationView, false);
        UiUtils.showView(messageBodyView, false);
        AppConstants.fileSizeOrDurationPositions.put(getMessageHash(), false);
        AppConstants.messageBodyPositions.put(getMessageHash(), false);
        String reactionValue = message.getReactionValue();
        if (StringUtils.isNotEmpty(reactionValue)) {
            loadDrawables(activity, attachedPhotoOrVideoThumbnailView, reactionValue);
        }
    }

    private void setupAudioMessage(String searchString) {
        String audioDuration = message.getAudioDuration();
        String fileCaption = message.getFileCaption() != null ?
                message.getFileCaption() : activity.getString(R.string.audio);
        File localFilePath = new File(message.getLocalUrl());
        if (localFilePath.exists()) {
            //Local File exists
            audioView.setAudio(message.getLocalUrl(), StringUtils.isNotEmpty(searchString) ? UiUtils.highlightTextIfNecessary(searchString, fileCaption, ContextCompat.getColor(activity, R.color.colorAccent)) : fileCaption, audioDuration);
        } else {
            audioView.setAudio(message.getRemoteUrl(), StringUtils.isNotEmpty(searchString) ? UiUtils.highlightTextIfNecessary(searchString, fileCaption, ContextCompat.getColor(activity, R.color.colorAccent)) : fileCaption, audioDuration);
        }
    }

    private void setupContactMessage(String searchString) {
        String contactName = message.getContactName();
        String contactPhoneNumber = message.getContactNumber();
        contactNameView.setText(StringUtils.isNotEmpty(searchString) ? UiUtils.highlightTextIfNecessary(searchString, contactName, ContextCompat.getColor(activity, R.color.colorAccent)) : contactName);
        String purifiedPhoneNumber = StringUtils.stripEnd(contactPhoneNumber, ",");
        if (contactPhoneNumbersView != null) {
            if (getMessageDirection() == MessageDirection.OUTGOING) {
                contactPhoneNumbersView.setText(UiUtils.fromHtml(Html.toHtml(StringUtils.isNotEmpty(searchString) ? UiUtils.highlightTextIfNecessary(searchString, purifiedPhoneNumber, ContextCompat.getColor(activity, R.color.colorAccent))
                        : new SpannableString(purifiedPhoneNumber))));
            } else {
                contactPhoneNumbersView.setText(UiUtils.fromHtml(Html.toHtml(StringUtils.isNotEmpty(searchString) ? UiUtils.highlightTextIfNecessary(searchString, purifiedPhoneNumber, ContextCompat.getColor(activity, R.color.colorAccent))
                        : new SpannableString(purifiedPhoneNumber))));
            }
        }
    }

    private void handleCommonalities() {
        handleMessageReadOnClickOfImageView();
    }

    public int getMessageHash() {
        return message.getMessageId().hashCode();
    }

    private void setupImageMessage(String searchString) {
        String filePath = message.getLocalUrl();
        File file = new File(filePath);
        if (file.exists()) {
            filePath = message.getLocalUrl();
        } else {
            filePath = message.getRemoteUrl();
            if (StringUtils.isNotEmpty(message.getRemoteUrl())) {
                UiUtils.showView(photoVideoProgressView, false);
            }
        }
        UiUtils.loadImage(activity, filePath, attachedPhotoOrVideoThumbnailView);
        UiUtils.showView(fileSizeDurationView, false);
        AppConstants.fileSizeOrDurationPositions.put(getMessageHash(), false);
        String fileCaption = message.getFileCaption();
        if (StringUtils.isNotEmpty(fileCaption)) {
            UiUtils.showView(messageBodyView, true);
            messageBodyView.setText(StringUtils.isNotEmpty(searchString) ? UiUtils.highlightTextIfNecessary(searchString, fileCaption, ContextCompat.getColor(activity, R.color.colorAccent)) : fileCaption);
            AppConstants.messageBodyPositions.put(getMessageHash(), true);
        } else {
            UiUtils.showView(messageBodyView, false);
            AppConstants.messageBodyPositions.put(getMessageHash(), false);
        }
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

    public void setupVideoMessage(String searchString) {
        String remoteVideoThumbnailUrl = message.getThumbnailUrl();
        File localThumbFile = new File(message.getLocalThumb());
        if (StringUtils.isNotEmpty(remoteVideoThumbnailUrl)) {
            HolloutLogger.d("VideoThumbnailPath", "Remote Video Thumb exists with value = " + remoteVideoThumbnailUrl);
            UiUtils.showView(photoVideoProgressView, false);
            UiUtils.loadImage(activity, message.getThumbnailUrl(), attachedPhotoOrVideoThumbnailView);
        } else {
            if (localThumbFile.exists()) {
                HolloutLogger.d("VideoThumbnailPath", "Local Video Thumb exists with value = " + localThumbFile);
                loadVideoFromPath(attachedPhotoOrVideoThumbnailView, message.getLocalThumb());
            }
        }
        UiUtils.showView(fileSizeDurationView, true);
        AppConstants.fileSizeOrDurationPositions.put(getMessageHash(), true);
        long videoLength = message.getVideoDuration();
        fileSizeDurationView.setText(UiUtils.getTimeString(videoLength));
        UiUtils.showView(playMediaIfVideoIcon, true);
        AppConstants.playableVideoPositions.put(getMessageHash(), true);
        String fileCaption = message.getFileCaption();
        if (fileCaption != null && StringUtils.isNotEmpty(fileCaption) && !StringUtils.containsIgnoreCase(fileCaption, activity.getString(R.string.photo))) {
            UiUtils.showView(messageBodyView, true);
            messageBodyView.setText(StringUtils.isNotEmpty(searchString) ? UiUtils.highlightTextIfNecessary(searchString, fileCaption, ContextCompat.getColor(activity, R.color.colorAccent)) : fileCaption);
            AppConstants.messageBodyPositions.put(getMessageHash(), true);
        } else {
            UiUtils.showView(messageBodyView, false);
            AppConstants.messageBodyPositions.put(getMessageHash(), false);
            messageBodyView.setText(activity.getString(R.string.video));
        }

        playMediaIfVideoIcon.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                UiUtils.blinkView(v);
                Intent mViewVideoIntent = new Intent(Intent.ACTION_VIEW);
                mViewVideoIntent.setDataAndType(Uri.parse(new File(message.getLocalUrl()).exists() ? message.getLocalUrl() : message.getRemoteUrl()), "video/*");
                activity.startActivity(mViewVideoIntent);
            }

        });

        attachedPhotoOrVideoThumbnailView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                playMediaIfVideoIcon.performClick();
            }
        });

    }

    public void loadVideoFromPath(ImageView videoView, String videoPath) {
        if (videoView != null) {
            if (Build.VERSION.SDK_INT >= 17) {
                if (!activity.isDestroyed()) {
                    Glide.with(activity).load(videoPath).error(R.drawable.ex_completed_ic_video).placeholder(R.drawable.ex_completed_ic_video).crossFade().into(videoView);
                }
            } else {
                Glide.with(activity).load(videoPath).error(R.drawable.ex_completed_ic_video).placeholder(R.drawable.ex_completed_ic_video).crossFade().into(videoView);
            }
        }
    }

    private void setupTxtMessage(String searchString) {
        setupMessageBodyOnlyMessage(searchString);
    }

    private void setUpGifMessage(String gifUrl) {
        UiUtils.showView(fileSizeDurationView, false);
        UiUtils.showView(messageBodyView, false);
        AppConstants.fileSizeOrDurationPositions.put(getMessageHash(), false);
        AppConstants.messageBodyPositions.put(getMessageHash(), false);
        if (StringUtils.isNotEmpty(gifUrl)) {
            if (Build.VERSION.SDK_INT >= 17) {
                if (!activity.isDestroyed()) {
                    if (StringUtils.isNotEmpty(gifUrl)) {
                        getLoadingGifImageCast().startLoading();
                        Glide.with(activity).load(gifUrl).asGif().listener(new RequestListener<String, GifDrawable>() {

                            @Override
                            public boolean onException(Exception e, String model, Target<GifDrawable> target, boolean isFirstResource) {
                                return false;
                            }

                            @Override
                            public boolean onResourceReady(GifDrawable resource, String model, Target<GifDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                                getLoadingGifImageCast().stopLoading();
                                return false;

                            }
                        }).into(attachedPhotoOrVideoThumbnailView);
                    }
                }
            } else {
                if (StringUtils.isNotEmpty(gifUrl)) {
                    getLoadingGifImageCast().startLoading();
                    Glide.with(activity).load(gifUrl).asGif().listener(new RequestListener<String, GifDrawable>() {

                        @Override
                        public boolean onException(Exception e, String model, Target<GifDrawable> target, boolean isFirstResource) {
                            return false;
                        }

                        @Override
                        public boolean onResourceReady(GifDrawable resource, String model, Target<GifDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {
                            getLoadingGifImageCast().stopLoading();
                            return false;
                        }
                    }).into(attachedPhotoOrVideoThumbnailView);
                }
            }
        }
    }

    private LoadingImageView getLoadingGifImageCast() {
        return (LoadingImageView) attachedPhotoOrVideoThumbnailView;
    }

    private void setupMessageBodyOnlyMessage(String searchString) {
        String messageBody = message.getMessageBody();
        if (StringUtils.isNotEmpty(messageBody)) {
            UiUtils.showView(messageBodyView, true);
            ArrayList includedLinks = UiUtils.pullLinks(messageBody);
            AppConstants.messageBodyPositions.put(getMessageHash(), true);
            if (includedLinks != null && !includedLinks.isEmpty()) {
                setupLinkPreviewMessage(includedLinks);
                AppConstants.linkPreviewPositions.put(getMessageHash(), true);
                UiUtils.showView(linkPreview, true);
            } else {
                AppConstants.linkPreviewPositions.put(getMessageHash(), false);
                UiUtils.showView(linkPreview, false);
            }
            if (messageBodyView != null) {
                if (getMessageDirection() == MessageDirection.OUTGOING) {
                    messageBodyView.setText(StringUtils.isNotEmpty(searchString)
                            ? UiUtils.fromHtml(Html.toHtml(UiUtils.highlightTextIfNecessary(searchString, messageBody, ContextCompat.getColor(activity, R.color.colorAccent)))) :
                            UiUtils.fromHtml(messageBody));
                } else {
                    messageBodyView.setText(StringUtils.isNotEmpty(searchString)
                            ? UiUtils.fromHtml(Html.toHtml(UiUtils.highlightTextIfNecessary(searchString, messageBody, ContextCompat.getColor(activity, R.color.colorAccent)))) :
                            UiUtils.fromHtml(messageBody));
                }
            }
        } else {
            UiUtils.showView(messageBodyView, false);
            AppConstants.messageBodyPositions.put(getMessageHash(), false);
        }
    }

    private void setupLinkPreviewMessage(List includedLinks) {
        try {
            String firstLink = (String) includedLinks.get(includedLinks.size() - 1);
            linkPreview.setData(firstLink);
        } catch (IndexOutOfBoundsException e) {
            HolloutLogger.d(TAG, "IndexOutOfBoundsException with error message = " + e.getMessage());
        }
    }

    private void handleMessageReadOnClickOfImageView() {
        if (attachedPhotoOrVideoThumbnailView != null) {
            attachedPhotoOrVideoThumbnailView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {

                }
            });
        }
    }

    private void setupMessageTimeAndDeliveryStatus() {
        Date messageDate = new Date(message.getTimeStamp());
        String messageTime = AppConstants.DATE_FORMATTER_IN_12HRS.format(messageDate);
        if (timeTextView != null) {
            timeTextView.setText(messageTime);
            timeTextView.setTextColor(ContextCompat.getColor(activity, R.color.grey_500));
        }
        if (getMessageDirection() == MessageDirection.OUTGOING && deliveryStatusView != null) {
            if (message.getMessageStatus() == MessageStatus.READ) {
                deliveryStatusView.setImageResource(R.drawable.msg_status_client_read);
            } else if (message.getMessageStatus() == MessageStatus.DELIVERED) {
                deliveryStatusView.setImageResource(R.drawable.msg_status_client_received);
            } else {
                deliveryStatusView.setImageResource(R.drawable.msg_status_server_receive);
            }
        }
    }

    private void refreshViews() {
        UiUtils.showView(fileSizeDurationView, AppConstants.fileSizeOrDurationPositions.get(getMessageHash()));
        UiUtils.showView(photoVideoProgressView, AppConstants.wavePositions.get(getMessageHash()));
        UiUtils.showView(messageBodyView, AppConstants.messageBodyPositions.get(getMessageHash()));
        UiUtils.showView(playMediaIfVideoIcon, AppConstants.playableVideoPositions.get(getMessageHash()));
        UiUtils.showView(linkPreview, AppConstants.linkPreviewPositions.get(getMessageHash()));
        UiUtils.showView(messageReplyRecyclerItemView, AppConstants.repliedMessagePositions.get(getMessageHash()));
        invalidateMessageBubble();
    }

    private void invalidateMessageBubble() {
        if (AppConstants.selectedMessagesPositions.get(getMessageHash())) {
            setBackgroundColor(ContextCompat.getColor(activity, R.color.lighter_blue));
            HolloutLogger.d("SelectionTag", "Selected MessageId = " + message.getMessageId());
        } else {
            setBackgroundColor(Color.TRANSPARENT);
            HolloutLogger.d("SelectionTag", "UnSelected MessageId = " + message.getMessageId());
        }
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        checkAndRegEventBus();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        checkAnUnRegEventBus();
        if (audioView != null) {
            audioView.cleanup();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.message_container:
            case R.id.conversation_message_view:
            case R.id.attached_photo_or_video_thumbnail:
            case R.id.audio_view:
                if (getChatActivity().getChatToolbar().isActionModeActivated()) {
                    updateActionMode();
                } else {
                    UiUtils.blinkView(v);
                    if (timeTextView != null) {
                        UiUtils.showView(timeTextView, timeTextView.getVisibility() == GONE);
                    }
                    if (deliveryStatusView != null) {
                        UiUtils.showView(deliveryStatusView, deliveryStatusView.getVisibility() == GONE);
                    }
                    if (message.getMessageType() == MessageType.CONTACT) {
                        String contactPhoneNumber = message.getContactNumber();
                        String contactName = message.getContactName();
                        placeCallOrAddToContacts(contactName, contactPhoneNumber);
                    } else if (message.getMessageType() == MessageType.DOCUMENT) {
                        String documentRemoteFilePath = message.getRemoteUrl();
                        String documentLocalFilePath = message.getLocalUrl();
                        File file = new File(documentLocalFilePath);
                        if (file != null && file.exists()) {
                            FileUtils.openFile(file, activity);
                        } else {
                            downloadFile(documentRemoteFilePath, documentLocalFilePath, file);
                        }
                    } else if (message.getMessageType() == MessageType.IMAGE) {
                        String filePath = message.getLocalUrl();
                        File file = new File(filePath);
                        if (file.exists()) {
                            filePath = message.getLocalUrl();
                            openImage(filePath);
                        } else {
                            filePath = message.getRemoteUrl();
                            openImage(filePath);
                        }
                    } else if (message.getMessageType() == MessageType.LOCATION) {
                        String lat = message.getLatitude();
                        String lng = message.getLongitude();
                        Uri gmmIntentUri = Uri.parse("geo:" + LocationUtils.getLocationFromMessage(String.valueOf(lat), String.valueOf(lng)));
                        Intent mapIntent = new Intent(Intent.ACTION_VIEW, gmmIntentUri);
                        mapIntent.setPackage("com.google.android.apps.maps");
                        if (mapIntent.resolveActivity(activity.getPackageManager()) != null) {
                            activity.startActivity(mapIntent);
                        } else {
                            UiUtils.showSafeToast("Failed to find an application that can open this");
                        }
                    } else if (message.getMessageType() == MessageType.TXT) {
                        ArrayList<String> includedLinks = UiUtils.pullLinks(message.getMessageBody());
                        if (!includedLinks.isEmpty()) {
                            String firstUrl = includedLinks.get(0);
                            if (!firstUrl.startsWith("http://") && !firstUrl.startsWith("https://")) {
                                firstUrl = "http://" + firstUrl;
                            }
                            Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(firstUrl));
                            activity.startActivity(browserIntent);
                        }
                    }
                }
                break;
        }

    }

    private void downloadFile(String documentRemoteFilePath, String documentLocalFilePath, final File file) {
        UiUtils.showSafeToast("Downloading File. File Would open after downloading...");
        Request request = new Request(documentRemoteFilePath, documentLocalFilePath);
        request.setPriority(Priority.HIGH);
        request.setNetworkType(NetworkType.ALL);
        Fetch fetch = ApplicationLoader.getInstance().getMainFetch();
        fetch.enqueue(request, new Func<Download>() {
            @Override
            public void call(Download download) {

            }
        }, new Func<Error>() {
            @Override
            public void call(Error error) {
                activity.runOnUiThread(new Runnable() {
                    @SuppressWarnings("ResultOfMethodCallIgnored")
                    public void run() {
                        if (file != null && file.exists() && file.isFile())
                            file.delete();
                        String str4 = getResources().getString(R.string.failed_to_download_file);
                        UiUtils.showSafeToast(str4);
                    }
                });
            }
        });
        fetch.addListener(new FetchListener() {
            @Override
            public void onQueued(Download download) {

            }

            @Override
            public void onCompleted(Download download) {
                activity.runOnUiThread(new Runnable() {
                    public void run() {
                        FileUtils.openFile(file, activity);
                    }
                });
            }

            @Override
            public void onError(Download download) {

            }

            @Override
            public void onProgress(Download download, long l, long l1) {

            }

            @Override
            public void onPaused(Download download) {

            }

            @Override
            public void onResumed(Download download) {

            }

            @Override
            public void onCancelled(Download download) {

            }

            @Override
            public void onRemoved(Download download) {

            }

            @Override
            public void onDeleted(Download download) {

            }
        });
    }

    private void openImage(String filePath) {
        Intent imageIntent = new Intent(activity, EaseShowImageActivity.class);
        imageIntent.putExtra(AppConstants.FILE_PATH, filePath);
        activity.startActivity(imageIntent);
    }

    private void placeCallOrAddToContacts(final String contactName, final String contactPhoneNumber) {
        final AlertDialog.Builder contactOptions = new AlertDialog.Builder(activity);
        contactOptions.setItems(new CharSequence[]{"Call", "Add to contacts"}, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                switch (which) {
                    case 0:
                        String purifiedPhoneNumber = StringUtils.stripEnd(contactPhoneNumber, ",");
                        final String[] numbers = purifiedPhoneNumber.split(",");
                        if (numbers.length > 1) {
                            AlertDialog.Builder numbersToCall = new AlertDialog.Builder(activity);
                            numbersToCall.setTitle("Select Number to Call");
                            numbersToCall.setItems(numbers, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                    EventBus.getDefault().post(new PlaceLocalCallEvent(numbers[which]));
                                }
                            });
                            numbersToCall.create().show();
                        } else {
                            EventBus.getDefault().post(new PlaceLocalCallEvent(purifiedPhoneNumber));
                        }
                        break;
                    case 1:
                        addAsContactConfirmed(activity, contactName, contactPhoneNumber.split(","));
                        break;
                }
            }
        });
        contactOptions.create().show();
    }

    public static void addAsContactConfirmed(final Context context, String name, String[] phones) {
        Intent intent = new Intent(Intent.ACTION_INSERT);
        intent.setType(ContactsContract.Contacts.CONTENT_TYPE);
        intent.putExtra(ContactsContract.Intents.Insert.NAME, name);
        intent.putExtra(ContactsContract.Intents.Insert.PHONE, phones[0]);
        try {
            String secondaryPhone = phones[1];
            String tertiaryPhone = phones[2];
            if (secondaryPhone != null) {
                intent.putExtra(ContactsContract.Intents.Insert.SECONDARY_PHONE, secondaryPhone);
            }
            if (tertiaryPhone != null) {
                intent.putExtra(ContactsContract.Intents.Insert.TERTIARY_PHONE, tertiaryPhone);
            }
        } catch (ArrayIndexOutOfBoundsException ignored) {

        }
        context.startActivity(intent);
    }

    public ChatActivity getChatActivity() {
        return (ChatActivity) activity;
    }

    @Override
    public boolean onLongClick(View view) {
        if (getChatActivity() != null) {
            getChatActivity().vibrateVibrator();
            updateActionMode();
        }
        return true;
    }

    private void updateActionMode() {
        addToOrRemoveFromSelectedMessages();
        getChatActivity().getChatToolbar().updateActionMode(AppConstants.selectedMessages.size());
    }

    private void addToOrRemoveFromSelectedMessages() {
        if (!AppConstants.selectedMessages.contains(message)) {
            AppConstants.selectedMessages.add(message);
            AppConstants.selectedMessagesPositions.put(getMessageHash(), true);
        } else {
            AppConstants.selectedMessages.remove(message);
            AppConstants.selectedMessagesPositions.put(getMessageHash(), false);
        }
        invalidateMessageBubble();
    }

    /**
     * set callback for  message
     */
    public void setMessageStatusCallback(final double progress) {
        if (message.getMessageType() != MessageType.TXT && photoVideoProgressView != null) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    if (progress >= 99) {
                        message.setFileUploadProgress(progress);
                        photoVideoProgressView.setProgress(100);
                        UiUtils.showView(photoVideoProgressView, false);
                        AppConstants.wavePositions.put(getMessageHash(), false);
                        refreshViews();
                        return;
                    }
                    UiUtils.showView(photoVideoProgressView, true);
                    photoVideoProgressView.setProgress((int) progress);
                    message.setFileUploadProgress(progress);
                }
            });
        } else {
            AppConstants.wavePositions.put(getMessageHash(), false);
            UiUtils.showView(photoVideoProgressView, false);
        }
    }

    @Override
    public void invalidate() {
        super.invalidate();
        ChatMessage newMessage = DbUtils.getMessage(message.getMessageId());
        if (newMessage != null) {
            this.message = newMessage;
            bindData(activity, newMessage, searchString);
        }
    }

    @SuppressWarnings("unused")
    @Subscribe(sticky = true, threadMode = ThreadMode.ASYNC)
    public void onEventAsync(final Object o) {
        UiUtils.runOnMain(new Runnable() {
            @Override
            public void run() {
                if (o instanceof FileUploadProgressEvent) {
                    FileUploadProgressEvent fileUploadProgressEvent = (FileUploadProgressEvent) o;
                    String uploadFileUrl = fileUploadProgressEvent.getFilePath();
                    double progress = fileUploadProgressEvent.getProgress();
                    String uniqueIdOfProcessInitiator = fileUploadProgressEvent.getPollableUniqueId();
                    if (uniqueIdOfProcessInitiator.equals(message.getMessageId()) && !fileUploadProgressEvent.isFromThumbnail()) {
                        message.setFileUploadProgress(progress);
                        setMessageStatusCallback(progress);
                    }
                }
            }
        });
    }

}
