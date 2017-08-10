package com.wan.hollout.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.parse.GetCallback;
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;
import com.wan.hollout.R;
import com.wan.hollout.utils.AppConstants;
import com.wan.hollout.utils.HolloutLogger;
import com.wan.hollout.utils.HolloutPreferences;
import com.wan.hollout.utils.UiUtils;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
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
        pushInterests();
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

    private void pushInterests() {
        ParseQuery<ParseObject> interestsQuery = ParseQuery.getQuery(AppConstants.INTERESTS);
        interestsQuery.setLimit(1);
        interestsQuery.getFirstInBackground(new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject object, ParseException e) {
                if (object != null) {
                    return;
                }
                purgeAndPushInterests(readInterestsJSON());
            }
        });
    }

    public String readInterestsJSON() {
        String json;
        try {
            InputStream is = getAssets().open("interests.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return json;
    }

    private void objectifyInterests(String jsonString) {
        List<ParseObject> listOfInterests = new ArrayList<>();
        try {
            JSONArray interestsArray = new JSONArray(jsonString);
            HolloutLogger.d("Interests", interestsArray.toString());
            for (int i = 0; i < interestsArray.length(); i++) {
                JSONObject jsonObject = interestsArray.getJSONObject(i);
                ParseObject parseObject = new ParseObject(AppConstants.INTERESTS);
                parseObject.put(AppConstants.NAME, jsonObject.optString("interest").toLowerCase());
                listOfInterests.add(parseObject);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        ParseObject.saveAllInBackground(listOfInterests);
    }

    private void purgeAndPushInterests(String s) {
        objectifyInterests(s);
    }

}