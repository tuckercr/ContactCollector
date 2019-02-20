package com.ns4d.contactCollector

import android.content.*
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.Toast
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.ns4d.contactCollector.db.ContactRepository
import com.ns4d.contactCollector.model.Contact
import com.ns4d.contactCollector.prefs.Prefs
import com.ns4d.contactCollector.prefs.Prefs.PREFS_FILENAME
import kotlinx.android.synthetic.main.fragment_contacts.*

/**
 * A fragment containing a search field and a RecyclerView list of contacts.
 */
class ContactFragment : androidx.fragment.app.Fragment() {

    var prefs: SharedPreferences? = null
    var contacts: List<Contact> = ArrayList()
    var mostRecentlyUpdatedContact: Long = 0

    private val refreshBroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {

            if (mostRecentlyUpdatedContact == prefs!!.getLong(Prefs.MOST_RECENT, -1)) {
                Log.d(TAG, "Ignoring broadcast - contacts haven't changed")
                return
            }

            if (contacts.isEmpty()) {
                Toast.makeText(context, "Contacts loaded", Toast.LENGTH_SHORT).show()
                initUi()
                return
            }

            Toast.makeText(context, "Contacts reloaded", Toast.LENGTH_SHORT).show()
            initUi()
            if (!searchTextView.text.isEmpty()) {
                filterContacts(searchTextView.text)
                return
            }
        }
    }

    /**
     * Initialise the UI, either setup the RecyclerView or an empty view
     */
    private fun initUi() {

        contacts = ContactRepository.getAll()
        contactsRecyclerView.adapter = ContactRecyclerViewAdapter(contacts)
        contactsRecyclerView.layoutManager = androidx.recyclerview.widget.LinearLayoutManager(context)

        if (contacts.isEmpty()) {
            searchTextView.visibility = GONE
            searchImageView.visibility = GONE
            emptyTextView.visibility = VISIBLE

            if (prefs!!.getBoolean(Prefs.SCAN_COMPLETED, false)) {
                emptyTextView.setText(R.string.no_contacts_found)
            } else {
                emptyTextView.setText(R.string.please_wait)
            }

        } else {
            searchTextView.visibility = VISIBLE
            searchImageView.visibility = VISIBLE
            emptyTextView.visibility = GONE
            mostRecentlyUpdatedContact = prefs!!.getLong(Prefs.MOST_RECENT, 0)
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        context?.let {
            LocalBroadcastManager.getInstance(it)
                    .registerReceiver(refreshBroadcastReceiver, IntentFilter(ACTION_REFRESH))
            prefs = it.getSharedPreferences(PREFS_FILENAME, 0)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            context?.let {
                LocalBroadcastManager.getInstance(it).unregisterReceiver(refreshBroadcastReceiver)
            }
        } catch (e: IllegalArgumentException) {
            Log.e(TAG, "Caught: " + e.message, e)

        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        return inflater.inflate(R.layout.fragment_contacts, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initUi()

        searchTextView.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(charSequence: CharSequence, start: Int, count: Int,
                                           after: Int) {
                // Ignore
            }

            override fun onTextChanged(charSequence: CharSequence, start: Int, count: Int, after: Int) {
                filterContacts(charSequence)
            }

            override fun afterTextChanged(editable: Editable) {
                // Ignore
            }
        })

    }

    private fun filterContacts(charSequence: CharSequence) {
        if (charSequence.isEmpty()) {
            contactsRecyclerView.swapAdapter(ContactRecyclerViewAdapter(contacts), true)
            return
        }

        val filteredContacts = ArrayList<Contact>()
        contacts.forEach {

            if (it.displayName.contains(charSequence, true)) {
                filteredContacts.add(it)
            }
        }
        contactsRecyclerView.swapAdapter(ContactRecyclerViewAdapter(filteredContacts), true)
    }

    companion object {
        const val ACTION_REFRESH = "com.ns4d.contactCollector.REFRESH"
        const val TAG = "ContactFragment"
    }
}
