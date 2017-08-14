package com.wan.hollout.eventbuses;

/**
 * Created by wan on 1/4/17.
 */
public class ClearSelectedPhotos {

    private boolean clear;

    public ClearSelectedPhotos(boolean clear) {
        this.clear = clear;
    }

    public boolean canClearSelectedPhotos() {
        return clear;
    }
    
}
