/*
 * BirthdayItemListAdapter.java
 *
 * TKBirthdayReminder (c) Thomas Künneth 2009 - 2023
 * All rights reserved.
 */
package com.thomaskuenneth.android.birthday;

import static com.thomaskuenneth.android.birthday.Utils.loadBitmap;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

public class BirthdayItemListAdapter extends RecyclerView.Adapter<BirthdayItemViewHolder> {

    private final LayoutInflater mInflater;
    private final List<BirthdayItem> items;
    private final Context context;
    private final boolean showAstrologicalSigns;
    private final int height;
    private final DateFormat format;
    private final Consumer<BirthdayItem> itemClicked;
    private BirthdayItem lastLongClicked;
    private final boolean showList;

    public BirthdayItemListAdapter(
            Context context,
            List<BirthdayItem> list,
            boolean showList,
            int height,
            Consumer<BirthdayItem> itemClicked
    ) {
        mInflater = LayoutInflater.from(context);
        this.items = list;
        this.context = context;
        this.height = height;
        this.format = new SimpleDateFormat(
                context.getString(R.string.month_and_day), Locale.getDefault());
        this.itemClicked = itemClicked;
        this.lastLongClicked = null;
        this.showList = showList;

        SharedPreferences prefs = PreferenceManager
                .getDefaultSharedPreferences(context);
        showAstrologicalSigns = prefs.getBoolean(
                PreferenceFragment.CHECKBOX_SHOW_ASTROLOGICAL_SIGNS, true);
    }

    @NonNull
    @Override
    public BirthdayItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = mInflater.inflate(showList ? R.layout.list_item_icon_text : R.layout.card, parent, false);
        return new BirthdayItemViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BirthdayItemViewHolder holder, int position) {
        BirthdayItem item = items.get(position);
        holder.itemView.setOnClickListener(view -> {
            itemClicked.accept(item);
        });
        holder.itemView.setOnLongClickListener(view -> {
            lastLongClicked = item;
            return false;
        });
        holder.textName.setText(item.getName());
        Date birthday = item.getBirthday();
        holder.textInfo.setText(Utils.getBirthdayAsString(context,
                birthday));
        holder.textDate.setText(Utils.getBirthdayDateAsString(format,
                item));
        holder.textZodiac.setText(showAstrologicalSigns ? Zodiac.getSign(
                context, birthday) : "");
        holder.textZodiac.setVisibility(holder.textZodiac.length() > 0 ? View.VISIBLE : View.GONE);
        Bitmap picture = loadBitmap(item, context, height);
        holder.icon.setImageBitmap(picture);
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    public BirthdayItem getItem(int position) {
        return items.get(position);
    }

    public BirthdayItem getLastLongClicked() {
        return lastLongClicked;
    }
}
