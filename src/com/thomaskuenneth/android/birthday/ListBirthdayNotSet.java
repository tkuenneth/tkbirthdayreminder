/**
 * ListBirthdayNotSet.java
 * 
 * TKBirthdayReminder (c) Thomas Künneth 2009
 * 
 * Alle Rechte beim Autoren. All rights reserved.
 */
package com.thomaskuenneth.android.birthday;

public class ListBirthdayNotSet extends AbstractBirthdayItemList {

	@Override
	protected boolean addToList(BirthdayItem item) {
		return (item.getBirthday() == null);
	}

	@Override
	public int compare(BirthdayItem item1, BirthdayItem item2) {
		if ((item1 == null) && (item2 == null)) {
			return 0;
		} else if (item1 == null) {
			return 1;
		} else if (item2 == null) {
			return -1;
		} else {
			return item1.getNameNotNull().compareTo(item2.getNameNotNull());
		}
	}
}
