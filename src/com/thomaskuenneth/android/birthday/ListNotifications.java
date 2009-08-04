/**
 * ListNotifications.java
 * 
 * TKBirthdayReminder (c) Thomas Künneth 2009
 * 
 * Alle Rechte beim Autoren. All rights reserved.
 */
package com.thomaskuenneth.android.birthday;

public class ListNotifications extends ListBirthdaySet {

	@Override
	protected boolean addToList(BirthdayItem item) {
		return NotificationView.checkItem(item);
	}

}
