package com.wan.hollout.ui.adapters;

import android.app.Activity;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.parse.ParseObject;
import com.parse.ParseUser;
import com.wan.hollout.R;
import com.wan.hollout.ui.utils.DateUtils;
import com.wan.hollout.ui.widgets.ConversationMessageView;
import com.wan.hollout.utils.AppConstants;
import com.wan.hollout.utils.HolloutUtils;
import com.wan.hollout.utils.UiUtils;

import org.json.JSONArray;

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

    private List<ParseObject> messages;
    private Activity context;
    private LayoutInflater layoutInflater;
    private Calendar calendar;
    private ParseUser signedInUser;

    //MeetPoint Types
    private static final int OUTGOING_MESSAGE_TEXT_ONLY = 0;
    private static final int OUTGOING_MESSAGE_WITH_PHOTO_LOCATION_OR_VIDEO = 1;
    private static final int OUTGOING_MESSAGE_WITH_AUDIO = 2;
    private static final int OUTGOING_MESSAGE_WITH_DOCUMENT_OR_OTHER_FILE = 3;
    private static final int OUTGOING_MESSAGE_WITH_CONTACT = 4;
    private static final int OUTGOING_MESSAGE_WITH_LINK_PREVIEW = 5;


    private static final int INCOMING_MESSAGE_TEXT_ONLY = 6;
    private static final int INCOMING_MESSAGE_WITH_PHOTO_LOCATION_OR_VIDEO = 7;
    private static final int INCOMING_MESSAGE_WITH_AUDIO = 8;
    private static final int INCOMING_MESSAGE_WITH_DOCUMENT_OR_OTHER_FILE = 9;
    private static final int INCOMING_MESSAGE_WITH_CONTACT = 10;
    private static final int INCOMING_MESSAGE_WITH_LINK_PREVIEW = 11;

    public MessagesAdapter(Activity context, List<ParseObject> messages) {
        this.context = context;
        this.messages = messages;
        this.layoutInflater = LayoutInflater.from(context);
        calendar = Calendar.getInstance();
        signedInUser = ParseUser.getCurrentUser();
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
            default:
                layoutRes = R.layout.outgoing_message_text_only;
                break;
        }
        View convertView = layoutInflater.inflate(layoutRes,parent,false);
        return new MessageItemsHolder(convertView);
    }

    @Override
    public int getItemViewType(int position) {
        ParseObject messageObject = messages.get(position);
        String senderId = messageObject.getString(AppConstants.SENDER_ID);
        JSONArray attachments = messageObject.getJSONArray(AppConstants.ATTACHMENT);
        if (attachments != null) {
            String attachmentType = messageObject.getString(AppConstants.ATTACHMENT_TYPE);
            switch (attachmentType) {
                case AppConstants.ATTACHMENT_TYPE_PHOTO:
                case AppConstants.ATTACHMENT_TYPE_VIDEO:
                case AppConstants.ATTACHMENT_TYPE_LOCATION:
                    if (senderId.equals(signedInUser.getObjectId())) {
                        return OUTGOING_MESSAGE_WITH_PHOTO_LOCATION_OR_VIDEO;
                    } else {
                        return INCOMING_MESSAGE_WITH_PHOTO_LOCATION_OR_VIDEO;
                    }
                case AppConstants.ATTACHMENT_TYPE_CONTACT:
                    if (senderId.equals(signedInUser.getObjectId())) {
                        return OUTGOING_MESSAGE_WITH_CONTACT;
                    } else {
                        return INCOMING_MESSAGE_WITH_CONTACT;
                    }
                case AppConstants.ATTACHMENT_TYPE_DOCUMENT:
                    if (senderId.equals(signedInUser.getObjectId())) {
                        return OUTGOING_MESSAGE_WITH_DOCUMENT_OR_OTHER_FILE;
                    } else {
                        return INCOMING_MESSAGE_WITH_DOCUMENT_OR_OTHER_FILE;
                    }
                case AppConstants.ATTACHMENT_TYPE_MUSIC:
                case AppConstants.ATTACHEMENT_TYPE_VOICE_NOTE:
                    if (senderId.equals(signedInUser.getObjectId())) {
                        return OUTGOING_MESSAGE_WITH_AUDIO;
                    } else {
                        return INCOMING_MESSAGE_WITH_AUDIO;
                    }
            }
        } else {
            String messageBody = messageObject.getString(AppConstants.MESSAGE_BODY);
            List<String> links = UiUtils.pullLinks(messageBody);
            if (!links.isEmpty()) {
                if (senderId.equals(signedInUser.getObjectId())) {
                    return OUTGOING_MESSAGE_WITH_LINK_PREVIEW;
                } else {
                    return INCOMING_MESSAGE_WITH_LINK_PREVIEW;
                }
            } else {
                if (senderId.equals(signedInUser.getObjectId())) {
                    return OUTGOING_MESSAGE_TEXT_ONLY;
                } else {
                    return INCOMING_MESSAGE_TEXT_ONLY;
                }
            }
        }
        return -1;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
        MessageItemsHolder messageItemsHolder = (MessageItemsHolder) holder;
        ParseObject messageObject = messages.get(position);
        if (messageObject != null) {
            messageItemsHolder.bindMessage(context, messageObject);
        }
    }

    @Override
    public String getHeaderId(int position) {
        ParseObject parseObject = messages.get(position);
        Date createdAt = parseObject.getCreatedAt();
        if (createdAt == null) {
            createdAt = new Date();
        }
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
        ParseObject message = messages.get(position);
        if (message != null) {
            Date createdAT = message.getCreatedAt();
            if (createdAT == null) {
                createdAT = new Date();
            }
            holder.bindHeader(DateUtils.getRelativeDate(context, Locale.getDefault(), createdAT.getTime()));
        }
    }

    @Override
    public int getItemCount() {
        return messages != null ? messages.size() : 0;
    }

    @SuppressWarnings("WeakerAccess")
    static class MessageItemsHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.conversation_message_view)
        ConversationMessageView conversationMessageView;

        public MessageItemsHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        public void bindMessage(Activity context, ParseObject parseObject) {
            conversationMessageView.bindData(context, parseObject);
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
