package com.wan.hollout.utils;

import android.support.annotation.NonNull;
import android.support.v4.app.FragmentActivity;

import com.facebook.login.LoginManager;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;

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

}
