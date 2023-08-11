/*
 * WidgetPreference.java
 *
 * TKBirthdayReminder (c) Thomas Künneth 2013 - 2023
 * All rights reserved.
 */
package com.thomaskuenneth.android.birthday;

import android.content.Context;
import android.util.AttributeSet;

import androidx.preference.DialogPreference;

public class WidgetPreference extends DialogPreference {

    static final String KEY = "key_widget_preference";

    public WidgetPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        setDialogLayoutResource(R.layout.widget_preference);
        setDialogTitle(R.string.widget_appearance);
        setTitle(R.string.widget_appearance);
        setKey(KEY);
    }
}
