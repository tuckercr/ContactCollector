package com.ns4d.contactCollector

import android.content.Context
import android.content.res.AssetFileDescriptor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.provider.ContactsContract
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.QuickContactBadge
import android.widget.TextView
import com.jaredrummler.fastscrollrecyclerview.FastScrollRecyclerView
import com.ns4d.contactCollector.model.Contact
import java.io.IOException
import java.util.*

/**
 * [RecyclerView.Adapter] that can display [Contact]s
 */
class ContactRecyclerViewAdapter(private val mValues: List<Contact>) : RecyclerView.Adapter<ContactRecyclerViewAdapter.ViewHolder>()
        , FastScrollRecyclerView.SectionedAdapter {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_contact, parent, false)
        return ViewHolder(view)
    }

    /**
     * Load a contact photo thumbnail and return it as a Bitmap,
     * resizing the image to the provided image dimensions as needed.
     * @param photoData photo ID Prior to Honeycomb, the contact's _ID value.
     * * For Honeycomb and later, the value of PHOTO_THUMBNAIL_URI.
     * *
     * @return A thumbnail Bitmap, sized to the provided width and height.
     * * Returns null if the thumbnail is not found.
     */
    private fun loadContactPhotoThumbnail(context: Context, photoData: String): Bitmap? {

        var assetFileDescriptor: AssetFileDescriptor? = null
        try {
            val thumbUri = Uri.parse(photoData)

            assetFileDescriptor = context.contentResolver.openAssetFileDescriptor(thumbUri, "r")

            val fileDescriptor = assetFileDescriptor!!.fileDescriptor

            if (fileDescriptor != null) {
                // Decode the bitmap
                return BitmapFactory.decodeFileDescriptor(
                        fileDescriptor, null, null)
            }

        } catch (e: Throwable) {
            // Ignore - sometimes there is no photo
        } finally {
            if (assetFileDescriptor != null) {
                try {
                    assetFileDescriptor.close()
                } catch (e: IOException) {
                    // Ignore
                }
            }
        }
        return null
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {

        val contactUri = ContactsContract.Contacts.getLookupUri(
                mValues[position].id,
                mValues[position].lookupKey)

        holder.mQuickContactBadge.assignContactUri(contactUri)

        if (mValues[position].thumbnailUri != null) {
            val thumbnailBitmap = loadContactPhotoThumbnail(holder.mQuickContactBadge.context, mValues[position].thumbnailUri!!)
            holder.mQuickContactBadge.setImageBitmap(thumbnailBitmap)
        } else {
            holder.mQuickContactBadge.setImageToDefault()
        }

        holder.mDisplayNameTextView.text = mValues[position].displayName
        holder.mJobTitleTextView.text = mValues[position].jobTitle
        holder.mCompanyNameTextView.text = mValues[position].company
        holder.mDetailsTextView.text = mValues[position].toString()
    }

    override fun getItemCount(): Int {
        return mValues.size
    }

    inner class ViewHolder(mView: View) : RecyclerView.ViewHolder(mView) {
        val mDisplayNameTextView: TextView = mView.findViewById(R.id.displayNameTextView)
        val mJobTitleTextView: TextView = mView.findViewById(R.id.jobTitleTextView)
        val mCompanyNameTextView: TextView = mView.findViewById(R.id.companyTextView)
        val mDetailsTextView: TextView = mView.findViewById(R.id.detailsTextView)
        val mQuickContactBadge: QuickContactBadge = mView.findViewById(R.id.quickContactBadge)

        override fun toString(): String {
            return super.toString() + " '" + mDetailsTextView.text + "'"
        }
    }

    override fun getSectionName(position: Int): String {
        return mValues[position].displayName.substring(0, 1).toUpperCase(Locale.ENGLISH)
    }

}
