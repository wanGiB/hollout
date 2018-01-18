package com.wan.hollout.ui.activities;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.amulyakhare.textdrawable.TextDrawable;
import com.flaviofaria.kenburnsview.KenBurnsView;
import com.liucanwen.app.headerfooterrecyclerview.HeaderAndFooterRecyclerViewAdapter;
import com.liucanwen.app.headerfooterrecyclerview.RecyclerViewUtils;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.wan.hollout.R;
import com.wan.hollout.callbacks.DoneCallback;
import com.wan.hollout.components.ApplicationLoader;
import com.wan.hollout.eventbuses.RemovableChatRequestEvent;
import com.wan.hollout.ui.adapters.FeaturedPhotosRectangleAdapter;
import com.wan.hollout.ui.adapters.PeopleToMeetAdapter;
import com.wan.hollout.ui.widgets.ChatRequestView;
import com.wan.hollout.ui.widgets.CircleImageView;
import com.wan.hollout.ui.widgets.HolloutEditText;
import com.wan.hollout.ui.widgets.HolloutTextView;
import com.wan.hollout.utils.AppConstants;
import com.wan.hollout.utils.AuthUtil;
import com.wan.hollout.utils.FontUtils;
import com.wan.hollout.utils.HolloutUtils;
import com.wan.hollout.utils.RequestCodes;
import com.wan.hollout.utils.UiUtils;

import net.alhazmy13.mediapicker.Image.ImagePicker;

import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.WordUtils;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.wan.hollout.utils.UiUtils.attachDrawableToTextView;
import static com.wan.hollout.utils.UiUtils.removeAllDrawablesFromTextView;

/**
 * @author Wan Clem
 */

@SuppressWarnings("unchecked")
public class UserProfileActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int UPLOAD_ACTION_TYPE_PROFILE_PHOTO = 0x10;
    private static final int UPLOAD_ACTION_TYPE_COVER_PHOTO = 0x20;
    private static final int UPLOAD_ACTION_TYPE_FEATURED_PHOTO = 0x30;

    private int currentUploadAction;

    @BindView(R.id.go_back)
    ImageView goBack;

    @BindView(R.id.online_status)
    HolloutTextView onlineStatusView;

    @BindView(R.id.signed_in_user_cover_image_view)
    KenBurnsView signedInUserCoverPhotoView;

    @BindView(R.id.user_display_name)
    HolloutEditText userDisplayNameView;

    @BindView(R.id.user_profile_photo_view)
    CircleImageView userProfilePhotoView;

    @BindView(R.id.about_user)
    TextView aboutUserTextView;

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

    @BindView(R.id.user_gender)
    CircleImageView userGenderView;

    @BindView(R.id.featured_photos_recycler_view)
    RecyclerView featuredPhotosRecyclerView;

    @BindView(R.id.featured_photos_dim_view)
    View featuredPhotosDimView;

    @BindView(R.id.user_status)
    HolloutTextView userStatusTextView;

    @BindView(R.id.scroller)
    ScrollView scrollView;

    @BindView(R.id.chat_request_view)
    ChatRequestView chatRequestView;

    private ParseObject parseUser;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
        ButterKnife.bind(this);
        aboutUserTextView.setTypeface(FontUtils.selectTypeface(this, 4));
        offloadIntent();
        initClickListeners();
        checkAndRegEventBus();
    }

    private void initClickListeners() {
        goBack.setOnClickListener(this);
    }

    private void offloadIntent() {
        Bundle intentExtras = getIntent().getExtras();
        if (intentExtras != null) {
            parseUser = intentExtras.getParcelable(AppConstants.USER_PROPERTIES);
        }
        loadUserDetails(parseUser);
        resolveReceivedChatRequestIfAny();
    }

    private void resolveReceivedChatRequestIfAny() {
        ParseObject signedInUser = AuthUtil.getCurrentUser();
        if (signedInUser != null) {
            ParseQuery<ParseObject> chatRequestsQuery = ParseQuery.getQuery(AppConstants.HOLLOUT_FEED);
            chatRequestsQuery.whereEqualTo(AppConstants.FEED_TYPE, AppConstants.FEED_TYPE_CHAT_REQUEST);
            chatRequestsQuery.include(AppConstants.FEED_CREATOR);
            chatRequestsQuery.whereEqualTo(AppConstants.FEED_RECIPIENT_ID, signedInUser.getString(AppConstants.REAL_OBJECT_ID));
            chatRequestsQuery.whereEqualTo(AppConstants.FEED_CREATOR_ID, parseUser.getString(AppConstants.REAL_OBJECT_ID).toLowerCase());
            chatRequestsQuery.getFirstInBackground(new GetCallback<ParseObject>() {
                @Override
                public void done(ParseObject object, ParseException e) {
                    if (e == null && object != null) {
                        //This user sent a chat request to signed in user
                        UiUtils.showView(chatRequestView, true);
                        chatRequestView.bindData(UserProfileActivity.this, null, object);
                    }
                }
            });
        }
    }

    private void loadUserDetails(ParseObject parseUser) {
        if (parseUser != null) {
            loadUserProfile(parseUser);
            offloadUserAboutsIfAvailable(parseUser);
        }
    }

    private void offloadUserAboutsIfAvailable(ParseObject parseUser) {
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
    private void loadUserProfile(final ParseObject parseUser) {
        final ParseObject signedInUser = AuthUtil.getCurrentUser();
        if (signedInUser != null) {
            String username = parseUser.getString(AppConstants.APP_USER_DISPLAY_NAME);
            String userAge = parseUser.getString(AppConstants.APP_USER_AGE);
            String userLocation = HolloutUtils.resolveToBestLocation(parseUser);
            ParseGeoPoint userGeoPoint = parseUser.getParseGeoPoint(AppConstants.APP_USER_GEO_POINT);
            ParseGeoPoint signedInUserGeoPoint = signedInUser.getParseGeoPoint(AppConstants.APP_USER_GEO_POINT);
            String distanceToUser = String.valueOf(Math.rint(RandomUtils.nextDouble(0, 10)));
            if (signedInUserGeoPoint != null && userGeoPoint != null) {
                double distanceInKills = signedInUserGeoPoint.distanceInKilometersTo(userGeoPoint);
                distanceToUser = HolloutUtils.formatDistance(distanceInKills);
            }
            if (signedInUser.getString(AppConstants.REAL_OBJECT_ID).equals(parseUser.getString(AppConstants.REAL_OBJECT_ID))) {
                if (userLocation != null) {
                    userLocationAndDistanceView.setText(userLocation);
                } else {
                    userLocationAndDistanceView.setText(distanceToUser + "KM from nearby kinds");
                }
            } else {
                if (UiUtils.canShowLocation(parseUser, AppConstants.ENTITY_TYPE_CLOSEBY, null)) {
                    if (StringUtils.isNotEmpty(userLocation)) {
                        userLocationAndDistanceView.setText(userLocation + ", " + distanceToUser + "KM from you");
                    } else {
                        userLocationAndDistanceView.setText(distanceToUser + "KM from you");
                    }
                } else {
                    userLocationAndDistanceView.setText(distanceToUser + "KM from you");
                }
            }

            String userStatus = parseUser.getString(AppConstants.APP_USER_STATUS);
            if (StringUtils.isNotEmpty(userStatus)) {
                userStatusTextView.setText(StringUtils.capitalize(userStatus));
            }

            if (parseUser.getString(AppConstants.REAL_OBJECT_ID).equals(signedInUser.getString(AppConstants.REAL_OBJECT_ID))) {
                userStatusTextView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent composeStatusIntent = new Intent(UserProfileActivity.this, ComposeStatusActivity.class);
                        startActivityForResult(composeStatusIntent, RequestCodes.COMPOSE_STATUS);
                    }
                });
            }
            UiUtils.attachDrawableToTextView(UserProfileActivity.this, userLocationAndDistanceView, R.drawable.ic_location_on, UiUtils.DrawableDirection.LEFT);

            userDisplayNameView.setText(WordUtils.capitalize(username));
            if (signedInUser.getString(AppConstants.REAL_OBJECT_ID).equals(parseUser.getString(AppConstants.REAL_OBJECT_ID))) {
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
                                AuthUtil.updateCurrentLocalUser(signedInUser, new DoneCallback<Boolean>() {
                                    @Override
                                    public void done(Boolean result, Exception e) {
                                        UiUtils.dismissProgressDialog();
                                        if (e == null) {
                                            dismissEditComponents();
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
                userDisplayNameView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
                    @Override
                    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                        if (actionId == EditorInfo.IME_ACTION_DONE) {
                            doneWithDisplayNameEdit.performClick();
                        }
                        return true;
                    }
                });
            } else {
                if (UiUtils.canShowAge(parseUser, AppConstants.ENTITY_TYPE_CLOSEBY, null)) {
                    if (!userAge.equals(AppConstants.UNKNOWN)) {
                        ageView.setText(WordUtils.capitalize(", " + userAge));
                    }
                }
            }

            String userGender = parseUser.getString(AppConstants.APP_USER_GENDER);
            if (!userGender.equals(AppConstants.UNKNOWN)) {
                UiUtils.showView(userGenderView, true);
                String firstChar = userGender.charAt(0) + "";
                int color = ContextCompat.getColor(UserProfileActivity.this, R.color.colorPrimary);
                TextDrawable.IBuilder builder = TextDrawable.builder()
                        .beginConfig()
                        .endConfig()
                        .round();
                TextDrawable colouredDrawable = builder.build(firstChar, color);
                Bitmap textBitmap = HolloutUtils.convertDrawableToBitmap(colouredDrawable);
                userGenderView.setImageBitmap(textBitmap);
            } else {
                UiUtils.showView(userGenderView, false);
            }

            if (signedInUser.getString(AppConstants.REAL_OBJECT_ID).equals(parseUser.getString(AppConstants.REAL_OBJECT_ID))) {
                userGenderView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        configureAgeAndGender();
                    }
                });
            }

            String userProfilePhotoUrl = parseUser.getString(AppConstants.APP_USER_PROFILE_PHOTO_URL);
            if (StringUtils.isNotEmpty(userProfilePhotoUrl)) {
                UiUtils.loadImage(UserProfileActivity.this, userProfilePhotoUrl, userProfilePhotoView);
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

            if (signedInUser.getString(AppConstants.REAL_OBJECT_ID).equals(parseUser.getString(AppConstants.REAL_OBJECT_ID))) {
                userProfilePhotoView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        AlertDialog.Builder profilePhotoOptionsBuilder = new AlertDialog.Builder(UserProfileActivity.this);
                        profilePhotoOptionsBuilder.setItems(new CharSequence[]{"Upload New Profile Photo", "View Photo"},
                                new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int which) {
                                        dialogInterface.dismiss();
                                        switch (which) {
                                            case 0:
                                                setCurrentUploadAction(UPLOAD_ACTION_TYPE_PROFILE_PHOTO);
                                                HolloutUtils.startImagePicker(UserProfileActivity.this);
                                                break;
                                            case 1:
                                                detailProfilePhoto(parseUser);
                                                break;
                                        }
                                    }
                                });
                        profilePhotoOptionsBuilder.create().show();
                    }
                });

                signedInUserCoverPhotoView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        AlertDialog.Builder coverPhotoOptionsBuilder = new AlertDialog.Builder(UserProfileActivity.this);
                        coverPhotoOptionsBuilder.setItems(new CharSequence[]{"Upload New Cover Photo", "View Cover Photo"}, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int which) {
                                switch (which) {
                                    case 0:
                                        setCurrentUploadAction(UPLOAD_ACTION_TYPE_COVER_PHOTO);
                                        HolloutUtils.startImagePicker(UserProfileActivity.this);
                                        break;
                                    case 1:
                                        detailCoverPhoto(parseUser);
                                        break;
                                }
                            }
                        });
                        coverPhotoOptionsBuilder.create().show();
                    }
                });
            } else {
                userProfilePhotoView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        detailProfilePhoto(parseUser);
                    }
                });
                signedInUserCoverPhotoView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        detailCoverPhoto(parseUser);
                    }
                });
            }
        }
        scrollView.smoothScrollTo(0, 0);
    }

    private void detailCoverPhoto(ParseObject parseUser) {
        String coverPhotoUrl = parseUser.getString(AppConstants.APP_USER_COVER_PHOTO);
        List<String> allPhotos = new ArrayList<>();
        if (StringUtils.isNotEmpty(coverPhotoUrl)) {
            String signedInUserProfilePhotoUrl = parseUser.getString(AppConstants.APP_USER_PROFILE_PHOTO_URL);
            List<String> featuredPhotos = parseUser.getList(AppConstants.APP_USER_FEATURED_PHOTOS);
            if (StringUtils.isNotEmpty(signedInUserProfilePhotoUrl)) {
                allPhotos.add(signedInUserProfilePhotoUrl);
            }
            if (featuredPhotos != null && !featuredPhotos.isEmpty()) {
                allPhotos.addAll(featuredPhotos);
            }
            openPhotoViewActivity(coverPhotoUrl, allPhotos, parseUser);
        }
    }

    private void detailProfilePhoto(ParseObject parseUser) {
        String signedInUserProfilePhotoUrl = parseUser.getString(AppConstants.APP_USER_PROFILE_PHOTO_URL);
        List<String> allPhotos = new ArrayList<>();
        if (StringUtils.isNotEmpty(signedInUserProfilePhotoUrl)) {
            List<String> featuredPhotos = parseUser.getList(AppConstants.APP_USER_FEATURED_PHOTOS);
            String coverPhotoUrl = parseUser.getString(AppConstants.APP_USER_COVER_PHOTO);
            if (StringUtils.isNotEmpty(coverPhotoUrl)) {
                allPhotos.add(coverPhotoUrl);
            }
            if (featuredPhotos != null && !featuredPhotos.isEmpty()) {
                allPhotos.addAll(featuredPhotos);
            }
            openPhotoViewActivity(signedInUserProfilePhotoUrl, allPhotos, parseUser);
        }
    }

    private void dismissEditComponents() {
        UiUtils.showView(doneWithDisplayNameEdit, false);
        userDisplayNameView.setCursorVisible(false);
        userDisplayNameView.setBackground(new ColorDrawable(ContextCompat.getColor(UserProfileActivity.this, android.R.color.transparent)));
        UiUtils.dismissKeyboard(userDisplayNameView);
    }

    private void configureAgeAndGender() {
        Intent genderAndAgeIntent = new Intent(UserProfileActivity.this, GenderAndAgeConfigurationActivity.class);
        genderAndAgeIntent.putExtra(AppConstants.CAN_LAUNCH_MAIN, false);
        startActivityForResult(genderAndAgeIntent, RequestCodes.CONFIGURE_BIRTHDAY_AND_GENDER);
    }

    @SuppressLint("SetTextI18n")
    private void fetchFeaturedPhotos(ParseObject parseUser) {
        ParseObject signedInUser = AuthUtil.getCurrentUser();
        List<String> featuredPhotos = parseUser.getList(AppConstants.APP_USER_FEATURED_PHOTOS);
        if (signedInUser != null) {
            if (signedInUser.getString(AppConstants.REAL_OBJECT_ID).equals(parseUser.getString(AppConstants.REAL_OBJECT_ID))) {
                if (featuredPhotos == null || featuredPhotos.isEmpty()) {
                    featurePhotosInstruction.setBackground(ContextCompat.getDrawable(UserProfileActivity.this, R.drawable.get_started_button_background));
                    featurePhotosInstruction.setText("Hi " + WordUtils.capitalize(signedInUser.getString(AppConstants.APP_USER_DISPLAY_NAME)) + ", tap here to feature some photos");
                    loadFeaturedPhotosPlaceHolder(parseUser);
                    featurePhotosInstruction.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            addNewFeaturedPhoto();
                        }
                    });
                    featuredPhotosPlaceHolderImageView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            featurePhotosInstruction.performClick();
                        }
                    });
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
    }

    private void loadFeaturedPhotosPlaceHolder(ParseObject parseUser) {
        String userProfilePhotoUrl = parseUser.getString(AppConstants.APP_USER_PROFILE_PHOTO_URL);
        if (StringUtils.isNotEmpty(userProfilePhotoUrl)) {
            UiUtils.loadImage(UserProfileActivity.this, userProfilePhotoUrl, featuredPhotosPlaceHolderImageView);
        }
    }

    private void setupFeaturedPhotos(ParseObject parseUser) {
        UiUtils.showView(featuredPhotosDimView, false);
        UiUtils.showView(featurePhotosInstruction, false);
        UiUtils.showView(featuredPhotosPlaceHolderImageView, false);
        List<String> featuredPhotos = parseUser.getList(AppConstants.APP_USER_FEATURED_PHOTOS);

        FeaturedPhotosRectangleAdapter featuredPhotosRectangleAdapter = new FeaturedPhotosRectangleAdapter(this,
                featuredPhotos,
                parseUser.getString(AppConstants.APP_USER_DISPLAY_NAME), parseUser.getString(AppConstants.REAL_OBJECT_ID));

        HeaderAndFooterRecyclerViewAdapter headerAndFooterRecyclerViewAdapter = new HeaderAndFooterRecyclerViewAdapter(featuredPhotosRectangleAdapter);
        featuredPhotosRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        featuredPhotosRecyclerView.setAdapter(headerAndFooterRecyclerViewAdapter);

        ParseObject signedInUser = AuthUtil.getCurrentUser();
        if (signedInUser != null && signedInUser.getString(AppConstants.REAL_OBJECT_ID).equals(parseUser.getString(AppConstants.REAL_OBJECT_ID))) {
            @SuppressLint("InflateParams")
            View footerView = getLayoutInflater().inflate(R.layout.add_more_featured_photo_view, null);
            RecyclerViewUtils.setFooterView(featuredPhotosRecyclerView, footerView);
            footerView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    addNewFeaturedPhoto();
                }
            });
        }
    }

    private void addNewFeaturedPhoto() {
        setCurrentUploadAction(UPLOAD_ACTION_TYPE_FEATURED_PHOTO);
        HolloutUtils.startImagePicker(this);
    }

    private void fetchCommonalities(final ParseObject parseUser) {
        final ParseObject signedInUser = AuthUtil.getCurrentUser();
        if (signedInUser != null) {
            if (signedInUser.getString(AppConstants.REAL_OBJECT_ID).equals(parseUser.getString(AppConstants.REAL_OBJECT_ID))) {
                List<String> aboutSignedInUser = signedInUser.getList(AppConstants.ABOUT_USER);
                if (aboutSignedInUser != null) {
                    aboutUserTextView.setText(WordUtils.capitalize(aboutSignedInUser.get(0)));
                    UiUtils.showView(startChatView, false);
                }
            } else {
                UiUtils.showView(startChatView, true);
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
                startChatView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        if (chatRequestView.getVisibility() == View.VISIBLE) {
                            UiUtils.showProgressDialog(UserProfileActivity.this, "Please wait...");
                            //Automatically accept chat request
                            chatRequestView.acceptChatRequest();
                            return;
                        }
                        Intent chatIntent = new Intent(UserProfileActivity.this, ChatActivity.class);
                        parseUser.put(AppConstants.CHAT_TYPE, AppConstants.CHAT_TYPE_SINGLE);
                        chatIntent.putExtra(AppConstants.USER_PROPERTIES, parseUser);
                        startActivity(chatIntent);
                    }
                });
            }
            handleUserOnlineStatus(parseUser);
            aboutUserTextView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (aboutUserRecyclerView.getVisibility() != View.VISIBLE) {
                        aboutUserRecyclerView.setVisibility(View.VISIBLE);
                        if (signedInUser.getString(AppConstants.REAL_OBJECT_ID).equals(parseUser.getString(AppConstants.REAL_OBJECT_ID))) {
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
        if (requestCode == RequestCodes.UPDATE_ABOUT_YOU
                || requestCode == RequestCodes.CONFIGURE_BIRTHDAY_AND_GENDER
                || requestCode == RequestCodes.COMPOSE_STATUS) {
            if (resultCode == RESULT_OK) {
                if (parseUser != null) {
                    loadUserDetails(AuthUtil.getCurrentUser());
                }
            }
        } else if (requestCode == ImagePicker.IMAGE_PICKER_REQUEST_CODE && resultCode == RESULT_OK) {
            List<String> mPaths = (List<String>) data.getSerializableExtra(ImagePicker.EXTRA_IMAGE_PATH);
            if (mPaths != null && !mPaths.isEmpty()) {
                String pickedPhotoFilePath = mPaths.get(0);
                if (pickedPhotoFilePath != null) {
                    int currentAction = getCurrentUploadAction();
                    if (currentAction == UPLOAD_ACTION_TYPE_COVER_PHOTO || currentAction == UPLOAD_ACTION_TYPE_PROFILE_PHOTO) {
                        if (currentAction == UPLOAD_ACTION_TYPE_PROFILE_PHOTO) {
                            UiUtils.showProgressDialog(UserProfileActivity.this, "Updating Profile Photo");
                        } else {
                            UiUtils.showProgressDialog(UserProfileActivity.this, "Updating Cover Photo");
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
                                                        UiUtils.dismissProgressDialog();
                                                        if (e == null) {
                                                            UiUtils.showSafeToast("Upload Success");
                                                            if (parseUser != null) {
                                                                loadUserDetails(AuthUtil.getCurrentUser());
                                                            }
                                                        } else {
                                                            UiUtils.showSafeToast("An error occurred while updating photo please try again");
                                                        }
                                                    }
                                                });
                                            } else {
                                                UiUtils.showSafeToast("An error occurred while updating photo.Invalid session");
                                                Intent splashIntent = new Intent(UserProfileActivity.this, SplashActivity.class);
                                                startActivity(splashIntent);
                                                finish();
                                            }
                                        } else {
                                            UiUtils.dismissProgressDialog();
                                            UiUtils.showSafeToast("An error occurred while updating photo. Please try again.");
                                        }
                                    }
                                });
                            }
                        });
                    } else {
                        UiUtils.showProgressDialog(UserProfileActivity.this, "Adding Feature Photo");
                        HolloutUtils.uploadFileAsync(pickedPhotoFilePath, AppConstants.PHOTO_DIRECTORY, new DoneCallback<String>() {
                            @Override
                            public void done(final String result, final Exception e) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        if (e == null && result != null) {
                                            ParseObject signedInUser = AuthUtil.getCurrentUser();
                                            if (signedInUser != null) {
                                                List<String> featuredPhotos = signedInUser.getList(AppConstants.APP_USER_FEATURED_PHOTOS);
                                                if (featuredPhotos != null) {
                                                    if (!featuredPhotos.contains(result)) {
                                                        featuredPhotos.add(result);
                                                    }
                                                } else {
                                                    featuredPhotos = new ArrayList<>();
                                                    featuredPhotos.add(result);
                                                }
                                                signedInUser.put(AppConstants.APP_USER_FEATURED_PHOTOS, featuredPhotos);
                                                AuthUtil.updateCurrentLocalUser(signedInUser, new DoneCallback<Boolean>() {
                                                    @Override
                                                    public void done(Boolean result, Exception e) {
                                                        UiUtils.dismissProgressDialog();
                                                        if (e == null) {
                                                            UiUtils.showSafeToast("Photo featured successfully");
                                                            if (parseUser != null) {
                                                                loadUserDetails(AuthUtil.getCurrentUser());
                                                            }
                                                        } else {
                                                            UiUtils.showSafeToast("An error occurred while featuring photo please try again");
                                                        }
                                                    }
                                                });
                                            } else {
                                                UiUtils.showSafeToast("An error occurred while featuring photo.Invalid session");
                                                Intent splashIntent = new Intent(UserProfileActivity.this, SplashActivity.class);
                                                startActivity(splashIntent);
                                                finish();
                                            }
                                        } else {
                                            UiUtils.dismissProgressDialog();
                                            UiUtils.showSafeToast("An error occurred while updating photo. Please try again.");
                                        }
                                    }
                                });
                            }
                        });

                    }
                }
            }
        }
    }

    private void openPhotoViewActivity(String photo, List<String> photos, ParseObject parseUser) {
        Intent mProfilePhotoViewIntent = new Intent(UserProfileActivity.this, SlidePagerActivity.class);
        mProfilePhotoViewIntent.putExtra(AppConstants.EXTRA_TITLE, parseUser.getString(AppConstants.APP_USER_DISPLAY_NAME));
        ArrayList<String> photoExtras = new ArrayList<>();
        photoExtras.add(0, photo);
        for (String photoItem : photos) {
            if (!photoExtras.contains(photoItem) && !photoItem.equals(photo)) {
                photoExtras.add(photoItem);
            }
        }
        mProfilePhotoViewIntent.putStringArrayListExtra(AppConstants.EXTRA_PICTURES, photoExtras);
        startActivity(mProfilePhotoViewIntent);
    }

    private void handleUserOnlineStatus(ParseObject parseUser) {
        Long userLastSeenAt = parseUser.getLong(AppConstants.APP_USER_LAST_SEEN);
        if (HolloutUtils.isNetWorkConnected(UserProfileActivity.this) && parseUser.getString(AppConstants.APP_USER_ONLINE_STATUS).equals(AppConstants.ONLINE)) {
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

        MenuItem filterPeopleMenuItem = menu.findItem(R.id.filter_people);
        MenuItem createNewGroupItem = menu.findItem(R.id.create_new_group);

        createNewGroupItem.setVisible(false);
        filterPeopleMenuItem.setVisible(false);

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
        } else if (doneWithDisplayNameEdit.getVisibility() == View.VISIBLE) {
            dismissEditComponents();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        checkAndRegEventBus();
    }

    @Override
    protected void onStop() {
        super.onStop();
        checkAnUnRegEventBus();
    }

    private void checkAndRegEventBus() {
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
    }

    private void checkAnUnRegEventBus() {
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (isFinishing()) overridePendingTransition(R.anim.fade_scale_in, R.anim.slide_to_right);
    }

    public void setCurrentUploadAction(int currentUploadAction) {
        this.currentUploadAction = currentUploadAction;
    }

    public int getCurrentUploadAction() {
        return currentUploadAction;
    }

    @SuppressWarnings("unused")
    @Subscribe(sticky = true, threadMode = ThreadMode.ASYNC)
    public void onEventAsync(final Object o) {
        UiUtils.runOnMain(new Runnable() {
            @Override
            public void run() {
                if (o instanceof RemovableChatRequestEvent) {
                    RemovableChatRequestEvent removableChatRequestEvent = (RemovableChatRequestEvent) o;
                    ParseObject removableChatRequest = removableChatRequestEvent.getRemovableChatRequest();
                    if (removableChatRequest != null) {
                        UiUtils.showView(chatRequestView, false);
                    }
                } else if (o instanceof String) {
                    String s = (String) o;
                    if (s.equals(AppConstants.REMOVE_SOMETHING)) {
                        if (isAContact(parseUser.getString(AppConstants.REAL_OBJECT_ID).toLowerCase())) {
                            UiUtils.dismissProgressDialog();
                            UiUtils.showView(chatRequestView, false);
                            startChatView.performClick();
                        }
                    }
                }
                EventBus.getDefault().removeAllStickyEvents();
            }
        });
    }

    private boolean isAContact(String recipientId) {
        ParseObject signedInUser = AuthUtil.getCurrentUser();
        if (signedInUser != null) {
            List<String> signedInUserChats = signedInUser.getList(AppConstants.APP_USER_CHATS);
            return (signedInUserChats != null && signedInUserChats.contains(recipientId.toLowerCase()));
        }
        return false;
    }

}
