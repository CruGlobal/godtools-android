package org.cru.godtools.shortcuts

import android.content.Context
import android.content.Intent
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import org.cru.godtools.base.dagger.HiltBroadcastReceiver

@AndroidEntryPoint
class LocaleUpdateBroadcastReceiver : HiltBroadcastReceiver() {
    @Inject
    internal lateinit var shortcutManager: GodToolsShortcutManager

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_LOCALE_CHANGED) return

        super.onReceive(context, intent)
        shortcutManager.onUpdateSystemLocale(goAsync())
    }
}
