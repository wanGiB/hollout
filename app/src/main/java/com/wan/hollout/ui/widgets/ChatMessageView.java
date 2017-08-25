package com.wan.hollout.ui.widgets;

import android.app.Activity;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.hyphenate.EMCallBack;
import com.hyphenate.chat.EMClient;
import com.hyphenate.chat.EMImageMessageBody;
import com.hyphenate.chat.EMMessage;
import com.hyphenate.chat.EMMessageBody;
import com.hyphenate.chat.EMTextMessageBody;
import com.hyphenate.chat.EMVideoMessageBody;
import com.hyphenate.exceptions.HyphenateException;
import com.john.waveview.WaveView;
import com.wan.hollout.R;
import com.wan.hollout.ui.widgets.chatmessageview.MessageBubbleLayout;
import com.wan.hollout.utils.AppConstants;
import com.wan.hollout.utils.UiUtils;

import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * @author Wan Clem
 */
@SuppressWarnings({"FieldCanBeLocal", "ConstantConditions"})
public class ChatMessageView extends RelativeLayout implements View.OnClickListener {

    @BindView(R.id.message_container)
    MessageBubbleLayout messageBubbleLayout;

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
    HolloutTextView contactPhoneNumbersView;

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

    @BindView(R.id.textview_time)
    TextView timeTextView;

    @BindView(R.id.delivery_status_view)
    ImageView deliveryStatusView;

    @Nullable
    @BindView(R.id.link_preview)
    LinkPreview linkPreview;

    private EMMessage message;
    private Activity activity;

    protected EMCallBack messageStatusCallback;

    protected Handler handler = new Handler(Looper.getMainLooper());

    public ChatMessageView(Context context) {
        super(context);
    }

    public ChatMessageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ChatMessageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void bindData(Activity context, EMMessage messageObject) {
        this.activity = context;
        this.message = messageObject;
        setupMessageBody();
        setupMessageTimeAndDeliveryStatus();
        refreshViews();
        messageBubbleLayout.setOnClickListener(this);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        ButterKnife.bind(this);
    }

    private EMMessage.Type getMessageType() {
        return message.getType();
    }

    public EMMessage.Direct getMessageDirection() {
        return message.direct();
    }

    private void setupMessageBody() {
        EMMessage.Type messageType = getMessageType();
        EMMessageBody messageBody = message.getBody();

        if (messageType == EMMessage.Type.TXT) {
            AppConstants.messageBodyPositions.put(getMessageHash(), true);
            setupTxtMessage((EMTextMessageBody) messageBody);
        }

        if (messageType == EMMessage.Type.IMAGE) {
            setupImageMessage((EMImageMessageBody) messageBody);
        }

        if (messageType == EMMessage.Type.VIDEO) {
            setupVideoMessage((EMVideoMessageBody) messageBody);
        }

        handleCommonalities();
        refreshViews();
    }

    private void handleCommonalities() {
        handleMessageReadOnClickOfImageView();
        setMessageStatusCallback();
    }

    public int getMessageHash() {
        return message.getMsgId().hashCode();
    }

    private void setupImageMessage(EMImageMessageBody messageBody) {
        String filePath = messageBody.getLocalUrl();

        File file = new File(filePath);

        if (file.exists()) {
            filePath = messageBody.getLocalUrl();
        } else {
            filePath = messageBody.getRemoteUrl();
            if (StringUtils.isNotEmpty(messageBody.getRemoteUrl())) {
                UiUtils.showView(photoVideoProgressView, false);
            }
        }

        UiUtils.loadImage(activity, filePath, attachedPhotoOrVideoThumbnailView);
        UiUtils.showView(fileSizeDurationView, false);
        AppConstants.fileSizeOrDurationPositions.put(getMessageHash(), false);

        try {
            String fileCaption = message.getStringAttribute(AppConstants.FILE_CAPTION);
            if (StringUtils.isNotEmpty(fileCaption)) {
                UiUtils.showView(messageBodyView, true);
                messageBodyView.setText(fileCaption);
                AppConstants.messageBodyPositions.put(getMessageHash(), true);
            } else {
                UiUtils.showView(messageBodyView, false);
                AppConstants.messageBodyPositions.put(getMessageHash(), false);
            }
        } catch (HyphenateException e) {
            e.printStackTrace();
        }

    }

    public void setupVideoMessage(EMVideoMessageBody upVideoMessage) {
        String videoThumb = upVideoMessage.getLocalThumb();
        File localThumbFile = new File(videoThumb);
        if (localThumbFile.exists()) {
            videoThumb = upVideoMessage.getLocalThumb();
        } else {
            videoThumb = upVideoMessage.getThumbnailUrl();
            if (StringUtils.isNotEmpty(videoThumb)) {
                UiUtils.showView(photoVideoProgressView, false);
            }
        }

        long videoLength = upVideoMessage.getVideoFileLength();

        UiUtils.loadImage(activity, videoThumb, attachedPhotoOrVideoThumbnailView);
        UiUtils.showView(fileSizeDurationView, true);
        AppConstants.fileSizeOrDurationPositions.put(getMessageHash(), true);
        fileSizeDurationView.setText(UiUtils.getTimeString(videoLength));

        UiUtils.showView(playMediaIfVideoIcon, true);
        AppConstants.playableVideoPositions.put(getMessageHash(), true);

        try {
            String fileCaption = message.getStringAttribute(AppConstants.FILE_CAPTION);
            if (StringUtils.isNotEmpty(fileCaption)) {
                UiUtils.showView(messageBodyView, true);
                messageBodyView.setText(fileCaption);
                AppConstants.messageBodyPositions.put(getMessageHash(), true);
            } else {
                UiUtils.showView(messageBodyView, false);
                AppConstants.messageBodyPositions.put(getMessageHash(), false);
            }
        } catch (HyphenateException e) {
            e.printStackTrace();
        }
    }

    private void setupTxtMessage(EMTextMessageBody messageBody) {
        String message = messageBody.getMessage();
        if (StringUtils.isNotEmpty(message)) {
            UiUtils.showView(messageBodyView, true);
            if (messageBodyView != null) {
                if (getMessageDirection() == EMMessage.Direct.SEND) {
                    messageBodyView.setText(UiUtils.fromHtml(message + " &#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;" +
                            "&#160;&#160;&#160;&#160;&#160;&#160;&#160;"));
                } else {
                    messageBodyView.setText(UiUtils.fromHtml(message
                            + " &#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;"));
                }
            }
        }
        acknowledgeMessageRead();
    }

    private void handleMessageReadOnClickOfImageView() {
        if (attachedPhotoOrVideoThumbnailView != null) {
            attachedPhotoOrVideoThumbnailView.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    acknowledgeMessageRead();
                }
            });
        }
    }

    /**
     * set callback for sending message
     */
    protected void setMessageStatusCallback() {
        if (messageStatusCallback == null) {

            messageStatusCallback = new EMCallBack() {

                @Override
                public void onSuccess() {
                    if (message.getType() != EMMessage.Type.TXT && photoVideoProgressView != null) {
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                UiUtils.showView(photoVideoProgressView, false);
                                AppConstants.wavePositions.put(getMessageHash(), false);
                            }
                        });
                    }
                }

                @Override
                public void onProgress(final int progress, String status) {
                    if (message.getType() != EMMessage.Type.TXT && photoVideoProgressView != null) {
                        handler.post(new Runnable() {
                            @Override
                            public void run() {
                                UiUtils.showView(photoVideoProgressView, true);
                                photoVideoProgressView.setProgress(progress);
                                if (progress >= 99) {
                                    UiUtils.showView(photoVideoProgressView, false);
                                    AppConstants.wavePositions.put(getMessageHash(), false);
                                }
                            }
                        });
                    }
                }

                @Override
                public void onError(int code, String error) {

                }

            };

        }

        message.setMessageStatusCallback(messageStatusCallback);

    }

    private void acknowledgeMessageRead() {
        if (message.direct() == EMMessage.Direct.RECEIVE && !message.isAcked() && message.getChatType() == EMMessage.ChatType.Chat) {
            try {
                EMClient.getInstance().chatManager().ackMessageRead(message.getFrom(), message.getMsgId());
            } catch (HyphenateException e) {
                e.printStackTrace();
            }
        }
    }

    private void setupMessageTimeAndDeliveryStatus() {
        Date messageDate = new Date(message.getMsgTime());
        String messageTime = AppConstants.DATE_FORMATTER_IN_12HRS.format(messageDate);
        timeTextView.setText(messageTime);
        if (getMessageDirection() == EMMessage.Direct.SEND) {
            if (message.isAcked()) {
                deliveryStatusView.setImageResource(R.drawable.msg_status_client_read);
            } else if (message.isListened()) {
                deliveryStatusView.setImageResource(R.drawable.msg_status_client_read);
            } else if (message.isDelivered()) {
                deliveryStatusView.setImageResource(hashDrawable() ? R.drawable.msg_status_client_received_white : R.drawable.msg_status_client_received);
            } else {
                deliveryStatusView.setImageResource(hashDrawable() ? R.drawable.msg_status_server_received_white : R.drawable.msg_status_server_receive);
            }
        }
    }

    public boolean hashDrawable() {
        return (message.getType() == EMMessage.Type.IMAGE || message.getType() == EMMessage.Type.VIDEO || message.getType() == EMMessage.Type.LOCATION) && message.direct() == EMMessage.Direct.SEND;
    }

    private void refreshViews() {
        UiUtils.showView(fileSizeDurationView, AppConstants.fileSizeOrDurationPositions.get(getMessageHash()));
        UiUtils.showView(photoVideoProgressView, AppConstants.wavePositions.get(getMessageHash()));
        UiUtils.showView(messageBodyView, AppConstants.messageBodyPositions.get(getMessageHash()));
        UiUtils.showView(playMediaIfVideoIcon, AppConstants.playableVideoPositions.get(getMessageHash()));
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.message_container:
                break;
        }
    }

}
