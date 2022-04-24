/*
 * WidgetPreferenceFragment.java
 *
 * TKBirthdayReminder (c) Thomas Künneth 2020 - 2022
 * All rights reserved.
 */
package com.thomaskuenneth.android.birthday;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceDialogFragmentCompat;

public class WidgetPreferenceFragment extends PreferenceDialogFragmentCompat
        implements SeekBar.OnSeekBarChangeListener {

    private static final String TAG = WidgetPreference.class.getSimpleName();
    private static final String OPACITY = "opacity";

    private Context context;
    private TextView seekbarInfo;
    private SeekBar seekbar;

    @Override
    protected void onBindDialogView(@NonNull View view) {
        super.onBindDialogView(view);
        context = getContext();
        seekbarInfo = view.findViewById(R.id.widget_opacity_info);
        seekbar = view.findViewById(R.id.widget_opacity);
        seekbar.setMax(255);
        int progress = getOpacity(context);
        seekbar.setProgress(progress);
        updateSeekbarInfo(progress);
        seekbar.setOnSeekBarChangeListener(this);
    }

    @Override
    public void onDialogClosed(boolean positiveResult) {
        final Context context = getContext();
        if ((context != null) && (positiveResult)) {
            SharedPreferences prefs = context.getSharedPreferences(TAG,
                    Context.MODE_PRIVATE);
            SharedPreferences.Editor e = prefs.edit();
            e.putInt(OPACITY, seekbar.getProgress());
            e.apply();
            TKBirthdayReminder.updateWidgets(context);
        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress,
                                  boolean fromUser) {
        updateSeekbarInfo(progress);
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
    }

    static int getOpacity(Context c) {
        SharedPreferences prefs = c.getSharedPreferences(TAG,
                Context.MODE_PRIVATE);
        return prefs.getInt(OPACITY, 128);
    }

    private void updateSeekbarInfo(int progress) {
        seekbarInfo.setText(context.getString(R.string.int_slash_int, progress, seekbar.getMax()));
    }
}
