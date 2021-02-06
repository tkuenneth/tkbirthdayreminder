/*
 * TKBirthdayReminder.java
 *
 * TKBirthdayReminder (c) Thomas Künneth 2009 - 2021
 * Alle Rechte beim Autoren. All rights reserved.
 */
package com.thomaskuenneth.android.birthday;

import android.Manifest;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Parcelable;
import android.provider.ContactsContract;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Spinner;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Dies ist die Hauptklasse von TKBirthdayReminder.
 *
 * @author Thomas Künneth
 */
public class TKBirthdayReminder extends AppCompatActivity {

    public static int storedVersionCode;
    public static int currentVersionCode;

    private static final String TAG = TKBirthdayReminder.class.getSimpleName();
    private static final String[] PERMISSIONS = new String[]{Manifest.permission.READ_CONTACTS,
            Manifest.permission.WRITE_CONTACTS,
            Manifest.permission.GET_ACCOUNTS
    };
    private static final String VERSION_CODE = "versionCode";
    private static final String NEW_EVENT_EVENT = "newEventEvent";
    private static final String STATE_KEY = "stateKey";
    private static final String LONG_CLICK_ITEM = "longClickItem";

    private ListView mainList;
    private EditText newEventYear;
    private Spinner newEventSpinnerDay, newEventSpinnerMonth;
    private Calendar newEventCal;
    private AnnualEvent newEventEvent;
    private BirthdayItem longClickedItem;
    private List<BirthdayItem> list;
    private int imageHeight;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        mainList = findViewById(R.id.main_list);
        imageHeight = getImageHeight(getWindowManager());
        list = null;
        longClickedItem = null;
        newEventEvent = null;
        mainList.setOnCreateContextMenuListener(this);
        mainList.setOnItemClickListener((parent, view, position, id) -> {
            BirthdayItem item = (BirthdayItem) mainList.getAdapter().getItem(
                    position);
            Intent i = new Intent(Intent.ACTION_VIEW, Uri.withAppendedPath(
                    ContactsContract.Contacts.CONTENT_URI,
                    Long.toString(item.getId())));
            startActivityForResult(i, Constants.RQ_SHOW_CONTACT);
        });

        if (isNewVersion()) {
            if (savedInstanceState == null) {
                showDialog(Constants.WELCOME_ID);
            }
        } else {
            if (savedInstanceState != null) {
                newEventEvent = savedInstanceState.getParcelable(NEW_EVENT_EVENT);
                list = savedInstanceState.getParcelableArrayList(STATE_KEY);
                if (list != null) {
                    long id = savedInstanceState.getLong(LONG_CLICK_ITEM);
                    for (BirthdayItem item : list) {
                        if (item.getId() == id) {
                            longClickedItem = item;
                            break;
                        }
                    }
                }
            }
            run();
        }
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (list instanceof ArrayList) {
            outState.putParcelableArrayList(STATE_KEY, (ArrayList<? extends Parcelable>) list);
        }
        if (longClickedItem != null) {
            outState.putLong(LONG_CLICK_ITEM,
                    longClickedItem.getId());
        }
        if (newEventEvent != null) {
            outState.putParcelable(NEW_EVENT_EVENT, newEventEvent);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        boolean canRun = true;
        for (int result : grantResults) {
            if (result == PackageManager.PERMISSION_DENIED) {
                canRun = false;
                break;
            }
        }
        if (canRun) {
            run();
        } else {
            finish();
        }
    }

    @Override
    public void onActivityResult(int reqCode,
                                 int resultCode,
                                 Intent data) {
        super.onActivityResult(reqCode, resultCode, data);
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
            case Constants.RQ_PREFERENCES:
            case Constants.RQ_SHOW_CONTACT:
                readContacts(true);
                break;
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        AdapterView.AdapterContextMenuInfo mi = (AdapterView.AdapterContextMenuInfo) menuInfo;
        BirthdayItem item = (BirthdayItem) mainList.getAdapter()
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
        AdapterView.AdapterContextMenuInfo mi = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        longClickedItem = (BirthdayItem) mainList.getAdapter().getItem(mi.position);
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
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
                break;
            case Constants.MENU_SEND_SMS:
                Uri smsUri = Uri.parse("smsto://"
                        + longClickedItem.getPrimaryPhoneNumber());
                Intent sendIntent = new Intent(Intent.ACTION_SENDTO, smsUri);
                sendIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                sendIntent.putExtra("sms_body", "");
                startActivity(sendIntent);
                break;
        }
        return true;
    }

    @Override
    protected void onPrepareDialog(int id, Dialog dialog) {
        super.onPrepareDialog(id, dialog);
        if (id == Constants.DATE_DIALOG_ID) {
            updateViewsFromEvent();
        }
    }

    @SuppressLint("InflateParams")
    @Override
    protected Dialog onCreateDialog(int id) {
        final View view;
        final LayoutInflater factory;
        Dialog dialog = null;
        switch (id) {
            case Constants.WELCOME_ID:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(R.string.legal);
                builder.setIcon(R.mipmap.ic_launcher);
                @SuppressLint("InflateParams") View textView = getLayoutInflater().inflate(R.layout.welcome, null);
                builder.setView(textView);
                if (isNewVersion()) {
                    builder.setPositiveButton(R.string.alert_dialog_continue,
                            (dialog1, which) -> {
                                writeToPreferences();
                                run();
                            });
                    builder.setNegativeButton(R.string.alert_dialog_abort,
                            (dialog12, which) -> finish());
                } else {
                    builder.setPositiveButton(R.string.alert_dialog_ok,
                            (dialog13, which) -> {
                            });
                }
                builder.setCancelable(false);
                return builder.create();

            case Constants.DATE_DIALOG_ID:
                newEventCal = Calendar.getInstance();
                factory = LayoutInflater.from(this);
                view = factory.inflate(R.layout.edit_birthday_date, null);
                newEventSpinnerDay = view.findViewById(R.id.new_event_day);
                newEventSpinnerMonth = view.findViewById(R.id.new_event_month);
                newEventYear = view.findViewById(R.id.new_event_year);
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
        }
        return dialog;
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
            case R.id.legal:
                showDialog(Constants.WELCOME_ID);
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

    public static SharedPreferences getSharedPreferences(Context context) {
        return context.getSharedPreferences(
                Constants.TKBIRTHDAYREMINDER, Context.MODE_PRIVATE);
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

    public static String getStringFromResources(Context context, int resId) {
        return context.getString(resId);
    }

    public static String getStringFromResources(Context context, int resId,
                                                Object... formatArgs) {
        return context.getString(resId, formatArgs);
    }

    public static void updateWidgets(Context context) {
        AppWidgetManager m = AppWidgetManager
                .getInstance(context);
        if (m != null) {
            int[] appWidgetIds = m
                    .getAppWidgetIds(new ComponentName(
                            context,
                            BirthdayWidget.class));
            if ((appWidgetIds != null)
                    && (appWidgetIds.length > 0)) {
                BirthdayWidget.updateWidgets(
                        context, m,
                        appWidgetIds);
            }
        }
    }


    /**
     * Aktualisiert einen Kontakt in der Datenbank und anschließend die Listen.
     *
     * @param item der zu aktualisierende Kontakt
     */
    public void updateContact(BirthdayItem item) {
        ContentResolver contentResolver = getContentResolver();
        String id = Long.toString(item.getId());
        // Log.d(TAG, "updateContact: " + item.getName() + " (" + id + ")");
        // lesen vorhandener Daten
        String[] dataQueryProjection = new String[]{
                ContactsContract.Data.MIMETYPE,
                ContactsContract.CommonDataKinds.Event.TYPE,
                ContactsContract.CommonDataKinds.Event.START_DATE,
                ContactsContract.CommonDataKinds.Note.NOTE,
                ContactsContract.Data._ID};
        String dataQuerySelection = ContactsContract.Data.CONTACT_ID + " = ?";
        String[] rawSelectionArgs = new String[]{id};
        Cursor dataQueryCursor = contentResolver.query(
                ContactsContract.Data.CONTENT_URI, dataQueryProjection,
                dataQuerySelection, rawSelectionArgs, null);
        if (dataQueryCursor != null) {
            while (dataQueryCursor.moveToNext()) {
                String mimeType = dataQueryCursor.getString(0);
                String dataId = dataQueryCursor.getString(4);
                // Event - evtl. der Geburtstag?
                if (ContactsContract.CommonDataKinds.Event.CONTENT_ITEM_TYPE
                        .equals(mimeType)) {
                    int type = dataQueryCursor.getInt(1);
                    if (ContactsContract.CommonDataKinds.Event.TYPE_BIRTHDAY == type) {
                        /* String gebdt = */
                        dataQueryCursor.getString(2);
                        // Log.d(TAG, "   ---> found birthday: " + gebdt);

                        String where = ContactsContract.Data.CONTACT_ID
                                + " = ? AND " + ContactsContract.Data._ID
                                + " = ? AND " + ContactsContract.Data.MIMETYPE
                                + " = ?";
                        String[] selectionArgs = new String[]{
                                id,
                                dataId,
                                ContactsContract.CommonDataKinds.Event.CONTENT_ITEM_TYPE};
                        /* int rowsDeleted = */
                        contentResolver.delete(
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
                        Date d = Utils.getDateFromString(note);
                        if (d != null) {
                            // Log.d(TAG, "   ---> extracted date: " +
                            // d.toString());
                            note = Utils.removeBirthdayFromString(note);
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
                            String[] selectionArgs = new String[]{
                                    id,
                                    dataId,
                                    ContactsContract.CommonDataKinds.Note.CONTENT_ITEM_TYPE};
                            /* int rowsUpdated = */
                            contentResolver.update(
                                    ContactsContract.Data.CONTENT_URI, values,
                                    where, selectionArgs);
                            // Log.d(TAG, "   ---> updating note " + dataId + ": "
                            // + rowsUpdated + " row(s) affected");
                        }
                    }
                }
            }
            dataQueryCursor.close();
        }

        // Geburtstag einfügen
        Date birthday = item.getBirthday();
        if (birthday != null) {
            // Strings für die Suche nach RawContacts
            String[] rawProjection = new String[]{ContactsContract.RawContacts._ID};
            String rawSelection = ContactsContract.RawContacts.CONTACT_ID + " = ?";
            // Werte für Tabellenzeile vorbereiten
            ContentValues values = new ContentValues();
            values.put(ContactsContract.CommonDataKinds.Event.START_DATE,
                    Utils.getDateAsStringYYYY_MM_DD(birthday));
            values.put(ContactsContract.Data.MIMETYPE,
                    ContactsContract.CommonDataKinds.Event.CONTENT_ITEM_TYPE);
            values.put(ContactsContract.CommonDataKinds.Event.TYPE,
                    ContactsContract.CommonDataKinds.Event.TYPE_BIRTHDAY);
            // alle RawContacts befüllen
            Cursor c = contentResolver.query(ContactsContract.RawContacts.CONTENT_URI,
                    rawProjection, rawSelection, rawSelectionArgs, null);
            if (c != null) {
                while (c.moveToNext()) {
                    String rawContactId = c.getString(0);
                    values.put(
                            ContactsContract.CommonDataKinds.Event.RAW_CONTACT_ID,
                            rawContactId);
                    /* Uri uri = */
                    contentResolver.insert(
                            ContactsContract.Data.CONTENT_URI, values);
//				Log.d(TAG, "   ---> inserting birthday for raw contact "
//						+ rawContactId
//						+ ((uri == null) ? " failed" : " succeeded"));
                }
                c.close();
            }
        }
        requestSync();
        readContacts(true);
    }

    private boolean hasPermission(String permission) {
        return checkSelfPermission(permission) == PackageManager.PERMISSION_GRANTED;
    }

    private void run() {
        boolean canRun = true;
        for (String p : PERMISSIONS) {
            if (!hasPermission(p)) {
                requestPermissions(PERMISSIONS, 0);
                canRun = false;
                break;
            }
        }
        if (canRun) {
            BootCompleteReceiver.startAlarm(this, true);
            readContacts(false);
        }
    }

    /**
     * Prüft, ob seit dem letzten Start eine neue Version installiert wurde.
     *
     * @return liefert {@code true}, wenn seit dem letzten Start eine neue
     * Version installiert wurde; sonst {@code false}
     */
    private boolean isNewVersion() {
        readFromPreferences();
        boolean newVersion = storedVersionCode < currentVersionCode;
        Log.d(TAG, "newVersion: " + newVersion);
        return newVersion;
    }

    /**
     * Belegt die beiden Variablen storedVersionCode (wird aus den shared
     * preferences ausgelsen) und currentVersionCode (wird aus PackageInfo
     * ermittelt).
     */
    private void readFromPreferences() {
        SharedPreferences prefs = getSharedPreferences(
                Constants.TKBIRTHDAYREMINDER, Context.MODE_PRIVATE);
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

    private void writeToPreferences() {
        SharedPreferences.Editor editor = getSharedPreferences(this).edit();
        editor.putInt(VERSION_CODE, currentVersionCode);
        editor.apply();
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
        if (intYear != Utils.INVISIBLE_YEAR) strYear = Integer.toString(intYear);
        newEventYear.setText(strYear);
    }

    private void installListeners() {
        newEventSpinnerMonth
                .setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

                    @Override
                    public void onItemSelected(AdapterView<?> parent,
                                               View view, int position, long id) {
                        newEventEvent.setMonth(position);
                        newEventSpinnerDay.post(() -> createAndSetDayAdapter());
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                    }
                });
        newEventSpinnerDay
                .setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

                    @Override
                    public void onItemSelected(AdapterView<?> parent,
                                               View view, int position, long id) {
                        newEventEvent.setDay(position + 1);
                    }

                    @Override
                    public void onNothingSelected(AdapterView<?> parent) {
                    }
                });

        newEventYear.addTextChangedListener(new TextWatcher() {

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }

            @Override
            public void afterTextChanged(Editable s) {
                createAndSetDayAdapter();
            }
        });
    }

    private void createAndSetMonthAdapter() {
        final DateFormat formatMonthShort = new SimpleDateFormat(
                "MMM", Locale.getDefault());
        ArrayAdapter<String> adapterMonth = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item);
        adapterMonth
                .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        for (int i = newEventCal.getMinimum(Calendar.MONTH); i <= newEventCal
                .getMaximum(Calendar.MONTH); i++) {
            newEventCal.set(Calendar.MONTH, i);
            adapterMonth.add(formatMonthShort.format(newEventCal.getTime()));
        }
        newEventSpinnerMonth.setAdapter(adapterMonth);
        newEventSpinnerMonth.setSelection(newEventEvent.getMonth());
    }

    private void createAndSetDayAdapter() {
        ArrayAdapter<String> adapterDay = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item);
        adapterDay
                .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
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
        Account[] accounts = null;
        try {
            accounts = am.getAccounts();
        } catch (SecurityException e) {
            Log.e(TAG, "requestSync()", e);
        }
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

    private void setList(List<BirthdayItem> list) {
        this.list = list;
        BirthdayItemListAdapter myListAdapter = new BirthdayItemListAdapter(
                this, list, imageHeight);
        mainList.setAdapter(myListAdapter);
    }

    /**
     * Liest die Kontakte neu ein.
     *
     * @param forceRead Ignoriert evtl. bereits gelesene Kontakte
     */
    protected void readContacts(final boolean forceRead) {
        final Handler h = new Handler(Looper.myLooper());
        Thread thread = new Thread(() -> {
            Looper.prepare();
            if ((list == null) || forceRead) {
                ContactsList cl = new ContactsList(
                        TKBirthdayReminder.this);
                list = cl.getMainList();
            }
            h.post(() -> {
                setList(list);
                updateWidgets(TKBirthdayReminder.this);
            });
        });
        thread.start();
    }

    private int checkYear() {
        int intYear = Utils.INVISIBLE_YEAR;
        String s = newEventYear.getText().toString();
        if (s.length() > 0) {
            try {
                intYear = Integer.parseInt(s);
            } catch (Throwable thr) {
                Log.e(TAG, "checkYear()", thr);
            }
        }
        return intYear;
    }

    private void showEditBirthdayDialog(BirthdayItem item) {
        Date birthday = item.getBirthday();
        newEventEvent = (birthday == null) ? new AnnualEvent()
                : new AnnualEvent(birthday);
        showDialog(Constants.DATE_DIALOG_ID);
    }

}