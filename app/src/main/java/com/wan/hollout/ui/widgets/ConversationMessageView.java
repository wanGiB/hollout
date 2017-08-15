package com.wan.hollout.ui.widgets;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.john.waveview.WaveView;
import com.parse.ParseObject;
import com.wan.hollout.R;
import com.wan.hollout.ui.widgets.chatmessageview.ChatMessageView;
import com.wan.hollout.utils.AppConstants;
import com.wan.hollout.utils.UiUtils;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;

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

    public ConversationMessageView(Context context, ParseObject parseObject) {
        super(context);
        init(context, parseObject);
    }

    public ConversationMessageView(Context context, AttributeSet attrs, ParseObject parseObject) {
        super(context, attrs);
        init(context, parseObject);
    }

    public ConversationMessageView(Context context, AttributeSet attrs, int defStyleAttr, ParseObject parseObject) {
        super(context, attrs, defStyleAttr);
        init(context, parseObject);
    }

    private void init(Context context, ParseObject parseObject) {
        this.messageObject = parseObject;
        inflate(context, getMessageDirection(parseObject).equals(AppConstants.MESSAGE_DIRECTION_INCOMING)
                ? R.layout.conversation_message_view_incoming
                : R.layout.conversation_message_view_outgoing, this);
    }

    public void bindData(ParseObject messageObject) {
        this.messageObject = messageObject;
        setupMessageBody();
        setupMessageDate();
        refreshViews();
        setOnClickListener(this);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        ButterKnife.bind(this);
    }

    private String getMessageDirection(ParseObject message) {
        return message.getString(AppConstants.MESSAGE_DIRECTION);
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
    }

    private void refreshViews() {
        UiUtils.showView(messageBodyView, AppConstants.messageBodyPositions.get(getMessageId()));
    }

    public int getMessageId() {
        return messageObject.getObjectId().hashCode();
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (deliveryStatusAndTimeView != null) {
            UiUtils.showView(deliveryStatusAndTimeView, false);
        }
    }

    @Override
    public void onClick(View v) {

    }

}
