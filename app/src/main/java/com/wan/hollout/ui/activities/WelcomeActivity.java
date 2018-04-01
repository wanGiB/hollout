package com.wan.hollout.ui.activities;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.crashlytics.android.Crashlytics;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.parse.GetCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.SaveCallback;
import com.sinch.gson.stream.JsonReader;
import com.truecaller.android.sdk.ITrueCallback;
import com.truecaller.android.sdk.TrueButton;
import com.truecaller.android.sdk.TrueClient;
import com.truecaller.android.sdk.TrueError;
import com.truecaller.android.sdk.TrueProfile;
import com.wan.hollout.R;
import com.wan.hollout.clients.CallClient;
import com.wan.hollout.clients.ChatClient;
import com.wan.hollout.interfaces.DoneCallback;
import com.wan.hollout.models.ChatMessage;
import com.wan.hollout.ui.widgets.HolloutTextView;
import com.wan.hollout.ui.widgets.ShimmerFrameLayout;
import com.wan.hollout.utils.AppConstants;
import com.wan.hollout.utils.AuthUtil;
import com.wan.hollout.utils.DbUtils;
import com.wan.hollout.utils.FirebaseUtils;
import com.wan.hollout.utils.HolloutLogger;
import com.wan.hollout.utils.HolloutPreferences;
import com.wan.hollout.utils.JsonUtils;
import com.wan.hollout.utils.RequestCodes;
import com.wan.hollout.utils.UiUtils;

import org.apache.commons.lang3.StringUtils;
import org.greenrobot.eventbus.EventBus;

import java.io.StringReader;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;


public class WelcomeActivity extends BaseActivity
        implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, View.OnClickListener, ITrueCallback {

    @BindView(R.id.shimmer_view_container)
    ShimmerFrameLayout shimmerFrameLayout;

    @BindView(R.id.button_login_google)
    Button continueWithGoogle;

    @BindView(R.id.app_intro_message)
    HolloutTextView appIntroMessageView;

    @BindView(R.id.com_truecaller_android_sdk_truebutton)
    TrueButton trueButton;

    /* Is there a ConnectionResult resolution in progress? */
    private boolean mIsResolving = false;
    /* Should we automatically resolve ConnectionResults when possible? */
    private boolean mShouldResolve = false;
    /* RequestCode for resolutions involving sign-in */
    private static final int RC_SIGN_IN = 9001;

    /* Client for accessing Google APIs */
    private GoogleApiClient mGoogleApiClient;
    //Facebook Callback Manager

    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener addAuthStateListener;

    private String TAG = "WelcomeActivity";
    private TrueClient mTrueClient;

    private ProgressDialog progressDialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        ButterKnife.bind(this);
        initGoogleApiStuffs();
        firebaseAuth = FirebaseAuth.getInstance();
        addAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
                if (firebaseUser != null) {
                    authenticateUser(firebaseUser);
                }
            }
        };
        boolean usable = trueButton.isUsable();
        if (usable) {
            mTrueClient = new TrueClient(this, this);
            mTrueClient.setReqNonce("12345678Min");
            trueButton.setTrueClient(mTrueClient);
        } else {
            trueButton.setVisibility(View.GONE);
        }
        appIntroMessageView.setText(UiUtils.fromHtml("<font color=#0096DE>Connect</font>,  <font color=#E8A723>Discover</font> and meet new people around you."));
        continueWithGoogle.setOnClickListener(this);
    }

    private void authenticateUser(final FirebaseUser firebaseUser) {
        ParseQuery<ParseObject> peopleQuery = ParseQuery.getQuery(AppConstants.PEOPLE_GROUPS_AND_ROOMS);
        peopleQuery.whereEqualTo(AppConstants.REAL_OBJECT_ID, getValidAppUserId(firebaseUser));
        peopleQuery.whereEqualTo(AppConstants.APP_USER_PASSWORD, getValidAppUserId(firebaseUser));
        peopleQuery.getFirstInBackground(new GetCallback<ParseObject>() {
            @Override
            public void done(ParseObject object, ParseException e) {
                if (e == null && object != null) {
                    AuthUtil.createLocalUser(object);
                    HolloutPreferences.persistCredentials(firebaseUser.getUid(), firebaseUser.getUid());
                    setupCrashlyticsUser(firebaseUser);
                    startChatClient();
                    UiUtils.dismissProgressDialog(progressDialog);
                    finishUp(true);
                } else {
                    if (e != null) {
                        if (e.getCode() == ParseException.USERNAME_MISSING || e.getCode() == ParseException.OBJECT_NOT_FOUND) {
                            createNewUserOnParse(firebaseUser);
                        } else {
                            UiUtils.showSafeToast("An error occurred while authenticating you. Please try again");
                            UiUtils.dismissProgressDialog(progressDialog);
                            Crashlytics.logException(e);
                        }
                    }
                }
            }
        });
    }

    private void startChatClient() {
        ChatClient.getInstance().startChatClient();
        CallClient.getInstance().startCallClient();
    }

    private void finishUp(boolean comingFromLogIn) {
        UiUtils.dismissProgressDialog(progressDialog);
        progressDialog = UiUtils.showProgressDialog(getCurrentActivityInstance(), "Please wait...");
        if (comingFromLogIn) {
            tryRetrieveChats(progressDialog, new DoneCallback<Boolean>() {
                @Override
                public void done(Boolean signInSuccess, Exception e) {
                    if (e == null && signInSuccess) {
                        navigateToAppropriateScreen();
                    } else {
                        UiUtils.dismissProgressDialog(progressDialog);
                        if (e != null) {
                            HolloutLogger.d("FetchedMessagesString", "Error fetching chats" + e.getMessage());
                        }
                        navigateToAppropriateScreen();
                    }
                }
            });
        } else {
            navigateToAppropriateScreen();
        }
    }

    protected void tryRetrieveChats(final ProgressDialog progressDialog, final DoneCallback<Boolean> backUpCompletedOptionCallback) {
        ParseObject signedInUser = AuthUtil.getCurrentUser();
        if (signedInUser != null) {
            FirebaseUtils.getArchives().child(signedInUser.getString(AppConstants.REAL_OBJECT_ID)).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(DataSnapshot dataSnapshot) {
                    if (dataSnapshot != null && dataSnapshot.exists()) {
                        String fetchedContent = dataSnapshot.getValue(String.class);
                        if (StringUtils.isNotEmpty(fetchedContent)) {
                            progressDialog.setMessage("Retrieving Messages...");
                            JsonReader jsonReader = new JsonReader(new StringReader(fetchedContent));
                            jsonReader.setLenient(true);
                            HolloutLogger.d("FetchedMessagesString", fetchedContent);
                            try {
                                List<ChatMessage> unSerializedMessages = JsonUtils.getGSon().fromJson(jsonReader, JsonUtils.getListType());
                                if (!unSerializedMessages.isEmpty()) {
                                    DbUtils.performBatchMessageInsertion(unSerializedMessages, new DoneCallback<Long[]>() {
                                        @Override
                                        public void done(Long[] progressValues, Exception e) {
                                            long current = progressValues[0];
                                            long total = progressValues[1];
                                            if (current != -1 && total != 0) {
                                                double percentage = (100.0 * (current + 1)) / total;
                                                if (percentage == 100) {
                                                    backUpCompletedOptionCallback.done(true, null);
                                                }
                                            } else {
                                                backUpCompletedOptionCallback.done(true, null);
                                            }
                                        }
                                    });
                                } else {
                                    backUpCompletedOptionCallback.done(true, null);
                                }
                            } catch (Exception e1) {
                                HolloutLogger.d("RudeBoy", e1.getMessage());
                            }
                        } else {
                            backUpCompletedOptionCallback.done(true, null);
                        }
                    } else {
                        backUpCompletedOptionCallback.done(true, null);
                    }
                }

                @Override
                public void onCancelled(DatabaseError databaseError) {
                    backUpCompletedOptionCallback.done(true, null);
                }
            });
        }
    }

    private void navigateToAppropriateScreen() {
        ParseObject currentUser = AuthUtil.getCurrentUser();
        if (currentUser != null) {
            List<String> aboutUser = currentUser.getList(AppConstants.ABOUT_USER);
            if (aboutUser != null) {
                if (!aboutUser.isEmpty()) {
                    String userAge = currentUser.getString(AppConstants.APP_USER_GENDER);
                    String userGender = currentUser.getString(AppConstants.APP_USER_GENDER);
                    if (userAge.equals(AppConstants.UNKNOWN) || userGender.equals(AppConstants.UNKNOWN)) {
                        launchGenderAndAgeActivity();
                    } else {
                        navigateToMainActivity();
                    }
                } else {
                    navigateToAboutActivity();
                }
            } else {
                navigateToAboutActivity();
            }
        }
    }

    private void launchGenderAndAgeActivity() {
        UiUtils.dismissProgressDialog(progressDialog);
        Intent genderAndAgeIntent = new Intent(WelcomeActivity.this, GenderAndAgeConfigurationActivity.class);
        startActivityForResult(genderAndAgeIntent, RequestCodes.CONFIGURE_BIRTHDAY_AND_GENDER);
    }

    private void navigateToAboutActivity() {
        UiUtils.dismissProgressDialog(progressDialog);
        Intent aboutUserIntent = new Intent(WelcomeActivity.this, AboutUserActivity.class);
        startActivity(aboutUserIntent);
        finish();
    }

    private void navigateToMainActivity() {
        UiUtils.dismissProgressDialog(progressDialog);
        Intent mainIntent = new Intent(WelcomeActivity.this, MainActivity.class);
        startActivity(mainIntent);
        finish();
    }

    private void createNewUserOnParse(final FirebaseUser firebaseUser) {
        UiUtils.dismissProgressDialog(progressDialog);
        progressDialog = UiUtils.showProgressDialog(this, "Authenticating account...");
        setupCrashlyticsUser(firebaseUser);
        final ParseObject newHolloutUser = new ParseObject(AppConstants.PEOPLE_GROUPS_AND_ROOMS);
        if (firebaseUser.getDisplayName() != null) {
            newHolloutUser.put(AppConstants.APP_USER_DISPLAY_NAME, firebaseUser.getDisplayName().toLowerCase());
        }
        if (firebaseUser.getPhotoUrl() != null) {
            newHolloutUser.put(AppConstants.APP_USER_PROFILE_PHOTO_URL, firebaseUser.getPhotoUrl().toString());
        }
        newHolloutUser.put(AppConstants.REAL_OBJECT_ID, getValidAppUserId(firebaseUser));
        newHolloutUser.put(AppConstants.APP_USER_PASSWORD, getValidAppUserId(firebaseUser));
        newHolloutUser.put(AppConstants.PLAY_SOUND_ON_NEW_MESAGE_NOTIF, true);
        newHolloutUser.put(AppConstants.OBJECT_TYPE, AppConstants.OBJECT_TYPE_INDIVIDUAL);
        newHolloutUser.put(AppConstants.WAKE_PHONE_ON_NOTIFICATION, true);
        newHolloutUser.put(AppConstants.SHOW_MESSAGE_TICKER, true);
        newHolloutUser.put(AppConstants.VIBRATE_ON_NEW_NOTIFICATION, true);
        newHolloutUser.put(AppConstants.MESSAGES_TEXT_SIZE, getString(R.string.text_size_default));
        newHolloutUser.put(AppConstants.SAVE_TO_GALLERY, true);
        newHolloutUser.put(AppConstants.APP_USER_LAST_SEEN, System.currentTimeMillis());
        newHolloutUser.put(AppConstants.LAST_SEEN_VISIBILITY_PREF, getString(R.string.anyone));
        newHolloutUser.put(AppConstants.LOCATION_VISIBILITY_PREF, getString(R.string.anyone));
        newHolloutUser.put(AppConstants.APP_USER_ONLINE_STATUS, AppConstants.ONLINE);
        newHolloutUser.put(AppConstants.AGE_VISIBILITY_PREF, getString(R.string.visible_to_only_me));
        newHolloutUser.put(AppConstants.APP_USER_GENDER, AppConstants.UNKNOWN);
        newHolloutUser.put(AppConstants.APP_USER_AGE, AppConstants.UNKNOWN);
        newHolloutUser.put(AppConstants.STATUS_VISIBILITY_PREF, getString(R.string.anyone));
        newHolloutUser.put(AppConstants.USER_PROFILE_PHOTO_UPLOAD_TIME, System.currentTimeMillis());
        newHolloutUser.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if (e == null) {
                    AuthUtil.createLocalUser(newHolloutUser);
                    HolloutPreferences.persistCredentials(firebaseUser.getUid(), firebaseUser.getUid());
                    startChatClient();
                    UiUtils.dismissProgressDialog(progressDialog);
                    finishUp(false);
                } else {
                    String errorMessage = e.getMessage();
                    if (StringUtils.isNotEmpty(errorMessage)) {
                        if (!StringUtils.containsIgnoreCase(errorMessage, "i/o")) {
                            UiUtils.showSafeToast(errorMessage);
                        } else {
                            UiUtils.showSafeToast(errorMessage);
                        }
                    } else {
                        UiUtils.showSafeToast("An unresolvable error occurred during authentication. Please try again ");
                        Crashlytics.logException(e);
                    }
                }
            }
        });
    }

    @NonNull
    private String getValidAppUserId(FirebaseUser firebaseUser) {
        return firebaseUser.getUid().trim().toLowerCase();
    }

    private void setupCrashlyticsUser(FirebaseUser firebaseUser) {
        if (firebaseUser.getEmail() != null) {
            Crashlytics.setUserEmail(firebaseUser.getEmail());
        }
        if (firebaseUser.getDisplayName() != null) {
            Crashlytics.setUserName(firebaseUser.getDisplayName());
        }
        Crashlytics.setUserIdentifier(firebaseUser.getUid());
    }

    private void initGoogleLogin() {
        UiUtils.dismissProgressDialog(progressDialog);
        progressDialog = UiUtils.showProgressDialog(WelcomeActivity.this, "Please wait...");
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    @Override
    protected void onStart() {
        super.onStart();
        checkAndRegEventBus();
        firebaseAuth.addAuthStateListener(addAuthStateListener);
        if (mGoogleApiClient != null && !mGoogleApiClient.isConnected()) {
            mGoogleApiClient.connect();
        }
    }

    private void initGoogleApiStuffs() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestProfile()
                .requestIdToken(getString(R.string.default_web_client_id))
                .build();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        if (!mIsResolving && mShouldResolve) {
            if (connectionResult.hasResolution()) {
                try {
                    connectionResult.startResolutionForResult(WelcomeActivity.this, RC_SIGN_IN);
                    mIsResolving = true;
                } catch (IntentSender.SendIntentException e) {
                    HolloutLogger.d("TAG", "Could not resolve ConnectionResult." + e.getMessage());
                    mIsResolving = false;
                    mGoogleApiClient.connect();
                }
            } else {
                // Could not resolve the connection result, show the user an error dialog.
                showErrorDialog();
            }
        }
    }

    private void showErrorDialog() {
        if (GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(WelcomeActivity.this) == ConnectionResult.SUCCESS) {
            // Show the default Google Play services error dialog which may still start an intent
            // on our behalf if the user can resolve the issue.
            GoogleApiAvailability.getInstance().getErrorDialog(WelcomeActivity.this,
                    RC_SIGN_IN, 0,
                    new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialog) {
                            mShouldResolve = false;
                            // updateUI(false);
                        }
                    }).show();
        } else {
            mShouldResolve = false;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            // If the error resolution was not successful we should not resolve further.
            GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
            if (resultCode == RESULT_OK) {
                handleSignInResult(result);
                mIsResolving = false;
                if (!mGoogleApiClient.isConnected()) {
                    mGoogleApiClient.connect();
                }
            } else {
                mShouldResolve = false;
                UiUtils.showSafeToast("Failed to log in via google. Please try again ");
                UiUtils.dismissProgressDialog(progressDialog);
            }
        } else if (requestCode == RequestCodes.CONFIGURE_BIRTHDAY_AND_GENDER) {
            finishUp(false);
        } else {
            if (mTrueClient != null) {
                mTrueClient.onActivityResult(requestCode, resultCode, data);
            }
        }

    }

    private void handleSignInResult(GoogleSignInResult result) {
        try {
            if (result.isSuccess()) {
                GoogleSignInAccount acct = result.getSignInAccount();
                if (acct != null) {
                    AuthCredential credential = GoogleAuthProvider.getCredential(acct.getIdToken(), null);
                    firebaseAuth.signInWithCredential(credential)
                            .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                                @Override
                                public void onComplete(@NonNull Task<AuthResult> task) {
                                    if (!task.isSuccessful()) {
                                        UiUtils.showSafeToast("Log In Aborted ");
                                        UiUtils.dismissProgressDialog(progressDialog);
                                    }
                                }
                            });
                }
            } else {
                HolloutLogger.d(TAG, "Could not get result");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.button_login_google:
                initGoogleLogin();
                break;
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        checkAndUnRegEventBus();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (shimmerFrameLayout != null) {
            shimmerFrameLayout.startShimmerAnimation();
        }
        checkAndRegEventBus();
    }

    @Override
    public void onPause() {
        if (shimmerFrameLayout != null) {
            shimmerFrameLayout.stopShimmerAnimation();
        }
        checkAndUnRegEventBus();
        if (isFinishing()) overridePendingTransition(R.anim.fade_scale_in, R.anim.slide_to_right);
        super.onPause();
    }

    private void checkAndRegEventBus() {
        if (!EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().register(this);
        }
    }

    private void checkAndUnRegEventBus() {
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
    }

    @Override
    public void onSuccesProfileShared(@NonNull final TrueProfile trueProfile) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                UiUtils.dismissProgressDialog(progressDialog);
                progressDialog = UiUtils.showProgressDialog(getCurrentActivityInstance(), "Please wait...");
                final String fullName = trueProfile.firstName + " " + trueProfile.lastName;
                String phoneNumber = trueProfile.phoneNumber;
                final String email = StringUtils.strip(StringUtils.deleteWhitespace(phoneNumber + "@hollout.com"), "+");
                final String password = "hollout";
                firebaseAuth.removeAuthStateListener(addAuthStateListener);
                firebaseAuth.createUserWithEmailAndPassword(email, password).addOnSuccessListener(new OnSuccessListener<AuthResult>() {
                    @Override
                    public void onSuccess(AuthResult authResult) {
                        UserProfileChangeRequest.Builder profileUpdates = new UserProfileChangeRequest.Builder();
                        profileUpdates.setDisplayName(fullName);
                        if (trueProfile.avatarUrl != null) {
                            profileUpdates.setPhotoUri(Uri.parse(trueProfile.avatarUrl));
                        }
                        if (firebaseAuth.getCurrentUser() != null) {
                            firebaseAuth.getCurrentUser().updateProfile(profileUpdates.build())
                                    .addOnSuccessListener(getCurrentActivityInstance(), new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            authenticateUser(firebaseAuth.getCurrentUser());
                                        }
                                    })
                                    .addOnFailureListener(getCurrentActivityInstance(), new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            UiUtils.dismissProgressDialog(progressDialog);
                                            UiUtils.showSafeToast(e.getMessage());
                                        }
                                    });
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        if (e instanceof FirebaseAuthUserCollisionException) {
                            //User already exists
                            firebaseAuth.signInWithEmailAndPassword(email, password)
                                    .addOnSuccessListener(getCurrentActivityInstance(), new OnSuccessListener<AuthResult>() {
                                        @Override
                                        public void onSuccess(AuthResult authResult) {
                                            authenticateUser(firebaseAuth.getCurrentUser());
                                        }
                                    })
                                    .addOnFailureListener(getCurrentActivityInstance(), new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            UiUtils.dismissProgressDialog(progressDialog);
                                            UiUtils.showSafeToast(e.getMessage());
                                        }
                                    });
                        } else {
                            UiUtils.dismissProgressDialog(progressDialog);
                            UiUtils.showSafeToast(e.getMessage());
                        }
                    }
                });
            }
        });
    }

    @Override
    public void onFailureProfileShared(@NonNull TrueError trueError) {
        UiUtils.showSafeToast("Error completing log in with true caller. Please try again.");
    }

}
