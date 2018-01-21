package com.wan.hollout.ui.fragments;

import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.SwitchPreference;

import com.github.machinarius.preferencefragment.PreferenceFragment;
import com.parse.ParseObject;
import com.wan.hollout.R;
import com.wan.hollout.interfaces.DoneCallback;
import com.wan.hollout.utils.AppConstants;
import com.wan.hollout.utils.AuthUtil;

import org.apache.commons.lang3.StringUtils;


/**
 * @author Wan Clem
 */

public class ChatSettingsFragment extends PreferenceFragment implements Preference.OnPreferenceChangeListener {

    private String[] messagesAndTextSize = new String[]{"12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", "30"};

    private ParseObject parseUser;
    ListPreference messagesAndTextSizePref;

    @Override
    public void onCreate(Bundle paramBundle) {
        super.onCreate(paramBundle);
        addPreferencesFromResource(R.xml.chat_settings);

        parseUser = AuthUtil.getCurrentUser();

        messagesAndTextSizePref = (ListPreference) findPreference(AppConstants.MESSAGES_TEXT_SIZE);
        messagesAndTextSizePref.setEntries(messagesAndTextSize);
        messagesAndTextSizePref.setEntryValues(messagesAndTextSize);
        messagesAndTextSizePref.setOnPreferenceChangeListener(this);

        SwitchPreference saveToGallerySwitchPref = (SwitchPreference) findPreference(AppConstants.SAVE_TO_GALLERY);
        saveToGallerySwitchPref.setOnPreferenceChangeListener(this);

        if (parseUser != null) {

            String messagesTextSize = parseUser.getString(AppConstants.MESSAGES_TEXT_SIZE);
            if (StringUtils.isNotEmpty(messagesTextSize)) {
                messagesAndTextSizePref.setValue(messagesTextSize);
                messagesAndTextSizePref.setSummary(messagesTextSize);
            }

            boolean saveToGallery = parseUser.getBoolean(AppConstants.SAVE_TO_GALLERY);
            saveToGallerySwitchPref.setChecked(saveToGallery);

        }

    }

    @Override
    public boolean onPreferenceChange(final Preference preference, final Object o) {
        String key = preference.getKey();
        if (key.equals(AppConstants.MESSAGES_TEXT_SIZE)) {
            messagesAndTextSizePref.setValue(o.toString());
            messagesAndTextSizePref.setSummary(o.toString());
        }
        if (parseUser != null) {
            parseUser.put(preference.getKey(), o);
            AuthUtil.updateCurrentLocalUser(parseUser, new DoneCallback<Boolean>() {
                @Override
                public void done(Boolean result, Exception e) {

                }
            });
        }
        return true;
    }
}
