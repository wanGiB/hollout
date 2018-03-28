package com.wan.hollout.utils;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.parse.DeleteCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.SaveCallback;
import com.wan.hollout.interfaces.DoneCallback;

/**
 * @author Wan Clem
 */

public class AuthUtil {

    public static Task<Void> signOut(@NonNull FragmentActivity activity) {
        // Get Credentials Helper
        GoogleSignInHelper signInHelper = GoogleSignInHelper.getInstance(activity);
        // Disable credentials auto sign-in
        Task<Status> disableCredentialsTask = signInHelper.disableAutoSignIn();
        // Google sign out
        Task<Status> signOutTask = signInHelper.signOut();
        // Wait for all tasks to complete
        return Tasks.whenAll(disableCredentialsTask, signOutTask);
    }

    public static ParseObject getCurrentUser() {
        ParseQuery<ParseObject> currentUserQuery = ParseQuery.getQuery(AppConstants.PEOPLE_GROUPS_AND_ROOMS);
        currentUserQuery.fromPin(AppConstants.AUTHENTICATED_USER_DETAILS);
        try {
            return currentUserQuery.getFirst();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void updateCurrentLocalUser(final ParseObject updatableProps, @Nullable  final DoneCallback<Boolean> successCallback) {
        updatableProps.pinInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                updateRemoteUserVariant(updatableProps, updatableProps.getString(AppConstants.REAL_OBJECT_ID), successCallback);
            }
        });
    }

    public static void createLocalUser(ParseObject remoteObject) {
        ParseObject newLocalObject = new ParseObject(AppConstants.PEOPLE_GROUPS_AND_ROOMS);
        for (String key : remoteObject.keySet()) {
            if (!key.equals(AppConstants.OBJECT_ID)) {
                newLocalObject.put(key, remoteObject.get(key));
            }
        }
        newLocalObject.pinInBackground(AppConstants.AUTHENTICATED_USER_DETAILS);
    }

    private static void updateRemoteUserVariant(final ParseObject updatableProps, String realObjectId, @Nullable final DoneCallback<Boolean> successCallback) {
        ParseQuery<ParseObject> personQuery = ParseQuery.getQuery(AppConstants.PEOPLE_GROUPS_AND_ROOMS);
        personQuery.whereEqualTo(AppConstants.REAL_OBJECT_ID, realObjectId);
        personQuery.getFirstInBackground(new GetCallback<ParseObject>() {
            @Override
            public void done(final ParseObject updatableObject, ParseException e) {
                if (updatableObject != null) {
                    for (String key : updatableProps.keySet()) {
                        HolloutLogger.d("UpdatableUserKey", key);
                        if (!key.equals(AppConstants.OBJECT_ID)) {
                            updatableObject.put(key, updatableProps.get(key));
                        }
                    }
                    updatableObject.saveInBackground(new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            if (successCallback != null) {
                                successCallback.done(true, null);
                            }
                            if (e != null) {
                                updatableObject.saveEventually();
                            }
                        }
                    });
                }
            }
        });
    }

    public static void logOutAuthenticatedUser(final DoneCallback<Boolean> dissolutionCallback) {
        final ParseObject localObject = getCurrentUser();
        if (localObject != null) {
            localObject.put(AppConstants.APP_USER_ONLINE_STATUS, AppConstants.OFFLINE);
            localObject.put(AppConstants.APP_USER_LAST_SEEN, System.currentTimeMillis());
            updateRemoteUserVariant(localObject, localObject.getString(AppConstants.REAL_OBJECT_ID), new DoneCallback<Boolean>() {
                @Override
                public void done(Boolean success, Exception e) {
                    localObject.unpinInBackground(AppConstants.AUTHENTICATED_USER_DETAILS, new DeleteCallback() {
                        @Override
                        public void done(ParseException e) {
                            if (dissolutionCallback != null) {
                                if (e == null) {
                                    dissolutionCallback.done(true, null);
                                } else {
                                    dissolutionCallback.done(false, e);
                                }
                            }
                        }
                    });
                }
            });
        }
    }
}
