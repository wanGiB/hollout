package com.wan.hollout.ui.utils;

import android.annotation.SuppressLint;

import com.wan.hollout.clients.ChatClient;
import com.wan.hollout.enums.MessageStatus;
import com.wan.hollout.interfaces.DoneCallback;
import com.wan.hollout.models.ChatMessage;
import com.wan.hollout.utils.DbUtils;
import com.wan.hollout.utils.FirebaseUtils;

/**
 * @author Wan Clem
 */
@SuppressLint("StaticFieldLeak")
public class FileUploader {

    private static FileUploader instance;

    public static FileUploader getInstance() {
        if (instance == null) {
            instance = new FileUploader();
        }
        return instance;
    }

    public void uploadFile(String filePath, String directory, final String messageId) {
        FirebaseUtils.uploadFileAsync(filePath, directory, messageId, false, new DoneCallback<String>() {
            @Override
            public void done(String result, Exception e) {
                if (e == null && result != null) {
                    ChatMessage chatMessage = DbUtils.getMessage(messageId);
                    if (chatMessage != null) {
                        chatMessage.setRemoteUrl(result);
                        chatMessage.setFileUploadProgress(100);
                        ChatClient.getInstance().sendMessage(chatMessage);
                    }
                } else {
                    if (e != null) {
                        ChatMessage chatMessage = DbUtils.getMessage(messageId);
                        if (chatMessage != null) {
                            chatMessage.setMessageStatus(MessageStatus.FAILED);
                            DbUtils.updateMessage(chatMessage);
                        }
                    }
                }
            }
        });
    }

    public void uploadVideoThumbnailAndProceed(String thumbnailPath, final String filePath, String thumbnailDirectory, final String videoDirectory, final String messageId) {
        FirebaseUtils.uploadFileAsync(thumbnailPath, thumbnailDirectory, messageId, true, new DoneCallback<String>() {
            @Override
            public void done(String result, Exception e) {
                if (e == null && result != null) {
                    ChatMessage chatMessage = DbUtils.getMessage(messageId);
                    if (chatMessage != null) {
                        chatMessage.setThumbnailUrl(result);
                        DbUtils.updateMessage(chatMessage);
                        uploadFile(filePath, videoDirectory, messageId);
                    }
                } else {
                    if (e != null) {
                        ChatMessage chatMessage = DbUtils.getMessage(messageId);
                        if (chatMessage != null) {
                            chatMessage.setMessageStatus(MessageStatus.FAILED);
                            DbUtils.updateMessage(chatMessage);
                        }
                    }
                }
            }
        });
    }

}
