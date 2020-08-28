package org.cru.godtools.base.dagger

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent

// Workaround for this bug: https://github.com/google/dagger/issues/1918
@Deprecated("Can be removed once https://github.com/google/dagger/issues/1918 is released")
abstract class HiltBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) = Unit
}
