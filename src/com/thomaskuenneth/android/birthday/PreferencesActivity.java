/**
 * PreferencesActivity.java
 * 
 * TKBirthdayReminder (c) Thomas Künneth 2010
 * Alle Rechte beim Autoren. All rights reserved.
 */
package com.thomaskuenneth.android.birthday;

import android.os.Bundle;
import android.preference.PreferenceActivity;

/**
 * Diese Activity fasst Einstellungen in einer Activity zusammen.
 * 
 * @author Thomas Künneth
 * 
 */
public class PreferencesActivity extends PreferenceActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		addPreferencesFromResource(R.xml.preferences);
	}

}
