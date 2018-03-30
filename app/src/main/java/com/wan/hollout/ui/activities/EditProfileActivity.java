package com.wan.hollout.ui.activities;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;
import com.mikepenz.iconics.typeface.IIcon;
import com.parse.ParseObject;
import com.soundcloud.android.crop.Crop;
import com.wan.hollout.R;
import com.wan.hollout.interfaces.DoneCallback;
import com.wan.hollout.utils.AppConstants;
import com.wan.hollout.utils.AuthUtil;
import com.wan.hollout.utils.HolloutUtils;
import com.wan.hollout.utils.UiUtils;

import net.alhazmy13.mediapicker.Image.ImagePicker;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.WordUtils;

import java.io.File;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * @author Wan Clem
 */

@SuppressWarnings("unchecked")
public class EditProfileActivity extends BaseActivity implements View.OnClickListener {

    private static final int UPLOAD_ACTION_TYPE_PROFILE_PHOTO = 0x10;
    private static final int UPLOAD_ACTION_TYPE_COVER_PHOTO = 0x20;

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.cancel_action)
    ImageView cancelActionView;

    @BindView(R.id.done_action)
    ImageView doneActionView;

    @BindView(R.id.user_name_box)
    EditText displayNameBox;

    @BindView(R.id.user_cover_photo_view)
    ImageView userCoverPhotoView;

    @BindView(R.id.user_profile_photo_view)
    ImageView userProfilePhotoView;

    @BindView(R.id.edit_cover_photo_text)
    TextView editCoverPhotoTextView;

    @BindView(R.id.edit_photo_text)
    TextView editProfilePhotoTextView;

    @BindView(R.id.gender_radio_group)
    RadioGroup genderRadioGroup;

    @BindView(R.id.male)
    RadioButton maleRadioButton;

    @BindView(R.id.female)
    RadioButton femaleRadioButton;

    @BindView(R.id.age_box)
    EditText ageBox;

    @BindView(R.id.about_user)
    TextView aboutUserView;

    @BindView(R.id.edit_about_you)
    ImageView editAboutYouIcon;

    @BindView(R.id.about_user_layout)
    View aboutUserLayout;

    public String selectedGenderType = null;

    private ParseObject signedInUserObject;

    private int EDIT_ABOUT_USER_REQUEST_CODE = 0x10;
    private int currentUploadAction;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        signedInUserObject = AuthUtil.getCurrentUser();
        doneActionView.setImageDrawable(getDrawableFromIcon(Color.WHITE, GoogleMaterial.Icon.gmd_done));
        cancelActionView.setImageDrawable(getDrawableFromIcon(Color.WHITE, GoogleMaterial.Icon.gmd_close));
        editAboutYouIcon.setImageDrawable(getDrawableFromIcon(Color.GRAY, GoogleMaterial.Icon.gmd_mode_edit));
        loadSignedInUser();
        initEventHandlers();
    }

    private void loadSignedInUser() {
        if (signedInUserObject != null) {
            String signedInUserName = signedInUserObject.getString(AppConstants.APP_USER_DISPLAY_NAME);
            String profilePhotoUrl = signedInUserObject.getString(AppConstants.APP_USER_PROFILE_PHOTO_URL);
            String coverPhotoUrl = signedInUserObject.getString(AppConstants.APP_USER_COVER_PHOTO);

            displayNameBox.setText(WordUtils.capitalize(signedInUserName));
            if (StringUtils.isNotEmpty(profilePhotoUrl)) {
                UiUtils.loadImage(this, profilePhotoUrl, userProfilePhotoView);
            }
            if (StringUtils.isNotEmpty(coverPhotoUrl)) {
                UiUtils.loadImage(this, coverPhotoUrl, userCoverPhotoView);
                userCoverPhotoView.setColorFilter(new PorterDuffColorFilter(ContextCompat.getColor(this, R.color.colorPrimary), PorterDuff.Mode.MULTIPLY));
            }
            String userGender = signedInUserObject.getString(AppConstants.APP_USER_GENDER);
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
            String userAge = signedInUserObject.getString(AppConstants.APP_USER_AGE);
            if (StringUtils.isNotEmpty(userAge) && !userAge.equals(AppConstants.UNKNOWN)) {
                ageBox.setText(userAge);
            }
            List<String> aboutUser = signedInUserObject.getList(AppConstants.ABOUT_USER);
            if (aboutUser != null && !aboutUser.isEmpty()) {
                aboutUserView.setText(WordUtils.capitalize(aboutUser.get(0)));
            }
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem searchItem = menu.findItem(R.id.action_search);
        searchItem.setVisible(false);
        MenuItem filterPeopleMenuItem = menu.findItem(R.id.filter_people);
        filterPeopleMenuItem.setVisible(false);
        MenuItem continueButton = menu.findItem(R.id.button_continue);
        continueButton.setVisible(false);
        supportInvalidateOptionsMenu();
        return super.onPrepareOptionsMenu(menu);
    }

    private void initEventHandlers() {
        cancelActionView.setOnClickListener(this);
        doneActionView.setOnClickListener(this);
        editCoverPhotoTextView.setOnClickListener(this);
        editProfilePhotoTextView.setOnClickListener(this);
        userProfilePhotoView.setOnClickListener(this);
        userCoverPhotoView.setOnClickListener(this);
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
                signedInUserObject.put(AppConstants.APP_USER_GENDER, selectedGenderType);
            }
        });
        editAboutYouIcon.setOnClickListener(this);
        aboutUserLayout.setOnClickListener(this);
    }

    private Drawable getDrawableFromIcon(int color, IIcon icon) {
        return new IconicsDrawable(this)
                .sizeDp(18)
                .color(color)
                .icon(icon);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.cancel_action:
                finish();
                break;
            case R.id.done_action:
                validateDataAndUpdateProfile();
                break;
            case R.id.edit_cover_photo_text:
            case R.id.user_cover_photo_view:
                UiUtils.blinkView(v);
                setCurrentUploadAction(UPLOAD_ACTION_TYPE_COVER_PHOTO);
                HolloutUtils.startImagePicker(getCurrentActivityInstance());
                break;
            case R.id.edit_photo_text:
            case R.id.user_profile_photo_view:
                UiUtils.blinkView(v);
                setCurrentUploadAction(UPLOAD_ACTION_TYPE_PROFILE_PHOTO);
                HolloutUtils.startImagePicker(getCurrentActivityInstance());
                break;
            case R.id.edit_about_you:
                UiUtils.blinkView(v);
                Intent editAboutUserIntent = new Intent(getCurrentActivityInstance(), AboutUserActivity.class);
                editAboutUserIntent.putExtra(AppConstants.CAN_LAUNCH_MAIN, false);
                startActivityForResult(editAboutUserIntent, EDIT_ABOUT_USER_REQUEST_CODE);
                break;
            case R.id.about_user_layout:
                editAboutYouIcon.performClick();
                break;
        }
    }

    public void setCurrentUploadAction(int currentUploadAction) {
        this.currentUploadAction = currentUploadAction;
    }

    public int getCurrentUploadAction() {
        return currentUploadAction;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == EDIT_ABOUT_USER_REQUEST_CODE && resultCode == RESULT_OK) {
            signedInUserObject = AuthUtil.getCurrentUser();
            loadSignedInUser();
        } else if (requestCode == ImagePicker.IMAGE_PICKER_REQUEST_CODE && resultCode == RESULT_OK) {
            List<String> mPaths = (List<String>) data.getSerializableExtra(ImagePicker.EXTRA_IMAGE_PATH);
            if (mPaths != null && !mPaths.isEmpty()) {
                final String pickedPhotoFilePath = mPaths.get(0);
                if (pickedPhotoFilePath != null) {
                    final int currentAction = getCurrentUploadAction();
                    if (currentAction == UPLOAD_ACTION_TYPE_COVER_PHOTO || currentAction == UPLOAD_ACTION_TYPE_PROFILE_PHOTO) {
                        AlertDialog.Builder cropConsentDialog = new AlertDialog.Builder(EditProfileActivity.this);
                        cropConsentDialog.setMessage("Crop Photo ?");
                        cropConsentDialog.setPositiveButton("CROP", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Crop.of(Uri.fromFile(new File(pickedPhotoFilePath)), Uri.fromFile(new File(getCacheDir(), "CropIt")))
                                        .asSquare().start(EditProfileActivity.this);
                            }
                        }).setNegativeButton("DON'T CROP", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                prepareForUpload(pickedPhotoFilePath, currentAction);
                            }
                        });
                        cropConsentDialog.create().show();
                    }
                }
            }
        } else if (requestCode == Crop.REQUEST_CROP) {
            if (resultCode == Activity.RESULT_OK) {
                Uri result = Uri.fromFile(new File(getCacheDir(), "CropIt"));
                if (result != null) {
                    prepareForUpload(result.getPath(), getCurrentUploadAction());
                }
            }
        }
    }

    private void prepareForUpload(String pickedPhotoFilePath, int currentAction) {
        final ProgressDialog progressDialog;
        if (currentAction == UPLOAD_ACTION_TYPE_PROFILE_PHOTO) {
            progressDialog = UiUtils.showProgressDialog(EditProfileActivity.this, "Updating Profile Photo");
        } else {
            progressDialog = UiUtils.showProgressDialog(EditProfileActivity.this, "Updating Cover Photo");
        }
        HolloutUtils.uploadFileAsync(pickedPhotoFilePath, AppConstants.PHOTO_DIRECTORY, new DoneCallback<String>() {
            @Override
            public void done(final String result, final Exception e) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (e == null && result != null) {
                            ParseObject signedInUser = AuthUtil.getCurrentUser();
                            if (signedInUser != null) {
                                signedInUser.put(getCurrentUploadAction() == UPLOAD_ACTION_TYPE_PROFILE_PHOTO ? AppConstants.APP_USER_PROFILE_PHOTO_URL : AppConstants.APP_USER_COVER_PHOTO, result);
                                signedInUser.put(getCurrentUploadAction() == UPLOAD_ACTION_TYPE_PROFILE_PHOTO ? AppConstants.USER_PROFILE_PHOTO_UPLOAD_TIME : AppConstants.USER_COVER_PHOTO_UPLOAD_TIME, System.currentTimeMillis());
                                AuthUtil.updateCurrentLocalUser(signedInUser, new DoneCallback<Boolean>() {
                                    @Override
                                    public void done(Boolean result, Exception e) {
                                        UiUtils.dismissProgressDialog(progressDialog);
                                        if (e == null) {
                                            UiUtils.showSafeToast("Upload Success");
                                            if (signedInUserObject != null) {
                                                signedInUserObject = AuthUtil.getCurrentUser();
                                                loadSignedInUser();
                                            }
                                        } else {
                                            UiUtils.showSafeToast("An error occurred while updating photo please try again");
                                        }
                                    }
                                });
                            } else {
                                UiUtils.showSafeToast("An error occurred while updating photo.Invalid session");
                                Intent splashIntent = new Intent(EditProfileActivity.this, SplashActivity.class);
                                startActivity(splashIntent);
                                finish();
                            }
                        } else {
                            UiUtils.dismissProgressDialog(progressDialog);
                            UiUtils.showSafeToast("An error occurred while updating photo. Please try again.");
                        }
                    }
                });
            }
        });
    }

    private void validateDataAndUpdateProfile() {
        String userDisplayName = displayNameBox.getText().toString().trim();
        if (StringUtils.isEmpty(userDisplayName)) {
            displayNameBox.setError("Please set a display name");
            return;
        }
        if (selectedGenderType == null) {
            UiUtils.showSafeToast("Select your gender type");
            return;
        }
        String age = ageBox.getText().toString().trim();
        if (StringUtils.isNotEmpty(age)) {
            if (Integer.parseInt(age) < 16) {
                Snackbar.make(ageBox, "Sorry, hollout is for 16yrs and above", Snackbar.LENGTH_LONG).show();
                return;
            }
        } else {
            Snackbar.make(ageBox, "Your age please", Snackbar.LENGTH_LONG).show();
            return;
        }
        signedInUserObject.put(AppConstants.APP_USER_DISPLAY_NAME, displayNameBox.getText().toString().trim());
        signedInUserObject.put(AppConstants.APP_USER_AGE, age);
        signedInUserObject.put(AppConstants.APP_USER_GENDER, selectedGenderType);
        updateProfile();
    }

    private void updateProfile() {
        final ProgressDialog progressDialog = ProgressDialog.show(getCurrentActivityInstance(), "Updating Profile", "Please wait...");
        AuthUtil.updateCurrentLocalUser(signedInUserObject, new DoneCallback<Boolean>() {
            @Override
            public void done(Boolean result, Exception e) {
                UiUtils.showSafeToast("Profile Update success!");
                UiUtils.dismissProgressDialog(progressDialog);
                finish();
            }
        });
    }

}
