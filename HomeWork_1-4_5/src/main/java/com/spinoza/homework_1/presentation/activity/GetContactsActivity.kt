package com.spinoza.homework_1.presentation.activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.spinoza.homework_1.R
import com.spinoza.homework_1.databinding.ActivityGetContactsBinding
import com.spinoza.homework_1.presentation.broadcastreceiver.ContactsReceiver
import com.spinoza.homework_1.presentation.service.GetContactsService
import com.spinoza.homework_1.presentation.utils.Constants.Companion.EXTRA_ERROR_TEXT

class GetContactsActivity : AppCompatActivity() {

    private val binding: ActivityGetContactsBinding by lazy {
        ActivityGetContactsBinding.inflate(layoutInflater)
    }

    private val contactsReceiver by lazy {
        ContactsReceiver { resultCode, intent ->
            finishActivity(resultCode, intent)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        contactsReceiver.register(this)

        if (!checkPermissionAndRequestContacts(
                ActivityCompat.checkSelfPermission(
                    this,
                    android.Manifest.permission.READ_CONTACTS
                )
            )
        ) {
            requestPermission()
        }
    }

    override fun onStop() {
        super.onStop()
        contactsReceiver.unregister()
    }

    private fun checkPermissionAndRequestContacts(permissions: Int): Boolean {
        if (permissions == PackageManager.PERMISSION_GRANTED) {
            binding.progressBar.visibility = View.VISIBLE
            startService(GetContactsService.newIntent(this))
            return true
        }
        return false
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
    ) {
        if (requestCode == READ_CONTACTS_REQUEST_CODE && grantResults.isNotEmpty()) {
            if (!checkPermissionAndRequestContacts(grantResults[0])) {
                val intent = Intent().putExtra(
                    EXTRA_ERROR_TEXT,
                    getString(R.string.no_permission)
                )
                finishActivity(Activity.RESULT_CANCELED, intent)
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    private fun requestPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(android.Manifest.permission.READ_CONTACTS),
            READ_CONTACTS_REQUEST_CODE
        )
    }

    private fun finishActivity(resultCode: Int, intent: Intent) {
        setResult(resultCode, intent)
        finish()
    }

    companion object {
        private const val READ_CONTACTS_REQUEST_CODE = 100

        fun newIntent(context: Context) = Intent(context, GetContactsActivity::class.java)
    }
}