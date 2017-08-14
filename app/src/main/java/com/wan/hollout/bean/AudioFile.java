package com.wan.hollout.bean;

import android.support.annotation.NonNull;

/**
 * Created by Wan on 4/2/2016.
 */
public class AudioFile implements Comparable<AudioFile> {
    public int audioId;
    public String author;
    public String title;
    public String path;
    public long duration;
    public String genre;
    public boolean selected;

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public boolean isSelected() {
        return selected;
    }

    public int getAudioId() {
        return audioId;
    }

    public void setAudioId(int audioId) {
        this.audioId = audioId;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public String getGenre() {
        return genre;
    }

    public void setGenre(String genre) {
        this.genre = genre;
    }

    @Override
    public int compareTo(@NonNull AudioFile another) {
        return this.getTitle().compareTo(another.getTitle());
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }
        if (!(o instanceof AudioFile)) {
            return false;
        }

        AudioFile audioFile = (AudioFile) o;
        String otherAudioFilePath = audioFile.getPath();
        String currentAudioFilePath = getPath();
        return currentAudioFilePath.equals(otherAudioFilePath);
    }

    @Override
    public int hashCode() {
        int result;
        final String name = getClass().getName();
        String currentAudioFilePath = getPath();
        String currentAudioFileName = getTitle();

        result = (currentAudioFilePath != null ? currentAudioFilePath.hashCode() : 0);
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (currentAudioFileName != null ? currentAudioFileName.hashCode() : 0);

        return result;
    }

}
