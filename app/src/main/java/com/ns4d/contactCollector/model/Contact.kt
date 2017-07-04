package com.ns4d.contactCollector.model

import com.ns4d.contactCollector.db.AppDatabase
import com.raizlabs.android.dbflow.annotation.Column
import com.raizlabs.android.dbflow.annotation.PrimaryKey
import com.raizlabs.android.dbflow.annotation.Table
import com.raizlabs.android.dbflow.structure.BaseModel
import java.text.DateFormat

/**
 * Simple contact class
 *
 * Created by ctucker on 6/21/17.
 */
@Table(name = "items", database = AppDatabase::class)
class Contact : BaseModel() {

    @PrimaryKey
    @Column(name = "id")
    var id: Long = 0

    @Column(name = "displayName")
    var displayName: String = ""

    @Column(name = "company")
    var company: String = ""

    @Column(name = "jobTitle")
    var jobTitle: String = ""

    @Column(name = "lastModified")
    var lastModified: Long = 0

    @Column(name = "emails")
    var emails : String = ""

    @Column(name = "phones")
    var phones: String = ""

    @Column(name = "groups")
    var groups: String = ""

    @Column(name = "websites")
    var websites: String = ""

    @Column(name = "lookupKey")
    var lookupKey: String = ""

    @Column(name = "thumbnailUri")
    var thumbnailUri: String? = ""

    @Column(name = "accountDetails")
    var accountDetails: String = ""

    @Column(name = "misc")
    var misc: String = ""

    override fun toString(): String {
        val lastModStr = DateFormat.getDateTimeInstance().format(lastModified)
        return "id=$id\nlastModified=$lastModStr\naccountDetails=$accountDetails\nemails=$emails\nphones=$phones\n" +
                "groups=[$groups]\nwebsites=[$websites]\n-----\neverything=[$misc]"
    }
}