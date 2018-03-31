package com.wan.hollout.ui.activities;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Vibrator;
import android.provider.Settings;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.mikepenz.google_material_typeface_library.GoogleMaterial;
import com.mikepenz.iconics.IconicsDrawable;
import com.mikepenz.iconics.typeface.IIcon;
import com.parse.ParseObject;
import com.wan.hollout.R;
import com.wan.hollout.eventbuses.MessageReceivedEvent;
import com.wan.hollout.eventbuses.SearchChatsEvent;
import com.wan.hollout.eventbuses.SearchPeopleEvent;
import com.wan.hollout.eventbuses.UnreadFeedsBadge;
import com.wan.hollout.interfaces.DoneCallback;
import com.wan.hollout.models.ChatMessage;
import com.wan.hollout.models.ConversationItem;
import com.wan.hollout.ui.fragments.ConversationsFragment;
import com.wan.hollout.ui.fragments.PeopleFragment;
import com.wan.hollout.ui.services.AppInstanceDetectionService;
import com.wan.hollout.ui.services.TimeChangeDetectionService;
import com.wan.hollout.ui.widgets.CircleImageView;
import com.wan.hollout.ui.widgets.MaterialSearchView;
import com.wan.hollout.ui.widgets.sharesheet.LinkProperties;
import com.wan.hollout.ui.widgets.sharesheet.ShareSheet;
import com.wan.hollout.ui.widgets.sharesheet.ShareSheetStyle;
import com.wan.hollout.ui.widgets.sharesheet.SharingHelper;
import com.wan.hollout.utils.AppConstants;
import com.wan.hollout.utils.AuthUtil;
import com.wan.hollout.utils.DbUtils;
import com.wan.hollout.utils.FirebaseUtils;
import com.wan.hollout.utils.FontUtils;
import com.wan.hollout.utils.GeneralNotifier;
import com.wan.hollout.utils.HolloutLogger;
import com.wan.hollout.utils.HolloutPermissions;
import com.wan.hollout.utils.HolloutPreferences;
import com.wan.hollout.utils.HolloutUtils;
import com.wan.hollout.utils.JsonUtils;
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
import java.util.Set;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.wan.hollout.utils.UiUtils.showView;

@SuppressWarnings("RedundantCast")
public class MainActivity extends BaseActivity implements ActivityCompat.OnRequestPermissionsResultCallback {

    @BindView(R.id.footerAd)
    LinearLayout footerView;

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.rootLayout)
    View rootLayout;

    @BindView(R.id.viewpager)
    ViewPager viewPager;

    @BindView(R.id.tabs)
    TabLayout tabLayout;

    @BindView(R.id.search_view)
    MaterialSearchView materialSearchView;

    //=======Action Mode Shits=====//
    @SuppressLint("StaticFieldLeak")
    public static View actionModeBar;

    @BindView(R.id.destroy_action_mode)
    ImageView destroyActionModeView;

    @BindView(R.id.action_item_selection_count)
    TextView selectionActionsCountView;

    @BindView(R.id.delete_conversation)
    ImageView deleteConversation;

    @BindView(R.id.block_user)
    ImageView blockUser;

    @BindView(R.id.nav_view)
    NavigationView navigationView;

    @BindView(R.id.drawer_layout)
    DrawerLayout drawerLayout;

    private HolloutPermissions holloutPermissions;

    private SharedPreferences.OnSharedPreferenceChangeListener onSharedPreferenceChangeListener;
    public static Vibrator vibrator;
    private ProgressDialog deleteConversationProgressDialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);
        actionModeBar = findViewById(R.id.action_mode_bar);
        setSupportActionBar(toolbar);
        ParseObject signedInUser = AuthUtil.getCurrentUser();
        Adapter adapter = setupViewPagerAdapter(viewPager);
        viewPager.setOffscreenPageLimit(3);
        tabLayout.setSelectedTabIndicatorHeight(7);
        tabLayout.setupWithViewPager(viewPager);
        setupTabs(adapter);
        fetchUnreadMessagesCount();
        viewPager.setCurrentItem(HolloutPreferences.getStartPageIndex());
        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        initAndroidPermissions();
        setupNavigationDrawer(signedInUser);
        if (!HolloutPreferences.isUserWelcomed()) {
            if (signedInUser != null) {
                UiUtils.showSafeToast("Welcome, " + WordUtils.capitalize(signedInUser.getString(AppConstants.APP_USER_DISPLAY_NAME)));
            }
            HolloutPreferences.setUserWelcomed(true);
        }
        checkAndRegEventBus();
        attachEventHandlers();
        GeneralNotifier.getNotificationManager().cancel(AppConstants.CHAT_REQUEST_NOTIFICATION_ID);
        GeneralNotifier.getNotificationManager().cancel(AppConstants.NEARBY_KIND_NOTIFICATION_ID);
        GeneralNotifier.getNotificationManager().cancel(AppConstants.NEW_MESSAGE_NOTIFICATION_ID);
        initEventHandlers();
        createDeleteConversationProgressDialog();
    }


    private void createDeleteConversationProgressDialog() {
        deleteConversationProgressDialog = new ProgressDialog(this);
        deleteConversationProgressDialog.setIndeterminate(false);
        deleteConversationProgressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
        deleteConversationProgressDialog.setMax(100);
    }

    private void checkDismissConversationProgressDialog() {
        if (deleteConversationProgressDialog != null && deleteConversationProgressDialog.isShowing()) {
            deleteConversationProgressDialog.dismiss();
        }
    }

    private void initEventHandlers() {
        View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (v.getId()) {
                    case R.id.block_user:
                        ConversationItem selectedUserToBlock = AppConstants.selectedPeople.get(0);
                        if (selectedUserToBlock != null) {
                            ParseObject selectedUserObject = selectedUserToBlock.getRecipient();
                            if (selectedUserObject != null) {
                                String userId = selectedUserObject.getString(AppConstants.REAL_OBJECT_ID);
                                if (!HolloutUtils.isUserBlocked(userId)) {
                                    final ProgressDialog progressDialog = UiUtils.showProgressDialog(MainActivity.this, "Blocking User. Please wait...");
                                    HolloutUtils.blockUser(userId, new DoneCallback<Boolean>() {
                                        @Override
                                        public void done(Boolean success, Exception e) {
                                            UiUtils.dismissProgressDialog(progressDialog);
                                            if (success) {
                                                UiUtils.showSafeToast("User blocked successfully!");
                                                ConversationsFragment.conversationsAdapter.notifyDataSetChanged();
                                                destroyActionMode();
                                            } else {
                                                UiUtils.showSafeToast("Sorry and error occurred while trying to block user");
                                            }
                                        }
                                    });
                                } else {
                                    final ProgressDialog progressDialog = UiUtils.showProgressDialog(MainActivity.this, "Unblocking User. Please wait...");
                                    HolloutUtils.unBlockUser(userId, new DoneCallback<Boolean>() {
                                        @Override
                                        public void done(Boolean success, Exception e) {
                                            UiUtils.dismissProgressDialog(progressDialog);
                                            if (success) {
                                                UiUtils.showSafeToast("User Unblocked successfully!");
                                                ConversationsFragment.conversationsAdapter.notifyDataSetChanged();
                                                destroyActionMode();
                                            } else {
                                                UiUtils.showSafeToast("Sorry and error occurred while trying to unblock user");
                                            }
                                        }
                                    });
                                }
                            }
                        }
                        break;
                    case R.id.delete_conversation:
                        AlertDialog.Builder deleteConversationConsentDialog = new AlertDialog.Builder(MainActivity.this);
                        deleteConversationConsentDialog.setMessage("Delete "
                                + (AppConstants.selectedPeople.size() == 1 ? "this conversation ? " : AppConstants.selectedPeople.size() + " conversations?"));
                        deleteConversationConsentDialog.setPositiveButton("DELETE", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                                for (ConversationItem conversationItem : AppConstants.selectedPeople) {
                                    deleteConversationProgressDialog.show();
                                    deleteConversationProgressDialog.setTitle("Deleting Conversation" + (AppConstants.selectedPeople.size() > 1 ? (AppConstants.selectedPeople.size()) : ""));
                                    String recipientId = conversationItem.getRecipient().getString(AppConstants.REAL_OBJECT_ID);
                                    DbUtils.deleteConversation(recipientId, new DoneCallback<Long[]>() {
                                        @Override
                                        public void done(Long[] progressValues, Exception e) {
                                            long current = progressValues[0];
                                            long total = progressValues[1];
                                            if (current != -1 && total != 0) {
                                                double percentage = (100.0 * (current + 1)) / total;
                                                deleteConversationProgressDialog.setProgress((int) percentage);
                                                if (percentage == 100) {
                                                    checkDismissConversationProgressDialog();
                                                    UiUtils.showSafeToast("Conversation cleared");
                                                }
                                            } else {
                                                checkDismissConversationProgressDialog();
                                                UiUtils.showSafeToast("No conversation to clear.");
                                            }
                                        }
                                    });
                                }
                            }
                        });
                        deleteConversationConsentDialog.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.dismiss();
                            }
                        });
                        deleteConversationConsentDialog.create().show();
                        break;
                    case R.id.destroy_action_mode:
                        destroyActionMode();
                        break;
                }
            }
        };
        blockUser.setOnClickListener(onClickListener);
        deleteConversation.setOnClickListener(onClickListener);
        destroyActionModeView.setOnClickListener(onClickListener);
    }

    private void attachEventHandlers() {
        materialSearchView.setOnSearchViewListener(new MaterialSearchView.SearchViewListener() {

            @Override
            public void onSearchViewShown() {
                EventBus.getDefault().post(AppConstants.DISABLE_NESTED_SCROLLING);
            }

            @Override
            public void onSearchViewClosed() {
                EventBus.getDefault().post(AppConstants.ENABLE_NESTED_SCROLLING);
                showView(tabLayout, true);
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

    private Drawable getDrawableFromIcon(IIcon icon) {
        return new IconicsDrawable(this)
                .sizeDp(18)
                .icon(icon);
    }

    private void setupNavigationDrawer(ParseObject signedInUser) {

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        Menu navMenu = navigationView.getMenu();

        navMenu.add(0, 0, 0, "Profile").setIcon(getDrawableFromIcon(GoogleMaterial.Icon.gmd_account_circle));
        navMenu.add(0, 1, 1, "Invite Friends").setIcon(getDrawableFromIcon(GoogleMaterial.Icon.gmd_insert_link));

        navMenu.addSubMenu("Help & Settings");
        navMenu.add(1, 3, 3, "Notification Settings").setIcon(getDrawableFromIcon(GoogleMaterial.Icon.gmd_notifications));
        navMenu.add(1, 4, 4, "Chats & Calls Settings").setIcon(getDrawableFromIcon(GoogleMaterial.Icon.gmd_chat));
        navMenu.add(1, 5, 5, "Privacy Settings").setIcon(getDrawableFromIcon(GoogleMaterial.Icon.gmd_security));
        navMenu.add(1, 6, 6, "Support Settings").setIcon(getDrawableFromIcon(GoogleMaterial.Icon.gmd_help));
        navMenu.add(1, 7, 7, "Log Out").setIcon(getDrawableFromIcon(GoogleMaterial.Icon.gmd_chevron_left));

        displaySignedInUserProps(signedInUser);

        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case 0:
                        launchUserProfile();
                        break;
                    case 1:
                        initSharing();
                        break;
                    case 3:
                        launchSettings(AppConstants.NOTIFICATION_SETTINGS_FRAGMENT);
                        break;
                    case 4:
                        launchSettings(AppConstants.CHATS_SETTINGS_FRAGMENT);
                        break;
                    case 5:
                        launchSettings(AppConstants.PRIVACY_AND_SECURITY_FRAGMENT);
                        break;
                    case 6:
                        launchSettings(AppConstants.SUPPORT_SETTINGS_FRAGMENT);
                        break;
                    case 7:
                        attemptLogOut();
                        break;
                }
                drawerLayout.closeDrawer(GravityCompat.START);
                return true;
            }
        });

        View editProfileView = findViewById(R.id.edit_profile);
        editProfileView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(getCurrentActivityInstance(), EditProfileActivity.class));
            }
        });

        String userFirebaseTokenFromPreference = HolloutPreferences.getUserFirebaseToken();
        String userFirebaseTokenFromSignedInUser = signedInUser.getString(AppConstants.USER_FIREBASE_TOKEN);
        if (userFirebaseTokenFromPreference != null) {
            if (userFirebaseTokenFromSignedInUser == null) {
                signedInUser.put(AppConstants.USER_FIREBASE_TOKEN, userFirebaseTokenFromPreference);
                AuthUtil.updateCurrentLocalUser(signedInUser, new DoneCallback<Boolean>() {
                    @Override
                    public void done(Boolean result, Exception e) {
                        //User Prefs updated;
                    }
                });
            } else {
                if (!userFirebaseTokenFromPreference.equals(userFirebaseTokenFromSignedInUser)) {
                    signedInUser.put(AppConstants.USER_FIREBASE_TOKEN, userFirebaseTokenFromPreference);
                    AuthUtil.updateCurrentLocalUser(signedInUser, new DoneCallback<Boolean>() {
                        @Override
                        public void done(Boolean result, Exception e) {
                            //User Prefs updated;
                        }
                    });
                }
            }
        }
    }

    private void displaySignedInUserProps(ParseObject signedInUser) {
        String userDisplayName = signedInUser.getString(AppConstants.APP_USER_DISPLAY_NAME);
        String userPhotoUrl = signedInUser.getString(AppConstants.APP_USER_PROFILE_PHOTO_URL);
        String userCoverPhotoUrl = signedInUser.getString(AppConstants.APP_USER_COVER_PHOTO);
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        View headerView = navigationView.getHeaderView(0);

        TextView signedInUserNameView = headerView.findViewById(R.id.signed_in_user_name_view);
        CircleImageView signedInUserProfilePhotoView = headerView.findViewById(R.id.signed_in_user_profile_image_view);
        ImageView signedInUserCoverPhotoView = headerView.findViewById(R.id.signed_in_user_cover_image_view);
        TextView signedInUserEmailView = headerView.findViewById(R.id.signed_in_user_email_view);

        //Load Data
        signedInUserNameView.setText(WordUtils.capitalize(userDisplayName));
        if (StringUtils.isNotEmpty(userPhotoUrl)) {
            UiUtils.loadImage(this, userPhotoUrl, signedInUserProfilePhotoView);
        }
        if (StringUtils.isNotEmpty(userCoverPhotoUrl)) {
            UiUtils.loadImage(this, userCoverPhotoUrl, signedInUserCoverPhotoView);
            signedInUserCoverPhotoView.setColorFilter(new PorterDuffColorFilter(ContextCompat.getColor(this, R.color.colorPrimary), PorterDuff.Mode.MULTIPLY));
        }
        if (firebaseUser != null) {
            String userEmail = firebaseUser.getEmail();
            if (userEmail != null) {
                userEmail = StringUtils.remove(userEmail, "@hollout.com");
                signedInUserEmailView.setText(userEmail);
            }
        }

        View.OnClickListener onClickListener = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                launchUserProfile();
            }
        };

        headerView.setOnClickListener(onClickListener);
        signedInUserCoverPhotoView.setOnClickListener(onClickListener);
        signedInUserProfilePhotoView.setOnClickListener(onClickListener);
    }

    private void initSharing() {
        // Create your link properties
        LinkProperties linkProperties = new LinkProperties()
                .setFeature("sharing");
        // Customize the appearance of your share sheet
        ShareSheetStyle shareSheetStyle = new ShareSheetStyle(this, "Check this out!",
                "Hi,I just connected with a new and awesome friend near me using Hollout. You should try it out too.")
                .setCopyUrlStyle(ContextCompat.getDrawable(this, R.drawable.ic_content_copy_black_48dp), "Copy link",
                        "Link added to clipboard!")
                .setMoreOptionStyle(ContextCompat.getDrawable(this, R.drawable.ic_expand_more_black_48dp), "Show more")
                .setSharingTitle("Invite Friends Via")
                .addPreferredSharingOption(SharingHelper.SHARE_WITH.FACEBOOK)
                .addPreferredSharingOption(SharingHelper.SHARE_WITH.EMAIL)
                .addPreferredSharingOption(SharingHelper.SHARE_WITH.WHATS_APP)
                .addPreferredSharingOption(SharingHelper.SHARE_WITH.FLICKR)
                .addPreferredSharingOption(SharingHelper.SHARE_WITH.FACEBOOK_MESSENGER)
                .addPreferredSharingOption(SharingHelper.SHARE_WITH.GMAIL)
                .addPreferredSharingOption(SharingHelper.SHARE_WITH.HANGOUT)
                .addPreferredSharingOption(SharingHelper.SHARE_WITH.INSTAGRAM)
                .addPreferredSharingOption(SharingHelper.SHARE_WITH.PINTEREST)
                .addPreferredSharingOption(SharingHelper.SHARE_WITH.MESSAGE)
                .addPreferredSharingOption(SharingHelper.SHARE_WITH.TWITTER);
        ShareSheet shareSheet = new ShareSheet();
        shareSheet.setImageUrl("https://firebasestorage.googleapis.com/v0/b/hollout-860db.appspot.com/o/Photos%2Fweb_hi_res_512.png?alt=media&token=ef62f277-a85b-4067-9981-f01034b43e9b");
        shareSheet.setCanonicalUrl_("https://play.google.com/store/apps/details?id=com.wan.hollout");
        shareSheet.showShareSheet(this, linkProperties, shareSheetStyle, new ShareSheet.BranchLinkShareListener() {

            @Override
            public void onShareLinkDialogLaunched() {

            }

            @Override
            public void onShareLinkDialogDismissed() {

            }

            @Override
            public void onLinkShareResponse(String sharedLink, String sharedChannel, Exception error) {

            }

            @Override
            public void onChannelSelected(String channelName) {

            }

        });

    }

    private void fetchUnreadMessagesCount() {
        getUnreadMessagesCount();
        if (onSharedPreferenceChangeListener != null) {
            HolloutPreferences.getInstance().unregisterOnSharedPreferenceChangeListener(onSharedPreferenceChangeListener);
            onSharedPreferenceChangeListener = null;
        }
        onSharedPreferenceChangeListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                getUnreadMessagesCount();
            }
        };
        HolloutPreferences.getInstance().registerOnSharedPreferenceChangeListener(onSharedPreferenceChangeListener);
    }

    private void getUnreadMessagesCount() {
        Set<String> unreadMessagesCount = HolloutPreferences.getTotalUnreadChats();
        if (unreadMessagesCount != null && unreadMessagesCount.size() != 0) {
            updateTab(1, unreadMessagesCount.size());
        } else {
            updateTab(1, 0);
        }
    }

    private void updateTab(int whichTab, long incrementValue) {
        TabLayout.Tab tab = tabLayout.getTabAt(whichTab);
        if (tab != null) {
            View tabView = tab.getCustomView();
            if (tabView != null) {
                TextView tabCountView = (TextView) tabView.findViewById(R.id.tab_count);
                if (tabCountView != null) {
                    showView(tabCountView, true);
                    if (incrementValue == 0) {
                        showView(tabCountView, false);
                    } else {
                        tabCountView.setText(String.valueOf(incrementValue));
                    }
                }
            }
        }
    }

    private Adapter setupViewPagerAdapter(ViewPager viewPager) {
        Adapter adapter = new Adapter(this, getSupportFragmentManager());
        adapter.addFragment(new PeopleFragment(), this.getString(R.string.people));
        adapter.addFragment(new ConversationsFragment(), this.getString(R.string.chats));
        viewPager.setAdapter(adapter);
        return adapter;
    }

    @Override
    public void onResume() {
        super.onResume();
        checkAndRegEventBus();
        fetchUnreadMessagesCount();
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
        if (requestCode == PermissionsUtils.REQUEST_LOCATION
                || requestCode == PermissionsUtils.REQUEST_STORAGE
                && holloutPermissions.verifyPermissions(grantResults)) {
            if (requestCode == PermissionsUtils.REQUEST_LOCATION) {
                HolloutPreferences.setCanAccessLocation();
            }
            tryAskForPermissions();
        } else {
            UiUtils.snackMessage("To enjoy all features of hollout, please grant the requested permissions.",
                    rootLayout, true, "OK", new DoneCallback<Object>() {
                        @Override
                        public void done(Object result, Exception e) {
                            tryAskForPermissions();
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
        fetchUnreadMessagesCount();
        displaySignedInUserProps(AuthUtil.getCurrentUser());
        checkStartTimeStampUpdateServer();
        HolloutPreferences.incrementActivityCount();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        HolloutPreferences.destroyActivityCount();
    }

    private void checkStartTimeStampUpdateServer() {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        if (firebaseUser != null) {
            String email = firebaseUser.getEmail();
            if (email != null) {
                if (email.equals("holloutdev@gmail.com") || email.equals("wannclem@gmail.com") || email.equals("wanaclem@gmail.com")) {
                    Intent timeChangedServiceIntent = new Intent(this, TimeChangeDetectionService.class);
                    startService(timeChangedServiceIntent);
                }
            }
        }
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        checkAndRegEventBus();
        fetchUnreadMessagesCount();
    }

    private void turnOnLocationMessage() {
        UiUtils.snackMessage("To enjoy all features of hollout, please Turn on your location.",
                rootLayout, true, "OK", new DoneCallback<Object>() {
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
                    switch (s) {
                        case AppConstants.PLEASE_REQUEST_LOCATION_ACCESSS:
                            if (isLocationEnabled(MainActivity.this)) {
                                tryAskForPermissions();
                            } else {
                                turnOnLocationMessage();
                            }
                            break;
                        case AppConstants.ATTEMPT_LOGOUT:
                            attemptLogOut();
                            break;
                        case AppConstants.TURN_OFF_ALL_TAB_LAYOUTS:
                            toggleViews();
                            break;
                        case AppConstants.CHECK_SELECTED_CONVERSATIONS:
                            updateActionMode();
                            ConversationsFragment.conversationsAdapter.notifyDataSetChanged();
                            break;
                    }
                } else if (o instanceof UnreadFeedsBadge) {
                    UnreadFeedsBadge unreadFeedsBadge = (UnreadFeedsBadge) o;
                    updateTab(2, unreadFeedsBadge.getUnreadFeedsSize());
                } else if (o instanceof MessageReceivedEvent) {
                    MessageReceivedEvent messageReceivedEvent = (MessageReceivedEvent) o;
                    ChatMessage message = messageReceivedEvent.getMessage();
                    String sender = message.getFrom();
                    if (!HolloutUtils.isAContact(sender)) {
                        EventBus.getDefault().post(AppConstants.CHECK_FOR_NEW_CHAT_REQUESTS);
                    } else {
                        fetchUnreadMessagesCount();
                    }
                }
            }
        });
    }

    private void toggleViews() {
        showView(tabLayout, false);
        materialSearchView.showSearch(true);
        if (viewPager.getCurrentItem() == 1) {
            materialSearchView.setHint("Search your chats");
        }
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        tryAskForPermissions();
    }

    private void tryAskForPermissions() {
        if (Build.VERSION.SDK_INT >= 23 && PermissionsUtils.checkSelfPermissionForLocation(this)) {
            holloutPermissions.requestLocationPermissions();
            return;
        }
        if (Build.VERSION.SDK_INT >= 23 && PermissionsUtils.checkSelfForStoragePermission(this)) {
            holloutPermissions.requestStoragePermissions();
            return;
        }
        startAppInstanceDetectionService();
    }

    private void startAppInstanceDetectionService() {
        Intent mAppInstanceDetectIntent = new Intent(this, AppInstanceDetectionService.class);
        startService(mAppInstanceDetectIntent);
    }

    @Override
    protected void onPause() {
        super.onPause();
        HolloutPreferences.setStartPageIndex(viewPager.getCurrentItem());
        if (isFinishing()) overridePendingTransition(R.anim.fade_scale_in, R.anim.slide_to_right);
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(Gravity.START)) {
            drawerLayout.closeDrawer(Gravity.START);
            return;
        }

        if (actionModeBar.getVisibility() == View.VISIBLE) {
            destroyActionMode();
            return;
        }
        if (viewPager.getCurrentItem() != 0) {
            viewPager.setCurrentItem(0);
            return;
        }

        if (AppConstants.ARE_REACTIONS_OPEN) {
            EventBus.getDefault().post(AppConstants.CLOSE_REACTIONS);
            return;
        }

        if (materialSearchView.isSearchOpen()) {
            materialSearchView.closeSearch();
            return;
        }

        super.onBackPressed();

    }

    public static void destroyActionMode() {
        UiUtils.showView(actionModeBar, false);
        AppConstants.selectedPeople.clear();
        AppConstants.selectedPeoplePositions.clear();
        ConversationsFragment.conversationsAdapter.notifyDataSetChanged();
    }

    public static boolean isActionModeActivated() {
        return actionModeBar.getVisibility() == View.VISIBLE;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem filterPeopleMenuItem = menu.findItem(R.id.filter_people);
//        MenuItem createNewGroupChatItem = menu.findItem(R.id.create_new_group);
        filterPeopleMenuItem.setVisible(viewPager.getCurrentItem() == 0);
//        createNewGroupChatItem.setVisible(viewPager.getCurrentItem() == 1);
        supportInvalidateOptionsMenu();
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        //noinspection SimplifiableIfStatement
        if (id == R.id.filter_people) {
            initPeopleFilterDialog();
            return true;
        } else if (id == R.id.action_search) {
            toggleViews();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private String genderChoice = AppConstants.Both;

    @SuppressLint("SetTextI18n")
    private void initPeopleFilterDialog() {
        final ParseObject signedInUser = AuthUtil.getCurrentUser();
        if (signedInUser != null) {
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
            genderChoice = signedInUser.getString(AppConstants.GENDER_FILTER);
            if (StringUtils.isNotEmpty(genderChoice)) {
                if (genderChoice.equals(AppConstants.Both)) {
                    genderFilterOptionsGroup.check(R.id.both);
                } else if (genderChoice.equals(AppConstants.MALE)) {
                    genderFilterOptionsGroup.check(R.id.males_only);
                } else if (genderChoice.equals(AppConstants.FEMALE)) {
                    genderFilterOptionsGroup.check(R.id.females_only);
                }
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
                    AuthUtil.updateCurrentLocalUser(signedInUser, new DoneCallback<Boolean>() {
                        @Override
                        public void done(Boolean result, Exception e) {
                            dialog.dismiss();
                            dialog.cancel();
                            EventBus.getDefault().post(AppConstants.REFRESH_PEOPLE);
                        }
                    });
                }
            });
            peopleFilterDialog.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            peopleFilterDialog.create().show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RequestCodes.MEET_PEOPLE_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                EventBus.getDefault().postSticky(AppConstants.REFRESH_PEOPLE);
            }
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
        final ProgressDialog progressDialog = UiUtils.showProgressDialog(MainActivity.this, "Please wait...");
        tryBackUpChatsBeforeLoginOut(new DoneCallback<Boolean>() {
            @Override
            public void done(Boolean backUpSuccess, Exception e) {
                if (e == null && backUpSuccess) {
                    FirebaseAuth.getInstance().signOut();
                    dissolveLoggedInUser(progressDialog);
                } else {
                    UiUtils.dismissProgressDialog(progressDialog);
                    UiUtils.showSafeToast("Failed to sign you out.Please try again");
                }
            }
        });
    }

    private void dissolveLoggedInUser(final ProgressDialog progressDialog) {
        AuthUtil.logOutAuthenticatedUser(new DoneCallback<Boolean>() {
            @Override
            public void done(Boolean result, Exception e) {
                if (e == null) {
                    HolloutPreferences.setUserWelcomed(false);
                    HolloutPreferences.clearPersistedCredentials();
                    HolloutPreferences.getInstance().getAll().clear();
                    ParseObject.unpinAllInBackground(AppConstants.APP_USERS);
                    ParseObject.unpinAllInBackground(AppConstants.HOLLOUT_FEED);
                    HolloutUtils.getKryoInstance().reset();
                    AuthUtil.signOut(MainActivity.this)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        finishUp(progressDialog);
                                    } else {
                                        UiUtils.dismissProgressDialog(progressDialog);
                                        UiUtils.showSafeToast("Failed to sign you out.Please try again");
                                    }
                                }
                            });
                } else {
                    UiUtils.dismissProgressDialog(progressDialog);
                    UiUtils.showSafeToast("Failed to sign you out.Please try again");
                }
            }
        });
    }

    /**
     * Starts the sign-in process and initializes the Drive client.
     */
    @SuppressWarnings("ConstantConditions")
    protected void tryBackUpChatsBeforeLoginOut(final DoneCallback<Boolean> backUpCompletedOptionCallback) {
        DbUtils.fetchAllMessages(new DoneCallback<List<ChatMessage>>() {
            @Override
            public void done(List<ChatMessage> result, Exception e) {
                if (result != null && !result.isEmpty() && e == null) {
                    String serializeChats = JsonUtils.getGSon().toJson(result, JsonUtils.getListType());
                    ParseObject signedInUser = AuthUtil.getCurrentUser();
                    if (signedInUser != null) {
                        String signedInUserId = signedInUser.getString(AppConstants.REAL_OBJECT_ID);
                        if (StringUtils.isNotEmpty(signedInUserId)) {
                            FirebaseUtils
                                    .getArchives()
                                    .child(signedInUserId)
                                    .setValue(serializeChats)
                                    .addOnSuccessListener(getCurrentActivityInstance(), new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            backUpCompletedOptionCallback.done(true, null);
                                        }
                                    })
                                    .addOnFailureListener(getCurrentActivityInstance(), new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            backUpCompletedOptionCallback.done(false, new Exception("Failed to complete sign out. Please try again."));
                                        }
                                    });
                        } else {
                            backUpCompletedOptionCallback.done(true, null);
                        }
                    } else {
                        backUpCompletedOptionCallback.done(true, null);
                    }
                } else {
                    backUpCompletedOptionCallback.done(true, null);
                }
            }
        });
    }

    private void finishUp(ProgressDialog progressDialog) {
        UiUtils.dismissProgressDialog(progressDialog);
        UiUtils.showSafeToast("You've being logged out");
        Intent splashIntent = new Intent(MainActivity.this, SplashActivity.class);
        splashIntent.putExtra(AppConstants.FROM_MAIN, true);
        startActivity(splashIntent);
        finish();
    }

    private void launchSettings(String settingsFragmentName) {
        Intent settingsIntent = new Intent(MainActivity.this, SettingsActivity.class);
        settingsIntent.putExtra(AppConstants.SETTINGS_FRAGMENT_NAME, settingsFragmentName);
        startActivity(settingsIntent);
    }

    private void launchUserProfile() {
        Intent signedInUserIntent = new Intent(MainActivity.this, UserProfileActivity.class);
        if (AuthUtil.getCurrentUser() != null) {
            signedInUserIntent.putExtra(AppConstants.USER_PROPERTIES, AuthUtil.getCurrentUser());
            startActivity(signedInUserIntent);
        }
    }

    public static void vibrateVibrator() {
        vibrator.vibrate(100);
    }

    public static void activateActionMode() {
        UiUtils.showView(actionModeBar, true);
    }

    public void updateActionMode() {
        selectionActionsCountView.setText(String.valueOf(AppConstants.selectedPeople.size()));
        UiUtils.showView(blockUser, AppConstants.selectedPeople.size() == 1);
        if (AppConstants.selectedPeople.isEmpty()) {
            destroyActionMode();
        }
    }

    @SuppressWarnings("RedundantCast")
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
