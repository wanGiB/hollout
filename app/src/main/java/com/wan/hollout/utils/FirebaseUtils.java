package com.wan.hollout.utils;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

/**
 *@author Wan Clem
 */

public class FirebaseUtils {

    private static DatabaseReference getRootRef(){
        return FirebaseDatabase.getInstance().getReference();
    }

    public static DatabaseReference getUsersReference(){
        return getRootRef().child(AppConstants.USERS);
    }
}
