/**
 * Constants.java
 * 
 * TKBirthdayReminder (c) Thomas Künneth 2009
 * Alle Rechte beim Autoren. All rights reserved.
 */
package com.thomaskuenneth.android.birthday;

/**
 * In dieser Klasse werden Konstanten (z. B. für result codes und Dialoge)
 * gesammelt.
 * 
 * @author Thomas Künneth
 * 
 */
public class Constants {

	public static final String SHARED_PREFS_KEY = "TKBirthdayReminder";

	public static final String TKBR2 = NotificationView.class.getName();

	public static final String LIST_BIRTHDAY_SET = "listBirthdaySet";
	public static final String LIST_BIRTHDAY_NOT_SET = "listBirthdayNotSet";
	public static final String LIST_NOTIFICATIONS = "listNotifications";

	/**
	 * IDs für Menüeinträge.
	 */
	public static final int MENU_CHANGE_DATE = R.string.menu_change_date;
	public static final int MENU_REMOVE_DATE = R.string.menu_remove_date;
	public static final int MENU_DIAL = R.string.menu_dial;
	public static final int MENU_SEND_SMS = R.string.menu_send_sms;

	/**
	 * IDs für Dialoge.
	 */
	public static final int TIME_DIALOG_ID = 1;
	public static final int NOTIFICATION_DAYS_DIALOG_ID = 2;
	public static final int DATE_DIALOG_ID = 3;
	public static final int NEW_CONTACT_ID = 4;

	/**
	 * IDs für result codes
	 */
	public static final int RQ_WELCOME = 0x290870;
	public static final int RQ_PICK_CONTACT = 0x231167;
	public static final int RQ_PICK_SOUND = 0x03091938;

}
