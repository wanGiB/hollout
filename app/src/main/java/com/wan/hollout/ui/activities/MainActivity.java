package com.wan.hollout.ui.activities;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.content.SharedPreferences;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Bundle;
import android.os.Vibrator;
import android.provider.Settings;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.JobIntentService;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;
import com.parse.ParseObject;
import com.wan.hollout.R;
import com.wan.hollout.eventbuses.MessageReceivedEvent;
import com.wan.hollout.eventbuses.SearchChatsEvent;
import com.wan.hollout.eventbuses.SearchPeopleEvent;
import com.wan.hollout.eventbuses.UnreadFeedsBadge;
import com.wan.hollout.interfaces.DoneCallback;
import com.wan.hollout.models.ChatMessage;
import com.wan.hollout.models.ConversationItem;
import com.wan.hollout.ui.fragments.ActivitiesFragment;
import com.wan.hollout.ui.fragments.ConversationsFragment;
import com.wan.hollout.ui.fragments.NearbyPeopleFragment;
import com.wan.hollout.ui.services.AppInstanceDetectionService;
import com.wan.hollout.ui.services.TimeChangeDetectionService;
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
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import butterknife.BindView;
import butterknife.ButterKnife;

import static com.wan.hollout.utils.UiUtils.runOnMain;
import static com.wan.hollout.utils.UiUtils.showView;

@SuppressWarnings("RedundantCast")
@SuppressLint("StaticFieldLeak")
public class MainActivity extends BaseActivity
        implements ActivityCompat.OnRequestPermissionsResultCallback {

    /**
     * The desired interval for location updates. Inexact. Updates may be more or less frequent.
     */
    private static final long UPDATE_INTERVAL_IN_MILLISECONDS = 60000;

    /**
     * The fastest rate for active location updates. Exact. Updates will never be more frequent
     * than this value.
     */
    private static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS =
            UPDATE_INTERVAL_IN_MILLISECONDS / 2;

    /**
     * Constant used in the location settings dialog.
     */
    private static final int REQUEST_CHECK_SETTINGS = 0x1;

    /**
     * Provides access to the Location Settings API.
     */
    private SettingsClient mSettingsClient;

    /**
     * Stores the types of location services the client is interested in using. Used for checking
     * settings to determine if the device has optimal location settings.
     */
    private LocationSettingsRequest mLocationSettingsRequest;

    /**
     * Stores parameters for requests to the FusedLocationProviderApi.
     */
    private LocationRequest mLocationRequest;

    @BindView(R.id.footerAd)
    LinearLayout footerView;

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.rootLayout)
    View rootLayout;

    @BindView(R.id.user_photo_container)
    View userPhotoContainer;

    public static ViewPager viewPager;
    public static TabLayout tabLayout;
    public static MaterialSearchView materialSearchView;

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

    @BindView(R.id.signed_in_user_profile_image_view)
    ImageView signedInUserImageView;

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
        tabLayout = findViewById(R.id.tabs);
        viewPager = findViewById(R.id.viewpager);
        actionModeBar = findViewById(R.id.action_mode_bar);
        materialSearchView = findViewById(R.id.search_view);
        setSupportActionBar(toolbar);
        ParseObject signedInUserObject = AuthUtil.getCurrentUser();
        loadSignedInUserImage(signedInUserObject);
        Adapter adapter = setupViewPagerAdapter(viewPager);
        viewPager.setOffscreenPageLimit(3);
        tabLayout.setSelectedTabIndicatorHeight(7);
        tabLayout.setupWithViewPager(viewPager);
        setupTabs(adapter);
        fetchUnreadMessagesCount();
        viewPager.setCurrentItem(HolloutPreferences.getStartPageIndex());
        vibrator = (Vibrator) getSystemService(VIBRATOR_SERVICE);
        if (!HolloutPreferences.isUserWelcomed()) {
            if (signedInUserObject != null) {
                UiUtils.showSafeToast("Welcome, " + WordUtils.capitalize(signedInUserObject.getString(AppConstants.APP_USER_DISPLAY_NAME)));
            }
            HolloutPreferences.setUserWelcomed(true);
        }
        initAndroidPermissions();
        attachEventHandlers();
        GeneralNotifier.getNotificationManager().cancel(AppConstants.CHAT_REQUEST_NOTIFICATION_ID);
        GeneralNotifier.getNotificationManager().cancel(AppConstants.NEARBY_KIND_NOTIFICATION_ID);
        GeneralNotifier.getNotificationManager().cancel(AppConstants.NEW_MESSAGE_NOTIFICATION_ID);
        initEventHandlers();
        createDeleteConversationProgressDialog();
        createLocationRequest();
        mSettingsClient = LocationServices.getSettingsClient(this);
        buildLocationSettingsRequest();
    }

    /**
     * Sets up the location request. Android has two location request settings:
     * {@code ACCESS_COARSE_LOCATION} and {@code ACCESS_FINE_LOCATION}. These settings control
     * the accuracy of the current location. This sample uses ACCESS_FINE_LOCATION, as defined in
     * the AndroidManifest.xml.
     * <p/>
     * When the ACCESS_FINE_LOCATION setting is specified, combined with a fast update
     * interval (5 seconds), the Fused Location Provider API returns location updates that are
     * accurate to within a few feet.
     * <p/>
     * These settings are appropriate for mapping applications that show real-time location
     * updates.
     */
    private void createLocationRequest() {
        mLocationRequest = LocationRequest.create();

        // Sets the desired interval for active location updates. This interval is
        // inexact. You may not receive updates at all if no location sources are available, or
        // you may receive them slower than requested. You may also receive updates faster than
        // requested if other applications are requesting location at a faster interval.
        mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);

        // Sets the fastest rate for active location updates. This interval is exact, and your
        // application will never receive updates faster than this value.
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);

        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    /**
     * Uses a {@link com.google.android.gms.location.LocationSettingsRequest.Builder} to build
     * a {@link com.google.android.gms.location.LocationSettingsRequest} that is used for checking
     * if a device has the needed location settings.
     */
    private void buildLocationSettingsRequest() {
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        mLocationSettingsRequest = builder.build();
    }

    private void checkLocationSettingsAvailable() {
        mSettingsClient.checkLocationSettings(mLocationSettingsRequest)
                .addOnSuccessListener(this, new OnSuccessListener<LocationSettingsResponse>() {
                    @Override
                    public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                        startAppInstanceDetectionService();
                    }
                }).addOnFailureListener(this, new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                int statusCode = ((ApiException) e).getStatusCode();
                switch (statusCode) {
                    case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                        HolloutLogger.i("AppInstanceDetectionService",
                                "Location settings are not satisfied. Attempting to upgrade " +
                                        "location settings ");
                        try {
                            // Show the dialog by calling startResolutionForResult(), and check the
                            // result in onActivityResult().
                            ResolvableApiException rae = (ResolvableApiException) e;
                            rae.startResolutionForResult(MainActivity.this, REQUEST_CHECK_SETTINGS);
                        } catch (IntentSender.SendIntentException sie) {
                            HolloutLogger.i("AppInstanceDetectionService", "PendingIntent unable to execute request.");
                        }
                        break;
                    case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                        String errorMessage = "Location settings are inadequate, and cannot be " +
                                "fixed here. Fix in Settings.";
                        HolloutLogger.e("AppInstanceDetectionService", errorMessage);
                        runOnMain(new Runnable() {
                            @Override
                            public void run() {
                                turnOnLocationMessage();
                            }
                        });
                }
            }
        });
    }

    private void loadSignedInUserImage(ParseObject signedInUserObject) {
        if (signedInUserObject != null) {
            String userPhotoUrl = signedInUserObject.getString(AppConstants.APP_USER_PROFILE_PHOTO_URL);
            if (StringUtils.isNotEmpty(userPhotoUrl)) {
                UiUtils.loadImage(this, userPhotoUrl, signedInUserImageView);
            }
        }
    }

    public void fetchMyPhotoLikes() {
        ParseObject signedInUserObject = AuthUtil.getCurrentUser();
        if (signedInUserObject != null) {
            String signedInUserId = signedInUserObject.getString(AppConstants.REAL_OBJECT_ID);
            if (StringUtils.isNotEmpty(signedInUserId)) {
                FirebaseUtils.getPhotoLikesReference().child(signedInUserId)
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if (dataSnapshot != null && dataSnapshot.exists()) {
                                    long dataSnapShotCount = dataSnapshot.getChildrenCount();
                                    if (dataSnapShotCount != 0) {
                                        GenericTypeIndicator<HashMap<String, Object>> genericTypeIndicator = new
                                                GenericTypeIndicator<HashMap<String, Object>>() {
                                                };
                                        List<HashMap<String, Object>> unseenPhotoLikes = new ArrayList<>();
                                        for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                            HashMap<String, Object> photoLike = snapshot.getValue(genericTypeIndicator);
                                            if (photoLike != null) {
                                                Boolean previewedByOwner = photoLike.containsKey(AppConstants.PREVIEWED);
                                                if (!previewedByOwner) {
                                                    unseenPhotoLikes.add(photoLike);
                                                }
                                            }
                                        }
                                        if (!unseenPhotoLikes.isEmpty()) {
                                            TabLayout.Tab tab = MainActivity.tabLayout.getTabAt(2);
                                            if (tab != null) {
                                                View viewAtTab = tab.getCustomView();
                                                if (viewAtTab != null && MainActivity.viewPager != null &&
                                                        MainActivity.viewPager.getCurrentItem() != 2) {
                                                    UiUtils.showView(viewAtTab.findViewById(R.id.activity_indicator), true);
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {

                            }

                        });

            }
        }
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
                                for (final ConversationItem conversationItem : AppConstants.selectedPeople) {
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
                                                    ParseObject signedInUserObject = AuthUtil.getCurrentUser();
                                                    if (signedInUserObject != null) {
                                                        List<String> userChats = signedInUserObject.getList(AppConstants.APP_USER_CHATS);
                                                        if (userChats != null && userChats.contains(conversationItem.getRecipient().getString(AppConstants.REAL_OBJECT_ID).toUpperCase())) {
                                                            userChats.remove(conversationItem.getRecipient().getString(AppConstants.REAL_OBJECT_ID).toUpperCase());
                                                            signedInUserObject.put(AppConstants.APP_USER_CHATS, userChats);
                                                            AuthUtil.updateCurrentLocalUser(signedInUserObject, null);
                                                            EventBus.getDefault().post(AppConstants.REFRESH_CONVERSATIONS);
                                                        }
                                                    }
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
                    case R.id.user_photo_container:
                        launchUserProfile();
                        break;
                }
            }
        };
        blockUser.setOnClickListener(onClickListener);
        deleteConversation.setOnClickListener(onClickListener);
        destroyActionModeView.setOnClickListener(onClickListener);
        userPhotoContainer.setOnClickListener(onClickListener);
    }

    private void attachEventHandlers() {
        materialSearchView.setOnSearchViewListener(new MaterialSearchView.SearchViewListener() {

            @Override
            public void onSearchViewShown() {
                EventBus.getDefault().post(AppConstants.DISABLE_NESTED_SCROLLING);
//                UiUtils.showView(floatingActionButton, false);
            }

            @Override
            public void onSearchViewClosed() {
                EventBus.getDefault().post(AppConstants.ENABLE_NESTED_SCROLLING);
                showView(tabLayout, true);
                EventBus.getDefault().post(AppConstants.SEARCH_VIEW_CLOSED);
//                UiUtils.showView(floatingActionButton, true);
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
                hideActivityIndicator(position);
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

    private void hideActivityIndicator(int position) {
        if (position == 2) {
            TabLayout.Tab tab = MainActivity.tabLayout.getTabAt(2);
            if (tab != null) {
                View viewAtTab = tab.getCustomView();
                if (viewAtTab != null) {
                    UiUtils.showView(viewAtTab.findViewById(R.id.activity_indicator), false);
                }
            }
        }
    }

    private void attemptProfileEdit() {
        startActivity(new Intent(getCurrentActivityInstance(), EditProfileActivity.class));
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
        adapter.addFragment(new NearbyPeopleFragment(), this.getString(R.string.nearby));
        adapter.addFragment(new ConversationsFragment(), this.getString(R.string.chats));
        adapter.addFragment(new ActivitiesFragment(), this.getString(R.string.activities));
        viewPager.setAdapter(adapter);
        return adapter;
    }

    @Override
    public void onResume() {
        super.onResume();
        ParseObject parseObject = AuthUtil.getCurrentUser();
        if (parseObject != null) {
            loadSignedInUserImage(parseObject);
        }
        fetchUnreadMessagesCount();
        checkIfPhotoIsBlurredOrUnclear();
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


    private void initAndroidPermissions() {
        holloutPermissions = new HolloutPermissions(this, footerView);
    }

    @Override
    protected void onStart() {
        super.onStart();
        fetchUnreadMessagesCount();
        ParseObject signedInUserObject = AuthUtil.getCurrentUser();
        if (signedInUserObject != null) {
            loadSignedInUserImage(signedInUserObject);
        }
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

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem searchMenuItem = menu.findItem(R.id.action_search);
        MenuItem peopleFilterMenuItem = menu.findItem(R.id.filter_people);
        MenuItem continueMenuItem = menu.findItem(R.id.button_continue);
        MenuItem profileMenuItem = menu.findItem(R.id.button_view_profile);
        MenuItem inviteFriends = menu.findItem(R.id.invite_friends);
        MenuItem logOut = menu.findItem(R.id.log_out);
        logOut.setVisible(true);
        inviteFriends.setVisible(true);
        profileMenuItem.setVisible(true);
        continueMenuItem.setVisible(false);
        searchMenuItem.setVisible(viewPager.getCurrentItem() != 2);
        peopleFilterMenuItem.setVisible(viewPager.getCurrentItem() == 0);
        supportInvalidateOptionsMenu();
        return super.onPrepareOptionsMenu(menu);
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
                            tryAskForPermissions();
                            break;
                        case AppConstants.TURN_OFF_ALL_TAB_LAYOUTS:
                            toggleViews();
                            break;
                        case AppConstants.CHECK_SELECTED_CONVERSATIONS:
                            updateActionMode();
                            ConversationsFragment.conversationsAdapter.notifyDataSetChanged();
                            break;
                        case AppConstants.ACCOUNT_DELETED_EVENT:
                            if (!isFinishing()) {
                                finish();
                            }
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
                        EventBus.getDefault().post(AppConstants.ORDER_CONVERSATIONS);
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
        fetchMyPhotoLikes();
        checkIfPhotoIsBlurredOrUnclear();
    }

    private void checkIfPhotoIsBlurredOrUnclear() {
        ParseObject signedInUser = AuthUtil.getCurrentUser();
        if (signedInUser != null) {
            String userPhotoUrl = signedInUser.getString(AppConstants.APP_USER_PROFILE_PHOTO_URL);
            String message = null;
            if (userPhotoUrl == null) {
                message = "Please setup your profile pic";
            } else {
                if (!StringUtils.containsIgnoreCase(userPhotoUrl, "firebase")) {
                    message = "Your current photo is blurred or unclear.";
                }
            }
            if (message != null) {
                displayProfileImageBrokenSnackBar(message);
            }
        }
    }

    private void displayProfileImageBrokenSnackBar(String message) {
        Snackbar snackbar = Snackbar.make(rootLayout, message,
                Snackbar.LENGTH_INDEFINITE).setAction("UPLOAD NEW",
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        attemptProfileEdit();
                    }
                });
        snackbar.show();
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
        checkLocationSettingsAvailable();
    }

    private void startAppInstanceDetectionService() {
        ParseObject signedInUserObject = AuthUtil.getCurrentUser();
        if (signedInUserObject != null) {
            Intent serviceIntent = new Intent();
            JobIntentService.enqueueWork(this, AppInstanceDetectionService.class, AppConstants.FIXED_JOB_ID, serviceIntent);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        HolloutPreferences.setStartPageIndex(viewPager.getCurrentItem());
        if (isFinishing()) overridePendingTransition(R.anim.fade_scale_in, R.anim.slide_to_right);
    }

    @Override
    public void onBackPressed() {
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
//            UiUtils.showView(floatingActionButton, true);
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
        } else if (id == R.id.button_view_profile) {
            launchUserProfile();
            return true;
        } else if (id == R.id.invite_friends) {
            initSharing();
            return true;
        } else if (id == R.id.log_out) {
            attemptLogOut();
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
        } else if (requestCode == REQUEST_CHECK_SETTINGS) {
            if (resultCode == RESULT_OK) {
                startAppInstanceDetectionService();
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
