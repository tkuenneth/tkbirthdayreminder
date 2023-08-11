/*
 * BirthdayItemViewHolder.java
 *
 * TKBirthdayReminder (c) Thomas Künneth 2023
 * All rights reserved.
 */
package com.thomaskuenneth.android.birthday;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

public class BirthdayItemViewHolder extends RecyclerView.ViewHolder {
    public final TextView textName, textInfo, textDate, textZodiac;
    public final ImageView icon;

    public BirthdayItemViewHolder(View view) {
        super(view);
        textName = view.findViewById(R.id.text1);
        textInfo = view.findViewById(R.id.text2);
        textDate = view.findViewById(R.id.text3);
        textZodiac = view.findViewById(R.id.text4);
        icon = view.findViewById(R.id.icon);
    }
}
