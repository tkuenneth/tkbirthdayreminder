/*
 * PreferenceFragment.java
 *
 * TKBirthdayReminder (c) Thomas Künneth 2020 - 2023
 * All rights reserved.
 */
package com.thomaskuenneth.android.birthday;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;

import androidx.annotation.RequiresApi;
import androidx.preference.Preference;
import androidx.preference.PreferenceDialogFragmentCompat;
import androidx.preference.PreferenceFragmentCompat;

public class PreferenceFragment extends PreferenceFragmentCompat {

    static final String CHECKBOX_SHOW_ASTROLOGICAL_SIGNS = "checkbox_show_astrological_signs";

    private final SharedPreferences.OnSharedPreferenceChangeListener l = (sharedPreferences, key) -> {
        if (CHECKBOX_SHOW_ASTROLOGICAL_SIGNS.equals(key)) {
            TKBirthdayReminder.updateWidgets(getActivity());
        }
    };

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        Context context = getContext();
        if (context != null) {
            AlarmReceiver.initChannels(context);
            setPreferencesFromResource(R.xml.preferences, null);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                Preference alarm_chooser = findPreference("alarm_chooser");
                if (alarm_chooser != null) {
                    alarm_chooser.setVisible(false);
                }
                Preference p = new Preference(context);
                p.setTitle(R.string.notification_settings);
                p.setKey("notification_channel_settings");
                p.setIntent(createNotificationSettingsIntent(requireContext()));
                getPreferenceScreen().addPreference(p);
            } else {
                Preference p = new Preference(context);
                p.setTitle(R.string.notification_sound);
                p.setKey("alarm_chooser");
                Intent intent = new Intent(context, SoundChooser.class);
                p.setIntent(intent);
                getPreferenceScreen().addPreference(p);
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        PreferenceManager.getDefaultSharedPreferences(getActivity()).registerOnSharedPreferenceChangeListener(l);
    }

    @Override
    public void onPause() {
        super.onPause();
        PreferenceManager.getDefaultSharedPreferences(getActivity()).unregisterOnSharedPreferenceChangeListener(l);
    }

    @Override
    public void onDisplayPreferenceDialog(Preference preference) {
        String key = preference.getKey();
        if (WidgetPreference.KEY.equals(key)) {
            showFragment(new WidgetPreferenceFragment(), key);
        } else if (NotificationPreference.KEY.equals(key)) {
            showFragment(new NotificationPreferenceFragment(), key);
        } else if (AlarmChooser.KEY.equals(key)) {
            showFragment(new AlarmChooserFragment(), key);
        } else {
            super.onDisplayPreferenceDialog(preference);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    public static Intent createNotificationSettingsIntent(Context context) {
        Intent intent = new Intent(Settings.ACTION_APP_NOTIFICATION_SETTINGS);
        intent.putExtra(Settings.EXTRA_APP_PACKAGE, context.getPackageName());
        return intent;
    }

    private void showFragment(PreferenceDialogFragmentCompat fragment, String key) {
        fragment.setTargetFragment(this, 0);
        final Bundle bundle = new Bundle(1);
        bundle.putString("key", key);
        fragment.setArguments(bundle);
        fragment.show(getParentFragmentManager(), key);
    }
}
