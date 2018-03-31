package com.wan.hollout.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.parse.ParseObject;
import com.raizlabs.android.dbflow.config.FlowManager;
import com.wan.hollout.R;
import com.wan.hollout.database.HolloutDb;
import com.wan.hollout.ui.widgets.ShimmerFrameLayout;
import com.wan.hollout.utils.AppConstants;
import com.wan.hollout.utils.AuthUtil;
import com.wan.hollout.utils.HolloutUtils;
import com.wan.hollout.utils.RequestCodes;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * @author Wan Clem
 */
@SuppressWarnings("ResultOfMethodCallIgnored")
public class SplashActivity extends AppCompatActivity {

    @Nullable
    @BindView(R.id.shimmer_view_container)
    ShimmerFrameLayout shimmerFrameLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        ButterKnife.bind(this);
    }

    private void checkAuthStatus() {
        ParseObject authenticatedUser = AuthUtil.getCurrentUser();
        if (authenticatedUser != null) {
            List<String> aboutUser = authenticatedUser.getList(AppConstants.ABOUT_USER);
            if (aboutUser != null) {
                if (!aboutUser.isEmpty()) {
                    String userAge = authenticatedUser.getString(AppConstants.APP_USER_GENDER);
                    String userGender = authenticatedUser.getString(AppConstants.APP_USER_GENDER);
                    if (userAge.equals(AppConstants.UNKNOWN) || userGender.equals(AppConstants.UNKNOWN)) {
                        launchGenderAndAgeActivity();
                    } else {
                        launchMainActivity();
                    }
                } else {
                    launchAboutActivity();
                }
            } else {
                launchAboutActivity();
            }
        } else {
            FlowManager.getDatabase(HolloutDb.class).reset();
            launchWelcomeActivity();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        startShimmerAnimation();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (!HolloutUtils.checkGooglePlayServices(SplashActivity.this)) {
                    return;
                }
                checkAuthStatus();
            }
        }, 500);
    }

    private void startShimmerAnimation() {
        if (shimmerFrameLayout != null) {
            shimmerFrameLayout.startShimmerAnimation();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (isFinishing()) overridePendingTransition(R.anim.fade_scale_in, R.anim.slide_to_right);
    }

    @Override
    protected void onStop() {
        super.onStop();
        stopShimmerAnimation();
    }

    private void stopShimmerAnimation() {
        if (shimmerFrameLayout != null) {
            shimmerFrameLayout.stopShimmerAnimation();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RequestCodes.CONFIGURE_BIRTHDAY_AND_GENDER && resultCode == RESULT_OK) {
            launchMainActivity();
        }
    }

    private void launchGenderAndAgeActivity() {
        Intent genderAndAgeIntent = new Intent(SplashActivity.this, GenderAndAgeConfigurationActivity.class);
        startActivityForResult(genderAndAgeIntent, RequestCodes.CONFIGURE_BIRTHDAY_AND_GENDER);
    }

    private void launchAboutActivity() {
        Intent aboutActivityIntent = new Intent(SplashActivity.this, AboutUserActivity.class);
        startActivity(aboutActivityIntent);
        finish();
    }

    private void launchMainActivity() {
        Intent mainIntent = new Intent(SplashActivity.this, MainActivity.class);
        startActivity(mainIntent);
        finishAct();
    }

    private void launchWelcomeActivity() {
        Intent welcomeIntent = new Intent(SplashActivity.this, WelcomeActivity.class);
        startActivity(welcomeIntent);
        finishAct();
    }

    private void finishAct() {
        finish();
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out);
    }

}