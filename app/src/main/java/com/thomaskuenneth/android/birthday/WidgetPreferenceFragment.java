package com.thomaskuenneth.android.birthday;

import android.content.Context;
import android.content.SharedPreferences;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

import androidx.preference.PreferenceDialogFragmentCompat;

public class WidgetPreferenceFragment extends PreferenceDialogFragmentCompat
        implements SeekBar.OnSeekBarChangeListener {

    private static final String TAG = WidgetPreference.class.getSimpleName();
    private static final String OPACITY = "opacity";

    private TextView seekbarInfo;
    private SeekBar seekbar;

    @Override
    protected void onBindDialogView(View view) {
        super.onBindDialogView(view);
        seekbarInfo = view.findViewById(R.id.widget_opacity_info);
        seekbar = view.findViewById(R.id.widget_opacity);
        seekbar.setMax(255);
        seekbar.setOnSeekBarChangeListener(this);
        seekbar.setProgress(getOpacity(getContext()));
    }

    @Override
    public void onDialogClosed(boolean positiveResult) {
        final Context context = getContext();
        if (positiveResult) {
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
        seekbarInfo.setText(getContext().getString(R.string.int_slash_int, progress, seekBar.getMax()));
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
}
