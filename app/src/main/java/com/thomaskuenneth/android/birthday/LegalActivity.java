/*
 * LegalActivity.java
 *
 * TKBirthdayReminder (c) Thomas Künneth 2009 - 2023
 * All rights reserved.
 */
package com.thomaskuenneth.android.birthday;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.util.Locale;
import java.util.Objects;

public class LegalActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        EdgeToEdge.enable(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.legal);
        Utils.configureActionBar(this);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);

        PackageManager pm = getPackageManager();
        String version = getString(R.string.unknown);
        try {
            String packageName = getPackageName();
            if (packageName != null) {
                String versionCode = "";
                PackageInfo packageInfo = pm.getPackageInfo(packageName, 0);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    versionCode = String.format(Locale.US, "%d", packageInfo.getLongVersionCode());
                } else {
                    versionCode = String.format(Locale.US, "%d", packageInfo.versionCode);
                }
                version = String.format("%s (%s)", packageInfo.versionName, versionCode);
            }
        } catch (PackageManager.NameNotFoundException ignored) {
        }
        TextView tv = findViewById(R.id.about_version);
        tv.setText(version);
    }
}
