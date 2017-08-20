package com.wan.hollout.ui.activities;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.afollestad.appthemeengine.ATE;
import com.afollestad.appthemeengine.Config;
import com.afollestad.appthemeengine.customizers.ATEActivityThemeCustomizer;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.hyphenate.EMCallBack;
import com.hyphenate.chat.EMClient;
import com.parse.LogOutCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.wan.hollout.R;
import com.wan.hollout.callbacks.DoneCallback;
import com.wan.hollout.chat.HolloutCommunicationsManager;
import com.wan.hollout.entities.drawerMenu.DrawerItemCategory;
import com.wan.hollout.entities.drawerMenu.DrawerItemPage;
import com.wan.hollout.eventbuses.SearchChatsEvent;
import com.wan.hollout.eventbuses.SearchPeopleEvent;
import com.wan.hollout.ui.fragments.ConversationsFragment;
import com.wan.hollout.ui.fragments.DrawerFragment;
import com.wan.hollout.ui.fragments.FeedFragment;
import com.wan.hollout.ui.fragments.PeopleFragment;
import com.wan.hollout.ui.services.AppInstanceDetectionService;
import com.wan.hollout.ui.services.ObjectReplicationService;
import com.wan.hollout.ui.widgets.MaterialSearchView;
import com.wan.hollout.utils.ATEUtils;
import com.wan.hollout.utils.AppConstants;
import com.wan.hollout.utils.AuthUtil;
import com.wan.hollout.utils.FontUtils;
import com.wan.hollout.utils.HolloutPermissions;
import com.wan.hollout.utils.HolloutPreferences;
import com.wan.hollout.utils.PermissionsUtils;
import com.wan.hollout.utils.RequestCodes;
import com.wan.hollout.utils.UiUtils;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.text.WordUtils;
import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends BaseActivity implements ATEActivityThemeCustomizer,
        DrawerFragment.FragmentDrawerListener,
        ActivityCompat.OnRequestPermissionsResultCallback {

    private boolean isDarkTheme;

    @BindView(R.id.footerAd)
    LinearLayout footerView;

    @BindView(R.id.drawer_layout)
    DrawerLayout drawer;

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.viewpager)
    ViewPager viewPager;

    @BindView(R.id.tabs)
    TabLayout tabLayout;

    @BindView(R.id.search_view)
    MaterialSearchView materialSearchView;

    private HolloutPermissions holloutPermissions;
    private DrawerFragment drawerFragment;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        isDarkTheme = HolloutPreferences.getHolloutPreferences().getBoolean("dark_theme", false);
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);

        ParseUser signedInUser = ParseUser.getCurrentUser();
        if (signedInUser == null) {
            Intent splashIntent = new Intent(MainActivity.this, SplashActivity.class);
            startActivity(splashIntent);
            finish();
            return;
        }

        Bundle intentExtras = getIntent().getExtras();

        if (intentExtras != null) {
            boolean accountConflict = intentExtras.getBoolean(AppConstants.ACCOUNT_CONFLICT, false);
            if (accountConflict) {
                resolveAuthenticationConflict();
                return;
            }
        }

        checkEMCAuthenticationStatus();

        final ActionBar ab = getSupportActionBar();

        if (ab != null) {
            ab.setHomeAsUpIndicator(R.drawable.ic_menu);
            ab.setDisplayHomeAsUpEnabled(true);
        }

        if (viewPager != null) {
            Adapter adapter = setupViewPagerAdapter(viewPager);
            viewPager.setOffscreenPageLimit(3);
            tabLayout.setSelectedTabIndicatorHeight(6);
            tabLayout.setupWithViewPager(viewPager);
            setupTabs(adapter);
        }

        fetchUnreadMessagesCount();

        if (HolloutPreferences.getHolloutPreferences().getBoolean("dark_theme", false)) {
            ATE.apply(this, "dark_theme");
        } else {
            ATE.apply(this, "light_theme");
        }

        viewPager.setCurrentItem(HolloutPreferences.getStartPageIndex());
        initAndroidPermissions();
        drawerFragment = (DrawerFragment) getSupportFragmentManager().findFragmentById(R.id.main_navigation_drawer_fragment);
        drawerFragment.setUp(drawer, this);

        if (!HolloutPreferences.isUserWelcomed()) {
            UiUtils.showSafeToast("Welcome, " + WordUtils.capitalize(signedInUser.getString(AppConstants.APP_USER_DISPLAY_NAME)));
            HolloutPreferences.setUserWelcomed(true);
        }

        checkAndRegEventBus();

        materialSearchView.setOnSearchViewListener(new MaterialSearchView.SearchViewListener() {

            @Override
            public void onSearchViewShown() {
                EventBus.getDefault().post(AppConstants.DISABLE_NESTED_SCROLLING);
                UiUtils.showView(tabLayout, false);
            }

            @Override
            public void onSearchViewClosed() {
                EventBus.getDefault().post(AppConstants.ENABLE_NESTED_SCROLLING);
                UiUtils.showView(tabLayout, true);
                EventBus.getDefault().post(AppConstants.SEARCH_VIEW_CLOSED);
            }

        });

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                if (materialSearchView.isSearchOpen()) {
                    if (position == 0) {
                        materialSearchView.setHint("Search People");
                    } else if (position == 1) {
                        materialSearchView.setHint("Search your chats");
                    } else {
                        materialSearchView.setHint("Search People");
                        materialSearchView.closeSearch();
                    }
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }

        });

        materialSearchView.setOnQueryTextListener(new MaterialSearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (viewPager.getCurrentItem() == 0) {
                    EventBus.getDefault().post(new SearchPeopleEvent(query));
                } else if (viewPager.getCurrentItem() == 1) {
                    EventBus.getDefault().post(new SearchChatsEvent(query));
                }
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (viewPager.getCurrentItem() == 0) {
                    EventBus.getDefault().post(new SearchPeopleEvent(newText));
                } else if (viewPager.getCurrentItem() == 1) {
                    EventBus.getDefault().post(new SearchChatsEvent(newText));
                }
                return true;
            }
        });

    }

    private SharedPreferences.OnSharedPreferenceChangeListener onSharedPreferenceChangeListener;

    private void fetchUnreadMessagesCount() {
        getUnreadMessagesCount();
        if (onSharedPreferenceChangeListener != null) {
            HolloutPreferences.getHolloutPreferences().unregisterOnSharedPreferenceChangeListener(onSharedPreferenceChangeListener);
            onSharedPreferenceChangeListener = null;
        }
        onSharedPreferenceChangeListener = new SharedPreferences.OnSharedPreferenceChangeListener() {

            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                getUnreadMessagesCount();
            }

        };
        HolloutPreferences.getHolloutPreferences().registerOnSharedPreferenceChangeListener(onSharedPreferenceChangeListener);
    }

    private void getUnreadMessagesCount() {
        long unreadMessagesCount = HolloutPreferences.getUnreadMessagesCount();
        if (unreadMessagesCount > 0) {
            updateTab(1, unreadMessagesCount);
        }
    }

    private void updateTab(int whichTab, long incrementValue) {
        TabLayout.Tab tab = tabLayout.getTabAt(whichTab);
        if (tab != null) {
            View tabView = tab.getCustomView();
            if (tabView != null) {
                TextView tabCountView = (TextView) tabView.findViewById(R.id.tab_count);
                if (tabCountView != null) {
                    UiUtils.showView(tabCountView, true);
                    String textInTab = tabCountView.getText().toString().trim();
                    if (StringUtils.isNotEmpty(textInTab)) {
                        long existingValue = Long.parseLong(tabCountView.getText().toString().trim());
                        if (existingValue != 0) {
                            existingValue = existingValue + incrementValue;
                        }
                        tabCountView.setText(String.valueOf(existingValue));
                    } else {
                        tabCountView.setText(String.valueOf(incrementValue));
                    }
                }
            }
        }
    }

    private void resolveAuthenticationConflict() {
        final AlertDialog.Builder accountConflictDialog = new AlertDialog.Builder(this);
        accountConflictDialog.setCancelable(false);
        accountConflictDialog.setTitle("Another Device detected!");
        accountConflictDialog.setMessage("This account was logged in to on another device. You'll be logged out here.");
        accountConflictDialog.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                attemptLogOut();
            }
        });
        accountConflictDialog.create().show();
    }

    private void checkIsSessionValid() {
        Bundle intentExtras = getIntent().getExtras();
        if (intentExtras != null) {
            boolean accountConflict = intentExtras.getBoolean(AppConstants.ACCOUNT_CONFLICT, false);
            if (accountConflict) {
                resolveAuthenticationConflict();
            }
        }
    }

    private Adapter setupViewPagerAdapter(ViewPager viewPager) {
        Adapter adapter = new Adapter(this, getSupportFragmentManager());
        adapter.addFragment(new PeopleFragment(), this.getString(R.string.people));
        adapter.addFragment(new ConversationsFragment(), this.getString(R.string.chats));
        adapter.addFragment(new FeedFragment(), this.getString(R.string.feeds));
        viewPager.setAdapter(adapter);
        return adapter;
    }

    @Override
    public void onResume() {
        super.onResume();
        checkAndRegEventBus();
        String ateKey = HolloutPreferences.getATEKey();
        ATEUtils.setStatusBarColor(this, ateKey, Config.primaryColor(this, ateKey));
        invalidateDrawerMenuHeader();
        checkEMCAuthenticationStatus();
        fetchUnreadMessagesCount();
    }

    private void checkEMCAuthenticationStatus() {
        if (!EMClient.getInstance().isLoggedInBefore()) {
            ParseUser parseUser = ParseUser.getCurrentUser();
            if (parseUser != null) {
                HolloutCommunicationsManager.getInstance().logInEMClient(parseUser.getUsername(), parseUser.getUsername(), new DoneCallback<Boolean>() {
                    @Override
                    public void done(Boolean success, Exception e) {
                        if (e == null && success) {
                            HolloutCommunicationsManager.getInstance().init(MainActivity.this);
                        }
                    }
                });
            } else {
                attemptLogOut();
            }
        } else {
            HolloutCommunicationsManager.getInstance().init(MainActivity.this);
        }
    }

    private void setupTabs(Adapter pagerAdapter) {
        for (int i = 0; i < tabLayout.getTabCount(); i++) {
            TabLayout.Tab tab = tabLayout.getTabAt(i);
            if (tab != null) {
                tab.setCustomView(pagerAdapter.getCustomTabView(i));
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PermissionsUtils.REQUEST_LOCATION && holloutPermissions.verifyPermissions(grantResults)) {
            HolloutPreferences.setCanAccessLocation(true);
        } else {
            UiUtils.snackMessage("To enjoy all features of hollout, please allow access to your location.",
                    drawer, true, "OK", new DoneCallback<Object>() {
                        @Override
                        public void done(Object result, Exception e) {
                            tryAccessLocation();
                        }
                    });

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
        checkEMCAuthenticationStatus();
        fetchUnreadMessagesCount();
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        checkAndRegEventBus();
        fetchUnreadMessagesCount();
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
                            tryAccessLocation();
                        } else {
                            turnOnLocationMessage();
                        }
                    }
                }
            }
        });
    }

    private void tryAccessLocation() {
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
        HolloutPreferences.setStartPageIndex(viewPager.getCurrentItem());
        if (isFinishing()) overridePendingTransition(R.anim.fade_scale_in, R.anim.slide_to_right);
    }

    /**
     * Method checks if MainActivity instance exist. If so, then drawer menu header will be invalidated.
     */
    public void invalidateDrawerMenuHeader() {
        if (drawerFragment != null) {
            drawerFragment.invalidateHeader();
        }
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
        } else if (materialSearchView.isSearchOpen()) {
            materialSearchView.closeSearch();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem filterPeopleMenuItem = menu.findItem(R.id.filter_people);
        MenuItem createNewGroupChatItem = menu.findItem(R.id.create_new_group);
        filterPeopleMenuItem.setVisible(viewPager.getCurrentItem() == 0);
        createNewGroupChatItem.setVisible(viewPager.getCurrentItem() == 1);
        supportInvalidateOptionsMenu();
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        ATE.applyMenu(this, getATEKey(), menu);
        MenuItem item = menu.findItem(R.id.action_search);
        materialSearchView.setMenuItem(item);
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
            drawer.openDrawer(GravityCompat.START);
            return true;
        } else if (id == android.R.id.home) {
            drawer.openDrawer(GravityCompat.START);
            return true;
        } else if (id == R.id.filter_people) {
            initPeopleFilterDialog();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private String genderChoice = AppConstants.Both;

    @SuppressLint("SetTextI18n")
    private void initPeopleFilterDialog() {
        final ParseUser signedInUser = ParseUser.getCurrentUser();
        String ageStartFilter = signedInUser.getString(AppConstants.START_AGE_FILTER_VALUE);
        final String ageEndFilter = signedInUser.getString(AppConstants.END_AGE_FILTER_VALUE);
        AlertDialog.Builder peopleFilterDialog = new AlertDialog.Builder(MainActivity.this);
        @SuppressLint("InflateParams")
        View peopleFilterDialogView = getLayoutInflater().inflate(R.layout.people_filter_options_dialog, null);
        RadioGroup genderFilterOptionsGroup = (RadioGroup) peopleFilterDialogView.findViewById(R.id.gender_filter_options);
        final EditText startAgeEditText = (EditText) peopleFilterDialogView.findViewById(R.id.start_age);
        if (StringUtils.isNotEmpty(ageStartFilter)) {
            startAgeEditText.setText(ageStartFilter);
        } else {
            startAgeEditText.setText("16");
        }
        final EditText endAgeEditText = (EditText) peopleFilterDialogView.findViewById(R.id.end_age);
        if (StringUtils.isNotEmpty(ageEndFilter)) {
            endAgeEditText.setText(ageEndFilter);
        } else {
            endAgeEditText.setText("70");
        }
        genderFilterOptionsGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, @IdRes int checkedId) {
                if (checkedId == R.id.males_only) {
                    genderChoice = AppConstants.MALE;
                } else if (checkedId == R.id.females_only) {
                    genderChoice = AppConstants.FEMALE;
                }
            }
        });
        peopleFilterDialog.setView(peopleFilterDialogView);
        peopleFilterDialog.setPositiveButton("UPDATE", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(final DialogInterface dialog, int which) {
                signedInUser.put(AppConstants.GENDER_FILTER, genderChoice);
                if (StringUtils.isNotEmpty(startAgeEditText.getText().toString().trim()) && StringUtils.isNotEmpty(endAgeEditText.getText().toString().trim())) {
                    signedInUser.put(AppConstants.AGE_START_FILTER, startAgeEditText.getText().toString().trim());
                    signedInUser.put(AppConstants.AGE_END_FILTER, endAgeEditText.getText().toString().trim());
                }
                signedInUser.saveInBackground(new SaveCallback() {
                    @Override
                    public void done(ParseException e) {
                        dialog.dismiss();
                        dialog.cancel();
                        EventBus.getDefault().post(AppConstants.REFRESH_PEOPLE);
                    }
                });
            }
        });
        peopleFilterDialog.create().show();
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

    private void startObjectReplicationService() {
        Intent objectReplicationServiceIntent = new Intent(MainActivity.this, ObjectReplicationService.class);
        startService(objectReplicationServiceIntent);
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        if (ParseUser.getCurrentUser() != null) {
            startObjectReplicationService();
        }
    }

    private void attemptLogOut() {
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle("Attention!");
        builder.setMessage("Are you sure to log out?");
        builder.setPositiveButton("YES", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                finishLogOut();
            }
        }).setNegativeButton("NO", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        });
        builder.create().show();
    }

    private void finishLogOut() {
        FirebaseAuth.getInstance().signOut();
        UiUtils.showProgressDialog(MainActivity.this, "Logging out...");
        if (ParseUser.getCurrentUser() != null) {
            ParseUser.logOutInBackground(new LogOutCallback() {
                @Override
                public void done(ParseException e) {
                    if (e == null) {
                        HolloutPreferences.setUserWelcomed(false);
                        HolloutPreferences.clearPersistedCredentials();
                        invalidateDrawerMenuHeader();
                        ParseObject.unpinAllInBackground(AppConstants.APP_USERS);
                        ParseObject.unpinAllInBackground(AppConstants.HOLLOUT_FEED);
                        AuthUtil.signOut(MainActivity.this).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {

                                    HolloutCommunicationsManager.getInstance().signOut(true, new EMCallBack() {
                                        @Override
                                        public void onSuccess() {
                                            finishUp();
                                        }

                                        @Override
                                        public void onError(int code, String error) {
                                            UiUtils.dismissProgressDialog();
                                            UiUtils.showSafeToast("Failed to sign you out.Please try again");
                                            checkIsSessionValid();
                                        }

                                        @Override
                                        public void onProgress(int progress, String status) {

                                        }

                                    });

                                } else {
                                    UiUtils.dismissProgressDialog();
                                    UiUtils.showSafeToast("Failed to sign you out.Please try again");
                                    checkIsSessionValid();
                                }
                            }
                        });
                    } else {
                        UiUtils.dismissProgressDialog();
                        UiUtils.showSafeToast("Failed to sign you out.Please try again");
                        checkIsSessionValid();
                    }
                }
            });
        } else {
            finish();
        }
    }

    private void finishUp() {
        UiUtils.dismissProgressDialog();
        UiUtils.showSafeToast("You've being logged out");
        Intent splashIntent = new Intent(MainActivity.this, SplashActivity.class);
        startActivity(splashIntent);
        finish();
    }

    @Override
    public void onDrawersPeopleOfSharedInterestsSelected() {
        Intent meetPeopleIntent = new Intent(MainActivity.this, MeetPeopleActivity.class);
        startActivityForResult(meetPeopleIntent, RequestCodes.MEET_PEOPLE_REQUEST_CODE);
    }

    @Override
    public void onDrawerItemCategorySelected(DrawerItemCategory drawerItemCategory) {
        if (drawerItemCategory.getId() == DrawerFragment.LOG_OUT) {
            attemptLogOut();
        } else if (drawerItemCategory.getId() == DrawerFragment.YOUR_PROFILE) {
            launchUserProfile();
        } else if (drawerItemCategory.getId() == DrawerFragment.NOTIFICATION_SETTINGS) {
            launchSettings(AppConstants.NOTIFICATION_SETTINGS_FRAGMENT);
        } else if (drawerItemCategory.getId() == DrawerFragment.CHATS_AND_CALLS_SETTINGS) {
            launchSettings(AppConstants.CHATS_SETTINGS_FRAGMENT);
        } else if (drawerItemCategory.getId() == DrawerFragment.ABOUT_AND_SUPPORT_SETTINGS) {
            launchSettings(AppConstants.SUPPORT_SETTINGS_FRAGMENT);
        } else if (drawerItemCategory.getId() == DrawerFragment.PRIVACY_SETTINGS) {
            launchSettings(AppConstants.PRIVACY_AND_SECURITY_FRAGMENT);
        }
    }

    private void launchSettings(String settingsFragmentName) {
        Intent settingsIntent = new Intent(MainActivity.this, SettingsActivity.class);
        settingsIntent.putExtra(AppConstants.SETTINGS_FRAGMENT_NAME, settingsFragmentName);
        startActivity(settingsIntent);
    }

    @Override
    public void onDrawerItemPageSelected(DrawerItemPage drawerItemPage) {

    }

    @Override
    public void onAccountSelected() {
        launchUserProfile();
    }

    private void launchUserProfile() {
        Intent signedInUserIntent = new Intent(MainActivity.this, UserProfileActivity.class);
        if (ParseUser.getCurrentUser() != null) {
            signedInUserIntent.putExtra(AppConstants.USER_PROPERTIES, ParseUser.getCurrentUser());
            startActivity(signedInUserIntent);
        }
    }

    private static class Adapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragments = new ArrayList<>();
        private final List<String> mFragmentTitles = new ArrayList<>();
        private LayoutInflater layoutInflater;
        private Context context;

        Adapter(Context context, FragmentManager fm) {
            super(fm);
            this.context = context;
            this.layoutInflater = LayoutInflater.from(context);
        }

        void addFragment(Fragment fragment, String title) {
            mFragments.add(fragment);
            mFragmentTitles.add(title);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragments.get(position);
        }

        @Override
        public int getCount() {
            return mFragments.size();
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitles.get(position);
        }

        View getCustomTabView(int pos) {
            @SuppressWarnings("InflateParams")
            View view = layoutInflater.inflate(R.layout.tab_custom_view, null);
            TextView tabTitle = (TextView) view.findViewById(R.id.tab_title);
            Typeface typeface = FontUtils.selectTypeface(context, 1);
            tabTitle.setTypeface(typeface);
            tabTitle.setText(getPageTitle(pos));
            return view;
        }

    }

}
