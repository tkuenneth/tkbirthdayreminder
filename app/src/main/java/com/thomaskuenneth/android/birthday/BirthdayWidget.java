/*
 * BirthdayWidget.java
 *
 * TKBirthdayReminder (c) Thomas Künneth 2010 - 2025
 * All rights reserved.
 */
package com.thomaskuenneth.android.birthday;

import static com.thomaskuenneth.android.birthday.Utils.loadBitmap;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.RemoteViews;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class BirthdayWidget extends AppWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager,
                         int[] appWidgetIds) {
        updateWidgets(context, appWidgetManager, appWidgetIds);
    }

    public static void updateWidgets(
            Context context,
            AppWidgetManager appWidgetManager,
            int[] appWidgetIds
    ) {
        RemoteViews updateViews = new RemoteViews(context.getPackageName(),
                R.layout.birthdaywidget_layout);
        Thread t = new Thread(() -> {
            ContactsList cl = new ContactsList(context);
            List<BirthdayItem> list = cl.getWidgetList();
            HashMap<String, Boolean> accounts = new HashMap<>();
            TKBirthdayReminder.clearAndFillAccountsMap(context, accounts, list);
            List<BirthdayItem> birthdays = TKBirthdayReminder.getFilteredList(accounts, list);
            Handler h = new Handler(Looper.getMainLooper());
            h.post(() -> {
                String name = "";
                String zodiac = "";
                String birthday_date = "";
                String birthday2 = "";
                Bitmap bitmap = null;
                final int total = birthdays.size();
                boolean moreThanOne = false;
                if (total > 0) {
                    int firstPos = 0;
                    int days = 0;
                    for (int currentPos = 0; currentPos < total; currentPos++) {
                        BirthdayItem item = birthdays.get(currentPos);
                        firstPos = currentPos;
                        days = Utils.getBirthdayInDays(item.getBirthday(), null);
                        if (days >= 0) {
                            break;
                        }
                    }
                    int sameDayCount = 0;
                    for (int currentPos = 0; currentPos < total; currentPos++) {
                        BirthdayItem item = birthdays.get(currentPos);
                        int currentDays = Utils.getBirthdayInDays(item.getBirthday(), null);
                        if (days == currentDays) {
                            sameDayCount += 1;
                        }
                    }
                    DateFormat format = new SimpleDateFormat(context.getString(R.string.month_and_day), Locale.getDefault());
                    BirthdayItem item = birthdays.get(firstPos);
                    name = item.getName();
                    Date birthday = item.getBirthday();
                    moreThanOne = sameDayCount > 1;
                    if (moreThanOne) {
                        birthday2 = Utils.trim(TKBirthdayReminder.getStringFromResources(context,
                                R.string.and_x_more,
                                sameDayCount - 1));
                    } else {
                        birthday2 = Utils.getBirthdayAsString(context, birthday);
                    }
                    birthday_date = Utils.getBirthdayDateAsString(format, item);
                    SharedPreferences prefs = PreferenceManager
                            .getDefaultSharedPreferences(context);
                    if (prefs.getBoolean(PreferenceFragment.CHECKBOX_SHOW_ASTROLOGICAL_SIGNS, true)) {
                        zodiac = Zodiac.getSign(context, birthday);
                    }

                    bitmap = loadBitmap(item, context);
                }
                int opacity = WidgetPreferenceFragment.getOpacity(context);
                int color = 0x000000;
                opacity <<= 24;
                updateViews.setInt(R.id.birthdaywidget_layout, "setBackgroundColor", opacity | color);
                updateViews.setTextViewText(R.id.text1, name);
                updateViews.setInt(R.id.text1, "setTextColor", Color.WHITE);
                updateViews.setTextViewText(R.id.text2, birthday2);
                updateViews.setInt(R.id.text2, "setTextColor", Color.WHITE);
                updateViews.setTextViewText(R.id.text3, birthday_date);
                updateViews.setInt(R.id.text3, "setTextColor", Color.WHITE);
                updateViews.setTextViewText(R.id.text4, zodiac);
                updateViews.setInt(R.id.text4, "setTextColor", Color.WHITE);
                updateViews.setViewVisibility(R.id.text4, moreThanOne ? View.GONE : View.VISIBLE);
                updateViews.setImageViewBitmap(R.id.icon, bitmap);
                updateViews.setViewVisibility(R.id.no_birthdays, total > 0 ? View.GONE : View.VISIBLE);

                Intent intent = new Intent(context, TKBirthdayReminder.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                PendingIntent pendingIntent = PendingIntent.getActivity(context, 0,
                        intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
                updateViews.setOnClickPendingIntent(R.id.birthdaywidget_layout,
                        pendingIntent);
                appWidgetManager.updateAppWidget(appWidgetIds, updateViews);
            });
        });
        t.start();
    }
}
