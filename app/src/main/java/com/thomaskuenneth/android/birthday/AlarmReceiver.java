/*
 * AlarmReceiver.java
 *
 * TKBirthdayReminder (c) Thomas Künneth 2009 - 2025
 * All rights reserved.
 */
package com.thomaskuenneth.android.birthday;

import static com.thomaskuenneth.android.birthday.Utils.loadBitmap;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.PowerManager;
import android.provider.ContactsContract;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.preference.PreferenceManager;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

public class AlarmReceiver extends BroadcastReceiver {

    private static final String TAG = AlarmReceiver.class.getSimpleName();
    private static final int MAX_NOTIFICATIONS = 14;
    private static final int MIN_NOTIFICATIONS_FOR_GROUP = 2;
    public static final long TEN_MINUTES_IN_MILLIS = 10 * 60 * 1000L;

    @Override
    public void onReceive(final Context context, Intent intent) {
        BootCompleteReceiver.startAlarm(context, true);
        PowerManager pm = context.getSystemService(PowerManager.class);
        final PowerManager.WakeLock wl = (pm != null) ? pm.newWakeLock(
                PowerManager.PARTIAL_WAKE_LOCK, TAG) : null;
        if (wl != null) {
            wl.acquire(TEN_MINUTES_IN_MILLIS);
        }
        initChannels(context);
        Runnable r = () -> {
            ContactsList cl = new ContactsList(context);
            List<BirthdayItem> list = cl.getNotificationsList();
            HashMap<String, Boolean> accounts = new HashMap<>();
            TKBirthdayReminder.clearAndFillAccountsMap(context, accounts, list);
            List<BirthdayItem> listNotifications = TKBirthdayReminder.getFilteredList(accounts, list);
            int total = listNotifications.size();
            final int visible, remaining;
            if (total > MAX_NOTIFICATIONS) {
                visible = MAX_NOTIFICATIONS;
                remaining = total - MAX_NOTIFICATIONS;
            } else {
                visible = total;
                remaining = 0;
            }
            if (visible > 0) {
                StringBuilder sbNames = new StringBuilder();
                List<MyBuilder> builders = new ArrayList<>();
                long when = System.currentTimeMillis();
                int numNames = 0;
                for (int i = 0; i < visible; i++) {
                    BirthdayItem event = listNotifications.get(i);
                    Intent intent1 = new Intent(Intent.ACTION_VIEW, Uri.withAppendedPath(
                            ContactsContract.Contacts.CONTENT_URI,
                            Long.toString(event.getId())));
                    intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    MyBuilder b = createBuilder(context,
                            when--,
                            intent1);
                    if (total >= MIN_NOTIFICATIONS_FOR_GROUP) {
                        b.builder.setGroup(Constants.TKBIRTHDAYREMINDER);
                    }
                    Date birthday = event.getBirthday();
                    Bitmap picture = loadBitmap(event, context);
                    b.setContentTitle(event.getName())
                            .setLargeIcon(picture)
                            .setContentText(Utils.getBirthdayAsString(context, birthday));
                    builders.add(b);
                    if (numNames < 2) {
                        if (sbNames.length() > 0) {
                            sbNames.append(", ");
                        }
                        sbNames.append(b.mContentTitle);
                        numNames += 1;
                    }
                }
                if (numNames < total) {
                    sbNames.append(context.getString(R.string.and_x_more, total - numNames));
                }
                if (total >= MIN_NOTIFICATIONS_FOR_GROUP) {
                    Intent intent1 = new Intent(context, TKBirthdayReminder.class);
                    intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    MyBuilder summary = createBuilder(context,
                            when,
                            intent1);
                    summary.setContentTitle(context.getString(R.string.app_name))
                            .setGroupSummary(true)
                            .setGroup(Constants.TKBIRTHDAYREMINDER);
                    NotificationCompat.InboxStyle style = new NotificationCompat.InboxStyle();
                    for (int i = builders.size() - 1; i >= 0; i--) {
                        MyBuilder builder = builders.get(i);
                        String s = String.format("%s - %s",
                                builder.mContentTitle,
                                builder.mContentText);
                        Spannable sb = new SpannableString(s);
                        sb.setSpan(new StyleSpan(Typeface.BOLD),
                                0,
                                builder.mContentTitle.length(),
                                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                        style.addLine(sb);
                    }
                    style.setBigContentTitle(summary.mContentTitle);
                    summary.setStyle(style);
                    if (remaining > 0) {
                        style.setSummaryText(context.getString(R.string.x_more_birthdays, remaining));
                    }
                    summary.setContentText(sbNames.toString());
                    builders.add(summary);
                }

                NotificationManagerCompat nm = NotificationManagerCompat.from(context);
                nm.cancelAll();
                int size = builders.size();
                for (int i = 0; i < size; i++) {
                    if ((Build.VERSION.SDK_INT < Build.VERSION_CODES.O) && ((i + 1) == size)) {
                        String tune = SoundChooser
                                .getNotificationSoundAsString(context);
                        if (tune != null) {
                            builders.get(i).setSound(Uri.parse(tune));
                        }
                    }
                    MyBuilder builder = builders.get(i);
                    Notification n = builder.build();
                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
                    if (prefs.getBoolean("notification_sound_insistent", false)) {
                        n.flags |= Notification.FLAG_INSISTENT;
                    }
                    try {
                        nm.notify(i, n);
                    } catch (SecurityException e) {
                        showToastMissingPermission(context);
                    }
                }
            }
            if (wl != null) {
                wl.release();
            }
        };
        Thread t = new Thread(r);
        t.start();
    }

    public static void showToastMissingPermission(Context context) {
        Toast.makeText(context, context.getString(R.string.missing_permission), Toast.LENGTH_SHORT).show();
    }

    public static void initChannels(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return;
        }
        NotificationManager nm = context.getSystemService(NotificationManager.class);
        if (nm != null) {
            NotificationChannel oldChannel = nm.getNotificationChannel(Constants.CHANNEL_ID_OLD);
            if (oldChannel != null) {
                nm.deleteNotificationChannel(Constants.CHANNEL_ID_OLD);
            }
            oldChannel = nm.getNotificationChannel(Constants.CHANNEL_ID_OLD_2);
            if (oldChannel != null) {
                nm.deleteNotificationChannel(Constants.CHANNEL_ID_OLD_2);
            }
            NotificationChannel channel = new NotificationChannel(Constants.CHANNEL_ID,
                    context.getString(R.string.notification_channel_name),
                    NotificationManager.IMPORTANCE_DEFAULT);
            nm.createNotificationChannel(channel);
        }
    }

    private static MyBuilder createBuilder(Context context,
                                           long when,
                                           Intent intent) {
        NotificationCompat.Builder b = new NotificationCompat.Builder(context, Constants.CHANNEL_ID);
        b.setSmallIcon(R.drawable.ic_notification)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setWhen(when)
                .setSortKey(Long.toHexString(when))
                .setShowWhen(false)
                .setCategory(NotificationCompat.CATEGORY_EVENT)
                .setContentIntent(PendingIntent.getActivity(context, 0,
                        intent, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE));
        return new MyBuilder(b);
    }
}
