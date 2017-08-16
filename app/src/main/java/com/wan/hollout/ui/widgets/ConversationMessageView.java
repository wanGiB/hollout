package com.wan.hollout.ui.widgets;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.john.waveview.WaveView;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.wan.hollout.R;
import com.wan.hollout.ui.widgets.chatmessageview.ChatMessageView;
import com.wan.hollout.utils.AppConstants;
import com.wan.hollout.utils.UiUtils;

import org.apache.commons.lang3.StringUtils;
import org.greenrobot.eventbus.EventBus;

import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * @author Wan Clem
 */
public class ConversationMessageView extends RelativeLayout implements View.OnClickListener {

    @BindView(R.id.message_container)
    ChatMessageView chatMessageView;

    @BindView(R.id.attached_photo_or_video_thumbnail)
    RoundedImageView attachedPhotoOrVideoThumbnailView;

    @BindView(R.id.play_media_if_video_icon)
    ImageView playMediaIfVideoIcon;

    @BindView(R.id.file_size_or_duration)
    HolloutTextView fileSizeDurationView;

    @BindView(R.id.contact_data_layout)
    LinearLayout contactLayout;

    @BindView(R.id.contact_icon)
    CircleColorImageView contactIconView;

    @BindView(R.id.contact_name)
    HolloutTextView contactNameView;

    @BindView(R.id.contact_phone_numbers)
    HolloutTextView contactPhoneNumbersView;

    @BindView(R.id.document_data_layout)
    LinearLayout documentDataLayout;

    @BindView(R.id.document_icon)
    CircleColorImageView documentIconView;

    @BindView(R.id.document_name_and_size)
    HolloutTextView documentNameAndSizeView;

    @BindView(R.id.audio_view)
    AudioView audioView;

    @BindView(R.id.photo_video_wave_view)
    WaveView photoVideoProgressView;

    @BindView(R.id.message_body)
    HolloutTextView messageBodyView;

    @BindView(R.id.delivery_status_and_time_view)
    HolloutTextView deliveryStatusAndTimeView;

    @BindView(R.id.link_preview)
    LinkPreview linkPreview;

    private ParseObject messageObject;
    private ParseUser signedInUser;

    public ConversationMessageView(Context context) {
        super(context);
    }

    public ConversationMessageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ConversationMessageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void bindData(Context context, ParseObject messageObject) {
        signedInUser = ParseUser.getCurrentUser();
        this.messageObject = messageObject;
        setupMessageBody();
        setupMessageDate();
        refreshViews();
        chatMessageView.setOnClickListener(this);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        ButterKnife.bind(this);
    }

    private String getSenderId(ParseObject message) {
        return message.getString(AppConstants.SENDER_ID);
    }

    private void setupMessageBody() {
        String messageBody = messageObject.getString(AppConstants.MESSAGE_BODY);
        if (StringUtils.isNotEmpty(messageBody)) {
            UiUtils.showView(messageBodyView, true);
            messageBodyView.setText(messageBody);
            AppConstants.messageBodyPositions.put(getMessageId(), true);
        }
    }

    private void setupMessageDate() {
        Date messageDate = messageObject.getCreatedAt();
        if (messageDate == null) {
            messageDate = new Date();
        }
        String deliveryStatus = messageObject.getString(AppConstants.DELIVERY_STATUS);
        String messageTime = AppConstants.DATE_FORMATTER_IN_12HRS.format(messageDate);
        if (getSenderId(messageObject).equals(signedInUser.getObjectId())) {
            deliveryStatusAndTimeView.setText(messageTime);
            UiUtils.removeAllDrawablesFromTextView(deliveryStatusAndTimeView);
        } else {
            switch (deliveryStatus) {
                case AppConstants.READ:
                    deliveryStatusAndTimeView.setText(getContext().getString(R.string.read).concat(messageTime));
                    UiUtils.attachDrawableToTextView(getContext(), deliveryStatusAndTimeView, R.drawable.msg_status_client_read, UiUtils.DrawableDirection.LEFT);
                    break;
                case AppConstants.DELIVERED:
                    deliveryStatusAndTimeView.setText(getContext().getString(R.string.delivered).concat(messageTime));
                    UiUtils.attachDrawableToTextView(getContext(), deliveryStatusAndTimeView, R.drawable.msg_status_client_received_white, UiUtils.DrawableDirection.LEFT);
                    break;
                default:
                    deliveryStatusAndTimeView.setText(getContext().getString(R.string.sent).concat(messageTime));
                    UiUtils.attachDrawableToTextView(getContext(), deliveryStatusAndTimeView, R.drawable.msg_status_server_receive, UiUtils.DrawableDirection.LEFT);
                    break;
            }
        }
    }

    private void refreshViews() {
        UiUtils.showView(messageBodyView, AppConstants.messageBodyPositions.get(getMessageId()));
        UiUtils.showView(deliveryStatusAndTimeView, AppConstants.messageTimeVisibilePositions.get(getMessageId()));
    }

    public int getMessageId() {
        return messageObject.getObjectId().hashCode();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (deliveryStatusAndTimeView != null) {
            UiUtils.showView(deliveryStatusAndTimeView, false);
            AppConstants.messageTimeVisibilePositions.put(getMessageId(), false);
        }
    }

    private boolean messageHasMedia() {
        return messageObject.getList(AppConstants.MESSAGE_MEDIA) != null;
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.message_container:
                AppConstants.messageTimeVisibilePositions.clear();
                AppConstants.messageTimeVisibilePositions.put(getMessageId(), true);
                UiUtils.showView(deliveryStatusAndTimeView, deliveryStatusAndTimeView.getVisibility() != VISIBLE);
//                EventBus.getDefault().post(AppConstants.REFRESH_MESSAGES_ADAPTER);
                break;
        }
    }

}
