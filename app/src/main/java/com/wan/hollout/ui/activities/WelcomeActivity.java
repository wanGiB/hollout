package com.wan.hollout.ui.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;

import com.crashlytics.android.Crashlytics;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.parse.SignUpCallback;
import com.wan.hollout.R;
import com.wan.hollout.eventbuses.TypingFinishedBus;
import com.wan.hollout.ui.widgets.HolloutTextView;
import com.wan.hollout.ui.widgets.ShimmerFrameLayout;
import com.wan.hollout.utils.AppConstants;
import com.wan.hollout.utils.HolloutLogger;
import com.wan.hollout.utils.HolloutPreferences;
import com.wan.hollout.utils.RequestCodes;
import com.wan.hollout.utils.TypingSimulationConstants;
import com.wan.hollout.utils.UiUtils;

import org.apache.commons.lang3.StringUtils;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import butterknife.BindView;
import butterknife.ButterKnife;


public class WelcomeActivity extends AppCompatActivity implements GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, View.OnClickListener {

    @BindView(R.id.shimmer_view_container)
    ShimmerFrameLayout shimmerFrameLayout;

    @BindView(R.id.blinking_divider)
    HolloutTextView blinkingDivider;

    @BindView(R.id.typing_text_view)
    HolloutTextView typingTextView;

    @BindView(R.id.button_login_facebook)
    Button continueWithFacebook;

    @BindView(R.id.button_login_google)
    Button continueWithGoogle;

    @BindView(R.id.app_intro_message)
    HolloutTextView appIntroMessageView;

    /* Is there a ConnectionResult resolution in progress? */
    private boolean mIsResolving = false;
    /* Should we automatically resolve ConnectionResults when possible? */
    private boolean mShouldResolve = false;
    /* RequestCode for resolutions involving sign-in */
    private static final int RC_SIGN_IN = 9001;

    /* Client for accessing Google APIs */
    private GoogleApiClient mGoogleApiClient;
    //Facebook Callback Manager
    private CallbackManager mCallbackManager;

    private FirebaseAuth firebaseAuth;
    private FirebaseAuth.AuthStateListener addAuthStateListener;

    private int fieldIndex = 0;

    String[] sentences = {
            "Your Sport Team Fans",
            "People in your field of study",
            "People who share in your passion",
            "People who share in your interests",
            "People who share in your concerns",
            "People people of like minds",
            "Any one like you!!!"
    };

    private String TAG = "WelcomeActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);
        ButterKnife.bind(this);
        checkAndRegEventBus();
        initGoogleApiStuffs();
        firebaseAuth = FirebaseAuth.getInstance();
        addAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
                if (firebaseUser != null) {
                    authenticateUserOnParse(firebaseUser);
                }
            }
        };
        appIntroMessageView.setText(UiUtils.fromHtml("<font color=#0096DE>Connect</font> and <font color=#3EB890>Holla</font> people of shared interests and profession <font color=#70CADB>around</font> you."));
        continueWithFacebook.setOnClickListener(this);
        continueWithGoogle.setOnClickListener(this);
        initDividerBlinkingAnimation();
        startTypingAnimation(fieldIndex);
    }

    private void authenticateUserOnParse(final FirebaseUser firebaseUser) {
        ParseUser.logInInBackground(firebaseUser.getUid(), firebaseUser.getUid(), new LogInCallback() {
            @Override
            public void done(ParseUser user, ParseException e) {
                UiUtils.dismissProgressDialog();
                if (e == null) {
                    if (user != null) {
                        HolloutPreferences.persistCredentials(firebaseUser.getUid(), firebaseUser.getUid());
                        setupCrashlyticsUser(firebaseUser);
                        finishUp();
                    } else {
                        createNewUserOnParse(firebaseUser);
                    }
                } else {
                    if (e.getCode() == ParseException.USERNAME_MISSING || e.getCode() == ParseException.OBJECT_NOT_FOUND) {
                        createNewUserOnParse(firebaseUser);
                    } else {
                        UiUtils.showSafeToast("An error while authenticating you. Please try again");
                    }
                }
            }
        });
    }

    private void finishUp() {
        ParseUser currentUser = ParseUser.getCurrentUser();
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
        Intent genderAndAgeIntent = new Intent(WelcomeActivity.this, GenderAndAgeConfigurationActivity.class);
        startActivityForResult(genderAndAgeIntent, RequestCodes.CONFIGURE_BIRTHDAY_AND_GENDER);
    }

    private void navigateToAboutActivity() {
        Intent aboutUserIntent = new Intent(WelcomeActivity.this, AboutUserActivity.class);
        startActivity(aboutUserIntent);
        finish();
    }

    private void navigateToMainActivity() {
        Intent mainIntent = new Intent(WelcomeActivity.this, MainActivity.class);
        startActivity(mainIntent);
        finish();
    }

    private void createNewUserOnParse(final FirebaseUser firebaseUser) {
        UiUtils.showProgressDialog(this, "Creating account...");
        setupCrashlyticsUser(firebaseUser);
        ParseUser newHolloutUser = new ParseUser();
        newHolloutUser.setUsername(firebaseUser.getUid());
        newHolloutUser.setPassword(firebaseUser.getUid());
        if (firebaseUser.getDisplayName() != null) {
            newHolloutUser.put(AppConstants.APP_USER_DISPLAY_NAME, firebaseUser.getDisplayName().toLowerCase());
        }
        if (firebaseUser.getEmail() != null) {
            newHolloutUser.setEmail(firebaseUser.getEmail());
        }
        if (firebaseUser.getPhotoUrl() != null) {
            newHolloutUser.put(AppConstants.APP_USER_PROFILE_PHOTO_URL, firebaseUser.getPhotoUrl().toString());
        }
        newHolloutUser.put(AppConstants.PLAY_SOUND_ON_NEW_MESAGE_NOTIF, true);
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
        newHolloutUser.put(AppConstants.USER_PROFILE_PHOTO_UPLOAD_TIME, System.currentTimeMillis());
        newHolloutUser.signUpInBackground(new SignUpCallback() {
            @Override
            public void done(ParseException e) {
                UiUtils.dismissProgressDialog();
                if (e == null) {
                    HolloutPreferences.persistCredentials(firebaseUser.getUid(), firebaseUser.getUid());
                    finishUp();
                } else {
                    String errorMessage = e.getMessage();
                    if (StringUtils.isNotEmpty(errorMessage)) {
                        if (!StringUtils.containsIgnoreCase(errorMessage, "i/o")) {
                            UiUtils.showSafeToast(errorMessage);
                        } else {
                            UiUtils.showSafeToast(errorMessage);
                        }
                    } else {
                        UiUtils.showSafeToast("An unresolvable error occurred during authentication. Please try again");
                    }
                }
            }
        });
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

    private void initFacebookLogin() {
        HolloutLogger.d(TAG, "Facebook init");

        mCallbackManager = CallbackManager.Factory.create();
        ArrayList<String> permissions = new ArrayList<>();

        permissions.add("email");
        permissions.add("public_profile");

        LoginManager.getInstance().registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {

            @Override
            public void onSuccess(LoginResult loginResult) {

                if (loginResult != null) {
                    HolloutLogger.d(TAG, "Login Result is not null");
                    final AccessToken accessToken = loginResult.getAccessToken();
                    if (accessToken != null) {
                        HolloutLogger.d(TAG, "Access token is not null");
                        //check declined permissions
                        Set<String> declinedPermissions = accessToken.getDeclinedPermissions();
                        if (declinedPermissions.isEmpty()) {
                            handleFacebookSignInResult(accessToken);
                        } else {
                            UiUtils.showSafeToast("Declined Permissions, For you to sign in with your " +
                                    "Facebook account, we require your email and your basic public profile, " +
                                    "kindly try again, and grant access");
                        }

                    }

                } else {
                    HolloutLogger.d(TAG, "Login result is null");
                }

            }

            @Override
            public void onCancel() {
                HolloutLogger.d(TAG, "User has canceled Login Dialog");
            }

            @Override
            public void onError(FacebookException e) {
                e.printStackTrace();
                HolloutLogger.d(TAG, "Facebook Authentication Error  = " + e.getMessage());
                Snackbar.make(continueWithFacebook, e.getMessage(), Snackbar.LENGTH_SHORT)
                        .setAction(R.string.text_retry, new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                initFacebookLogin();
                            }
                        }).show();
            }

        });

        LoginManager.getInstance().logInWithReadPermissions(this, permissions);

    }

    private void initGoogleLogin() {
        UiUtils.showProgressDialog(WelcomeActivity.this, "Please wait...");
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    private void handleFacebookSignInResult(AccessToken accessToken) {
        UiUtils.showProgressDialog(WelcomeActivity.this, "Please wait...");
        AuthCredential credential = FacebookAuthProvider.getCredential(accessToken.getToken());
        firebaseAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (!task.isSuccessful()) {
                            UiUtils.showSafeToast("LogIn Aborted");
                        }
                    }
                });
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        startTypingAnimation(0);
    }

    private void initDividerBlinkingAnimation() {
        Animation blinkingAnimation = AnimationUtils.loadAnimation(this, android.R.anim.fade_in);
        blinkingAnimation.setRepeatMode(Animation.REVERSE);
        blinkingAnimation.setDuration(450);
        blinkingAnimation.setRepeatCount(Animation.INFINITE);
        blinkingAnimation.setInterpolator(new AccelerateDecelerateInterpolator());
        blinkingDivider.startAnimation(blinkingAnimation);
    }

    private void startTypingAnimation(int index) {
        if (index == sentences.length) {
            index = 0;
        }
        fieldIndex = index;
        String currentlyTypedWord = sentences[index];
        TypingSimulationConstants.CURRENTLY_TYPED_WORD = currentlyTypedWord;
        typingTextView.animateText(currentlyTypedWord);
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
            if (resultCode == RESULT_OK) {
                GoogleSignInResult result = Auth.GoogleSignInApi.getSignInResultFromIntent(data);
                handleSignInResult(result);
                mIsResolving = false;
                if (!mGoogleApiClient.isConnected()) {
                    mGoogleApiClient.connect();
                }
            } else {
                mShouldResolve = false;
            }
        } else if (requestCode == RequestCodes.CONFIGURE_BIRTHDAY_AND_GENDER) {
            finishUp();
        } else {
            mCallbackManager.onActivityResult(requestCode, resultCode, data);
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
                                        UiUtils.showSafeToast("LogIn Aborted");
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
            case R.id.button_login_facebook:
                initFacebookLogin();
                break;
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

    @SuppressWarnings("unused")
    @Subscribe(sticky = true, threadMode = ThreadMode.ASYNC)
    public void onEventAsync(final Object o) {
        UiUtils.runOnMain(new Runnable() {
            @Override
            public void run() {
                if (o instanceof TypingFinishedBus) {
                    TypingFinishedBus typingFinishedBus = (TypingFinishedBus) o;
                    if (typingFinishedBus.isTypingFinished()) {

                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                startTypingAnimation(fieldIndex + 1);
                            }
                        }, 3000);
                    }
                }
            }
        });
    }
}
