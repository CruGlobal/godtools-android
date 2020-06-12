package org.cru.godtools.shortcuts

import android.content.Context
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.annotation.WorkerThread
import androidx.core.content.getSystemService
import androidx.core.content.pm.ShortcutManagerCompat
import org.ccci.gto.android.common.db.Query
import org.ccci.gto.android.common.db.get
import org.cru.godtools.model.Tool
import org.keynote.godtools.android.db.Contract.ToolTable
import org.keynote.godtools.android.db.GodToolsDao

private const val TYPE_TOOL = "tool|"

open class KotlinGodToolsShortcutManager(protected val context: Context, protected val dao: GodToolsDao) {
    @get:RequiresApi(Build.VERSION_CODES.N_MR1)
    protected val shortcutManager by lazy { context.getSystemService<ShortcutManager>() }

    // TODO: make this a suspend function to support calling it from any thread
    @WorkerThread
    @RequiresApi(Build.VERSION_CODES.N_MR1)
    protected fun updateDynamicShortcuts(shortcuts: Map<String, ShortcutInfo>) {
        shortcutManager?.dynamicShortcuts = Query.select<Tool>()
            .where(ToolTable.FIELD_ADDED.eq(true))
            .orderBy(ToolTable.SQL_ORDER_BY_ORDER)
            .get(dao)
            .mapNotNull { shortcuts[it.shortcutId] }
            .take(ShortcutManagerCompat.getMaxShortcutCountPerActivity(context))
    }

    @RequiresApi(Build.VERSION_CODES.N_MR1)
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
