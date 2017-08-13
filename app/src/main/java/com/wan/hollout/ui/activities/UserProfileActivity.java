package com.wan.hollout.ui.activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;

import com.afollestad.appthemeengine.customizers.ATEActivityThemeCustomizer;
import com.parse.ParseUser;
import com.wan.hollout.R;
import com.wan.hollout.utils.AppConstants;
import com.wan.hollout.utils.HolloutPreferences;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * @author Wan Clem
 */

public class UserProfileActivity extends BaseActivity implements ATEActivityThemeCustomizer {

    private boolean isDarkTheme;

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(R.id.user_cover_photo_view)
    ImageView userCoverPhotoView;

    private ParseUser parseUser;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        isDarkTheme = HolloutPreferences.getHolloutPreferences().getBoolean("dark_theme", false);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_profile);
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
        ButterKnife.bind(this);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }
        offloadIntent();
    }

    private void offloadIntent() {
        parseUser = getIntent().getExtras().getParcelable(AppConstants.USER_PROPERTIES);
        if (parseUser != null) {

        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem searchItem = menu.findItem(R.id.action_search);
        searchItem.setVisible(false);
        MenuItem settingsActionItem = menu.findItem(R.id.action_settings);
        settingsActionItem.setVisible(false);
        supportInvalidateOptionsMenu();
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public int getActivityTheme() {
        return isDarkTheme ? R.style.AppThemeNormalDark : R.style.AppThemeNormalLight;
    }

}
