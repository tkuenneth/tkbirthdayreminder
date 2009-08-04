/**
 * TimePickerDialogHelper.java
 * 
 * TKBirthdayReminder (c) Thomas Künneth 2009
 * 
 * Alle Rechte beim Autoren. All rights reserved.
 */
package com.thomaskuenneth.android.birthday;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

/**
 * Diese Klasse hilft beim Datenaustausch zwischen den Preferences und dem
 * TimePickerDialog.
 * 
 * @author Thomas Künneth
 * 
 */
public class TimePickerDialogHelper {

	public static final String TKBIRTHDAY_REMINDER = "TKBirthdayReminder";

	private static final String NOTIFICATION_TIME_HOUR = "notificationTimeHour";
	private static final String NOTIFICATION_TIME_MINUTE = "notificationTimeMinute";

	/**
	 * Stunden (0 - 23)
	 */
	public static int hour;

	/**
	 * Minuten (0 - 59)
	 */
	public static int minute;

	public static void readFromPreferences(Context context) {
		SharedPreferences prefs = context.getSharedPreferences(
				TKBIRTHDAY_REMINDER, Context.MODE_PRIVATE);
		hour = prefs.getInt(NOTIFICATION_TIME_HOUR, 12);
		minute = prefs.getInt(NOTIFICATION_TIME_MINUTE, 0);
	}

	public static void writeToPreferences(Context context, int hour, int minute) {
		TimePickerDialogHelper.hour = hour;
		TimePickerDialogHelper.minute = minute;
		SharedPreferences prefs = context.getSharedPreferences(
				TKBIRTHDAY_REMINDER, Context.MODE_PRIVATE);
		Editor editor = prefs.edit();
		editor.putInt(NOTIFICATION_TIME_HOUR, hour);
		editor.putInt(NOTIFICATION_TIME_MINUTE, minute);
		editor.commit();
	}
}
