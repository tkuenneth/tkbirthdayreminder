/*
 * NotificationPreference.java
 *
 * TKBirthdayReminder (c) Thomas Künneth 2017 - 2019
 * Alle Rechte beim Autoren. All rights reserved.
 */
package com.thomaskuenneth.android.birthday;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.View;
import android.widget.CheckBox;

public class NotificationPreference extends DialogPreference {

    private static final int[] IDS = new int[]{R.id.days_1, R.id.days_2,
            R.id.days_3, R.id.days_4, R.id.days_5, R.id.days_6, R.id.days_7};
    private static final String NOTIFICATION_DAYS = "notificationDays";
    private static final int BITS = 7;

    private final SharedPreferences prefs;
    private final CheckBox[] checkboxes = new CheckBox[BITS];

    public NotificationPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
        prefs = TKBirthdayReminder.getSharedPreferences(context);
        setDialogLayoutResource(R.layout.notification_days);
    }

    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);
        int notificationDays = prefs.getInt(NOTIFICATION_DAYS, 0);
        for (int bit = 0; bit < 7; bit++) {
            checkboxes[bit] = view.findViewById(IDS[bit]);
            int mask = 1 << bit;
            checkboxes[bit].setChecked((notificationDays & mask) == mask);
        }
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);
        if (positiveResult) {
            int bits = 0;
            for (int bit = 0; bit < 7; bit++) {
                if (checkboxes[bit].isChecked()) {
                    bits |= (1 << bit);
                }
            }
            SharedPreferences.Editor editor = prefs.edit();
            editor.putInt(NOTIFICATION_DAYS, bits);
            editor.apply();
        }
    }

    static int getNotificationDays(Context context) {
        return TKBirthdayReminder.getSharedPreferences(context).getInt(NOTIFICATION_DAYS, 0);
    }
}
