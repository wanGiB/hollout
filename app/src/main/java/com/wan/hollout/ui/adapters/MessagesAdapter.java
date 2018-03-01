package com.wan.hollout.ui.adapters;

import android.app.Activity;
import android.graphics.drawable.TransitionDrawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.wan.hollout.R;
import com.wan.hollout.enums.MessageDirection;
import com.wan.hollout.enums.MessageType;
import com.wan.hollout.models.ChatMessage;
import com.wan.hollout.ui.utils.DateUtils;
import com.wan.hollout.ui.widgets.ChatMessageView;
import com.wan.hollout.utils.AppConstants;
import com.wan.hollout.utils.HolloutLogger;
import com.wan.hollout.utils.HolloutUtils;
import com.wan.hollout.utils.UiUtils;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * @author Wan Clem
 */

@SuppressWarnings({"FieldCanBeLocal", "unchecked"})
public class MessagesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> implements
        StickyRecyclerHeadersAdapter<MessagesAdapter.MessagedDatesHeaderHolder> {

    private List<ChatMessage> messages;
    private Activity context;
    private LayoutInflater layoutInflater;
    private Calendar calendar;

    private static final int OUTGOING_MESSAGE_TEXT_ONLY = 0;
    private static final int OUTGOING_MESSAGE_WITH_PHOTO_LOCATION_OR_VIDEO = 1;
    private static final int OUTGOING_MESSAGE_WITH_AUDIO = 2;
    private static final int OUTGOING_MESSAGE_WITH_DOCUMENT_OR_OTHER_FILE = 3;
    private static final int OUTGOING_MESSAGE_WITH_CONTACT = 4;
    private static final int OUTGOING_MESSAGE_WITH_LINK_PREVIEW = 5;
    private static final int OUTGOING_MESSAGE_WITH_REACTION = 6;
    private static final int OUTGOING_MESSAGE_WITH_GIF = 7;

    private static final int INCOMING_MESSAGE_TEXT_ONLY = 8;
    private static final int INCOMING_MESSAGE_WITH_PHOTO_LOCATION_OR_VIDEO = 9;
    private static final int INCOMING_MESSAGE_WITH_AUDIO = 10;
    private static final int INCOMING_MESSAGE_WITH_DOCUMENT_OR_OTHER_FILE = 11;
    private static final int INCOMING_MESSAGE_WITH_CONTACT = 12;
    private static final int INCOMING_MESSAGE_WITH_LINK_PREVIEW = 13;
    private static final int INCOMING_MESSAGE_WITH_REACTION = 14;
    private static final int INCOMING_MESSAGE_WITH_GIF = 15;
    private static final int MESSAGE_TYPE_CALL = 16;

    private String searchString;

    public MessagesAdapter(Activity context, List<ChatMessage> messages) {
        this.context = context;
        this.messages = messages;
        this.layoutInflater = LayoutInflater.from(context);
        this.calendar = Calendar.getInstance();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        int layoutRes;
        switch (viewType) {
            case OUTGOING_MESSAGE_TEXT_ONLY:
                layoutRes = R.layout.outgoing_message_text_only;
                break;
            case OUTGOING_MESSAGE_WITH_PHOTO_LOCATION_OR_VIDEO:
                layoutRes = R.layout.outgoing_message_with_photo_location_or_video;
                break;
            case OUTGOING_MESSAGE_WITH_AUDIO:
                layoutRes = R.layout.outgoing_message_with_audio;
                break;
            case OUTGOING_MESSAGE_WITH_DOCUMENT_OR_OTHER_FILE:
                layoutRes = R.layout.outgoing_message_with_document_or_other_file;
                break;
            case OUTGOING_MESSAGE_WITH_CONTACT:
                layoutRes = R.layout.outgoing_message_with_contact;
                break;
            case OUTGOING_MESSAGE_WITH_LINK_PREVIEW:
                layoutRes = R.layout.outgoing_message_with_link_preview;
                break;
            case OUTGOING_MESSAGE_WITH_REACTION:
                layoutRes = R.layout.outgoing_message_with_reaction;
                break;
            case OUTGOING_MESSAGE_WITH_GIF:
                layoutRes = R.layout.outgoing_message_with_gif;
                break;
            case INCOMING_MESSAGE_TEXT_ONLY:
                layoutRes = R.layout.incoming_message_text_only;
                break;
            case INCOMING_MESSAGE_WITH_PHOTO_LOCATION_OR_VIDEO:
                layoutRes = R.layout.incoming_message_with_photo_location_or_video;
                break;
            case INCOMING_MESSAGE_WITH_AUDIO:
                layoutRes = R.layout.incoming_message_with_audio;
                break;
            case INCOMING_MESSAGE_WITH_DOCUMENT_OR_OTHER_FILE:
                layoutRes = R.layout.incoming_message_with_document_or_other_file;
                break;
            case INCOMING_MESSAGE_WITH_CONTACT:
                layoutRes = R.layout.incoming_message_with_contact;
                break;
            case INCOMING_MESSAGE_WITH_LINK_PREVIEW:
                layoutRes = R.layout.incoming_message_with_link_preview;
                break;
            case INCOMING_MESSAGE_WITH_REACTION:
                layoutRes = R.layout.incoming_message_with_reaction;
                break;
            case INCOMING_MESSAGE_WITH_GIF:
                layoutRes = R.layout.incoming_message_with_gif;
                break;
            case MESSAGE_TYPE_CALL:
                layoutRes = R.layout.missed_call_view;
                break;
            default:
                layoutRes = R.layout.outgoing_message_text_only;
                break;
        }
        HolloutLogger.d("LayoutYawa", "Inflated View Type = " + viewType);
        View convertView = layoutInflater.inflate(layoutRes, parent, false);
        return new MessageItemsHolder(convertView);
    }

    @Override
    public int getItemViewType(int position) {
        ChatMessage messageObject = messages.get(position);
        MessageDirection messageDirection = messageObject.getMessageDirection();
        MessageType messageType = messageObject.getMessageType();
        if (messageType == MessageType.IMAGE || messageType == MessageType.VIDEO || messageType == MessageType.LOCATION) {
            return messageDirection == MessageDirection.OUTGOING ? OUTGOING_MESSAGE_WITH_PHOTO_LOCATION_OR_VIDEO : INCOMING_MESSAGE_WITH_PHOTO_LOCATION_OR_VIDEO;
        }
        if (messageType == MessageType.CONTACT) {
            return messageDirection == MessageDirection.OUTGOING ? OUTGOING_MESSAGE_WITH_CONTACT : INCOMING_MESSAGE_WITH_CONTACT;
        }
        if (messageType == MessageType.AUDIO) {
            return messageDirection == MessageDirection.OUTGOING ? OUTGOING_MESSAGE_WITH_AUDIO : INCOMING_MESSAGE_WITH_AUDIO;
        }
        if (messageType == MessageType.DOCUMENT) {
            return messageDirection == MessageDirection.OUTGOING ? OUTGOING_MESSAGE_WITH_DOCUMENT_OR_OTHER_FILE : INCOMING_MESSAGE_WITH_DOCUMENT_OR_OTHER_FILE;
        }
        if (messageType == MessageType.VOICE) {
            return messageDirection == MessageDirection.OUTGOING ? OUTGOING_MESSAGE_WITH_AUDIO : INCOMING_MESSAGE_WITH_AUDIO;
        }
        if (messageType == MessageType.TXT) {
            List<String> links = UiUtils.pullLinks(messageObject.getMessageBody());
            if (!links.isEmpty()) {
                return messageDirection == MessageDirection.OUTGOING ? OUTGOING_MESSAGE_WITH_LINK_PREVIEW : INCOMING_MESSAGE_WITH_LINK_PREVIEW;
            } else {
                return messageDirection == MessageDirection.OUTGOING ? OUTGOING_MESSAGE_TEXT_ONLY : INCOMING_MESSAGE_TEXT_ONLY;
            }
        }
        if (messageType == MessageType.REACTION) {
            return messageDirection == MessageDirection.OUTGOING ? OUTGOING_MESSAGE_WITH_REACTION : INCOMING_MESSAGE_WITH_REACTION;
        }
        if (messageType == MessageType.GIF) {
            return messageDirection == MessageDirection.OUTGOING ? OUTGOING_MESSAGE_WITH_GIF : INCOMING_MESSAGE_WITH_GIF;
        }
        if (messageType == MessageType.CALL) {
            return MESSAGE_TYPE_CALL;
        }
        return -1;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        MessageItemsHolder messageItemsHolder = (MessageItemsHolder) holder;
        ChatMessage messageObject = messages.get(position);
        if (messageObject != null) {
            messageItemsHolder.bindMessage(context, messageObject, getSearchString());
        }
    }

    @Override
    public String getHeaderId(int position) {
        ChatMessage chatMessage = messages.get(position);
        Date createdAt = new Date(chatMessage.getTimeStamp());
        calendar.setTime(createdAt);
        return String.valueOf(HolloutUtils.hashCode(calendar.get(Calendar.YEAR), calendar.get(Calendar.DAY_OF_YEAR)));
    }

    @Override
    public MessagedDatesHeaderHolder onCreateHeaderViewHolder(ViewGroup parent) {
        View messageDatesHeaderView = layoutInflater.inflate(R.layout.scroll_date_header, parent, false);
        return new MessagedDatesHeaderHolder(messageDatesHeaderView);
    }

    @Override
    public void onBindHeaderViewHolder(MessagedDatesHeaderHolder holder, int position) {
        ChatMessage message = messages.get(position);
        if (message != null) {
            Date createdAt = new Date(message.getTimeStamp());
            holder.bindHeader(DateUtils.getRelativeDate(context, Locale.getDefault(), createdAt.getTime()));
        }
    }

    @Override
    public int getItemCount() {
        return messages != null ? messages.size() : 0;
    }

    public void setSearchString(String searchString) {
        this.searchString = searchString;
    }

    public String getSearchString() {
        return searchString;
    }

    @SuppressWarnings("WeakerAccess")
    static class MessageItemsHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.conversation_message_view)
        ChatMessageView chatMessageView;

        public MessageItemsHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        public void bindMessage(Activity context, ChatMessage emMessage, String searchString) {
            chatMessageView.bindData(context, emMessage, searchString);
            if (AppConstants.bounceablePositions.get(emMessage.getMessageId().hashCode())) {
                itemView.setBackground(ContextCompat.getDrawable(context, R.drawable.background_color_transition));
                TransitionDrawable transition = (TransitionDrawable) itemView.getBackground();
                transition.setCrossFadeEnabled(true);
                transition.startTransition(3000);
                AppConstants.bounceablePositions.clear();
            }
        }

    }

    @SuppressWarnings("WeakerAccess")
    static class MessagedDatesHeaderHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.scroll_date_header)
        TextView scrollDateHeaderView;

        public MessagedDatesHeaderHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        public void bindHeader(String relativeDate) {
            scrollDateHeaderView.setText(relativeDate);
        }

    }

}
