/**
 * AbstractListActivity.java
 * 
 * TKBirthdayReminder (c) Thomas Künneth 2009
 * Alle Rechte beim Autoren. All rights reserved.
 */
package com.thomaskuenneth.android.birthday;

import java.text.DateFormat;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.app.TimePickerDialog;
import android.app.TimePickerDialog.OnTimeSetListener;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Contacts;
import android.provider.Contacts.People;
import android.util.Log;
import android.view.ContextMenu;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnKeyListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemSelectedListener;

import com.thomaskuenneth.android.util.ContactsContractWrapper;

/**
 * Diese abstrakte Basisklasse stellt die Kernunktionalität der Listenansichten
 * zur Verfügung.
 * 
 * @author Thomas Künneth
 * @see ListActivity
 * @see OnTimeSetListener
 */
public abstract class AbstractListActivity extends ListActivity implements
		OnTimeSetListener {

	private static final String NEW_EVENT_EVENT = "newEventEvent";
	private static final String LONG_CLICK_ITEM = "longClickItem";

	// Neuer Kontakt
	private static final DateFormat FORMAT_MONTH_SHORT = new SimpleDateFormat(
			"MMM");
	private TextView newEventDescr, newEventYear;
	private Spinner newEventSpinnerDay, newEventSpinnerMonth;
	private Calendar newEventCal;
	private AnnualEvent newEventEvent;

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

	/**
	 * Schlüssel, unter dem der Erinnerungston eingetragen wird.
	 */
	private static final String NOTIFICATION_SOUND = "notificationSound";

	protected ArrayList<BirthdayItem> list;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

		// ggf. gespeicherte Liste wiederherstellen
		list = null;
		longClickedItem = null;
		newEventEvent = null;
		if (savedInstanceState != null) {
			newEventEvent = savedInstanceState.getParcelable(NEW_EVENT_EVENT);
			String stateKey = getStateKey();
			if (stateKey != null) {
				list = savedInstanceState.getParcelableArrayList(stateKey);
				if (list != null) {
					long id = savedInstanceState.getLong(stateKey + "_"
							+ LONG_CLICK_ITEM);
					for (BirthdayItem item : list) {
						if (item.getId() == id) {
							longClickedItem = item;
							break;
						}
					}
				}
			}
		}

		getListView().setOnCreateContextMenuListener(this);
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		String stateKey = getStateKey();
		if (stateKey != null) {
			outState.putParcelableArrayList(stateKey, list);
			if (longClickedItem != null) {
				outState.putLong(stateKey + "_" + LONG_CLICK_ITEM,
						longClickedItem.getId());
			}
		}
		if (newEventEvent != null) {
			outState.putParcelable(NEW_EVENT_EVENT, newEventEvent);
		}
	}

	@Override
	public void onActivityResult(int reqCode, int resultCode, Intent data) {
		super.onActivityResult(reqCode, resultCode, data);
		Log.d(getClass().getName(), MessageFormat.format(
				"onActivityResult: requestCode={0}, resultCode={1}", reqCode,
				resultCode));

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
					showEditBirthdayDialog(longClickedItem);
				}
			}
			break;
		case (Constants.RQ_PICK_SOUND):
			if (resultCode == Activity.RESULT_OK) {
				Bundle b = data.getExtras();
				if (b != null) {
					Uri uri = (Uri) b
							.get(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
					setNotificationSoundAsString(uri);
				}
			}
			break;
		}
	}

	private void setNotificationSoundAsString(Uri uri) {
		String sound = null;
		if (uri != null) {
			sound = uri.toString();
		}
		SharedPreferences prefs = getSharedPreferences(
				Constants.SHARED_PREFS_KEY, Context.MODE_PRIVATE);
		Editor e = prefs.edit();
		e.putString(NOTIFICATION_SOUND, sound);
		e.commit();
	}

	public static String getNotificationSoundAsString(Context context) {
		SharedPreferences prefs = context.getSharedPreferences(
				Constants.SHARED_PREFS_KEY, Context.MODE_PRIVATE);
		return prefs.getString(NOTIFICATION_SOUND, null);
	}

	private void showEditBirthdayDialog(BirthdayItem item) {
		Date birthday = item.getBirthday();
		newEventEvent = (birthday == null) ? new AnnualEvent()
				: new AnnualEvent(birthday);
		showDialog(Constants.DATE_DIALOG_ID);
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		AdapterContextMenuInfo mi = (AdapterContextMenuInfo) menuInfo;
		BirthdayItem item = (BirthdayItem) getListAdapter()
				.getItem(mi.position);
		menu.setHeaderTitle(item.getName());
		if (item.getBirthday() != null) {
			menu.add(Menu.NONE, Constants.MENU_CHANGE_DATE, Menu.NONE,
					Constants.MENU_CHANGE_DATE);
			menu.add(Menu.NONE, Constants.MENU_REMOVE_DATE, Menu.NONE,
					Constants.MENU_REMOVE_DATE);
		}
		if (item.getPrimaryPhoneNumber() != null) {
			menu.add(Menu.NONE, Constants.MENU_DIAL, Menu.NONE,
					Constants.MENU_DIAL);
			String string = getString(Constants.MENU_SEND_SMS, item
					.getPrimaryPhoneNumber());
			menu.add(Menu.NONE, Constants.MENU_SEND_SMS, Menu.NONE, string);
		}
	}

	@Override
	public boolean onContextItemSelected(MenuItem item) {
		AdapterContextMenuInfo mi = (AdapterContextMenuInfo) item.getMenuInfo();
		longClickedItem = (BirthdayItem) getListAdapter().getItem(mi.position);
		switch (item.getItemId()) {
		case Constants.MENU_CHANGE_DATE:
			showEditBirthdayDialog(longClickedItem);
			break;
		case Constants.MENU_REMOVE_DATE:
			longClickedItem.setBirthday(null);
			updateContact(longClickedItem);
			break;
		case Constants.MENU_DIAL:
			Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:"
					+ longClickedItem.getPrimaryPhoneNumber()));
			startActivity(intent);
			break;
		case Constants.MENU_SEND_SMS:
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
		super.onPrepareDialog(id, dialog);
		switch (id) {
		case Constants.DATE_DIALOG_ID:
			updateViewsFromEvent();
			break;
		case Constants.TIME_DIALOG_ID:
			TimePickerDialogHelper.readFromPreferences(this);
			TimePickerDialog picker = (TimePickerDialog) dialog;
			picker.updateTime(TimePickerDialogHelper.hour,
					TimePickerDialogHelper.minute);
			break;
		case Constants.NOTIFICATION_DAYS_DIALOG_ID:
			updateCheckboxes(dialog);
			break;
		case Constants.NEW_CONTACT_ID:
			updateViewsFromEvent();
			break;
		}
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		LayoutInflater factory = null;
		View view = null;
		Dialog dialog = null;
		switch (id) {
		case Constants.DATE_DIALOG_ID:
			newEventCal = Calendar.getInstance();
			factory = LayoutInflater.from(this);
			view = factory.inflate(R.layout.edit_birthday_date, null);
			newEventSpinnerDay = (Spinner) view
					.findViewById(R.id.new_event_day);
			newEventSpinnerMonth = (Spinner) view
					.findViewById(R.id.new_event_month);
			newEventYear = (TextView) view.findViewById(R.id.new_event_year);
			newEventDescr = null;
			dialog = new AlertDialog.Builder(this).setTitle(
					R.string.menu_change_date).setPositiveButton(
					android.R.string.ok, new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog,
								int whichButton) {
							newEventEvent.setYear(checkYear());
							Calendar cal = Calendar.getInstance();
							cal.set(Calendar.YEAR, newEventEvent.getYear());
							cal.set(Calendar.MONTH, newEventEvent.getMonth());
							cal.set(Calendar.DAY_OF_MONTH, newEventEvent
									.getDay());
							longClickedItem.setBirthday(cal.getTime());
							updateContact(longClickedItem);
							newEventEvent = null;
							removeDialog(Constants.DATE_DIALOG_ID);
						}
					}).setNegativeButton(android.R.string.cancel,
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog,
								int whichButton) {
							newEventEvent = null;
							removeDialog(Constants.DATE_DIALOG_ID);
						}
					}).setView(view).create();
			installListeners();
			updateViewsFromEvent();
			break;
		case Constants.TIME_DIALOG_ID:
			TimePickerDialogHelper.readFromPreferences(this);
			dialog = new TimePickerDialog(this, this,
					TimePickerDialogHelper.hour, TimePickerDialogHelper.minute,
					true);
			break;
		case Constants.NOTIFICATION_DAYS_DIALOG_ID:
			view = LayoutInflater.from(this).inflate(
					R.layout.notification_days, null);
			dialog = new AlertDialog.Builder(this).setTitle(
					R.string.menu_set_notification_days).setPositiveButton(
					R.string.alert_dialog_ok,
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
			break;
		case Constants.NEW_CONTACT_ID:
			newEventCal = Calendar.getInstance();
			factory = LayoutInflater.from(this);
			view = factory.inflate(R.layout.new_contact, null);
			newEventDescr = (TextView) view.findViewById(R.id.new_event_descr);
			newEventSpinnerDay = (Spinner) view
					.findViewById(R.id.new_event_day);
			newEventSpinnerMonth = (Spinner) view
					.findViewById(R.id.new_event_month);
			newEventYear = (TextView) view.findViewById(R.id.new_event_year);
			dialog = new AlertDialog.Builder(this).setTitle(
					R.string.new_entry_dialog_title).setPositiveButton(
					android.R.string.ok, new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog,
								int whichButton) {
							newEventEvent.setYear(checkYear());
							newEventEvent.setDescr(newEventDescr.getText()
									.toString());
							addContact(newEventEvent);
							newEventEvent = null;
							removeDialog(Constants.NEW_CONTACT_ID);
						}
					}).setNegativeButton(android.R.string.cancel,
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog,
								int whichButton) {
							newEventEvent = null;
							removeDialog(Constants.NEW_CONTACT_ID);
						}
					}).setView(view).create();
			installListeners();
			updateViewsFromEvent();
			break;
		}
		return dialog;
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
			showDialog(Constants.TIME_DIALOG_ID);
			break;
		case R.id.set_notification_days:
			showDialog(Constants.NOTIFICATION_DAYS_DIALOG_ID);
			break;
		case R.id.set_notification_sound:
			Intent i = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
			i.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT,
					Boolean.FALSE);
			i
					.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT,
							Boolean.TRUE);
			i.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE,
					RingtoneManager.TYPE_ALL);
			i.putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE,
					getString(R.string.menu_set_notification_sound));
			String current = getNotificationSoundAsString(this);
			if (current != null) {
				i.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI, Uri
						.parse(current));
			}
			startActivityForResult(i, Constants.RQ_PICK_SOUND);
			break;
		// case R.id.preferences:
		// Intent i = new Intent(this, Preferences.class);
		// i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		// startActivity(i);
		// break;
		case R.id.new_entry:
			newEventEvent = new AnnualEvent();
			showDialog(Constants.NEW_CONTACT_ID);
			break;
		case R.id.set_date:
			// Intent intent = new Intent(Intent.ACTION_PICK,
			// People.CONTENT_URI);
			Intent intent = new Intent(this, BirthdayNotSetActivity.class);
			startActivityForResult(intent, Constants.RQ_PICK_CONTACT);
			break;
		}
		return true;
	}

	private void updateViewsFromEvent() {
		if (newEventEvent == null) {
			return;
		}
		// Spinner vorbereiten
		createAndSetMonthAdapter();
		createAndSetDayAdapter();
		// Jahres-Textfeld befüllen
		int intYear = newEventEvent.getYear();
		newEventYear.setText(Integer.toString(intYear));
		if (newEventDescr != null) {
			newEventDescr.setText(newEventEvent.getDescr());
		}
	}

	private void installListeners() {
		// Listener registrieren
		newEventSpinnerMonth
				.setOnItemSelectedListener(new OnItemSelectedListener() {

					@Override
					public void onItemSelected(AdapterView<?> parent,
							View view, int position, long id) {
						newEventEvent.setMonth(position);
						newEventSpinnerDay.post(new Runnable() {

							@Override
							public void run() {
								createAndSetDayAdapter();
							}

						});
					}

					@Override
					public void onNothingSelected(AdapterView<?> parent) {
					}
				});
		newEventSpinnerDay
				.setOnItemSelectedListener(new OnItemSelectedListener() {

					@Override
					public void onItemSelected(AdapterView<?> parent,
							View view, int position, long id) {
						newEventEvent.setDay(position + 1);
					}

					@Override
					public void onNothingSelected(AdapterView<?> parent) {
					}
				});
		newEventYear.setOnKeyListener(new OnKeyListener() {

			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				createAndSetDayAdapter();
				return false;
			}
		});
	}

	private void createAndSetMonthAdapter() {
		ArrayAdapter<String> adapterMonth = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item);
		adapterMonth
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		for (int i = newEventCal.getMinimum(Calendar.MONTH); i <= newEventCal
				.getMaximum(Calendar.MONTH); i++) {
			newEventCal.set(Calendar.MONTH, i);
			adapterMonth.add(FORMAT_MONTH_SHORT.format(newEventCal.getTime()));
		}
		newEventSpinnerMonth.setAdapter(adapterMonth);
		newEventSpinnerMonth.setSelection(newEventEvent.getMonth());
	}

	private void createAndSetDayAdapter() {
		ArrayAdapter<String> adapterDay = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item);
		adapterDay
				.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		// Kalender setzen
		int intYear = checkYear();
		newEventCal.set(Calendar.YEAR, intYear);
		newEventCal.set(Calendar.DAY_OF_MONTH, 1);
		newEventCal.set(Calendar.MONTH, newEventEvent.getMonth());
		int max = newEventCal.getActualMaximum(Calendar.DAY_OF_MONTH);
		for (int i = newEventCal.getMinimum(Calendar.DAY_OF_MONTH); i <= max; i++) {
			adapterDay.add(Integer.toString(i));
		}
		if (newEventEvent.getDay() > max) {
			newEventEvent.setDay(max);
		}
		newEventSpinnerDay.setAdapter(adapterDay);
		newEventSpinnerDay.setSelection(newEventEvent.getDay() - 1);
	}

	/**
	 * Aktualisiert einen Kontakt in der Datenbank und anschließend die Listen.
	 * 
	 * @param item
	 *            der zu aktualisierende Kontakt
	 */
	public void updateContact(BirthdayItem item) {
		if (TKBirthdayReminder.isContactsContractPresent) {
			_updateContact_new(item);
		} else {
			_updateContact_old(item);
		}
	}

	private void _updateContact_old(BirthdayItem item) {
		ContentResolver contentResolver = getContentResolver();
		Uri uri = Uri.withAppendedPath(Contacts.People.CONTENT_URI, Long
				.toString(item.getId()));
		// lesen des Notizfeldes
		Cursor c = contentResolver
				.query(uri, new String[] { Contacts.PeopleColumns.NOTES },
						null, null, null);
		if (c != null) {
			if (c.moveToFirst()) {
				String notes = TKDateUtils.getStringFromDate(
						item.getBirthday(), c.getString(0));
				// Aktualisieren des Eintrags
				ContentValues values = new ContentValues();
				values.put(Contacts.PeopleColumns.NOTES, notes);
				contentResolver.update(uri, values, null, null);
			}
			c.close();
			readContacts(true);
		}
	}

	private void _updateContact_new(BirthdayItem item) {
		ContentResolver contentResolver = getContentResolver();
		String id = Long.toString(item.getId());
		Uri uri = Uri.withAppendedPath(Contacts.People.CONTENT_URI, id);
		// lesen des Notizfeldes
		Cursor c = contentResolver
				.query(uri, new String[] { Contacts.PeopleColumns.NOTES },
						null, null, null);
		if (c != null) {
			if (c.moveToFirst()) {
				String notes = TKDateUtils.getStringFromDate(
						item.getBirthday(), c.getString(0));
				// die zu setzenden Werte
				ContentValues values = new ContentValues();
				values.put(ContactsContractWrapper.CommonDataKinds.Note.NOTE,
						notes);
				// Aktualisieren des Eintrags
				uri = ContactsContractWrapper.Data.CONTENT_URI;
				Cursor cc = contentResolver.query(uri, null,
						ContactsContractWrapper.Data.RAW_CONTACT_ID + " = ?",
						new String[] { id }, null);
				if (cc != null) {
					if (cc.moveToNext()) {
						long l = cc
								.getLong(cc
										.getColumnIndex(ContactsContractWrapper.Data.RAW_CONTACT_ID));
						String noteWhere = ContactsContractWrapper.Data.RAW_CONTACT_ID
								+ " = ? AND "
								+ ContactsContractWrapper.Data.MIMETYPE
								+ " = ?";
						String[] noteWhereParams = new String[] {
								id,
								ContactsContractWrapper.CommonDataKinds.Note.CONTENT_ITEM_TYPE };
						if (contentResolver.update(uri, values, noteWhere,
								noteWhereParams) == 0) {
							values
									.put(
											ContactsContractWrapper.Data.MIMETYPE,
											ContactsContractWrapper.CommonDataKinds.Note.CONTENT_ITEM_TYPE);
							values
									.put(
											ContactsContractWrapper.Data.RAW_CONTACT_ID,
											l);
							contentResolver.insert(uri, values);
						}
					}
					cc.close();
				}
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
			Bundle bundleList = bundleExtras.getBundle(Constants.TKBR2);
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

	private void addContact(AnnualEvent event) {
		long id = ContactsList.addGroup(this, "TKBirthdayReminder");
		String name = event.getDescr();
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.YEAR, event.getYear());
		cal.set(Calendar.MONTH, event.getMonth());
		cal.set(Calendar.DAY_OF_MONTH, event.getDay());
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

						AppWidgetManager m = AppWidgetManager
								.getInstance(AbstractListActivity.this);
						if (m != null) {
							int[] appWidgetIds = m
									.getAppWidgetIds(new ComponentName(
											AbstractListActivity.this,
											BirthdayWidget.class));
							if ((appWidgetIds != null)
									&& (appWidgetIds.length > 0)) {
								BirthdayWidget.updateWidgets(
										AbstractListActivity.this, m,
										appWidgetIds);
							}
						}
					}

				});
			}

		});
		thread.start();
	}

	protected abstract ArrayList<BirthdayItem> getProperList(ContactsList cl);

	protected abstract String getStateKey();

	public static int getNotificationDays(Context context) {
		SharedPreferences prefs = context.getSharedPreferences(
				Constants.SHARED_PREFS_KEY, Context.MODE_PRIVATE);
		return prefs.getInt(NOTIFICATION_DAYS, 0);
	}

	// /////////////////////
	// OnTimeSetListener //
	// /////////////////////

	@Override
	public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
		TimePickerDialogHelper.writeToPreferences(this, hourOfDay, minute);
		BootCompleteReceiver.startAlarm(this, true);
	}

	private int checkYear() {
		int intYear = 1980;
		try {
			intYear = Integer.parseInt(newEventYear.getText().toString());
		} catch (Throwable thr) {
		}
		return intYear;
	}
}
