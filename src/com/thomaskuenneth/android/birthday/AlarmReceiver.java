/**
 * AlarmReceiver.java
 * 
 * TKBirthdayReminder (c) Thomas Künneth 2009
 * Alle Rechte beim Autoren. All rights reserved.
 */
package com.thomaskuenneth.android.birthday;

import java.util.ArrayList;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

/**
 * Diese Klasse tritt in Aktion, wenn ein Alarm ausgelöst wurde.
 * 
 * @author Thomas Künneth
 * @see BroadcastReceiver
 */
public class AlarmReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(final Context context, Intent intent) {
		BootCompleteReceiver.startAlarm(context, true);
		Runnable r = new Runnable() {
			public void run() {
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
					Notification notif = new Notification(
							R.drawable.birthdaycake_32,
							context
									.getString(R.string.alarmreceiver_tickertext),
							System.currentTimeMillis());
					notif.setLatestEventInfo(context, context
							.getString(R.string.app_name), context.getString(
							num == 1 ? R.string.alarmreceiver_message_1
									: R.string.alarmreceiver_message, num),
							contentIntent);
					String current = AbstractListActivity
							.getNotificationSoundAsString(context);
					if (current != null) {
						notif.sound = Uri.parse(current);
					}
					NotificationManager nm = (NotificationManager) context
							.getSystemService(Context.NOTIFICATION_SERVICE);
					nm.notify(NotificationView.NOTIFICATION_ID, notif);
				}
			}
		};
		new Thread(r).start();
	}
}
