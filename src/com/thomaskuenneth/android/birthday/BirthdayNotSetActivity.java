/**
 * BirthdayNotSetActivity.java
 * 
 * TKBirthdayReminder (c) Thomas Künneth 2009
 * 
 * Alle Rechte beim Autoren. All rights reserved.
 */
package com.thomaskuenneth.android.birthday;

public class BirthdayNotSetActivity extends AbstractListActivity {

	public BirthdayNotSetActivity() {
		super(ContactsList.getListBirthdayNotSet());
	}

}
