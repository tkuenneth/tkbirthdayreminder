/*
 * AlarmChooser.java
 *
 * TKBirthdayReminder (c) Thomas Künneth 2017 - 2025
 * All rights reserved.
 */
package com.thomaskuenneth.android.birthday;


import android.app.AlarmManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.AttributeSet;

import androidx.preference.DialogPreference;

public class AlarmChooser extends DialogPreference {

    static final String KEY = "key_alarm_chooser";

    public AlarmChooser(Context context, AttributeSet attrs) {
        super(context, attrs);
        setDialogLayoutResource(R.layout.notification_time);
        setDialogTitle(R.string.notification_time);
        setTitle(R.string.notification_time);
        SharedPreferences prefs = TKBirthdayReminder.getSharedPreferences(context);
        boolean enabled = BootCompleteReceiver.canScheduleExactAlarms(context.getSystemService(AlarmManager.class));
        setEnabled(enabled);
        String summary = enabled ? prefs.getString(BootCompleteReceiver.KEY_NEXT_NOTIFICATION_TIME, null)
                : context.getString(R.string.alarms_are_off);
        setSummary(summary);
        setKey(KEY);
    }
}
