package com.wan.hollout.ui.activities;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatDelegate;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.LinearLayout;

import com.afollestad.appthemeengine.ATE;
import com.afollestad.appthemeengine.customizers.ATEActivityThemeCustomizer;
import com.parse.LogOutCallback;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.wan.hollout.R;
import com.wan.hollout.callbacks.DoneCallback;
import com.wan.hollout.entities.drawerMenu.DrawerItemCategory;
import com.wan.hollout.entities.drawerMenu.DrawerItemPage;
import com.wan.hollout.ui.fragments.DrawerFragment;
import com.wan.hollout.ui.fragments.MainFragment;
import com.wan.hollout.ui.services.AppInstanceDetectionService;
import com.wan.hollout.utils.AppConstants;
import com.wan.hollout.utils.HolloutPermissions;
import com.wan.hollout.utils.HolloutPreferences;
import com.wan.hollout.utils.PermissionsUtils;
import com.wan.hollout.utils.RequestCodes;
import com.wan.hollout.utils.UiUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends BaseActivity implements ATEActivityThemeCustomizer, DrawerFragment.FragmentDrawerListener, ActivityCompat.OnRequestPermissionsResultCallback {

    private boolean isDarkTheme;

    @BindView(R.id.footerAd)
    LinearLayout footerView;

    @BindView(R.id.drawer_layout)
    DrawerLayout drawer;

    private HolloutPermissions holloutPermissions;

    /**
     * Reference tied drawer menu, represented as fragment.
     */
    private Runnable homeRunnable = new Runnable() {
        @Override
        public void run() {
            navigateToMainFragment();
        }
    };

    private DrawerFragment drawerFragment;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        isDarkTheme = HolloutPreferences.getHolloutPreferences().getBoolean("dark_theme", false);
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        initAndroidPermissions();
        homeRunnable.run();
        drawerFragment = (DrawerFragment) getSupportFragmentManager().findFragmentById(R.id.main_navigation_drawer_fragment);
        drawerFragment.setUp(drawer, this);
        ParseUser signedInUser = ParseUser.getCurrentUser();
        if (!HolloutPreferences.isUserWelcomed()) {
            if (signedInUser != null) {
                UiUtils.showSafeToast("Welcome, " + signedInUser.getString(AppConstants.APP_USER_DISPLAY_NAME));
            }
            HolloutPreferences.setUserWelcomed();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PermissionsUtils.REQUEST_LOCATION && holloutPermissions.verifyPermissions(grantResults)) {
            HolloutPreferences.setCanAccessLocation(true);
        }
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

    private void initAndroidPermissions() {
        holloutPermissions = new HolloutPermissions(this, footerView);
    }

    @Override
    protected void onStop() {
        super.onStop();
        checkAnUnRegEventBus();
    }

    @Override
    protected void onStart() {
        super.onStart();
        checkAndRegEventBus();
    }

    private void turnOnLocationMessage() {
        UiUtils.snackMessage("To enjoy all features of hollout, please Turn on your location.",
                drawer, true, "OK", new DoneCallback<Object>() {
                    @Override
                    public void done(Object result, Exception e) {
                        Intent mLocationSettingsIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                        startActivity(mLocationSettingsIntent);
                    }
                });

    }

    @SuppressWarnings("deprecation")
    public boolean isLocationEnabled(Context context) {
        int locationMode = 0;
        String locationProviders;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            try {
                locationMode = Settings.Secure.getInt(context.getContentResolver(), Settings.Secure.LOCATION_MODE);
            } catch (Settings.SettingNotFoundException e) {
                e.printStackTrace();
            }
            return locationMode != Settings.Secure.LOCATION_MODE_OFF;
        } else {
            locationProviders = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.LOCATION_PROVIDERS_ALLOWED);
            return !TextUtils.isEmpty(locationProviders);
        }
    }

    @SuppressWarnings("unused")
    @Subscribe(sticky = true, threadMode = ThreadMode.ASYNC)
    public void onEventAsync(final Object o) {
        UiUtils.runOnMain(new Runnable() {
            @Override
            public void run() {
                if (o instanceof String) {
                    String s = (String) o;
                    if (s.equals(AppConstants.PLEASE_REQUEST_LOCATION_ACCESSS)) {
                        if (isLocationEnabled(MainActivity.this)) {
                            startAppInstanceDetectionService();
                        } else {
                            turnOnLocationMessage();
                        }
                    }
                }
            }
        });
    }

    private void startAppInstanceDetectionService() {
        boolean canAccessLocation = HolloutPreferences.canAccessLocation();
        if (Build.VERSION.SDK_INT >= 23 && !canAccessLocation) {
            holloutPermissions.requestLocationPermissions();
            return;
        }
        Intent mAppInstanceDetectIntent = new Intent(this, AppInstanceDetectionService.class);
        startService(mAppInstanceDetectIntent);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (isFinishing())
            overridePendingTransition(R.anim.slide_from_right, R.anim.fade_scale_out);
    }

    /**
     * Method checks if MainActivity instance exist. If so, then drawer menu header will be invalidated.
     */
    public void invalidateDrawerMenuHeader() {
        if (drawerFragment != null) {
            drawerFragment.invalidateHeader();
        }
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RequestCodes.MEET_PEOPLE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                EventBus.getDefault().post(AppConstants.REFRESH_PEOPLE);
            }
        } else {
            getSupportFragmentManager().findFragmentById(R.id.fragment_container).onActivityResult(requestCode, resultCode, data);
        }
    }

    private void attemptLogOut() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Attention!");
        builder.setMessage("Are you sure to log out?");
        builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                UiUtils.showProgressDialog(MainActivity.this, "Logging out...");
                ParseUser.logOutInBackground(new LogOutCallback() {
                    @Override
                    public void done(ParseException e) {
                        HolloutPreferences.clearPersistedCredentials();
                        UiUtils.showSafeToast("You've being logged out");
                        invalidateDrawerMenuHeader();
                    }
                });
            }
        }).setNegativeButton("NO", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        builder.create().show();
    }

    @Override
    public void onDrawersPeopleOfSharedInterestsSelected() {

    }

    @Override
    public void onDrawerItemCategorySelected(DrawerItemCategory drawerItemCategory) {
        if (drawerItemCategory.getId() == DrawerFragment.LOG_OUT) {
            attemptLogOut();
        } else if (drawerItemCategory.getId() == DrawerFragment.MEET_PEOPLE) {
            Intent meetPeopleIntent = new Intent(MainActivity.this, MeetPeopleActivity.class);
            startActivityForResult(meetPeopleIntent, RequestCodes.MEET_PEOPLE_REQUEST_CODE);
        }
    }

    @Override
    public void onDrawerItemPageSelected(DrawerItemPage drawerItemPage) {

    }

    @Override
    public void onAccountSelected() {

    }

}
