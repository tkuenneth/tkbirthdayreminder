/**
 * NotificationView.java
 * 
 * TKBirthdayReminder (c) Thomas Künneth 2009
 * 
 * Alle Rechte beim Autoren. All rights reserved.
 */
package com.thomaskuenneth.android.birthday;

import android.app.NotificationManager;
import android.os.Bundle;

public class NotificationView extends AbstractListActivity {

	/**
	 * ID für die Notification.
	 */
	public static final int NOTIFICATION_ID = 231167;

	public NotificationView() {
		super(ContactsList.getListNotifications());
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		nm.cancel(NOTIFICATION_ID);
	}

	public static boolean checkItem(BirthdayItem item) {
		if (item.getBirthday() != null) {
			int days = TKDateUtils.getBirthdayInDays(item.getBirthday());
			if (days == 0) {
				return true;
			}
			int notificationDays = TKBirthdayReminder.getNotificationDays();
			for (int bit = 0; bit < 7; bit++) {
				int mask = 1 << bit;
				if ((notificationDays & mask) == mask) {
					if (days == (1 + bit)) {
						return true;
					}
				}
			}
		}
		return false;
	}

}
