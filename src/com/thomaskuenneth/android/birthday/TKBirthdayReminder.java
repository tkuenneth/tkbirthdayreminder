/**
 * TKBirthdayReminder.java
 * 
 * TKBirthdayReminder (c) Thomas Künneth 2009
 * Alle Rechte beim Autoren. All rights reserved.
 */
package com.thomaskuenneth.android.birthday;

import java.util.ArrayList;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.util.Log;

/**
 * Dies ist die Hauptklasse von TKBirthdayReminder. Sie prüft, ob eine neue
 * Version installiert wurde und zeigt ggf. einen Willkommensdialog an.
 * 
 * @author Thomas Künneth
 * @see AbstractListActivity
 */
public class TKBirthdayReminder extends AbstractListActivity {

	/**
	 * Der aktuelle sowie der zuletzt gespeicherte versionCode. Wird verwendet,
	 * um ggf. beim Start ein README oä. anzuzeigen.
	 */
	private static final String VERSION_CODE = "versionCode";
	public static int storedVersionCode;
	public static int currentVersionCode;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		// auf neue Version prüfen
		if (isNewVersion()) {
			Intent i = new Intent(this, WhatsNew.class);
			startActivityForResult(i, Constants.RQ_WELCOME);
		} else {
			run();
		}
	}

	@Override
	protected String getStateKey() {
		return Constants.LIST_BIRTHDAY_SET;
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == Constants.RQ_WELCOME) {
			if (resultCode == RESULT_OK) {
				writeToPreferences();
				run();
			} else if (resultCode == RESULT_CANCELED) {
				finish();
			}
		}
	}

	public static String getStringFromResources(Context context, int resId) {
		return context.getString(resId);
	}

	public static String getStringFromResources(Context context, int resId,
			Object... formatArgs) {
		return context.getString(resId, formatArgs);
	}

	/**
	 * Setzt den Alarm und die Liste mit Geburtstagen; falls diese nicht aus
	 * einer zuletzt gespeicherten Instanz wiederhergestellt werden konnte,
	 * werden die Kontakte neu eingelesen.
	 */
	private void run() {
		BootCompleteReceiver.startAlarm(this, true);
		readContacts(false);
	}

	@Override
	protected ArrayList<BirthdayItem> getProperList(ContactsList cl) {
		return cl.getListBirthdaySet();
	}

	/**
	 * Prüft, ob seit dem letzten Start eine neue Version installiert wurde.
	 * 
	 * @return liefert {@code true}, wenn seit dem letzten Start eine neue
	 *         Version installiert wurde; sonst {@code false}
	 */
	private boolean isNewVersion() {
		readFromPreferences();
		boolean newVersion = storedVersionCode < currentVersionCode;
		Log.d(getClass().getName(), "newVersion: " + newVersion);
		return newVersion;
	}

	/**
	 * Belegt die beiden Variablen storedVersionCode (wird aus den shared
	 * preferences ausgelsen) und currentVersionCode (wird aus PackageInfo
	 * ermittelt).
	 */
	private void readFromPreferences() {
		SharedPreferences prefs = getSharedPreferences(
				Constants.SHARED_PREFS_KEY, Context.MODE_PRIVATE);
		storedVersionCode = prefs.getInt(VERSION_CODE, 0);
		try {
			PackageInfo info = getPackageManager().getPackageInfo(
					getPackageName(), 0);
			currentVersionCode = info.versionCode;
		} catch (NameNotFoundException e) {
			// da es nur ein Versionscheck ist, ignorieren wir den Fehler
			currentVersionCode = 0;
		}
	}

	/**
	 * Speichert den aktuellen versionCode in den shared preferences.
	 */
	private void writeToPreferences() {
		SharedPreferences prefs = getSharedPreferences(
				Constants.SHARED_PREFS_KEY, Context.MODE_PRIVATE);
		SharedPreferences.Editor editor = prefs.edit();
		editor.putInt(VERSION_CODE, currentVersionCode);
		editor.commit();
	}
}