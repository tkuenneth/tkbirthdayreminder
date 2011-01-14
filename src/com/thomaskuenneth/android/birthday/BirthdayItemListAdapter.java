/**
 * BirthdayItemListAdapter.java
 * 
 * TKBirthdayReminder (c) Thomas Künneth 2009 - 2011
 * Alle Rechte beim Autoren. All rights reserved.
 */
package com.thomaskuenneth.android.birthday;

import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.List;

import android.content.ContentUris;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.thomaskuenneth.android.util.Zodiac;

public class BirthdayItemListAdapter extends BaseAdapter {

	private static final String TAG = BirthdayItemListAdapter.class
			.getSimpleName();
	private final LayoutInflater mInflater;
	private final List<BirthdayItem> items;
	private final Context context;
	private final boolean showAstrologicalSigns;

	public BirthdayItemListAdapter(Context context, List<BirthdayItem> list) {
		this.mInflater = LayoutInflater.from(context);
		this.items = list;
		this.context = context;

		SharedPreferences prefs = PreferenceManager
				.getDefaultSharedPreferences(context);
		showAstrologicalSigns = prefs.getBoolean(
				"checkbox_show_astrological_signs", true);
	}

	public int getCount() {
		return items.size();
	}

	public Object getItem(int position) {
		return items.get(position);
	}

	public long getItemId(int position) {
		return position;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		ViewHolder holder;

		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.list_item_icon_text, null);

			holder = new ViewHolder();
			holder.textName = (TextView) convertView.findViewById(R.id.text1);
			holder.textInfo = (TextView) convertView.findViewById(R.id.text2);
			holder.textDate = (TextView) convertView.findViewById(R.id.text3);
			holder.textZodiac = (TextView) convertView.findViewById(R.id.text4);
			holder.icon = (ImageView) convertView.findViewById(R.id.icon);

			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		BirthdayItem item = (BirthdayItem) getItem(position);
		holder.textName.setText(item.getName());
		Date birthday = item.getBirthday();
		holder.textInfo.setText(TKDateUtils.getBirthdayAsString(context,
				birthday));
		holder.textDate.setText(TKDateUtils.getBirthdayDateAsString(birthday));
		holder.textZodiac.setText(showAstrologicalSigns ? Zodiac.getSign(
				context, birthday) : "");

		Bitmap picture = loadBitmap(item, context);
		holder.icon.setImageBitmap(picture);

		return convertView;
	}

	public static Bitmap loadBitmap(BirthdayItem item, Context context) {
		Bitmap picture = item.getPicture();
		if (picture == null) {
			InputStream input = null;
			try {
				Uri uri = ContentUris.withAppendedId(
						ContactsContract.Contacts.CONTENT_URI, item.getId());
				input = ContactsContract.Contacts.openContactPhotoInputStream(
						context.getContentResolver(), uri);
				if (input != null) {
					picture = BitmapFactory.decodeStream(input);
				} else {
					picture = BitmapFactory.decodeResource(context
							.getResources(), R.drawable.birthdaycake_32);
				}
				item.setPicture(picture);
			} catch (Throwable tr) {
				Log.e(TAG, "getView(): loading contact photo", tr);
			} finally {
				if (input != null) {
					try {
						input.close();
					} catch (IOException e) {
					}
				}
			}
		}
		return picture;
	}

	static class ViewHolder {
		TextView textName, textInfo, textDate, textZodiac;
		ImageView icon;
	}
}
