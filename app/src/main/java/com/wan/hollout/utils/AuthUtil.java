package com.wan.hollout.utils;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;

import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.parse.DeleteCallback;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.SaveCallback;
import com.wan.hollout.interfaces.DoneCallback;

import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.List;

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
            ParseObject parseObject = currentUserQuery.getFirst();
            if (parseObject != null) {
                currentUserQuery.cancel();
            }
            return parseObject;
        } catch (ParseException e) {
            e.printStackTrace();
        }
        currentUserQuery.cancel();
        return null;
    }

    private static void runLegacyDataCleanUp(ParseObject updatableProps) {
        if (updatableProps.containsKey(AppConstants.INTERESTS)) {
            updatableProps.remove(AppConstants.INTERESTS);
        }
        List<String> aboutUser = updatableProps.getList(AppConstants.ABOUT_USER);
        String classification = updatableProps.getString(AppConstants.CLASSIFICATION);
        if (aboutUser != null) {
            if (classification != null) {
                if (aboutUser.contains(classification)) {
                    aboutUser.remove(classification);
                }
                updatableProps.remove(AppConstants.CLASSIFICATION);
            }
        }
    }

    public static void createLocalUser(ParseObject remoteObject) {
        if (remoteObject.containsKey(AppConstants.OBJECT_ID)) {
            remoteObject.remove(AppConstants.OBJECT_ID);
        }
        remoteObject.pinInBackground(AppConstants.AUTHENTICATED_USER_DETAILS);
    }

    public static void updateCurrentLocalUser(final ParseObject updatableProps, @Nullable  final DoneCallback<Boolean> successCallback) {
        runLegacyDataCleanUp(updatableProps);
        updatableProps.pinInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                checkRunDataCleanUpBeforeUpdate(updatableProps, updatableProps.getString(AppConstants.REAL_OBJECT_ID), successCallback);
            }
        });
    }

    private static void checkRunDataCleanUpBeforeUpdate(final ParseObject updatableProps, final String realObjectId, @Nullable final DoneCallback<Boolean> successCallback) {
        final ParseQuery<ParseObject> peopleQuery = ParseQuery.getQuery(AppConstants.PEOPLE_GROUPS_AND_ROOMS);
        peopleQuery.whereEqualTo(AppConstants.REAL_OBJECT_ID, realObjectId);
        peopleQuery.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if (e == null) {
                    if (objects != null && !objects.isEmpty()) {
                        HolloutLogger.d("ObjectResult", "Objects found");
                        if (objects.size() > 1) {
                            HolloutLogger.d("ObjectResult", "Objects size greater than 1");
                            List<ParseObject> deletableObjects = new ArrayList<>();
                            for (ParseObject parseObject : objects) {
                                if (parseObject != null) {
                                    List<String> aboutUser = parseObject.getList(AppConstants.ABOUT_USER);
                                    if (aboutUser == null) {
                                        deletableObjects.add(parseObject);
                                    } else {
                                        if (aboutUser.isEmpty()) {
                                            deletableObjects.add(parseObject);
                                        }
                                    }
                                }
                            }
                            if (!deletableObjects.isEmpty()) {
                                ParseObject.deleteAllInBackground(deletableObjects, new DeleteCallback() {
                                    @Override
                                    public void done(ParseException e) {
                                        updateRemoteUserVariant(updatableProps, realObjectId, successCallback);
                                    }
                                });
                            } else {
                                updateRemoteUserVariant(updatableProps, realObjectId, successCallback);
                            }
                        } else {
                            HolloutLogger.d("ObjectResult", "Objects size is less than or equal to 1");
                            updateRemoteUserVariant(updatableProps, realObjectId, successCallback);
                        }
                    }
                }
                peopleQuery.cancel();
            }
        });
    }

    private static void updateRemoteUserVariant(final ParseObject updatableProps, String realObjectId,
                                                @Nullable final DoneCallback<Boolean> successCallback) {
        final ParseQuery<ParseObject> personQuery = ParseQuery.getQuery(AppConstants.PEOPLE_GROUPS_AND_ROOMS);
        personQuery.whereEqualTo(AppConstants.REAL_OBJECT_ID, realObjectId);
        personQuery.getFirstInBackground(new GetCallback<ParseObject>() {
            @Override
            public void done(final ParseObject updatableObject, ParseException e) {
                try {
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
                    } else {
                        //No such object was found, log out user
                        HolloutLogger.d("ObjectResult", "On a Low Key");
                        doLowKeyLogOut();
                    }
                } catch (ConcurrentModificationException ignored) {

                }
                personQuery.cancel();
            }
        });
    }

    private static void doLowKeyLogOut() {
        HolloutPreferences.setUserWelcomed(false);
        HolloutPreferences.clearPersistedCredentials();
        HolloutPreferences.getInstance().getAll().clear();
        ParseObject.unpinAllInBackground(AppConstants.APP_USERS);
        ParseObject.unpinAllInBackground(AppConstants.HOLLOUT_FEED);
        EventBus.getDefault().post(AppConstants.TERMINATE_APPLICATION);
    }

    public static void logOutAuthenticatedUser(final DoneCallback<Boolean> dissolutionCallback) {
        final ParseObject localObject = getCurrentUser();
        if (localObject != null) {
            localObject.put(AppConstants.APP_USER_ONLINE_STATUS, AppConstants.OFFLINE);
            localObject.put(AppConstants.APP_USER_LAST_SEEN, System.currentTimeMillis());
            checkRunDataCleanUpBeforeUpdate(localObject, localObject.getString(AppConstants.REAL_OBJECT_ID), new DoneCallback<Boolean>() {
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
