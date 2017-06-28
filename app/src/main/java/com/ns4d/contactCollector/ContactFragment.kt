package com.ns4d.contactCollector

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v4.content.LocalBroadcastManager
import android.support.v7.widget.LinearLayoutManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.ns4d.contactCollector.db.ContactRepository
import kotlinx.android.synthetic.main.fragment_contacts.*

/**
 * A fragment representing a list of Items.
 */
class ContactFragment : Fragment() {

    val ACTION_REFRESH = "com.ns4d.contactCollector.REFRESH"
    val TAG = "ContactFragment"

    val refreshBroadcastReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {

            Toast.makeText(context, "Contacts (re)loaded", Toast.LENGTH_SHORT).show()

            // TODO: don't recreate the adapter unless anything changed
            contactsRecyclerView.adapter = ContactRecyclerViewAdapter(ContactRepository.getAll())
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        LocalBroadcastManager.getInstance(context)
                .registerReceiver(refreshBroadcastReceiver, IntentFilter(ACTION_REFRESH))
    }

    override fun onDestroy() {
        super.onDestroy()
        try {
            context.unregisterReceiver(refreshBroadcastReceiver)
        }
        catch (e: IllegalArgumentException) {
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
        contactsRecyclerView.adapter = ContactRecyclerViewAdapter(ContactRepository.getAll())
    }
}
