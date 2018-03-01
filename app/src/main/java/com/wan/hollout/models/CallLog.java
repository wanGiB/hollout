package com.wan.hollout.models;

import com.raizlabs.android.dbflow.annotation.Column;
import com.raizlabs.android.dbflow.annotation.PrimaryKey;
import com.raizlabs.android.dbflow.annotation.Table;
import com.raizlabs.android.dbflow.structure.BaseModel;
import com.wan.hollout.database.HolloutDb;

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

    public String getCallId() {
        return callId;
    }

    public void setCallId(String callId) {
        this.callId = callId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public int getCallStatus() {
        return callStatus;
    }

    public void setCallStatus(int callStatus) {
        this.callStatus = callStatus;
    }

    public boolean isVoiceCall() {
        return voiceCall;
    }

    public void setVoiceCall(boolean voiceCall) {
        this.voiceCall = voiceCall;
    }

    public boolean isIncoming() {
        return incoming;
    }

    public void setIncoming(boolean incoming) {
        this.incoming = incoming;
    }

    public boolean isCallTime() {
        return callTime;
    }

    public void setCallTime(boolean callTime) {
        this.callTime = callTime;
    }

    public String getPartyId() {
        return partyId;
    }

    public void setPartyId(String partyId) {
        this.partyId = partyId;
    }

    public String getPartyPhotoUrl() {
        return partyPhotoUrl;
    }

    public void setPartyPhotoUrl(String partyPhotoUrl) {
        this.partyPhotoUrl = partyPhotoUrl;
    }

    public String getPartyName() {
        return partyName;
    }

    public void setPartyName(String partyName) {
        this.partyName = partyName;
    }
}
