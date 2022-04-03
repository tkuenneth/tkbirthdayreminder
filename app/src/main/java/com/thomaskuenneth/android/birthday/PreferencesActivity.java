/*
 * PreferencesActivity.java
 *
 * TKBirthdayReminder (c) Thomas Künneth 2010 - 2022
 * All rights reserved.
 */
package com.thomaskuenneth.android.birthday;

import android.os.Bundle;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;

public class PreferencesActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings);
        getSupportFragmentManager().beginTransaction().replace(R.id.settings,
                new PreferenceFragment()).commit();
        ActionBar ab = getSupportActionBar();
        if (ab != null) {
            ab.setDisplayShowHomeEnabled(true);
        }
    }
}
