package org.cru.godtools.shortcuts

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.work.WorkManager
import dagger.Lazy
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class LocaleUpdateBroadcastReceiver : BroadcastReceiver() {
    @Inject
    internal lateinit var workManager: Lazy<WorkManager>

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_LOCALE_CHANGED) return
        workManager.get().scheduleUpdateShortcutsWork()
    }
}
