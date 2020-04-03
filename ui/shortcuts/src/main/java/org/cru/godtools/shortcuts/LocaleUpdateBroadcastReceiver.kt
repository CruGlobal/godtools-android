package org.cru.godtools.shortcuts

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import dagger.android.AndroidInjection

class LocaleUpdateBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_LOCALE_CHANGED) return

        AndroidInjection.inject(this, context)
        GodToolsShortcutManager.getInstance(context).onUpdateSystemLocale(goAsync())
    }
}
