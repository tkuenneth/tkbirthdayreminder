/*
 * PreferencesActivity.java
 *
 * TKBirthdayReminder (c) Thomas Künneth 2010 - 2020
 * Alle Rechte beim Autoren. All rights reserved.
 */
package com.thomaskuenneth.android.birthday;


import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

/**
 * Diese Activity fasst Einstellungen in einer Activity zusammen.
 *
 * @author Thomas Künneth
 */
public class PreferencesActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings);
        getSupportFragmentManager().beginTransaction().replace(R.id.settings,
                new PreferenceFragment()).commit();
    }
}
