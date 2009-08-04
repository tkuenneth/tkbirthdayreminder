/**
 * ContactsList.java
 * 
 * TKBirthdayReminder (c) Thomas Künneth 2009
 * 
 * Alle Rechte beim Autoren. All rights reserved.
 */
package com.thomaskuenneth.android.birthday;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.provider.BaseColumns;
import android.provider.Contacts;
import android.provider.Contacts.People;
import android.util.Log;

public class ContactsList {

	/*
	 * Diese Listen werden von den Activities verwendet.
	 */
	private static final List<BirthdayItem> birthdaySet = new ArrayList<BirthdayItem>();
	private static final List<BirthdayItem> birthdayNotSet = new ArrayList<BirthdayItem>();
	private static final List<BirthdayItem> notifications = new ArrayList<BirthdayItem>();

	private static final List<AbstractListActivity> activities = new ArrayList<AbstractListActivity>();

	private static Thread current = null;
	private static final List<Runnable> runnables = new ArrayList<Runnable>();

	public static List<BirthdayItem> getListBirthdaySet() {
		return birthdaySet;
	}

	public static List<BirthdayItem> getListBirthdayNotSet() {
		return birthdayNotSet;
	}

	public static List<BirthdayItem> getListNotifications() {
		return notifications;
	}

	public static synchronized void addListener(AbstractListActivity activity) {
		activities.add(activity);
	}

	public static synchronized void removeListener(AbstractListActivity activity) {
		activities.remove(activity);
	}

	/**
	 * Liest alle Kontakte aus der Datenbank und befüllt die drei Listen.
	 */
	public static void readContacts(final Context context, final Runnable r) {
		new Thread(new Runnable() {

			@Override
			public void run() {
				_readContacts(context, r);
			}

		}).start();
	}

	private static synchronized void _readContacts(final Context context,
			final Runnable r) {
		if (r != null) {
			runnables.add(r);
		}
		if (current == null) {
			current = new Thread(new Runnable() {

				@Override
				public void run() {
					AbstractBirthdayItemList _birthdaySet = new ListBirthdaySet();
					AbstractBirthdayItemList _birthdayNotSet = new ListBirthdayNotSet();
					AbstractBirthdayItemList _notifications = new ListNotifications();
					Hashtable<Long, Boolean> hashtableID = new Hashtable<Long, Boolean>();

					ContentResolver contentResolver = context
							.getContentResolver();
					Uri uri = Uri.parse("content://contacts/groups/system_id/"
							+ Contacts.Groups.GROUP_MY_CONTACTS + "/members");
					readContacts(contentResolver, uri, _birthdaySet,
							_birthdayNotSet, _notifications, hashtableID);
					// jetzt die eigene Gruppe
					uri = Uri.parse("content://contacts/groups/" + "name/"
							+ Uri.encode("TKBirthdayReminder") + "/members");
					readContacts(contentResolver, uri, _birthdaySet,
							_birthdayNotSet, _notifications, hashtableID);
					Collections.sort(_birthdaySet, _birthdaySet);
					Collections.sort(_birthdayNotSet, _birthdayNotSet);
					Collections.sort(_notifications, _notifications);
					birthdaySet.clear();
					birthdaySet.addAll(_birthdaySet);
					birthdayNotSet.clear();
					birthdayNotSet.addAll(_birthdayNotSet);
					notifications.clear();
					notifications.addAll(_notifications);
					// die Activities werden im UI thread aktualisiert
					for (final AbstractListActivity activity : activities) {
						activity.runOnUiThread(new Runnable() {

							@Override
							public void run() {
								activity.notifyDataSetChanged();
							}

						});
					}
					synchronized (runnables) {
						Looper.prepare();
						Handler h = new Handler(Looper.getMainLooper());
						List<Runnable> delete = new ArrayList<Runnable>();
						for (Runnable r : runnables) {
							h.post(r);
							delete.add(r);
						}
						runnables.removeAll(delete);
					}
					current = null;
				}
			});
			current.start();
		}
	}

	private static void readContacts(ContentResolver contentResolver, Uri uri,
			AbstractBirthdayItemList birthdaySet,
			AbstractBirthdayItemList birthdayNotSet,
			AbstractBirthdayItemList notifications,
			Hashtable<Long, Boolean> hashtableID) {
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
					if (birthdaySet.addToList(item)) {
						birthdaySet.add(item);
					}
					if (birthdayNotSet.addToList(item)) {
						birthdayNotSet.add(item);
					}
					if (notifications.addToList(item)) {
						notifications.add(item);
					}
					hashtableID.put(key, Boolean.TRUE);
				}
			}
			c.close();
		}
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
}
