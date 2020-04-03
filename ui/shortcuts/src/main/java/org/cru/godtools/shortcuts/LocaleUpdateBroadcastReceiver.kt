package org.cru.godtools.shortcuts

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import dagger.android.AndroidInjection
import javax.inject.Inject

class LocaleUpdateBroadcastReceiver : BroadcastReceiver() {
    @Inject
    internal lateinit var shortcutManager: GodToolsShortcutManager

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_LOCALE_CHANGED) return

        AndroidInjection.inject(this, context)
        shortcutManager.onUpdateSystemLocale(goAsync())
    }
}
