/*
 * BootCompleteReceiver.java
 *
 * TKBirthdayReminder (c) Thomas Künneth 2009 - 2022
 * All rights reserved.
 */
package com.thomaskuenneth.android.birthday;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.widget.Toast;

import java.util.Calendar;
import java.util.GregorianCalendar;

public class BootCompleteReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            startAlarm(context.getApplicationContext(), false);
        }
    }

    public static void startAlarm(Context context, boolean nextDay) {
        Intent intent = new Intent(context, AlarmReceiver.class);
        PendingIntent sender = PendingIntent
                .getBroadcast(context, 0, intent, PendingIntent.FLAG_IMMUTABLE);
        Calendar cal = Calendar.getInstance();
        int minCurrent = (cal.get(Calendar.HOUR_OF_DAY) * 60)
                + cal.get(Calendar.MINUTE);
        SharedPreferences prefs = TKBirthdayReminder.getSharedPreferences(context);
        int hour = prefs.getInt(AlarmChooserFragment.NOTIFICATION_TIME_HOUR, 12);
        int minute = prefs.getInt(AlarmChooserFragment.NOTIFICATION_TIME_MINUTE, 0);
        cal.set(GregorianCalendar.HOUR_OF_DAY, hour);
        cal.set(GregorianCalendar.MINUTE, minute);
        int minAlarm = (cal.get(Calendar.HOUR_OF_DAY) * 60)
                + cal.get(Calendar.MINUTE);
        if (nextDay) {
            if (minCurrent >= minAlarm) {
                cal.add(Calendar.DAY_OF_MONTH, 1);
            }
        }
        AlarmManager am = context.getSystemService(AlarmManager.class);
        if ((am != null) && canScheduleExactAlarms(am)) {
            am.setExactAndAllowWhileIdle(AlarmManager.RTC, cal.getTimeInMillis(), sender);
            if (nextDay) {
                Toast toast = Toast.makeText(context, Utils
                                .getNotificationDateAsString(context, cal.getTime()),
                        Toast.LENGTH_LONG);
                toast.show();
            }
        }
        TKBirthdayReminder.updateWidgets(context.getApplicationContext());
    }

    public static boolean canScheduleExactAlarms(AlarmManager am) {
        return Build.VERSION.SDK_INT <= Build.VERSION_CODES.R || am.canScheduleExactAlarms();
    }
}