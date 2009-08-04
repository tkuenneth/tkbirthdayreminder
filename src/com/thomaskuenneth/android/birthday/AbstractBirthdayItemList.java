/**
 * AbstractBirthdayItemList.java
 * 
 * TKBirthdayReminder (c) Thomas Künneth 2009
 * 
 * Alle Rechte beim Autoren. All rights reserved.
 */
package com.thomaskuenneth.android.birthday;

import java.util.ArrayList;
import java.util.Comparator;

public abstract class AbstractBirthdayItemList extends ArrayList<BirthdayItem>
		implements Comparator<BirthdayItem> {

	private static final long serialVersionUID = 7406623114780527948L;

	/**
	 * Abgeleitete Klassen prüfen, ob das Element ihrer Liste hinzugefügt werden
	 * soll.
	 * 
	 * @param item
	 *            das hinzuzufügende Element
	 * @return liefert {@code true}, wenn das Element hinzugefügt werden soll,
	 *         sonst {@code false}
	 */
	protected abstract boolean addToList(BirthdayItem item);

}
