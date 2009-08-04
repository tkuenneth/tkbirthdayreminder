/**
 * WhatsNew.java
 * 
 * TKBirthdayReminder (c) Thomas Künneth 2009
 * 
 * Alle Rechte beim Autoren. All rights reserved.
 */
package com.thomaskuenneth.android.birthday;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;

public class WhatsNew extends Activity {

	private static final int MESSAGE = 1;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		showDialog(MESSAGE);
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		if (id == MESSAGE) {
			AlertDialog.Builder builder = new AlertDialog.Builder(WhatsNew.this);
			builder.setTitle(R.string.welcome);
			builder.setIcon(R.drawable.birthdaycake_32);
			View textView = getLayoutInflater().inflate(R.layout.welcome, null);
			builder.setView(textView);
			builder.setPositiveButton(R.string.alert_dialog_continue,
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							setResult(RESULT_OK);
							finish();
						}

					});
			builder.setNegativeButton(R.string.alert_dialog_abort,
					new DialogInterface.OnClickListener() {

						@Override
						public void onClick(DialogInterface dialog, int which) {
							setResult(RESULT_CANCELED);
							finish();
						}

					});
			builder.setCancelable(false);
			return builder.create();
		}
		return null;
	}
}
