/**
 * BirthdayItem.java
 * 
 * TKBirthdayReminder (c) Thomas Künneth 2009
 * 
 * Alle Rechte beim Autoren. All rights reserved.
 */
package com.thomaskuenneth.android.birthday;

import java.util.Date;

public class BirthdayItem {

	private String name;
	private Date birthday;
	private long id;
	private String primaryPhoneNumber;

	public BirthdayItem(String name, Date birthday, long id, String primaryPhoneNumber) {
		this.name = name;
		this.birthday = birthday;
		this.id = id;
		this.primaryPhoneNumber = primaryPhoneNumber;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Date getBirthday() {
		return birthday;
	}

	public void setBirthday(Date birthday) {
		this.birthday = birthday;
	}

	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public String getPrimaryPhoneNumber() {
		return primaryPhoneNumber;
	}

	public void setPrimaryPhoneNumber(String primaryPhoneNumber) {
		this.primaryPhoneNumber = primaryPhoneNumber;
	}

	public String getNameNotNull() {
		String name = getName();
		if (name == null) {
			name = "";
		}
		return name;
	}
}
