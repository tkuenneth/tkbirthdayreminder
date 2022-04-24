/*
 * NotificationPreferenceFragment.java
 *
 * TKBirthdayReminder (c) Thomas Künneth 2022
 * All rights reserved.
 */
package com.thomaskuenneth.android.birthday;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.View;
import android.widget.CheckBox;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceDialogFragmentCompat;

public class NotificationPreferenceFragment extends PreferenceDialogFragmentCompat {

    private static final int[] IDS = new int[]{R.id.days_1, R.id.days_2,
            R.id.days_3, R.id.days_4, R.id.days_5, R.id.days_6, R.id.days_7};
    private static final String NOTIFICATION_DAYS = "notificationDays";
    private static final int BITS = 7;

    private final CheckBox[] checkboxes = new CheckBox[BITS];

    private SharedPreferences prefs;

    @Override
    protected void onBindDialogView(@NonNull View view) {
        super.onBindDialogView(view);
        Context context = getContext();
        if (context != null) {
            prefs = TKBirthdayReminder.getSharedPreferences(context);
            int notificationDays = prefs.getInt(NOTIFICATION_DAYS, 0);
            for (int bit = 0; bit < 7; bit++) {
                checkboxes[bit] = view.findViewById(IDS[bit]);
                int mask = 1 << bit;
                checkboxes[bit].setChecked((notificationDays & mask) == mask);
            }
        }
    }

    @Override
    public void onDialogClosed(boolean positiveResult) {
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
