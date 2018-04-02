package com.wan.hollout.ui.widgets;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.gif.GifDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.parse.ParseObject;
import com.wan.hollout.R;
import com.wan.hollout.animations.KeyframesDrawable;
import com.wan.hollout.animations.KeyframesDrawableBuilder;
import com.wan.hollout.animations.deserializers.KFImageDeserializer;
import com.wan.hollout.animations.model.KFImage;
import com.wan.hollout.enums.MessageType;
import com.wan.hollout.eventbuses.ScrollToMessageEvent;
import com.wan.hollout.models.ChatMessage;
import com.wan.hollout.utils.AppConstants;
import com.wan.hollout.utils.AuthUtil;
import com.wan.hollout.utils.HolloutLogger;
import com.wan.hollout.utils.LocationUtils;
import com.wan.hollout.utils.UiUtils;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.WordUtils;
import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author Wan Clem
 */

@SuppressWarnings({"ConstantConditions", "RedundantCast", "unused"})
@SuppressLint("SetTextI18n")
public class MessageReplyRecyclerItemView extends RelativeLayout implements View.OnClickListener {

    private static final String TAG = "MessageReplyRecyclerItemView";
    private HolloutTextView replyTitleView;
    private ChatMessageTextView replySubTitleView;
    private FrameLayout replyAttachmentView;
    private ImageView replyIconView;
    private ImageView playReplyIconView;
    private LinearLayout messageReplyView;
    private ChatMessage repliedMessage;

    private Activity activity;

    public MessageReplyRecyclerItemView(Context context) {
        this(context, null);
    }

    public MessageReplyRecyclerItemView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public MessageReplyRecyclerItemView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        inflate(context, R.layout.message_reply_recycler_view_item, this);
        initView();
    }

    private void initView() {
        replyTitleView = (HolloutTextView) findViewById(R.id.reply_title);
        replySubTitleView = (ChatMessageTextView) findViewById(R.id.reply_message_body);
        replyAttachmentView = (FrameLayout) findViewById(R.id.reply_attachment_view);
        replyIconView = (ImageView) findViewById(R.id.reply_icon);
        playReplyIconView = (ImageView) findViewById(R.id.play_reply_msg_if_video);
        RelativeLayout contentView = (RelativeLayout) findViewById(R.id.content_view);
        messageReplyView = (LinearLayout) findViewById(R.id.message_reply_view);
        contentView.setOnClickListener(this);
    }

    public int getMessageHash() {
        return repliedMessage.getMessageId().hashCode();
    }

    public void bindMessageReply(Activity activity, ChatMessage repliedMessage) {
        this.activity = activity;
        this.repliedMessage = repliedMessage;
        setupMessageBody();
    }

    public void reMeasureMessageReplyView(ViewGroup.LayoutParams layoutParams) {
        ViewGroup.LayoutParams params = messageReplyView.getLayoutParams();
        params.width = layoutParams.width;
        if (messageReplyView.getVisibility() == VISIBLE) {
            messageReplyView.setLayoutParams(params);
        }
    }

    private MessageType getMessageType() {
        return repliedMessage.getMessageType();
    }

    private void setupMessageBody() {
        ParseObject signedInUser = AuthUtil.getCurrentUser();
        MessageType messageType = getMessageType();
        String senderNameOfMessage = repliedMessage.getFromName();
        if (signedInUser != null) {
            String signedInUserId = signedInUser.getString(AppConstants.REAL_OBJECT_ID);
            if (signedInUserId.equals(repliedMessage.getFrom())) {
                replyTitleView.setText(WordUtils.capitalize(activity.getString(R.string.you)));
            } else {
                replyTitleView.setText(WordUtils.capitalize(senderNameOfMessage));
            }
            if (messageType == MessageType.TXT) {
                setupTxtMessage(repliedMessage.getMessageBody());
            }
            if (messageType == MessageType.REACTION) {
                setupReactionMessage(repliedMessage.getReactionValue());
            }
            if (messageType == MessageType.GIF) {
                setUpGifMessage(repliedMessage.getGifUrl());
            }
            if (messageType == MessageType.IMAGE) {
                setupImageMessage(repliedMessage);
            }
            if (messageType == MessageType.VIDEO) {
                setupVideoMessage(repliedMessage);
            }
            if (messageType == MessageType.AUDIO) {
                setupAudioMessage(repliedMessage);
            }
            if (messageType == MessageType.VOICE) {
                setupVoiceMessage(repliedMessage);
            }
            if (messageType == MessageType.DOCUMENT) {
                setupDocumentMessage(repliedMessage);
            }
            if (messageType == MessageType.LOCATION) {
                setupLocationMessage(repliedMessage);
            }
            if (messageType == MessageType.CONTACT) {
                setupContactMessage(repliedMessage);
            }
            refreshViewComponents();
        }

    }

    private void setupLocationMessage(ChatMessage message) {
        String locationName = message.getLocationAddress();
        replySubTitleView.setText(activity.getString(R.string.location));
        if (locationName != null) {
            replySubTitleView.setText(locationName);
        }
        String locationStaticMap = LocationUtils.loadStaticMap(message.getLatitude(),
                message.getLongitude());
        if (StringUtils.isNotEmpty(locationStaticMap)) {
            UiUtils.showView(replyAttachmentView, true);
            UiUtils.showView(playReplyIconView, false);
            AppConstants.messageReplyAttachmentPositions.put(getMessageHash(), true);
            UiUtils.loadImage(activity, locationStaticMap, replyIconView);
        }
    }

    @SuppressLint("SetTextI18n")
    private void setupVoiceMessage(ChatMessage chatMessage) {
        String audioDuration = chatMessage.getAudioDuration();
        replySubTitleView.setText(activity.getString(R.string.voice_note) + " - " + audioDuration);
    }

    private void setupImageMessage(ChatMessage chatMessage) {
        UiUtils.showView(replyAttachmentView, true);
        AppConstants.messageReplyAttachmentPositions.put(getMessageHash(), true);
        String filePath = chatMessage.getLocalUrl();
        File file = new File(filePath);
        if (file.exists()) {
            filePath = chatMessage.getLocalUrl();
        } else {
            filePath = chatMessage.getRemoteUrl();
        }
        UiUtils.loadImage(activity, filePath, replyIconView);
        String fileCaption = repliedMessage.getFileCaption();
        if (StringUtils.isNotEmpty(fileCaption)) {
            replySubTitleView.setText(fileCaption);
        } else {
            replySubTitleView.setText(activity.getString(R.string.photo));
        }
    }

    private void setupDocumentMessage(ChatMessage message) {
        String documentName = message.getDocumentName();
        String documentSize = message.getDocumentSize();
        if (StringUtils.isNotEmpty(documentName)) {
            replySubTitleView.setText(documentName + "\n" + documentSize);
        }
    }

    private void setupAudioMessage(ChatMessage message) {
        String audioDuration = message.getAudioDuration();
        replySubTitleView.setText(activity.getString(R.string.audio) + " - " + audioDuration);
    }

    private void setupContactMessage(ChatMessage message) {
        String contactName = message.getContactName();
        String contactPhoneNumber = message.getContactNumber();
        String purifiedPhoneNumber = StringUtils.stripEnd(contactPhoneNumber, ",");
        replySubTitleView.setText(UiUtils.fromHtml("<b>" + activity.getString(R.string.contact) + "</b>") + " - " + contactName + " " + purifiedPhoneNumber);
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

    public void setupVideoMessage(final ChatMessage videoMessage) {
        String remoteVideoThumbnailUrl = videoMessage.getThumbnailUrl();
        File localThumbFile = new File(videoMessage.getLocalThumb());
        if (StringUtils.isNotEmpty(remoteVideoThumbnailUrl)) {
            HolloutLogger.d("VideoThumbnailPath", "Remote Video Thumb exists with value = " + remoteVideoThumbnailUrl);
            UiUtils.loadImage(activity, videoMessage.getThumbnailUrl(), replyIconView);
        } else {
            if (localThumbFile.exists()) {
                HolloutLogger.d("VideoThumbnailPath", "Local Video Thumb exists with value = " + localThumbFile);
                loadVideoFromPath(replyIconView, videoMessage.getLocalThumb());
            }
        }
        AppConstants.messageReplyAttachmentPositions.put(getMessageHash(), true);
        AppConstants.messageReplyAttachmentMediaPlayPositions.put(getMessageHash(), true);
        UiUtils.showView(playReplyIconView, true);
        UiUtils.showView(replyAttachmentView, true);
        long videoLength = videoMessage.getVideoDuration();
        replySubTitleView.setText(UiUtils.fromHtml(activity.getString(R.string.video) + " - <b>" + UiUtils.getTimeString(videoLength) + "</b>"));
        String fileCaption = repliedMessage.getFileCaption();
        if (fileCaption != null && StringUtils.isNotEmpty(fileCaption) && !StringUtils.containsIgnoreCase(fileCaption, activity.getString(R.string.photo))) {
            replySubTitleView.setText(UiUtils.fromHtml(fileCaption + " - <b>" + UiUtils.getTimeString(videoLength) + "</b>"));
        }
    }

    private void launchVideoIntent(ChatMessage videoMessage) {
        Intent mViewVideoIntent = new Intent(Intent.ACTION_VIEW);
        mViewVideoIntent.setDataAndType(Uri.parse(new File(videoMessage.getLocalUrl()).exists() ? videoMessage.getLocalUrl() : videoMessage.getRemoteUrl()), "video/*");
        activity.startActivity(mViewVideoIntent);
    }

    private void loadDrawables(Context context, ImageView emojiView, String reactionTag) {
        UiUtils.showView(replyAttachmentView, true);
        AppConstants.messageReplyAttachmentPositions.put(getMessageHash(), true);
        KeyframesDrawable imageDrawable = new KeyframesDrawableBuilder().withImage(getKFImage(context, reactionTag)).build();
        emojiView.setImageDrawable(imageDrawable);
        imageDrawable.startAnimation();
    }

    private void setupReactionMessage(String reactionValue) {
        if (StringUtils.isNotEmpty(reactionValue)) {
            replySubTitleView.setText(StringUtils.strip(reactionValue.split("/")[1], ".json"));
            loadDrawables(activity, replyIconView, reactionValue);
        }
    }

    private KFImage getKFImage(Context context, String fileName) {
        AssetManager assetManager = context.getAssets();
        KFImage kfImage = null;
        try {
            InputStream stream = assetManager.open(fileName);
            kfImage = KFImageDeserializer.deserialize(stream);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return kfImage;
    }

    private void setupTxtMessage(String messageBody) {
        setupMessageBodyOnlyMessage(messageBody);
    }

    private void setUpGifMessage(String gifUrl) {
        AppConstants.messageReplyAttachmentPositions.put(getMessageHash(), true);
        UiUtils.showView(replyAttachmentView, true);
        replySubTitleView.setText(UiUtils.fromHtml("<b>" + activity.getString(R.string.gif) + "</b>"));
        if (StringUtils.isNotEmpty(gifUrl)) {
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
                        }).into(replyIconView);
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
                    }).into(replyIconView);
                }
            }
        }
    }

    private void setupMessageBodyOnlyMessage(String messageBody) {
        replySubTitleView.setText(UiUtils.fromHtml(messageBody));
    }

    private void refreshViewComponents() {
        UiUtils.showView(replyAttachmentView, AppConstants.messageReplyAttachmentPositions.get(getMessageHash()));
        UiUtils.showView(playReplyIconView, AppConstants.messageReplyAttachmentMediaPlayPositions.get(getMessageHash()));
    }

    @Override
    public void onClick(View view) {
        EventBus.getDefault().post(new ScrollToMessageEvent(repliedMessage));
    }

}

