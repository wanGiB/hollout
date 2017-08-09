package com.wan.hollout.ui.activities;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatDelegate;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;

import com.afollestad.appthemeengine.ATE;
import com.afollestad.appthemeengine.customizers.ATEActivityThemeCustomizer;
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
import com.wan.hollout.R;
import com.wan.hollout.callbacks.DoneCallback;
import com.wan.hollout.entities.drawerMenu.DrawerItemCategory;
import com.wan.hollout.entities.drawerMenu.DrawerItemPage;
import com.wan.hollout.ui.fragments.DrawerFragment;
import com.wan.hollout.ui.fragments.MainFragment;
import com.wan.hollout.ui.widgets.RevealPopupWindow;
import com.wan.hollout.utils.AppConstants;
import com.wan.hollout.utils.FirebaseUtils;
import com.wan.hollout.utils.HolloutLogger;
import com.wan.hollout.utils.HolloutPreferences;
import com.wan.hollout.utils.UiUtils;

import org.apache.commons.lang3.StringUtils;
import org.greenrobot.eventbus.EventBus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Set;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends BaseActivity implements ATEActivityThemeCustomizer, GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, DrawerFragment.FragmentDrawerListener {

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

    private RevealPopupWindow signInOptionsWindow;
    private FirebaseAuth firebaseAuth;

    private FirebaseAuth.AuthStateListener addAuthStateListener;

    private boolean isDarkTheme;

    @BindView(R.id.drawer_layout)
    DrawerLayout drawer;

    @BindView(R.id.fragment_container)
    FrameLayout containerView;

    /**
     * Reference tied drawer menu, represented as fragment.
     */
    private Runnable homeRunnable = new Runnable() {

        @Override
        public void run() {
            navigateToMainFragment();
        }

    };

    private String TAG = "MainActivity";

    private DrawerFragment drawerFragment;

    private DoneCallback<Boolean> authenticationDoneCallback;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        isDarkTheme = HolloutPreferences.getHolloutPreferences().getBoolean("dark_theme", false);
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
        setContentView(R.layout.activity_main);
        firebaseAuth = FirebaseAuth.getInstance();
        ButterKnife.bind(this);
        initGoogleApiStuffs();
        initOAuthDialog();

        addAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                EventBus.getDefault().post(AppConstants.JUST_AUTHENTICATED);
                FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
                UiUtils.dismissProgressDialog();
                if (firebaseUser != null) {
                    UiUtils.showSafeToast("Welcome, " + firebaseUser.getDisplayName());
                    updateUserState(firebaseUser);
                    logUser(firebaseUser);
                    invalidateDrawerMenuHeader();
                    if (authenticationDoneCallback != null) {
                        authenticationDoneCallback.done(true, null);
                    }
                }
            }
        };

        homeRunnable.run();
        drawerFragment = (DrawerFragment) getSupportFragmentManager().findFragmentById(R.id.main_navigation_drawer_fragment);
        drawerFragment.setUp(drawer, this);
        HolloutPreferences.setUserWelcomed();
    }

    private void updateUserState(FirebaseUser firebaseUser) {
        HashMap<String, String> contributorProps = new HashMap<>();
        String userDisplayName = firebaseUser.getDisplayName();
        if (StringUtils.isNotEmpty(userDisplayName)) {
            contributorProps.put(AppConstants.USER_DISPLAY_NAME, userDisplayName.toLowerCase(Locale.getDefault()));
        }
        if (firebaseUser.getPhotoUrl() != null) {
            contributorProps.put(AppConstants.USER_PHOTO_URL, firebaseUser.getPhotoUrl().toString());
        }
        contributorProps.put(AppConstants.USE_ID, firebaseUser.getUid());
        if (firebaseUser.getEmail() != null) {
            contributorProps.put(AppConstants.USER_EMAIL, firebaseUser.getEmail());
        }
        FirebaseUtils.getUsersReference().child(firebaseUser.getUid()).setValue(contributorProps);
    }

    private void initOAuthDialog() {
        @SuppressLint("InflateParams") View oauthDialog = getLayoutInflater().inflate(R.layout.oauth_dialog, null);
        Button facebookLoginButton = ButterKnife.findById(oauthDialog, R.id.button_login_facebook);
        Button googleLoginButton = ButterKnife.findById(oauthDialog, R.id.button_login_google);
        View dismissableView = ButterKnife.findById(oauthDialog, R.id.dismissable_fram);
        signInOptionsWindow = new RevealPopupWindow(oauthDialog, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        dismissableView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (signInOptionsWindow.isShowing()) {
                    signInOptionsWindow.dismiss();
                }
            }
        });

        View.OnClickListener onClickListener = new View.OnClickListener() {

            @Override
            public void onClick(View view) {
                switch (view.getId()) {
                    case R.id.button_login_facebook:
                        dismissPopUps();
                        UiUtils.showProgressDialog(MainActivity.this, "Please wait...");
                        initFacebookLogin();
                        break;
                    case R.id.button_login_google:
                        dismissPopUps();
                        initGoogleLogin();
                        break;
                }

            }

        };
        facebookLoginButton.setOnClickListener(onClickListener);
        googleLoginButton.setOnClickListener(onClickListener);
    }


    public void initiateAuthentication(DoneCallback<Boolean> authenticationDoneCallback) {
        this.authenticationDoneCallback = authenticationDoneCallback;
        showSignInOptionsDialog();
    }

    private void showSignInOptionsDialog() {
        if (!signInOptionsWindow.isShowing()) {
            signInOptionsWindow.showAtLocation(containerView, Gravity.TOP, (int) containerView.getX(), (int) containerView.getY());
        }
    }


    private void initGoogleLogin() {
        Intent signInIntent = Auth.GoogleSignInApi.getSignInIntent(mGoogleApiClient);
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    private void handleFacebookSignInResult(AccessToken accessToken) {
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
    protected void onStart() {
        super.onStart();
        firebaseAuth.addAuthStateListener(addAuthStateListener);
        if (mGoogleApiClient != null && !mGoogleApiClient.isConnected()) {
            mGoogleApiClient.connect();
        }
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
                Snackbar.make(containerView, e.getMessage(), Snackbar.LENGTH_SHORT)
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

    private void dismissPopUps() {
        if (signInOptionsWindow.isShowing()) {
            signInOptionsWindow.dismiss();
        }
    }

    /**
     * Method checks if MainActivity instance exist. If so, then drawer menu header will be invalidated.
     */
    public void invalidateDrawerMenuHeader() {
        if (drawerFragment != null) {
            drawerFragment.invalidateHeader();
        }
    }

    private void logUser(FirebaseUser firebaseUser) {
        // You can call any combination of these three methods
        Crashlytics.setUserIdentifier(firebaseUser.getUid());
        if (firebaseUser.getEmail() != null) {
            Crashlytics.setUserEmail(firebaseUser.getEmail());
        }
        Crashlytics.setUserName(firebaseUser.getDisplayName());
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

    private void navigateToMainFragment() {
        Fragment fragment = new MainFragment();
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, fragment).commitAllowingStateLoss();
    }

    @Override
    public int getActivityTheme() {
        return isDarkTheme ? R.style.AppThemeNormalDark : R.style.AppThemeNormalLight;
    }

    @Override
    public void onBackPressed() {
        if (drawerFragment != null && drawerFragment.isSubMenuVisible()) {
            drawerFragment.animateSubListHide();
        } else if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else if (AppConstants.ARE_REACTIONS_OPEN) {
            EventBus.getDefault().post(AppConstants.CLOSE_REACTIONS);
        } else if (signInOptionsWindow != null) {
            if (signInOptionsWindow.isShowing()) {
                signInOptionsWindow.dismiss();
            } else {
                super.onBackPressed();
            }
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        ATE.applyMenu(this, getATEKey(), menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        } else if (id == android.R.id.home) {
            drawer.openDrawer(GravityCompat.START);
            return true;
        } else if (id == R.id.action_search) {
            //TODO:Open search bar here
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {
        if (!mIsResolving && mShouldResolve) {
            if (connectionResult.hasResolution()) {
                try {
                    connectionResult.startResolutionForResult(MainActivity.this, RC_SIGN_IN);
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
        if (GoogleApiAvailability.getInstance().isGooglePlayServicesAvailable(MainActivity.this) == ConnectionResult.SUCCESS) {
            // Show the default Google Play services error dialog which may still start an intent
            // on our behalf if the user can resolve the issue.
            GoogleApiAvailability.getInstance().getErrorDialog(MainActivity.this,
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
        getSupportFragmentManager().findFragmentById(R.id.fragment_container).onActivityResult(requestCode, resultCode, data);
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
        } else {
            mCallbackManager.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void handleSignInResult(GoogleSignInResult result) {
        UiUtils.showProgressDialog(this, "Please wait...");
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
    public void onConnected(@Nullable Bundle bundle) {

    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onDrawersNotificationsSelected() {

    }

    @Override
    public void onDrawerItemCategorySelected(DrawerItemCategory drawerItemCategory) {
        long categoryId = drawerItemCategory.getId();
        if (categoryId == DrawerFragment.LOG_OUT) {
            attemptLogOut();
        }
    }

    @Override
    public void onDrawerItemPageSelected(DrawerItemPage drawerItemPage) {

    }

    @Override
    public void onAccountSelected() {

    }

    private void attemptLogOut() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Attention!");
        builder.setMessage("Are you sure to log out?");
        builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                UiUtils.showProgressDialog(MainActivity.this, "Logging out...");
                FirebaseAuth.getInstance().signOut();
                UiUtils.showSafeToast("You've being signed out");
            }
        }).setNegativeButton("NO", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        builder.create().show();
    }

}
