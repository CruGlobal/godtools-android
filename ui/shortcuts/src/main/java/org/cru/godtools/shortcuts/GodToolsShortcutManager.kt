package org.cru.godtools.shortcuts

import android.content.Context
import android.content.Intent
import android.content.pm.ShortcutManager
import android.os.Build
import androidx.annotation.AnyThread
import androidx.annotation.RequiresApi
import androidx.annotation.WorkerThread
import androidx.core.content.getSystemService
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import com.squareup.picasso.Picasso
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
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
import org.cru.godtools.model.event.ToolUsedEvent
import org.cru.godtools.tract.activity.createTractActivityIntent
import org.greenrobot.eventbus.Subscribe
import org.keynote.godtools.android.db.Contract.ToolTable
import org.keynote.godtools.android.db.GodToolsDao
import java.io.IOException
import java.util.Locale
import java.util.concurrent.atomic.AtomicReference

private const val TYPE_TOOL = "tool|"

open class KotlinGodToolsShortcutManager(
    protected val context: Context,
    protected val dao: GodToolsDao,
    protected val settings: Settings
) {
    @get:RequiresApi(Build.VERSION_CODES.N_MR1)
    protected val shortcutManager by lazy { context.getSystemService<ShortcutManager>() }

    // region Events
    @AnyThread
    @Subscribe
    fun onToolUsed(event: ToolUsedEvent) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            shortcutManager?.reportShortcutUsed(event.toolCode.toolShortcutId)
        }
    }
    // endregion Events

    // region Pending Shortcuts
    @AnyThread
    fun canPinToolShortcut(tool: Tool?) = when (tool?.type) {
        Tool.Type.TRACT, Tool.Type.ARTICLE -> ShortcutManagerCompat.isRequestPinShortcutSupported(context)
        else -> false
    }

    @AnyThread
    fun pinShortcut(pendingShortcut: PendingShortcut) {
        pendingShortcut.shortcut?.let { ShortcutManagerCompat.requestPinShortcut(context, it, null) }
    }

    @WorkerThread
    protected fun updatePendingShortcut(shortcut: PendingShortcut) = runBlocking {
        shortcut.mutex.withLock {
            val tool = dao.find<Tool>(shortcut.tool) ?: return@runBlocking
            shortcut.shortcut = createToolShortcut(tool)
        }
    }
    // endregion Pending Shortcuts

    // region Update Existing Shortcuts
    private val updateShortcutsJob = AtomicReference<Job?>()
    private val updateShortcutsMutex = Mutex()

    @AnyThread
    protected fun launchUpdateShortcutsJob(immediate: Boolean) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N_MR1) return

        // cancel any pending update
        updateShortcutsJob.getAndSet(null)?.takeIf { it.isActive }?.cancel()

        // launch the update
        updateShortcutsJob.set(GlobalScope.launch {
            if (!immediate) delay(5_000)
            withContext(NonCancellable) { updateShortcuts() }
        })
    }

    @WorkerThread
    @RequiresApi(Build.VERSION_CODES.N_MR1)
    protected fun updateShortcuts() = runBlocking {
        updateShortcutsMutex.withLock {
            val shortcuts = createAllShortcuts()
            updateDynamicShortcuts(shortcuts)
            updatePinnedShortcuts(shortcuts)
        }
    }

    @RequiresApi(Build.VERSION_CODES.N_MR1)
    private suspend fun updateDynamicShortcuts(shortcuts: Map<String, ShortcutInfoCompat>) =
        withContext(Dispatchers.IO) {
            shortcutManager?.dynamicShortcuts = Query.select<Tool>()
                .where(ToolTable.FIELD_ADDED.eq(true))
                .orderBy(ToolTable.SQL_ORDER_BY_ORDER)
                .get(dao)
                .mapNotNull { shortcuts[it.shortcutId]?.toShortcutInfo() }
                .take(ShortcutManagerCompat.getMaxShortcutCountPerActivity(context))
        }

    @RequiresApi(Build.VERSION_CODES.N_MR1)
    private fun updatePinnedShortcuts(shortcuts: Map<String, ShortcutInfoCompat>) {
        shortcutManager?.apply {
            disableShortcuts(pinnedShortcuts.map { it.id }.filterNot { shortcuts.containsKey(it) })
            enableShortcuts(shortcuts.keys.toList())
            ShortcutManagerCompat.updateShortcuts(context, shortcuts.values.toList())
        }
    }
    // endregion Update Existing Shortcuts

    private suspend fun createAllShortcuts() = withContext(Dispatchers.IO) {
        dao.get(Tool::class.java)
            .map { async { createToolShortcut(it) } }.awaitAll()
            .filterNotNull()
            .associateBy { it.id }
    }

    @AnyThread
    @OptIn(ExperimentalStdlibApi::class)
    private suspend fun createToolShortcut(tool: Tool) = withContext(Dispatchers.IO) {
        val code = tool.code ?: return@withContext null

        // generate the list of locales to use for this tool
        val locales = buildList {
            val translation = dao.getLatestTranslation(code, settings.primaryLanguage).orElse(null)
                ?: dao.getLatestTranslation(code, Locale.ENGLISH).orElse(null)
                ?: return@withContext null
            add(translation.languageCode)
            settings.parallelLanguage?.let { add(it) }
        }

        // generate the target intent for this shortcut
        val intent = when (tool.type) {
            Tool.Type.TRACT -> context.createTractActivityIntent(code, *locales.toTypedArray())
            Tool.Type.ARTICLE -> context.createCategoriesIntent(code, locales[0])
            else -> return@withContext null
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
                    // TODO: create a suspend extension method to async load an image in a coroutine
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
        ShortcutInfoCompat.Builder(context, tool.shortcutId)
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
