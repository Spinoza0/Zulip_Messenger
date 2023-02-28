package org.kimp.tfs.hw1.ui.activity

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import dagger.hilt.android.AndroidEntryPoint
import org.kimp.tfs.hw1.HomeworkApplication
import org.kimp.tfs.hw1.R
import org.kimp.tfs.hw1.databinding.ActivitySecondLayoutBinding
import org.kimp.tfs.hw1.util.service.ContactsLoaderService
import timber.log.Timber

@AndroidEntryPoint
class SecondActivity: AppCompatActivity() {
    private lateinit var binding: ActivitySecondLayoutBinding

    private val readContactsPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
            Timber.tag(HomeworkApplication.TAG)
                .i("Was granted to read contacts list")

            startService(Intent(this, ContactsLoaderService::class.java))
        } else {
            Timber.tag(HomeworkApplication.TAG)
                .i("User denied access to the contacts list")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySecondLayoutBinding.inflate(layoutInflater)
        setContentView(binding.root)

        connectActions()
    }

    private fun connectActions() {
        binding.loadContactsButton.setOnClickListener {
            when {
                checkSelfPermission(Manifest.permission.READ_CONTACTS) == PackageManager.PERMISSION_GRANTED -> {
                    startService(Intent(this, ContactsLoaderService::class.java))
                }
                shouldShowRequestPermissionRationale(Manifest.permission.READ_CONTACTS) -> {
                    AlertDialog.Builder(this)
                        .setMessage(R.string.contacts_permission_request_rationale)
                        .setPositiveButton(R.string.contacts_permission_request_grant) { _, _ ->
                            readContactsPermissionLauncher.launch(Manifest.permission.READ_CONTACTS)
                        }
                        .setNegativeButton(R.string.contacts_permission_request_deny) { _, _, -> }
                        .show()
                }
                else -> {
                    readContactsPermissionLauncher.launch(Manifest.permission.READ_CONTACTS)
                }
            }
        }
    }
}