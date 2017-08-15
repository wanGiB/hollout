package com.wan.hollout.ui.activities;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.afollestad.appthemeengine.customizers.ATEActivityThemeCustomizer;
import com.wan.hollout.R;
import com.wan.hollout.ui.fragments.ChatSettingsFragment;
import com.wan.hollout.ui.fragments.NotificationSettingsFragment;
import com.wan.hollout.ui.fragments.PrivacySettingsFragment;
import com.wan.hollout.ui.fragments.SupportSettings;
import com.wan.hollout.utils.AppConstants;
import com.wan.hollout.utils.HolloutPreferences;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * * Created by Wan on 7/16/2016.
 */

public class SettingsActivity extends BaseActivity implements ATEActivityThemeCustomizer {

    @BindView(R.id.toolbar)
    public Toolbar toolbar;

    private boolean isDarkTheme;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        isDarkTheme = HolloutPreferences.getHolloutPreferences().getBoolean("dark_theme", false);
        super.onCreate(savedInstanceState);
        AppCompatDelegate.setCompatVectorFromResourcesEnabled(true);
        setContentView(R.layout.settings);
        ButterKnife.bind(this);

        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        if (getIntent().getExtras() != null) {
            String fragmentName = getIntent().getStringExtra(AppConstants.SETTINGS_FRAGMENT_NAME);
            switch (fragmentName) {
                case AppConstants.NOTIFICATION_SETTINGS_FRAGMENT:
                    updateActionBar("Notification Settings");
                    getSupportFragmentManager().beginTransaction().add(R.id.settings_content, new NotificationSettingsFragment()).commit();
                    break;
                case AppConstants.CHATS_SETTINGS_FRAGMENT:
                    updateActionBar("Chat Settings");
                    getSupportFragmentManager().beginTransaction().add(R.id.settings_content, new ChatSettingsFragment()).commit();
                    break;
                case AppConstants.SUPPORT_SETTINGS_FRAGMENT:
                    updateActionBar("Support Settings");
                    getSupportFragmentManager().beginTransaction().add(R.id.settings_content, new SupportSettings()).commit();
                    break;
                case AppConstants.PRIVACY_AND_SECURITY_FRAGMENT:
                    updateActionBar("Privacy and Security");
                    getSupportFragmentManager().beginTransaction().add(R.id.settings_content, new PrivacySettingsFragment()).commit();
                    break;
            }
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
    protected void onPause() {
        super.onPause();
        if (isFinishing()) overridePendingTransition(R.anim.fade_scale_in, R.anim.slide_to_right);
    }

    @Override
    public int getActivityTheme() {
        return isDarkTheme ? R.style.AppThemeNormalDark : R.style.AppThemeNormalLight;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    private void updateActionBar(String title) {
        if (getSupportActionBar() != null) {
            getSupportActionBar().setTitle(title);
        }
    }

}
