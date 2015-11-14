/**
 * BirthdayItem.java
 * 
 * TKBirthdayReminder (c) Thomas Künneth 2009 - 2011
 * Alle Rechte beim Autoren. All rights reserved.
 */
package com.thomaskuenneth.android.birthday;

import java.text.ParseException;
import java.util.Date;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

/**
 * Diese Klasse repräsentiert einen Eintrag in Listen von Personen (mit oder
 * ohne Geburtstag).
 * 
 * @author Thomas Künneth
 * 
 */
public class BirthdayItem implements Parcelable {

	private String name;
	private Date birthday;
	private long id;
	private String primaryPhoneNumber;
	private Bitmap picture;

	public BirthdayItem(String name, Date birthday, long id,
			String primaryPhoneNumber) {
		this(name, birthday, id, primaryPhoneNumber, null);
	}

	public BirthdayItem(String name, Date birthday, long id,
			String primaryPhoneNumber, Bitmap picture) {
		this.name = name;
		this.birthday = birthday;
		this.id = id;
		this.primaryPhoneNumber = primaryPhoneNumber;
		this.picture = picture;
	}

	public String getName() {
		return name;
	}

	public String getNameNotNull() {
		return (getName() == null) ? "" : name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Date getBirthday() {
		return birthday;
	}

	public void setBirthday(Date birthday) {
		this.birthday = birthday;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getPrimaryPhoneNumber() {
		return primaryPhoneNumber;
	}

	public void setPrimaryPhoneNumber(String primaryPhoneNumber) {
		this.primaryPhoneNumber = primaryPhoneNumber;
	}

	public Bitmap getPicture() {
		return picture;
	}

	public void setPicture(Bitmap picture) {
		this.picture = picture;
	}

	public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
		public BirthdayItem createFromParcel(Parcel in) {
			String name = in.readString();
			Date birthday = null;
			try {
				birthday = TKDateUtils.FORMAT_YYYYMMDD.parse(in.readString());
			} catch (ParseException e) {
			}
			long id = in.readLong();
			String primaryPhoneNumber = in.readString();
			return new BirthdayItem(name, birthday, id, primaryPhoneNumber);
		}

		public BirthdayItem[] newArray(int size) {
			return new BirthdayItem[size];
		}
	};

	// //////////////
	// Parcelable //
	// //////////////

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(name);
		String date = null;
		try {
			date = TKDateUtils.FORMAT_YYYYMMDD.format(birthday);
		} catch (Throwable thr) {
		}
		dest.writeString(date == null ? "" : date);
		dest.writeLong(id);
		dest.writeString(primaryPhoneNumber);
	}
}
