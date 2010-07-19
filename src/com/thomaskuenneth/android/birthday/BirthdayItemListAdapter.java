/**
 * BirthdayItemListAdapter.java
 * 
 * TKBirthdayReminder (c) Thomas Künneth 2009
 * Alle Rechte beim Autoren. All rights reserved.
 */
package com.thomaskuenneth.android.birthday;

import java.util.Date;
import java.util.List;

import android.content.ContentUris;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.provider.Contacts.People;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.thomaskuenneth.android.util.Zodiac;

public class BirthdayItemListAdapter extends BaseAdapter {

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

		Bitmap picture = item.getPicture();
		if (picture == null) {
			Uri uriPerson = ContentUris.withAppendedId(People.CONTENT_URI, item
					.getId());
			picture = People.loadContactPhoto(context, uriPerson,
					R.drawable.birthdaycake_32, null);
			item.setPicture(picture);
		}

		holder.icon.setImageBitmap(picture);
		return convertView;
	}

	static class ViewHolder {
		TextView textName, textInfo, textDate, textZodiac;
		ImageView icon;
	}
}
