/*
 * AlarmReceiver.java
 * 
 * TKBirthdayReminder (c) Thomas Künneth 2009 - 2017
 * Alle Rechte beim Autoren. All rights reserved.
 */
package com.thomaskuenneth.android.birthday;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.PowerManager;

import java.util.ArrayList;

/**
 * Diese Klasse tritt in Aktion, wenn ein Alarm ausgelöst wurde.
 *
 * @author Thomas Künneth
 * @see BroadcastReceiver
 */
public class AlarmReceiver extends BroadcastReceiver {

    private static final String TAG = AlarmReceiver.class.getSimpleName();

    @Override
    public void onReceive(final Context context, Intent intent) {
        BootCompleteReceiver.startAlarm(context, true);
        PowerManager pm = (PowerManager) context
                .getSystemService(Context.POWER_SERVICE);
        final PowerManager.WakeLock wl = pm.newWakeLock(
                PowerManager.PARTIAL_WAKE_LOCK, TAG);
        Runnable r = new Runnable() {
            public void run() {
                wl.acquire();
                ContactsList cl = new ContactsList(context);
                ArrayList<BirthdayItem> listNotifications = cl
                        .getListNotifications();
                int num = listNotifications.size();
                if (num > 0) {
                    Intent notificationIntent = new Intent(context,
                            NotificationView.class);
                    Bundle bundle = new Bundle();
                    bundle.putParcelableArrayList(Constants.LIST_NOTIFICATIONS,
                            listNotifications);
                    notificationIntent.putExtra(Constants.TKBR2, bundle);
                    PendingIntent contentIntent = PendingIntent.getActivity(
                            context, 0, notificationIntent, 0);
                    Notification.Builder builder = new Notification.Builder(context);
                    builder.setSmallIcon(R.drawable.birthdaycake_32)
                            .setWhen(System.currentTimeMillis())
                            .setContentText(context.getString(
                                    num == 1 ? R.string.alarmreceiver_message_1
                                            : R.string.alarmreceiver_message, num))
                            .setContentTitle(context.getString(R.string.alarmreceiver_tickertext))
                            .setContentIntent(contentIntent);
                    String current = AbstractListActivity
                            .getNotificationSoundAsString(context);
                    if (current != null) {
                        builder.setSound(Uri.parse(current));
                    }
                    NotificationManager nm = (NotificationManager) context
                            .getSystemService(Context.NOTIFICATION_SERVICE);
                    nm.notify(NotificationView.NOTIFICATION_ID, builder.getNotification());
                }
                wl.release();
            }
        };
        Thread t = new Thread(r);
        t.start();
    }
}
