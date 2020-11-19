package org.cru.godtools.shortcuts

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.ShortcutManager
import android.os.Build
import androidx.annotation.AnyThread
import androidx.annotation.RequiresApi
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
import java.util.concurrent.atomic.AtomicReference
import javax.inject.Inject
import javax.inject.Singleton
import kotlin.coroutines.CoroutineContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.ObsoleteCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.channels.Channel.Factory.CONFLATED
import kotlinx.coroutines.channels.actor
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import org.ccci.gto.android.common.db.Query
import org.ccci.gto.android.common.db.find
import org.ccci.gto.android.common.db.get
import org.ccci.gto.android.common.util.LocaleUtils
import org.cru.godtools.article.ui.categories.createCategoriesIntent
import org.cru.godtools.base.Settings
import org.cru.godtools.base.tool.SHORTCUT_LAUNCH
import org.cru.godtools.base.tool.createTractActivityIntent
import org.cru.godtools.base.ui.util.getName
import org.cru.godtools.base.util.getGodToolsFile
import org.cru.godtools.model.Attachment
import org.cru.godtools.model.Tool
import org.cru.godtools.model.event.AttachmentUpdateEvent
import org.cru.godtools.model.event.ToolUpdateEvent
import org.cru.godtools.model.event.ToolUsedEvent
import org.cru.godtools.model.event.TranslationUpdateEvent
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.keynote.godtools.android.db.Contract.ToolTable
import org.keynote.godtools.android.db.GodToolsDao
import timber.log.Timber

private const val TYPE_TOOL = "tool|"

internal const val DELAY_PENDING_SHORTCUT_UPDATE = 100L

@Singleton
class GodToolsShortcutManager @VisibleForTesting internal constructor(
    @ApplicationContext private val context: Context,
    private val dao: GodToolsDao,
    eventBus: EventBus,
    private val settings: Settings,
    private val coroutineScope: CoroutineScope,
    private val ioDispatcher: CoroutineContext = Dispatchers.IO
) : SharedPreferences.OnSharedPreferenceChangeListener {
    @Inject
    constructor(
        @ApplicationContext context: Context,
        dao: GodToolsDao,
        eventBus: EventBus,
        settings: Settings
    ) : this(context, dao, eventBus, settings, CoroutineScope(Dispatchers.Default + SupervisorJob()))

    @get:RequiresApi(Build.VERSION_CODES.N_MR1)
    private val shortcutManager by lazy { context.getSystemService<ShortcutManager>() }

    init {
        // register event listeners
        eventBus.register(this)
        settings.registerOnSharedPreferenceChangeListener(this)
    }

    // region Events
    @AnyThread
    fun onUpdateSystemLocale(result: BroadcastReceiver.PendingResult) {
        coroutineScope.launch {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) updateShortcuts()
            updatePendingShortcuts()
            result.finish()
        }
    }

    @AnyThread
    @Subscribe
    fun onToolUpdate(event: ToolUpdateEvent) {
        // Could change which tools are visible or the label for tools
        launchUpdateShortcutsJob(false)
        updatePendingShortcutsActor.offer(Unit)
    }

    @AnyThread
    @Subscribe
    fun onAttachmentUpdate(event: AttachmentUpdateEvent) {
        // Handles potential icon image changes.
        launchUpdateShortcutsJob(false)
        updatePendingShortcutsActor.offer(Unit)
    }

    @AnyThread
    @Subscribe
    fun onTranslationUpdate(event: TranslationUpdateEvent) {
        // Could change which tools are available or the label for tools
        launchUpdateShortcutsJob(false)
        updatePendingShortcutsActor.offer(Unit)
    }

    @AnyThread
    @Subscribe
    fun onToolUsed(event: ToolUsedEvent) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            shortcutManager?.reportShortcutUsed(event.toolCode.toolShortcutId)
        }
    }

    override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?) {
        when (key) {
            // primary/parallel language preferences changed. key=null when preferences are cleared
            Settings.PREF_PRIMARY_LANGUAGE, Settings.PREF_PARALLEL_LANGUAGE, null -> {
                launchUpdateShortcutsJob(false)
                updatePendingShortcutsActor.offer(Unit)
            }
        }
    }
    // endregion Events

    // region Pending Shortcuts
    private val pendingShortcuts = mutableMapOf<String, WeakReference<PendingShortcut>>()

    @AnyThread
    fun canPinToolShortcut(tool: Tool?) = when (tool?.type) {
        Tool.Type.TRACT, Tool.Type.ARTICLE -> ShortcutManagerCompat.isRequestPinShortcutSupported(context)
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

    @VisibleForTesting
    @OptIn(ObsoleteCoroutinesApi::class)
    internal val updatePendingShortcutsActor = coroutineScope.actor<Unit>(capacity = CONFLATED) {
        channel.consumeAsFlow().conflate().collectLatest {
            delay(DELAY_PENDING_SHORTCUT_UPDATE)
            updatePendingShortcuts()
        }
    }

    @AnyThread
    private suspend fun updatePendingShortcuts() = coroutineScope {
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
    private val updateShortcutsJob = AtomicReference<Job?>()
    private val updateShortcutsMutex = Mutex()

    init {
        // launch an initial update
        launchUpdateShortcutsJob(false)
    }

    @AnyThread
    private fun launchUpdateShortcutsJob(immediate: Boolean) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N_MR1) return

        // cancel any pending update
        updateShortcutsJob.getAndSet(null)?.takeIf { it.isActive }?.cancel()

        // launch the update
        updateShortcutsJob.set(coroutineScope.launch {
            if (!immediate) delay(5_000)
            withContext(NonCancellable) { updateShortcuts() }
        })
    }

    @AnyThread
    @RequiresApi(Build.VERSION_CODES.N_MR1)
    private suspend fun updateShortcuts() = updateShortcutsMutex.withLock {
        val shortcuts = createAllShortcuts()
        updateDynamicShortcuts(shortcuts)
        updatePinnedShortcuts(shortcuts)
    }

    @RequiresApi(Build.VERSION_CODES.N_MR1)
    private suspend fun updateDynamicShortcuts(shortcuts: Map<String, ShortcutInfoCompat>) = try {
        withContext(Dispatchers.IO) {
            shortcutManager?.dynamicShortcuts = Query.select<Tool>()
                .where(ToolTable.FIELD_ADDED.eq(true))
                .orderBy(ToolTable.SQL_ORDER_BY_ORDER)
                .get(dao)
                .mapNotNull { shortcuts[it.shortcutId]?.toShortcutInfo() }
                .take(ShortcutManagerCompat.getMaxShortcutCountPerActivity(context))
        }
    } catch (e: IllegalStateException) {
        Timber.tag("GodToolsShortcutManager").e(e, "Error updating dynamic shortcuts")
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
            val translation = dao.getLatestTranslation(code, settings.primaryLanguage)
                ?: dao.getLatestTranslation(code, Locale.ENGLISH)
                ?: return@withContext null
            add(translation.languageCode)
            settings.parallelLanguage?.let { add(it) }
        }

        // generate the target intent for this shortcut
        val intent = when (tool.type) {
            Tool.Type.TRACT -> context.createTractActivityIntent(code, *locales.toTypedArray())
            Tool.Type.ARTICLE -> context.createCategoriesIntent(code, locales[0])
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
            ?.let { context.getGodToolsFile(it.localFilename) }
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
}

private val Tool.shortcutId get() = code.toolShortcutId
private val String?.toolShortcutId get() = "$TYPE_TOOL$this"
