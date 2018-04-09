package com.wan.hollout.ui.activities;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.ScrollView;
import android.widget.TextView;

import com.flaviofaria.kenburnsview.KenBurnsView;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.liucanwen.app.headerfooterrecyclerview.HeaderAndFooterRecyclerViewAdapter;
import com.liucanwen.app.headerfooterrecyclerview.RecyclerViewUtils;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;
import com.mikepenz.iconics.typeface.IIcon;
import com.parse.DeleteCallback;
import com.parse.FindCallback;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseGeoPoint;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.wan.hollout.R;
import com.wan.hollout.components.ApplicationLoader;
import com.wan.hollout.interfaces.DoneCallback;
import com.wan.hollout.ui.adapters.FeaturedPhotosRectangleAdapter;
import com.wan.hollout.ui.widgets.CircleImageView;
import com.wan.hollout.ui.widgets.HolloutTextView;
import com.wan.hollout.utils.AppConstants;
import com.wan.hollout.utils.AuthUtil;
import com.wan.hollout.utils.FontUtils;
import com.wan.hollout.utils.HolloutPreferences;
import com.wan.hollout.utils.HolloutUtils;
import com.wan.hollout.utils.RequestCodes;
import com.wan.hollout.utils.UiUtils;

import net.alhazmy13.mediapicker.Image.ImagePicker;

import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.WordUtils;
import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.HashMap;
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
    HolloutTextView userDisplayNameView;

    @BindView(R.id.user_profile_photo_view)
    CircleImageView userProfilePhotoView;

    @BindView(R.id.about_user)
    TextView aboutUserTextView;

    @BindView(R.id.start_chat_or_edit_profile_view)
    ImageView startChatView;

    @BindView(R.id.settings_icon)
    ImageView settingsIcon;

    @BindView(R.id.delete_account)
    Button deleteAccountButton;

    @BindView(R.id.user_location_and_distance)
    HolloutTextView userLocationAndDistanceView;

    @BindView(R.id.feature_photos_instruction)
    HolloutTextView featurePhotosInstruction;

    @BindView(R.id.featured_photos_place_holder_image)
    ImageView featuredPhotosPlaceHolderImageView;

    @BindView(R.id.age_view)
    HolloutTextView ageView;

    @BindView(R.id.featured_photos_recycler_view)
    RecyclerView featuredPhotosRecyclerView;

    @BindView(R.id.featured_photos_dim_view)
    View featuredPhotosDimView;

    @BindView(R.id.user_status)
    HolloutTextView userStatusTextView;

    @BindView(R.id.scroller)
    ScrollView scrollView;

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
    }

    private void initClickListeners() {
        goBack.setOnClickListener(this);
        deleteAccountButton.setOnClickListener(this);
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
        if (signedInUser != null && parseUser != null) {
            final ParseQuery<ParseObject> chatRequestsQuery = ParseQuery.getQuery(AppConstants.HOLLOUT_FEED);
            chatRequestsQuery.whereEqualTo(AppConstants.FEED_TYPE, AppConstants.FEED_TYPE_CHAT_REQUEST);
            chatRequestsQuery.include(AppConstants.FEED_CREATOR);
            chatRequestsQuery.whereEqualTo(AppConstants.FEED_RECIPIENT_ID, signedInUser.getString(AppConstants.REAL_OBJECT_ID).toLowerCase());
            chatRequestsQuery.whereEqualTo(AppConstants.FEED_CREATOR_ID, parseUser.getString(AppConstants.REAL_OBJECT_ID).toLowerCase());
            chatRequestsQuery.getFirstInBackground(new GetCallback<ParseObject>() {
                @Override
                public void done(final ParseObject object, ParseException e) {
                    if (e == null && object != null) {
                        Snackbar.make(featuredPhotosRecyclerView, "This user sent you a chat request",
                                Snackbar.LENGTH_INDEFINITE).setAction("ACCEPT", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                HolloutUtils.acceptOrDeclineChat(object, true, parseUser.getString(AppConstants.REAL_OBJECT_ID),
                                        parseUser.getString(AppConstants.APP_USER_DISPLAY_NAME));
                            }
                        }).show();
                    }
                    chatRequestsQuery.cancel();
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
    }

    @SuppressLint("SetTextI18n")
    private void loadUserProfile(final ParseObject parseUser) {
        final ParseObject signedInUser = AuthUtil.getCurrentUser();
        if (signedInUser != null) {
            String username = parseUser.getString(AppConstants.APP_USER_DISPLAY_NAME);
            String userAge = parseUser.getString(AppConstants.APP_USER_AGE);
            ParseGeoPoint userGeoPoint = parseUser.getParseGeoPoint(AppConstants.APP_USER_GEO_POINT);
            ParseGeoPoint signedInUserGeoPoint = signedInUser.getParseGeoPoint(AppConstants.APP_USER_GEO_POINT);
            String distanceToUser = String.valueOf(Math.rint(RandomUtils.nextDouble(0, 10)));
            if (signedInUserGeoPoint != null && userGeoPoint != null) {
                double distanceInKills = signedInUserGeoPoint.distanceInKilometersTo(userGeoPoint);
                distanceToUser = HolloutUtils.formatDistance(distanceInKills);
            }
            if (signedInUser.getString(AppConstants.REAL_OBJECT_ID).equals(parseUser.getString(AppConstants.REAL_OBJECT_ID))) {
                userLocationAndDistanceView.setText("");

            } else {
                if (UiUtils.canShowLocation(parseUser, AppConstants.ENTITY_TYPE_CLOSEBY, new HashMap<String, Object>())) {
                    String formattedDistance = HolloutUtils.formatDistanceToUser(distanceToUser);
                    userLocationAndDistanceView.setText(formattedDistance + "KM");
                } else {
                    userLocationAndDistanceView.setText("");
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
                UiUtils.showView(deleteAccountButton, true);
            } else {
                UiUtils.showView(deleteAccountButton, false);
            }

            userDisplayNameView.setText(WordUtils.capitalize(username));
            if (UiUtils.canShowAge(parseUser, AppConstants.ENTITY_TYPE_CLOSEBY, new HashMap<String, Object>())) {
                if (!userAge.equals(AppConstants.UNKNOWN)) {
                    ageView.setText(WordUtils.capitalize(", " + userAge));
                }
            }
            if (signedInUser.getString(AppConstants.REAL_OBJECT_ID).equals(parseUser.getString(AppConstants.REAL_OBJECT_ID))) {
                ageView.setText(WordUtils.capitalize(", " + userAge));
            }

            String userProfilePhotoUrl = parseUser.getString(AppConstants.APP_USER_PROFILE_PHOTO_URL);
            if (StringUtils.isNotEmpty(userProfilePhotoUrl)) {
                UiUtils.loadImage(UserProfileActivity.this, userProfilePhotoUrl, userProfilePhotoView);
            } else {
                userProfilePhotoView.setImageResource(R.drawable.empty_profile);
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

    private Drawable getDrawableFromIcon(IIcon icon) {
        return new IconicsDrawable(this)
                .sizeDp(18)
                .icon(icon);
    }

    private void launchSettings(String settingsFragmentName) {
        Intent settingsIntent = new Intent(UserProfileActivity.this, SettingsActivity.class);
        settingsIntent.putExtra(AppConstants.SETTINGS_FRAGMENT_NAME, settingsFragmentName);
        startActivity(settingsIntent);
    }

    private void iniSettingsPopupMenu() {
        PopupMenu popupMenu = new PopupMenu(this, settingsIcon);
        Menu menu = popupMenu.getMenu();
        menu.add(1, 3, 3, "Notification Settings").setIcon(getDrawableFromIcon(GoogleMaterial.Icon.gmd_notifications));
        menu.add(1, 4, 4, "Chats & Calls Settings").setIcon(getDrawableFromIcon(GoogleMaterial.Icon.gmd_chat));
        menu.add(1, 5, 5, "Privacy Settings").setIcon(getDrawableFromIcon(GoogleMaterial.Icon.gmd_security));
        menu.add(1, 6, 6, "Support Settings").setIcon(getDrawableFromIcon(GoogleMaterial.Icon.gmd_help));
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (item.getItemId() == 3) {
                    launchSettings(AppConstants.NOTIFICATION_SETTINGS_FRAGMENT);
                } else if (item.getItemId() == 4) {
                    launchSettings(AppConstants.CHATS_SETTINGS_FRAGMENT);
                } else if (item.getItemId() == 5) {
                    launchSettings(AppConstants.PRIVACY_AND_SECURITY_FRAGMENT);
                } else if (item.getItemId() == 6) {
                    launchSettings(AppConstants.SUPPORT_SETTINGS_FRAGMENT);
                }
                return true;
            }
        });
        popupMenu.show();
    }

    private void tryDeleteAccount() {
        AlertDialog.Builder deleteAccountBuilder = new AlertDialog.Builder(UserProfileActivity.this);
        deleteAccountBuilder.setTitle("Delete Account");
        deleteAccountBuilder.setMessage("This would permanently remove your account from Hollout");
        deleteAccountBuilder.setPositiveButton("DELETE", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dissolveLoggedInUser();
            }
        });
        deleteAccountBuilder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        deleteAccountBuilder.create().show();
    }

    private void deleteObjectReferences(String signedInUserId, final DoneCallback<Boolean> objectReferenceDeletionCallBack) {
        final ParseQuery<ParseObject> userQuery = ParseQuery.getQuery(AppConstants.HOLLOUT_FEED);
        userQuery.whereEqualTo(AppConstants.FEED_RECIPIENT_ID, signedInUserId);
        userQuery.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> objects, ParseException e) {
                if (objects != null) {
                    ParseObject.deleteAllInBackground(objects, new DeleteCallback() {
                        @Override
                        public void done(ParseException e) {
                            objectReferenceDeletionCallBack.done(true, null);
                        }
                    });
                } else {
                    objectReferenceDeletionCallBack.done(true, null);
                }
                userQuery.cancel();
            }
        });
    }

    private void dissolveLoggedInUser() {
        final ProgressDialog progressDialog = ProgressDialog.show(this, "Deleting your account", "Please wait...");
        final ParseObject signedInUserObject = AuthUtil.getCurrentUser();
        if (signedInUserObject != null) {
            String signedInUserId = signedInUserObject.getString(AppConstants.REAL_OBJECT_ID);
            deleteObjectReferences(signedInUserId, new DoneCallback<Boolean>() {
                @Override
                public void done(Boolean result, Exception e) {
                    proceedWithAccountDissolution(progressDialog, signedInUserObject);
                }
            });
        }
    }

    private void proceedWithAccountDissolution(final ProgressDialog progressDialog, final ParseObject signedInUserObject) {
        if (signedInUserObject != null) {
            String signedInUserId = signedInUserObject.getString(AppConstants.REAL_OBJECT_ID);
            if (StringUtils.isNotEmpty(signedInUserId)) {
                final ParseQuery<ParseObject> userQuery = ParseQuery.getQuery(AppConstants.PEOPLE_GROUPS_AND_ROOMS);
                userQuery.whereEqualTo(AppConstants.REAL_OBJECT_ID, signedInUserId);
                userQuery.findInBackground(new FindCallback<ParseObject>() {
                    @Override
                    public void done(List<ParseObject> objects, ParseException e) {
                        if (objects != null && !objects.isEmpty()) {
                            ParseObject.deleteAllInBackground(objects, new DeleteCallback() {
                                @Override
                                public void done(ParseException e) {
                                    if (e == null) {
                                        HolloutPreferences.setUserWelcomed(false);
                                        HolloutPreferences.clearPersistedCredentials();
                                        HolloutPreferences.getInstance().getAll().clear();
                                        ParseObject.unpinAllInBackground(AppConstants.APP_USERS);
                                        ParseObject.unpinAllInBackground(AppConstants.HOLLOUT_FEED);
                                        HolloutUtils.getKryoInstance().reset();
                                        signedInUserObject.unpinInBackground(AppConstants.AUTHENTICATED_USER_DETAILS);
                                        FirebaseAuth.getInstance().signOut();
                                        AuthUtil.signOut(UserProfileActivity.this)
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        if (task.isSuccessful()) {
                                                            UiUtils.dismissProgressDialog(progressDialog);
                                                            UiUtils.showSafeToast("Your account was deleted successfully!");
                                                            startActivity(new Intent(UserProfileActivity.this, SplashActivity.class));
                                                            EventBus.getDefault().postSticky(AppConstants.ACCOUNT_DELETED_EVENT);
                                                            finish();
                                                        } else {
                                                            UiUtils.dismissProgressDialog(progressDialog);
                                                            UiUtils.showSafeToast("Failed to sign you out.Please try again");
                                                        }
                                                    }
                                                });
                                    } else {
                                        UiUtils.dismissProgressDialog(progressDialog);
                                        UiUtils.showSafeToast("Error deleting profile. Please can you try again.");
                                    }
                                }
                            });
                        } else {
                            UiUtils.dismissProgressDialog(progressDialog);
                            UiUtils.showSafeToast("Error deleting profile. Please can you try again.");
                        }
                        userQuery.cancel();
                    }
                });
            } else {
                UiUtils.dismissProgressDialog(progressDialog);
                UiUtils.showSafeToast("No valid user session exists for account dissolution.");
            }
        } else {
            UiUtils.dismissProgressDialog(progressDialog);
            UiUtils.showSafeToast("No valid user session exists for account dissolution.");
        }
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
                    setupFeaturedPhotos(AuthUtil.getCurrentUser());
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
                    aboutUserTextView.setText(WordUtils.capitalize(TextUtils.join(",", aboutSignedInUser)));
                    startChatView.setImageResource(R.drawable.edit_my_profile);
                    settingsIcon.setImageDrawable(new IconicsDrawable(this, GoogleMaterial.Icon.gmd_settings).color(Color.WHITE).sizeDp(20));
                    settingsIcon.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            iniSettingsPopupMenu();
                        }
                    });
                }
                aboutUserTextView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent editAboutYouIntent = new Intent(UserProfileActivity.this, AboutUserActivity.class);
                        editAboutYouIntent.putExtra(AppConstants.CAN_LAUNCH_MAIN, false);
                        startActivityForResult(editAboutYouIntent, RequestCodes.UPDATE_ABOUT_YOU);
                    }
                });
                startChatView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent editProfileIntent = new Intent(UserProfileActivity.this, EditProfileActivity.class);
                        startActivity(editProfileIntent);
                    }
                });
            } else {
                startChatView.setImageResource(R.drawable.home_chat_icon);
                List<String> aboutUser = parseUser.getList(AppConstants.ABOUT_USER);
                if (aboutUser != null) {
                    aboutUserTextView.setText(WordUtils.capitalize(TextUtils.join(",", aboutUser)));
                }
                startChatView.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent chatIntent = new Intent(UserProfileActivity.this, ChatActivity.class);
                        parseUser.put(AppConstants.CHAT_TYPE, AppConstants.CHAT_TYPE_SINGLE);
                        chatIntent.putExtra(AppConstants.USER_PROPERTIES, parseUser);
                        startActivity(chatIntent);
                    }
                });
            }
            handleUserOnlineStatus(parseUser);
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
                final String pickedPhotoFilePath = mPaths.get(0);
                if (pickedPhotoFilePath != null) {
                    final int currentAction = getCurrentUploadAction();
                    if (currentAction == UPLOAD_ACTION_TYPE_COVER_PHOTO || currentAction == UPLOAD_ACTION_TYPE_PROFILE_PHOTO) {
//                        AlertDialog.Builder cropConsentDialog = new AlertDialog.Builder(UserProfileActivity.this);
//                        cropConsentDialog.setMessage("Crop Photo ?");
//                        cropConsentDialog.setPositiveButton("CROP", new DialogInterface.OnClickListener() {
//                            @Override
//                            public void onClick(DialogInterface dialog, int which) {
//                                Crop.of(Uri.fromFile(new File(pickedPhotoFilePath)), Uri.fromFile(new File(getCacheDir(), "cropped")))
//                                        .asSquare().start(UserProfileActivity.this);
//                            }
//                        }).setNegativeButton("DON'T CROP", new DialogInterface.OnClickListener() {
//                            @Override
//                            public void onClick(DialogInterface dialog, int which) {
//                            }
//                        });
//                        cropConsentDialog.create().show();
                        prepareForUpload(pickedPhotoFilePath, currentAction);
                    } else {
                        final ProgressDialog progressDialog = UiUtils.showProgressDialog(UserProfileActivity.this, "Featuring Photo");
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
                                                        UiUtils.dismissProgressDialog(progressDialog);
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
                                            UiUtils.dismissProgressDialog(progressDialog);
                                            UiUtils.showSafeToast("An error occurred while updating photo. Please try again.");
                                        }
                                    }
                                });
                            }
                        });
                    }
                }
            }
        } /*else if (requestCode == Crop.REQUEST_CROP) {
            if (resultCode == Activity.RESULT_OK) {
                Uri result = Crop.getOutput(data);
                if (result != null) {
                    prepareForUpload(result.getPath(), getCurrentUploadAction());
                }
            }
        }*/
    }

    private void prepareForUpload(String pickedPhotoFilePath, int currentAction) {
        final ProgressDialog progressDialog;
        if (currentAction == UPLOAD_ACTION_TYPE_PROFILE_PHOTO) {
            progressDialog = UiUtils.showProgressDialog(UserProfileActivity.this, "Updating Profile Photo");
        } else {
            progressDialog = UiUtils.showProgressDialog(UserProfileActivity.this, "Updating Cover Photo");
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
                            UiUtils.dismissProgressDialog(progressDialog);
                            UiUtils.showSafeToast("An error occurred while updating photo. Please try again.");
                        }
                    }
                });
            }
        });
    }

    private void openPhotoViewActivity(String photo, List<String> photos, ParseObject parseUser) {
        Intent mProfilePhotoViewIntent = new Intent(UserProfileActivity.this, SlidePagerActivity.class);
        mProfilePhotoViewIntent.putExtra(AppConstants.EXTRA_TITLE, parseUser.getString(AppConstants.APP_USER_DISPLAY_NAME));
        mProfilePhotoViewIntent.putExtra(AppConstants.EXTRA_USER_ID, parseUser.getString(AppConstants.REAL_OBJECT_ID));
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
        ParseObject signedInUser = AuthUtil.getCurrentUser();
        Long userLastSeenAt = parseUser.getLong(AppConstants.USER_CURRENT_TIME_STAMP) != 0
                ? parseUser.getLong(AppConstants.USER_CURRENT_TIME_STAMP) :
                parseUser.getLong(AppConstants.APP_USER_LAST_SEEN);
        if (signedInUser != null) {
            if (HolloutUtils.isNetWorkConnected(UserProfileActivity.this) && parseUser.getLong(AppConstants.USER_CURRENT_TIME_STAMP) == signedInUser.getLong(AppConstants.USER_CURRENT_TIME_STAMP)) {
                attachDrawableToTextView(ApplicationLoader.getInstance(), onlineStatusView, R.drawable.ic_online, UiUtils.DrawableDirection.LEFT);
                onlineStatusView.setText(getString(R.string.online));
            } else {
                removeAllDrawablesFromTextView(onlineStatusView);
                onlineStatusView.setText(UiUtils.getLastSeen(userLastSeenAt));
            }
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        MenuItem searchItem = menu.findItem(R.id.action_search);

        searchItem.setVisible(false);

        MenuItem filterPeopleMenuItem = menu.findItem(R.id.filter_people);
//        MenuItem createNewGroupItem = menu.findItem(R.id.create_new_group);

//        createNewGroupItem.setVisible(false);
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
            case R.id.delete_account:
                tryDeleteAccount();
                break;
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
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

}
