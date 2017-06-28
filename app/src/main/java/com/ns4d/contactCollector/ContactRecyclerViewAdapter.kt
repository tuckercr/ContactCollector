package com.ns4d.contactCollector

import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import java.text.DateFormat

/**
 * [RecyclerView.Adapter] that can display [Contact]s
 *
 * TODO Databinding
 */
class ContactRecyclerViewAdapter(private val mValues: List<Contact>) : RecyclerView.Adapter<ContactRecyclerViewAdapter.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_contact, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.mDisplayNameTextView.text = mValues[position].displayName
        holder.mModifiedDate.text = DateFormat.getDateTimeInstance().format((mValues[position].lastModified))
        holder.mDetailsTextView.text = mValues[position].toString()
    }

    override fun getItemCount(): Int {
        return mValues.size
    }

    inner class ViewHolder(mView: View) : RecyclerView.ViewHolder(mView) {
        val mDisplayNameTextView: TextView = mView.findViewById(R.id.displayNameTextView) as TextView
        val mModifiedDate: TextView = mView.findViewById(R.id.lastModifiedTextView) as TextView
        val mDetailsTextView: TextView = mView.findViewById(R.id.detailsTextView) as TextView

        override fun toString(): String {
            return super.toString() + " '" + mDetailsTextView.text + "'"
        }
    }
}
