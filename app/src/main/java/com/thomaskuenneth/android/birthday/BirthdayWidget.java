/*
 * BirthdayWidget.java
 * 
 * TKBirthdayReminder (c) Thomas Künneth 2010 - 2017
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
import android.preference.PreferenceManager;
import android.view.WindowManager;
import android.widget.RemoteViews;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
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
        TKBirthdayReminder.setWidgetAppearance(context, updateViews, R.id.birthdaywidget_layout);
        updateViews(updateViews, context);
        Intent intent = new Intent(context, TKBirthdayReminder.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(context, 0,
                intent, 0);
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
        ContactsList cl = new ContactsList(context);
        ArrayList<BirthdayItem> birthdays = cl.getListBirthdaySet();
        if (birthdays.size() > 0) {
            int i = 0;
            for (int pos = 0; pos < birthdays.size(); pos++) {
                BirthdayItem item = birthdays.get(pos);
                if (TKDateUtils.getBirthdayInDays(item.getBirthday()) < 0) {
                    continue;
                }
                i = pos;
                break;
            }
            DateFormat format = new SimpleDateFormat(context.getString(R.string.month_and_day), Locale.getDefault());
            BirthdayItem item = birthdays.get(i);
            name = item.getName();
            Date birthday = item.getBirthday();
            birthday2 = TKDateUtils.getBirthdayAsString(context, birthday);
            birthday_date = TKDateUtils.getBirthdayDateAsString(format, item);

            SharedPreferences prefs = PreferenceManager
                    .getDefaultSharedPreferences(context);
            if (prefs.getBoolean("checkbox_show_astrological_signs", true)) {
                zodiac = Zodiac.getSign(context, birthday);
            }

            WindowManager wm = (WindowManager) context
                    .getSystemService(Context.WINDOW_SERVICE);
            Bitmap picture = BirthdayItemListAdapter.loadBitmap(item,
                    context, TKBirthdayReminder.getImageHeight(wm));
            updateViews.setImageViewBitmap(R.id.icon, picture);
        }
        updateViews.setTextViewText(R.id.text1, name);
        updateViews.setTextViewText(R.id.text2, birthday2);
        updateViews.setTextViewText(R.id.text3, birthday_date);
        updateViews.setTextViewText(R.id.text4, zodiac);
    }
}
