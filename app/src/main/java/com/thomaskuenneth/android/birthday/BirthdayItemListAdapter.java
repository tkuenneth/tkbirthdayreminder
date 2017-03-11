/*
 * BirthdayItemListAdapter.java
 * 
 * TKBirthdayReminder (c) Thomas Künneth 2009 - 2017
 * Alle Rechte beim Autoren. All rights reserved.
 */
package com.thomaskuenneth.android.birthday;

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

import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

class BirthdayItemListAdapter extends BaseAdapter {

    private static final String TAG = BirthdayItemListAdapter.class
            .getSimpleName();

    private final LayoutInflater mInflater;
    private final List<BirthdayItem> items;
    private final Context context;
    private final boolean showAstrologicalSigns;
    private final int height;
    private final DateFormat format;

    BirthdayItemListAdapter(Context context, List<BirthdayItem> list,
                            int height) {
        this.mInflater = LayoutInflater.from(context);
        this.items = list;
        this.context = context;
        this.height = height;
        this.format = new SimpleDateFormat(
                context.getString(R.string.month_and_day), Locale.getDefault());

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
            convertView.setPadding(4, 2, 16, 2);

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
        holder.textDate.setText(TKDateUtils.getBirthdayDateAsString(format,
                item));
        holder.textZodiac.setText(showAstrologicalSigns ? Zodiac.getSign(
                context, birthday) : "");

        Bitmap picture = loadBitmap(item, context, height);
        holder.icon.setImageBitmap(picture);
        return convertView;
    }

    static Bitmap loadBitmap(BirthdayItem item, Context context,
                             int size) {
        Bitmap picture = item.getPicture();
        if ((picture != null) && (picture.isRecycled())) {
            picture = null;
        }
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
//                    int id;
//                    if (size == 32) {
//                        id = R.drawable.birthdaycake_32;
//                    } else if (size == 48) {
//                        id = R.drawable.birthdaycake_48;
//                    } else {
//                        id = R.drawable.birthdaycake_96;
//                    }
                    picture = BitmapFactory.decodeResource(
                            context.getResources(), R.mipmap.ic_launcher);
                }
                if (picture.getHeight() != size) {
                    Bitmap temp = picture;
                    float w = (float) picture.getWidth();
                    float h = (float) picture.getHeight();
                    // höhe : breite = height : width
                    int h2 = (int) (((h / w)) * size);
                    picture = Bitmap
                            .createScaledBitmap(temp, size, h2, false);
                    if (temp != picture) {
                        temp.recycle();
                    }
                }
                item.setPicture(picture);
            } catch (Throwable tr) {
                Log.e(TAG, "loadBitmap()", tr);
            } finally {
                if (input != null) {
                    try {
                        input.close();
                    } catch (IOException e) {
                        Log.e(TAG, "loadBitmap()", e);
                    }
                }
            }
        }
        return picture;
    }

    private static class ViewHolder {
        TextView textName, textInfo, textDate, textZodiac;
        ImageView icon;
    }
}
