package org.cru.godtools.shortcuts

import androidx.core.content.pm.ShortcutInfoCompat

class PendingShortcut internal constructor(val tool: String) {
    @Volatile
    var shortcut: ShortcutInfoCompat? = null
}
