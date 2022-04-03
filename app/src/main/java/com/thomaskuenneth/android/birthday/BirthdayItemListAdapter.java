/*
 * BirthdayItemListAdapter.java
 *
 * TKBirthdayReminder (c) Thomas Künneth 2009 - 2022
 * All rights reserved.
 */
package com.thomaskuenneth.android.birthday;

import android.annotation.SuppressLint;
import android.content.ContentUris;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.preference.PreferenceManager;
import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.content.ContextCompat;

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
                PreferenceFragment.CHECKBOX_SHOW_ASTROLOGICAL_SIGNS, true);
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

    @SuppressLint("InflateParams")
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;

        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.list_item_icon_text, null);
            convertView.setPadding(0, 4, 0, 4);

            holder = new ViewHolder();
            holder.textName = convertView.findViewById(R.id.text1);
            holder.textInfo = convertView.findViewById(R.id.text2);
            holder.textDate = convertView.findViewById(R.id.text3);
            holder.textZodiac = convertView.findViewById(R.id.text4);
            holder.icon = convertView.findViewById(R.id.icon);

            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        BirthdayItem item = (BirthdayItem) getItem(position);
        holder.textName.setText(item.getName());
        Date birthday = item.getBirthday();
        holder.textInfo.setText(Utils.getBirthdayAsString(context,
                birthday));
        holder.textDate.setText(Utils.getBirthdayDateAsString(format,
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
                    Drawable d = ContextCompat.getDrawable(context, R.drawable.ic_launcher_foreground);
                    if (d != null) {
                        picture = getBitmap(d);
                    }
                }
                if (picture != null) {
                    if (picture.getHeight() != size) {
                        Bitmap temp = picture;
                        float w = (float) picture.getWidth();
                        float h = (float) picture.getHeight();
                        int h2 = (int) (((h / w)) * size);
                        picture = Bitmap
                                .createScaledBitmap(temp, size, h2, false);
                        if (temp != picture) {
                            temp.recycle();
                        }
                    }
                    item.setPicture(picture);
                }
            } catch (Throwable tr) {
                Utils.logError(TAG, "loadBitmap()", tr);
            } finally {
                if (input != null) {
                    try {
                        input.close();
                    } catch (IOException e) {
                        Utils.logError(TAG, "loadBitmap()", e);
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

    private static Bitmap getBitmap(Drawable vectorDrawable) {
        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(),
                vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        vectorDrawable.draw(canvas);
        return bitmap;
    }
}
