/*
 * BootCompleteReceiver.java
 * 
 * TKBirthdayReminder (c) Thomas Künneth 2009 - 2017
 * Alle Rechte beim Autoren. All rights reserved.
 */
package com.thomaskuenneth.android.birthday;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.text.format.DateUtils;
import android.widget.Toast;

import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 * Diese Klasse sorgt dafür, dass am Ende des Bootvorgangs ein Alarm gesetzt
 * wird.
 *
 * @author Thomas Künneth
 * @see BroadcastReceiver
 */
public class BootCompleteReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        startAlarm(context.getApplicationContext(), false);
    }

    /**
     * Stellt den Alarm.
     *
     * @param context Kontext
     * @param nextDay legt fest, ob der Alarm auf den nächsten Tag verschoben wird,
     *                falls die Alarmzeit schon verstrichen ist
     */
    public static void startAlarm(Context context, boolean nextDay) {
        Intent intent = new Intent(context, AlarmReceiver.class);
        PendingIntent sender = PendingIntent
                .getBroadcast(context, 0, intent, 0);
        Calendar cal = Calendar.getInstance();
        int minCurrent = (cal.get(Calendar.HOUR_OF_DAY) * 60)
                + cal.get(Calendar.MINUTE);
        SharedPreferences prefs = TKBirthdayReminder.getSharedPreferences(context);
        int hour = prefs.getInt(AlarmPreference.NOTIFICATION_TIME_HOUR, 12);
        int minute = prefs.getInt(AlarmPreference.NOTIFICATION_TIME_MINUTE, 0);
        cal.set(GregorianCalendar.HOUR_OF_DAY, hour);
        cal.set(GregorianCalendar.MINUTE, minute);
        int minAlarm = (cal.get(Calendar.HOUR_OF_DAY) * 60)
                + cal.get(Calendar.MINUTE);
        if (nextDay) {
            if (minCurrent >= minAlarm) {
                cal.add(Calendar.DAY_OF_MONTH, 1);
            }
        }
        AlarmManager am = (AlarmManager) context
                .getSystemService(Service.ALARM_SERVICE);
        am.setRepeating(AlarmManager.RTC, cal.getTimeInMillis(),
                DateUtils.DAY_IN_MILLIS, sender);
        if (nextDay) {
            Toast toast = Toast.makeText(context, TKDateUtils
                            .getNotificationDateAsString(context, cal.getTime()),
                    Toast.LENGTH_LONG);
            toast.show();
        }
    }
}