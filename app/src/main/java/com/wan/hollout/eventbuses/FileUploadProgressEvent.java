package com.wan.hollout.eventbuses;

/**
 * @author Wan Clem
 */

public class FileUploadProgressEvent {

    private double progress;
    private String filePath;
    private String pollableUniqueId;
    private boolean fromThumbnail;

    public FileUploadProgressEvent(double progress, boolean fromThumbnail, String filePath, String pollableUniqueId) {
        this.progress = progress;
        this.filePath = filePath;
        this.fromThumbnail = fromThumbnail;
        this.pollableUniqueId = pollableUniqueId;
    }

    public boolean isFromThumbnail() {
        return fromThumbnail;
    }

    public String getPollableUniqueId() {
        return pollableUniqueId;
    }

    public double getProgress() {
        return progress;
    }

    public String getFilePath() {
        return filePath;
    }

}
