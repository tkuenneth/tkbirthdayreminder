package com.thomaskuenneth.android.util;

import android.net.Uri;

public class ContactsContractWrapper {

	public static int i = 10;

	public static class CommonDataKinds {

		public static class Note {

			public static String NOTE = "data1";

			public static String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/note";
		}
	}

	public static class Data {

		public static Uri CONTENT_URI = Uri
				.parse("content://com.android.contacts/data");

		public static String CONTACT_ID = "contact_id";

		public static String MIMETYPE = "mimetype";

		public static String RAW_CONTACT_ID = "raw_contact_id";

	}
}
