/**
 * BirthdayNotSetActivity.java
 * 
 * TKBirthdayReminder (c) Thomas Künneth 2009
 * Alle Rechte beim Autoren. All rights reserved.
 */
package com.thomaskuenneth.android.birthday;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Contacts;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;

public class BirthdayNotSetActivity extends AbstractListActivity implements
		OnItemClickListener {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		getListView().setOnItemClickListener(this);

		readContacts(false);
	}

	@Override
	protected String getStateKey() {
		return Constants.LIST_BIRTHDAY_NOT_SET;
	}

	@Override
	protected ArrayList<BirthdayItem> getProperList(ContactsList cl) {
		return cl.getListBirthdayNotSet();
	}

	/***********************
	 * OnItemClickListener *
	 ***********************/

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		Object o = parent.getAdapter().getItem(position);
		if (o instanceof BirthdayItem) {
			BirthdayItem item = (BirthdayItem) o;
			Intent data = new Intent();
			data.setData(Uri.withAppendedPath(Contacts.People.CONTENT_URI, Long
					.toString(item.getId())));
			setResult(Activity.RESULT_OK, data);
			finish();
		}
	}
}
