/*
 * ContactsUtils.kt
 *
 * TKBirthdayReminder (c) Thomas Künneth 2025
 * All rights reserved.
 */
package com.thomaskuenneth.android.birthday

import android.content.ContentResolver
import android.provider.ContactsContract

fun getAccountTypeForContact(
    contentResolver: ContentResolver, contactId: String
): Pair<String, String>? {
    val projection = arrayOf(
        ContactsContract.RawContacts.ACCOUNT_NAME, ContactsContract.RawContacts.ACCOUNT_TYPE
    )
    val selection = "${ContactsContract.RawContacts.CONTACT_ID} = ?"
    val selectionArgs = arrayOf(contactId)
    contentResolver.query(
        ContactsContract.RawContacts.CONTENT_URI, projection, selection, selectionArgs, null
    )?.use {
        if (it.moveToFirst()) {
            val accountNameIndex = it.getColumnIndex(ContactsContract.RawContacts.ACCOUNT_NAME)
            val accountTypeIndex = it.getColumnIndex(ContactsContract.RawContacts.ACCOUNT_TYPE)
            val accountName = it.getString(accountNameIndex)
            val accountType = it.getString(accountTypeIndex)
            return Pair<String, String>(accountName, accountType)
        }
    }
    return null
}
