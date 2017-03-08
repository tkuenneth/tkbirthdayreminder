/*
 * PreferencesActivity.java
 * 
 * TKBirthdayReminder (c) Thomas Künneth 2010 - 2017
 * Alle Rechte beim Autoren. All rights reserved.
 */
package com.thomaskuenneth.android.birthday;


import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;

/**
 * Diese Activity fasst Einstellungen in einer Activity zusammen.
 *
 * @author Thomas Künneth
 */
public class PreferencesActivity extends PreferenceActivity {

    private SharedPreferences.OnSharedPreferenceChangeListener l;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
        l = new SharedPreferences.OnSharedPreferenceChangeListener() {

            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
                if ("checkbox_show_astrological_signs".equals(key)) {
                    TKBirthdayReminder.updateWidgets(PreferencesActivity.this);
                }
            }
        };
    }

    @Override
    protected void onResume() {
        super.onResume();
        PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(l);
    }

    @Override
    protected void onPause() {
        super.onPause();
        PreferenceManager.getDefaultSharedPreferences(this).unregisterOnSharedPreferenceChangeListener(l);
    }
}
