/**
 * BirthdayWidget.java
 * 
 * TKBirthdayReminder (c) Thomas Künneth 2010
 * Alle Rechte beim Autoren. All rights reserved.
 */
package com.thomaskuenneth.android.birthday;

import java.util.ArrayList;
import java.util.Date;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.Contacts.People;
import android.widget.RemoteViews;

import com.thomaskuenneth.android.util.Zodiac;

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
				intent, 0);
		updateViews.setOnClickPendingIntent(R.id.birthdaywidget_layout,
				pendingIntent);
		appWidgetManager.updateAppWidget(appWidgetIds, updateViews);
	}

	private static void updateViews(final RemoteViews updateViews,
			Context context) {
		ContactsList cl = new ContactsList(context);
		if (cl != null) {
			ArrayList<BirthdayItem> birthdays = cl.getListBirthdaySet();
			if (birthdays.size() > 0) {
				BirthdayItem item = birthdays.get(0);
				updateViews.setTextViewText(R.id.text1, item.getName());
				Date birthday = item.getBirthday();
				updateViews.setTextViewText(R.id.text2, TKDateUtils
						.getBirthdayAsString(context, birthday));
				updateViews.setTextViewText(R.id.text3, TKDateUtils
						.getBirthdayDateAsString(birthday));
				updateViews.setTextViewText(R.id.text4, Zodiac.getSign(context,
						birthday));

				Bitmap picture = item.getPicture();
				if (picture == null) {
					Uri uriPerson = ContentUris.withAppendedId(
							People.CONTENT_URI, item.getId());
					picture = People.loadContactPhoto(context, uriPerson,
							R.drawable.birthdaycake_32, null);
					item.setPicture(picture);
				}
				updateViews.setImageViewBitmap(R.id.icon, picture);
			}
		}
	}
}
