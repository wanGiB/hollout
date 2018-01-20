package com.wan.hollout.eventbuses;

/**
 * @author Wan Clem
 */

public class PlaceLocalCallEvent {

    private String phoneNumber;

    public PlaceLocalCallEvent(String phoneNumber){
        this.phoneNumber = phoneNumber;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

}
