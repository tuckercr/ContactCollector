package com.ns4d.contactCollector.java

import android.content.ContentResolver
import android.content.Context
import android.database.Cursor
import android.provider.ContactsContract
import android.util.Log
import com.ns4d.contactCollector.model.Contact
import com.ns4d.contactCollector.model.Email
import com.ns4d.contactCollector.model.Phone
import com.ns4d.contactCollector.prefs.Prefs
import java.util.*

/**
 * This class accesses the address book using ContactsContract
 *
 *
 * This is the only Java code in the project
 *
 *
 * Created by ctucker on 6/21/17.
 */
object ContactsContractUtils {

    private val TAG = "ContactsContractUtils"

    fun retrieveContacts(context: Context): Boolean {

        val cr = context.contentResolver

        // Map of Group ID -> Group Title
        val groupsMap = initGroupsMap(cr)

        var cur: Cursor? = null

        var mostRecentlyUpdatedContactTimestamp: Long = 0

        try {
            cur = cr.query(ContactsContract.Contacts.CONTENT_URI, null, null, null, null)
            if (cur != null && cur.count > 0) {
                while (cur.moveToNext()) {
                    val id = cur.getLong(cur.getColumnIndex(ContactsContract.Contacts._ID))
                    val name = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME))

                    // These are for the QuickContactBadge
                    val lookupKey = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.LOOKUP_KEY))
                    val thumbnailUri = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.PHOTO_THUMBNAIL_URI))
                    val lastModified = cur.getLong(cur.getColumnIndex(ContactsContract.Contacts.CONTACT_LAST_UPDATED_TIMESTAMP))

                    if (lastModified > mostRecentlyUpdatedContactTimestamp) {
                        mostRecentlyUpdatedContactTimestamp = lastModified
                    }
                    val phones = HashSet<Phone>()
                    val emails = HashSet<Email>()

                    // Phone Numbers
                    if (Integer.parseInt(cur.getString(
                            cur.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {

                        val pCur = cr.query(
                                ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null,
                                ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                                arrayOf(id.toString()), null)

                        if (pCur != null) {
                            while (pCur.moveToNext()) {
                                val phoneNo = pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER))
                                val type = pCur.getInt(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE))
                                val label = ContactsContract.CommonDataKinds.Email.getTypeLabel(context.resources, type, "Custom").toString()
                                phones.add(Phone(phoneNo, label, type))
                            }
                            pCur.close()
                        }
                    }

                    // Emails
                    val emailCursor = cr.query(
                            ContactsContract.CommonDataKinds.Email.CONTENT_URI, null,
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                            arrayOf(id.toString()), null)

                    if (emailCursor != null) {
                        while (emailCursor.moveToNext()) {
                            val email = emailCursor.getString(emailCursor.getColumnIndex(ContactsContract.CommonDataKinds.Email.ADDRESS))
                            val emailType = emailCursor.getInt(emailCursor.getColumnIndex(ContactsContract.CommonDataKinds.Email.TYPE))
                            val type = ContactsContract.CommonDataKinds.Email.getTypeLabel(context.resources, emailType, "Custom").toString()
                            emails.add(Email(email, type))
                        }
                        emailCursor.close()
                    }

                    // Organization & Job Title
                    var company: String? = ""
                    var jobTitle: String? = ""
                    val orgCur = cr.query(ContactsContract.Data.CONTENT_URI, null,
                            ContactsContract.Data.CONTACT_ID + " = ? AND " + ContactsContract.Data.MIMETYPE + " = ?",
                            arrayOf(id.toString(), ContactsContract.CommonDataKinds.Organization.CONTENT_ITEM_TYPE), null)
                    if (orgCur != null) {
                        if (orgCur.moveToFirst()) {
                            company = orgCur.getString(orgCur.getColumnIndex(ContactsContract.CommonDataKinds.Organization.DATA))
                            jobTitle = orgCur.getString(orgCur.getColumnIndex(ContactsContract.CommonDataKinds.Organization.TITLE))
                        }
                        orgCur.close()
                    }

                    // Get a person's group membership relationship to us
                    val groupsCursor = cr.query(ContactsContract.Data.CONTENT_URI,
                            arrayOf(ContactsContract.Data.CONTACT_ID, ContactsContract.Data.DATA1),
                            ContactsContract.Data.CONTACT_ID + " = ? AND " + ContactsContract.Data.MIMETYPE + "=?",
                            arrayOf(id.toString(), ContactsContract.CommonDataKinds.GroupMembership.CONTENT_ITEM_TYPE), null)

                    val groups = StringBuilder()
                    if (groupsCursor != null) {
                        while (groupsCursor.moveToNext()) {
                            val data1 = groupsCursor.getString(groupsCursor
                                    .getColumnIndex(ContactsContract.Data.DATA1))
                            groups.append(groupsMap[data1])

                            if (!groupsCursor.isLast) {
                                groups.append(",")
                            }
                        }
                        groupsCursor.close()
                    }

                    // Get a person's websites
                    val websiteCursor = cr.query(ContactsContract.Data.CONTENT_URI, null,
                            ContactsContract.Data.CONTACT_ID + " = ? AND " + ContactsContract.Data.MIMETYPE + "=?",
                            arrayOf(id.toString(), ContactsContract.CommonDataKinds.Website.CONTENT_ITEM_TYPE), null

                    )
                    val websites = StringBuilder("")
                    if (websiteCursor != null) {
                        while (websiteCursor.moveToNext()) {
                            val url = websiteCursor.getString(websiteCursor.getColumnIndex(ContactsContract.CommonDataKinds.Website.URL))
                            if (url != null) {
                                websites.append(url)

                                if (!websiteCursor.isLast) {
                                    websites.append(", ")
                                }
                            }
                        }
                        websiteCursor.close()
                    }

                    // Get all the rest
                    val dataCursor = cr.query(ContactsContract.Data.CONTENT_URI, null,
                            ContactsContract.Data.CONTACT_ID + " = ?",
                            arrayOf(id.toString()), null

                    )
                    val misc = StringBuilder("")
                    if (dataCursor != null) {
                        while (dataCursor.moveToNext()) {
                            val url = dataCursor.getString(dataCursor.getColumnIndex(ContactsContract.CommonDataKinds.Website.URL))
                            if (url != null) {
                                misc.append(url)
                                if (!dataCursor.isLast) {
                                    misc.append(", ")
                                }
                            }
                        }
                        dataCursor.close()
                    }

                    // Account Type and Name
                    val accountCursor = cr.query(ContactsContract.RawContacts.CONTENT_URI,
                            arrayOf(ContactsContract.RawContacts.DELETED, ContactsContract.RawContacts.ACCOUNT_NAME, ContactsContract.RawContacts.ACCOUNT_TYPE),
                            ContactsContract.RawContacts.CONTACT_ID + "=?",
                            arrayOf(id.toString()), null)

                    val accountDetails = StringBuilder()
                    if (accountCursor != null && accountCursor.count > 0) {
                        accountCursor.moveToFirst()
                        val accountName = accountCursor.getString(accountCursor.getColumnIndex(ContactsContract.RawContacts.ACCOUNT_NAME))
                        val accountType = accountCursor.getString(accountCursor.getColumnIndex(ContactsContract.RawContacts.ACCOUNT_TYPE))
                        accountDetails.append(accountName)
                        accountDetails.append(" (")
                        accountDetails.append(accountType)
                        accountDetails.append(")")
                        accountCursor.close()
                    }


                    // FIXME get LinkedIn
                    val contact = Contact()
                    contact.id = id
                    contact.accountDetails = accountDetails.toString()
                    contact.lookupKey = lookupKey
                    contact.thumbnailUri = thumbnailUri
                    contact.displayName = name
                    contact.lastModified = lastModified
                    contact.jobTitle = if (jobTitle == null) "" else jobTitle
                    contact.company = if (company == null) "" else company
                    contact.emails = emails.toString()
                    contact.phones = phones.toString()
                    contact.groups = groups.toString()
                    contact.misc = misc.toString()
                    contact.websites = websites.toString()
                    contact.save()

                    // Uncomment for debugging
                    // Log.v(TAG, contact.toString());
                }
            }
            return true

        } catch (e: Exception) {
            Log.e(TAG, "Caught: " + e.message, e)
            return false

        } finally {
            if (cur != null) {
                cur.close()
            }

            context.getSharedPreferences(Prefs.PREFS_FILENAME, 0)
                    .edit()
                    .putLong(Prefs.MOST_RECENT, mostRecentlyUpdatedContactTimestamp)
                    .apply()
        }
    }

    private fun initGroupsMap(cr: ContentResolver): Map<String, String> {

        val groupsMap = HashMap<String, String>()
        var groupsCursor: Cursor? = null
        try {
            // Get all the groups
            groupsCursor = cr.query(
                    ContactsContract.Groups.CONTENT_URI,
                    arrayOf(ContactsContract.Groups._ID, ContactsContract.Groups.TITLE), null, null, null
            )

            if (groupsCursor != null) {
                while (groupsCursor.moveToNext()) {
                    val groupTitle = groupsCursor.getString(1)
                    val id = groupsCursor.getString(0)
                    groupsMap.put(id, groupTitle)
                }
            }

        } catch (e: Exception) {
            Log.e(TAG, "Caught: " + e.message, e)

        } finally {
            if (groupsCursor != null) {
                groupsCursor.close()
            }
        }

        return groupsMap

    }

    /**
     * Gets a list of contacts for a given group.

     * @param resolver Contact Resolver
     * *
     * @param groupID  The group ID
     * *
     * @return A HashMap
     */
    @Suppress("unused")
    fun getContactsForGroup(resolver: ContentResolver, groupID: String): Map<String, String> {

        val map = HashMap<String, String>()
        var dataCursor: Cursor? = null
        try {
            dataCursor = resolver.query(
                    ContactsContract.Data.CONTENT_URI, null,
                    ContactsContract.Data.MIMETYPE + " = ? AND " + ContactsContract.Data.DATA1 + " = ? ",
                    arrayOf(ContactsContract.CommonDataKinds.GroupMembership.CONTENT_ITEM_TYPE, groupID), null
            )

            if (dataCursor != null) {
                dataCursor.moveToFirst()

                do {
                    val contactId = dataCursor.getString(dataCursor.getColumnIndex(ContactsContract.Data.CONTACT_ID))
                    val displayName = dataCursor.getString(dataCursor.getColumnIndex(ContactsContract.Data.DISPLAY_NAME))
                    val groupId = dataCursor.getString(dataCursor.getColumnIndex(ContactsContract.Data.DATA1))
                    map.put(contactId, displayName)

                    Log.d(TAG, "contact_id: $contactId  contact: $displayName   groupID: $groupId")
                } while (dataCursor.moveToNext())
            }

            return map
        } finally {
            if (dataCursor != null) {
                dataCursor.close()
            }
        }
    }
}
