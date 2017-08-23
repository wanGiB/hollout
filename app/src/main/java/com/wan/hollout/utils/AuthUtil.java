package com.wan.hollout.utils;

import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;

import com.facebook.login.LoginManager;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.parse.DeleteCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.SaveCallback;
import com.wan.hollout.callbacks.DoneCallback;

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

        // Facebook sign out
        try {
            LoginManager.getInstance().logOut();
        } catch (NoClassDefFoundError e) {
            // do nothing
        }
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

    public static void updateCurrentLocalUser(final ParseObject updatableProps, final DoneCallback<Boolean>successCallback) {
        updatableProps.pinInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                updateRemoteUserVariant(updatableProps, updatableProps.getString(AppConstants.REAL_OBJECT_ID),successCallback);
            }
        });
    }

    public static void createLocalUser(ParseObject remoteObject) {
        ParseObject newLocalObject = new ParseObject(AppConstants.PEOPLE_GROUPS_AND_ROOMS);
        for (String key : remoteObject.keySet()) {
            if (key.equals(AppConstants.OBJECT_ID)) {
                newLocalObject.put(AppConstants.MASKED_OBJECT_ID, remoteObject.get(AppConstants.OBJECT_ID));
            } else {
                newLocalObject.put(key, remoteObject.get(key));
            }
        }
        newLocalObject.pinInBackground(AppConstants.AUTHENTICATED_USER_DETAILS);
    }

    private static void updateRemoteUserVariant(final ParseObject updatableProps, String realObjectId, final DoneCallback<Boolean> successCallback) {
        ParseQuery<ParseObject> personQuery = ParseQuery.getQuery(AppConstants.PEOPLE_GROUPS_AND_ROOMS);
        personQuery.whereEqualTo(AppConstants.REAL_OBJECT_ID, realObjectId);
        personQuery.getFirstInBackground(new GetCallback<ParseObject>() {
            @Override
            public void done(final ParseObject object, ParseException e) {
                if (object != null) {
                    for (String key : updatableProps.keySet()) {
                        if (!key.equals(AppConstants.OBJECT_ID)) {
                            object.put(key, updatableProps.get(key));
                        }
                    }
                    object.saveInBackground(new SaveCallback() {
                        @Override
                        public void done(ParseException e) {
                            successCallback.done(true,null);
                            if (e != null) {
                                object.saveEventually();
                            }
                        }
                    });
                }
            }
        });
    }

    public static void dissolveAuthenticatedUser(final DoneCallback<Boolean> dissolutionCallback) {
        final ParseObject localObject = getCurrentUser();
        if (localObject != null) {
            localObject.unpinInBackground(AppConstants.AUTHENTICATED_USER_DETAILS,new DeleteCallback() {
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
    }

}
