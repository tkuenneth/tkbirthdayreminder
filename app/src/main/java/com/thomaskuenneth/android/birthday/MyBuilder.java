package com.thomaskuenneth.android.birthday;

import android.app.Notification;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;

class MyBuilder {

    private final NotificationCompat.Builder builder;
    String mContentTitle;
    String mContentText;

    MyBuilder(NotificationCompat.Builder builder) {
        this.builder = builder;
    }

    void setGroup(String g) {
        builder.setGroup(g);
    }

    NotificationCompat.Builder setContentTitle(String t) {
        builder.setContentTitle(t);
        this.mContentTitle = t;
        return builder;
    }

    void setContentText(String t) {
        builder.setContentText(t);
        this.mContentText = t;
    }

    void setSound(Uri u) {
        builder.setSound(u);
    }

    void setStyle(NotificationCompat.Style style) {
        builder.setStyle(style);
    }

    Notification build() {
        return builder.build();
    }
}
