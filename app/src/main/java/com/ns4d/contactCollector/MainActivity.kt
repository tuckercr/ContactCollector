package com.ns4d.contactCollector

import android.Manifest
import android.Manifest.permission.READ_CONTACTS
import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Environment
import android.support.annotation.UiThread
import android.support.design.widget.FloatingActionButton
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v4.content.PermissionChecker.PERMISSION_GRANTED
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import com.ns4d.contactCollector.db.ContactRepository
import kotlinx.android.synthetic.main.fragment_contacts.*

class MainActivity : AppCompatActivity() {

    var PERMISSION_CONTACTS = 1000
    var PERMISSION_EXTERNAL_STORAGE = 1001

    var TAG = "MainActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val toolbar = findViewById(R.id.toolbar) as Toolbar
        setSupportActionBar(toolbar)

        val fab = findViewById(R.id.fab) as FloatingActionButton
        fab.setOnClickListener {
            if (requestContactsPermission()) {
                CollectorService.startActionScan(this, 0)
            }
        }

        if (requestContactsPermission()) {
            CollectorService.startActionScan(this, 0)
        }
    }

    @UiThread
    fun showRationaleForContacts() {

        AlertDialog.Builder(this)
                .setTitle("Permission Required")
                .setMessage("Please grant permission to read your contacts.")
                .setPositiveButton(android.R.string.ok, { _: DialogInterface, _: Int ->
                    ActivityCompat.requestPermissions(this, arrayOf(READ_CONTACTS),
                            PERMISSION_CONTACTS)
                })
                .show()
    }

    @UiThread
    fun showRationaleForStorage() {

        AlertDialog.Builder(this)
                .setTitle("Permission Required")
                .setMessage("To save, please grant permission to write to external storage.")
                .setPositiveButton(android.R.string.ok, { _: DialogInterface, _: Int ->
                    ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                            PERMISSION_EXTERNAL_STORAGE)
                })
                .show()
    }

    /**
     * Check and request READ_CONTACTS permission
     */
    fun requestContactsPermission(): Boolean {

        if (ContextCompat.checkSelfPermission(this, READ_CONTACTS) == PERMISSION_GRANTED) {
            return true
        }

        if (ActivityCompat.shouldShowRequestPermissionRationale(this, READ_CONTACTS)) {
            showRationaleForContacts()
            return false
        }

        ActivityCompat.requestPermissions(this, arrayOf(READ_CONTACTS), PERMISSION_CONTACTS)
        return false
    }

    /**
     * Check and request WRITE_EXTERNAL_STORAGE permission
     */
    fun requestExternalStoragePermission(): Boolean {

        if (ContextCompat.checkSelfPermission(this, WRITE_EXTERNAL_STORAGE) == PERMISSION_GRANTED) {
            return true
        }

        if (ActivityCompat.shouldShowRequestPermissionRationale(this, WRITE_EXTERNAL_STORAGE)) {
            showRationaleForStorage()
            return false
        }

        ActivityCompat.requestPermissions(this, arrayOf(WRITE_EXTERNAL_STORAGE), PERMISSION_EXTERNAL_STORAGE)
        return false
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when {
            item.itemId == R.id.action_save ->
                if (requestExternalStoragePermission()) {
                saveContacts()
                return true
            } else {
                Log.e(TAG, "Insufficient permission to save!")
            }

            item.itemId == R.id.action_about -> {

                val size = ContactRepository.count()
                val msg = getString(R.string.app_name) + "\nVersion: " + BuildConfig.VERSION_NAME + "." + BuildConfig.VERSION_CODE + "\nFound " + size + " contacts"
                AlertDialog.Builder(this)
                        .setTitle(R.string.action_about)
                        .setMessage(msg)
                        .show()
            }
        }

        return super.onOptionsItemSelected(item)
    }

    /**
     * This method assumes that permission has been granted
     */
    private fun saveContacts() {

        AlertDialog.Builder(this).setTitle("Save")
                .setMessage("Your contacts will be saved to a file named contactsCollector.txt in your Downloads folder.")
                .setPositiveButton(android.R.string.yes, { _: DialogInterface, _: Int ->

                    if (isExternalStorageWritable()) {
                        CollectorService.startActionSave(this)
                    } else {
                        Toast.makeText(MainActivity@ this, "External storage is locked", Toast.LENGTH_SHORT).show()
                    }

                }).show()
    }

    /**
     * Checks the status of external storage
     */
    private fun isExternalStorageWritable(): Boolean {
        return Environment.MEDIA_MOUNTED == Environment.getExternalStorageState()
    }

    /**
     * When permission has been granted, keep doing whatever it was we were doing
     */
    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {

        if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            when (requestCode) {
                PERMISSION_CONTACTS -> {
                    Toast.makeText(this, "Thanks.  Please wait a moment while we scan your address book.", Toast.LENGTH_LONG).show()
                    CollectorService.startActionScan(this, 0)
                }
                PERMISSION_EXTERNAL_STORAGE -> {
                    saveContacts()
                }
                else -> Log.e(TAG, "Unexpected permission code")
            }
        } else {
            Log.e(TAG, "Permission Denied")
            emptyTextView.setText(R.string.please_grant_permission)
        }
    }
}
