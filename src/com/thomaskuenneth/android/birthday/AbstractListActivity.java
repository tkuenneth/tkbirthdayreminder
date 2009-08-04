/**
 * AbstractListActivity.java
 * 
 * TKBirthdayReminder (c) Thomas Künneth 2009
 * 
 * Alle Rechte beim Autoren. All rights reserved.
 */
package com.thomaskuenneth.android.birthday;

import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.app.DatePickerDialog.OnDateSetListener;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Contacts;
import android.provider.Contacts.People;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.BaseAdapter;
import android.widget.DatePicker;
import android.widget.AdapterView.AdapterContextMenuInfo;

/**
 * Diese abstrakte Basisklasse stellt die Kernunktionalität der beiden
 * Listenansichten zur Verfügung. Die Methoden des {@link Comparator} Interfaces
 * müssen in den abgeleiteten Klassen implementiert werden.
 * <p>
 * 
 * @see BirthdaySetActivity
 * @see BirthdayNotSetActivity
 * @author Thomas Künneth
 * 
 */
public abstract class AbstractListActivity extends ListActivity implements
		OnDateSetListener {

	/**
	 * IDs für Menüeinträge.
	 */
	private static final int MENU_SET_DATE = R.string.menu_set_date;
	private static final int MENU_CHANGE_DATE = R.string.menu_change_date;
	private static final int MENU_REMOVE_DATE = R.string.menu_remove_date;
	private static final int MENU_DIAL = R.string.menu_dial;
	private static final int MENU_SEND_SMS = R.string.menu_send_sms;

	/**
	 * ID für den Datumsauswahldialog.
	 */
	private static final int DATE_DIALOG_ID = 0;

	private List<BirthdayItem> list;

	/**
	 * Listenelement, das lange angeklickt wurde.
	 */
	private BirthdayItem longClickedItem;

	public AbstractListActivity(List<BirthdayItem> list) {
		super();
		this.list = list;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
		setListAdapter(new MyListAdapter(getBaseContext(), list));
		getListView().setOnCreateContextMenuListener(this);
		ContactsList.addListener(this);
		notifyDataSetChanged();
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		ContactsList.removeListener(this);
	}

	@Override
	public void onCreateContextMenu(ContextMenu menu, View v,
			ContextMenuInfo menuInfo) {
		AdapterContextMenuInfo mi = (AdapterContextMenuInfo) menuInfo;
		BirthdayItem item = (BirthdayItem) getListAdapter()
				.getItem(mi.position);
		menu.setHeaderTitle(item.getName());
		if (item.getBirthday() == null) {
			menu.add(Menu.NONE, MENU_SET_DATE, Menu.NONE, MENU_SET_DATE);
		} else {
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
		case MENU_SET_DATE:
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

	public synchronized void notifyDataSetChanged() {
		BaseAdapter ba = (BaseAdapter) getListAdapter();
		if (ba != null) {
			ba.notifyDataSetInvalidated();
		}
	}

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
		}
		super.onPrepareDialog(id, dialog);
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		if (id == DATE_DIALOG_ID) {
			Calendar cal = new GregorianCalendar();
			Date birthday = longClickedItem.getBirthday();
			if (birthday != null) {
				cal.setTime(birthday);
			}
			return new DatePickerDialog(this, this, cal
					.get(GregorianCalendar.YEAR), cal
					.get(GregorianCalendar.MONTH), cal
					.get(GregorianCalendar.DAY_OF_MONTH));
		}
		return null;
	}

	/**
	 * Aktualisiert einen Kontakt in der Datenbank und anschließend die Listen
	 * aller Instanzen.
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
		}
		Handler h = new Handler(Looper.getMainLooper());
		h.post(new Runnable() {

			@Override
			public void run() {
				setProgressBarIndeterminateVisibility(true);
			}

		});
		ContactsList.readContacts(AbstractListActivity.this, new Runnable() {

			@Override
			public void run() {
				setProgressBarIndeterminateVisibility(false);
			}

		});
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
}
