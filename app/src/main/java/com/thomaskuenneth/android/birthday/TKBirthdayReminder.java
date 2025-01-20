/*
 * TKBirthdayReminder.java
 *
 * TKBirthdayReminder (c) Thomas Künneth 2009 - 2025
 * All rights reserved.
 */
package com.thomaskuenneth.android.birthday;

import android.Manifest;
import android.accounts.Account;
import android.accounts.AccountManager;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.provider.Settings;
import android.text.Editable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextWatcher;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.DisplayMetrics;
import android.view.ContextMenu;
import android.view.DisplayCutout;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowInsets;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.window.core.layout.WindowSizeClass;
import androidx.window.core.layout.WindowWidthSizeClass;
import androidx.window.layout.WindowMetrics;
import androidx.window.layout.WindowMetricsCalculator;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class TKBirthdayReminder extends AppCompatActivity {

    private static final String TAG = TKBirthdayReminder.class.getSimpleName();
    private static final String[] PERMISSIONS = new String[]{Manifest.permission.READ_CONTACTS,
            Manifest.permission.WRITE_CONTACTS,
            Manifest.permission.GET_ACCOUNTS
    };
    private static final String NEW_EVENT_EVENT = "newEventEvent";
    private static final String STATE_KEY = "stateKey";
    private static final String LONG_CLICK_ITEM = "longClickItem";

    private static final int APP_VERSION = BuildConfig.VERSION_CODE;
    private static final String HIDE_MESSAGE_KEY = "hideMessageAppVersion";
    private static final String HIDE_MESSAGE_KEY_ALARMS = "hideMessageExactAlarms";

    private static final int DATE_DIALOG_ID = 3;

    private static final int RQ_PICK_CONTACT = 0x2311;
    private static final int RQ_PREFERENCES = 0x0606;
    private static final int RQ_SHOW_CONTACT = 0x0103;

    private static final int MENU_CHANGE_DATE = R.string.menu_change_date;
    private static final int MENU_REMOVE_DATE = R.string.menu_remove_date;
    private static final int MENU_DIAL = R.string.menu_dial;
    private static final int MENU_SEND_SMS = R.string.menu_send_sms;

    private RecyclerView birthdaysList;
    private BirthdayItemListAdapter birthdaysListAdapter;
    private EditText newEventYear;
    private Spinner newEventSpinnerDay, newEventSpinnerMonth;
    private Calendar newEventCal;
    private AnnualEvent newEventEvent;
    private BirthdayItem longClickedItem;
    private List<BirthdayItem> list;
    private boolean showList;

    private final HashMap<String, Boolean> accounts;

    public TKBirthdayReminder() {
        super();
        accounts = new HashMap<>();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        EdgeToEdge.enable(this);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        applyDisplayCutout();
        Utils.configureActionBar(this);
        findViewById(R.id.requestPermissions).setOnClickListener((view) -> {
            requestPermissions(PERMISSIONS, 0);
        });
        list = null;
        longClickedItem = null;
        newEventEvent = null;
        birthdaysList = findViewById(R.id.birthdaysList);
        registerForContextMenu(birthdaysList);

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
    }

    private void applyDisplayCutout() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            findViewById(R.id.top_level).setOnApplyWindowInsetsListener(new View.OnApplyWindowInsetsListener() {
                @NonNull
                @Override
                public WindowInsets onApplyWindowInsets(@NonNull View view, @NonNull WindowInsets insets) {
                    DisplayCutout displayCutout = insets.getDisplayCutout();
                    if (displayCutout != null) {
                        int left = displayCutout.getSafeInsetLeft();
                        int right = displayCutout.getSafeInsetRight();
                        view.setPadding(left, 0, right, 0);
                    }
                    return insets;
                }
            });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        DisplayMetrics metrics = getResources().getDisplayMetrics();
        WindowMetrics windowMetrics = WindowMetricsCalculator.getOrCreate().computeCurrentWindowMetrics(this);
        float widthInDp = windowMetrics.getBounds().width() / metrics.density;
        float heightInDp = windowMetrics.getBounds().height() / metrics.density;
        WindowSizeClass windowSizeClass = WindowSizeClass.compute(widthInDp, heightInDp);
        if (windowSizeClass.getWindowWidthSizeClass().equals(WindowWidthSizeClass.COMPACT)) {
            showList = true;
            birthdaysList.setLayoutManager(new LinearLayoutManager(this));
        } else {
            showList = false;
            birthdaysList.setLayoutManager(new GridLayoutManager(this,
                    windowSizeClass.getWindowWidthSizeClass().equals(WindowWidthSizeClass.EXPANDED)
                            && PreferenceManager
                            .getDefaultSharedPreferences(this).getBoolean("show_three_columns", true)
                            ? 3 : 2));
        }
        run();
        updateUI();
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
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        run();
    }

    @Override
    public void onActivityResult(int reqCode,
                                 int resultCode,
                                 Intent data) {
        super.onActivityResult(reqCode, resultCode, data);
        switch (reqCode) {
            case (RQ_PICK_CONTACT):
                if (resultCode == Activity.RESULT_OK) {
                    Uri contactData = data.getData();
                    if (contactData != null) {
                        Cursor c = getContentResolver().query(contactData, null, null, null, null);
                        if (c != null) {
                            if (c.moveToFirst()) {
                                longClickedItem = ContactsList.createItemFromCursor(
                                        getContentResolver(), c);
                                showEditBirthdayDialog(longClickedItem);
                            }
                            Utils.closeCursorCatchThrowable(c);
                        }
                    }
                }
                break;
            case RQ_PREFERENCES:
            case RQ_SHOW_CONTACT:
                readContacts(true);
                break;
        }
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
                                    ContextMenu.ContextMenuInfo menuInfo) {
        BirthdayItem item = birthdaysListAdapter.getLastLongClicked();
        if (item == null) return;
        menu.setHeaderTitle(item.getName());
        if (item.getBirthday() != null) {
            menu.add(Menu.NONE, MENU_CHANGE_DATE, Menu.NONE,
                    MENU_CHANGE_DATE);
            menu.add(Menu.NONE, MENU_REMOVE_DATE, Menu.NONE,
                    MENU_REMOVE_DATE);
        }
        if (getPackageManager().hasSystemFeature(PackageManager.FEATURE_TELEPHONY)) {
            if (item.getPrimaryPhoneNumber() != null) {
                menu.add(Menu.NONE, MENU_DIAL, Menu.NONE,
                        MENU_DIAL);
                String string = getString(MENU_SEND_SMS,
                        item.getPrimaryPhoneNumber());
                menu.add(Menu.NONE, MENU_SEND_SMS, Menu.NONE, string);
            }
        }
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        longClickedItem = birthdaysListAdapter.getLastLongClicked();
        switch (item.getItemId()) {
            case MENU_CHANGE_DATE -> showEditBirthdayDialog(longClickedItem);
            case MENU_REMOVE_DATE -> {
                longClickedItem.setBirthday(null);
                updateContact(longClickedItem);
            }
            case MENU_DIAL -> {
                Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:"
                        + longClickedItem.getPrimaryPhoneNumber()));
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intent);
            }
            case MENU_SEND_SMS -> {
                Uri smsUri = Uri.parse("smsto://"
                        + longClickedItem.getPrimaryPhoneNumber());
                Intent sendIntent = new Intent(Intent.ACTION_SENDTO, smsUri);
                sendIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                sendIntent.putExtra("sms_body", "");
                startActivity(sendIntent);
            }
        }
        return true;
    }

    @Override
    protected void onPrepareDialog(int id, Dialog dialog) {
        super.onPrepareDialog(id, dialog);
        if (id == DATE_DIALOG_ID) {
            updateViewsFromEvent();
        }
    }

    @SuppressLint("InflateParams")
    @Override
    protected Dialog onCreateDialog(int id) {
        final View view;
        final LayoutInflater factory;
        Dialog dialog = null;
        if (id == DATE_DIALOG_ID) {
            newEventCal = Calendar.getInstance();
            factory = LayoutInflater.from(this);
            view = factory.inflate(R.layout.edit_birthday_date, null);
            newEventSpinnerDay = view.findViewById(R.id.new_event_day);
            newEventSpinnerMonth = view.findViewById(R.id.new_event_month);
            newEventYear = view.findViewById(R.id.new_event_year);
            dialog = new AlertDialog.Builder(this)
                    .setTitle(R.string.menu_change_date)
                    .setPositiveButton(android.R.string.ok,
                            (dialog15, whichButton) -> {
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
                                removeDialog(DATE_DIALOG_ID);
                            })
                    .setNegativeButton(android.R.string.cancel,
                            (dialog14, whichButton) -> {
                                newEventEvent = null;
                                removeDialog(DATE_DIALOG_ID);
                            }).setView(view).create();
            installListeners();
            updateViewsFromEvent();
        }
        return dialog;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        menu.findItem(R.id.set_date).setVisible(hasRequiredPermissions());
        menu.findItem(R.id.new_entry).setVisible(hasRequiredPermissions());
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        Menu accountsMenu = menu.findItem(R.id.accounts).getSubMenu();
        if (accountsMenu != null) {
            accountsMenu.clear();
            accounts.forEach((accountName, isChecked) -> {
                MenuItem accountNameMenuItem = accountsMenu.add(accountName);
                accountNameMenuItem.setCheckable(true);
                accountNameMenuItem.setChecked(isChecked);
                accountNameMenuItem.setOnMenuItemClickListener(item -> {
                    boolean checked = !item.isChecked();
                    accounts.put(accountName, checked);
                    updatePreferences(accountName, checked);
                    filterList();
                    return true;
                });
            });
        }
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.preferences) {
            Intent iPrefs = new Intent(this, PreferencesActivity.class);
            startActivityForResult(iPrefs, RQ_PREFERENCES);
        } else if (item.getItemId() == R.id.new_entry) {
            Intent intentContact = new Intent(
                    ContactsContract.Intents.Insert.ACTION,
                    ContactsContract.Contacts.CONTENT_URI);
            intentContact.putExtra("finishActivityOnSaveCompleted", true);
            startActivityForResult(intentContact, RQ_PICK_CONTACT);
        } else if (item.getItemId() == R.id.set_date) {
            Intent intent = new Intent(Intent.ACTION_PICK,
                    ContactsContract.Contacts.CONTENT_URI);
            startActivityForResult(intent, RQ_PICK_CONTACT);
        } else if (item.getItemId() == R.id.legal) {
            Intent intent = new Intent(this, LegalActivity.class);
            startActivity(intent);
        }
        return true;
    }

    public static SharedPreferences getSharedPreferences(Context context) {
        return context.getSharedPreferences(
                Constants.TKBIRTHDAYREMINDER, Context.MODE_PRIVATE);
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

    public void updateContact(BirthdayItem item) {
        ContentResolver contentResolver = getContentResolver();
        String id = Long.toString(item.getId());
        Utils.logDebug(TAG, "updateContact: " + item.getName() + " (" + id + ")");
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
                // Event - maybe the birthday?
                if (ContactsContract.CommonDataKinds.Event.CONTENT_ITEM_TYPE
                        .equals(mimeType)) {
                    int type = dataQueryCursor.getInt(1);
                    if (ContactsContract.CommonDataKinds.Event.TYPE_BIRTHDAY == type) {
                        String gebdt = dataQueryCursor.getString(2);
                        Utils.logDebug(TAG, "   ---> found birthday: " + gebdt);
                        String where = ContactsContract.Data.CONTACT_ID
                                + " = ? AND " + ContactsContract.Data._ID
                                + " = ? AND " + ContactsContract.Data.MIMETYPE
                                + " = ?";
                        String[] selectionArgs = new String[]{
                                id,
                                dataId,
                                ContactsContract.CommonDataKinds.Event.CONTENT_ITEM_TYPE};
                        contentResolver.delete(
                                ContactsContract.Data.CONTENT_URI, where,
                                selectionArgs);
                    }
                }
                // Or a notes field?
                else if (ContactsContract.CommonDataKinds.Note.CONTENT_ITEM_TYPE
                        .equals(mimeType)) {
                    String note = dataQueryCursor.getString(3);
                    // update note
                    if ((note != null) && (note.length() >= 17)) {
                        // Birthday=yyyymmdd contains 17 chars
                        Date d = Utils.getDateFromString(note);
                        if (d != null) {
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
                            contentResolver.update(
                                    ContactsContract.Data.CONTENT_URI, values,
                                    where, selectionArgs);
                        }
                    }
                }
            }
            Utils.closeCursorCatchThrowable(dataQueryCursor);
        }
        // Insert birthday
        Date birthday = item.getBirthday();
        if (birthday != null) {
            String[] rawProjection = new String[]{ContactsContract.RawContacts._ID};
            String rawSelection = ContactsContract.RawContacts.CONTACT_ID + " = ?";
            ContentValues values = new ContentValues();
            values.put(ContactsContract.CommonDataKinds.Event.START_DATE,
                    Utils.getDateAsStringYYYY_MM_DD(birthday));
            values.put(ContactsContract.Data.MIMETYPE,
                    ContactsContract.CommonDataKinds.Event.CONTENT_ITEM_TYPE);
            values.put(ContactsContract.CommonDataKinds.Event.TYPE,
                    ContactsContract.CommonDataKinds.Event.TYPE_BIRTHDAY);
            Cursor c = contentResolver.query(ContactsContract.RawContacts.CONTENT_URI,
                    rawProjection, rawSelection, rawSelectionArgs, null);
            if (c != null) {
                while (c.moveToNext()) {
                    String rawContactId = c.getString(0);
                    values.put(
                            ContactsContract.CommonDataKinds.Event.RAW_CONTACT_ID,
                            rawContactId);
                    contentResolver.insert(
                            ContactsContract.Data.CONTENT_URI, values);
                }
                Utils.closeCursorCatchThrowable(c);
            }
        }
        requestSync();
        readContacts(true);
    }

    public static boolean shouldCheckNotificationSettings(NotificationManager nm) {
        if (!nm.areNotificationsEnabled())
            return true;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            List<NotificationChannel> channels = nm.getNotificationChannels();
            for (NotificationChannel channel : channels) {
                if (channel.getImportance() <= NotificationManager.IMPORTANCE_LOW) {
                    return true;
                }
            }
        }
        return false;
    }

    public static boolean shouldCheckAlarmSettings(AlarmManager am) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            return !am.canScheduleExactAlarms();
        }
        return false;
    }

    private boolean hasRequiredPermissions() {
        for (String permission : PERMISSIONS) {
            if (checkSelfPermission(permission) == PackageManager.PERMISSION_DENIED)
                return false;
        }
        return true;
    }

    private boolean updateUI() {
        boolean hasRequiredPermissions = hasRequiredPermissions();
        checkNotificationVisibility(hasRequiredPermissions);
        checkExactAlarmVisibility(hasRequiredPermissions);
        return hasRequiredPermissions;
    }

    private void run() {
        boolean hasRequiredPermissions = updateUI();
        findViewById(R.id.birthdaysList).setVisibility(hasRequiredPermissions ? View.VISIBLE : View.GONE);
        findViewById(R.id.permission_info).setVisibility(!hasRequiredPermissions ? View.VISIBLE : View.GONE);
        findViewById(R.id.no_birthdays).setVisibility(View.GONE);
        if (hasRequiredPermissions) {
            BootCompleteReceiver.startAlarm(this, true);
            readContacts(false);
        }
        invalidateOptionsMenu();
    }

    private void updateViewsFromEvent() {
        if (newEventEvent == null) {
            return;
        }
        createAndSetMonthAdapter();
        createAndSetDayAdapter();
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
            Utils.logError(TAG, "requestSync()", e);
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

    private void filterList() {
        setList(list);
    }

    private void setList(List<BirthdayItem> list) {
        this.list = list;
        List<BirthdayItem> filtered = new ArrayList<>();
        for (BirthdayItem item : list) {
            if (!item.isAccountNameSet() || Boolean.TRUE.equals(accounts.get(item.getAccountName()))) {
                filtered.add(item);
            }
        }
        birthdaysListAdapter = new BirthdayItemListAdapter(
                this,
                filtered,
                showList,
                item -> {
                    Intent i = new Intent(Intent.ACTION_VIEW, Uri.withAppendedPath(
                            ContactsContract.Contacts.CONTENT_URI,
                            Long.toString(item.getId())));
                    i.addFlags(Intent.FLAG_ACTIVITY_LAUNCH_ADJACENT | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivityForResult(i, RQ_SHOW_CONTACT);
                }
        );
        birthdaysList.setAdapter(birthdaysListAdapter);
    }

    protected void readContacts(final boolean forceRead) {
        final Handler h = new Handler(Objects.requireNonNull(Looper.myLooper()));
        Thread thread = new Thread(() -> {
            Looper.prepare();
            if ((list == null) || forceRead) {
                ContactsList cl = new ContactsList(
                        TKBirthdayReminder.this);
                list = cl.getMainList();
            }
            h.post(() -> {
                updateAccounts(list);
                setList(list);
                findViewById(R.id.no_birthdays).setVisibility(!list.isEmpty() ? View.GONE : View.VISIBLE);
                updateWidgets(TKBirthdayReminder.this);
            });
        });
        thread.start();
    }

    private void updateAccounts(List<BirthdayItem> list) {
        accounts.clear();
        SharedPreferences prefs = getSharedPreferences(TKBirthdayReminder.this);
        list.forEach(item -> {
            if (item.isAccountNameSet()) {
                String accountName = item.getAccountName();
                boolean isChecked = prefs.getBoolean(getPreferencesKeyForAccountName(accountName), true);
                accounts.put(accountName, isChecked);
            }
        });
    }

    private void updatePreferences(String accountName, boolean isChecked) {
        SharedPreferences prefs = getSharedPreferences(TKBirthdayReminder.this);
        prefs.edit().putBoolean(getPreferencesKeyForAccountName(accountName), isChecked).apply();
    }

    private String getPreferencesKeyForAccountName(String accountName) {
        return String.format("account_%s", accountName);
    }

    private int checkYear() {
        int intYear = Utils.INVISIBLE_YEAR;
        String s = newEventYear.getText().toString();
        if (!s.isEmpty()) {
            try {
                intYear = Integer.parseInt(s);
            } catch (Throwable thr) {
                Utils.logError(TAG, "checkYear()", thr);
            }
        }
        return intYear;
    }

    private void showEditBirthdayDialog(BirthdayItem item) {
        Date birthday = item.getBirthday();
        newEventEvent = (birthday == null) ? new AnnualEvent()
                : new AnnualEvent(birthday);
        showDialog(DATE_DIALOG_ID);
    }

    private void checkNotificationVisibility(boolean hasRequiredPermissions) {
        boolean visible = false;
        ViewGroup root = findViewById(R.id.info_layout);
        if (hasRequiredPermissions && shouldCheckNotificationVisibility() && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            configureInfo(root,
                    R.string.check_notification_settings,
                    R.string.hide_message,
                    HIDE_MESSAGE_KEY,
                    PreferenceFragment.createNotificationSettingsIntent(TKBirthdayReminder.this));
            visible = shouldCheckNotificationSettings(getSystemService(NotificationManager.class));
        }
        root.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    private void checkExactAlarmVisibility(boolean hasRequiredPermissions) {
        boolean visible = false;
        ViewGroup root = findViewById(R.id.info_layout_alarms);
        if (hasRequiredPermissions && shouldCheckAlarmVisibility() && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            configureInfo(root,
                    R.string.exact_alarms_are_off,
                    R.string.hide_message,
                    HIDE_MESSAGE_KEY_ALARMS,
                    new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM));
            visible = shouldCheckAlarmSettings(getSystemService(AlarmManager.class));
        }
        root.setVisibility(visible ? View.VISIBLE : View.GONE);
    }

    private void configureInfo(ViewGroup root, int resIdMessage, int resIdLink, String prefsKey, Intent i) {
        TextView info = root.findViewById(R.id.info);
        String s = getString(resIdMessage);
        String settings = getString(resIdLink);
        Spannable spannable = new SpannableString(s);
        int pos = s.indexOf(settings);
        if (pos >= 0) {
            spannable.setSpan(new ClickableSpan() {
                @Override
                public void onClick(@NonNull View widget) {
                    SharedPreferences prefs = getSharedPreferences(TKBirthdayReminder.this);
                    prefs.edit().putInt(prefsKey, APP_VERSION).apply();
                    root.setVisibility(View.GONE);
                }
            }, pos, pos + settings.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
            info.setMovementMethod(LinkMovementMethod.getInstance());
        }
        info.setText(spannable);
        Button b = root.findViewById(R.id.check);
        b.setOnClickListener(view -> {
            startActivity(i);
        });
    }

    private boolean shouldCheckNotificationVisibility() {
        int lastSavedVersion = getSharedPreferences(this).getInt(HIDE_MESSAGE_KEY, 0);
        return lastSavedVersion < APP_VERSION;
    }

    private boolean shouldCheckAlarmVisibility() {
        int lastSavedVersion = getSharedPreferences(this).getInt(HIDE_MESSAGE_KEY_ALARMS, 0);
        return lastSavedVersion < APP_VERSION;
    }
}
