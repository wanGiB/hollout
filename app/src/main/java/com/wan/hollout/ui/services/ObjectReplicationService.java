package com.wan.hollout.ui.services;

import android.app.IntentService;
import android.content.Intent;
import android.support.annotation.Nullable;

import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.wan.hollout.utils.AppConstants;

import java.util.Set;

/**
 * @author Wan Clem
 */

public class ObjectReplicationService extends IntentService {

    private ParseUser signedInUser;

    public ObjectReplicationService() {
        super("ObjectReplicationService");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        initSignedInUser();
    }

    private void initSignedInUser() {
        if (signedInUser == null) {
            signedInUser = ParseUser.getCurrentUser();
        }
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        initSignedInUser();
        if (intent != null) {
            replicateObject();
        }
    }

    private void replicateObject() {
        if (signedInUser != null) {
            final String signedInUserObjectId = signedInUser.getObjectId();
            ParseQuery<ParseObject> replicableObjectQuery = ParseQuery.getQuery(AppConstants.PEOPLE_AND_GROUPS);
            replicableObjectQuery.whereEqualTo(AppConstants.REPLICATED_OBJECT_ID, signedInUserObjectId);
            replicableObjectQuery.getFirstInBackground(new GetCallback<ParseObject>() {
                @Override
                public void done(ParseObject object, ParseException e) {
                    if (e == null && object != null) {
                        performObjectReplication(object);
                    } else {
                        ParseObject newReplicableObject = new ParseObject(AppConstants.PEOPLE_AND_GROUPS);
                        newReplicableObject.put(AppConstants.REPLICATED_OBJECT_ID, signedInUserObjectId);
                        newReplicableObject.put(AppConstants.OBJECT_TYPE, AppConstants.OBJECT_TYPE_INDIVIDUAL);
                        performObjectReplication(newReplicableObject);
                    }
                }
            });
        }
    }

    private void performObjectReplication(ParseObject newReplicableObject) {
        Set<String> keys = signedInUser.keySet();
        if (!keys.isEmpty()) {
            for (String key : keys) {
                if (!key.equals(AppConstants.OBJECT_ID)) {
                    newReplicableObject.put(key, signedInUser.get(key));
                }
            }
            newReplicableObject.saveInBackground();
        }
    }

}
