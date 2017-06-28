package com.ns4d.contactCollector

import android.app.IntentService
import android.content.Context
import android.content.Intent
import android.os.Environment.DIRECTORY_DOWNLOADS
import android.support.v4.content.LocalBroadcastManager
import android.text.TextUtils
import android.util.Log
import com.ns4d.contactCollector.db.ContactRepository
import java.io.*


/**
 * An [IntentService] subclass for the contact scan/collection
 */
class CollectorService : IntentService("CollectorService") {

    private val TAG = "CollectorService"

    override fun onHandleIntent(intent: Intent?) {
        if (intent != null) {
            val action = intent.action
            if (ACTION_SCAN == action) {
                val timestamp = intent.getLongExtra(EXTRA_TIMESTAMP, 0L)
                handleActionScan(timestamp)
            } else if (ACTION_SAVE == action) {
                handleActionSave()
            }
        }
    }

    private fun handleActionSave() {

        Log.d(TAG, "handleActionSave")
        ContactsUtils.retrieveContacts(this)

        val root = android.os.Environment.getExternalStoragePublicDirectory(DIRECTORY_DOWNLOADS)

        val file = File(root.absolutePath, "contactsCollector.txt")

        try {
            val f = FileOutputStream(file)
            val pw = PrintWriter(f)

            for (contact in ContactRepository.getAll()) {
                pw.println(contact.displayName)
                if (!TextUtils.isEmpty(contact.company)) {
                    pw.println("company: " + contact.company)
                }
                if (!TextUtils.isEmpty(contact.jobTitle)) {
                    pw.println("jobTitle: " + contact.jobTitle)
                }
                if (!contact.emails.isEmpty()) {
                    pw.println("emails: " + contact.emails)
                }
                if (!contact.phones.isEmpty()) {
                    pw.println("phones: " + contact.phones)
                }
                pw.println("")
            }

            pw.flush()
            pw.close()
            f.close()
        } catch (e: FileNotFoundException) {
            Log.e(TAG, "Caught: " + e.message, e)
        } catch (e: IOException) {
            Log.e(TAG, "Caught: " + e.message, e)
        }
    }

    /**
     * Handle action Foo in the provided background thread with the provided
     * parameters.
     */
    private fun handleActionScan(timestamp: Long) {

        Log.d(TAG, "handleActionScan: " + timestamp)
        ContactsUtils.retrieveContacts(this)

        LocalBroadcastManager.getInstance(this)
                .sendBroadcast(Intent(ContactFragment().ACTION_REFRESH))
    }

    companion object {

        private val ACTION_SCAN = "com.ns4d.contactCollector.action.SCAN"
        private val ACTION_SAVE = "com.ns4d.contactCollector.action.SAVE"
        private val EXTRA_TIMESTAMP = "com.ns4d.contactCollector.extra.TIMESTAMP"

        /**
         * Starts this service to scan the contacts.
         *
         * @param timestamp Optional timestamp.  Only contacts modified after this will be queried.
         */
        fun startActionScan(context: Context, timestamp: Long) {

            val intent = Intent(context, CollectorService::class.java)
            intent.action = ACTION_SCAN
            intent.putExtra(EXTRA_TIMESTAMP, timestamp)
            context.startService(intent)
        }

        fun startActionSave(context: Context) {
            val intent = Intent(context, CollectorService::class.java)
            intent.action = ACTION_SAVE
            context.startService(intent)
        }
    }
}
