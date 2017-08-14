package com.wan.hollout.ui.activities;

import android.annotation.TargetApi;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.parse.ParseObject;
import com.parse.ParseUser;
import com.wan.hollout.R;
import com.wan.hollout.ui.adapters.SlidePagerAdapter;
import com.wan.hollout.ui.widgets.PageIndicator;
import com.wan.hollout.utils.AppConstants;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;

public class SlidePagerActivity extends AppCompatActivity {

    private PageIndicator mPageIndicator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_slide_pager);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        showUi();
        ViewPager pager = (ViewPager) findViewById(R.id.pager);
        SlidePagerAdapter pagerAdapter = new SlidePagerAdapter(getSupportFragmentManager());
        if (getIntent() == null) return;
        String title = getIntent().getStringExtra(AppConstants.EXTRA_TITLE);
        String userId = getIntent().getStringExtra(AppConstants.APP_USER_ID);
        ParseObject signedInUser =  ParseUser.getCurrentUser();
        if (signedInUser != null) {
            if (signedInUser.getObjectId().equals(userId)) {
                getSupportActionBar().setTitle("Me");
            } else {
                if (StringUtils.isNotEmpty(title)) {
                    getSupportActionBar().setTitle(StringUtils.capitalize(title));
                } else {
                    getSupportActionBar().setTitle("Profile Photos");
                }
            }
            // set pictures
            ArrayList<String> pics = getIntent().getStringArrayListExtra(AppConstants.EXTRA_PICTURES);
            pagerAdapter.addAll(pics);
            pager.setAdapter(pagerAdapter);
            pager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                                              @Override
                                              public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

                                              }

                                              @Override
                                              public void onPageSelected(int position) {

                                              }

                                              @Override
                                              public void onPageScrollStateChanged(int state) {

                                              }

                                          });

            mPageIndicator = (PageIndicator) findViewById(R.id.indicator);

            mPageIndicator.setIndicatorType(PageIndicator.IndicatorType.FRACTION);
            mPageIndicator.setViewPager(pager);

            getWindow().getDecorView().setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {

                @Override
                public void onSystemUiVisibilityChange(int visibility) {
                    if (visibility == 0) {
                        getSupportActionBar().show();
                    }
                }
            });
        }
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            showUi();
        } else {
            hideUi();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (isFinishing())
            overridePendingTransition(R.anim.slide_from_right, R.anim.fade_scale_out);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            toggleActionBar();
        }
        return true;
    }

    private void toggleActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            if (actionBar.isShowing()) {
                actionBar.hide();
                hideUi();
            } else {
                showUi();
                actionBar.show();
            }
        }
    }

    private void showUi() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);

        } else {
            requestWindowFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
            getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
    }

    private void hideUi() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN);
        } else {
            requestWindowFeature(Window.FEATURE_NO_TITLE);
            getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case android.R.id.home:
                finish();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

}
