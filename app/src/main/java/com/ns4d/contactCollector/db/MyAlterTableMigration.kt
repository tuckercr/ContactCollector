//package com.ns4d.contactCollector.db
//
//import android.util.Log
//import com.ns4d.contactCollector.model.Contact
//import com.raizlabs.android.dbflow.annotation.Migration
//import com.raizlabs.android.dbflow.sql.SQLiteType
//import com.raizlabs.android.dbflow.sql.migration.AlterTableMigration
//
//
///**
// * TODO This didn't work so I just renamed the cache, but it would be good to revist this and figure out why
// *
// * Created by ctucker on 7/3/17.
// */
//@Migration(version = AppDatabase.VERSION, database = AppDatabase::class)
//class MyAlterTableMigration : AlterTableMigration<Contact>(Contact::class.java) {
//
//    val TAG = "MyAlterTableMigration"
//    override fun onPreMigrate() {
//        super.onPreMigrate()
//        Log.e(TAG, "onPreMigrate");
//        if (AppDatabase.VERSION == 2) {
//            addColumn(SQLiteType.TEXT, "groups")
//        }
//
//        if (AppDatabase.VERSION == 3) {
//            addColumn(SQLiteType.TEXT, "misc")
//        }
//    }
//}