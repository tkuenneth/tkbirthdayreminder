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

public class TKDateUtils {

	private static final SimpleDateFormat FORMAT_YYYYMMDD = new SimpleDateFormat(
			"yyyyMMdd");

	private static final DateFormat FORMAT_SHORT_DATE = SimpleDateFormat
			.getDateInstance(DateFormat.SHORT);

	private static final DateFormat FORMAT_SHORT_TIME = SimpleDateFormat
			.getTimeInstance(DateFormat.SHORT);

	public static String getNotificationDateAsString(Date date) {
		return TKBirthdayReminder.getStringFromResources(
				R.string.next_notification, FORMAT_SHORT_DATE.format(date),
				FORMAT_SHORT_TIME.format(date));
	}

	public static String getBirthdayAsString(Date birthday) {
		if (birthday == null) {
			return TKBirthdayReminder
					.getStringFromResources(R.string.no_birthday_set);
		}
		int days = getBirthdayInDays(birthday);
		String when;
		if (days == 0) {
			when = TKBirthdayReminder.getStringFromResources(R.string.today);
		} else if (days == 1) {
			when = TKBirthdayReminder.getStringFromResources(R.string.tomorrow);
		} else if (days == -1) {
			when = TKBirthdayReminder
					.getStringFromResources(R.string.yesterday);
		} else if (days > 1) {
			when = TKBirthdayReminder.getStringFromResources(
					R.string.in_n_days, days);
		} else {
			when = TKBirthdayReminder.getStringFromResources(
					R.string.n_days_ago, -days);
		}
		int resId = (days < 0) ? R.string.past : R.string.present_and_future;
		return TKBirthdayReminder.getStringFromResources(resId,
				getAge(birthday), when, FORMAT_SHORT_DATE.format(birthday));
	}

	public static int getBirthdayInDays(Date birthday) {
		String stringBirthday = FORMAT_YYYYMMDD.format(birthday);
		int monthBirthday = Integer.parseInt(stringBirthday.substring(4, 6)) - 1;
		int dayBirthday = Integer.parseInt(stringBirthday.substring(6, 8));
		Calendar cal = new GregorianCalendar();
		int daysToday = cal.get(Calendar.DAY_OF_YEAR);
		cal.set(Calendar.DAY_OF_MONTH, dayBirthday);
		cal.set(Calendar.MONTH, monthBirthday);
		int daysBirthday = cal.get(Calendar.DAY_OF_YEAR);
		return daysBirthday - daysToday;
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
					"(.*)Birthday=\\d\\d\\d\\d\\d\\d\\d\\d(.*)$", Pattern.DOTALL);
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
