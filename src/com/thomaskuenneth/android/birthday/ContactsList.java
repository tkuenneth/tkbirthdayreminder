/**
 * ContactsList.java
 * 
 * TKBirthdayReminder (c) Thomas Künneth 2009
 * Alle Rechte beim Autoren. All rights reserved.
 */
package com.thomaskuenneth.android.birthday;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Hashtable;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.BaseColumns;
import android.provider.Contacts;
import android.provider.Contacts.People;
import android.util.Log;

/**
 * Diese Klasse liest Kontakte ein. Außerdem stellt sie die Listen zur
 * Verfügung, die in den entsprechenden Activities angezeigt werden.
 * 
 * @author Thomas Künneth
 * 
 */
public class ContactsList {

	/*
	 * Diese Listen können durch Aufruf des passenden Getters von den Activities
	 * verwendet werden.
	 */
	private final ArrayList<BirthdayItem> birthdaySet;
	private final ArrayList<BirthdayItem> birthdayNotSet;
	private final ArrayList<BirthdayItem> notifications;

	/*
	 * Wird verwendet, um Kontakte nicht mehrfach anzuzeigen.
	 */
	private final Hashtable<Long, Boolean> hashtableID;

	private final Context context;

	public ContactsList(Context context) {
		birthdaySet = new ArrayList<BirthdayItem>();
		birthdayNotSet = new ArrayList<BirthdayItem>();
		notifications = new ArrayList<BirthdayItem>();
		hashtableID = new Hashtable<Long, Boolean>();
		this.context = context;
		readContacts(context);
	}

	public ArrayList<BirthdayItem> getListBirthdaySet() {
		return birthdaySet;
	}

	public ArrayList<BirthdayItem> getListBirthdayNotSet() {
		return birthdayNotSet;
	}

	public ArrayList<BirthdayItem> getListNotifications() {
		return notifications;
	}

	/**
	 * Liest alle Kontakte aus der Datenbank und befüllt die drei Listen.
	 */
	private void readContacts(final Context context) {
		ContentResolver contentResolver = context.getContentResolver();
		Uri uri = Uri.parse("content://contacts/groups/system_id/"
				+ Contacts.Groups.GROUP_MY_CONTACTS + "/members");
		read(contentResolver, uri);
		// jetzt die eigene Gruppe
		uri = Uri.parse("content://contacts/groups/" + "name/"
				+ Uri.encode("TKBirthdayReminder") + "/members");
		uri = Contacts.People.CONTENT_URI;
		read(contentResolver, uri);
		Collections.sort(birthdaySet, new BirthdaySetComparator());
		Collections.sort(birthdayNotSet, new BirthdayNotSetComparator());
		Collections.sort(notifications, new NotificationsComparator());
	}

	private void read(ContentResolver contentResolver, Uri uri) {
		String[] projection = new String[] { People.DISPLAY_NAME, People.NOTES,
				People._ID, People.NUMBER };
		Cursor c = contentResolver.query(uri, projection, null, null, null);
		if (c != null) {
			while (c.moveToNext()) {
				String name = c.getString(0);
				String notes = c.getString(1);
				long id = c.getLong(2);
				String primaryPhoneNumber = c.getString(3);
				Long key = new Long(id);
				if (!hashtableID.containsKey(key)) {
					BirthdayItem item = new BirthdayItem(name, TKDateUtils
							.getDateFromString(notes), id, primaryPhoneNumber);
					if (addToListBirthdaySet(item)) {
						birthdaySet.add(item);
					}
					if (addToListBirthdayNotSet(item)) {
						birthdayNotSet.add(item);
					}
					if (addToListNotifications(item)) {
						notifications.add(item);
					}
					hashtableID.put(key, Boolean.TRUE);
				}
			}
			c.close();
		}
	}

	private boolean addToListBirthdaySet(BirthdayItem item) {
		return (item.getBirthday() != null);
	}

	private boolean addToListBirthdayNotSet(BirthdayItem item) {
		return (item.getBirthday() == null);
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

	public static long addGroup(Context context, String name) {
		long id = 0;
		ContentResolver cr = context.getContentResolver();
		// Gruppe schon vorhanden?
		Uri uri = Contacts.Groups.CONTENT_URI;
		Cursor c = cr.query(uri, new String[] { Contacts.GroupsColumns.NAME,
				BaseColumns._ID }, Contacts.GroupsColumns.NAME + " = ?",
				new String[] { name }, null);
		if ((c != null) && (c.moveToFirst())) {
			while (!c.isAfterLast()) {
				String groupName = c.getString(0);
				Log.d(ContactsList.class.getName(), groupName);
				id = c.getLong(1);
				c.moveToNext();
			}
		} else {
			// Gruppe anlegen
			ContentValues values = new ContentValues();
			values.put(Contacts.GroupsColumns.NAME, name);
			values.put(Contacts.GroupsColumns.SHOULD_SYNC, 0);
			uri = cr.insert(uri, values);
			// _ID ermitteln
			c = cr.query(uri, new String[] { BaseColumns._ID }, null, null,
					null);
			if ((c != null) && (c.moveToFirst())) {
				id = c.getLong(0);
			}
		}
		return id;
	}

	public static void createPerson(Context context, long groupId, String name,
			String notes) {
		ContentValues personInfo = new ContentValues();
		personInfo.put(Contacts.PeopleColumns.NAME, name);
		personInfo.put(Contacts.PeopleColumns.NOTES, notes);
		ContentResolver cr = context.getContentResolver();
		Uri newEntry = cr.insert(People.CONTENT_URI, personInfo);
		Cursor c = cr.query(newEntry, new String[] { BaseColumns._ID }, null,
				null, null);
		if ((c != null) && (c.moveToFirst())) {
			long personId = c.getLong(0);
			Contacts.People.addToGroup(cr, personId, groupId);
		}
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

	private static class BirthdayNotSetComparator implements
			Comparator<BirthdayItem> {

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

	private static class NotificationsComparator extends BirthdaySetComparator {
	}
}
