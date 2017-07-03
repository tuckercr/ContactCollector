package com.ns4d.contactCollector

import android.content.*
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.content.LocalBroadcastManager
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.Toast
import com.ns4d.contactCollector.db.ContactRepository
import com.ns4d.contactCollector.prefs.Prefs
import com.ns4d.contactCollector.prefs.Prefs.PREFS_FILENAME
import kotlinx.android.synthetic.main.fragment_contacts.*

/**
 * A fragment representing a list of Items.
 */
class ContactFragment : Fragment() {

    val ACTION_REFRESH = "com.ns4d.contactCollector.REFRESH"
    val TAG = "ContactFragment"
    var prefs: SharedPreferences? = null

    val refreshBroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {

            Toast.makeText(context, "Contacts (re)loaded", Toast.LENGTH_SHORT).show()

            // TODO: don't recreate the adapter unless anything changed
            initUi()
        }
    }

    /**
     * Initialise the UI, either setup the RecyclerView or an empty view
     */
    private fun initUi() {

        val contacts = ContactRepository.getAll()

        if (contacts.size == 0) {

            searchTextView.visibility = GONE
            searchImageView.visibility = GONE
            emptyTextView.visibility = VISIBLE

            if (prefs!!.getBoolean(Prefs.SCAN_COMPLETED, false)) {
                emptyTextView.setText(R.string.no_contacts_found)
            } else {
                emptyTextView.setText(R.string.please_wait)
            }

        } else {
            contactsRecyclerView.adapter = ContactRecyclerViewAdapter(contacts)
//            searchTextView.visibility = VISIBLE
//            searchImageView.visibility = VISIBLE
            emptyTextView.visibility = GONE
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        LocalBroadcastManager.getInstance(context)
                .registerReceiver(refreshBroadcastReceiver, IntentFilter(ACTION_REFRESH))
        prefs = activity.getSharedPreferences(PREFS_FILENAME, 0)
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            context.unregisterReceiver(refreshBroadcastReceiver)
        } catch (e: IllegalArgumentException) {
            Log.e(TAG, "Caught: " + e.message, e)

        }
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        return inflater!!.inflate(R.layout.fragment_contacts, container, false)
    }

    override fun onViewCreated(view: View?, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        contactsRecyclerView.layoutManager = LinearLayoutManager(context)
        initUi()
    }
}
