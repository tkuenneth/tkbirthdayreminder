/*
 * PreferencesActivity.java
 *
 * TKBirthdayReminder (c) Thomas Künneth 2010 - 2023
 * All rights reserved.
 */
package com.thomaskuenneth.android.birthday;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Objects;

public class PreferencesActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        EdgeToEdge.enable(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings);
        Utils.configureActionBar(this);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportFragmentManager().beginTransaction().replace(R.id.settings,
                new PreferenceFragment()).commit();
    }
}
