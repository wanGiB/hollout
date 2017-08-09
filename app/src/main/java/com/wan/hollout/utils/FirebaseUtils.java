package com.wan.hollout.utils;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

/**
 * @author Wan Clem
 */

public class FirebaseUtils {

    private static DatabaseReference getRootRef() {
        return FirebaseDatabase.getInstance().getReference();
    }

    public static DatabaseReference getUsersReference() {
        return getRootRef().child(AppConstants.USERS);
    }

    public static DatabaseReference getLabelsReference() {
        return getRootRef().child(AppConstants.LABELS);
    }

    public static DatabaseReference getLikesReference(String postId) {
        return getRootRef().child(AppConstants.FEED_LIKES + "/" + postId);
    }

    public static DatabaseReference getViewsReference(String postId) {
        return getRootRef().child(AppConstants.FEED_VIEWS + "/" + postId);
    }

}
