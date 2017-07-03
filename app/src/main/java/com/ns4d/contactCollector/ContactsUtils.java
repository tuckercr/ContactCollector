package com.ns4d.contactCollector;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.util.Log;

import java.util.HashSet;
import java.util.Set;

/**
 * This class accesses the address book using ContactsContract
 * <p>
 * This is the only Java code in the project
 * <p>
 * Created by ctucker on 6/21/17.
 */
class ContactsUtils {

    private static final String TAG = "ContactsUtils";

    public static boolean retrieveContacts(Context context) {

        ContentResolver cr = context.getContentResolver();

        Cursor cur = null;

        try {
            cur = cr.query(ContactsContract.Contacts.CONTENT_URI,
                    null, null, null, null);
            if (cur != null && cur.getCount() > 0) {
                while (cur.moveToNext()) {
                    long id = cur.getLong(cur.getColumnIndex(ContactsContract.Contacts._ID));
                    String name = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));

                    String company = "";
                    String jobTitle = "";

                    long lastModified = cur.getLong(cur.getColumnIndex(ContactsContract.Contacts.CONTACT_LAST_UPDATED_TIMESTAMP));
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

                    // Organization
                    Cursor orgCur = cr.query(ContactsContract.Data.CONTENT_URI,
                            null,
                            ContactsContract.Data.CONTACT_ID + " = ? AND " + ContactsContract.Data.MIMETYPE + " = ?",
                            new String[]{String.valueOf(id), ContactsContract.CommonDataKinds.Organization.CONTENT_ITEM_TYPE}, null);
                    if (orgCur != null) {
                        if (orgCur.moveToFirst()) {
                            company = orgCur.getString(orgCur.getColumnIndex(ContactsContract.CommonDataKinds.Organization.DATA));
                            jobTitle = orgCur.getString(orgCur.getColumnIndex(ContactsContract.CommonDataKinds.Organization.TITLE));
                        }
                        orgCur.close();
                    }

                    Contact contact = new Contact();
                    contact.setId(id);
                    contact.setDisplayName(name);
                    contact.setLastModified(lastModified);
                    contact.setJobTitle(jobTitle == null ? "" : jobTitle);
                    contact.setCompany(company == null ? "" : company);
                    contact.setEmails(emails.toString());
                    contact.setPhones(phones.toString());
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
        }
    }
}
