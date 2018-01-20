package com.wan.hollout.eventbuses;

/**
 * @author Wan Clem
 */

public class PlaceCallEvent {

    private String phoneNumber;

    public PlaceCallEvent(String phoneNumber){
        this.phoneNumber = phoneNumber;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

}
