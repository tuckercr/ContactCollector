package com.ns4d.contactCollector

import android.Manifest.permission.READ_CONTACTS
import android.Manifest.permission.WRITE_EXTERNAL_STORAGE
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.annotation.UiThread
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.PermissionChecker.PERMISSION_GRANTED
import com.ns4d.contactCollector.db.ContactRepository
import kotlinx.android.synthetic.main.fragment_contacts.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        val toolbar = findViewById<Toolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)

        if (requestContactsPermission()) {
            CollectorService.enqueueActionScan(this, 0)
        }
    }

    @UiThread
    fun showRationaleForContacts() {

        AlertDialog.Builder(this)
                .setTitle(R.string.permission_required)
                .setMessage(R.string.pls_grant_msg)
                .setPositiveButton(android.R.string.ok) { _: DialogInterface, _: Int ->
                    ActivityCompat.requestPermissions(this, arrayOf(READ_CONTACTS),
                            PERMISSION_CONTACTS)
                }
                .show()
    }

    @UiThread
    fun showRationaleForStorage() {

        AlertDialog.Builder(this)
                .setTitle(R.string.permission_required)
                .setMessage(R.string.rationale_msg)
                .setPositiveButton(android.R.string.ok) { _: DialogInterface, _: Int ->
                    ActivityCompat.requestPermissions(this, arrayOf(WRITE_EXTERNAL_STORAGE),
                            PERMISSION_EXTERNAL_STORAGE)
                }
                .show()
    }

    /**
     * Check and request READ_CONTACTS permission
     */
    private fun requestContactsPermission(): Boolean {

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
    private fun requestExternalStoragePermission(): Boolean {

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

        when (item.itemId) {
            R.id.action_save ->
                if (requestExternalStoragePermission()) {
                    saveContacts()
                    return true
                } else {
                    Log.e(TAG, "Insufficient permission to save!")
                }
            R.id.action_about -> {

                val size = ContactRepository.count()
                val msg = getString(R.string.app_name) + "\nVersion: " + BuildConfig.VERSION_NAME + "." + BuildConfig.VERSION_CODE + "\nFound " + size + " contacts"
                AlertDialog.Builder(this)
                    .setTitle(R.string.action_about)
                    .setMessage(msg)
                    .show()
            }
            R.id.action_reload -> {

                if (requestContactsPermission()) {
                    CollectorService.enqueueActionScan(this, 0)
                }
            }
        }

        return super.onOptionsItemSelected(item)
    }

    /**
     * This method assumes that permission has been granted
     */
    private fun saveContacts() {

        AlertDialog.Builder(this)
                .setTitle(R.string.save)
                .setMessage(R.string.save_confirmation_msg)
                .setPositiveButton(android.R.string.ok) { _: DialogInterface, _: Int ->

                    if (isExternalStorageWritable()) {
                        CollectorService.enqueueActionSave(this)
                    } else {
                        Toast.makeText(this, getString(R.string.storage_locked), Toast.LENGTH_SHORT).show()
                    }

                }.show()
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

        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            when (requestCode) {
                PERMISSION_CONTACTS -> {
                    Toast.makeText(this, R.string.please_wait_msg, Toast.LENGTH_LONG).show()
                    CollectorService.enqueueActionScan(this, 0)
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

    companion object {
        private const val PERMISSION_CONTACTS = 1000
        private const val PERMISSION_EXTERNAL_STORAGE = 1001
        private const val TAG = "MainActivity"
    }
}
