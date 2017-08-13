package com.wan.hollout.ui.activities;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;

import com.wan.hollout.R;
import com.wan.hollout.ui.fragments.ChatSettingsFragment;
import com.wan.hollout.ui.fragments.NotificationSettingsFragment;
import com.wan.hollout.ui.fragments.PrivacySettingsFragment;
import com.wan.hollout.ui.fragments.SupportSettings;
import com.wan.hollout.utils.AppConstants;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * * Created by Wan on 7/16/2016.
 */

public class SettingsActivity extends AppCompatActivity {

    @BindView(R.id.toolbar)
    public Toolbar toolbar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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
