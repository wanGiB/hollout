package com.wan.hollout.ui.activities;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Vibrator;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.method.ScrollingMovementMethod;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.ImageView;

import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.wan.hollout.R;
import com.wan.hollout.interfaces.DoneCallback;
import com.wan.hollout.ui.adapters.InterestsSuggestionAdapter;
import com.wan.hollout.ui.widgets.HolloutTextView;
import com.wan.hollout.utils.AppConstants;
import com.wan.hollout.utils.AuthUtil;
import com.wan.hollout.utils.HolloutLogger;
import com.wan.hollout.utils.HolloutPreferences;
import com.wan.hollout.utils.HolloutUtils;
import com.wan.hollout.utils.RequestCodes;
import com.wan.hollout.utils.UiUtils;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import bolts.Capture;
import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * @author Wan Clem
 */

@SuppressWarnings({"deprecation", "FieldCanBeLocal"})
public class AboutUserActivity extends BaseActivity {

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.pick_photo_view)
    ImageView userPhotoView;

    @BindView(R.id.interests_suggestion_recycler_view)
    RecyclerView interestsSuggestionRecyclerView;

    @BindView(R.id.more_about_user_field)
    EditText moreAboutUserField;

    @BindView(R.id.reason_for_interests_view)
    HolloutTextView reasonForInterestsView;

    @BindView(R.id.rootLayout)
    View rootLayout;

    private ParseObject signedInUser;

    private String NO_QUALIFIER_TIP = "Do not use the <b>I,a,am,an</b> prefixes. Use only keywords.<br/><br/><b>Example: Fashion Designer.</b> and not <b>A fashion Designer</b>";
    private String NO_WORK_PLACE = "Nope! Do not include where you work or school.";

    private Vibrator vibrator;

    private Animation shakeAnimation;

    private boolean keyboardListenersAttached = false;
    private ViewTreeObserver.OnGlobalLayoutListener keyboardLayoutListener;
    private String TAG = "AboutUserActivity";

    private InterestsSuggestionAdapter interestsSuggestionAdapter;
    private Capture<String> lastSelection = new Capture<>();

    private boolean canLaunchMain = true;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.about_user_layout);
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        signedInUser = AuthUtil.getCurrentUser();
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("About You");
        }
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            canLaunchMain = getIntent().getExtras().getBoolean(AppConstants.CAN_LAUNCH_MAIN, false);
        }
        loadSignedInUserPhoto();
        initVibrator();
        initShakeAnimation();
        attemptToOffloadPersistedInfoAboutUser();
        initComponents();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (isFinishing())
            overridePendingTransition(R.anim.fade_scale_in, R.anim.slide_to_right);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem searchItem = menu.findItem(R.id.action_search);
        searchItem.setVisible(false);
        MenuItem filterPeopleMenuItem = menu.findItem(R.id.filter_people);
        MenuItem continueButton = menu.findItem(R.id.button_continue);
        continueButton.setVisible(StringUtils.isNotEmpty(moreAboutUserField.getText().toString().trim()));
        filterPeopleMenuItem.setVisible(false);
        supportInvalidateOptionsMenu();
        return super.onPrepareOptionsMenu(menu);
    }

    private void attemptToOffloadPersistedInfoAboutUser() {
        if (signedInUser != null) {
            List<String> aboutUser = signedInUser.getList(AppConstants.ABOUT_USER);
            if (aboutUser != null) {
                if (!aboutUser.isEmpty()) {
                    String aboutUserString = TextUtils.join(",", aboutUser);
                    moreAboutUserField.setText(aboutUserString);
                    moreAboutUserField.setSelection(moreAboutUserField.length());
                    if (aboutUser.size() == 1) {
                        lastSelection.set(aboutUserString);
                    } else {
                        lastSelection.set(StringUtils.substringAfterLast(aboutUserString, ","));
                    }
                }
            }
        }
    }

    private void suggestOccupation(final String searchKey) {
        final ParseQuery<ParseObject> interestsQuery = ParseQuery.getQuery(AppConstants.INTERESTS);
        interestsQuery.whereContains(AppConstants.NAME, searchKey.trim().toLowerCase());
        interestsQuery.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if (e == null) {
                    if (StringUtils.isNotEmpty(moreAboutUserField.getText().toString().trim())) {
                        if (objects != null) {
                            if (!objects.isEmpty()) {
                                initInterestsAdapter(searchKey, objects);
                                UiUtils.showView(interestsSuggestionRecyclerView, true);
                            } else {
                                UiUtils.showView(interestsSuggestionRecyclerView, false);
                            }
                        } else {
                            UiUtils.showView(interestsSuggestionRecyclerView, false);
                        }
                    } else {
                        UiUtils.showView(interestsSuggestionRecyclerView, false);
                    }
                } else {
                    UiUtils.showView(interestsSuggestionRecyclerView, false);
                }
                interestsQuery.cancel();
            }
        });
    }

    private void initInterestsAdapter(String searchKey, List<ParseObject> occupations) {
        interestsSuggestionAdapter = new InterestsSuggestionAdapter(AboutUserActivity.this, occupations, searchKey,
                new InterestsSuggestionAdapter.OccupationSelectedListener() {
                    @Override
                    public void onSelectedOccupation(String occupation) {
                        lastSelection.set(occupation);
                        if (!moreAboutUserField.getText().toString().trim().contains(",")) {
                            moreAboutUserField.setText(occupation);
                            moreAboutUserField.setSelection(moreAboutUserField.getText().toString().trim().length());
                        } else {
                            int start = moreAboutUserField.getText().toString().lastIndexOf(",");
                            int end = Math.max(moreAboutUserField.getSelectionEnd(), 0);
                            moreAboutUserField.getText().replace(Math.min(start, end), Math.max(start, end),
                                    "," + occupation, 0, ("," + occupation).length());
                            moreAboutUserField.setSelection(moreAboutUserField.getText().toString().trim().length());
                        }
                        interestsSuggestionAdapter.notifyDataSetChanged();
                        UiUtils.showView(interestsSuggestionRecyclerView, false);
                        UiUtils.dismissKeyboard(moreAboutUserField);
                    }
                });
        LinearLayoutManager horizontalLinearLayoutManager = new LinearLayoutManager(AboutUserActivity.this, LinearLayoutManager.HORIZONTAL, false);
        interestsSuggestionRecyclerView.setLayoutManager(horizontalLinearLayoutManager);
        interestsSuggestionRecyclerView.setAdapter(interestsSuggestionAdapter);
    }

    private void initComponents() {
        reasonForInterestsView.setMovementMethod(ScrollingMovementMethod.getInstance());
        if (signedInUser != null) {
            interestsSuggestionRecyclerView.setNestedScrollingEnabled(true);
        }
        moreAboutUserField.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @SuppressLint("SetTextI18n")
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String textInOccupationField = moreAboutUserField.getText().toString().trim();
                String validText = textInOccupationField;
                if (StringUtils.isNotEmpty(textInOccupationField)) {
                    if (!textInOccupationField.contains(",")) {
                        if (!StringUtils.equalsIgnoreCase(textInOccupationField, lastSelection.get())) {
                            suggestOccupation(textInOccupationField);
                        }
                    } else {
                        String lastStringInOccupationField = StringUtils.substringAfterLast(textInOccupationField, ",");
                        validText = lastStringInOccupationField;
                        if (!StringUtils.equalsIgnoreCase(lastStringInOccupationField, lastSelection.get())) {
                            suggestOccupation(lastStringInOccupationField);
                        }
                    }
                } else {
                    reasonForInterestsView.setText(getString(R.string.describe_yourself));
                    UiUtils.showView(reasonForInterestsView, true);
                    UiUtils.showView(reasonForInterestsView, false);
                }
                String strippable = null;
                if (StringUtils.startsWithIgnoreCase(textInOccupationField, "Am a")
                        || StringUtils.startsWithIgnoreCase(textInOccupationField, "I am a")
                        || StringUtils.startsWithIgnoreCase(textInOccupationField, "I am ")
                        || StringUtils.startsWith(textInOccupationField, "A ")
                        || StringUtils.startsWith(textInOccupationField,"I\'m")
                        || StringUtils.startsWithIgnoreCase(textInOccupationField, "An ")
                        || StringUtils.startsWithIgnoreCase(textInOccupationField, "The ")) {
                    vibrateDevice(NO_QUALIFIER_TIP);
                    return;
                } else {
                    if (StringUtils.containsIgnoreCase(textInOccupationField, " at ")) {
                        strippable = " at ";
                    } else if (StringUtils.containsIgnoreCase(textInOccupationField, " in ")) {
                        strippable = " in ";
                    } else if (StringUtils.containsIgnoreCase(textInOccupationField, " with ")) {
                        strippable = " with ";
                    } else if (StringUtils.containsIgnoreCase(textInOccupationField, " of ")) {
                        strippable = " of ";
                    }
                    if (strippable != null) {
                        vibrateDevice(NO_WORK_PLACE + " <b>" + StringUtils.substringBefore(validText, strippable) + "</b> is just fine");
                        return;
                    }
                }
                reasonForInterestsView.setTextColor(ContextCompat.getColor(AboutUserActivity.this, R.color.light_grey));
                UiUtils.showView(reasonForInterestsView, false);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }

        });

    }

    private void checkAndContinue() {
        if (canMoveFurther()) {
            List<String> aboutUserList = new ArrayList<>();
            String enteredInterests = moreAboutUserField.getText().toString().trim();
            buildInterests(aboutUserList, enteredInterests);
            signedInUser.put(AppConstants.ABOUT_USER, aboutUserList);
            final ProgressDialog progressDialog = UiUtils.showProgressDialog(AboutUserActivity.this, "Please wait...");
            AuthUtil.updateCurrentLocalUser(signedInUser, new DoneCallback<Boolean>() {
                @Override
                public void done(Boolean success, Exception e) {
                    if (e == null) {
                        refreshSearchCriteria();
                        checkAndPushInterests(Arrays.asList(moreAboutUserField.getText().toString().trim().split(",")));
                        UiUtils.dismissProgressDialog(progressDialog);
                        if (!HolloutPreferences.isUserWelcomed()) {
                            launchGenderAndBirthDayActivity();
                        } else {
                            if (canLaunchMain) {
                                launchMainActivity();
                            } else {
                                Intent callerIntent = new Intent();
                                setResult(RESULT_OK, callerIntent);
                                finish();
                            }
                        }
                    } else {
                        UiUtils.dismissProgressDialog(progressDialog);
                        UiUtils.showSafeToast("Error completing operation. Please try again. ");
                    }
                }
            });
        } else {
            Snackbar.make(rootLayout, cantMoveFurtherErrorMessage(), Snackbar.LENGTH_SHORT).show();
        }
    }

    private void refreshSearchCriteria() {
        ParseObject newSignedInUserObject = AuthUtil.getCurrentUser();
        if (newSignedInUserObject != null) {
            newSignedInUserObject.put(AppConstants.SEARCH_CRITERIA, HolloutUtils.constructSearch());
            AuthUtil.updateCurrentLocalUser(newSignedInUserObject, null);
        }
    }

    private void checkAndPushInterests(List<String> interests) {
        for (final String s : interests) {
            final ParseQuery<ParseObject> parseObjectParseQuery = ParseQuery.getQuery(AppConstants.INTERESTS);
            parseObjectParseQuery.whereEqualTo(AppConstants.NAME, s.trim().toLowerCase());
            parseObjectParseQuery.getFirstInBackground(new GetCallback<ParseObject>() {
                @Override
                public void done(ParseObject object, ParseException e) {
                    if (object == null) {
                        ParseObject newObject = new ParseObject(AppConstants.INTERESTS);
                        newObject.put(AppConstants.NAME, s.trim().toLowerCase());
                        newObject.saveInBackground();
                        parseObjectParseQuery.cancel();
                    }
                }
            });
        }
    }

    private void launchGenderAndBirthDayActivity() {
        Intent genderAndBirthDayIntent = new Intent(AboutUserActivity.this, GenderAndAgeConfigurationActivity.class);
        startActivityForResult(genderAndBirthDayIntent, RequestCodes.CONFIGURE_BIRTHDAY_AND_GENDER);
    }

    private void launchMainActivity() {
        Intent mainIntent = new Intent(AboutUserActivity.this, MainActivity.class);
        startActivity(mainIntent);
        finish();
    }

    private void buildInterests(List<String> interests, String interestTag) {
        String[] enteredOccupations = interestTag.split(",");
        for (String interest : enteredOccupations) {
            if (!interests.contains(interest.toLowerCase())) {
                interests.add(interest.toLowerCase());
            }
        }
    }

    public boolean canMoveFurther() {
        return StringUtils.isNotEmpty(moreAboutUserField.getText().toString().trim());
    }

    public String cantMoveFurtherErrorMessage() {
        return getString(R.string.about_you_please);
    }

    private void vibrateDevice(String s) {
        vibrator.vibrate(100);
        moreAboutUserField.startAnimation(shakeAnimation);
        UiUtils.showView(reasonForInterestsView, true);
        reasonForInterestsView.setTextColor(ContextCompat.getColor(AboutUserActivity.this, R.color.dark_gray));
        reasonForInterestsView.setText(UiUtils.fromHtml(s));
    }

    @Override
    protected void onStart() {
        super.onStart();
        listenForConfigChanges();
        keyboardListenersAttached = true;
        rootLayout.getViewTreeObserver().addOnGlobalLayoutListener(keyboardLayoutListener);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (keyboardListenersAttached) {
            rootLayout.getViewTreeObserver().removeOnGlobalLayoutListener(keyboardLayoutListener);
        }
    }

    private void listenForConfigChanges() {
        keyboardLayoutListener = new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                Rect r = new Rect();
                rootLayout.getWindowVisibleDisplayFrame(r);
                int screenHeight = rootLayout.getRootView().getHeight();
                // r.bottom is the position above soft keypad or device button.
                // if keypad is shown, the r.bottom is smaller than that before.
                int keypadHeight = screenHeight - r.bottom;
                HolloutLogger.d(TAG, "keypadHeight = " + keypadHeight);
                if (keypadHeight > screenHeight * 0.15) { // 0.15 ratio is perhaps enough to determine keypad height.
                    // keyboard is opened
                    onKeyboardShown();
                } else {
                    // keyboard is closed
                    onKeyboardHidden();
                }
            }
        };
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RequestCodes.CONFIGURE_BIRTHDAY_AND_GENDER) {
            if (resultCode == RESULT_OK) {
                launchMainActivity();
                finish();
            }
        } else if (requestCode == RequestCodes.MEET_PEOPLE_REQUEST_CODE) {
            launchMainActivity();
            finish();
        }
    }

    private void onKeyboardHidden() {
        HolloutLogger.d(TAG, "Keyboard Hidden");
    }

    private void onKeyboardShown() {
        HolloutLogger.d(TAG, "Keyboard Shown");
    }

    private void initVibrator() {
        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
    }

    private void initShakeAnimation() {
        shakeAnimation = AnimationUtils.loadAnimation(this, R.anim.shake);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        } else if (item.getItemId() == R.id.button_continue) {
            UiUtils.dismissKeyboard(moreAboutUserField);
            checkAndContinue();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void loadSignedInUserPhoto() {
        if (signedInUser != null) {
            String signedInUserPhotoUrl = signedInUser.getString(AppConstants.APP_USER_PROFILE_PHOTO_URL);
            if (StringUtils.isNotEmpty(signedInUserPhotoUrl)) {
                UiUtils.loadImage(this, signedInUserPhotoUrl, userPhotoView);
            }
        }
    }

}
