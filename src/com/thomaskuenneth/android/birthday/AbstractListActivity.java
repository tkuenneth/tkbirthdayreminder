/**
 * AbstractListActivity.java
 * 
 * TKBirthdayReminder (c) Thomas Künneth 2009
 * Alle Rechte beim Autoren. All rights reserved.
 */
package com.thomaskuenneth.android.birthday;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.app.TimePickerDialog;
import android.app.DatePickerDialog.OnDateSetListener;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Contacts;
import android.provider.Contacts.People;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TimePicker;
import android.widget.AdapterView.AdapterContextMenuInfo;

/**
 * Diese abstrakte Basisklasse stellt die Kernunktionalität der Listenansichten
 * zur Verfügung.
 * 
 * @author Thomas Künneth
 * @see ListActivity
 * @see OnDateSetListener
 * @see OnTimeSetListener
 */
public abstract class AbstractListActivity extends ListActivity implements
		OnDateSetListener, OnTimeSetListener {

	/**
	 * IDs für Menüeinträge.
	 */
	private static final int MENU_CHANGE_DATE = R.string.menu_change_date;
	private static final int MENU_REMOVE_DATE = R.string.menu_remove_date;
	private static final int MENU_DIAL = R.string.menu_dial;
	private static final int MENU_SEND_SMS = R.string.menu_send_sms;

	/**
	 * IDs für Dialoge.
	 */
	private static final int TIME_DIALOG_ID = 11;
	private static final int NOTIFICATION_DAYS_DIALOG_ID = 1;
	private static final int NEW_ENTRY_ID = 2;
	private static final int DATE_DIALOG_ID = 10;

	/**
	 * Listenelement, das lange angeklickt wurde.
	 */
	private BirthdayItem longClickedItem;

	/**
	 * IDs der Checkboxen im Dialog zum Setzen der Erinnerungstage.
	 */
	private static final int[] ids = new int[] { R.id.days_1, R.id.days_2,
			R.id.days_3, R.id.days_4, R.id.days_5, R.id.days_6, R.id.days_7 };

	/**
	 * Schlüssel, unter dem die Erinnerungstage abgelegt werden.
	 */
	private static final String NOTIFICATION_DAYS = "notificationDays";

	protected ArrayList<BirthdayItem> list;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		getListView().setOnCreateContextMenuListener(this);
	}

	@Override
	public void onActivityResult(int reqCode, int resultCode, Intent data) {
		super.onActivityResult(reqCode, resultCode, data);

		switch (reqCode) {
		case (Constants.RQ_PICK_CONTACT):
			if (resultCode == Activity.RESULT_OK) {
				Uri contactData = data.getData();
				Cursor c = managedQuery(contactData, null, null, null, null);
				if (c.moveToFirst()) {
					String name = c.getString(c
							.getColumnIndex(People.DISPLAY_NAME));
					String notes = c.getString(c.getColumnIndex(People.NOTES));
					long id = c.getLong(c.getColumnIndex(People._ID));
					String primaryPhoneNumber = c.getString(c
							.getColumnIndex(People.NUMBER));
					longClickedItem = new BirthdayItem(name, TKDateUtils
							.getDateFromString(notes), id, primaryPhoneNumber);
					showDialog(DATE_DIALOG_ID);
				}
			}
			break;
		}
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		AdapterContextMenuInfo mi = (AdapterContextMenuInfo) menuInfo;
		BirthdayItem item = (BirthdayItem) getListAdapter()
				.getItem(mi.position);
		menu.setHeaderTitle(item.getName());
		if (item.getBirthday() != null) {
			menu.add(Menu.NONE, MENU_CHANGE_DATE, Menu.NONE, MENU_CHANGE_DATE);
			menu.add(Menu.NONE, MENU_REMOVE_DATE, Menu.NONE, MENU_REMOVE_DATE);
		}
		if (item.getPrimaryPhoneNumber() != null) {
			menu.add(Menu.NONE, MENU_DIAL, Menu.NONE, MENU_DIAL);
			String string = getString(MENU_SEND_SMS, item
					.getPrimaryPhoneNumber());
			menu.add(Menu.NONE, MENU_SEND_SMS, Menu.NONE, string);
		}
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo mi = (AdapterContextMenuInfo) item.getMenuInfo();
		longClickedItem = (BirthdayItem) getListAdapter().getItem(mi.position);
		switch (item.getItemId()) {
		case MENU_CHANGE_DATE:
			showDialog(DATE_DIALOG_ID);
			break;
		case MENU_REMOVE_DATE:
			longClickedItem.setBirthday(null);
			updateContact(longClickedItem);
			break;
		case MENU_DIAL:
			Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:"
					+ longClickedItem.getPrimaryPhoneNumber()));
			startActivity(intent);
			break;
		case MENU_SEND_SMS:
			Uri smsUri = Uri.parse("smsto://"
					+ longClickedItem.getPrimaryPhoneNumber());
			Intent sendIntent = new Intent(Intent.ACTION_SENDTO, smsUri);
			sendIntent.putExtra("sms_body", "");
			startActivity(sendIntent);
			break;
		}
		return true;
	}

	/***********
	 * Dialoge *
	 ***********/

	@Override
	protected void onPrepareDialog(int id, Dialog dialog) {
		if (id == DATE_DIALOG_ID) {
			Calendar cal = new GregorianCalendar();
			Date birthday = longClickedItem.getBirthday();
			if (birthday != null) {
				cal.setTime(birthday);
			}
			DatePickerDialog picker = (DatePickerDialog) dialog;
			picker.updateDate(cal.get(GregorianCalendar.YEAR), cal
					.get(GregorianCalendar.MONTH), cal
					.get(GregorianCalendar.DAY_OF_MONTH));
		} else if (id == TIME_DIALOG_ID) {
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
		if (id == DATE_DIALOG_ID) {
			Calendar cal = new GregorianCalendar();
			return new DatePickerDialog(this, this, cal
					.get(GregorianCalendar.YEAR), cal
					.get(GregorianCalendar.MONTH), cal
					.get(GregorianCalendar.DAY_OF_MONTH));
		} else if (id == TIME_DIALOG_ID) {
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

	// Optionsmenü

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
		inflater.inflate(R.menu.menu, menu);
		if (this instanceof BirthdayNotSetActivity) {
			menu.removeItem(R.id.set_date);
		}
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
		case R.id.set_date:
//			Intent intent = new Intent(Intent.ACTION_PICK, People.CONTENT_URI);
			Intent intent = new Intent(this, BirthdayNotSetActivity.class);
			startActivityForResult(intent, Constants.RQ_PICK_CONTACT);
			break;
		}
		return true;
	}

	/**
	 * Aktualisiert einen Kontakt in der Datenbank und anschließend die Listen.
	 * 
	 * @param item
	 *            der zu aktualisierende Kontakt
	 */
	public void updateContact(BirthdayItem item) {
		ContentResolver contentResolver = getContentResolver();
		Uri uri = Uri.withAppendedPath(Contacts.People.CONTENT_URI, Long
				.toString(item.getId()));
		// lesen des Notizfeldes
		Cursor c = contentResolver.query(uri, null, null, null, null);
		if (c != null) {
			if (c.moveToFirst()) {
				String notes = c.getString(c.getColumnIndex(People.NOTES));
				// Aktualisieren des Eintrags
				ContentValues values = new ContentValues();
				values.put(People.NOTES, TKDateUtils.getStringFromDate(item
						.getBirthday(), notes));
				contentResolver.update(uri, values, null, null);
			}
			c.close();
			readContacts(true);
		}
	}

	public void setList(ArrayList<BirthdayItem> list) {
		this.list = list;
		BirthdayItemListAdapter myListAdapter = new BirthdayItemListAdapter(
				this, list);
		setListAdapter(myListAdapter);
	}

	protected void setListFromBundle(String listKey) {
		Bundle bundleExtras = getIntent().getExtras();
		if (bundleExtras != null) {
			Bundle bundleList = bundleExtras.getBundle(Constants.TKBR);
			if (bundleList != null) {
				ArrayList<BirthdayItem> list = bundleList
						.getParcelableArrayList(listKey);
				if (list != null) {
					setList(list);
				}
			}
		}
	}

	private void updateCheckboxes(Dialog dialog) {
		SharedPreferences prefs = getSharedPreferences(
				Constants.SHARED_PREFS_KEY, Context.MODE_PRIVATE);
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
				Constants.SHARED_PREFS_KEY, Context.MODE_PRIVATE);
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
		readContacts(true);
	}

	/**
	 * Liest die Kontakte neu ein.
	 * 
	 * @param forceRead
	 *            Ignoriert evtl. bereits gelesene Kontakte
	 */
	protected void readContacts(final boolean forceRead) {
		setProgressBarIndeterminateVisibility(true);
		final Handler h = new Handler(Looper.myLooper());
		Thread thread = new Thread(new Runnable() {

			@Override
			public void run() {
				Looper.prepare();
				if ((list == null) || forceRead) {
					ContactsList cl = new ContactsList(
							AbstractListActivity.this);
					list = getProperList(cl);
				}
				h.post(new Runnable() {

					@Override
					public void run() {
						setList(list);
						setProgressBarIndeterminateVisibility(false);
					}

				});
			}

		});
		thread.start();
	}
	
	protected abstract ArrayList<BirthdayItem> getProperList(ContactsList cl);

	public static int getNotificationDays(Context context) {
		SharedPreferences prefs = context.getSharedPreferences(
				Constants.SHARED_PREFS_KEY, Context.MODE_PRIVATE);
		return prefs.getInt(NOTIFICATION_DAYS, 0);
	}

	// /////////////////////
	// OnDateSetListener //
	// /////////////////////

	@Override
	public void onDateSet(DatePicker view, int year, int monthOfYear,
			int dayOfMonth) {
		Calendar cal = new GregorianCalendar();
		cal.set(GregorianCalendar.YEAR, year);
		cal.set(GregorianCalendar.MONTH, monthOfYear);
		cal.set(GregorianCalendar.DAY_OF_MONTH, dayOfMonth);
		longClickedItem.setBirthday(cal.getTime());
		updateContact(longClickedItem);
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
