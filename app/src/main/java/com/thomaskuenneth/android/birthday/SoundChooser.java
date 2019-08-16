/*
 * SoundChooser.java
 *
 * TKBirthdayReminder (c) Thomas Künneth 2017 - 2019
 * Alle Rechte beim Autoren. All rights reserved.
 */
package com.thomaskuenneth.android.birthday;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.widget.Toast;

public class SoundChooser extends Activity {

    private static final int RQ_PICK_SOUND = 0x03091938;
    private static final String NOTIFICATION_SOUND = "notificationSound";

    @Override
    protected void onStart() {
        super.onStart();
        Intent i = new Intent(RingtoneManager.ACTION_RINGTONE_PICKER);
        i.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_DEFAULT,
                Boolean.FALSE);
        i.putExtra(RingtoneManager.EXTRA_RINGTONE_SHOW_SILENT, Boolean.TRUE);
        i.putExtra(RingtoneManager.EXTRA_RINGTONE_TYPE,
                RingtoneManager.TYPE_ALL);
        i.putExtra(RingtoneManager.EXTRA_RINGTONE_TITLE,
                getString(R.string.notification_sound));
        String current = getNotificationSoundAsString(this);
        if (current != null) {
            i.putExtra(RingtoneManager.EXTRA_RINGTONE_EXISTING_URI,
                    Uri.parse(current));
        }
        try {
            startActivityForResult(i, RQ_PICK_SOUND);
        } catch (ActivityNotFoundException e) {
            Toast.makeText(this, getString(R.string.activity_not_found), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RQ_PICK_SOUND) {
            if (resultCode == RESULT_OK) {
                Bundle b = data.getExtras();
                if (b != null) {
                    Uri uri = (Uri) b
                            .get(RingtoneManager.EXTRA_RINGTONE_PICKED_URI);
                    if (uri != null) {
                        SharedPreferences.Editor editor = TKBirthdayReminder.getSharedPreferences(this).edit();
                        editor.putString(NOTIFICATION_SOUND, uri.toString());
                        editor.apply();
                    }
                }
            }
            finish();
        }
    }

    public static String getNotificationSoundAsString(Context context) {
        return TKBirthdayReminder.getSharedPreferences(context).getString(NOTIFICATION_SOUND, null);
    }
}
