package com.ns4d.contactCollector.db

import com.raizlabs.android.dbflow.annotation.Database

/**
 * The DBFlow database definition
 *
 * Created by ctucker on 6/22/17.
 */
@Database(name = AppDatabase.NAME, version = AppDatabase.VERSION)
object AppDatabase {

    const val NAME: String = "contactDb"
    const val VERSION: Int = 1
}
