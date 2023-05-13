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

    fun isGranted(permissionType: Type): Boolean = isGranted(
        ActivityCompat.checkSelfPermission(fragment.requireActivity(), permissionType.value)
    )

    fun request(permissionType: Type, onGrantedCallback: () -> Unit) {
        this.onGrantedCallback = onGrantedCallback
        requestPermissionLauncher.launch(permissionType.value)
    }

    private fun isGranted(permission: Int): Boolean =
        PackageManager.PERMISSION_GRANTED == permission

    enum class Type(val value: String) {
        READ(android.Manifest.permission.READ_EXTERNAL_STORAGE),
        WRITE(android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
    }
}