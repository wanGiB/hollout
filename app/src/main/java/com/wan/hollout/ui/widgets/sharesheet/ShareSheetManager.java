package com.wan.hollout.ui.widgets.sharesheet;


import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Application;
import android.os.Build;
import android.os.Bundle;


/**
 * @author Wan Clem
 */

public class ShareSheetManager {

    private static ShareLinkManager shareLinkManager_;

    public static void shareLink(ShareSheet.ShareLinkBuilder builder) {
        //Cancel any existing sharing in progress.
        if (shareLinkManager_ != null) {
            shareLinkManager_.cancelShareLinkDialog(true);
        }
        shareLinkManager_ = new ShareLinkManager();
        shareLinkManager_.shareLink(builder);
    }

    /**
     * <p>Cancel current share link operation and Application selector dialog. If your app is not using auto session management, make sure you are
     * calling this method before your activity finishes inorder to prevent any window leak. </p>
     *
     * @param animateClose A {@link Boolean} to specify whether to close the dialog with an animation.
     *                     A value of true will close the dialog with an animation. Setting this value
     *                     to false will close the Dialog immediately.
     */
    public void cancelShareLinkDialog(boolean animateClose) {
        if (shareLinkManager_ != null) {
            shareLinkManager_.cancelShareLinkDialog(animateClose);
        }
    }


    /**
     * <p>Class that observes activity life cycle events and determines when to start and stop
     * session.</p>
     */
    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    private class BranchActivityLifeCycleObserver implements Application.ActivityLifecycleCallbacks {
        private int activityCnt_ = 0; //Keep the count of live  activities.


        @Override
        public void onActivityCreated(Activity activity, Bundle bundle) {

        }

        @Override
        public void onActivityStarted(Activity activity) {
            activityCnt_++;
        }

        @Override
        public void onActivityResumed(Activity activity) {

        }

        @Override
        public void onActivityPaused(Activity activity) {
            /* Close any opened sharing dialog.*/
            if (shareLinkManager_ != null) {
                shareLinkManager_.cancelShareLinkDialog(true);
            }
        }

        @Override
        public void onActivityStopped(Activity activity) {
            activityCnt_--; // Check if this is the last activity. If so, stop the session.

        }

        @Override
        public void onActivitySaveInstanceState(Activity activity, Bundle bundle) {
        }

        @Override
        public void onActivityDestroyed(Activity activity) {

        }

    }
}
