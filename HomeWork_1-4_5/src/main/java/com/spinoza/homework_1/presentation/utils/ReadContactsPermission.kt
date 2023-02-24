package com.spinoza.homework_1.presentation.utils

import android.app.Activity
import android.content.pm.PackageManager
import androidx.core.app.ActivityCompat

class ReadContactsPermission(private val activity: Activity) {

    fun isGranted(): Boolean = isGranted(
        ActivityCompat.checkSelfPermission(
            activity,
            android.Manifest.permission.READ_CONTACTS
        )
    )

    fun isGranted(requestCode: Int, grantResults: IntArray) =
        requestCode == READ_CONTACTS_REQUEST_CODE &&
                grantResults.isNotEmpty() &&
                isGranted(grantResults[0])

    private fun isGranted(permission: Int): Boolean =
        PackageManager.PERMISSION_GRANTED == permission

    fun request() {
        ActivityCompat.requestPermissions(
            activity,
            arrayOf(android.Manifest.permission.READ_CONTACTS),
            READ_CONTACTS_REQUEST_CODE
        )
    }

    companion object {
        private const val READ_CONTACTS_REQUEST_CODE = 100
    }
}