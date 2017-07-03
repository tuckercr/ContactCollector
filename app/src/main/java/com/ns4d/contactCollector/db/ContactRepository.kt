package com.ns4d.contactCollector.db

/**
 * Accessor methods for the local contact db
 *
 * Created by ctucker on 6/22/17.
 */
import com.ns4d.contactCollector.Contact
import com.ns4d.contactCollector.Contact_Table
import com.raizlabs.android.dbflow.sql.language.Method
import com.raizlabs.android.dbflow.sql.language.Select

object ContactRepository {

    fun getAll(): MutableList<Contact> {
        return Select()
                .from(Contact::class.java)
                .where()
                .orderBy(Contact_Table.displayName, true)
                .queryList()
    }

    fun count(): Long {
        return Select(Method.count()).from(Contact::class.java).count()
    }
}