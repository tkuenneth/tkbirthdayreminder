/*
 * BirthdayWidget.java
 *
 * TKBirthdayReminder (c) Thomas Künneth 2010 - 2021
 * Alle Rechte beim Autoren. All rights reserved.
 */
package com.thomaskuenneth.android.birthday;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.WindowManager;
import android.widget.RemoteViews;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class BirthdayWidget extends AppWidgetProvider {

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager,
                         int[] appWidgetIds) {
        updateWidgets(context, appWidgetManager, appWidgetIds);
    }

    public static void updateWidgets(Context context,
                                     AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        RemoteViews updateViews = new RemoteViews(context.getPackageName(),
                R.layout.birthdaywidget_layout);
        updateViews(updateViews, context);
        Intent intent = new Intent(context, TKBirthdayReminder.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0,
                intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE);
        updateViews.setOnClickPendingIntent(R.id.birthdaywidget_layout,
                pendingIntent);
        appWidgetManager.updateAppWidget(appWidgetIds, updateViews);
    }

    private static void updateViews(final RemoteViews updateViews,
                                    Context context) {
        String name = "";
        String zodiac = "";
        String birthday_date = "";
        String birthday2 = "";
        String text5 = "";
        ContactsList cl = new ContactsList(context);
        List<BirthdayItem> birthdays = cl.getWidgetList();
        final int total = birthdays.size();
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
            birthday2 = Utils.getBirthdayAsString(context, birthday);
            if (sameDayCount > 1) {
                text5 = Utils.trim(TKBirthdayReminder.getStringFromResources(context,
                        R.string.and_x_more,
                        sameDayCount - 1));
            }
            birthday_date = Utils.getBirthdayDateAsString(format, item);

            SharedPreferences prefs = PreferenceManager
                    .getDefaultSharedPreferences(context);
            if (prefs.getBoolean(PreferenceFragment.CHECKBOX_SHOW_ASTROLOGICAL_SIGNS, true)) {
                zodiac = Zodiac.getSign(context, birthday);
            }

            WindowManager wm = context
                    .getSystemService(WindowManager.class);
            if (wm != null) {
                Bitmap picture = BirthdayItemListAdapter.loadBitmap(item,
                        context, TKBirthdayReminder.getImageHeight(wm));
                updateViews.setImageViewBitmap(R.id.icon, picture);
            }
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
        updateViews.setViewVisibility(R.id.text4, text5.length() > 0 ? View.GONE : View.VISIBLE);
        updateViews.setTextViewText(R.id.text5, text5);
        updateViews.setInt(R.id.text5, "setTextColor", Color.WHITE);
        updateViews.setViewVisibility(R.id.text5, text5.length() > 0 ? View.VISIBLE : View.GONE);
    }
}
