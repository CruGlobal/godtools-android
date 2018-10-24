package org.cru.godtools.shortcuts

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

class LocaleUpdateBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (Intent.ACTION_LOCALE_CHANGED == intent.action) {
            GodToolsShortcutManager.getInstance(context).onUpdateSystemLocale(goAsync())
        }
    }
}
