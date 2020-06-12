package org.cru.godtools.shortcuts

import android.content.Context
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.content.getSystemService
import org.cru.godtools.model.Tool
import org.keynote.godtools.android.db.GodToolsDao

private const val TYPE_TOOL = "tool|"

open class KotlinGodToolsShortcutManager(protected val context: Context, protected val dao: GodToolsDao) {
    @get:RequiresApi(Build.VERSION_CODES.N_MR1)
    protected val shortcutManager by lazy { context.getSystemService<ShortcutManager>() }

    @TargetApi(Build.VERSION_CODES.N_MR1)
    protected fun updatePinnedShortcuts(shortcuts: Map<String, ShortcutInfo>) {
        shortcutManager?.apply {
            disableShortcuts(pinnedShortcuts.map { it.id }.filterNot { shortcuts.containsKey(it) })
            enableShortcuts(shortcuts.keys.toList())
            updateShortcuts(shortcuts.values.toList())
        }
    }

    protected val Tool.shortcutId get() = code.toolShortcutId
    protected val String?.toolShortcutId get() = "$TYPE_TOOL$this"
}
