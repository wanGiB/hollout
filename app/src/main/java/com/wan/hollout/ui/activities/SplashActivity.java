package com.wan.hollout.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.wan.hollout.R;
import com.wan.hollout.utils.HolloutPreferences;

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
        if (HolloutPreferences.isUserWelcomed()) {
            launchMainActivity();
        } else {
            launchWelcomeActivity();
        }
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