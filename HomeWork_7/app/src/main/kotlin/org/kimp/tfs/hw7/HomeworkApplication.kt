package org.kimp.tfs.hw7

import android.app.Application
import com.google.android.material.color.DynamicColors
import dagger.hilt.android.HiltAndroidApp
import timber.log.Timber

@HiltAndroidApp
class HomeworkApplication : Application() {

    override fun onCreate() {
        Timber.plant(Timber.DebugTree())

        if (DynamicColors.isDynamicColorAvailable()) {
            DynamicColors.applyToActivitiesIfAvailable(this)
            Timber.tag(TAG).i("Enabled dynamic colors")
        }

        super.onCreate()
    }

    companion object {
        const val TAG = "TFSApplication"
    }
}
