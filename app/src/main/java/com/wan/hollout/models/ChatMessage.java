package com.wan.hollout.models;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import com.google.gson.annotations.Expose;
import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.ConflictAction;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.wan.hollout.converters.MessageDirectionConverter;
import com.wan.hollout.converters.MessageStatusConverter;
import com.wan.hollout.converters.MessageTypeConverter;
import com.wan.hollout.db.HolloutDb;
import com.wan.hollout.enums.MessageDirection;
import com.wan.hollout.enums.MessageStatus;
import com.wan.hollout.enums.MessageType;

import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * @author Wan Clem
 */
@SuppressWarnings("WeakerAccess")
@Table(database = HolloutDb.class,
        primaryKeyConflict = ConflictAction.REPLACE,
        insertConflict = ConflictAction.REPLACE,
        updateConflict = ConflictAction.REPLACE)
public class ChatMessage implements Comparable<ChatMessage>, Parcelable {

    @PrimaryKey
    @Column
    @Expose
    public String messageId;

    @Column(typeConverter = MessageStatusConverter.class)
    @Expose
    public MessageStatus messageStatus;

    @Column(typeConverter = MessageDirectionConverter.class)
    @Expose
    public MessageDirection messageDirection;

    @Column(typeConverter = MessageTypeConverter.class)
    @Expose
    public MessageType messageType;

    @Column
    @Expose
    public String from;

    @Column
    @Expose
    public String fromName;

    @Column
    @Expose
    public String fromPhotoUrl;

    @Column
    @Expose
    public String to;

    @Column
    @Expose
    public String conversationId;

    @Column
    @Expose
    public boolean acknowledged;

    @Column
    @Expose
    public boolean listened;

    @Column
    @Expose
    public long timeStamp;

    @Column
    @Expose
    public String messageBody;

    @Column
    @Expose
    public String fileCaption;

    @Column
    @Expose
    public String documentName;

    @Column
    @Expose
    public String documentSize;

    @Column
    @Expose
    public String repliedMessageId;

    @Column
    @Expose
    public String locationAddress;

    @Column
    @Expose
    public String latitude;

    @Column
    @Expose
    public String longitude;

    @Column
    @Expose
    public String localUrl;

    @Column
    @Expose
    public String remoteUrl;

    @Column
    @Expose
    public String reactionValue;

    @Column
    @Expose
    public String contactName;

    @Column
    @Expose
    public String contactNumber;

    @Column
    @Expose
    public String audioDuration;

    @Column
    @Expose
    public long videoDuration;

    @Column
    @Expose
    public String gifUrl;

    @Column
    @Expose
    public String thumbnailUrl;

    @Column
    @Expose
    public String localThumb;

    @Column
    @Expose
    public double fileUploadProgress;

    @Column
    @Expose
    public String fileMimeType;

    public ChatMessage() {

    }

    protected ChatMessage(Parcel in) {
        messageId = in.readString();
        from = in.readString();
        to = in.readString();
        conversationId = in.readString();
        acknowledged = in.readByte() != 0;
        listened = in.readByte() != 0;
        timeStamp = in.readLong();
        messageBody = in.readString();
        fileCaption = in.readString();
        documentName = in.readString();
        documentSize = in.readString();
        repliedMessageId = in.readString();
        locationAddress = in.readString();
        latitude = in.readString();
        longitude = in.readString();
        localUrl = in.readString();
        remoteUrl = in.readString();
        reactionValue = in.readString();
        contactName = in.readString();
        contactNumber = in.readString();
        audioDuration = in.readString();
        videoDuration = in.readLong();
        gifUrl = in.readString();
        thumbnailUrl = in.readString();
        localThumb = in.readString();
        fileUploadProgress = in.readDouble();
        fileMimeType = in.readString();
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public void setMessageDirection(MessageDirection messageDirection) {
        this.messageDirection = messageDirection;
    }

    public void setMessageType(MessageType messageType) {
        this.messageType = messageType;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public void setFromName(String fromName) {
        this.fromName = fromName;
    }

    public void setFromPhotoUrl(String fromPhotoUrl) {
        this.fromPhotoUrl = fromPhotoUrl;
    }

    public String getFromPhotoUrl() {
        return fromPhotoUrl;
    }

    public void setTo(String to) {
        this.to = to;
    }

    public void setConversationId(String conversationId) {
        this.conversationId = conversationId;
    }

    public void setAcknowledged(boolean acknowledged) {
        this.acknowledged = acknowledged;
    }

    public void setListened(boolean listened) {
        this.listened = listened;
    }

    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }

    public void setMessageBody(String messageBody) {
        this.messageBody = messageBody;
    }

    public void setFileCaption(String fileCaption) {
        this.fileCaption = fileCaption;
    }

    public void setDocumentName(String documentName) {
        this.documentName = documentName;
    }

    public void setDocumentSize(String documentSize) {
        this.documentSize = documentSize;
    }

    public void setRepliedMessageId(String repliedMessageId) {
        this.repliedMessageId = repliedMessageId;
    }

    public void setLocationAddress(String locationAddress) {
        this.locationAddress = locationAddress;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }

    public void setLocalUrl(String localUrl) {
        this.localUrl = localUrl;
    }

    public void setRemoteUrl(String remoteUrl) {
        this.remoteUrl = remoteUrl;
    }

    public void setReactionValue(String reactionValue) {
        this.reactionValue = reactionValue;
    }

    public void setContactName(String contactName) {
        this.contactName = contactName;
    }

    public void setContactNumber(String contactNumber) {
        this.contactNumber = contactNumber;
    }

    public void setAudioDuration(String audioDuration) {
        this.audioDuration = audioDuration;
    }

    public void setGifUrl(String gifUrl) {
        this.gifUrl = gifUrl;
    }

    public void setThumbnailUrl(String thumbnailUrl) {
        this.thumbnailUrl = thumbnailUrl;
    }

    public void setLocalThumb(String localThumb) {
        this.localThumb = localThumb;
    }

    public void setFileMimeType(String fileMimeType) {
        this.fileMimeType = fileMimeType;
    }

    public double getFileUploadProgress() {
        return fileUploadProgress;
    }

    public void setFileUploadProgress(double fileUploadProgress) {
        this.fileUploadProgress = fileUploadProgress;
    }

    public String getFromName() {
        return fromName;
    }

    public static final Creator<ChatMessage> CREATOR = new Creator<ChatMessage>() {
        @Override
        public ChatMessage createFromParcel(Parcel in) {
            return new ChatMessage(in);
        }

        @Override
        public ChatMessage[] newArray(int size) {
            return new ChatMessage[size];
        }
    };

    public long getVideoDuration() {
        return videoDuration;
    }

    public String getLocalThumb() {
        return localThumb;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }

    public String getMessageId() {
        return messageId;
    }

    public MessageStatus getMessageStatus() {
        return messageStatus;
    }

    public MessageDirection getMessageDirection() {
        return messageDirection;
    }

    public MessageType getMessageType() {
        return messageType;
    }

    public String getFrom() {
        return from;
    }

    public String getTo() {
        return to;
    }

    public String getConversationId() {
        return conversationId;
    }

    public boolean isAcknowledged() {
        return acknowledged;
    }

    public boolean isListened() {
        return listened;
    }

    public String getMessageBody() {
        return messageBody;
    }

    public String getFileCaption() {
        return fileCaption;
    }

    public void setVideoDuration(long videoDuration) {
        this.videoDuration = videoDuration;
    }

    public String getDocumentName() {
        return documentName;
    }

    public String getDocumentSize() {
        return documentSize;
    }

    public String getRepliedMessageId() {
        return repliedMessageId;
    }

    public String getLatitude() {
        return latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public String getLocationAddress() {
        return locationAddress;
    }

    public String getLocalUrl() {
        return localUrl;
    }

    public String getRemoteUrl() {
        return remoteUrl;
    }

    public String getReactionValue() {
        return reactionValue;
    }

    public String getContactName() {
        return contactName;
    }

    public String getContactNumber() {
        return contactNumber;
    }

    public String getAudioDuration() {
        return audioDuration;
    }

    public String getGifUrl() {
        return gifUrl;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public String getFileMimeType() {
        return fileMimeType;
    }

    @Override
    public int compareTo(@NonNull ChatMessage another) {
        return Long.valueOf(timeStamp).compareTo(another.getTimeStamp());
    }

    @Override
    public int hashCode() {
        int result;
        result = this.messageId.hashCode();
        final String name = getClass().getName();
        result = 31 * result + (name != null ? name.hashCode() : 0);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        if (obj.getClass() != getClass()) {
            return false;
        }
        ChatMessage another = (ChatMessage) obj;
        return this.getMessageId().equals(another.getMessageId());
    }

    public void setMessageStatus(MessageStatus messageStatus) {
        this.messageStatus = messageStatus;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(messageId);
        dest.writeString(from);
        dest.writeString(to);
        dest.writeString(conversationId);
        dest.writeByte((byte) (acknowledged ? 1 : 0));
        dest.writeByte((byte) (listened ? 1 : 0));
        dest.writeLong(timeStamp);
        dest.writeString(messageBody);
        dest.writeString(fileCaption);
        dest.writeString(documentName);
        dest.writeString(documentSize);
        dest.writeString(repliedMessageId);
        dest.writeString(locationAddress);
        dest.writeString(latitude);
        dest.writeString(longitude);
        dest.writeString(localUrl);
        dest.writeString(remoteUrl);
        dest.writeString(reactionValue);
        dest.writeString(contactName);
        dest.writeString(contactNumber);
        dest.writeString(audioDuration);
        dest.writeString(gifUrl);
        dest.writeString(thumbnailUrl);
        dest.writeString(localThumb);
        dest.writeLong(videoDuration);
        dest.writeDouble(fileUploadProgress);
        dest.writeString(fileMimeType);
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this);
    }

}
