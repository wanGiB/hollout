package com.wan.hollout.eventbuses;

/**
 * @author Wan Clem
 */

public class GifMessageEvent {

    private String gifUrl;

    public GifMessageEvent(String gifUrl) {
        this.gifUrl = gifUrl;
    }

    public String getGifUrl() {
        return gifUrl;
    }

}
