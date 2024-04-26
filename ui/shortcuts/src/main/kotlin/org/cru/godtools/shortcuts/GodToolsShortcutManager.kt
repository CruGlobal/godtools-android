package org.cru.godtools.shortcuts

import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.annotation.AnyThread
import androidx.annotation.VisibleForTesting
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.content.pm.ShortcutManagerCompat.FLAG_MATCH_CACHED
import androidx.core.content.pm.ShortcutManagerCompat.FLAG_MATCH_PINNED
import androidx.core.graphics.drawable.IconCompat
import com.google.android.gms.common.wrappers.InstantApps
import com.squareup.picasso.Picasso
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.IOException
import java.lang.ref.WeakReference
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.asFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.mapNotNull
import kotlinx.coroutines.flow.merge
import kotlinx.coroutines.flow.take
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.ccci.gto.android.common.picasso.getBitmap
import org.cru.godtools.base.Settings
import org.cru.godtools.base.ToolFileSystem
import org.cru.godtools.base.tool.SHORTCUT_LAUNCH
import org.cru.godtools.base.ui.createArticlesIntent
import org.cru.godtools.base.ui.createCyoaActivityIntent
import org.cru.godtools.base.ui.createTractActivityIntent
import org.cru.godtools.base.ui.util.getName
import org.cru.godtools.db.repository.AttachmentsRepository
import org.cru.godtools.db.repository.ToolsRepository
import org.cru.godtools.db.repository.TranslationsRepository
import org.cru.godtools.model.Tool
import org.cru.godtools.model.event.ToolUsedEvent
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import timber.log.Timber

internal const val DELAY_UPDATE_SHORTCUTS = 5000L
internal const val DELAY_UPDATE_PENDING_SHORTCUTS = 100L

private val SUPPORTED_TOOL_TYPES = setOf(Tool.Type.ARTICLE, Tool.Type.CYOA, Tool.Type.TRACT)

@Singleton
class GodToolsShortcutManager @VisibleForTesting internal constructor(
    private val attachmentsRepository: AttachmentsRepository,
    private val context: Context,
    eventBus: EventBus,
    private val fs: ToolFileSystem,
    private val picasso: Picasso,
    private val settings: Settings,
    private val toolsRepository: ToolsRepository,
    private val translationsRepository: TranslationsRepository,
    private val coroutineScope: CoroutineScope,
    private val ioDispatcher: CoroutineContext = Dispatchers.IO,
) {
    @Inject
    constructor(
        attachmentsRepository: AttachmentsRepository,
        @ApplicationContext context: Context,
        eventBus: EventBus,
        fs: ToolFileSystem,
        picasso: Picasso,
        settings: Settings,
        toolsRepository: ToolsRepository,
        translationsRepository: TranslationsRepository,
    ) : this(
        attachmentsRepository,
        context,
        eventBus,
        fs,
        picasso,
        settings,
        toolsRepository,
        translationsRepository,
        coroutineScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    )

    @VisibleForTesting
    internal val isEnabled = !InstantApps.isInstantApp(context)

    // region Events
    init {
        // register event listeners
        if (isEnabled) eventBus.register(this)
    }

    @AnyThread
    @Subscribe
    fun onToolUsed(event: ToolUsedEvent) {
        if (!isEnabled) return
        ShortcutManagerCompat.reportShortcutUsed(context, ShortcutId.Tool(event.toolCode).id)
    }
    // endregion Events

    // region Pending Shortcuts
    private val pendingShortcuts = mutableMapOf<String, WeakReference<PendingShortcut>>()

    @AnyThread
    fun canPinToolShortcut(tool: Tool?) = when {
        !isEnabled -> false
        else -> when (tool?.type) {
            Tool.Type.ARTICLE,
            Tool.Type.CYOA,
            Tool.Type.TRACT -> ShortcutManagerCompat.isRequestPinShortcutSupported(context)
            else -> false
        }
    }

    @AnyThread
    fun getPendingToolShortcut(code: String?, vararg locales: Locale?): PendingShortcut? {
        if (!isEnabled) return null
        if (code == null) return null
        val id = ShortcutId.Tool(code, *locales)

        return synchronized(pendingShortcuts) {
            pendingShortcuts[id.id]?.get()
                ?: PendingShortcut(id).also {
                    pendingShortcuts[id.id] = WeakReference(it)
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
        shortcut.shortcut = when (shortcut.id) {
            is ShortcutId.Tool -> createToolShortcut(shortcut.id)
        }
    }
    // endregion Pending Shortcuts

    // region Update Existing Shortcuts
    private val updateShortcutsMutex = Mutex()

    @VisibleForTesting
    internal suspend fun updateShortcuts() {
        updateShortcutsMutex.withLock {
            coroutineScope {
                launch { updateDynamicShortcuts() }
                launch { updatePinnedShortcuts() }
            }
        }
    }

    @VisibleForTesting
    internal suspend fun updateDynamicShortcuts() {
        if (!isEnabled) return

        val dynamicShortcuts = withContext(ioDispatcher) {
            toolsRepository.getNormalTools()
                .filter { it.isFavorite }
                .sortedWith(Tool.COMPARATOR_FAVORITE_ORDER)
                .asFlow()
                .mapNotNull { createToolShortcut(it) }
                .take(ShortcutManagerCompat.getMaxShortcutCountPerActivity(context))
                .toList()
        }

        try {
            ShortcutManagerCompat.setDynamicShortcuts(context, dynamicShortcuts)
        } catch (e: IllegalStateException) {
            Timber.tag("GodToolsShortcutManager").e(e, "Error updating dynamic shortcuts")
        }
    }

    @VisibleForTesting
    internal suspend fun updatePinnedShortcuts() {
        if (!isEnabled) return

        withContext(ioDispatcher) {
            val types = FLAG_MATCH_PINNED or FLAG_MATCH_CACHED
            val (invalid, shortcuts) = ShortcutManagerCompat.getShortcuts(context, types)
                .mapNotNull { it.id to ShortcutId.parseId(it.id) }
                .map { (id, shortcutId) ->
                    when (shortcutId) {
                        null -> CompletableDeferred(id to null)
                        is ShortcutId.Tool -> async { shortcutId.id to createToolShortcut(shortcutId) }
                    }
                }
                .awaitAll()
                .partition { it.second == null }
                .let { it.first.map { it.first } to it.second.mapNotNull { it.second } }

            if (invalid.isNotEmpty()) {
                ShortcutManagerCompat.disableShortcuts(context, invalid, null)
            }
            if (shortcuts.isNotEmpty()) {
                ShortcutManagerCompat.enableShortcuts(context, shortcuts)
                ShortcutManagerCompat.updateShortcuts(context, shortcuts)
            }
        }
    }
    // endregion Update Existing Shortcuts

    internal suspend fun refreshShortcutsNow() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) updateShortcuts()
        updatePendingShortcuts()
    }

    @VisibleForTesting
    internal suspend fun createToolShortcut(id: ShortcutId.Tool) = withContext(ioDispatcher) {
        createToolShortcut(id, toolsRepository.findTool(id.tool))
    }

    private suspend fun createToolShortcut(tool: Tool) = tool.code
        ?.let { ShortcutId.Tool(it) }
        ?.let { createToolShortcut(it, tool) }

    private suspend fun createToolShortcut(id: ShortcutId.Tool, tool: Tool?) = withContext(ioDispatcher) {
        if (tool == null) return@withContext null
        val type = tool.type
        if (type !in SUPPORTED_TOOL_TYPES) return@withContext null

        // generate the list of translations to use for this tool
        val translations = if (id.isFavoriteToolShortcut) {
            flowOf(settings.appLanguage, tool.defaultLocale)
                .mapNotNull { translationsRepository.findLatestTranslation(id.tool, it) }
                .take(1)
                .toList()
        } else {
            id.locales.mapNotNull { translationsRepository.findLatestTranslation(id.tool, it) }
        }
        if (translations.isEmpty()) return@withContext null

        // generate the target intent for this shortcut
        val locales = translations.map { it.languageCode }
        val intent = when (type) {
            Tool.Type.ARTICLE -> context.createArticlesIntent(id.tool, locales[0])
            Tool.Type.CYOA -> context.createCyoaActivityIntent(id.tool, *locales.toTypedArray())
            Tool.Type.TRACT -> context.createTractActivityIntent(id.tool, *locales.toTypedArray())
            else -> error("Unexpected Tool Type: $type")
        }
        intent.action = Intent.ACTION_VIEW
        intent.putExtra(SHORTCUT_LAUNCH, true)

        // Generate the shortcut label
        val label = translations.first().getName(tool, context)

        // create the icon bitmap
        val icon: IconCompat = tool.detailsBannerId
            ?.let { attachmentsRepository.findAttachment(it) }
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
            ?: IconCompat.createWithResource(context, org.cru.godtools.ui.R.mipmap.ic_launcher)

        // build the shortcut
        ShortcutInfoCompat.Builder(context, id.id)
            .setAlwaysBadged()
            .setIntent(intent)
            .setShortLabel(label)
            .setLongLabel(label)
            .setIcon(icon)
            .build()
    }

    @Singleton
    internal class Dispatcher @VisibleForTesting internal constructor(
        private val manager: GodToolsShortcutManager,
        attachmentsRepository: AttachmentsRepository,
        settings: Settings,
        toolsRepository: ToolsRepository,
        translationsRepository: TranslationsRepository,
        coroutineScope: CoroutineScope
    ) {
        @Inject
        constructor(
            manager: GodToolsShortcutManager,
            attachmentsRepository: AttachmentsRepository,
            settings: Settings,
            toolsRepository: ToolsRepository,
            translationsRepository: TranslationsRepository,
        ) : this(
            manager = manager,
            attachmentsRepository = attachmentsRepository,
            settings = settings,
            toolsRepository = toolsRepository,
            translationsRepository = translationsRepository,
            coroutineScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
        )

        @VisibleForTesting
        internal val updatePendingShortcutsJob = coroutineScope.launch {
            if (!manager.isEnabled) return@launch

            merge(
                settings.appLanguageFlow,
                attachmentsRepository.attachmentsChangeFlow(),
                toolsRepository.toolsChangeFlow(),
                translationsRepository.translationsChangeFlow(),
            ).conflate().collectLatest {
                delay(DELAY_UPDATE_PENDING_SHORTCUTS)
                manager.updatePendingShortcuts()
            }
        }

        @VisibleForTesting
        internal val updateShortcutsJob = coroutineScope.launch {
            if (!manager.isEnabled) return@launch

            merge(
                settings.appLanguageFlow,
                attachmentsRepository.attachmentsChangeFlow(),
                toolsRepository.toolsChangeFlow(),
                translationsRepository.translationsChangeFlow(),
            ).conflate().collectLatest {
                delay(DELAY_UPDATE_SHORTCUTS)
                manager.updateShortcuts()
            }
        }
    }
}
