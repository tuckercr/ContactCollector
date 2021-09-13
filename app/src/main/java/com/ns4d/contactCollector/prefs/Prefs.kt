package com.ns4d.contactCollector.prefs

import android.content.Context
import android.content.SharedPreferences
import com.ns4d.contactCollector.BuildConfig

/**
 * Shared Preferences Constants and Helper Methods
 *
 * Created by ctucker on 6/28/17.
 */
object Prefs {
    const val PREFS_FILENAME = "contact.prefs"

    // This assumes that the version is incremented with each DB schema change
    const val SCAN_COMPLETED = "scan.completed." + BuildConfig.VERSION_CODE

    // The lastModified date of the most recently updated contact
    const val MOST_RECENT = "most.recent.contact"

    fun getEditor(context: Context): SharedPreferences.Editor {
        return context.getSharedPreferences(PREFS_FILENAME, 0)!!.edit()
    }
}