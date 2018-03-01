package com.wan.hollout.utils;

/**
 * @author Wan Clem
 */

public class CallStateTracker {

    private boolean isOutgoing = false;
    private boolean wasAnswered = false;
    private boolean wasRejected = false;
    private boolean wasRinging = false;

    private String callerId;

    private static CallStateTracker instance;

    private CallStateTracker() {

    }

    public static CallStateTracker getInstance() {
        if (instance == null) {
            instance = new CallStateTracker();
        }
        return instance;
    }

    public void setCallerId(String callerId) {
        this.callerId = callerId;
    }

    public void setWasRejected(boolean wasRejected) {
        this.wasRejected = wasRejected;
    }

    public void setWasRinging(boolean wasRinging) {
        this.wasRinging = wasRinging;
    }

    public boolean wasRinging() {
        return wasRinging;
    }

    public String getCallerId() {
        return callerId;
    }

    public void setOutgoing(boolean outgoing) {
        isOutgoing = outgoing;
    }

    public void setWasAnswered(boolean wasAnswered) {
        this.wasAnswered = wasAnswered;
    }

    public boolean isOutgoing() {
        return isOutgoing;
    }

    public boolean wasAnswered() {
        return wasAnswered;
    }

    public boolean wasRejected() {
        return wasRejected;
    }

    public void purgeMessage() {
        String callerId = getCallerId();
        boolean isOutgoing = isOutgoing();
        boolean answered = wasAnswered();
        boolean rejected = wasRejected();
        boolean wasRinging = wasRinging();

        HolloutLogger.d("CallLogs", "CallerId = " + callerId + ", " +
                "IsOutgoing = " + isOutgoing + ", Answered = " + answered + "," +
                " Rejected = " + rejected + ", WasRinging = " + wasRinging);
    }

}
