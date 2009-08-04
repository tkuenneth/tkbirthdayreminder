/**
 * TKBirthdayReminder.java
 * 
 * TKBirthdayReminder (c) Thomas Künneth 2009
 * 
 * Alle Rechte beim Autoren. All rights reserved.
 */
package com.thomaskuenneth.android.birthday;

import java.util.Calendar;
import java.util.GregorianCalendar;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.TabActivity;
import android.app.TimePickerDialog;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TabHost;
import android.widget.TimePicker;

/**
 * Dies ist die Hauptklasse von TKBirthdayReminder.
 * <p>
 * 
 * @author Thomas Künneth
 * 
 */
public class TKBirthdayReminder extends TabActivity implements
		OnTimeSetListener {

	/**
	 * Schlüssel, unter dem die Erinnerungstage abgelegt werden.
	 */
	private static final String NOTIFICATION_DAYS = "notificationDays";

	/**
	 * IDs für Dialoge.
	 */
	private static final int TIME_DIALOG_ID = 0;
	private static final int NOTIFICATION_DAYS_DIALOG_ID = 1;
	private static final int NEW_ENTRY_ID = 2;

	/**
	 * Wird verwendet, um den Willkommen-Dialog zu identifizieren.
	 */
	private static final int REQUEST_CODE = 290870;

	/**
	 * Die beiden Tabs der Anwendung.
	 */
	private static final String TAB1 = "tab1";
	private static final String TAB2 = "tab2";

	/**
	 * IDs der Checkboxen im Dialog zum Setzen der Erinnerungstage.
	 */
	private static final int[] ids = new int[] { R.id.days_1, R.id.days_2,
			R.id.days_3, R.id.days_4, R.id.days_5, R.id.days_6, R.id.days_7 };

	public static Context instance = null;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		instance = this;
		// ggf. einen Willkommen-Dialog anzeigen
		if (isNewVersion()) {
			Intent i = new Intent(this, WhatsNew.class);
			startActivityForResult(i, REQUEST_CODE);
		} else {
			run();
		}
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (requestCode == REQUEST_CODE) {
			if (resultCode == RESULT_OK) {
				// Der aktuelle versionCode wird gespeichert.
				VersionCodeHelper.writeToPreferences(this);
				run();
			} else if (resultCode == RESULT_CANCELED) {
				finish();
			}
		}
	}

	/***********
	 * Dialoge *
	 ***********/

	@Override
	protected void onPrepareDialog(int id, Dialog dialog) {
		if (id == TIME_DIALOG_ID) {
			TimePickerDialogHelper.readFromPreferences(this);
			TimePickerDialog picker = (TimePickerDialog) dialog;
			picker.updateTime(TimePickerDialogHelper.hour,
					TimePickerDialogHelper.minute);
		} else if (id == NOTIFICATION_DAYS_DIALOG_ID) {
			updateCheckboxes(dialog);
		}
		super.onPrepareDialog(id, dialog);
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		if (id == TIME_DIALOG_ID) {
			TimePickerDialogHelper.readFromPreferences(this);
			return new TimePickerDialog(this, this,
					TimePickerDialogHelper.hour, TimePickerDialogHelper.minute,
					true);
		} else if (id == NOTIFICATION_DAYS_DIALOG_ID) {
			View view = LayoutInflater.from(this).inflate(
					R.layout.notification_days, null);
			return new AlertDialog.Builder(this)
					// .setIcon(R.drawable.alert_dialog_icon)
					.setTitle(R.string.menu_set_notification_days)
					.setPositiveButton(R.string.alert_dialog_ok,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {
									updateNotificationDays((AlertDialog) dialog);
								}
							}).setNegativeButton(R.string.alert_dialog_cancel,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {
								}
							}).setView(view).create();
		} else if (id == NEW_ENTRY_ID) {
			LayoutInflater factory = LayoutInflater.from(this);
			View view = factory.inflate(R.layout.new_contact, null);
			return new AlertDialog.Builder(this)
					// .setIcon(R.drawable.alert_dialog_icon)
					.setTitle(R.string.new_entry_dialog_title)
					.setPositiveButton(R.string.alert_dialog_ok,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {
									addContact((AlertDialog) dialog);
								}
							}).setNegativeButton(R.string.alert_dialog_cancel,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {
								}
							}).setView(view).create();
		}
		return null;
	}

	private void updateCheckboxes(Dialog dialog) {
		SharedPreferences prefs = getSharedPreferences(
				TimePickerDialogHelper.TKBIRTHDAY_REMINDER,
				Context.MODE_PRIVATE);
		int notificationDays = prefs.getInt(NOTIFICATION_DAYS, 0);
		for (int bit = 0; bit < 7; bit++) {
			CheckBox cb = (CheckBox) dialog.findViewById(ids[bit]);
			int mask = 1 << bit;
			cb.setChecked((notificationDays & mask) == mask ? true : false);
		}
	}

	private void updateNotificationDays(Dialog dialog) {
		int bits = 0;
		for (int bit = 0; bit < 7; bit++) {
			CheckBox cb = (CheckBox) dialog.findViewById(ids[bit]);
			if (cb.isChecked()) {
				bits |= (1 << bit);
			}
		}
		SharedPreferences prefs = getSharedPreferences(
				TimePickerDialogHelper.TKBIRTHDAY_REMINDER,
				Context.MODE_PRIVATE);
		Editor editor = prefs.edit();
		editor.putInt(NOTIFICATION_DAYS, bits);
		editor.commit();
	}

	private void addContact(Dialog dialog) {
		EditText v1 = (EditText) dialog.findViewById(R.id.new_contact_name);
		DatePicker v2 = (DatePicker) dialog.findViewById(R.id.new_contact_date);
		long id = ContactsList.addGroup(this, "TKBirthdayReminder");
		String name = v1.getText().toString();
		Calendar cal = new GregorianCalendar();
		cal.set(GregorianCalendar.YEAR, v2.getYear());
		cal.set(GregorianCalendar.MONTH, v2.getMonth());
		cal.set(GregorianCalendar.DAY_OF_MONTH, v2.getDayOfMonth());
		String notes = TKDateUtils.getStringFromDate(cal.getTime(), null);
		ContactsList.createPerson(this, id, name, notes);
		readContacts();
	}

	public static int getNotificationDays() {
		if (instance != null) {
			SharedPreferences prefs = instance.getSharedPreferences(
					TimePickerDialogHelper.TKBIRTHDAY_REMINDER,
					Context.MODE_PRIVATE);
			return prefs.getInt(NOTIFICATION_DAYS, 0);
		}
		return 0;
	}

	private void run() {
		BootCompleteReceiver.startAlarm(this, true);

		TabHost tabHost = getTabHost();
		tabHost.addTab(tabHost.newTabSpec(TAB1).setIndicator(
				getString(R.string.tab1),
				getResources().getDrawable(R.drawable.birthdaycake_32))
				.setContent(new Intent(this, BirthdaySetActivity.class)));
		tabHost.addTab(tabHost.newTabSpec(TAB2).setIndicator(
				getString(R.string.tab2),
				getResources().getDrawable(
						R.drawable.birthdaycake_with_questionmark_32))
				.setContent(new Intent(this, BirthdayNotSetActivity.class)));

		readContacts();
	}

	private void readContacts() {
		setProgressBarIndeterminateVisibility(true);
		ContactsList.readContacts(TKBirthdayReminder.this, new Runnable() {

			@Override
			public void run() {
				setProgressBarIndeterminateVisibility(false);
			}

		});

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.set_alarm:
			showDialog(TIME_DIALOG_ID);
			break;
		case R.id.set_notification_days:
			showDialog(NOTIFICATION_DAYS_DIALOG_ID);
			break;
		// case R.id.preferences:
		// Intent i = new Intent(this, Preferences.class);
		// i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		// startActivity(i);
		// break;
		case R.id.new_entry:
			showDialog(NEW_ENTRY_ID);
			break;
		}
		return true;
	}

	public static String getStringFromResources(int resId) {
		return instance.getString(resId);
	}

	public static String getStringFromResources(int resId, Object... formatArgs) {
		return instance.getString(resId, formatArgs);
	}

	/**
	 * Prüft, ob seit dem letzten Start eine neue Version installiert wurde.
	 * 
	 * @return liefert {@code true}, wenn seit dem letzten Start eine neue
	 *         Version installiert wurde; sonst {@code false}
	 */
	private boolean isNewVersion() {
		VersionCodeHelper.readFromPreferences(this);
		return VersionCodeHelper.storedVersionCode < VersionCodeHelper.currentVersionCode;
	}

	// /////////////////////
	// OnTimeSetListener //
	// /////////////////////

	@Override
	public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
		TimePickerDialogHelper.writeToPreferences(this, hourOfDay, minute);
		BootCompleteReceiver.startAlarm(this, true);
	}
}