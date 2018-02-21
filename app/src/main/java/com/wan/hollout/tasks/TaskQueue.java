package com.wan.hollout.tasks;

import com.google.firebase.storage.UploadTask;

import java.util.HashMap;

/**
 * @author Wan Clem
 */

public class TaskQueue {

    private static HashMap<String, UploadTask> uploadQueue = new HashMap<>();

    public static TaskQueue getInstance() {
        return new TaskQueue();
    }

    public boolean checkTaskQueue(String key) {
        return uploadQueue.containsKey(key);
    }

    public void popTask(String key) {
        if (checkTaskQueue(key)) {
            uploadQueue.remove(key);
        }
    }

    public void offerNewTask(String key, UploadTask uploadTask) {
        uploadQueue.put(key, uploadTask);
    }

    public UploadTask getTask(String currentLocalFilePath) {
        return uploadQueue.get(currentLocalFilePath);
    }

}
