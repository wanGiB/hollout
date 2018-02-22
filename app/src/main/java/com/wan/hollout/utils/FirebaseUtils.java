package com.wan.hollout.utils;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.wan.hollout.eventbuses.FileUploadProgressEvent;
import com.wan.hollout.interfaces.DoneCallback;
import com.wan.hollout.tasks.TaskQueue;

import org.greenrobot.eventbus.EventBus;

import java.io.File;

/**
 * @author Wan Clem
 */

@SuppressWarnings({"WeakerAccess", "UnusedReturnValue"})
public class FirebaseUtils {

    private static String TAG = FirebaseUtils.class.getSimpleName();

    private static DatabaseReference getRootRef() {
        return FirebaseDatabase.getInstance().getReference();
    }

    public static DatabaseReference getUsersReference() {
        return getRootRef().child(AppConstants.USERS);
    }

    public static StorageReference getFirebaseStorageReference() {
        return FirebaseStorage.getInstance().getReferenceFromUrl(AppConstants.HOLLOUT_FILES_BUCKET);
    }

    public static DatabaseReference getMessageDeliveryStatus() {
        return getRootRef().child(AppConstants.MESSAGE_DELIVERY_STATUS);
    }

    public static UploadTask uploadFileAsync(final String filePath, String directory, final String uniqueIdOfProcessInitiator, final boolean fromThumbnail,
                                             @Nullable final DoneCallback<String> doneCallback) {
        Uri uri = Uri.fromFile(new File(filePath));
        StorageReference storageReference = FirebaseUtils.getFirebaseStorageReference().child(directory).child(uri.getLastPathSegment());
        final UploadTask uploadTask = storageReference.putFile(uri.normalizeScheme());
        if (uploadTask.isPaused()) {
            uploadTask.resume();
        }
        uploadTask.addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
            @SuppressWarnings("unused")
            @Override
            public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                double progress = (100.0 * taskSnapshot.getBytesTransferred()) / taskSnapshot.getTotalByteCount();
                if (uniqueIdOfProcessInitiator != null) {
                    FileUploadProgressEvent fileUploadProgressEvent = new FileUploadProgressEvent(progress, fromThumbnail, filePath, uniqueIdOfProcessInitiator);
                    EventBus.getDefault().post(fileUploadProgressEvent);
                }
                HolloutLogger.d("UploadProgress", (int) progress + "");
            }
        }).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                if (taskSnapshot.getDownloadUrl() != null) {
                    String returnedFileUrl = taskSnapshot.getDownloadUrl().toString();
                    FileUploadProgressEvent fileUploadProgressEvent = new FileUploadProgressEvent(100, fromThumbnail, filePath, uniqueIdOfProcessInitiator);
                    EventBus.getDefault().post(fileUploadProgressEvent);
                    HolloutLogger.d(TAG, "Completed Upload of file = " + filePath + " with upload url = " + returnedFileUrl);
                    if (TaskQueue.getInstance().checkTaskQueue(filePath)) {
                        TaskQueue.getInstance().popTask(filePath);
                    }
                    if (doneCallback != null) {
                        doneCallback.done(returnedFileUrl, null);
                    }
                }
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                uploadTask.pause();
                if (TaskQueue.getInstance().checkTaskQueue(filePath)) {
                    TaskQueue.getInstance().popTask(filePath);
                }
                HolloutLogger.e(TAG, "An error occurred while uploading file. Error message = " + e.getMessage());
                if (doneCallback != null) {
                    doneCallback.done(null, e);
                }
            }
        });
        return uploadTask;
    }

}
