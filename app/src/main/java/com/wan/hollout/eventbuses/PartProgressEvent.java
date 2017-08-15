package com.wan.hollout.eventbuses;


import android.support.annotation.NonNull;


public class PartProgressEvent {

  public final String attachment;
  public final long       total;
  public final long       progress;

  public PartProgressEvent(@NonNull String attachment, long total, long progress) {
    this.attachment = attachment;
    this.total      = total;
    this.progress   = progress;
  }
}
