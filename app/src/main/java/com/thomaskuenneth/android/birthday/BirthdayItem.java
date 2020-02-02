/*
 * BirthdayItem.java
 *
 * TKBirthdayReminder (c) Thomas Künneth 2009 - 2017
 * Alle Rechte beim Autoren. All rights reserved.
 */
package com.thomaskuenneth.android.birthday;

import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import java.text.ParseException;
import java.util.Date;

/**
 * Diese Klasse repräsentiert einen Eintrag in Listen von Personen (mit oder
 * ohne Geburtstag).
 *
 * @author Thomas Künneth
 */
public class BirthdayItem implements Parcelable {

    private static final String TAG = BirthdayItem.class.getSimpleName();

    private String name;
    private Date birthday;
    private long id;
    private String primaryPhoneNumber;
    private Bitmap picture;

    BirthdayItem(String name, Date birthday, long id,
                 String primaryPhoneNumber) {
        this(name, birthday, id, primaryPhoneNumber, null);
    }

    private BirthdayItem(String name, Date birthday, long id,
                         String primaryPhoneNumber, Bitmap picture) {
        this.name = name;
        this.birthday = birthday;
        this.id = id;
        this.primaryPhoneNumber = primaryPhoneNumber;
        this.picture = picture;
    }

    String getName() {
        return name;
    }

    Date getBirthday() {
        return birthday;
    }

    void setBirthday(Date birthday) {
        this.birthday = birthday;
    }

    long getId() {
        return id;
    }

    void setId(long id) {
        this.id = id;
    }

    String getPrimaryPhoneNumber() {
        return primaryPhoneNumber;
    }

    Bitmap getPicture() {
        return picture;
    }

    void setPicture(Bitmap picture) {
        this.picture = picture;
    }

    public static final Parcelable.Creator CREATOR = new Parcelable.Creator() {
        public BirthdayItem createFromParcel(Parcel in) {
            String name = in.readString();
            Date birthday = null;
            try {
                String s = in.readString();
                if (s != null) {
                    birthday = TKDateUtils.FORMAT_YYYYMMDD.parse(s);
                }
            } catch (ParseException e) {
                Log.e(TAG, "createFromParcel()", e);
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
            Log.e(TAG, "writeToParcel()", thr);
        }
        dest.writeString(date == null ? "" : date);
        dest.writeLong(id);
        dest.writeString(primaryPhoneNumber);
    }
}
