package com.spinoza.homework_1.presentation.activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.spinoza.homework_1.R
import com.spinoza.homework_1.databinding.ActivityGetContactsBinding
import com.spinoza.homework_1.domain.GetContactsResult
import com.spinoza.homework_1.presentation.broadcastreceiver.ContactsReceiver
import com.spinoza.homework_1.presentation.service.GetContactsService
import com.spinoza.homework_1.presentation.utils.Constants.EXTRA_RESULT
import com.spinoza.homework_1.presentation.utils.ReadContactsPermission

class GetContactsActivity : AppCompatActivity() {

    private val binding: ActivityGetContactsBinding by lazy {
        ActivityGetContactsBinding.inflate(layoutInflater)
    }

    private val contactsReceiver = ContactsReceiver { resultCode, intent ->
        finishActivity(resultCode, intent)
    }

    private val readContactsPermission = ReadContactsPermission(this)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        contactsReceiver.register(this)

        if (readContactsPermission.isGranted()) {
            startGetContactService()
        } else {
            readContactsPermission.request()
        }
    }

    override fun onStop() {
        super.onStop()
        contactsReceiver.unregister()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray,
    ) {
        if (readContactsPermission.isGranted(requestCode, grantResults)) {
            startGetContactService()
        } else {
            val intent = Intent().putExtra(
                EXTRA_RESULT,
                GetContactsResult.Error(getString(R.string.no_permission))
            )
            finishActivity(Activity.RESULT_CANCELED, intent)
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    private fun startGetContactService() {
        binding.progressBar.visibility = View.VISIBLE
        startService(GetContactsService.newIntent(this))
    }

    private fun finishActivity(resultCode: Int, intent: Intent) {
        setResult(resultCode, intent)
        finish()
    }

    companion object {
        fun newIntent(context: Context) = Intent(context, GetContactsActivity::class.java)
    }
}