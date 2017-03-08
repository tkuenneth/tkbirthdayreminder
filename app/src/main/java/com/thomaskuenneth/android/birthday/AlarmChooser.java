/*
 * AlarmChooser.java
 *
 * TKBirthdayReminder (c) Thomas Künneth 2017
 * Alle Rechte beim Autoren. All rights reserved.
 */
package com.thomaskuenneth.android.birthday;


import android.app.Activity;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.widget.TimePicker;

public class AlarmChooser extends Activity {

    static final String NOTIFICATION_TIME_HOUR = "notificationTimeHour";
    static final String NOTIFICATION_TIME_MINUTE = "notificationTimeMinute";

    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        prefs = TKBirthdayReminder.getSharedPreferences(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        int hour = prefs.getInt(NOTIFICATION_TIME_HOUR, 12);
        int minute = prefs.getInt(NOTIFICATION_TIME_MINUTE, 0);
        TimePickerDialog picker = new TimePickerDialog(this,
                new TimePickerDialog.OnTimeSetListener() {

                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putInt(NOTIFICATION_TIME_HOUR, hourOfDay);
                        editor.putInt(NOTIFICATION_TIME_MINUTE, minute);
                        editor.apply();
                        BootCompleteReceiver.startAlarm(AlarmChooser.this, true);
                        finish();
                    }
                }, hour, minute, DateFormat.is24HourFormat(this));
        picker.setOnDismissListener(new DialogInterface.OnDismissListener() {

            @Override
            public void onDismiss(DialogInterface dialog) {
                finish();
            }
        });
        picker.show();
    }
}
