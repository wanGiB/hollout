package com.wan.hollout.ui.fragments;

import android.os.Bundle;
import android.preference.Preference;
import android.preference.SwitchPreference;

import com.github.machinarius.preferencefragment.PreferenceFragment;
import com.parse.ParseUser;
import com.wan.hollout.R;
import com.wan.hollout.utils.AppConstants;

/**
 * @author Wan Clem
 */

public class NotificationSettingsFragment extends PreferenceFragment implements Preference.OnPreferenceChangeListener {

    private ParseUser parseUser;

    @Override
    public void onCreate(Bundle paramBundle) {
        super.onCreate(paramBundle);
        addPreferencesFromResource(R.xml.notification_settings);

        parseUser = ParseUser.getCurrentUser();

        SwitchPreference playSoundOnNewMessageSwitch = (SwitchPreference) findPreference(AppConstants.PLAY_SOUND_ON_NEW_MESAGE_NOTIF);
        playSoundOnNewMessageSwitch.setOnPreferenceChangeListener(this);

        SwitchPreference wakePhoneOnNotifSwitch = (SwitchPreference) findPreference(AppConstants.WAKE_PHONE_ON_NOTIFICATION);
        wakePhoneOnNotifSwitch.setOnPreferenceChangeListener(this);

        SwitchPreference showMessageTickerSwitch = (SwitchPreference) findPreference(AppConstants.SHOW_MESSAGE_TICKER);
        showMessageTickerSwitch.setOnPreferenceChangeListener(this);

        SwitchPreference vibrateOnNotifSwitch = (SwitchPreference) findPreference(AppConstants.VIBRATE_ON_NEW_NOTIFICATION);
        vibrateOnNotifSwitch.setOnPreferenceChangeListener(this);

        if (parseUser != null) {
            boolean playSoundOnNewMessageNotifValue = parseUser.getBoolean(AppConstants.PLAY_SOUND_ON_NEW_MESAGE_NOTIF);
            playSoundOnNewMessageSwitch.setChecked(playSoundOnNewMessageNotifValue);

            boolean wakePhoneOnNotificationValue = parseUser.getBoolean(AppConstants.WAKE_PHONE_ON_NOTIFICATION);
            wakePhoneOnNotifSwitch.setChecked(wakePhoneOnNotificationValue);

            boolean showMessageTickerValue = parseUser.getBoolean(AppConstants.SHOW_MESSAGE_TICKER);
            showMessageTickerSwitch.setChecked(showMessageTickerValue);

            boolean vibrateOnNewNotificationsValue = parseUser.getBoolean(AppConstants.VIBRATE_ON_NEW_NOTIFICATION);
            vibrateOnNotifSwitch.setChecked(vibrateOnNewNotificationsValue);
        }

    }

    @Override
    public boolean onPreferenceChange(final Preference preference, final Object o) {
        if (parseUser != null) {
            parseUser.put(preference.getKey(), o);
            parseUser.saveInBackground();
        }
        return true;
    }

}
