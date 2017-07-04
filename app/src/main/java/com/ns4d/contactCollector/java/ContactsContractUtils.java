package com.ns4d.contactCollector.java;

import android.content.ContentResolver;
import android.content.Context;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.util.Log;

import com.ns4d.contactCollector.model.Contact;
import com.ns4d.contactCollector.model.Email;
import com.ns4d.contactCollector.model.Phone;
import com.ns4d.contactCollector.prefs.Prefs;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * This class accesses the address book using ContactsContract
 * <p>
 * This is the only Java code in the project
 * <p>
 * Created by ctucker on 6/21/17.
 */
public class ContactsContractUtils {

    private static final String TAG = "ContactsContractUtils";

    public static boolean retrieveContacts(Context context) {

        ContentResolver cr = context.getContentResolver();

        // Map of Group ID -> Group Title
        Map<String, String> groupsMap = initGroupsMap(cr);

        Cursor cur = null;

        long mostRecentlyUpdatedContactTimestamp = 0;

        try {
            cur = cr.query(ContactsContract.Contacts.CONTENT_URI,
                    null, null, null, null);
            if (cur != null && cur.getCount() > 0) {
                while (cur.moveToNext()) {
                    long id = cur.getLong(cur.getColumnIndex(ContactsContract.Contacts._ID));
                    String name = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));

                    // These are for the QuickContactBadge
                    String lookupKey = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.LOOKUP_KEY));
                    String thumbnailUri = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.PHOTO_THUMBNAIL_URI));
                    long lastModified = cur.getLong(cur.getColumnIndex(ContactsContract.Contacts.CONTACT_LAST_UPDATED_TIMESTAMP));

                    if (lastModified > mostRecentlyUpdatedContactTimestamp) {
                        mostRecentlyUpdatedContactTimestamp = lastModified;
                    }
                    Set<Phone> phones = new HashSet<>();
                    Set<Email> emails = new HashSet<>();

                    // Phone Numbers
                    if (Integer.parseInt(cur.getString(
                            cur.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {

                        Cursor pCur = cr.query(
                                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                                null,
                                ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                                new String[]{String.valueOf(id)}, null);

                        if (pCur != null) {
                            while (pCur.moveToNext()) {
                                String phoneNo = pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                                int type = pCur.getInt(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.TYPE));
                                String label = ContactsContract.CommonDataKinds.Email.getTypeLabel(context.getResources(), type, "Custom").toString();
                                phones.add(new Phone(phoneNo, label, type));
                            }
                            pCur.close();
                        }
                    }

                    // Emails
                    Cursor emailCursor = cr.query(
                            ContactsContract.CommonDataKinds.Email.CONTENT_URI,
                            null,
                            ContactsContract.CommonDataKinds.Phone.CONTACT_ID + " = ?",
                            new String[]{String.valueOf(id)}, null);

                    if (emailCursor != null) {
                        while (emailCursor.moveToNext()) {
                            String email = emailCursor.getString(emailCursor.getColumnIndex(ContactsContract.CommonDataKinds.Email.ADDRESS));
                            int emailType = emailCursor.getInt(emailCursor.getColumnIndex(ContactsContract.CommonDataKinds.Email.TYPE));
                            String type = ContactsContract.CommonDataKinds.Email.getTypeLabel(context.getResources(), emailType, "Custom").toString();
                            emails.add(new Email(email, type));
                        }
                        emailCursor.close();
                    }

                    // Organization & Job Title
                    String company = "";
                    String jobTitle = "";
                    Cursor orgCur = cr.query(ContactsContract.Data.CONTENT_URI,
                            null,
                            ContactsContract.Data.CONTACT_ID + " = ? AND " + ContactsContract.Data.MIMETYPE + " = ?",
                            new String[]{
                                    String.valueOf(id), ContactsContract.CommonDataKinds.Organization.CONTENT_ITEM_TYPE}, null);
                    if (orgCur != null) {
                        if (orgCur.moveToFirst()) {
                            company = orgCur.getString(orgCur.getColumnIndex(ContactsContract.CommonDataKinds.Organization.DATA));
                            jobTitle = orgCur.getString(orgCur.getColumnIndex(ContactsContract.CommonDataKinds.Organization.TITLE));
                        }
                        orgCur.close();
                    }

                    // Get a person's group membership relationship to us
                    Cursor groupsCursor = cr.query(ContactsContract.Data.CONTENT_URI,
                            new String[]{
                                    ContactsContract.Data.CONTACT_ID,
                                    ContactsContract.Data.DATA1
                            },
                            ContactsContract.Data.CONTACT_ID + " = ? AND " + ContactsContract.Data.MIMETYPE + "=?",
                            new String[]{
                                    String.valueOf(id),
                                    ContactsContract.CommonDataKinds.GroupMembership.CONTENT_ITEM_TYPE},
                            null);

                    StringBuilder groups = new StringBuilder();
                    if (groupsCursor != null) {
                        while (groupsCursor.moveToNext()) {
                            String data1 = groupsCursor.getString(groupsCursor
                                    .getColumnIndex(ContactsContract.Data.DATA1));
                            groups.append(groupsMap.get(data1));

                            if (!groupsCursor.isLast()) {
                                groups.append(",");
                            }
                        }
                        groupsCursor.close();
                    }

                    // Get a person's websites
                    Cursor websiteCursor = cr.query(ContactsContract.Data.CONTENT_URI,
                            null,
                            ContactsContract.Data.CONTACT_ID + " = ? AND " + ContactsContract.Data.MIMETYPE + "=?",
                            new String[]{
                                    String.valueOf(id),
                                    ContactsContract.CommonDataKinds.Website.CONTENT_ITEM_TYPE}, null

                    );
                    StringBuilder websites = new StringBuilder("");
                    if (websiteCursor != null) {
                        while (websiteCursor.moveToNext()) {
                            String url = websiteCursor.getString(websiteCursor.getColumnIndex(ContactsContract.CommonDataKinds.Website.URL));
                            if (url != null) {
                                websites.append(url);

                                if (!websiteCursor.isLast()) {
                                    websites.append(", ");
                                }
                            }
                        }
                        websiteCursor.close();
                    }

                    // Get all the rest
                    Cursor dataCursor = cr.query(ContactsContract.Data.CONTENT_URI,
                            null,
                            ContactsContract.Data.CONTACT_ID + " = ?",
                            new String[]{
                                    String.valueOf(id)}, null

                    );
                    StringBuilder misc = new StringBuilder("");
                    if (dataCursor != null) {
                        while (dataCursor.moveToNext()) {
                            String url = dataCursor.getString(dataCursor.getColumnIndex(ContactsContract.CommonDataKinds.Website.URL));
                            if (url != null) {
                                misc.append(url);
                                if (!dataCursor.isLast()) {
                                    misc.append(", ");
                                }
                            }
                        }
                        dataCursor.close();
                    }

                    // Account Type and Name
                    Cursor accountCursor = cr.query(ContactsContract.RawContacts.CONTENT_URI,
                            new String[]{ContactsContract.RawContacts.DELETED, ContactsContract.RawContacts.ACCOUNT_NAME, ContactsContract.RawContacts.ACCOUNT_TYPE},
                            ContactsContract.RawContacts.CONTACT_ID + "=?",
                            new String[]{String.valueOf(id)},
                            null);

                    StringBuilder accountDetails = new StringBuilder();
                    if (accountCursor != null && accountCursor.getCount() > 0) {
                        accountCursor.moveToFirst();
                        String accountName = accountCursor.getString(accountCursor.getColumnIndex(ContactsContract.RawContacts.ACCOUNT_NAME));
                        String accountType = accountCursor.getString(accountCursor.getColumnIndex(ContactsContract.RawContacts.ACCOUNT_TYPE));
                        accountDetails.append(accountName);
                        accountDetails.append(" (");
                        accountDetails.append(accountType);
                        accountDetails.append(")");
                        accountCursor.close();
                    }


                    // FIXME get LinkedIn
                    Contact contact = new Contact();
                    contact.setId(id);
                    contact.setAccountDetails(accountDetails.toString());
                    contact.setLookupKey(lookupKey);
                    contact.setThumbnailUri(thumbnailUri);
                    contact.setDisplayName(name);
                    contact.setLastModified(lastModified);
                    contact.setJobTitle(jobTitle == null ? "" : jobTitle);
                    contact.setCompany(company == null ? "" : company);
                    contact.setEmails(emails.toString());
                    contact.setPhones(phones.toString());
                    contact.setGroups(groups.toString());
                    contact.setMisc(misc.toString());
                    contact.setWebsites(websites.toString());
                    contact.save();

                    // Uncomment for debugging
                    // Log.v(TAG, contact.toString());
                }
            }
            return true;

        } catch (Exception e) {
            Log.e(TAG, "Caught: " + e.getMessage(), e);
            return false;

        } finally {
            if (cur != null) {
                cur.close();
            }

            SharedPreferences.Editor editor = context.getSharedPreferences(Prefs.PREFS_FILENAME, 0).edit();
            editor.putLong(Prefs.MOST_RECENT, mostRecentlyUpdatedContactTimestamp);
            editor.apply();
        }
    }

    private static Map<String, String> initGroupsMap(ContentResolver cr) {

        Map<String, String> groupsMap = new HashMap<>();
        Cursor groupsCursor = null;
        try {
            // Get all the groups
            groupsCursor = cr.query(
                    ContactsContract.Groups.CONTENT_URI,
                    new String[]{
                            ContactsContract.Groups._ID,
                            ContactsContract.Groups.TITLE
                    }, null, null, null
            );

            if (groupsCursor != null) {
                while (groupsCursor.moveToNext()) {
                    String group_title = groupsCursor.getString(1);
                    String id = groupsCursor.getString(0);
                    groupsMap.put(id, group_title);
                }
            }

        } catch (Exception e) {
            Log.e(TAG, "Caught: " + e.getMessage(), e);

        } finally {
            if (groupsCursor != null) {
                groupsCursor.close();
            }
        }

        return groupsMap;

    }

    /**
     * Gets a list of contacts for a given group.
     *
     * @param resolver Contact Resolver
     * @param groupID  The group ID
     * @return A HashMap
     */
    @NonNull
    public static Map<String, String> getContactsForGroup(@NonNull ContentResolver resolver, @NonNull String groupID) {

        HashMap<String, String> map = new HashMap<>();
        Cursor dataCursor = null;
        try {
            dataCursor = resolver.query(
                    ContactsContract.Data.CONTENT_URI,
                    null,
                    ContactsContract.Data.MIMETYPE + " = ? AND " + ContactsContract.Data.DATA1 + " = ? ",
                    new String[]{
                            ContactsContract.CommonDataKinds.GroupMembership.CONTENT_ITEM_TYPE,
                            groupID
                    },
                    null
            );

            if (dataCursor != null) {
                dataCursor.moveToFirst();

                do {
                    String contactId = dataCursor.getString(dataCursor.getColumnIndex(ContactsContract.Data.CONTACT_ID));
                    String displayName = dataCursor.getString(dataCursor.getColumnIndex(ContactsContract.Data.DISPLAY_NAME));
                    String groupId = dataCursor.getString(dataCursor.getColumnIndex(ContactsContract.Data.DATA1));
                    map.put(contactId, displayName);

                    Log.d(TAG, "contact_id: " + contactId + "  contact: " + displayName + "   groupID: " + groupId);
                } while (dataCursor.moveToNext());
            }

            return map;
        } finally {
            if (dataCursor != null) {
                dataCursor.close();
            }
        }
    }
}
