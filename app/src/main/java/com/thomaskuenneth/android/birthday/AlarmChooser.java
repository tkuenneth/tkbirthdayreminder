/*
 * AlarmChooser.java
 *
 * TKBirthdayReminder (c) Thomas Künneth 2017 - 2020
 * Alle Rechte beim Autoren. All rights reserved.
 */
package com.thomaskuenneth.android.birthday;


import android.content.Context;
import android.content.SharedPreferences;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TimePicker;

public class AlarmChooser extends DialogPreference {

    static final String NOTIFICATION_TIME_HOUR = "notificationTimeHour";
    static final String NOTIFICATION_TIME_MINUTE = "notificationTimeMinute";

    private SharedPreferences prefs;
    private TimePicker picker;

    public AlarmChooser(Context context, AttributeSet attrs) {
        super(context, attrs);
        prefs = TKBirthdayReminder.getSharedPreferences(context);
        setDialogLayoutResource(R.layout.notification_time);
    }


    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);
        int hour = prefs.getInt(NOTIFICATION_TIME_HOUR, 12);
        int minute = prefs.getInt(NOTIFICATION_TIME_MINUTE, 0);
        picker = view.findViewById(R.id.notification_time_picker);
        picker.setHour(hour);
        picker.setMinute(minute);
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);
        if (positiveResult) {
            SharedPreferences.Editor editor = prefs.edit();
            editor.putInt(NOTIFICATION_TIME_HOUR, picker.getHour());
            editor.putInt(NOTIFICATION_TIME_MINUTE, picker.getMinute());
            editor.apply();
            BootCompleteReceiver.startAlarm(AlarmChooser.this.getContext(), true);
        }
    }
}
