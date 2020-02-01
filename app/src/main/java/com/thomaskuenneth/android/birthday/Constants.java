/*
 * Constants.java
 *
 * TKBirthdayReminder (c) Thomas Künneth 2009 - 2020
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

    static final String TKBIRTHDAYREMINDER = "TKBirthdayReminder";
    static final String CHANNEL_ID = "default";

    static final int MENU_CHANGE_DATE = R.string.menu_change_date;
    static final int MENU_REMOVE_DATE = R.string.menu_remove_date;
    static final int MENU_DIAL = R.string.menu_dial;
    static final int MENU_SEND_SMS = R.string.menu_send_sms;

    static final int DATE_DIALOG_ID = 3;
    static final int WELCOME_ID = 5;

    static final int RQ_PICK_CONTACT = 0x2311;
    static final int RQ_PREFERENCES = 0x0606;
    static final int RQ_SHOW_CONTACT = 0x0103;
}
