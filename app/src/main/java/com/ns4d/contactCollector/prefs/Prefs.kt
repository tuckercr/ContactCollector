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
    const val SCAN_COMPLETED = "scan.completed." + BuildConfig.VERSION_CODE

    fun getEditor(context: Context): SharedPreferences.Editor {
        return context.getSharedPreferences(Prefs.PREFS_FILENAME, 0)!!.edit()
    }
}