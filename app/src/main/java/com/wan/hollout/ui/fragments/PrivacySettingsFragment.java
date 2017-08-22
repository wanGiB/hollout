package com.wan.hollout.ui.fragments;

import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceCategory;

import com.github.machinarius.preferencefragment.PreferenceFragment;
import com.parse.ParseObject;
import com.wan.hollout.R;
import com.wan.hollout.callbacks.DoneCallback;
import com.wan.hollout.utils.AppConstants;
import com.wan.hollout.utils.AuthUtil;

import org.apache.commons.lang3.StringUtils;

/**
 * * Created by Wan on 7/16/2016.
 */

public class PrivacySettingsFragment extends PreferenceFragment implements Preference.OnPreferenceChangeListener {

    private ListPreference locationVisibilityPref;
    private ListPreference lastSeenVisibilityPref;
    private ListPreference statusVisibilityPref;
    private ListPreference ageVisibilityPref;
    private PreferenceCategory locationSettingsCategory, presenceSettingsCategory, statusSettingsCategory, ageSettingsCategory;
    private Preference locationVisibilityContractPref, lastSeenVisibilityContractPref, statusVisibilityContractPref, ageVisibilityContractPref;
    private ParseObject parseUser;

    @Override
    public void onCreate(Bundle paramBundle) {
        super.onCreate(paramBundle);
        addPreferencesFromResource(R.xml.privacy_and_security_settings);
        parseUser = AuthUtil.getCurrentUser();

        locationSettingsCategory = (PreferenceCategory) findPreference("location_settings_category");
        ageSettingsCategory = (PreferenceCategory) findPreference("age_settings_category");
        statusSettingsCategory = (PreferenceCategory) findPreference("status_settings_category");
        presenceSettingsCategory = (PreferenceCategory) findPreference("presence_settings_category");

        locationVisibilityPref = (ListPreference) findPreference(AppConstants.LOCATION_VISIBILITY_PREF);
        locationVisibilityPref.setOnPreferenceChangeListener(this);

        lastSeenVisibilityPref = (ListPreference) findPreference(AppConstants.LAST_SEEN_VISIBILITY_PREF);
        lastSeenVisibilityPref.setOnPreferenceChangeListener(this);

        statusVisibilityPref = (ListPreference) findPreference(AppConstants.STATUS_VISIBILITY_PREF);
        statusVisibilityPref.setOnPreferenceChangeListener(this);

        ageVisibilityPref = (ListPreference) findPreference(AppConstants.AGE_VISIBILITY_PREF);
        ageVisibilityPref.setOnPreferenceChangeListener(this);

        locationVisibilityContractPref = new Preference(getActivity());
        locationVisibilityContractPref.setLayoutResource(R.layout.location_visibility_contract_layout);

        lastSeenVisibilityContractPref = new Preference(getActivity());
        lastSeenVisibilityContractPref.setLayoutResource(R.layout.last_seen_visibility_contract_layout);

        statusVisibilityContractPref = new Preference(getActivity());
        statusVisibilityContractPref.setLayoutResource(R.layout.status_visibilty_contract_layout);

        ageVisibilityContractPref = new Preference(getActivity());
        ageVisibilityContractPref.setLayoutResource(R.layout.age_visibility_contract_layout);

        if (parseUser != null) {

            String locationVisibilityValue = parseUser.getString(AppConstants.LOCATION_VISIBILITY_PREF);

            if (StringUtils.isNotEmpty(locationVisibilityValue)) {
                populateLocationSettings(locationVisibilityValue);
            }

            String lastSeenVisibilityValue = parseUser.getString(AppConstants.LAST_SEEN_VISIBILITY_PREF);
            if (StringUtils.isNotEmpty(lastSeenVisibilityValue)) {
                populateLastSeenSettings(lastSeenVisibilityValue);
            }

            String statusVisibilityValue = parseUser.getString(AppConstants.STATUS_VISIBILITY_PREF);
            if (StringUtils.isNotEmpty(statusVisibilityValue)) {
                populateStatusSettings(statusVisibilityValue);
            }

            String profileViewsVisibilityValue = parseUser.getString(AppConstants.AGE_VISIBILITY_PREF);

            populateAgeSettings(profileViewsVisibilityValue);

        }

    }

    @Override
    public boolean onPreferenceChange(final Preference preference, final Object newValue) {

        String key = preference.getKey();
        if (key.equals(lastSeenVisibilityPref.getKey())) {
            populateLastSeenSettings(newValue.toString());
        } else if (key.equals(locationVisibilityPref.getKey())) {
            populateLocationSettings(newValue.toString());
        } else if (key.equals(statusVisibilityPref.getKey())) {
            populateStatusSettings(newValue.toString());
        } else if (key.equals(ageVisibilityPref.getKey())) {
            populateAgeSettings(newValue.toString());
        }
        if (parseUser != null) {
            parseUser.put(preference.getKey(), newValue);
            AuthUtil.updateCurrentLocalUser(parseUser, new DoneCallback<Boolean>() {
                @Override
                public void done(Boolean result, Exception e) {

                }
            });
        }
        return true;
    }

    private void populateLocationSettings(String value) {
        if (value != null) {
            locationVisibilityPref.setSummary(value);
            if (value.equals(getString(R.string.visible_to_only_me))) {
                locationSettingsCategory.addPreference(locationVisibilityContractPref);
            } else {
                locationSettingsCategory.removePreference(locationVisibilityContractPref);
            }
        }
    }

    private void populateAgeSettings(String profileViewsVisibilityValue) {

        if (profileViewsVisibilityValue != null) {

            ageVisibilityPref.setSummary(profileViewsVisibilityValue);

            if (profileViewsVisibilityValue.equals(getString(R.string.visible_to_only_me))) {
                ageSettingsCategory.addPreference(ageVisibilityContractPref);
            } else {
                ageSettingsCategory.removePreference(ageVisibilityContractPref);
            }

        }

    }

    private void populateLastSeenSettings(String lastSeenVisibilityValue) {
        if (lastSeenVisibilityValue != null) {
            lastSeenVisibilityPref.setSummary(lastSeenVisibilityValue);
            if (lastSeenVisibilityValue.equals(getString(R.string.visible_to_only_me))) {
                presenceSettingsCategory.addPreference(lastSeenVisibilityContractPref);
            } else {
                presenceSettingsCategory.removePreference(lastSeenVisibilityContractPref);
            }
        }
    }

    private void populateStatusSettings(String statusVisibilityValue) {
        if (statusVisibilityValue != null) {
            statusVisibilityPref.setSummary(statusVisibilityValue);
            if (statusVisibilityValue.equals(getString(R.string.visible_to_only_me))) {
                statusSettingsCategory.addPreference(statusVisibilityContractPref);
            } else {
                statusSettingsCategory.removePreference(statusVisibilityContractPref);
            }
        }
    }

}
