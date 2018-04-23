package com.wan.hollout.utils;

import android.graphics.Bitmap;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.provider.MediaStore;

import com.wan.hollout.components.ApplicationLoader;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStream;

/**
 * @author Wan Clem
 */

public class StoriesFileUploader {

    public static void captureFileForUpload(Uri fileUri, String parentObjectId, String storyObjectId) {
        String filePath = fileUri.getPath();
        String fileMiMeType = FileUtils.getMimeType(filePath);
        boolean isVideo = HolloutUtils.isVideo(fileMiMeType);
        if (isVideo) {
            Bitmap videoThumb = ThumbnailUtils.createVideoThumbnail(filePath, MediaStore.Images.Thumbnails.MINI_KIND);
            File thumbOutPutFile = HolloutUtils.getFilePath(System.currentTimeMillis() + "VideoThumb",
                    ApplicationLoader.getInstance(),
                    "thumb", true);
            try {
                OutputStream outputStream = new FileOutputStream(thumbOutPutFile);
                videoThumb.compress(Bitmap.CompressFormat.PNG, 100, outputStream);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

        }
        
    }

}
