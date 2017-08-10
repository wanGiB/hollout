package com.wan.hollout.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.wan.hollout.R;
import com.wan.hollout.utils.AppConstants;
import com.wan.hollout.utils.HolloutPreferences;
import com.wan.hollout.utils.UiUtils;

import org.apache.commons.lang3.StringUtils;

import java.util.List;

import butterknife.ButterKnife;

/**
 * @author Wan Clem
 */
public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        ButterKnife.bind(this);
        checkAuthStatus();
    }

    private void checkAuthStatus() {
        ParseUser parseUser = ParseUser.getCurrentUser();
        if (parseUser != null) {
            List<String> aboutUser = parseUser.getList(AppConstants.ABOUT_USER);
            if (aboutUser != null) {
                if (!aboutUser.isEmpty()) {
                    launchMainActivity();
                } else {
                    launchAboutActivity();
                }
            } else {
                launchAboutActivity();
            }
        } else {
            String availableUsername = HolloutPreferences.getAvailableUsername();
            String availablePassword = HolloutPreferences.getAvailablePassword();
            if (StringUtils.isNotEmpty(availablePassword) && StringUtils.isNotEmpty(availableUsername)) {
                loginUser(availableUsername, availablePassword);
            } else {
                launchWelcomeActivity();
            }
        }
    }

    private void loginUser(final String username, final String password) {
        UiUtils.showProgressDialog(SplashActivity.this, "Refreshing your session. Please wait...");
        ParseUser.logInInBackground(username, password, new LogInCallback() {
            @Override
            public void done(final ParseUser user, final ParseException e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        UiUtils.dismissProgressDialog();
                        if (e == null && user != null) {
                            HolloutPreferences.persistCredentials(username, password);
                            launchMainActivity();
                        } else {
                            if (e != null) {
                                String errorMessage = e.getMessage();
                                if (!errorMessage.contains("i/o")) {
                                    UiUtils.showSafeToast(e.getMessage());
                                } else {
                                    UiUtils.showSafeToast("Failed to login. Please review your data connection and login in again");
                                    launchWelcomeActivity();
                                }
                            }
                        }
                    }
                });
            }
        });
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