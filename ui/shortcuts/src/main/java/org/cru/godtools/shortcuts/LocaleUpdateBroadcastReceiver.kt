package org.cru.godtools.shortcuts

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class LocaleUpdateBroadcastReceiver : BroadcastReceiver() {
    @Inject
    internal lateinit var shortcutManager: GodToolsShortcutManager

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_LOCALE_CHANGED) return
        shortcutManager.onUpdateSystemLocale(goAsync())
    }
}
