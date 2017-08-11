package com.wan.hollout.ui.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatDelegate;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.FrameLayout;

import com.afollestad.appthemeengine.ATE;
import com.afollestad.appthemeengine.customizers.ATEActivityThemeCustomizer;
import com.parse.LogOutCallback;
import com.parse.ParseException;
import com.parse.ParseUser;
import com.wan.hollout.R;
import com.wan.hollout.entities.drawerMenu.DrawerItemCategory;
import com.wan.hollout.entities.drawerMenu.DrawerItemPage;
import com.wan.hollout.ui.fragments.DrawerFragment;
import com.wan.hollout.ui.fragments.MainFragment;
import com.wan.hollout.utils.AppConstants;
import com.wan.hollout.utils.HolloutPreferences;
import com.wan.hollout.utils.RequestCodes;
import com.wan.hollout.utils.UiUtils;

import org.greenrobot.eventbus.EventBus;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends BaseActivity implements ATEActivityThemeCustomizer, DrawerFragment.FragmentDrawerListener {

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

    private DrawerFragment drawerFragment;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        isDarkTheme = HolloutPreferences.getHolloutPreferences().getBoolean("dark_theme", false);
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
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
        getSupportFragmentManager().findFragmentById(R.id.fragment_container).onActivityResult(requestCode, resultCode, data);
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
            Intent meetPeopleIntent = new Intent(MainActivity.this, PeopleILikeToMeetActivity.class);
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
