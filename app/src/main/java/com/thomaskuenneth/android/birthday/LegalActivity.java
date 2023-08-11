/*
 * LegalActivity.java
 *
 * TKBirthdayReminder (c) Thomas Künneth 2009 - 2023
 * All rights reserved.
 */
package com.thomaskuenneth.android.birthday;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Objects;

public class LegalActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.legal);
        setSupportActionBar(findViewById(R.id.actionBar));
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
    }
}
