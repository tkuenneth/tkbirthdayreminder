/*
 * AlarmReceiver.java
 * 
 * TKBirthdayReminder (c) Thomas Künneth 2009 - 2017
 * Alle Rechte beim Autoren. All rights reserved.
 */
package com.thomaskuenneth.android.birthday;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.PowerManager;
import android.provider.ContactsContract;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.IntentCompat;
import android.support.v7.app.NotificationCompat;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.StyleSpan;
import android.view.WindowManager;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static android.app.PendingIntent.FLAG_ONE_SHOT;

/**
 * Diese Klasse tritt in Aktion, wenn ein Alarm ausgelöst wurde.
 *
 * @author Thomas Künneth
 * @see BroadcastReceiver
 */
public class AlarmReceiver extends BroadcastReceiver {

    private static final String TAG = AlarmReceiver.class.getSimpleName();
    private static final int MAX_NOTIFICATIONS = 14;
    private static final int MIN_NOTIFICATIONS_FOR_GROUP = 2; // 4 ist Android 7.x default

    @Override
    public void onReceive(final Context context, Intent intent) {
        BootCompleteReceiver.startAlarm(context, true);
        PowerManager pm = (PowerManager) context
                .getSystemService(Context.POWER_SERVICE);
        final PowerManager.WakeLock wl = pm.newWakeLock(
                PowerManager.PARTIAL_WAKE_LOCK, TAG);
        wl.acquire();
        Runnable r = new Runnable() {
            public void run() {
                ContactsList cl = new ContactsList(context);
                List<BirthdayItem> listNotifications = cl
                        .getNotificationsList();
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
                    WindowManager wm = (WindowManager) context
                            .getSystemService(Context.WINDOW_SERVICE);
                    List<NotificationCompat.Builder> builders = new ArrayList<>();
                    long when = System.currentTimeMillis();
                    int numNames = 0;
                    for (int i = 0; i < visible; i++) {
                        BirthdayItem event = listNotifications.get(i);
                        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.withAppendedPath(
                                ContactsContract.Contacts.CONTENT_URI,
                                Long.toString(event.getId())));
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | IntentCompat.FLAG_ACTIVITY_CLEAR_TASK);
                        NotificationCompat.Builder b = createBuilder(context,
                                when--,
                                R.mipmap.ic_launcher,
                                intent);
                        if (total >= MIN_NOTIFICATIONS_FOR_GROUP) {
                            b.setGroup(Constants.TKBIRTHDAYREMINDER);
                        }
                        Date birthday = event.getBirthday();
                        Bitmap picture = BirthdayItemListAdapter.loadBitmap(event,
                                context, TKBirthdayReminder.getImageHeight(wm));
                        b.setContentTitle(event.getName())
                                .setLargeIcon(picture)
                                .setContentText(TKDateUtils.getBirthdayAsString(context, birthday));
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
                        Intent intent = new Intent(context, TKBirthdayReminder.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | IntentCompat.FLAG_ACTIVITY_CLEAR_TASK);
                        NotificationCompat.Builder summary = createBuilder(context,
                                when,
                                R.mipmap.ic_launcher,
                                intent);
                        summary.setGroup(Constants.TKBIRTHDAYREMINDER);
                        summary.setGroupSummary(true);
                        summary.setContentTitle(context.getString(R.string.app_name));
                        NotificationCompat.InboxStyle style = new NotificationCompat.InboxStyle();
                        for (int i = builders.size() - 1; i >= 0; i--) {
                            NotificationCompat.Builder builder = builders.get(i);
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
                        if ((i + 1) == size) {
                            String tune = SoundChooser
                                    .getNotificationSoundAsString(context);
                            if (tune != null) {
                                builders.get(i).setSound(Uri.parse(tune));
                            }
                        }
                        NotificationCompat.Builder builder = builders.get(i);
                        nm.notify(i, builder.build());
                    }
                }
                wl.release();
            }
        };
        Thread t = new Thread(r);
        t.start();
    }

    private static NotificationCompat.Builder createBuilder(Context context,
                                                            long when,
                                                            int smallIcon,
                                                            Intent intent) {
        NotificationCompat.Builder b = new NotificationCompat.Builder(context);
        b.setSmallIcon(smallIcon)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setWhen(when)
                .setSortKey(Long.toHexString(when))
                .setShowWhen(false)
                .setCategory(NotificationCompat.CATEGORY_EVENT)
                .setContentIntent(PendingIntent.getActivity(context, 0, intent, FLAG_ONE_SHOT));
        return b;
    }
}
