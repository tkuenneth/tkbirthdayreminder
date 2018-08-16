/*
 * Sign.java
 * 
 * TKBirthdayReminder (c) Thomas Künneth 2009 - 2017
 * Alle Rechte beim Autoren. All rights reserved.
 */
package com.thomaskuenneth.android.birthday;

import java.util.Hashtable;

/**
 * Diese Klasse wird verwendet, um ein Tierkreiszeichen zu speichern. In einer
 * {@link Hashtable} könnte ein Monat als Schlüssel auf eine Instanz verweisen;
 * jeder Monat gehört zu zwei Tierkreiszeichen; der erste Tag des zweiten
 * Zeichens wird hier gespeichert.
 *
 * @author Thomas Künneth
 */
class Sign {

    private final int firstDayOfSecondSign, firstSign, secondSign;

    Sign(int firstDayOfSecondSign, int firstSign, int secondSign) {
        super();
        this.firstDayOfSecondSign = firstDayOfSecondSign;
        this.firstSign = firstSign;
        this.secondSign = secondSign;
    }

    int getFirstDayOfSecondSign() {
        return firstDayOfSecondSign;
    }

    int getFirstSign() {
        return firstSign;
    }

    int getSecondSign() {
        return secondSign;
    }
}
