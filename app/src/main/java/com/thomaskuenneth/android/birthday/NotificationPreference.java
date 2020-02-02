/*
 * NotificationPreference.java
 *
 * TKBirthdayReminder (c) Thomas Künneth 2017 - 2019
 * Alle Rechte beim Autoren. All rights reserved.
 */
package com.thomaskuenneth.android.birthday;

import android.content.Context;
import android.util.AttributeSet;

import androidx.preference.DialogPreference;

public class NotificationPreference extends DialogPreference {

    static final String KEY = "key_notification_preference";

    public NotificationPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        setDialogLayoutResource(R.layout.notification_days);
        setDialogTitle(R.string.notification_days);
        setTitle(R.string.notification_days);
        setKey(KEY);
    }
}
