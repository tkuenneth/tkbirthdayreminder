/*
 * BottomSpace.java
 *
 * TKBirthdayReminder (c) Thomas Künneth 2025
 * All rights reserved.
 */
package com.thomaskuenneth.android.birthday;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.View;

import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class BottomSpace extends View {

    public BottomSpace(Context context, AttributeSet attrs) {
        super(context, attrs);
        setBackgroundColor(Color.TRANSPARENT);
        ViewCompat.setOnApplyWindowInsetsListener(this, (view, insets) -> {
            view.getLayoutParams().height = insets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom;
            view.requestLayout();
            return insets;
        });
    }
}
