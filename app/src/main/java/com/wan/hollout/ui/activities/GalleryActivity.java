package com.wan.hollout.ui.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;

import com.afollestad.appthemeengine.ATE;
import com.afollestad.appthemeengine.Config;
import com.afollestad.appthemeengine.customizers.ATEActivityThemeCustomizer;
import com.wan.hollout.R;
import com.wan.hollout.bean.HolloutFile;
import com.wan.hollout.ui.adapters.MainViewPagerAdapter;
import com.wan.hollout.ui.fragments.UserMusicFragment;
import com.wan.hollout.ui.fragments.UserPhotosFragment;
import com.wan.hollout.ui.fragments.UserVideosFragment;
import com.wan.hollout.ui.widgets.HolloutTextView;
import com.wan.hollout.ui.widgets.MaterialSearchView;
import com.wan.hollout.utils.ATEUtils;
import com.wan.hollout.utils.AppConstants;
import com.wan.hollout.utils.HolloutPreferences;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

@SuppressWarnings("unused")
public class GalleryActivity extends BaseActivity implements ATEActivityThemeCustomizer,
        ActivityCompat.OnRequestPermissionsResultCallback,
        ViewPager.OnPageChangeListener, View.OnClickListener {

    private boolean isDarkTheme;

    @BindView(R.id.toolbar)
    public Toolbar toolbar;

    @BindView(R.id.viewpager)
    ViewPager galleryPager;

    @BindView(R.id.tabs)
    TabLayout galleryTabsLayout;

    @BindView(R.id.search_view)
    MaterialSearchView searchView;

    @BindView(R.id.content_selected_footer_view)
    LinearLayout selectedItemsFooterView;

    @BindView(R.id.selected_item_count)
    HolloutTextView selectedItemCountView;

    @BindView(R.id.send_selected_items)
    FloatingActionButton sendSelectedItemsFab;

    @BindView(R.id.cancel_selections)
    HolloutTextView cancelSelections;

    public MainViewPagerAdapter pagerAdapter;

    private static final int EXTERNAL_STORAGE_ACCESS_REQUEST_CODE = 2;
    public boolean aSearchInProgress = false;
    public boolean onFirstPage = true;

    public static boolean maxFileForATransferReached;

    public ArrayList<HolloutFile> selectedFiles = new ArrayList<>();

    private ArrayList<Fragment> fragments = new ArrayList<>();
    private ArrayList<String> titles = new ArrayList<>();

    private int initPosition;
    private String initTitle;

    //When a user is sending out files to another user  it is either images,videos and music files are sent out separately and not together.Let's keep track of the last received file type using this
    public String lastReceivedFileType = null;

    public boolean isMaximumFileForATransferReached() {
        return maxFileForATransferReached;
    }

    public static void setMaxFileForATransferReached(boolean maximumFileForTransferReached) {
        maxFileForATransferReached = maximumFileForTransferReached;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        isDarkTheme = HolloutPreferences.getHolloutPreferences().getBoolean("dark_theme", false);
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
        setContentView(R.layout.gallery);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            String recipientName = extras.getString(AppConstants.RECIPIENT_NAME);
            if (StringUtils.isNotEmpty(recipientName)) {
                setupToolbarText("Share files with " + StringUtils.capitalize(recipientName));
            } else {
                initTitle = extras.getString(AppConstants.INIT_TITLE);
                initPosition = extras.getInt(AppConstants.INIT_POSITION);
                if (StringUtils.isNotEmpty(initTitle)) {
                    setupToolbarText(initTitle);
                } else {
                    setupToolbarText("Share files");
                }
            }
        }
        if (getIntent().getExtras() != null) {
            Bundle bundle = getIntent().getExtras();
        }

        if (!titles.contains("Photos")) {
            titles.add("Photos");
            fragments.add(new UserPhotosFragment());
        }

        if (!titles.contains("Videos")) {
            titles.add("Videos");
            fragments.add(new UserVideosFragment());
        }

        if (initTitle == null) {
            if (!titles.contains("Music")) {
                titles.add("Music");
                fragments.add(new UserMusicFragment());
            }
        }

        galleryTabsLayout.setSelectedTabIndicatorHeight(6);
        pagerAdapter = new MainViewPagerAdapter(this, getSupportFragmentManager(), fragments, titles);

        if (galleryPager != null) {
            setupViewPager(galleryPager);
            setupTabs();
        }

        if (HolloutPreferences.getHolloutPreferences().getBoolean("dark_theme", false)) {
            ATE.apply(this, "dark_theme");
        } else {
            ATE.apply(this, "light_theme");
        }

        searchView.setOnSearchViewListener(new MaterialSearchView.SearchViewListener() {

            @Override
            public void onSearchViewShown() {
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {

                    }
                }, 50);
            }

            @Override
            public void onSearchViewClosed() {

            }

        });

        searchView.setOnQueryTextListener(new MaterialSearchView.OnQueryTextListener() {

            @Override
            public boolean onQueryTextSubmit(String query) {
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                if (galleryPager.getCurrentItem() == 2) {

                } else if (galleryPager.getCurrentItem() == 1) {

                }
                return true;
            }

        });

        galleryPager.addOnPageChangeListener(this);

        sendSelectedItemsFab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (!selectedFiles.isEmpty()) {
                    Intent mCallerIntent = new Intent();
                    mCallerIntent.putParcelableArrayListExtra(AppConstants.GALLERY_RESULTS, selectedFiles);
                    setResult(RESULT_OK, mCallerIntent);
                    finish();
                }
            }
        });

        cancelSelections.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        if (selectedFiles.isEmpty()) {
            sendSelectedItemsFab.hide();
        }

    }

    @Override
    public void onResume() {
        super.onResume();
        String ateKey = HolloutPreferences.getATEKey();
        ATEUtils.setStatusBarColor(this, ateKey, Config.primaryColor(this, ateKey));
    }

    @Override
    public int getActivityTheme() {
        return isDarkTheme ? R.style.AppThemeNormalDark : R.style.AppThemeNormalLight;
    }

    private void setupToolbarText(String title) {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(title);
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public void finish() {
        super.finish();
        selectedFiles.clear();
    }

    private void setupTabs() {
        galleryTabsLayout.setTabGravity(TabLayout.GRAVITY_FILL);
        galleryPager.setAdapter(pagerAdapter);
        galleryTabsLayout.setupWithViewPager(galleryPager);
        for (int i = 0; i < galleryTabsLayout.getTabCount(); i++) {
            TabLayout.Tab tab = galleryTabsLayout.getTabAt(i);
            if (tab != null) {
                tab.setCustomView(pagerAdapter.getCustomTabView(i));
            }
        }

        if (initPosition != 0) {
            galleryPager.setCurrentItem(initPosition);
        }

        galleryTabsLayout.requestFocus();
    }


    private void setupViewPager(ViewPager viewPager) {
        viewPager.setAdapter(pagerAdapter);
        viewPager.setOffscreenPageLimit(3);
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (isFinishing()) overridePendingTransition(R.anim.fade_scale_in, R.anim.slide_to_right);
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        MenuItem searchItem = menu.findItem(R.id.action_search);

        searchItem.setVisible(false);

        MenuItem filterPeopleMenuItem = menu.findItem(R.id.filter_people);
        MenuItem createNewGroupItem  = menu.findItem(R.id.create_new_group);
        MenuItem invitePeopleMenuItem = menu.findItem(R.id.invite_people);

        invitePeopleMenuItem.setVisible(false);
        createNewGroupItem.setVisible(false);
        filterPeopleMenuItem.setVisible(false);

        supportInvalidateOptionsMenu();

        return super.onPrepareOptionsMenu(menu);

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onPageSelected(int position) {
        switch (position) {
            case 0:
                if (searchView.isSearchOpen()) {
                    searchView.closeSearch();
                }
                onFirstPage = true;
                if (initTitle != null) {
                    setupToolbarText("Attach Photos");
                }
                break;
            case 1:
                if (initTitle != null) {
                    setupToolbarText("Attach Videos");
                }
                searchView.setHint("Search Videos");
                onFirstPage = false;
                break;
            case 2:
                if (initTitle != null) {
                    setupToolbarText("Attach Audio");
                }
                searchView.setHint("Search Music");
                onFirstPage = false;
                break;
        }
    }

    @Override
    public void onBackPressed() {
        if (searchView.isSearchOpen()) {
            searchView.closeSearch();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    @Override
    public void onClick(View v) {
    }

    public void addFile(HolloutFile holloutFile) {
        if (lastReceivedFileType == null) {
            lastReceivedFileType = holloutFile.getFileType();
            if (!selectedFiles.contains(holloutFile)) {
                selectedFiles.add(holloutFile);
            }
        } else {
            if (!holloutFile.getFileType().equals(lastReceivedFileType)) {
                //A new set of file type was received,clear previous selections
                selectedFiles.clear();
                selectedFiles.add(holloutFile);
                lastReceivedFileType = holloutFile.getFileType();
            } else {
                if (!selectedFiles.contains(holloutFile)) {
                    selectedFiles.add(holloutFile);
                }
            }
        }

        if (!selectedFiles.isEmpty()) {
            displaySelectedFiles(selectedFiles.size(), lastReceivedFileType);
        } else {
            selectedItemCountView.setText(" ");
            sendSelectedItemsFab.hide();
        }
    }

    public ArrayList<HolloutFile> getSelectedFiles() {
        return selectedFiles;
    }

    public void removeFile(HolloutFile holloutFile) {
        if (selectedFiles.contains(holloutFile)) {
            selectedFiles.remove(holloutFile);
        }
        if (!selectedFiles.isEmpty()) {
            displaySelectedFiles(selectedFiles.size(), lastReceivedFileType);
        } else {
            selectedItemCountView.setText(" ");
            sendSelectedItemsFab.hide();
        }
    }

    private void displaySelectedFiles(int sizeOfSelectedFiles, String lastReceivedFileType) {

        sendSelectedItemsFab.show();

        String countSuffix;

        switch (lastReceivedFileType) {
            case AppConstants.FILE_TYPE_PHOTO:
                countSuffix = "Photo";
                break;
            case AppConstants.FILE_TYPE_AUDIO:
                countSuffix = "Audio";
                break;
            default:
                countSuffix = "Video";
                break;
        }

        if (sizeOfSelectedFiles == 1) {
            selectedItemCountView.setText(sizeOfSelectedFiles + " " + countSuffix);
        } else if (sizeOfSelectedFiles > 1) {
            if (!countSuffix.equals("Audio")) {
                selectedItemCountView.setText(sizeOfSelectedFiles + " " + countSuffix + "s");
            } else {
                selectedItemCountView.setText(sizeOfSelectedFiles + " " + countSuffix);
            }
        }

    }

    @Override
    protected void onStop() {
        super.onStop();
        selectedFiles.clear();
    }

}
