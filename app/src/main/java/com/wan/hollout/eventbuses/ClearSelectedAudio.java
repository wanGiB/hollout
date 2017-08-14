package com.wan.hollout.eventbuses;

/**
 * Created by wan on 1/4/17.
 */

public class ClearSelectedAudio {
    public boolean clearSelectedAudio;

    public ClearSelectedAudio(boolean clearSelectedAudio) {
        this.clearSelectedAudio = clearSelectedAudio;
    }

    public boolean canClearSelectedAudio() {
        return clearSelectedAudio;
    }

}
