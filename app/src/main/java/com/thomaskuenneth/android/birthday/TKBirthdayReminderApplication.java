/*
 * TKBirthdayReminderApplication.java
 *
 * TKBirthdayReminder (c) Thomas Künneth 2023 - 2025
 * All rights reserved.
 */
package com.thomaskuenneth.android.birthday;

import android.app.Application;

import androidx.work.Constraints;
import androidx.work.ExistingPeriodicWorkPolicy;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import com.google.android.material.color.DynamicColors;

import java.util.concurrent.TimeUnit;

public class TKBirthdayReminderApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        DynamicColors.applyToActivitiesIfAvailable(this);
        schedulePeriodicWork();
    }

    private void schedulePeriodicWork() {
        var constraints = new Constraints.Builder().build();
        PeriodicWorkRequest periodicWorkRequest = new PeriodicWorkRequest.Builder(
                PeriodicWorker.class, 4, TimeUnit.HOURS)
                .setConstraints(constraints).build();
        WorkManager.getInstance(this).enqueueUniquePeriodicWork(
                PeriodicWorker.class.getSimpleName(),
                ExistingPeriodicWorkPolicy.UPDATE,
                periodicWorkRequest);
    }
}
