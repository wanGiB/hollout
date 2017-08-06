package com.wan.hollout.eventbuses;

/**
 * @author Wan Clem
 */
public class ConnectivityChangedAction {
    private boolean connectivityChanged;

    public ConnectivityChangedAction(boolean connectivityChanged) {
        this.connectivityChanged = connectivityChanged;
    }

    public boolean isConnectivityChanged() {
        return connectivityChanged;
    }

}
