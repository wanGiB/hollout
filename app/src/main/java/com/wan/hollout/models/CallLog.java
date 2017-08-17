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
public class CallLog extends BaseModel {

    @PrimaryKey
    @Column
    public String callId;

    @Column
    public String content;

    @Column
    public int callStatus;

    @Column
    public boolean voiceCall;

    @Column
    public boolean incoming;

    @Column
    public boolean callTime;

    @Column
    public String partyId;

    @Column
    public String partyPhotoUrl;

    @Column
    public String partyName;

}
