package com.wan.hollout.ui.fragments;

import android.os.Bundle;
import android.preference.Preference;

import com.app.hollout.R;
import com.app.hollout.utils.HolloutUtils;
import com.github.machinarius.preferencefragment.PreferenceFragment;

/**
 * * Created by Wan on 7/16/2016.
 */

public class SupportSettings extends PreferenceFragment {

    @Override
    public void onCreate(Bundle paramBundle) {
        super.onCreate(paramBundle);
        addPreferencesFromResource(R.xml.support_settings);
        Preference aboutAppPreference = findPreference("app_version_key");
        aboutAppPreference.setTitle("Hollout Version " + HolloutUtils.getAppVersionName());
    }

}
