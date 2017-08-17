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
public class MeetPoint extends BaseModel {

    @PrimaryKey
    @Column
    public String senderId;

    @Column
    public String meetPoint;

    public void setMeetPoint(String meetPoint) {
        this.meetPoint = meetPoint;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getMeetPoint() {
        return meetPoint;
    }

    public String getSenderId() {
        return senderId;
    }

}
