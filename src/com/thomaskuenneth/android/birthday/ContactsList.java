/*
 * ContactsList.java
 * 
 * TKBirthdayReminder (c) Thomas Künneth 2009 - 2012
 * Alle Rechte beim Autoren. All rights reserved.
 */
package com.thomaskuenneth.android.birthday;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract;

/**
 * Diese Klasse liest Kontakte ein. Außerdem stellt sie die Listen zur
 * Verfügung, die in den entsprechenden Activities angezeigt werden.
 * 
 * @author Thomas Künneth
 * 
 */
public class ContactsList {

//	private static final String TAG = ContactsList.class.getSimpleName();

	/*
	 * Diese Listen können durch Aufruf des passenden Getters von den Activities
	 * verwendet werden.
	 */
	private final ArrayList<BirthdayItem> birthdaySet;
	private final ArrayList<BirthdayItem> notifications;

	private final Context context;

	public ContactsList(Context context) {
		birthdaySet = new ArrayList<BirthdayItem>();
		notifications = new ArrayList<BirthdayItem>();
		this.context = context;
		readContacts(context);
	}

	public ArrayList<BirthdayItem> getListBirthdaySet() {
		return birthdaySet;
	}

	public ArrayList<BirthdayItem> getListNotifications() {
		return notifications;
	}

	/**
	 * Liest alle Kontakte aus der Datenbank und befüllt die zwei Listen.
	 */
	private void readContacts(final Context context) {
		ContentResolver contentResolver = context.getContentResolver();
		queryContacts(contentResolver);
		Collections.sort(birthdaySet, new BirthdaySetComparator());
		Collections.sort(notifications, new NotificationsComparator());
	}

	private void queryContacts(ContentResolver contentResolver) {
		// IDs und Namen aller Kontakte ermitteln
		String[] mainQueryProjection = { ContactsContract.Contacts._ID,
				ContactsContract.Contacts.DISPLAY_NAME };
		Cursor mainQueryCursor = contentResolver.query(
				ContactsContract.Contacts.CONTENT_URI, mainQueryProjection,
				null, null, null);
		// Trefferliste abarbeiten...
		if (mainQueryCursor != null) {
			while (mainQueryCursor.moveToNext()) {
				BirthdayItem item = createItemFromCursor(contentResolver,
						mainQueryCursor);
				if (addToListBirthdaySet(item)) {
					birthdaySet.add(item);
				}
				if (addToListNotifications(item)) {
					notifications.add(item);
				}
			}
			mainQueryCursor.close();
		}
	}

	public static BirthdayItem createItemFromCursor(
			ContentResolver contentResolver, Cursor mainQueryCursor) {
		String contactId = mainQueryCursor.getString(mainQueryCursor
				.getColumnIndex(ContactsContract.Contacts._ID));
		String displayName = mainQueryCursor.getString(mainQueryCursor
				.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
//		Log.d(TAG, "===> " + displayName + " (" + contactId + ")");
		// Telefonnummer, Geburtsdatum und ggf. Notizen lesen
		String phoneNumber = null;
		Date gebdt = null;
		String[] dataQueryProjection = new String[] {
				ContactsContract.Data.MIMETYPE,
				ContactsContract.CommonDataKinds.Event.TYPE,
				ContactsContract.CommonDataKinds.Event.START_DATE,
				ContactsContract.CommonDataKinds.Note.NOTE,
				ContactsContract.CommonDataKinds.Phone.NUMBER,
				ContactsContract.CommonDataKinds.Phone.TYPE };
		String dataQuerySelection = ContactsContract.Data.CONTACT_ID + " = ?";
		String[] dataQuerySelectionArgs = new String[] { contactId };
		Cursor dataQueryCursor = contentResolver.query(
				ContactsContract.Data.CONTENT_URI, dataQueryProjection,
				dataQuerySelection, dataQuerySelectionArgs, null);
		if (dataQueryCursor != null) {
			while (dataQueryCursor.moveToNext()) {
				String mimeType = dataQueryCursor.getString(0);
				if (ContactsContract.CommonDataKinds.Event.CONTENT_ITEM_TYPE
						.equals(mimeType)) {
					int type = dataQueryCursor.getInt(1);
					if (ContactsContract.CommonDataKinds.Event.TYPE_BIRTHDAY == type) {
						String stringBirthday = dataQueryCursor.getString(2);
//						Log.d(TAG, "     birthday date: " + stringBirthday);
						Date d = TKDateUtils.getDateFromString1(stringBirthday);
						if (d != null) {
							gebdt = d;
						}
					}
				} else if (ContactsContract.CommonDataKinds.Note.CONTENT_ITEM_TYPE
						.equals(mimeType)) {
					String note = dataQueryCursor.getString(3);
//					Log.d(TAG, "     note: " + note);
					Date d = TKDateUtils.getDateFromString(note);
					if (d != null) {
						gebdt = d;
					}
				} else if (ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE
						.equals(mimeType)) {
					if (ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE == dataQueryCursor
							.getInt(5)) {
						phoneNumber = dataQueryCursor.getString(4);
//						Log.d(TAG, "     phone: " + phoneNumber);
					}
				}
			}
			// FIXME: führt offenbar zu ANRs
			dataQueryCursor.close();
		}
		// jetzt Objekt erzeugen und in Listen einfügen
		BirthdayItem item = new BirthdayItem(displayName, gebdt, 
				Long.valueOf(contactId), phoneNumber);
		return item;
	}

	private boolean addToListBirthdaySet(BirthdayItem item) {
		return (item.getBirthday() != null);
	}

	private boolean addToListNotifications(BirthdayItem item) {
		Date birthday = item.getBirthday();
		if (birthday != null) {
			int daysUntilBirthday = TKDateUtils.getBirthdayInDays(birthday);
			if (daysUntilBirthday == 0) {
				return true;
			} else if ((daysUntilBirthday < 0) || (daysUntilBirthday > 7)) {
				return false;
			}
			int notificationDays = AbstractListActivity
					.getNotificationDays(context);
			int mask = 1 << (daysUntilBirthday - 1);
			if ((notificationDays & mask) == mask) {
				return true;
			}
		}
		return false;
	}

	private static class BirthdaySetComparator implements
			Comparator<BirthdayItem> {

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

	private static class NotificationsComparator extends BirthdaySetComparator {
	}
}
