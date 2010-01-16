/**
 * DateUtils.java
 * 
 * TKBirthdayReminder (c) Thomas Künneth 2009
 * 
 * Alle Rechte beim Autoren. All rights reserved.
 */
package com.thomaskuenneth.android.birthday;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.Context;
import android.util.Log;

public class TKDateUtils {

	private enum ABC {
		aries, taurus, gemini, cancer, leo, virgo, libra, scorpius, sagittarius, capricornus, aquarius, pisces
	};

	public static final SimpleDateFormat FORMAT_YYYYMMDD = new SimpleDateFormat(
			"yyyyMMdd");

	public static final SimpleDateFormat FORMAT_WEEKDAY = new SimpleDateFormat(
			"EEE");

	private static final DateFormat FORMAT_SHORT_DATE = SimpleDateFormat
			.getDateInstance(DateFormat.SHORT);

	private static final DateFormat FORMAT_SHORT_TIME = SimpleDateFormat
			.getTimeInstance(DateFormat.SHORT);

	public static String getNotificationDateAsString(Context context, Date date) {
		return TKBirthdayReminder.getStringFromResources(context,
				R.string.next_notification, FORMAT_SHORT_DATE.format(date),
				FORMAT_SHORT_TIME.format(date));
	}

	public static String getBirthdayAsString(Context context, Date birthday) {
		if (birthday == null) {
			return TKBirthdayReminder.getStringFromResources(context,
					R.string.no_birthday_set);
		}
		Date buffer = new Date();
		int days = getBirthdayInDays(birthday, buffer);
		String when;
		if (days == 0) {
			when = TKBirthdayReminder.getStringFromResources(context,
					R.string.today);
		} else if (days == 1) {
			when = TKBirthdayReminder.getStringFromResources(context,
					R.string.tomorrow);
		} else if (days == -1) {
			when = TKBirthdayReminder.getStringFromResources(context,
					R.string.yesterday);
		} else if (days > 1) {
			when = TKBirthdayReminder.getStringFromResources(context,
					R.string.in_n_days, days);
		} else {
			when = TKBirthdayReminder.getStringFromResources(context,
					R.string.n_days_ago, -days);
		}
		int resId = (days < 0) ? R.string.past : R.string.present_and_future;
		return TKBirthdayReminder.getStringFromResources(context, resId,
				getAge(birthday), when, FORMAT_WEEKDAY.format(buffer));
	}

	public static String getBirthdayDateAsString(Date birthday) {
		if (birthday == null) {
			return "";
		}
		return FORMAT_SHORT_DATE.format(birthday);
	}

	public static int getBirthdayInDays(Date birthday) {
		return getBirthdayInDays(birthday, null);
	}

	public static synchronized int getBirthdayInDays(Date birthday, Date buffer) {
		if (birthday != null) {
			String stringBirthday = FORMAT_YYYYMMDD.format(birthday);
			int monthBirthday = Integer
					.parseInt(stringBirthday.substring(4, 6)) - 1;
			int dayBirthday = Integer.parseInt(stringBirthday.substring(6, stringBirthday.length()));
			Calendar cal = new GregorianCalendar();
			int daysToday = cal.get(Calendar.DAY_OF_YEAR);
			cal.set(Calendar.DAY_OF_MONTH, dayBirthday);
			cal.set(Calendar.MONTH, monthBirthday);
			if (buffer != null) {
				buffer.setTime(cal.getTimeInMillis());
			}
			int daysBirthday = cal.get(Calendar.DAY_OF_YEAR);
			return daysBirthday - daysToday;
		}
		return 0;
	}

	public static int getAge(Date birthday) {
		String stringBirthday = FORMAT_YYYYMMDD.format(birthday);
		int yearBirthday = Integer.parseInt(stringBirthday.substring(0, 4));
		Calendar cal = new GregorianCalendar();
		int yearToday = cal.get(Calendar.YEAR);
		int age = yearToday - yearBirthday;
		return age;
	}

	public static String getStringFromDate(Date birthday, String string) {
		StringBuilder sb = new StringBuilder();
		if (string != null) {
			Pattern p = Pattern.compile(
					"(.*)Birthday=\\d\\d\\d\\d\\d\\d\\d\\d(.*)$",
					Pattern.DOTALL);
			Matcher m = p.matcher(string.subSequence(0, string.length()));
			if (m.matches()) {
				sb.append(m.group(1).trim());
				sb.append(m.group(2).trim());
			} else {
				sb.append(string.trim());
			}
		}
		if (birthday != null) {
			if (sb.length() > 0) {
				sb.append('\n');
			}
			sb.append("Birthday=");
			sb.append(FORMAT_YYYYMMDD.format(birthday));
		}
		return sb.toString();
	}

	public static Date getDateFromString(String string) {
		if (string != null) {
			Pattern p = Pattern.compile(
					".*Birthday=(\\d\\d\\d\\d\\d\\d\\d\\d).*$", Pattern.DOTALL);
			Matcher m = p.matcher(string.subSequence(0, string.length()));
			if (m.matches()) {
				String date = m.group(1);
				try {
					return FORMAT_YYYYMMDD.parse(date);
				} catch (Throwable thr) {
					// no further action taken
				}
			}
		}
		return null;
	}
}
