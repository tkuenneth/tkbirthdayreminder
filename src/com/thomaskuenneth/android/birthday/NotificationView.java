/**
 * NotificationView.java
 * 
 * TKBirthdayReminder (c) Thomas Künneth 2009
 * Alle Rechte beim Autoren. All rights reserved.
 */
package com.thomaskuenneth.android.birthday;

import java.util.ArrayList;

import android.app.NotificationManager;
import android.os.Bundle;

/**
 * Diese Activity zeigt eine Liste mit baldigen Geburtstagen an.
 * 
 * @author Thomas Künneth
 * @see AbstractListActivity
 */
public class NotificationView extends AbstractListActivity {

	/**
	 * ID für die Notification.
	 */
	public static final int NOTIFICATION_ID = 231167;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		nm.cancel(NOTIFICATION_ID);
		setListFromBundle(Constants.LIST_NOTIFICATIONS);
	}

	@Override
	protected ArrayList<BirthdayItem> getProperList(ContactsList cl) {
		return cl.getListNotifications();
	}
}
