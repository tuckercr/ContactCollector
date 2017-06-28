package com.ns4d.contactCollector

import com.ns4d.contactCollector.db.AppDatabase
import com.raizlabs.android.dbflow.annotation.Column
import com.raizlabs.android.dbflow.annotation.PrimaryKey
import com.raizlabs.android.dbflow.annotation.Table
import com.raizlabs.android.dbflow.structure.BaseModel

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

    override fun toString(): String {

        return "id='$id'\ncompany='$company'\njobTitle='$jobTitle'\nemails=$emails\nphones=$phones"
    }
}