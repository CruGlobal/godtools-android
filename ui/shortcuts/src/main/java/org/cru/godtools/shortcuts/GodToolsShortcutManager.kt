package org.cru.godtools.shortcuts

import android.content.Context
import android.content.Intent
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.annotation.WorkerThread
import androidx.core.content.getSystemService
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import com.squareup.picasso.Picasso
import org.ccci.gto.android.common.db.Query
import org.ccci.gto.android.common.db.find
import org.ccci.gto.android.common.db.get
import org.ccci.gto.android.common.util.LocaleUtils
import org.cru.godtools.article.ui.categories.createCategoriesIntent
import org.cru.godtools.base.Settings
import org.cru.godtools.base.ui.util.getName
import org.cru.godtools.base.util.getGodToolsFile
import org.cru.godtools.model.Attachment
import org.cru.godtools.model.Tool
import org.cru.godtools.tract.activity.createTractActivityIntent
import org.keynote.godtools.android.db.Contract.ToolTable
import org.keynote.godtools.android.db.GodToolsDao
import java.io.IOException
import java.util.Locale

private const val TYPE_TOOL = "tool|"

open class KotlinGodToolsShortcutManager(
    protected val context: Context,
    protected val dao: GodToolsDao,
    protected val settings: Settings
) {
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

    @WorkerThread
    @OptIn(ExperimentalStdlibApi::class)
    protected fun createToolShortcut(tool: Tool): ShortcutInfoCompat? {
        val code = tool.code ?: return null

        // generate the list of locales to use for this tool
        val locales = buildList {
            val translation = dao.getLatestTranslation(code, settings.primaryLanguage).orElse(null)
                ?: dao.getLatestTranslation(code, Locale.ENGLISH).orElse(null)
                ?: return null
            add(translation.languageCode)
            settings.parallelLanguage?.let { add(it) }
        }

        // generate the target intent for this shortcut
        val intent = when (tool.type) {
            Tool.Type.TRACT -> context.createTractActivityIntent(code, *locales.toTypedArray())
            Tool.Type.ARTICLE -> context.createCategoriesIntent(code, locales[0])
            else -> return null
        }.apply { action = Intent.ACTION_VIEW }

        // Generate the shortcut label
        val label = LocaleUtils.getFallbacks(Locale.getDefault(), Locale.ENGLISH).asSequence()
            .mapNotNull { dao.getLatestTranslation(code, it).orElse(null) }
            .firstOrNull()
            .getName(tool, context)

        // create the icon bitmap
        val icon: IconCompat = dao.find<Attachment>(tool.detailsBannerId)
            ?.let { context.getGodToolsFile(it.localFileName) }
            ?.let {
                try {
                    Picasso.get().load(it)
                        .resizeDimen(R.dimen.adaptive_app_icon_size, R.dimen.adaptive_app_icon_size)
                        .centerCrop()
                        .get()
                } catch (e: IOException) {
                    null
                }
            }
            ?.let { IconCompat.createWithAdaptiveBitmap(it) }
            ?: IconCompat.createWithResource(context, R.mipmap.ic_launcher)

        // build the shortcut
        return ShortcutInfoCompat.Builder(context, tool.shortcutId)
            .setAlwaysBadged()
            .setIntent(intent)
            .setShortLabel(label)
            .setLongLabel(label)
            .setIcon(icon)
            .build()
    }

    private val Tool.shortcutId get() = code.toolShortcutId
    protected val String?.toolShortcutId get() = "$TYPE_TOOL$this"
}
