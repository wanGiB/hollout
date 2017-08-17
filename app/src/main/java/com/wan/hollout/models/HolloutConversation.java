package com.wan.hollout.models;

import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.structure.BaseModel;
import com.wan.hollout.db.HolloutDb;

/**
 * @author Wan Clem
 */

@Table(database = HolloutDb.class)
public class HolloutConversation extends BaseModel {

    @PrimaryKey
    @Column
    public String conversationId;

    @Column
    public String conversationName;

    @Column
    public String conversationPhoto;

    @Column
    public String lastMessage;

    @Column
    public String lastMessageTime;

    @Column
    public String deliveryStatus;

    @Column
    public boolean online;

    public String getConversationId() {
        return conversationId;
    }

    public String getConversationName() {
        return conversationName;
    }

    public String getConversationPhoto() {
        return conversationPhoto;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public String getLastMessageTime() {
        return lastMessageTime;
    }

    public String getDeliveryStatus() {
        return deliveryStatus;
    }

    public boolean isOnline() {
        return online;
    }
}
