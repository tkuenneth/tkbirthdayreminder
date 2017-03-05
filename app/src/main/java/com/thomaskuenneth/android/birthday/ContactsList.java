/*
 * ContactsList.java
 * 
 * TKBirthdayReminder (c) Thomas Künneth 2009 - 2017
 * Alle Rechte beim Autoren. All rights reserved.
 */
package com.thomaskuenneth.android.birthday;

import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.Hashtable;

/**
 * Diese Klasse liest Kontakte ein. Außerdem stellt sie die Listen zur
 * Verfügung, die in den entsprechenden Activities angezeigt werden.
 *
 * @author Thomas Künneth
 */
class ContactsList {

    private static final String TAG = ContactsList.class.getSimpleName();

    /*
     * Diese Listen können durch Aufruf des passenden Getters von den Activities
     * verwendet werden.
     */
    private final ArrayList<BirthdayItem> birthdaySet;
    private final ArrayList<BirthdayItem> notifications;

    private final Context context;

    ContactsList(Context context) {
        birthdaySet = new ArrayList<>();
        notifications = new ArrayList<>();
        this.context = context;
        readContacts(context);
    }

    ArrayList<BirthdayItem> getListBirthdaySet() {
        return birthdaySet;
    }

    ArrayList<BirthdayItem> getListNotifications() {
        return notifications;
    }

    private void readContacts(final Context context) {
        SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(context);
        boolean hidePastsBirthdays = prefs.getBoolean("hide_past_birthdays",
                false);
        ContentResolver contentResolver = context.getContentResolver();
        queryContacts(contentResolver, hidePastsBirthdays);
        Collections.sort(birthdaySet, new BirthdaySetComparator());
        Collections.sort(notifications, new NotificationsComparator());
    }

    private void queryContacts(ContentResolver contentResolver,
                               boolean hidePastsBirthdays) {
        final int intNowAsMMDD = Integer.parseInt(TKDateUtils.FORMAT_YYYYMMDD.format(
                new Date()).substring(4, 8));
        Hashtable<String, Boolean> ht = new Hashtable<>();
        String[] mainQueryProjection = {ContactsContract.Contacts._ID,
                ContactsContract.Contacts.DISPLAY_NAME};
        Cursor mainQueryCursor = null;
        try {
            mainQueryCursor = contentResolver.query(
                    ContactsContract.Contacts.CONTENT_URI, mainQueryProjection,
                    null, null, null);
        } catch (SecurityException e) {
            Log.e(TAG, "queryContacts() - missing required permissions");
        }
        if (mainQueryCursor != null) {
            while (mainQueryCursor.moveToNext()) {
                BirthdayItem item = createItemFromCursor(contentResolver,
                        mainQueryCursor);
                String name = item.getName();
                Date date = item.getBirthday();
                if ((name != null) && (date != null)) {
                    String strYYYYMMDD = TKDateUtils.FORMAT_YYYYMMDD
                            .format(date);
                    String key = name + strYYYYMMDD;
                    if (ht.containsKey(key)) {
                        continue;
                    } else {
                        if (hidePastsBirthdays) {
                            int intBirthdayAsMMDD = Integer
                                    .parseInt(strYYYYMMDD.substring(4, 8));
                            if (intBirthdayAsMMDD < intNowAsMMDD) {
                                continue;
                            }
                        }
                        ht.put(key, Boolean.TRUE);
                    }
                }
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

    static BirthdayItem createItemFromCursor(
            ContentResolver contentResolver, Cursor mainQueryCursor) {
        String contactId = mainQueryCursor.getString(mainQueryCursor
                .getColumnIndex(ContactsContract.Contacts._ID));
        String displayName = mainQueryCursor.getString(mainQueryCursor
                .getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));
        String phoneNumber = null;
        Date gebdt = null;
        String[] dataQueryProjection = new String[]{
                ContactsContract.Data.MIMETYPE,
                ContactsContract.CommonDataKinds.Event.TYPE,
                ContactsContract.CommonDataKinds.Event.START_DATE,
                ContactsContract.CommonDataKinds.Note.NOTE,
                ContactsContract.CommonDataKinds.Phone.NUMBER,
                ContactsContract.CommonDataKinds.Phone.TYPE};
        String dataQuerySelection = ContactsContract.Data.CONTACT_ID + " = ?";
        String[] dataQuerySelectionArgs = new String[]{contactId};
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
                        Date d = TKDateUtils.getDateFromString1(stringBirthday);
                        if (d != null) {
                            gebdt = d;
                        }
                    }
                } else if (ContactsContract.CommonDataKinds.Note.CONTENT_ITEM_TYPE
                        .equals(mimeType)) {
                    String note = dataQueryCursor.getString(3);
                    Date d = TKDateUtils.getDateFromString(note);
                    if (d != null) {
                        gebdt = d;
                    }
                } else if (ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE
                        .equals(mimeType)) {
                    if (ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE == dataQueryCursor
                            .getInt(5)) {
                        phoneNumber = dataQueryCursor.getString(4);
                    }
                }
            }
            dataQueryCursor.close();
        }
        return new BirthdayItem(displayName, gebdt,
                Long.valueOf(contactId), phoneNumber);
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
            int notificationDays = TKBirthdayReminder
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
                int days2 = TKDateUtils.getBirthdayInDays(item2.getBirthday());
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
