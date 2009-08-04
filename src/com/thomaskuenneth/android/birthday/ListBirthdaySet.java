/**
 * ListBirthdaySet.java
 * 
 * TKBirthdayReminder (c) Thomas Künneth 2009
 * 
 * Alle Rechte beim Autoren. All rights reserved.
 */
package com.thomaskuenneth.android.birthday;

public class ListBirthdaySet extends AbstractBirthdayItemList {

	private static final long serialVersionUID = -9119410495440261488L;

	@Override
	protected boolean addToList(BirthdayItem item) {
		return (item.getBirthday() != null);
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
			int days1 = TKDateUtils.getBirthdayInDays(item1.getBirthday());
			if (days1 < -7) {
				days1 = 1000 + days1;
			}
			int days2 = TKDateUtils.getBirthdayInDays(item2.getBirthday());
			if (days2 < -7) {
				days2 = 1000 + days2;
			}
			if (days1 == days2) {
				return 0;
			} else {
				return (days1 < days2) ? -1 : 1;
			}
		}
	}
}
