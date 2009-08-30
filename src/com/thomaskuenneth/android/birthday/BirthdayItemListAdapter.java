/**
 * BirthdayItemListAdapter.java
 * 
 * TKBirthdayReminder (c) Thomas Künneth 2009
 * Alle Rechte beim Autoren. All rights reserved.
 */
package com.thomaskuenneth.android.birthday;

import java.util.List;

import android.content.ContentUris;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.Contacts.People;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class BirthdayItemListAdapter extends BaseAdapter {

	private final LayoutInflater mInflater;
	private final List<BirthdayItem> items;
	private final Context context;

	public BirthdayItemListAdapter(Context context, List<BirthdayItem> list) {
		this.mInflater = LayoutInflater.from(context);
		this.items = list;
		this.context = context;
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
		// A ViewHolder keeps references to children views to avoid unneccessary
		// calls
		// to findViewById() on each row.
		ViewHolder holder;

		// When convertView is not null, we can reuse it directly, there is no
		// need
		// to reinflate it. We only inflate a new View when the convertView
		// supplied
		// by ListView is null.
		if (convertView == null) {
			convertView = mInflater.inflate(R.layout.list_item_icon_text, null);

			// Creates a ViewHolder and store references to the two children
			// views
			// we want to bind data to.
			holder = new ViewHolder();
			holder.text1 = (TextView) convertView.findViewById(R.id.text1);
			holder.text2 = (TextView) convertView.findViewById(R.id.text2);
			holder.icon = (ImageView) convertView.findViewById(R.id.icon);

			convertView.setTag(holder);
		} else {
			// Get the ViewHolder back to get fast access to the TextView
			// and the ImageView.
			holder = (ViewHolder) convertView.getTag();
		}

		// Bind the data efficiently with the holder.
		BirthdayItem item = (BirthdayItem) getItem(position);
		holder.text1.setText(item.getName());
		holder.text2.setText(TKDateUtils.getBirthdayAsString(context, item
				.getBirthday()));

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
		TextView text1, text2;
		ImageView icon;
	}
}
