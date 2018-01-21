package com.wan.hollout.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatDelegate;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;

import com.parse.ParseObject;
import com.wan.hollout.R;
import com.wan.hollout.interfaces.DoneCallback;
import com.wan.hollout.ui.widgets.CircleImageView;
import com.wan.hollout.ui.widgets.CircularProgressButton;
import com.wan.hollout.ui.widgets.MaterialEditText;
import com.wan.hollout.utils.AppConstants;
import com.wan.hollout.utils.AuthUtil;
import com.wan.hollout.utils.UiUtils;

import org.apache.commons.lang3.StringUtils;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * @author Wan Clem
 */

public class ComposeStatusActivity extends BaseActivity {

    @BindView(R.id.status_field)
    public MaterialEditText statusField;

    @BindView(R.id.share_thought)
    public CircularProgressButton shareThoughtButton;

    @BindView(R.id.user_photo_view)
    CircleImageView userPhotoView;

    private ParseObject signedInUser;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
        setContentView(R.layout.compose_status_layout);
        ButterKnife.bind(this);
        signedInUser = AuthUtil.getCurrentUser();
        shareThoughtButton.setIndeterminateProgressMode(true);
        if (signedInUser != null) {
            String signedInUserStatus = signedInUser.getString(AppConstants.APP_USER_STATUS);
            String signedInUserProfilePhotoUrl = signedInUser.getString(AppConstants.APP_USER_PROFILE_PHOTO_URL);

            if (StringUtils.isNotEmpty(signedInUserStatus)) {
                statusField.setText(signedInUserStatus);
            } else {
                statusField.setText(getString(R.string.hey_there_holla_me_on_hollout));
            }
            if (StringUtils.isNotEmpty(signedInUserProfilePhotoUrl)) {
                UiUtils.loadImage(ComposeStatusActivity.this, signedInUserProfilePhotoUrl, userPhotoView);
            }
        }
        statusField.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (StringUtils.isNotEmpty(charSequence.toString().trim())) {
                    if (charSequence.toString().length() <= 90) {
                        UiUtils.showView(shareThoughtButton, true);
                    } else {
                        shareThoughtButton.setVisibility(View.GONE);
                    }
                } else {
                    shareThoughtButton.setVisibility(View.GONE);
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }

        });

        shareThoughtButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (StringUtils.isNotEmpty(statusField.getText().toString().trim())) {
                    if (signedInUser != null) {
                        String previousStatus = signedInUser.getString(AppConstants.APP_USER_STATUS);
                        if (StringUtils.isNotEmpty(previousStatus)) {
                            if (statusField.getText().toString().trim().equals(previousStatus)) {
                                //It's same status,just finish the activity
                                finish();
                            } else {
                                //Set new status
                                setNewStatus();
                            }
                        } else {
                            //Set new status
                            setNewStatus();
                        }
                    }
                }
            }
        });
    }

    private void setNewStatus() {
        if (signedInUser != null) {
            UiUtils.morphRequestToProgress(shareThoughtButton);
            UiUtils.dismissKeyboard(statusField);
            signedInUser.put(AppConstants.APP_USER_STATUS, statusField.getText().toString().trim());
            AuthUtil.updateCurrentLocalUser(signedInUser, new DoneCallback<Boolean>() {
                @Override
                public void done(Boolean result, Exception e) {
                    if (e == null) {
                        UiUtils.showSafeToast("Success!");
                        UiUtils.morphRequestToSuccess(shareThoughtButton);
                        Intent callerIntent = new Intent();
                        setResult(RESULT_OK, callerIntent);
                        finish();
                    }
                }
            });
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (isFinishing()) overridePendingTransition(R.anim.fade_scale_in, R.anim.slide_to_right);
    }

}
