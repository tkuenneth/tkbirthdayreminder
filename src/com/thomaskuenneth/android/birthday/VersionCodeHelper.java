package com.thomaskuenneth.android.birthday;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;

public class VersionCodeHelper {

	/**
	 * Der aktuelle versionCode. Wird verwendet, um ggf. beim Start ein README
	 * oä. anzuzeigen.
	 */
	private static final String VERSION_CODE = "versionCode";

	public static int storedVersionCode;

	public static int currentVersionCode;

	public static void readFromPreferences(Context context) {
		SharedPreferences prefs = context.getSharedPreferences(
				TimePickerDialogHelper.TKBIRTHDAY_REMINDER,
				Context.MODE_PRIVATE);
		// liefert den gespeicherten versionCode oder 0
		storedVersionCode = prefs.getInt(VERSION_CODE, 0);
		try {
			PackageInfo info = context.getPackageManager().getPackageInfo(
					context.getPackageName(), 0);
			currentVersionCode = info.versionCode;
		} catch (NameNotFoundException e) {
			// da es nur ein Versionscheck ist, ignorieren wir den Fehler
			currentVersionCode = 0;
		}
	}

	public static void writeToPreferences(Context context) {
		SharedPreferences prefs = context.getSharedPreferences(
				TimePickerDialogHelper.TKBIRTHDAY_REMINDER,
				Context.MODE_PRIVATE);
		// speichert den aktuellen versionCode
		SharedPreferences.Editor editor = prefs.edit();
		editor.putInt(VERSION_CODE, currentVersionCode);
		editor.commit();
	}
}
