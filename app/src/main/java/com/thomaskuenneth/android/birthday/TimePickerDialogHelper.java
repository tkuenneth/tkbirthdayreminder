/*
 * TimePickerDialogHelper.java
 * 
 * TKBirthdayReminder (c) Thomas Künneth 2009 - 2017
 * 
 * Alle Rechte beim Autoren. All rights reserved.
 */
package com.thomaskuenneth.android.birthday;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

/**
 * Diese Klasse hilft beim Datenaustausch zwischen den Preferences und dem
 * TimePickerDialog.
 *
 * @author Thomas Künneth
 */
class TimePickerDialogHelper {

    private static final String NOTIFICATION_TIME_HOUR = "notificationTimeHour";
    private static final String NOTIFICATION_TIME_MINUTE = "notificationTimeMinute";

    /**
     * Stunden (0 - 23)
     */
    static int hour;

    /**
     * Minuten (0 - 59)
     */
    static int minute;

    static void readFromPreferences(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(
                Constants.SHARED_PREFS_KEY, Context.MODE_PRIVATE);
        hour = prefs.getInt(NOTIFICATION_TIME_HOUR, 12);
        minute = prefs.getInt(NOTIFICATION_TIME_MINUTE, 0);
    }

    static void writeToPreferences(Context context, int hour, int minute) {
        TimePickerDialogHelper.hour = hour;
        TimePickerDialogHelper.minute = minute;
        SharedPreferences prefs = context.getSharedPreferences(
                Constants.SHARED_PREFS_KEY, Context.MODE_PRIVATE);
        Editor editor = prefs.edit();
        editor.putInt(NOTIFICATION_TIME_HOUR, hour);
        editor.putInt(NOTIFICATION_TIME_MINUTE, minute);
        editor.apply();
    }
}
