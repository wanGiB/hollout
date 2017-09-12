package com.wan.hollout.eventbuses;

/**
 * @author Wan Clem
 */

public class CloseGifWindow {

    private boolean closeWindow;

    public CloseGifWindow(boolean closeWindow) {
        this.closeWindow = closeWindow;
    }

    public boolean canCloseWindow(){
        return closeWindow;
    }

}
