package com.spinoza.messenger_tfs

import android.app.Application
import android.content.Context
import androidx.test.runner.AndroidJUnitRunner
import com.spinoza.messenger_tfs.app.TestApp

class CustomTestRunner: AndroidJUnitRunner() {

    override fun newApplication(cl: ClassLoader?, name: String?, context: Context?): Application {
        return super.newApplication(cl, TestApp::class.java.name, context)
    }
}