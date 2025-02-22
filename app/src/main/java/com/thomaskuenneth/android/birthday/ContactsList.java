/*
 * ContactsList.java
 *
 * TKBirthdayReminder (c) Thomas Künneth 2009 - 2025
 * All rights reserved.
 */
package com.thomaskuenneth.android.birthday;

import static com.thomaskuenneth.android.birthday.ContactsUtilsKt.getAccountTypeForContact;

import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.Hashtable;
import java.util.List;

import kotlin.Pair;

class ContactsList implements Comparator<BirthdayItem> {

    private static final String TAG = ContactsList.class.getSimpleName();

    private final List<BirthdayItem> main;
    private final List<BirthdayItem> widget;
    private final List<BirthdayItem> notifications;

    private final Context context;

    ContactsList(Context context) {
        main = new ArrayList<>();
        widget = new ArrayList<>();
        notifications = new ArrayList<>();
        this.context = context;
        readContacts(context);
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
            int days1 = Utils.getBirthdayInDays(item1.getBirthday(), null);
            int days2 = Utils.getBirthdayInDays(item2.getBirthday(), null);
            if (days1 == days2) {
                return 0;
            } else {
                return (days1 < days2) ? -1 : 1;
            }
        }
    }

    List<BirthdayItem> getMainList() {
        return main;
    }

    List<BirthdayItem> getWidgetList() {
        return widget;
    }

    List<BirthdayItem> getNotificationsList() {
        return notifications;
    }

    private void readContacts(final Context context) {
        ContentResolver contentResolver = context.getContentResolver();
        queryContacts(contentResolver);
        widget.sort(this);
        notifications.sort(this);
        main.sort(this);
    }

    private void queryContacts(ContentResolver contentResolver) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        boolean hidePastBirthdays = prefs.getBoolean("hide_past_birthdays", false);
        Hashtable<String, Boolean> ht = new Hashtable<>();
        String[] mainQueryProjection = {ContactsContract.Contacts._ID,
                ContactsContract.Contacts.DISPLAY_NAME};
        Cursor mainQueryCursor = null;
        try {
            mainQueryCursor = contentResolver.query(
                    ContactsContract.Contacts.CONTENT_URI, mainQueryProjection,
                    null, null, null);
        } catch (SecurityException e) {
            Utils.logError(TAG, "queryContacts() - missing required permissions", e);
        }
        if (mainQueryCursor != null) {
            while (mainQueryCursor.moveToNext()) {
                try {
                    BirthdayItem item = createItemFromCursor(contentResolver,
                            mainQueryCursor);
                    String name = item.getName();
                    Date date = item.getBirthday();
                    if ((name != null) && (date != null)) {
                        String str_YYYY_MM_DD = Utils.FORMAT_YYYYMMDD
                                .format(date);
                        Utils.logDebug(TAG, name + ": " + str_YYYY_MM_DD);
                        String key = name + str_YYYY_MM_DD;
                        if (!ht.containsKey(key)) {
                            ht.put(key, Boolean.TRUE);
                            Date birthday = item.getBirthday();
                            if (shouldAddToMainList(birthday, hidePastBirthdays)) {
                                main.add(item);
                            }
                            if (shouldAddToWidgetList(birthday)) {
                                widget.add(item);
                            }
                            if (shouldAddToNotificationsList(birthday)) {
                                notifications.add(item);
                            }
                        }
                    }
                } catch (Exception e) {
                    Utils.logError(TAG, e.getMessage(), e);
                }
            }
            mainQueryCursor.close();
        }
    }

    static BirthdayItem createItemFromCursor(
            ContentResolver contentResolver, Cursor mainQueryCursor) {
        String contactId = mainQueryCursor.getString(mainQueryCursor
                .getColumnIndexOrThrow(ContactsContract.Contacts._ID));
        Pair<String, String> account = getAccountTypeForContact(contentResolver, contactId);
        String accountName = account != null ? account.getFirst() : "";
        String displayName = mainQueryCursor.getString(mainQueryCursor
                .getColumnIndexOrThrow(ContactsContract.Contacts.DISPLAY_NAME));
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
                        Date d = Utils.getDateFromString1(stringBirthday);
                        if (d != null) {
                            gebdt = d;
                        }
                    }
                } else if (ContactsContract.CommonDataKinds.Note.CONTENT_ITEM_TYPE
                        .equals(mimeType)) {
                    String note = dataQueryCursor.getString(3);
                    Date d = Utils.getDateFromString(note);
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
                Long.parseLong(contactId), phoneNumber, accountName);
    }

    private boolean shouldAddToMainList(Date birthday, boolean hidePastBirthdays) {
        return birthday != null && (!hidePastBirthdays || Utils.getBirthdayInDays(birthday, null) >= 0);
    }

    private boolean shouldAddToWidgetList(Date birthday) {
        return birthday != null;
    }

    private boolean shouldAddToNotificationsList(Date birthday) {
        if (birthday != null) {
            int daysUntilBirthday = Utils.getBirthdayInDays(birthday, null);
            if (daysUntilBirthday == 0) {
                return true;
            } else if ((daysUntilBirthday < 0) || (daysUntilBirthday > 7)) {
                return false;
            }
            int notificationDays = NotificationPreferenceFragment
                    .getNotificationDays(context);
            int mask = 1 << (daysUntilBirthday - 1);
            return (notificationDays & mask) == mask;
        }
        return false;
    }
}
