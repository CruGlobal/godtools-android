package org.cru.godtools.shortcuts

import android.content.Context
import android.content.Intent
import android.content.pm.ShortcutManager
import android.os.Build
import androidx.annotation.AnyThread
import androidx.annotation.RequiresApi
import androidx.annotation.RestrictTo
import androidx.annotation.VisibleForTesting
import androidx.core.content.getSystemService
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import com.squareup.picasso.Picasso
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.IOException
import java.lang.ref.WeakReference
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.ccci.gto.android.common.db.Expression.Companion.constants
import org.ccci.gto.android.common.db.Query
import org.ccci.gto.android.common.db.find
import org.ccci.gto.android.common.db.get
import org.ccci.gto.android.common.picasso.getBitmap
import org.ccci.gto.android.common.util.LocaleUtils
import org.cru.godtools.base.Settings
import org.cru.godtools.base.ToolFileSystem
import org.cru.godtools.base.tool.SHORTCUT_LAUNCH
import org.cru.godtools.base.ui.createArticlesIntent
import org.cru.godtools.base.ui.createCyoaActivityIntent
import org.cru.godtools.base.ui.createTractActivityIntent
import org.cru.godtools.base.ui.util.getName
import org.cru.godtools.model.Attachment
import org.cru.godtools.model.Tool
import org.cru.godtools.model.Translation
import org.cru.godtools.model.event.ToolUsedEvent
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.keynote.godtools.android.db.Contract.ToolTable
import org.keynote.godtools.android.db.GodToolsDao
import timber.log.Timber

private const val TYPE_TOOL = "tool|"

internal const val DELAY_UPDATE_SHORTCUTS = 5000L
internal const val DELAY_UPDATE_PENDING_SHORTCUTS = 100L

@Singleton
class GodToolsShortcutManager @VisibleForTesting internal constructor(
    private val context: Context,
    private val dao: GodToolsDao,
    eventBus: EventBus,
    private val fs: ToolFileSystem,
    private val picasso: Picasso,
    private val settings: Settings,
    private val coroutineScope: CoroutineScope,
    private val ioDispatcher: CoroutineContext = Dispatchers.IO
) {
    @Inject
    constructor(
        @ApplicationContext context: Context,
        dao: GodToolsDao,
        eventBus: EventBus,
        fs: ToolFileSystem,
        picasso: Picasso,
        settings: Settings
    ) : this(
        context,
        dao,
        eventBus,
        fs,
        picasso,
        settings,
        CoroutineScope(Dispatchers.Default + SupervisorJob())
    )

    @get:RequiresApi(Build.VERSION_CODES.N_MR1)
    private val shortcutManager by lazy { context.getSystemService<ShortcutManager>() }

    init {
        // register event listeners
        eventBus.register(this)
    }

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
    private val pendingShortcuts = mutableMapOf<String, WeakReference<PendingShortcut>>()

    @AnyThread
    fun canPinToolShortcut(tool: Tool?) = when (tool?.type) {
        Tool.Type.ARTICLE,
        Tool.Type.CYOA,
        Tool.Type.TRACT -> ShortcutManagerCompat.isRequestPinShortcutSupported(context)
        else -> false
    }

    @AnyThread
    fun getPendingToolShortcut(code: String?): PendingShortcut? {
        val id = code?.toolShortcutId ?: return null

        return synchronized(pendingShortcuts) {
            pendingShortcuts[id]?.get()
                ?: PendingShortcut(code).also {
                    pendingShortcuts[id] = WeakReference(it)
                    coroutineScope.launch { updatePendingShortcut(it) }
                }
        }
    }

    @AnyThread
    fun pinShortcut(pendingShortcut: PendingShortcut) {
        pendingShortcut.shortcut?.let { ShortcutManagerCompat.requestPinShortcut(context, it, null) }
    }

    @AnyThread
    @VisibleForTesting
    internal suspend fun updatePendingShortcuts() = coroutineScope {
        synchronized(pendingShortcuts) {
            val i = pendingShortcuts.iterator()
            while (i.hasNext()) {
                when (val shortcut = i.next().value.get()) {
                    null -> i.remove()
                    else -> launch { updatePendingShortcut(shortcut) }
                }
            }
        }
    }

    @AnyThread
    private suspend fun updatePendingShortcut(shortcut: PendingShortcut) = shortcut.mutex.withLock {
        withContext(ioDispatcher) { dao.find<Tool>(shortcut.tool)?.let { shortcut.shortcut = createToolShortcut(it) } }
    }
    // endregion Pending Shortcuts

    // region Update Existing Shortcuts
    private val updateShortcutsMutex = Mutex()

    @VisibleForTesting
    @RequiresApi(Build.VERSION_CODES.N_MR1)
    internal suspend fun updateShortcuts() = updateShortcutsMutex.withLock {
        val shortcuts = createAllShortcuts()
        updateDynamicShortcuts(shortcuts)
        updatePinnedShortcuts(shortcuts)
    }

    @VisibleForTesting
    @RequiresApi(Build.VERSION_CODES.N_MR1)
    internal suspend fun updateDynamicShortcuts(shortcuts: Map<String, ShortcutInfoCompat>) {
        val manager = shortcutManager ?: return

        val dynamicShortcuts = withContext(ioDispatcher) {
            Query.select<Tool>()
                .where(
                    ToolTable.FIELD_TYPE.`in`(*constants(Tool.Type.ARTICLE, Tool.Type.TRACT))
                        .and(ToolTable.FIELD_ADDED.eq(true))
                )
                .orderBy(ToolTable.SQL_ORDER_BY_ORDER)
                .get(dao).asSequence()
                .mapNotNull { shortcuts[it.shortcutId]?.toShortcutInfo() }
                .take(manager.maxShortcutCountPerActivity)
                .toList()
        }

        try {
            manager.dynamicShortcuts = dynamicShortcuts
        } catch (e: IllegalStateException) {
            Timber.tag("GodToolsShortcutManager").e(e, "Error updating dynamic shortcuts")
        }
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

    internal suspend fun refreshShortcutsNow() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) updateShortcuts()
        updatePendingShortcuts()
    }

    private suspend fun createAllShortcuts() = withContext(ioDispatcher) {
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
            val translation = dao.getLatestTranslation(code, settings.primaryLanguage)
                ?: dao.getLatestTranslation(code, Locale.ENGLISH)
                ?: return@withContext null
            add(translation.languageCode)
            settings.parallelLanguage?.let { add(it) }
        }

        // generate the target intent for this shortcut
        val intent = when (tool.type) {
            Tool.Type.ARTICLE -> context.createArticlesIntent(code, locales[0])
            Tool.Type.CYOA -> context.createCyoaActivityIntent(code, *locales.toTypedArray())
            Tool.Type.TRACT -> context.createTractActivityIntent(code, *locales.toTypedArray())
            else -> return@withContext null
        }
        intent.action = Intent.ACTION_VIEW
        intent.putExtra(SHORTCUT_LAUNCH, true)

        // Generate the shortcut label
        val label = LocaleUtils.getFallbacks(Locale.getDefault(), Locale.ENGLISH).asSequence()
            .mapNotNull { dao.getLatestTranslation(code, it) }
            .firstOrNull()
            .getName(tool, context)

        // create the icon bitmap
        val icon: IconCompat = tool.detailsBannerId
            ?.let { dao.find<Attachment>(it) }
            ?.getFile(fs)
            ?.let {
                try {
                    picasso.load(it)
                        .resizeDimen(R.dimen.adaptive_app_icon_size, R.dimen.adaptive_app_icon_size)
                        .centerCrop()
                        .getBitmap()
                } catch (e: IOException) {
                    null
                }
            }
            ?.let { IconCompat.createWithAdaptiveBitmap(it) }
            ?: IconCompat.createWithResource(context, org.cru.godtools.base.ui.R.mipmap.ic_launcher)

        // build the shortcut
        ShortcutInfoCompat.Builder(context, tool.shortcutId)
            .setAlwaysBadged()
            .setIntent(intent)
            .setShortLabel(label)
            .setLongLabel(label)
            .setIcon(icon)
            .build()
    }

    @Singleton
    class Dispatcher @VisibleForTesting internal constructor(
        private val manager: GodToolsShortcutManager,
        private val dao: GodToolsDao,
        settings: Settings,
        coroutineScope: CoroutineScope
    ) {
        @Inject
        constructor(
            manager: GodToolsShortcutManager,
            dao: GodToolsDao,
            settings: Settings
        ) : this(manager, dao, settings, CoroutineScope(Dispatchers.Default + SupervisorJob()))

        @VisibleForTesting
        internal val updatePendingShortcutsJob = coroutineScope.launch {
            merge(
                settings.primaryLanguageFlow,
                settings.parallelLanguageFlow,
                dao.invalidationFlow(Tool::class.java, Attachment::class.java, Translation::class.java)
            ).conflate().collectLatest {
                delay(DELAY_UPDATE_PENDING_SHORTCUTS)
                manager.updatePendingShortcuts()
            }
        }

        @VisibleForTesting
        internal val updateShortcutsJob = coroutineScope.launch {
            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N_MR1) return@launch

            merge(
                settings.primaryLanguageFlow,
                settings.parallelLanguageFlow,
                dao.invalidationFlow(Tool::class.java, Attachment::class.java, Translation::class.java)
            ).conflate().collectLatest {
                delay(DELAY_UPDATE_SHORTCUTS)
                manager.updateShortcuts()
            }
        }

        @RestrictTo(RestrictTo.Scope.TESTS)
        internal fun shutdown() {
            updatePendingShortcutsJob.cancel()
            updateShortcutsJob.cancel()
        }
    }
}

private val Tool.shortcutId get() = code.toolShortcutId
private val String?.toolShortcutId get() = "$TYPE_TOOL$this"
