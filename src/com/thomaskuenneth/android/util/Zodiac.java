/**
 * Zodiac.java
 * 
 * TKBirthdayReminder (c) Thomas Künneth 2009
 * Alle Rechte beim Autoren. All rights reserved.
 */
package com.thomaskuenneth.android.util;

import java.util.Calendar;
import java.util.Date;
import java.util.Hashtable;

import android.content.Context;

import com.thomaskuenneth.android.birthday.R;

/**
 * Diese Klasse bildet die 12 Tierkreiszeichen in eine {@link Hashtable} ab,
 * deren Schlüssel der Monat ist.
 * 
 * @author Thomas Künneth
 * 
 */
public class Zodiac extends Hashtable<Integer, Sign> {

	private static final long serialVersionUID = -1206152506632529038L;

	private static final Zodiac INSTANCE = new Zodiac();

	private static final Calendar CAL = Calendar.getInstance();

	private Zodiac() {
		super();
		put(Calendar.JANUARY, new Sign(21, R.string.capricornus,
				R.string.aquarius));
		put(Calendar.FEBRUARY, new Sign(20, R.string.aquarius, R.string.pisces));
		put(Calendar.MARCH, new Sign(21, R.string.pisces, R.string.aries));
		put(Calendar.APRIL, new Sign(21, R.string.aries, R.string.taurus));
		put(Calendar.MAY, new Sign(22, R.string.taurus, R.string.gemini));
		put(Calendar.JUNE, new Sign(22, R.string.gemini, R.string.cancer));
		put(Calendar.JULY, new Sign(23, R.string.cancer, R.string.leo));
		put(Calendar.AUGUST, new Sign(24, R.string.leo, R.string.virgo));
		put(Calendar.SEPTEMBER, new Sign(24, R.string.virgo, R.string.libra));
		put(Calendar.OCTOBER, new Sign(24, R.string.libra, R.string.scorpius));
		put(Calendar.NOVEMBER, new Sign(23, R.string.scorpius,
				R.string.sagittarius));
		put(Calendar.DECEMBER, new Sign(22, R.string.sagittarius,
				R.string.capricornus));
	}

	public static Zodiac getInstance() {
		return INSTANCE;
	}

	public static Sign getSign(int month) {
		return getInstance().get(month);
	}

	public static String getSign(Context context, Date date) {
		if ((context != null) && (date != null)) {
			CAL.setTime(date);
			Sign sign = getSign(CAL.get(Calendar.MONTH));
			if (sign != null) {
				return context.getString(CAL.get(Calendar.DAY_OF_MONTH) >= sign
						.getFirstDayOfSecondSign() ? sign.getSecondSign()
						: sign.getFirstSign());
			}
		}
		return "";
	}
}
