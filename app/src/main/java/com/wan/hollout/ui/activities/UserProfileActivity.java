package com.wan.hollout.ui.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import com.flaviofaria.kenburnsview.KenBurnsView;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.wan.hollout.R;
import com.wan.hollout.components.ApplicationLoader;
import com.wan.hollout.ui.adapters.PeopleToMeetAdapter;
import com.wan.hollout.ui.widgets.CircleImageView;
import com.wan.hollout.ui.widgets.HolloutEditText;
import com.wan.hollout.ui.widgets.HolloutTextView;
import com.wan.hollout.utils.AppConstants;
import com.wan.hollout.utils.HolloutUtils;
import com.wan.hollout.utils.RequestCodes;
import com.wan.hollout.utils.UiUtils;

import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.WordUtils;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.wan.hollout.utils.UiUtils.attachDrawableToTextView;
import static com.wan.hollout.utils.UiUtils.removeAllDrawablesFromTextView;

/**
 * @author Wan Clem
 */

public class UserProfileActivity extends AppCompatActivity implements View.OnClickListener {

    @BindView(R.id.go_back)
    ImageView goBack;

    @BindView(R.id.online_status)
    HolloutTextView onlineStatusView;

    @BindView(R.id.signed_in_user_cover_image_view)
    KenBurnsView signedInUserCoverPhotoView;

    @BindView(R.id.user_display_name)
    HolloutEditText userDisplayNameView;

    @BindView(R.id.avatar)
    CircleImageView userAvatarView;

    @BindView(R.id.about_user)
    HolloutTextView aboutUserTextView;

    @BindView(R.id.start_chat_view)
    FloatingActionButton startChatView;

    @BindView(R.id.user_location_and_distance)
    HolloutTextView userLocationAndDistanceView;

    @BindView(R.id.about_user_recycler_view)
    RecyclerView aboutUserRecyclerView;

    @BindView(R.id.feature_photos_instruction)
    HolloutTextView featurePhotosInstruction;

    @BindView(R.id.featured_photos_place_holder_image)
    ImageView featuredPhotosPlaceHolderImageView;

    @BindView(R.id.edit_about_you)
    HolloutTextView editAboutYou;

    @BindView(R.id.age_view)
    HolloutTextView ageView;

    @BindView(R.id.done_with_display_name_edit)
    ImageView doneWithDisplayNameEdit;

    private ParseUser parseUser;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
        ButterKnife.bind(this);
        offloadIntent();
        initClickListeners();
    }

    private void initClickListeners() {
        goBack.setOnClickListener(this);
    }

    private void offloadIntent() {
        parseUser = getIntent().getExtras().getParcelable(AppConstants.USER_PROPERTIES);
        loadUserDetails();
    }

    private void loadUserDetails() {
        if (parseUser != null) {
            loadUserProfile(parseUser);
            offloadUserAboutsIfAvailable(parseUser);
        }
    }

    private void offloadUserAboutsIfAvailable(ParseUser parseUser) {
        List<ParseObject> aboutUserList = new ArrayList<>();
        List<String> userAboutList = parseUser.getList(AppConstants.ABOUT_USER);
        if (userAboutList != null) {
            if (!userAboutList.isEmpty()) {
                for (String interest : userAboutList) {
                    ParseObject interestsObject = new ParseObject(AppConstants.INTERESTS);
                    interestsObject.put(AppConstants.NAME, interest.toLowerCase());
                    interestsObject.put(AppConstants.SELECTED, true);
                    if (!aboutUserList.contains(interestsObject)) {
                        aboutUserList.add(interestsObject);
                    }
                }
            }
        }
        setupUserAboutAdapter(aboutUserList);
    }

    private void setupUserAboutAdapter(List<ParseObject> parseObjects) {
        PeopleToMeetAdapter peopleToMeetAdapter = new PeopleToMeetAdapter(this, parseObjects, AppConstants.PEOPLE_TO_MEET_HOST_TYPE_SELECTED);
        LinearLayoutManager horizontalLinearLayoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        aboutUserRecyclerView.setLayoutManager(horizontalLinearLayoutManager);
        aboutUserRecyclerView.setAdapter(peopleToMeetAdapter);
    }

    @SuppressLint("SetTextI18n")
    private void loadUserProfile(ParseUser parseUser) {
        final ParseUser signedInUser = ParseUser.getCurrentUser();
        if (signedInUser != null) {
            String username = parseUser.getString(AppConstants.APP_USER_DISPLAY_NAME);
            String userAge = parseUser.getString(AppConstants.APP_USER_AGE);
            String userLocation = HolloutUtils.resolveToBestLocation(parseUser);
            ParseGeoPoint userGeoPoint = parseUser.getParseGeoPoint(AppConstants.APP_USER_GEO_POINT);
            ParseGeoPoint signedInUserGeoPoint = signedInUser.getParseGeoPoint(AppConstants.APP_USER_GEO_POINT);
            String distanceToUser = String.valueOf(RandomUtils.nextDouble(0, 10));
            if (signedInUserGeoPoint != null && userGeoPoint != null) {
                double distanceInKills = signedInUserGeoPoint.distanceInKilometersTo(userGeoPoint);
                String value = HolloutUtils.formatDistance(distanceInKills);
                distanceToUser = value + "KM";
            }
            if (signedInUser.getObjectId().equals(parseUser.getObjectId())) {
                if (userLocation != null) {
                    userLocationAndDistanceView.setText(userLocation);
                } else {
                    userLocationAndDistanceView.setText(distanceToUser + "KM from nearby kinds");
                }
            } else {
                if (UiUtils.canShowLocation(parseUser, AppConstants.ENTITY_TYPE_CLOSEBY, null)) {
                    if (userLocation != null) {
                        userLocationAndDistanceView.setText(userLocation + ", " + distanceToUser);
                    } else {
                        userLocationAndDistanceView.setText(distanceToUser + "KM from you");
                    }
                } else {
                    userLocationAndDistanceView.setText(distanceToUser + "KM from you");
                }
            }
            userDisplayNameView.setText(WordUtils.capitalize(username));
            if (signedInUser.getObjectId().equals(parseUser.getObjectId())) {
                userDisplayNameView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        userDisplayNameView.setFocusable(true);
                        userDisplayNameView.setFocusableInTouchMode(true);
                        userDisplayNameView.setCursorVisible(true);
                        userDisplayNameView.setSelection(userDisplayNameView.getText().toString().trim().length());
                        userDisplayNameView.requestFocus();
                        UiUtils.showKeyboard(userDisplayNameView);
                        userDisplayNameView.setBackground(ContextCompat.getDrawable(UserProfileActivity.this, R.drawable.blue_grey_thin_edit_text_bg));
                        UiUtils.showView(doneWithDisplayNameEdit, true);
                        doneWithDisplayNameEdit.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                UiUtils.showProgressDialog(UserProfileActivity.this, "Updating display name...");
                                signedInUser.put(AppConstants.APP_USER_DISPLAY_NAME, userDisplayNameView.getText().toString().trim());
                                signedInUser.saveInBackground(new SaveCallback() {
                                    @Override
                                    public void done(ParseException e) {
                                        UiUtils.dismissProgressDialog();
                                        if (e == null) {
                                            UiUtils.showView(doneWithDisplayNameEdit, false);
                                            userDisplayNameView.setCursorVisible(false);
                                            userDisplayNameView.setBackground(new ColorDrawable(ContextCompat.getColor(UserProfileActivity.this, android.R.color.transparent)));
                                            UiUtils.dismissKeyboard(userDisplayNameView);
                                            UiUtils.showSafeToast("Success!");
                                        } else {
                                            UiUtils.showSafeToast("Error updating display name. Please review your data connection");
                                        }
                                    }
                                });
                            }
                        });
                    }
                });
                if (!userAge.equals(AppConstants.UNKNOWN)) {
                    ageView.setText(WordUtils.capitalize(", " + userAge));
                    ageView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            configureAgeAndGender();
                        }
                    });
                }
            } else {
                if (UiUtils.canShowAge(parseUser, AppConstants.ENTITY_TYPE_CLOSEBY, null)) {
                    if (!userAge.equals(AppConstants.UNKNOWN)) {
                        userDisplayNameView.setText(WordUtils.capitalize(", " + userAge));
                    }
                }
            }

            String userProfilePhotoUrl = parseUser.getString(AppConstants.APP_USER_PROFILE_PHOTO_URL);
            if (StringUtils.isNotEmpty(userProfilePhotoUrl)) {
                UiUtils.loadImage(UserProfileActivity.this, userProfilePhotoUrl, userAvatarView);
            }
            String userCoverPhoto = parseUser.getString(AppConstants.APP_USER_COVER_PHOTO);
            if (StringUtils.isNotEmpty(userCoverPhoto)) {
                UiUtils.loadImage(UserProfileActivity.this, userCoverPhoto, signedInUserCoverPhotoView);
            } else {
                if (StringUtils.isNotEmpty(userProfilePhotoUrl)) {
                    UiUtils.loadImage(UserProfileActivity.this, userProfilePhotoUrl, signedInUserCoverPhotoView);
                }
            }

            fetchCommonalities(parseUser);
            fetchFeaturedPhotos(parseUser);

        }

    }

    private void configureAgeAndGender() {
        Intent genderAndAgeIntent = new Intent(UserProfileActivity.this, GenderAndAgeConfigurationActivity.class);
        genderAndAgeIntent.putExtra(AppConstants.CAN_LAUNCH_MAIN, false);
        startActivityForResult(genderAndAgeIntent, RequestCodes.CONFIGURE_BIRTHDAY_AND_GENDER);
    }

    @SuppressLint("SetTextI18n")
    private void fetchFeaturedPhotos(ParseUser parseUser) {
        ParseUser signedInUser = ParseUser.getCurrentUser();
        List<String> featuredPhotos = parseUser.getList(AppConstants.APP_USER_FEATURED_PHOTOS);
        if (signedInUser.getObjectId().equals(parseUser.getObjectId())) {
            if (featuredPhotos == null || featuredPhotos.isEmpty()) {
                featurePhotosInstruction.setBackground(ContextCompat.getDrawable(UserProfileActivity.this, R.drawable.get_started_button_background));
                featurePhotosInstruction.setText("Hi " + WordUtils.capitalize(signedInUser.getString(AppConstants.APP_USER_DISPLAY_NAME)) + ", tap here to add some featured photos");
                loadFeaturedPhotosPlaceHolder(parseUser);
            } else {
                setupFeaturedPhotos(parseUser);
            }
        } else {
            if (featuredPhotos == null || featuredPhotos.isEmpty()) {
                featurePhotosInstruction.setText("" + WordUtils.capitalize(parseUser.getString(AppConstants.APP_USER_DISPLAY_NAME)) + " doesn't have any featured photos yet");
                loadFeaturedPhotosPlaceHolder(parseUser);
            } else {
                setupFeaturedPhotos(parseUser);
            }
        }
    }

    private void loadFeaturedPhotosPlaceHolder(ParseUser parseUser) {
        String userProfilePhotoUrl = parseUser.getString(AppConstants.APP_USER_PROFILE_PHOTO_URL);
        if (StringUtils.isNotEmpty(userProfilePhotoUrl)) {
            UiUtils.loadImage(UserProfileActivity.this, userProfilePhotoUrl, featuredPhotosPlaceHolderImageView);
        }
    }

    private void setupFeaturedPhotos(ParseUser parseUser) {

    }

    private void fetchCommonalities(final ParseUser parseUser) {
        final ParseUser signedInUser = ParseUser.getCurrentUser();
        if (signedInUser != null) {
            if (signedInUser.getObjectId().equals(parseUser.getObjectId())) {
                List<String> aboutSignedInUser = signedInUser.getList(AppConstants.ABOUT_USER);
                if (aboutSignedInUser != null) {
                    aboutUserTextView.setText(WordUtils.capitalize(aboutSignedInUser.get(0)));
                    UiUtils.showView(startChatView,false);
                }
            } else {
                UiUtils.showView(startChatView,true);
                startChatView.setImageResource(R.drawable.chat_tab);
                List<String> aboutUser = parseUser.getList(AppConstants.ABOUT_USER);
                List<String> aboutSignedInUser = signedInUser.getList(AppConstants.ABOUT_USER);
                if (aboutUser != null && aboutSignedInUser != null) {
                    try {
                        List<String> common = new ArrayList<>(aboutUser);
                        common.retainAll(aboutSignedInUser);
                        String firstInterest = !common.isEmpty() ? common.get(0) : aboutUser.get(0);
                        aboutUserTextView.setText(StringUtils.capitalize(firstInterest));
                    } catch (NullPointerException ignored) {

                    }
                }
            }
            handleUserOnlineStatus(parseUser);
            aboutUserTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (aboutUserRecyclerView.getVisibility() != View.VISIBLE) {
                        aboutUserRecyclerView.setVisibility(View.VISIBLE);
                        if (signedInUser.getObjectId().equals(parseUser.getObjectId())) {
                            editAboutYou.setVisibility(View.VISIBLE);
                        }
                    } else {
                        aboutUserRecyclerView.setVisibility(View.GONE);
                        UiUtils.showView(editAboutYou, false);
                    }
                    editAboutYou.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            Intent editAboutYouIntent = new Intent(UserProfileActivity.this, AboutUserActivity.class);
                            editAboutYouIntent.putExtra(AppConstants.CAN_LAUNCH_MAIN, false);
                            startActivityForResult(editAboutYouIntent, RequestCodes.UPDATE_ABOUT_YOU);
                        }
                    });
                }
            });
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RequestCodes.UPDATE_ABOUT_YOU) {
            if (resultCode == RESULT_OK) {
                if (parseUser != null) {
                    loadUserDetails();
                }
            }
        }
    }

    private void handleUserOnlineStatus(ParseUser parseUser) {
        Long userLastSeenAt = parseUser.getLong(AppConstants.APP_USER_LAST_SEEN);
        if (HolloutUtils.isNetWorkConnected(ApplicationLoader.getInstance())
                && parseUser.getString(AppConstants.APP_USER_ONLINE_STATUS).
                equals(AppConstants.ONLINE)) {
            attachDrawableToTextView(ApplicationLoader.getInstance(), onlineStatusView, R.drawable.ic_online, UiUtils.DrawableDirection.LEFT);
            onlineStatusView.setText(getString(R.string.online));
        } else {
            removeAllDrawablesFromTextView(onlineStatusView);
            onlineStatusView.setText(UiUtils.getLastSeen(userLastSeenAt));
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem searchItem = menu.findItem(R.id.action_search);
        searchItem.setVisible(false);
        MenuItem settingsActionItem = menu.findItem(R.id.action_settings);
        settingsActionItem.setVisible(false);
        supportInvalidateOptionsMenu();
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.go_back:
                onBackPressed();
                break;
        }
    }

    @Override
    public void onBackPressed() {
        if (aboutUserRecyclerView.getVisibility() == View.VISIBLE) {
            aboutUserTextView.performClick();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (isFinishing())
            overridePendingTransition(R.anim.slide_from_right, R.anim.fade_scale_out);
    }

}
