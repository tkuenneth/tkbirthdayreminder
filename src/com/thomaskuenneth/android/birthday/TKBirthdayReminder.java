/**
 * TKBirthdayReminder.java
 * 
 * TKBirthdayReminder (c) Thomas Künneth 2009
 * Alle Rechte beim Autoren. All rights reserved.
 */
package com.thomaskuenneth.android.birthday;

import java.util.ArrayList;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

/**
 * Dies ist die Hauptklasse von TKBirthdayReminder. Sie prüft, ob eine neue
 * Version installiert wurde und zeigt ggf. einen Willkommensdialog an.
 * 
 * @author Thomas Künneth
 * @see AbstractListActivity
 */
public class TKBirthdayReminder extends AbstractListActivity {

	public static final boolean isContactsContractPresent = _isContactsContractPresent();

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
			if (savedInstanceState == null) {
				showDialog(Constants.WELCOME_ID);
			}
		} else {
			run();
		}
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case Constants.WELCOME_ID:
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle(R.string.welcome);
			builder.setIcon(R.drawable.birthdaycake_32);
			View textView = getLayoutInflater().inflate(R.layout.welcome, null);
			builder.setView(textView);
			builder.setPositiveButton(R.string.alert_dialog_continue,
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							writeToPreferences();
							run();
						}

					});
			builder.setNegativeButton(R.string.alert_dialog_abort,
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							finish();
						}

					});
			builder.setCancelable(false);
			return builder.create();
		default:
			return super.onCreateDialog(id);
		}
	}

	@Override
	protected String getStateKey() {
		return Constants.LIST_BIRTHDAY_SET;
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

	private static boolean _isContactsContractPresent() {
		boolean result = false;
		try {
			Class.forName("android.provider.ContactsContract");
			result = true;
		} catch (Throwable thr) {
		}
		Log.d(TKBirthdayReminder.class.getName(), Boolean.toString(result));
		return result;
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