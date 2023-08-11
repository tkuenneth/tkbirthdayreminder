/*
 * TKBirthdayReminderApplication.java
 *
 * TKBirthdayReminder (c) Thomas Künneth 2023
 * All rights reserved.
 */
package com.thomaskuenneth.android.birthday;

import android.app.Application;

import com.google.android.material.color.DynamicColors;

public class TKBirthdayReminderApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        DynamicColors.applyToActivitiesIfAvailable(this);
    }
}
