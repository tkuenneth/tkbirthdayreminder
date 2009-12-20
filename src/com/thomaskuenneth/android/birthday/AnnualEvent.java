/**
 * AnnualEvent.java
 * 
 * TKBirthdayReminder (c) Thomas Künneth 2009
 * Alle Rechte beim Autoren. All rights reserved.
 */
package com.thomaskuenneth.android.birthday;

import java.util.Calendar;
import java.util.Date;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Diese Klasse repräsentiert ein jährliches Ereignis. Dieses hat eine
 * Beschreibung sowie die Felder Monat, Tag und Jahr.
 * 
 * @author Thomas Künneth
 * 
 */
public class AnnualEvent implements Parcelable {

	private String descr;
	private int year, month, day;

	public AnnualEvent() {
		this(Calendar.getInstance());
	}

	public AnnualEvent(Date date) {
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		this.descr = "";
		this.year = cal.get(Calendar.YEAR);
		this.month = cal.get(Calendar.MONTH);
		this.day = cal.get(Calendar.DAY_OF_MONTH);
	}

	public AnnualEvent(Calendar cal) {
		this("", cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal
				.get(Calendar.DAY_OF_MONTH));
	}

	public AnnualEvent(String descr, int month, int day) {
		this(descr, Integer.MAX_VALUE, month, day);
	}

	public AnnualEvent(String descr, int year, int month, int day) {
		this.descr = descr;
		this.year = year;
		this.month = month;
		this.day = day;
	}

	public String getDescr() {
		return descr;
	}

	public void setDescr(String descr) {
		this.descr = descr;
	}

	public int getYear() {
		return year;
	}

	public void setYear(int year) {
		this.year = year;
	}

	public int getMonth() {
		return month;
	}

	public void setMonth(int month) {
		this.month = month;
	}

	public int getDay() {
		return day;
	}

	public void setDay(int day) {
		this.day = day;
	}

	public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
		public AnnualEvent createFromParcel(Parcel in) {
			String descr = in.readString();
			int year = in.readInt();
			int month = in.readInt();
			int day = in.readInt();
			return new AnnualEvent(descr, year, month, day);
		}

		public AnnualEvent[] newArray(int size) {
			return new AnnualEvent[size];
		}
	};

	/************************
	 * Parcelable interface *
	 ************************/

	@Override
	public int describeContents() {
		return 0;
	}

	@Override
	public void writeToParcel(Parcel dest, int flags) {
		dest.writeString(descr);
		dest.writeInt(year);
		dest.writeInt(month);
		dest.writeInt(day);
	}
}
