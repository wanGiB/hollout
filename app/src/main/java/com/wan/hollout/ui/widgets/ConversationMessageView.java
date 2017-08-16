package com.wan.hollout.ui.widgets;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.Nullable;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.john.waveview.WaveView;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.wan.hollout.R;
import com.wan.hollout.ui.widgets.chatmessageview.ChatMessageView;
import com.wan.hollout.utils.AppConstants;
import com.wan.hollout.utils.UiUtils;

import org.apache.commons.lang3.StringUtils;

import java.util.Date;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * @author Wan Clem
 */
@SuppressWarnings("FieldCanBeLocal")
public class ConversationMessageView extends RelativeLayout implements View.OnClickListener {

    @BindView(R.id.message_container)
    ChatMessageView chatMessageView;

    @Nullable
    @BindView(R.id.attached_photo_or_video_thumbnail)
    RoundedImageView attachedPhotoOrVideoThumbnailView;

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

    @BindView(R.id.delivery_status_and_time_view)
    TextView deliveryStatusAndTimeView;

    @Nullable
    @BindView(R.id.link_preview)
    LinkPreview linkPreview;

    private ParseObject messageObject;
    private ParseUser signedInUser;

    private Activity activity;

    public ConversationMessageView(Context context) {
        super(context);
    }

    public ConversationMessageView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ConversationMessageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void bindData(Activity context, ParseObject messageObject) {
        this.activity = context;
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
            if (messageBodyView != null) {
                messageBodyView.setText(UiUtils.fromHtml(messageBody + " &#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;&#160;"));
            }
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
                    deliveryStatusAndTimeView.setText(activity.getString(R.string.read).concat(messageTime));
                    UiUtils.attachDrawableToTextView(activity, deliveryStatusAndTimeView, R.drawable.msg_status_client_read, UiUtils.DrawableDirection.LEFT);
                    break;
                case AppConstants.DELIVERED:
                    deliveryStatusAndTimeView.setText(activity.getString(R.string.delivered).concat(messageTime));
                    UiUtils.attachDrawableToTextView(getContext(), deliveryStatusAndTimeView, R.drawable.msg_status_client_received_white, UiUtils.DrawableDirection.LEFT);
                    break;
                default:
                    deliveryStatusAndTimeView.setText(activity.getString(R.string.sent).concat(messageTime));
                    UiUtils.attachDrawableToTextView(activity, deliveryStatusAndTimeView, R.drawable.msg_status_server_receive, UiUtils.DrawableDirection.LEFT);
                    break;
            }
        }
    }

    private void refreshViews() {

    }

    public int getMessageId() {
        return messageObject.getObjectId().hashCode();
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
