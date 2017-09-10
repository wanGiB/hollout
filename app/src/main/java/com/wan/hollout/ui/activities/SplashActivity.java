package com.wan.hollout.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.wan.hollout.R;
import com.wan.hollout.utils.AppConstants;
import com.wan.hollout.utils.AuthUtil;
import com.wan.hollout.utils.HolloutLogger;
import com.wan.hollout.utils.RequestCodes;

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
@SuppressWarnings("ResultOfMethodCallIgnored")
public class SplashActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        ButterKnife.bind(this);
        checkAuthStatus();
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
        }else{
            launchWelcomeActivity();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (isFinishing()) overridePendingTransition(R.anim.fade_scale_in, R.anim.slide_to_right);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RequestCodes.CONFIGURE_BIRTHDAY_AND_GENDER && resultCode == RESULT_OK) {
            launchPeopleToMeetActivity();
        } else if (requestCode == RequestCodes.MEET_PEOPLE_REQUEST_CODE) {
            launchMainActivity();
        }
    }

    private void launchPeopleToMeetActivity() {
        Intent peopleToMeetActivityIntent = new Intent(SplashActivity.this, MeetPeopleActivity.class);
        startActivityForResult(peopleToMeetActivityIntent, RequestCodes.MEET_PEOPLE_REQUEST_CODE);
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

    private void pushInterestsIfNotAlreadyPushed() {
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
            InputStream is = getAssets().open("reactions/interests.json");
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