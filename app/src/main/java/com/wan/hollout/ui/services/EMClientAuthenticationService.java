package com.wan.hollout.ui.services;

import android.app.IntentService;
import android.content.Intent;
import android.support.annotation.Nullable;

import com.hyphenate.chat.EMClient;
import com.parse.ParseObject;
import com.wan.hollout.interfaces.DoneCallback;
import com.wan.hollout.managers.HolloutCommunicationsManager;
import com.wan.hollout.utils.AppConstants;
import com.wan.hollout.utils.AuthUtil;

import org.greenrobot.eventbus.EventBus;

/**
 * @author Wan Clem
 */

public class EMClientAuthenticationService extends IntentService {

    private ParseObject signedInUser;

    public EMClientAuthenticationService() {
        super("EMClientAuthenticationService");
    }

    @Override
    public void onCreate() {
        super.onCreate();
        initSignedInUser();
    }

    private void initSignedInUser() {
        if (signedInUser == null) {
            signedInUser = AuthUtil.getCurrentUser();
        }
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {
        initSignedInUser();
        if (intent != null) {
            checkEMCAuthenticationStatus();
        }
    }

    private void checkEMCAuthenticationStatus() {
        if (!EMClient.getInstance().isLoggedInBefore()) {
            ParseObject parseUser = AuthUtil.getCurrentUser();
            if (parseUser != null) {
                HolloutCommunicationsManager.getInstance().logInEMClient(parseUser.getString(AppConstants.REAL_OBJECT_ID), parseUser.getString(AppConstants.REAL_OBJECT_ID),
                        new DoneCallback<Boolean>() {
                            @Override
                            public void done(Boolean success, Exception e) {
                                if (e == null && success) {
                                    HolloutCommunicationsManager.getInstance().init(EMClientAuthenticationService.this);
                                }
                            }
                        });
            } else {
                EventBus.getDefault().post(AppConstants.ATTEMPT_LOGOUT);
            }
        } else {
            HolloutCommunicationsManager.getInstance().init(EMClientAuthenticationService.this);
        }
    }

}
