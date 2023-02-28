package org.kimp.tfs.hw1.util.broadcast

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import dagger.hilt.android.AndroidEntryPoint
import org.kimp.tfs.hw1.HomeworkApplication
import timber.log.Timber

@AndroidEntryPoint
class ContactsReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        Timber.tag(HomeworkApplication.TAG)
            .i("Something received")
    }

    companion object {
        const val ACTION = "org.kimp.tfs.hw1.CONTACTS"
        val INTENT_FILTER = IntentFilter(ACTION)
    }
}