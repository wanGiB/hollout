package com.wan.hollout.eventbuses;

import android.net.Uri;

/**
 * @author Wan Clem
 */

public class SelectedFileUriEvent {
    public Uri uri;

    public SelectedFileUriEvent(Uri uri) {
        this.uri = uri;
    }

    public Uri getUri() {
        return uri;
    }

}
