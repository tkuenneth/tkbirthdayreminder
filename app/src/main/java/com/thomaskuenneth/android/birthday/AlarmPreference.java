/*
 * AlarmPreference.java
 *
 * TKBirthdayReminder (c) Thomas Künneth 2017
 * Alle Rechte beim Autoren. All rights reserved.
 */
package com.thomaskuenneth.android.birthday;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TimePicker;

public class AlarmPreference extends DialogPreference {

    static final String NOTIFICATION_TIME_HOUR = "notificationTimeHour";
    static final String NOTIFICATION_TIME_MINUTE = "notificationTimeMinute";

    private final SharedPreferences prefs;
    private final Context context;

    private TimePicker picker;

    public AlarmPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        prefs = TKBirthdayReminder.getSharedPreferences(context);
        this.context = context;
        setDialogLayoutResource(R.layout.notification_time);
    }

    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);
        picker = (TimePicker) view.findViewById(R.id.notification_time);
        int hour = prefs.getInt(NOTIFICATION_TIME_HOUR, 12);
        picker.setCurrentHour(hour);
        int minute = prefs.getInt(NOTIFICATION_TIME_MINUTE, 0);
        picker.setCurrentMinute(minute);
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);
        if (positiveResult) {
            SharedPreferences.Editor editor = prefs.edit();
            editor.putInt(NOTIFICATION_TIME_HOUR, picker.getCurrentHour());
            editor.putInt(NOTIFICATION_TIME_MINUTE, picker.getCurrentMinute());
            editor.apply();
            BootCompleteReceiver.startAlarm(context, true);
        }
    }
}
