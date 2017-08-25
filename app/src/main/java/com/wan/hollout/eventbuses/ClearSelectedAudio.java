package com.wan.hollout.eventbuses;

/**
 * @author Wan Clem
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
