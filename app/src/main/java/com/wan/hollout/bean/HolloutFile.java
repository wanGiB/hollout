package com.wan.hollout.bean;

import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;

import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * * Created by Wan on 6/17/2016.
 */
public class HolloutFile implements Parcelable, Comparable<HolloutFile>, Serializable {

    public String localFilePath;
    public String remoteFilePath;
    public String mediaThumb;
    public String fileType;
    public String fileKey;
    public Drawable placeHolder;
    public String remoteFilePathPlaceHolder;
    public int sourceWidth;
    public int sourceHeight;
    public String cappFileCaption;
    public String fileName;
    public long fileDate;
    public long fileDuration;
    public Uri fileUri;

    //Requires a no argument constructor for firebase mapping
    public HolloutFile() {

    }

    public HolloutFile(String filePath) {
        this.localFilePath = filePath;
    }

    protected HolloutFile(Parcel in) {
        localFilePath = in.readString();
        remoteFilePath = in.readString();
        mediaThumb = in.readString();
        fileType = in.readString();
        fileKey = in.readString();
        remoteFilePathPlaceHolder = in.readString();
        sourceWidth = in.readInt();
        sourceHeight = in.readInt();
        cappFileCaption = in.readString();
        fileName = in.readString();
        fileDate = in.readLong();
        fileDuration = in.readLong();
        fileUri = in.readParcelable(Uri.class.getClassLoader());
        holloutFiles = in.createTypedArrayList(HolloutFile.CREATOR);
        contactId = in.readString();
    }

    public static final Creator<HolloutFile> CREATOR = new Creator<HolloutFile>() {
        @Override
        public HolloutFile createFromParcel(Parcel in) {
            return new HolloutFile(in);
        }

        @Override
        public HolloutFile[] newArray(int size) {
            return new HolloutFile[size];
        }
    };

    public void setFileUri(Uri fileUri) {
        this.fileUri = fileUri;
    }

    public Uri getFileUri() {
        return fileUri;
    }

    public void setRemoteFilePathPlaceHolder(String remoteFilePathPlaceHolder) {
        this.remoteFilePathPlaceHolder = remoteFilePathPlaceHolder;
    }

    public String getRemoteFilePathPlaceHolder() {
        return remoteFilePathPlaceHolder;
    }

    public void setFileDuration(long fileDuration) {
        this.fileDuration = fileDuration;
    }

    public void setContactId(String contactId) {
        this.contactId = contactId;
    }

    public long getFileDuration() {
        return fileDuration;
    }

    public String getContactId() {
        return contactId;
    }

    public void setFileDate(long fileDate) {
        this.fileDate = fileDate;
    }

    public long getFileDate() {
        return fileDate;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFileName() {
        return fileName;
    }

    public ArrayList<HolloutFile> holloutFiles;
    private String contactId;

    public void setHolloutFiles(ArrayList<HolloutFile> holloutFiles) {
        this.holloutFiles = holloutFiles;
    }

    public ArrayList<HolloutFile> getHolloutFiles() {
        return holloutFiles;
    }

    public void setCappFileCaption(String cappFileCaption) {
        this.cappFileCaption = cappFileCaption;
    }

    public String getCappFileCaption() {
        return cappFileCaption;
    }

    public void setSourceHeight(int sourceHeight) {
        this.sourceHeight = sourceHeight;
    }

    public void setSourceWidth(int sourceWidth) {
        this.sourceWidth = sourceWidth;
    }

    public int getSourceHeight() {
        return sourceHeight;
    }

    public int getSourceWidth() {
        return sourceWidth;
    }

    public void setPlaceHolder(Drawable placeHolder) {
        this.placeHolder = placeHolder;
    }

    public Drawable getPlaceHolder() {
        return placeHolder;
    }

    public void setLocalFilePath(String localFilePath) {
        this.localFilePath = localFilePath;
    }

    public void setRemoteFilePath(String remoteFilePath) {
        this.remoteFilePath = remoteFilePath;
    }

    public String getFileKey() {
        return fileKey;
    }

    public String getFileType() {
        return fileType;
    }

    public String getLocalFilePath() {
        return localFilePath;
    }

    public String getRemoteFilePath() {
        return remoteFilePath;
    }

    public void setMediaThumb(String mediaThumb) {
        this.mediaThumb = mediaThumb;
    }

    public String getMediaThumb() {
        return mediaThumb;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    public void setFileKey(String fileKey) {
        this.fileKey = fileKey;
    }

    @Override
    public int compareTo(@NonNull HolloutFile another) {
        String lhsFile = this.remoteFilePath != null ? remoteFilePath : localFilePath;
        String rhsFile = another.remoteFilePath != null ? another.remoteFilePath : another.localFilePath;
        return rhsFile.compareTo(lhsFile);
    }

    @Override
    public int hashCode() {
        int result;
        result = StringUtils.isNotEmpty(this.remoteFilePath) ? this.remoteFilePath.hashCode() : this.localFilePath.hashCode();
        final String name = getClass().getName();
        result = 31 * result + (name != null ? name.hashCode() : 0);
        return result;
    }

    @Override
    public boolean equals(Object obj) {

        if (obj == null) {
            return false;
        }

        if (obj == this) {
            return true;
        }

        if (obj.getClass() != getClass()) {
            return false;
        }

        HolloutFile another = (HolloutFile) obj;

        return (StringUtils.isNotEmpty(this.remoteFilePath) && StringUtils.isNotEmpty(another.remoteFilePath)) ? (this.remoteFilePath.equals(another.remoteFilePath)) : (localFilePath.equals(another.localFilePath));

    }

    @Override
    public String toString() {
        return "localFilePath = " + localFilePath + "\n" +
                "remoteFilePath = " + remoteFilePath + "\n" +
                "fileKey = " + fileKey + "\n" +
                "fileType =" + fileType;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(localFilePath);
        dest.writeString(remoteFilePath);
        dest.writeString(mediaThumb);
        dest.writeString(fileType);
        dest.writeString(fileKey);
        dest.writeString(remoteFilePathPlaceHolder);
        dest.writeInt(sourceWidth);
        dest.writeInt(sourceHeight);
        dest.writeString(cappFileCaption);
        dest.writeString(fileName);
        dest.writeLong(fileDate);
        dest.writeLong(fileDuration);
        dest.writeParcelable(fileUri, flags);
        dest.writeTypedList(holloutFiles);
        dest.writeString(contactId);
    }
}
