package com.wan.hollout.ui.activities;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Rect;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.Toolbar;
import android.text.method.LinkMovementMethod;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.parse.ParseObject;
import com.wan.hollout.R;
import com.wan.hollout.interfaces.DoneCallback;
import com.wan.hollout.ui.widgets.CircleImageView;
import com.wan.hollout.utils.AppConstants;
import com.wan.hollout.utils.AuthUtil;
import com.wan.hollout.utils.HolloutLogger;
import com.wan.hollout.utils.UiUtils;

import org.apache.commons.lang3.StringUtils;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * @author Wan Clem
 */

public class GenderAndAgeConfigurationActivity extends BaseActivity {

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.age_box)
    EditText ageBox;

    @BindView(R.id.pick_photo_view)
    CircleImageView userPhotoView;

    @BindView(R.id.gender_radio_group)
    RadioGroup genderRadioGroup;

    @BindView(R.id.rootLayout)
    View rootLayout;

    @BindView(R.id.accept_app_license)
    CheckBox acceptLicenseCheck;

    @BindView(R.id.male)
    RadioButton maleRadioButton;

    @BindView(R.id.female)
    RadioButton femaleRadioButton;

    private ParseObject signedInUser;

    public String selectedGenderType = null;

    private boolean keyboardListenersAttached = false;
    private ViewTreeObserver.OnGlobalLayoutListener keyboardLayoutListener;

    private String TAG = "GenderAndAgeConfiguration";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gender_and_age_layout);
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        signedInUser = AuthUtil.getCurrentUser();

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("You are ");
        }

        if (signedInUser != null) {
            offloadUserDetails();
        }

        genderRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(RadioGroup group, @IdRes int checkedId) {
                if (checkedId == R.id.male) {
                    selectedGenderType = getString(R.string.male);
                    maleRadioButton.setTextColor(Color.WHITE);
                    femaleRadioButton.setTextColor(Color.BLACK);
                } else {
                    selectedGenderType = getString(R.string.female);
                    femaleRadioButton.setTextColor(Color.WHITE);
                    maleRadioButton.setTextColor(Color.BLACK);
                }

                signedInUser.put(AppConstants.APP_USER_GENDER, selectedGenderType);

            }

        });

        setupTermsAndConditionsView();
    }

    private void checkAndContinue() {
        if (acceptLicenseCheck.getVisibility() == View.GONE) {
            acceptLicenseCheck.setVisibility(View.VISIBLE);
        } else {
            if (!acceptLicenseCheck.isChecked()) {
                Snackbar.make(acceptLicenseCheck, "You must accept our privacy policy", Snackbar.LENGTH_LONG).show();
                return;
            }
            if (selectedGenderType.equals(AppConstants.UNKNOWN)) {
                Snackbar.make(acceptLicenseCheck, "Your gender please", Snackbar.LENGTH_LONG).show();
                return;
            }
            String age = ageBox.getText().toString().trim();
            if (StringUtils.isNotEmpty(age)) {
                if (Integer.parseInt(age) < 16) {
                    Snackbar.make(acceptLicenseCheck, "Sorry, hollout is for 16yrs and above", Snackbar.LENGTH_LONG).show();
                    return;
                }
            } else {
                Snackbar.make(ageBox, "Your age please", Snackbar.LENGTH_LONG).show();
                return;
            }
            signedInUser.put(AppConstants.APP_USER_GENDER, selectedGenderType);
            signedInUser.put(AppConstants.APP_USER_AGE, ageBox.getText().toString().trim());
            UiUtils.showProgressDialog(GenderAndAgeConfigurationActivity.this, "Please wait...");
            AuthUtil.updateCurrentLocalUser(signedInUser, new DoneCallback<Boolean>() {
                @Override
                public void done(Boolean result, Exception e) {
                    UiUtils.dismissProgressDialog();
                    if (e == null) {
                        navigateBackToCaller();
                    } else {
                        Snackbar.make(acceptLicenseCheck, "An error occurred while updating details. Please review your data and try again",
                                Snackbar.LENGTH_LONG).show();
                    }
                }
            });
        }
    }

    private void setupTermsAndConditionsView() {
        acceptLicenseCheck.setText(UiUtils.fromHtml("By continuing, you agree to our <a href=https://firebasestorage.googleapis.com/v0/b/hollout-860db.appspot.com/o/WebComponents%2Fprivacy_policy.html?alt=media&token=e4b6c9b9-524c-45a9-847b-6f3bb1157a6f>Privacy Policy</a>"));
        acceptLicenseCheck.setMovementMethod(LinkMovementMethod.getInstance());
        acceptLicenseCheck.setClickable(true);
    }

    private void navigateBackToCaller() {
        Intent mCallerIntent = new Intent();
        setResult(RESULT_OK, mCallerIntent);
        finish();
    }

    private void offloadUserDetails() {
        String userPhotoUrl = signedInUser.getString(AppConstants.APP_USER_PROFILE_PHOTO_URL);
        if (StringUtils.isNotEmpty(userPhotoUrl)) {
            UiUtils.loadImage(this, userPhotoUrl, userPhotoView);
        }
        String userAge = signedInUser.getString(AppConstants.APP_USER_AGE);
        if (StringUtils.isNotEmpty(userAge) && !userAge.equals(AppConstants.UNKNOWN)) {
            ageBox.setText(userAge);
        }
        String userGender = signedInUser.getString(AppConstants.APP_USER_GENDER);
        if (StringUtils.isNotEmpty(userGender)) {
            selectedGenderType = userGender;
            if (userGender.equals(getString(R.string.male))) {
                genderRadioGroup.check(R.id.male);
                maleRadioButton.setTextColor(Color.WHITE);
                femaleRadioButton.setTextColor(Color.BLACK);
            } else if (userGender.equals(getString(R.string.female))) {
                genderRadioGroup.check(R.id.female);
                femaleRadioButton.setTextColor(Color.WHITE);
                maleRadioButton.setTextColor(Color.BLACK);
            }
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        MenuItem searchItem = menu.findItem(R.id.action_search);

        searchItem.setVisible(false);

        MenuItem filterPeopleMenuItem = menu.findItem(R.id.filter_people);
//        MenuItem createNewGroupItem  = menu.findItem(R.id.create_new_group);

//        createNewGroupItem.setVisible(false);
        filterPeopleMenuItem.setVisible(false);

        MenuItem continueButton = menu.findItem(R.id.button_continue);
        continueButton.setVisible(true);

        supportInvalidateOptionsMenu();

        return super.onPrepareOptionsMenu(menu);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.button_continue) {
            UiUtils.dismissKeyboard(ageBox);
            checkAndContinue();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (isFinishing()) overridePendingTransition(R.anim.fade_scale_in, R.anim.slide_to_right);
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

    private void onKeyboardHidden() {
        HolloutLogger.d(TAG, "Keyboard Hidden");
    }

    private void onKeyboardShown() {
        HolloutLogger.d(TAG, "Keyboard Shown");
    }

}
