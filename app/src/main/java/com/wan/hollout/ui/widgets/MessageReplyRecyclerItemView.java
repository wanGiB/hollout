package com.wan.hollout.ui.widgets;

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
import com.hyphenate.chat.EMImageMessageBody;
import com.hyphenate.chat.EMLocationMessageBody;
import com.hyphenate.chat.EMMessage;
import com.hyphenate.chat.EMMessageBody;
import com.hyphenate.chat.EMTextMessageBody;
import com.hyphenate.chat.EMVideoMessageBody;
import com.hyphenate.chat.EMVoiceMessageBody;
import com.hyphenate.exceptions.HyphenateException;
import com.parse.ParseObject;
import com.wan.hollout.R;
import com.wan.hollout.animations.KeyframesDrawable;
import com.wan.hollout.animations.KeyframesDrawableBuilder;
import com.wan.hollout.animations.deserializers.KFImageDeserializer;
import com.wan.hollout.animations.model.KFImage;
import com.wan.hollout.eventbuses.ScrollToMessageEvent;
import com.wan.hollout.utils.AppConstants;
import com.wan.hollout.utils.AuthUtil;
import com.wan.hollout.utils.HolloutLogger;
import com.wan.hollout.utils.LocationUtils;
import com.wan.hollout.utils.UiUtils;

import org.apache.commons.lang3.StringUtils;
import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * @author Wan Clem
 */

@SuppressWarnings("ConstantConditions")
public class MessageReplyRecyclerItemView extends RelativeLayout implements View.OnClickListener {

    private static final String TAG = "MessageReplyRecyclerItemView";
    private HolloutTextView replyTitleView;
    private HolloutTextView replySubTitleView;
    private FrameLayout replyAttachmentView;
    private ImageView replyIconView;
    private ImageView playReplyIconView;
    private LinearLayout messageReplyView;
    private RelativeLayout contentView;
    private EMMessage repliedMessage;

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
        replySubTitleView = (HolloutTextView) findViewById(R.id.reply_message_body);
        replyAttachmentView = (FrameLayout) findViewById(R.id.reply_attachment_view);
        replyIconView = (ImageView) findViewById(R.id.reply_icon);
        playReplyIconView = (ImageView) findViewById(R.id.play_reply_msg_if_video);
        contentView = (RelativeLayout) findViewById(R.id.content_view);
        messageReplyView = (LinearLayout) findViewById(R.id.message_reply_view);
        contentView.setOnClickListener(this);
    }

    public int getMessageHash() {
        return repliedMessage.getMsgId().hashCode();
    }

    public void bindMessageReply(Activity activity, EMMessage repliedMessage) {
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

    private EMMessage.Type getMessageType() {
        return repliedMessage.getType();
    }

    private void setupMessageBody() {

        ParseObject signedInUser = AuthUtil.getCurrentUser();

        EMMessage.Type messageType = getMessageType();
        EMMessageBody messageBody = repliedMessage.getBody();
        try {
            String senderNameOfMessage = repliedMessage.getStringAttribute(AppConstants.APP_USER_DISPLAY_NAME);
            if (signedInUser != null) {
                String signedInUserId = signedInUser.getString(AppConstants.REAL_OBJECT_ID);
                if (signedInUserId.equals(repliedMessage.getFrom())) {
                    replyTitleView.setText(activity.getString(R.string.you));
                } else {
                    replyTitleView.setText(senderNameOfMessage);
                }
                if (messageType == EMMessage.Type.TXT) {
                    setupTxtMessage((EMTextMessageBody) messageBody);
                }

                if (messageType == EMMessage.Type.IMAGE) {
                    setupImageMessage((EMImageMessageBody) messageBody);
                }

                if (messageType == EMMessage.Type.VIDEO) {
                    setupVideoMessage((EMVideoMessageBody) messageBody);
                }

                if (messageType == EMMessage.Type.FILE) {
                    setupFileMessage();
                }

                if (messageType == EMMessage.Type.VOICE) {
                    setupVoiceMessage((EMVoiceMessageBody) messageBody);
                }

                if (messageType == EMMessage.Type.LOCATION) {
                    setupLocationMessage((EMLocationMessageBody) messageBody);
                }

                refreshViewComponents();

            }

        } catch (HyphenateException e) {
            e.printStackTrace();
        }

    }

    private void setupLocationMessage(EMLocationMessageBody messageBody) {
        String locationName = messageBody.getAddress();
        replySubTitleView.setText(activity.getString(R.string.location));
        if (locationName != null) {
            replySubTitleView.setText(locationName);
        }
        String locationStaticMap = LocationUtils.loadStaticMap(String.valueOf(messageBody.getLatitude()),
                String.valueOf(messageBody.getLongitude()));

        if (StringUtils.isNotEmpty(locationStaticMap)) {
            UiUtils.showView(replyAttachmentView, true);
            UiUtils.showView(playReplyIconView, false);
            AppConstants.messageReplyAttachmentPositions.put(getMessageHash(), true);
            UiUtils.loadImage(activity, locationStaticMap, replyIconView);
        }

    }

    private void setupVoiceMessage(EMVoiceMessageBody emFileMessageBody) {
        String audioDuration = UiUtils.getTimeString(emFileMessageBody.getLength());
        replySubTitleView.setText(activity.getString(R.string.voice_note) + " - " + audioDuration);
    }

    private void setupImageMessage(EMImageMessageBody messageBody) {
        UiUtils.showView(replyAttachmentView, true);
        AppConstants.messageReplyAttachmentPositions.put(getMessageHash(), true);
        String filePath = messageBody.getLocalUrl();
        File file = new File(filePath);
        if (file.exists()) {
            filePath = messageBody.getLocalUrl();
        } else {
            filePath = messageBody.getRemoteUrl();
        }
        UiUtils.loadImage(activity, filePath, replyIconView);
        try {
            String fileCaption = repliedMessage.getStringAttribute(AppConstants.FILE_CAPTION);
            if (StringUtils.isNotEmpty(fileCaption)) {
                replySubTitleView.setText(fileCaption);
            } else {
                replySubTitleView.setText(activity.getString(R.string.photo));
            }
        } catch (HyphenateException e) {
            e.printStackTrace();
            HolloutLogger.e(TAG, e.getMessage());
        }

    }

    private void setupFileMessage() {
        try {
            String fileType = repliedMessage.getStringAttribute(AppConstants.FILE_TYPE);
            if (fileType.equals(AppConstants.FILE_TYPE_CONTACT)) {
                setupContactMessage();
            }
            if (fileType.equals(AppConstants.FILE_TYPE_AUDIO)) {
                setupAudioMessage();
            }
            if (fileType.equals(AppConstants.FILE_TYPE_DOCUMENT)) {
                setupDocumentMessage();
            }
        } catch (HyphenateException e) {
            e.printStackTrace();
            HolloutLogger.e(TAG, e.getMessage());
        }
    }

    private void setupDocumentMessage() {
        try {
            String documentName = repliedMessage.getStringAttribute(AppConstants.FILE_NAME);
            String documentSize = repliedMessage.getStringAttribute(AppConstants.FILE_SIZE);
            if (StringUtils.isNotEmpty(documentName)) {
                replySubTitleView.setText(documentName + "\n" + documentSize);
            }
        } catch (HyphenateException e) {
            e.printStackTrace();
        }
    }

    private void setupAudioMessage() {
        try {
            String audioDuration = repliedMessage.getStringAttribute(AppConstants.AUDIO_DURATION);
            replySubTitleView.setText(activity.getString(R.string.audio) + " - " + audioDuration);
        } catch (HyphenateException e) {
            e.printStackTrace();
            HolloutLogger.e(TAG, "Audio Exception = " + e.getMessage());
        }
    }

    private void setupContactMessage() {
        try {
            String contactName = repliedMessage.getStringAttribute(AppConstants.CONTACT_NAME);
            String contactPhoneNumber = repliedMessage.getStringAttribute(AppConstants.CONTACT_NUMBER);
            String purifiedPhoneNumber = StringUtils.stripEnd(contactPhoneNumber, ",");
            replySubTitleView.setText(activity.getString(R.string.contact) + " - " + contactName + " " + purifiedPhoneNumber);
        } catch (HyphenateException e) {
            e.printStackTrace();
            HolloutLogger.e(TAG, e.getMessage());
        }
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

    public void setupVideoMessage(final EMVideoMessageBody upVideoMessage) {
        String remoteVideoThumbnailUrl = upVideoMessage.getThumbnailUrl();
        File localThumbFile = new File(upVideoMessage.getLocalThumb());
        if (StringUtils.isNotEmpty(remoteVideoThumbnailUrl)) {
            HolloutLogger.d("VideoThumbnailPath", "Remote Video Thumb exists with value = " + remoteVideoThumbnailUrl);
            UiUtils.loadImage(activity, upVideoMessage.getThumbnailUrl(), replyIconView);
        } else {
            if (localThumbFile.exists()) {
                HolloutLogger.d("VideoThumbnailPath", "Local Video Thumb exists with value = " + localThumbFile);
                loadVideoFromPath(replyIconView, upVideoMessage.getLocalThumb());
            }
        }
        AppConstants.messageReplyAttachmentPositions.put(getMessageHash(), true);
        AppConstants.messageReplyAttachmentMediaPlayPositions.put(getMessageHash(), true);
        UiUtils.showView(playReplyIconView, true);
        UiUtils.showView(replyAttachmentView, true);
        long videoLength = upVideoMessage.getDuration();
        replySubTitleView.setText(UiUtils.fromHtml(activity.getString(R.string.video) + " - <b>" + UiUtils.getTimeString(videoLength) + "</b>"));
        try {
            String fileCaption = repliedMessage.getStringAttribute(AppConstants.FILE_CAPTION);
            if (fileCaption != null && StringUtils.isNotEmpty(fileCaption) && !StringUtils.containsIgnoreCase(fileCaption, activity.getString(R.string.photo))) {
                replySubTitleView.setText(UiUtils.fromHtml(fileCaption + " - <b>" + UiUtils.getTimeString(videoLength) + "</b>"));
            }
        } catch (HyphenateException e) {
            e.printStackTrace();
            HolloutLogger.e(TAG, e.getMessage());
        }
    }

    private void launchVideoIntent(EMVideoMessageBody upVideoMessage) {
        Intent mViewVideoIntent = new Intent(Intent.ACTION_VIEW);
        mViewVideoIntent.setDataAndType(Uri.parse(new File(upVideoMessage.getLocalUrl()).exists() ? upVideoMessage.getLocalUrl() : upVideoMessage.getRemoteUrl()), "video/*");
        activity.startActivity(mViewVideoIntent);
    }

    private void loadDrawables(Context context, ImageView emojiView, String reactionTag) {
        UiUtils.showView(replyAttachmentView, true);
        AppConstants.messageReplyAttachmentPositions.put(getMessageHash(), true);
        KeyframesDrawable imageDrawable = new KeyframesDrawableBuilder().withImage(getKFImage(context, reactionTag)).build();
        emojiView.setImageDrawable(imageDrawable);
        imageDrawable.startAnimation();
    }

    private void setupReactionMessage() {
        try {
            String reactionValue = repliedMessage.getStringAttribute(AppConstants.REACTION_VALUE);
            if (StringUtils.isNotEmpty(reactionValue)) {
                replySubTitleView.setText(StringUtils.strip(reactionValue.split("/")[1], ".json"));
                loadDrawables(activity, replyIconView, reactionValue);
            }
        } catch (HyphenateException e) {
            e.printStackTrace();
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

    private void setupTxtMessage(EMTextMessageBody messageBody) {
        try {
            String messageAttributeType = repliedMessage.getStringAttribute(AppConstants.MESSAGE_ATTR_TYPE);
            if (messageAttributeType != null) {
                switch (messageAttributeType) {
                    case AppConstants.MESSAGE_ATTR_TYPE_REACTION:
                        String reaction = repliedMessage.getStringAttribute(AppConstants.REACTION_VALUE);
                        if (reaction != null) {
                            setupReactionMessage();
                        } else {
                            setupMessageBodyOnlyMessage(messageBody);
                        }
                        break;
                    case AppConstants.MESSAGE_ATTR_TYPE_GIF:
                        String gifUrl = repliedMessage.getStringAttribute(AppConstants.GIF_URL);
                        if (gifUrl != null) {
                            setUpGifMessage(gifUrl);
                        } else {
                            setupMessageBodyOnlyMessage(messageBody);
                        }
                        break;
                    default:
                        setupMessageBodyOnlyMessage(messageBody);
                        break;
                }
            } else {
                setupMessageBodyOnlyMessage(messageBody);
            }
        } catch (HyphenateException e) {
            setupMessageBodyOnlyMessage(messageBody);
        }
    }

    private void setUpGifMessage(String gifUrl) {
        AppConstants.messageReplyAttachmentPositions.put(getMessageHash(), true);
        UiUtils.showView(replyAttachmentView,true);
        replySubTitleView.setText(UiUtils.fromHtml("<b>"+activity.getString(R.string.gif)+"</b>"));
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

    private void setupMessageBodyOnlyMessage(EMTextMessageBody messageBody) {
        String message = messageBody.getMessage();
        replySubTitleView.setText(UiUtils.fromHtml(message));
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
