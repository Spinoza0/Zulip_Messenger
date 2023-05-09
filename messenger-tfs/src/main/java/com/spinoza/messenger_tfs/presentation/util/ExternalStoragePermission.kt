package com.spinoza.messenger_tfs.presentation.util

import android.content.pm.PackageManager
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import com.spinoza.messenger_tfs.presentation.feature.messages.MessagesFragment
import javax.inject.Inject

class ExternalStoragePermission @Inject constructor(private val fragment: MessagesFragment) {

    private var onGrantedCallback: (() -> Unit)? = null

    private val requestPermissionLauncher = fragment.registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            onGrantedCallback?.invoke()
        }
    }

    fun isGranted(): Boolean = isGranted(
        ActivityCompat.checkSelfPermission(
            fragment.requireActivity(),
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
    )

    fun request(onGrantedCallback: () -> Unit) {
        this.onGrantedCallback = onGrantedCallback
        requestPermissionLauncher.launch(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
    }

    private fun isGranted(permission: Int): Boolean =
        PackageManager.PERMISSION_GRANTED == permission
}