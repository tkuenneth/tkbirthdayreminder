/*
 * Utils.java
 *
 * TKBirthdayReminder (c) Thomas Künneth 2009 - 2023
 * All rights reserved.
 */
package com.thomaskuenneth.android.birthday;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.ContactsContract;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;

import com.google.android.material.appbar.AppBarLayout;

import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class Utils {

    private static final String TAG = Utils.class.getSimpleName();

    static final int INVISIBLE_YEAR = 9996;

    static final SimpleDateFormat FORMAT_YYYYMMDD = new SimpleDateFormat("yyyyMMdd", Locale.US);

    private static final DateFormat FORMAT_SHORT_DATE = SimpleDateFormat.getDateInstance(DateFormat.SHORT);

    private static final DateFormat FORMAT_SHORT_TIME = SimpleDateFormat.getTimeInstance(DateFormat.SHORT);

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
            sb.append(String.format(Locale.getDefault(), "-%02d-%02d", 1 + cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)));
        }
        return sb.toString();
    }

    static String getNotificationDateAsString(Context context, Date date) {
        return TKBirthdayReminder.getStringFromResources(context, R.string.next_notification, FORMAT_SHORT_DATE.format(date), FORMAT_SHORT_TIME.format(date));
    }

    static String getBirthdayAsString(Context context, Date birthday) {
        final SimpleDateFormat formatWeekday = new SimpleDateFormat("EEE", Locale.getDefault());
        if (birthday == null) {
            return TKBirthdayReminder.getStringFromResources(context, R.string.no_birthday_set);
        }
        Date buffer = new Date();
        int days = getBirthdayInDays(birthday, buffer);
        String when;
        if (days == 0) {
            when = TKBirthdayReminder.getStringFromResources(context, R.string.today);
        } else if (days == 1) {
            when = TKBirthdayReminder.getStringFromResources(context, R.string.tomorrow);
        } else if (days == -1) {
            when = TKBirthdayReminder.getStringFromResources(context, R.string.yesterday);
        } else if (days > 1) {
            when = TKBirthdayReminder.getStringFromResources(context, R.string.in_n_days, days);
        } else {
            when = TKBirthdayReminder.getStringFromResources(context, R.string.n_days_ago, -days);
        }
        int age = getAge(birthday);
        if (age < 0) {
            return TKBirthdayReminder.getStringFromResources(context, R.string.birthday_no_year, when, formatWeekday.format(buffer));
        } else {
            int resId = (days < 0) ? R.string.past : R.string.present_and_future;
            return TKBirthdayReminder.getStringFromResources(context, resId, age, when, formatWeekday.format(buffer));
        }
    }

    static String getBirthdayDateAsString(DateFormat format, BirthdayItem item) {
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

    static synchronized int getBirthdayInDays(Date birthday, Date buffer) {
        Calendar cal = Calendar.getInstance();
        int daysToday = cal.get(Calendar.DAY_OF_YEAR);
        if (birthday != null) {
            int year = cal.get(Calendar.YEAR);
            cal.setTime(birthday);
            cal.set(Calendar.YEAR, year);
        }
        if (buffer != null) {
            buffer.setTime(cal.getTimeInMillis());
        }
        return cal.get(Calendar.DAY_OF_YEAR) - daysToday;
    }

    private static int getAge(Date birthday) {
        String stringBirthday = FORMAT_YYYYMMDD.format(birthday);
        int yearBirthday = Integer.parseInt(stringBirthday.substring(0, 4));
        Calendar cal = Calendar.getInstance();
        int yearToday = cal.get(Calendar.YEAR);
        return yearToday - yearBirthday;
    }

    static String removeBirthdayFromString(String string) {
        StringBuilder sb = new StringBuilder();
        if (string != null) {
            Pattern p = Pattern.compile("(.*)Birthday=\\d\\d\\d\\d\\d\\d\\d\\d(.*)$", Pattern.DOTALL);
            Matcher m = p.matcher(string.subSequence(0, string.length()));
            if (m.matches()) {
                String group1 = trim(m.group(1));
                sb.append(group1);
                String group2 = trim(m.group(2));
                if ((group1.length() > 0) && (group2.length() > 0)) {
                    sb.append('\n');
                }
                sb.append(group2);
            } else {
                sb.append(string);
            }
        }
        return sb.toString();
    }

    static Date getDateFromString(String string) {
        Date result = null;
        if (string != null) {
            Pattern p = Pattern.compile(".*Birthday=(\\d\\d\\d\\d\\d\\d\\d\\d).*$", Pattern.DOTALL);
            Matcher m = p.matcher(string.subSequence(0, string.length()));
            if (m.matches()) {
                String date = m.group(1);
                if (date != null) {
                    try {
                        result = FORMAT_YYYYMMDD.parse(date);
                    } catch (Throwable tr) {
                        logError(TAG, "getDateFromString()", tr);
                    }
                }
            }
        }
        return result;
    }

    static Date getDateFromString1(String string) {
        Date result = null;
        if (string != null) {
            Pattern p = Pattern.compile("(\\d\\d\\d\\d).*(\\d\\d).*(\\d\\d)", Pattern.DOTALL);
            Matcher m = p.matcher(string.subSequence(0, string.length()));
            if (m.matches()) {
                String date = m.group(1) + m.group(2) + m.group(3);
                try {
                    result = FORMAT_YYYYMMDD.parse(date);
                } catch (Throwable tr) {
                    Log.e(TAG, String.format("getDateFromString1(%s)", date), tr);
                }
            } else {
                p = Pattern.compile(".*-(\\d\\d)-(\\d\\d)$", Pattern.DOTALL);
                m = p.matcher(string.subSequence(0, string.length()));
                if (m.matches()) {
                    Calendar cal = Calendar.getInstance();
                    try {
                        String group1 = m.group(1);
                        if (group1 != null) {
                            cal.set(Calendar.MONTH, Integer.parseInt(group1) - 1);
                        }
                        String group2 = m.group(2);
                        if (group2 != null) {
                            cal.set(Calendar.DAY_OF_MONTH, Integer.parseInt(group2));
                        }
                        cal.set(Calendar.YEAR, INVISIBLE_YEAR);
                        result = cal.getTime();
                    } catch (Throwable tr) {
                        logError(TAG, "getDateFromString1()", tr);
                    }
                }
            }
        }
        return result;
    }

    static String trim(String s) {
        if (s != null) {
            return s.trim();
        }
        return "";
    }

    static void closeCursorCatchThrowable(Cursor c) {
        try {
            c.close();
        } catch (Throwable t) {
            logError(TAG, "closeCursor()", t);
        }
    }

    static void logDebug(String tag, String msg) {
        if (Log.isLoggable(tag, Log.DEBUG)) {
            Log.d(tag, msg);
        }
    }

    static void logError(String tag, String msg, Throwable tr) {
        if (Log.isLoggable(tag, Log.ERROR)) {
            Log.e(tag, msg, tr);
        }
    }

    public static Bitmap loadBitmap(BirthdayItem item, Context context) {
        Uri uri = ContentUris.withAppendedId(ContactsContract.Contacts.CONTENT_URI, item.getId());
        try (InputStream input = ContactsContract.Contacts.openContactPhotoInputStream(context.getContentResolver(), uri)) {
            Bitmap picture = null;
            if (input != null) {
                picture = BitmapFactory.decodeStream(input);
            } else {
                Drawable d = ContextCompat.getDrawable(context, R.drawable.ic_launcher_foreground);
                if (d != null) {
                    picture = getBitmap(d);
                }
            }
            if (picture != null) {
                Bitmap old = item.getPicture();
                if (old != null && !old.isRecycled()) {
                    old.recycle();
                }
                item.setPicture(picture);
                return picture;
            }
        } catch (Throwable tr) {
            Utils.logError(TAG, "loadBitmap()", tr);
        }
        return null;
    }

    private static Bitmap getBitmap(Drawable vectorDrawable) {
        Bitmap bitmap = Bitmap.createBitmap(
                vectorDrawable.getIntrinsicWidth(),
                vectorDrawable.getIntrinsicHeight(),
                Bitmap.Config.ARGB_8888
        );
        Canvas canvas = new Canvas(bitmap);
        vectorDrawable.setBounds(
                0,
                0,
                canvas.getWidth(),
                canvas.getHeight()
        );
        vectorDrawable.draw(canvas);
        return bitmap;
    }

    static void configureActionBar(AppCompatActivity activity) {
        Toolbar actionBar = activity.findViewById(R.id.actionBar);
        activity.setSupportActionBar(actionBar);
        ViewCompat.setOnApplyWindowInsetsListener(actionBar, (v, insets) -> {
            AppBarLayout.LayoutParams params = (AppBarLayout.LayoutParams) v.getLayoutParams();
            params.topMargin = insets.getSystemWindowInsetTop();
            v.setLayoutParams(params);
            return insets;
        });
    }
}
