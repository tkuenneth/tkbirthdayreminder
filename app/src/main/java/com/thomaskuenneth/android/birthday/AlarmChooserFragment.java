/*
 * AlarmChooserFragment.java
 *
 * TKBirthdayReminder (c) Thomas Künneth 2020 - 2022
 * All rights reserved.
 */
package com.thomaskuenneth.android.birthday;

import android.content.Context;
import android.content.SharedPreferences;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.TimePicker;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceDialogFragmentCompat;

public class AlarmChooserFragment extends PreferenceDialogFragmentCompat {

    static final String NOTIFICATION_TIME_HOUR = "notificationTimeHour";
    static final String NOTIFICATION_TIME_MINUTE = "notificationTimeMinute";

    private SharedPreferences prefs;
    private TimePicker picker;

    @Override
    protected void onBindDialogView(@NonNull View view) {
        super.onBindDialogView(view);
        picker = view.findViewById(R.id.notification_time_picker);
        Context context = getContext();
        if (context != null) {
            prefs = TKBirthdayReminder.getSharedPreferences(context);
            int hour = prefs.getInt(NOTIFICATION_TIME_HOUR, 12);
            int minute = prefs.getInt(NOTIFICATION_TIME_MINUTE, 0);
            picker.setHour(hour);
            picker.setMinute(minute);
            picker.setIs24HourView(DateFormat.is24HourFormat(context));
        }
    }

    @Override
    public void onDialogClosed(boolean positiveResult) {
        if (positiveResult) {
            SharedPreferences.Editor editor = prefs.edit();
            editor.putInt(NOTIFICATION_TIME_HOUR, picker.getHour());
            editor.putInt(NOTIFICATION_TIME_MINUTE, picker.getMinute());
            editor.apply();
            BootCompleteReceiver.startAlarm(getContext(), true);
        }
    }
}
