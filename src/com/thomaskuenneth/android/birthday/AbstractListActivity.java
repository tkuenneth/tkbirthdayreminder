/*
 * AbstractListActivity.java
 * 
 * TKBirthdayReminder (c) Thomas Künneth 2009 - 2012
 * Alle Rechte beim Autoren. All rights reserved.
 */
package com.thomaskuenneth.android.birthday;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;

import android.accounts.Account;
import android.accounts.AccountManager;
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
import android.provider.ContactsContract;
import android.provider.ContactsContract.RawContacts;
import android.util.DisplayMetrics;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnKeyListener;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;

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

//	private static final String TAG = AbstractListActivity.class
//			.getSimpleName();

	private static final String NEW_EVENT_EVENT = "newEventEvent";
	private static final String LONG_CLICK_ITEM = "longClickItem";

	// Neuer Kontakt
	private static final DateFormat FORMAT_MONTH_SHORT = new SimpleDateFormat(
			"MMM");
	private TextView newEventYear;
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

	private int imageHeight;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);

		imageHeight = getImageHeight(getWindowManager());

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
		super.onCreate(savedInstanceState);

		getListView().setOnCreateContextMenuListener(this);
		getListView().setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				BirthdayItem item = (BirthdayItem) getListAdapter().getItem(
						position);
				Intent i = new Intent(Intent.ACTION_VIEW, Uri.withAppendedPath(
						ContactsContract.Contacts.CONTENT_URI,
						Long.toString(item.getId())));
				startActivityForResult(i, Constants.RQ_SHOW_CONTACT);
			}
		});
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
		// Log.d(TAG, MessageFormat.format(
		// "onActivityResult: requestCode={0}, resultCode={1}", reqCode,
		// resultCode));

		switch (reqCode) {
		case (Constants.RQ_PICK_CONTACT):
			if (resultCode == Activity.RESULT_OK) {
				Uri contactData = data.getData();
				Cursor c = managedQuery(contactData, null, null, null, null);
				if (c.moveToFirst()) {
					longClickedItem = ContactsList.createItemFromCursor(
							getContentResolver(), c);
					showEditBirthdayDialog(longClickedItem);
					// longClickedItem.setBirthday(new Date());
					// updateContact(longClickedItem);
				}
				// c.close();
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
		case Constants.RQ_PREFERENCES:
			readContacts(true);
			break;
		case Constants.RQ_SHOW_CONTACT:
			readContacts(true);
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
			String string = getString(Constants.MENU_SEND_SMS,
					item.getPrimaryPhoneNumber());
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
			dialog = new AlertDialog.Builder(this)
					.setTitle(R.string.menu_change_date)
					.setPositiveButton(android.R.string.ok,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {
									newEventEvent.setYear(checkYear());
									Calendar cal = Calendar.getInstance();
									cal.set(Calendar.YEAR,
											newEventEvent.getYear());
									cal.set(Calendar.MONTH,
											newEventEvent.getMonth());
									cal.set(Calendar.DAY_OF_MONTH,
											newEventEvent.getDay());
									if (longClickedItem != null) {
										longClickedItem.setBirthday(cal
												.getTime());
										updateContact(longClickedItem);
									}
									newEventEvent = null;
									removeDialog(Constants.DATE_DIALOG_ID);
								}
							})
					.setNegativeButton(android.R.string.cancel,
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
			dialog = new AlertDialog.Builder(this)
					.setTitle(R.string.menu_set_notification_days)
					.setPositiveButton(R.string.alert_dialog_ok,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {
									updateNotificationDays((AlertDialog) dialog);
								}
							})
					.setNegativeButton(R.string.alert_dialog_cancel,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {
								}
							}).setView(view).create();
			break;
		}
		return dialog;
	}

	// Optionsmenü

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
			showDialog(Constants.TIME_DIALOG_ID);
			break;
		case R.id.set_notification_days:
			showDialog(Constants.NOTIFICATION_DAYS_DIALOG_ID);
			break;
		case R.id.set_notification_sound:
			Intent i = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
			i.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT,
					Boolean.FALSE);
			i.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, Boolean.TRUE);
			i.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE,
					RingtoneManager.TYPE_ALL);
			i.putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE,
					getString(R.string.menu_set_notification_sound));
			String current = getNotificationSoundAsString(this);
			if (current != null) {
				i.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI,
						Uri.parse(current));
			}
			startActivityForResult(i, Constants.RQ_PICK_SOUND);
			break;
		case R.id.preferences:
			Intent iPrefs = new Intent(this, PreferencesActivity.class);
			startActivityForResult(iPrefs, Constants.RQ_PREFERENCES);
			break;
		case R.id.new_entry:
			Intent intentContact = new Intent(
					ContactsContract.Intents.Insert.ACTION,
					ContactsContract.Contacts.CONTENT_URI);
			intentContact.putExtra("finishActivityOnSaveCompleted", true);
			startActivityForResult(intentContact, Constants.RQ_PICK_CONTACT);
			break;
		case R.id.set_date:
			Intent intent = new Intent(Intent.ACTION_PICK,
					ContactsContract.Contacts.CONTENT_URI);
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
		String strYear = "";
		int intYear = newEventEvent.getYear();
		if (intYear != TKDateUtils.INVISIBLE_YEAR) {
			strYear = Integer.toString(intYear);
		}
		newEventYear.setText(strYear);
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

	private void requestSync() {
		AccountManager am = AccountManager.get(this);
		Account[] accounts = am.getAccounts();

		if (accounts != null) {
			int i = 0;
			for (Account account : accounts) {
				int isSyncable = ContentResolver.getIsSyncable(account,
						ContactsContract.AUTHORITY);

				if (isSyncable > 0) {
					Bundle extras = new Bundle();
					extras.putBoolean(
							ContentResolver.SYNC_EXTRAS_OVERRIDE_TOO_MANY_DELETIONS,
							true);
					extras.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
					extras.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED,
							true);
					extras.putBoolean(ContentResolver.SYNC_EXTRAS_UPLOAD, true);
					ContentResolver.requestSync(accounts[i++],
							ContactsContract.AUTHORITY, extras);
				}
			}
		}
	}

	/**
	 * Aktualisiert einen Kontakt in der Datenbank und anschließend die Listen.
	 * 
	 * @param item
	 *            der zu aktualisierende Kontakt
	 */
	public void updateContact(BirthdayItem item) {
		ContentResolver contentResolver = getContentResolver();

		String id = Long.toString(item.getId());
		// Log.d(TAG, "updateContact: " + item.getName() + " (" + id + ")");
		// lesen vorhandener Daten
		String[] dataQueryProjection = new String[] {
				ContactsContract.Data.MIMETYPE,
				ContactsContract.CommonDataKinds.Event.TYPE,
				ContactsContract.CommonDataKinds.Event.START_DATE,
				ContactsContract.CommonDataKinds.Note.NOTE,
				ContactsContract.Data._ID };
		String dataQuerySelection = ContactsContract.Data.CONTACT_ID + " = ?";
		String[] rawSelectionArgs = new String[] { id };
		String[] dataQuerySelectionArgs = rawSelectionArgs;
		Cursor dataQueryCursor = contentResolver.query(
				ContactsContract.Data.CONTENT_URI, dataQueryProjection,
				dataQuerySelection, dataQuerySelectionArgs, null);
		while (dataQueryCursor.moveToNext()) {
			String mimeType = dataQueryCursor.getString(0);
			String dataId = dataQueryCursor.getString(4);
			// Event - evtl. der Geburtstag?
			if (ContactsContract.CommonDataKinds.Event.CONTENT_ITEM_TYPE
					.equals(mimeType)) {
				int type = dataQueryCursor.getInt(1);
				if (ContactsContract.CommonDataKinds.Event.TYPE_BIRTHDAY == type) {
					/* String gebdt = */ dataQueryCursor.getString(2);
					// Log.d(TAG, "   ---> found birthday: " + gebdt);

					String where = ContactsContract.Data.CONTACT_ID
							+ " = ? AND " + ContactsContract.Data._ID
							+ " = ? AND " + ContactsContract.Data.MIMETYPE
							+ " = ?";
					String[] selectionArgs = new String[] {
							id,
							dataId,
							ContactsContract.CommonDataKinds.Event.CONTENT_ITEM_TYPE };
					/* int rowsDeleted = */ contentResolver.delete(
							ContactsContract.Data.CONTENT_URI, where,
							selectionArgs);
					// Log.d(TAG, "   ---> deleting birthday " + dataId + ": "
					// + rowsDeleted + " row(s) affected");
				}
			}
			// oder ein Notizfeld?
			else if (ContactsContract.CommonDataKinds.Note.CONTENT_ITEM_TYPE
					.equals(mimeType)) {
				String note = dataQueryCursor.getString(3);
				// Log.d(TAG, "   ---> found note: " + note + " (_ID = " +
				// dataId
				// + ")");
				// Notiz aktualisieren
				if ((note != null) && (note.length() >= 17)) {
					// Birthday=yyyymmdd enthält 17 Zeichen
					Date d = TKDateUtils.getDateFromString(note);
					if (d != null) {
						// Log.d(TAG, "   ---> extracted date: " +
						// d.toString());
						// Datum aus dem Notizfeld entfernen
						note = TKDateUtils.getStringFromDate(null, note);
						ContentValues values = new ContentValues();
						values.put(ContactsContract.CommonDataKinds.Note.NOTE,
								note);
						values.put(
								ContactsContract.Data.MIMETYPE,
								ContactsContract.CommonDataKinds.Note.CONTENT_ITEM_TYPE);
						String where = ContactsContract.Data.CONTACT_ID
								+ " = ? AND " + ContactsContract.Data._ID
								+ " = ? AND " + ContactsContract.Data.MIMETYPE
								+ " = ?";
						String[] selectionArgs = new String[] {
								id,
								dataId,
								ContactsContract.CommonDataKinds.Note.CONTENT_ITEM_TYPE };
						/* int rowsUpdated = */ contentResolver.update(
								ContactsContract.Data.CONTENT_URI, values,
								where, selectionArgs);
						// Log.d(TAG, "   ---> updating note " + dataId + ": "
						// + rowsUpdated + " row(s) affected");
					}
				}
			}
		}
		dataQueryCursor.close();

		// Geburtstag einfügen
		Date birthday = item.getBirthday();
		if (birthday != null) {
			// Strings für die Suche nach RawContacts
			String[] rawProjection = new String[] { RawContacts._ID };
			String rawSelection = RawContacts.CONTACT_ID + " = ?";
			// Werte für Tabellenzeile vorbereiten
			ContentValues values = new ContentValues();
			values.put(ContactsContract.CommonDataKinds.Event.START_DATE,
					TKDateUtils.getDateAsStringYYYY_MM_DD(birthday));
			values.put(ContactsContract.Data.MIMETYPE,
					ContactsContract.CommonDataKinds.Event.CONTENT_ITEM_TYPE);
			values.put(ContactsContract.CommonDataKinds.Event.TYPE,
					ContactsContract.CommonDataKinds.Event.TYPE_BIRTHDAY);
			// alle RawContacts befüllen
			Cursor c = contentResolver.query(RawContacts.CONTENT_URI,
					rawProjection, rawSelection, rawSelectionArgs, null);
			while (c.moveToNext()) {
				String rawContactId = c.getString(0);
				values.put(
						ContactsContract.CommonDataKinds.Event.RAW_CONTACT_ID,
						rawContactId);
				/* Uri uri = */ contentResolver.insert(
						ContactsContract.Data.CONTENT_URI, values);
//				Log.d(TAG, "   ---> inserting birthday for raw contact "
//						+ rawContactId
//						+ ((uri == null) ? " failed" : " succeeded"));
			}
			c.close();
		}
		requestSync();
		readContacts(true);
	}

	public void setList(ArrayList<BirthdayItem> list) {
		this.list = list;
		BirthdayItemListAdapter myListAdapter = new BirthdayItemListAdapter(
				this, list, imageHeight);
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
		int intYear = TKDateUtils.INVISIBLE_YEAR;
		try {
			intYear = Integer.parseInt(newEventYear.getText().toString());
		} catch (Throwable thr) {
		}
		return intYear;
	}

	public static int getImageHeight(WindowManager wm) {
		DisplayMetrics outMetrics = new DisplayMetrics();
		wm.getDefaultDisplay().getMetrics(outMetrics);
		if (outMetrics.densityDpi <= DisplayMetrics.DENSITY_LOW) {
			return 32;
		} else if (outMetrics.densityDpi <= DisplayMetrics.DENSITY_MEDIUM) {
			return 48;
		} else if (outMetrics.densityDpi <= DisplayMetrics.DENSITY_HIGH) {
			return 96;
		} else if (outMetrics.densityDpi <= DisplayMetrics.DENSITY_XHIGH) {
			return 128;
		} else {
			return 144;
		}
	}
}
