/**
 * AlarmReceiver.java
 * 
 * TKBirthdayReminder (c) Thomas Künneth 2009
 * 
 * Alle Rechte beim Autoren. All rights reserved.
 */
package com.thomaskuenneth.android.birthday;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Diese Klasse tritt in Aktion, wenn ein Alarm ausgelöst wurde.
 * 
 * @author Thomas Künneth
 * 
 */
public class AlarmReceiver extends BroadcastReceiver {

	@Override
	public void onReceive(final Context context, Intent intent) {
		Runnable r = new Runnable() {
			public void run() {
				int num = ContactsList.getListNotifications().size();
				if (num > 0) {
					NotificationManager nm = (NotificationManager) context
							.getSystemService(Context.NOTIFICATION_SERVICE);
					PendingIntent contentIntent = PendingIntent.getActivity(
							context, 0, new Intent(context,
									NotificationView.class), 0);
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
					nm.notify(NotificationView.NOTIFICATION_ID, notif);
				}
			}
		};
		TKBirthdayReminder.instance = context;
		ContactsList.readContacts(context, r);
	}
}
