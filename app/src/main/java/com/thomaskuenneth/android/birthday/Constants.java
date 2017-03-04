/*
 * Constants.java
 *
 * TKBirthdayReminder (c) Thomas Künneth 2009 - 2017
 * Alle Rechte beim Autoren. All rights reserved.
 */
package com.thomaskuenneth.android.birthday;

/**
 * In dieser Klasse werden Konstanten (z. B. für result codes und Dialoge)
 * gesammelt.
 *
 * @author Thomas Künneth
 */
class Constants {

    static final String SHARED_PREFS_KEY = "TKBirthdayReminder";

    static final String TKBR2 = NotificationView.class.getName();

    static final String LIST_BIRTHDAY_SET = "listBirthdaySet";
    static final String LIST_NOTIFICATIONS = "listNotifications";

    /**
     * IDs für Menüeinträge.
     */
    static final int MENU_CHANGE_DATE = R.string.menu_change_date;
    static final int MENU_REMOVE_DATE = R.string.menu_remove_date;
    static final int MENU_DIAL = R.string.menu_dial;
    static final int MENU_SEND_SMS = R.string.menu_send_sms;

    /**
     * IDs für Dialoge.
     */
    static final int TIME_DIALOG_ID = 1;
    static final int NOTIFICATION_DAYS_DIALOG_ID = 2;
    static final int DATE_DIALOG_ID = 3;
    static final int WELCOME_ID = 5;

    /**
     * IDs für result codes
     */
    static final int RQ_PICK_CONTACT = 0x231167;
    static final int RQ_PICK_SOUND = 0x03091938;
    static final int RQ_PREFERENCES = 0x060667;
    static final int RQ_SHOW_CONTACT = 0x01032002;

}
