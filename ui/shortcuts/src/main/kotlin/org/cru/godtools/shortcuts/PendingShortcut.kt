package org.cru.godtools.shortcuts

import androidx.core.content.pm.ShortcutInfoCompat
import kotlinx.coroutines.sync.Mutex

class PendingShortcut internal constructor(val tool: String) {
    internal val mutex = Mutex()

    @Volatile
    var shortcut: ShortcutInfoCompat? = null
}
