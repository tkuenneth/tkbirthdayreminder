/*
 * DateUtils.java
 * 
 * TKBirthdayReminder (c) Thomas Künneth 2009 - 2017
 * 
 * Alle Rechte beim Autoren. All rights reserved.
 */
package com.thomaskuenneth.android.birthday;

import android.content.Context;
import android.util.Log;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Diese Klasse enthält datums- und kalenderbezogene Hilfsmethoden.
 *
 * @author Thomas Künneth
 */
class TKDateUtils {

    private static final String TAG = TKDateUtils.class.getSimpleName();

    static final int INVISIBLE_YEAR = 9996;

    static final SimpleDateFormat FORMAT_YYYYMMDD = new SimpleDateFormat(
            "yyyyMMdd", Locale.US);

    private static final SimpleDateFormat FORMAT_WEEKDAY = new SimpleDateFormat(
            "EEE", Locale.getDefault());

    private static final DateFormat FORMAT_SHORT_DATE = SimpleDateFormat
            .getDateInstance(DateFormat.SHORT);

    private static final DateFormat FORMAT_SHORT_TIME = SimpleDateFormat
            .getTimeInstance(DateFormat.SHORT);

    static String getDateAsStringYYYY_MM_DD(Date date) {
        StringBuilder sb = new StringBuilder();
        if (date != null) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(date);
            int year = cal.get(Calendar.YEAR);
            if (year == INVISIBLE_YEAR) {
                sb.append("-");
            } else {
                sb.append(String.format(Locale.getDefault(), "%04d", year));
            }
            sb.append(String.format(Locale.getDefault(), "-%02d-%02d", 1 + cal.get(Calendar.MONTH),
                    cal.get(Calendar.DAY_OF_MONTH)));
        }
        return sb.toString();
    }

    static String getNotificationDateAsString(Context context, Date date) {
        return TKBirthdayReminder.getStringFromResources(context,
                R.string.next_notification, FORMAT_SHORT_DATE.format(date),
                FORMAT_SHORT_TIME.format(date));
    }

    static String getBirthdayAsString(Context context, Date birthday) {
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
        int age = getAge(birthday);
        if (age < 0) {
            return TKBirthdayReminder.getStringFromResources(context,
                    R.string.birthday_no_year, when,
                    FORMAT_WEEKDAY.format(buffer));
        } else {
            int resId = (days < 0) ? R.string.past
                    : R.string.present_and_future;
            return TKBirthdayReminder.getStringFromResources(context, resId,
                    age, when, FORMAT_WEEKDAY.format(buffer));
        }
    }

    static String getBirthdayDateAsString(DateFormat format,
                                          BirthdayItem item) {
        StringBuilder sb = new StringBuilder();
        Date birthday = item.getBirthday();
        if (birthday != null) {
            Calendar cal = Calendar.getInstance();
            cal.setTime(birthday);
            int year = cal.get(Calendar.YEAR);
            if (year == INVISIBLE_YEAR) {
                sb.append(format.format(birthday));
            } else {
                sb.append(FORMAT_SHORT_DATE.format(birthday));
            }
        }
        return sb.toString();
    }

    static int getBirthdayInDays(Date birthday) {
        return getBirthdayInDays(birthday, null);
    }

    private static synchronized int getBirthdayInDays(Date birthday, Date buffer) {
        if (birthday != null) {
            String stringBirthday = FORMAT_YYYYMMDD.format(birthday);
            int monthBirthday = Integer
                    .parseInt(stringBirthday.substring(4, 6)) - 1;
            int dayBirthday = Integer.parseInt(stringBirthday.substring(6,
                    stringBirthday.length()));
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

    /**
     * Ermittelt das Alter.
     *
     * @param birthday Geburtsdatum
     * @return Alter
     */
    private static int getAge(Date birthday) {
        String stringBirthday = FORMAT_YYYYMMDD.format(birthday);
        int yearBirthday = Integer.parseInt(stringBirthday.substring(0, 4));
        Calendar cal = new GregorianCalendar();
        int yearToday = cal.get(Calendar.YEAR);
        return yearToday - yearBirthday;
    }

    static String getStringFromDate(Date birthday, String string) {
        StringBuilder sb = new StringBuilder();
        if (string != null) {
            Pattern p = Pattern.compile(
                    "(.*)Birthday=\\d\\d\\d\\d\\d\\d\\d\\d(.*)$",
                    Pattern.DOTALL);
            Matcher m = p.matcher(string.subSequence(0, string.length()));
            if (m.matches()) {
                String group1 = m.group(1).trim();
                sb.append(group1);
                String group2 = m.group(2).trim();
                if ((group1.length() > 0) && (group2.length() > 0)) {
                    sb.append('\n');
                }
                sb.append(group2);
            } else {
                sb.append(string);
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

    static Date getDateFromString(String string) {
        Date result = null;
        if (string != null) {
            Pattern p = Pattern.compile(
                    ".*Birthday=(\\d\\d\\d\\d\\d\\d\\d\\d).*$", Pattern.DOTALL);
            Matcher m = p.matcher(string.subSequence(0, string.length()));
            if (m.matches()) {
                String date = m.group(1);
                try {
                    result = FORMAT_YYYYMMDD.parse(date);
                } catch (Throwable tr) {
                    // Log.e(TAG, "getDateFromString()", tr);
                }
            }
        }
        return result;
    }

    static Date getDateFromString1(String string) {
        Date result = null;
        if (string != null) {
            Pattern p = Pattern.compile("(\\d\\d\\d\\d).*(\\d\\d).*(\\d\\d)",
                    Pattern.DOTALL);
            Matcher m = p.matcher(string.subSequence(0, string.length()));
            if (m.matches()) {
                String date = m.group(1) + m.group(2) + m.group(3);
                try {
                    result = FORMAT_YYYYMMDD.parse(date);
                } catch (Throwable tr) {
                    Log.e(TAG, "getDateFromString1()", tr);
                }
            } else {
                p = Pattern.compile(".*-(\\d\\d)-(\\d\\d)$", Pattern.DOTALL);
                m = p.matcher(string.subSequence(0, string.length()));
                if (m.matches()) {
                    Calendar cal = Calendar.getInstance();
                    try {
                        cal.set(Calendar.MONTH,
                                Integer.parseInt(m.group(1)) - 1);
                        cal.set(Calendar.DAY_OF_MONTH,
                                Integer.parseInt(m.group(2)));
                        cal.set(Calendar.YEAR, INVISIBLE_YEAR);
                        result = cal.getTime();
                    } catch (Throwable tr) {
                        // Log.e(TAG, "getDateFromString1()", tr);
                    }
                }
            }
        }
        return result;
    }
}
