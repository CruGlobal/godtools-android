package org.cru.godtools.shortcuts

import android.app.Application
import android.content.Intent
import android.content.pm.ActivityInfo
import android.content.pm.ResolveInfo
import android.content.pm.ShortcutManager
import android.os.Build
import androidx.core.content.getSystemService
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.android.gms.common.wrappers.InstantApps
import io.mockk.Called
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerifyAll
import io.mockk.every
import io.mockk.just
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.spyk
import io.mockk.unmockkStatic
import io.mockk.verify
import io.mockk.verifyAll
import java.util.Locale
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.ccci.gto.android.common.testing.timber.ExceptionRaisingTree
import org.ccci.gto.android.common.util.content.equalsIntent
import org.cru.godtools.base.Settings
import org.cru.godtools.base.tool.SHORTCUT_LAUNCH
import org.cru.godtools.base.ui.createTractActivityIntent
import org.cru.godtools.db.repository.ToolsRepository
import org.cru.godtools.db.repository.TranslationsRepository
import org.cru.godtools.model.Tool
import org.cru.godtools.model.event.ToolUsedEvent
import org.cru.godtools.model.randomTool
import org.cru.godtools.model.randomTranslation
import org.greenrobot.eventbus.EventBus
import org.junit.runner.RunWith
import org.robolectric.Shadows
import org.robolectric.annotation.Config
import org.robolectric.annotation.Config.NEWEST_SDK
import org.robolectric.annotation.Config.OLDEST_SDK

private const val ACTION_INSTALL_SHORTCUT = "com.android.launcher.action.INSTALL_SHORTCUT"
private const val INSTALL_SHORTCUT_PERMISSION = "com.android.launcher.permission.INSTALL_SHORTCUT"

@RunWith(AndroidJUnit4::class)
@Config(sdk = [OLDEST_SDK, Build.VERSION_CODES.N, Build.VERSION_CODES.N_MR1, NEWEST_SDK])
@OptIn(ExperimentalCoroutinesApi::class)
class GodToolsShortcutManagerTest {
    private val app = spyk(
        ApplicationProvider.getApplicationContext<Application>()
            .also { Shadows.shadowOf(it).grantPermissions(INSTALL_SHORTCUT_PERMISSION) }
    ) {
        val rawPm = packageManager
        every { packageManager } returns spyk(rawPm) {
            val shortcutReceiver = ResolveInfo().apply {
                activityInfo = ActivityInfo().apply { permission = INSTALL_SHORTCUT_PERMISSION }
            }
            every { queryBroadcastReceivers(match { it.action == ACTION_INSTALL_SHORTCUT }, 0) }
                .returns(listOf(shortcutReceiver))
        }
    }
    private val eventBus: EventBus = mockk(relaxUnitFun = true)
    private val settings: Settings = mockk {
        every { appLanguage } returns Locale.ENGLISH
    }
    private val shortcutManagerService = app.getSystemService<ShortcutManager>()?.let {
        spyk(it) {
            every { app.getSystemService(ShortcutManager::class.java) } returns this
        }
    }
    private val testScope = TestScope()
    private val toolsRepository: ToolsRepository = mockk {
        coEvery { findTool(any()) } returns null
    }
    private val translationsRepository: TranslationsRepository = mockk {
        coEvery { findLatestTranslation(any(), any()) } returns null
    }

    private val shortcutManager by lazy {
        GodToolsShortcutManager(
            attachmentsRepository = mockk(),
            context = app,
            eventBus = eventBus,
            fs = mockk(),
            picasso = mockk(),
            settings = settings,
            toolsRepository = toolsRepository,
            translationsRepository = translationsRepository,
            coroutineScope = testScope.backgroundScope,
            ioDispatcher = UnconfinedTestDispatcher(testScope.testScheduler)
        )
    }

    @BeforeTest
    fun setup() {
        mockkStatic(InstantApps::class, ShortcutManagerCompat::class)
        every { ShortcutManagerCompat.enableShortcuts(any(), any()) } just Runs
        every { ShortcutManagerCompat.updateShortcuts(any(), any()) } returns true
        every { ShortcutManagerCompat.disableShortcuts(any(), any(), any()) } just Runs

        mockInstantApp(false)
    }

    @AfterTest
    fun cleanup() {
        unmockkStatic(InstantApps::class, ShortcutManagerCompat::class)
    }

    // region isEnabled
    @Test
    fun `isEnabled - default behavior`() {
        assertTrue(shortcutManager.isEnabled)
    }

    @Test
    fun `isEnabled - Instant App`() {
        mockInstantApp(true)

        assertFalse(shortcutManager.isEnabled)
    }
    // endregion isEnabled

    // region Events
    // region EventBus
    @Test
    fun `EventBus - register callback`() {
        assertTrue(shortcutManager.isEnabled)
        verify { eventBus.register(shortcutManager) }
    }

    @Test
    fun `EventBus - Don't register when an Instant App`() {
        mockInstantApp(true)

        assertFalse(shortcutManager.isEnabled)
        verify { eventBus wasNot Called }
    }
    // endregion EventBus

    // region onToolUsed()
    @Test
    fun `onToolUsed()`() {
        every { ShortcutManagerCompat.reportShortcutUsed(any(), any()) } just Runs

        shortcutManager.onToolUsed(ToolUsedEvent("kgp"))
        verify { ShortcutManagerCompat.reportShortcutUsed(any(), "tool|kgp") }
    }

    @Test
    fun `onToolUsed() - Instant App`() {
        mockInstantApp(true)
        every { ShortcutManagerCompat.reportShortcutUsed(any(), any()) } answers { callOriginal() }

        shortcutManager.onToolUsed(ToolUsedEvent("kgp"))
        verify(exactly = 0) { ShortcutManagerCompat.reportShortcutUsed(any(), any()) }
    }
    // endregion onToolUsed()
    // endregion Events

    // region Pending Shortcuts
    // region canPinToolShortcut(tool)
    @Test
    fun `canPinToolShortcut() - Valid - Launcher Supports Pinning`() {
        every { ShortcutManagerCompat.isRequestPinShortcutSupported(any()) } returns true

        assertTrue(shortcutManager.canPinToolShortcut(randomTool(type = Tool.Type.ARTICLE)))
        assertTrue(shortcutManager.canPinToolShortcut(randomTool(type = Tool.Type.CYOA)))
        assertTrue(shortcutManager.canPinToolShortcut(randomTool(type = Tool.Type.TRACT)))
        verify { ShortcutManagerCompat.isRequestPinShortcutSupported(any()) }
    }

    @Test
    fun `canPinToolShortcut() - Valid - Launcher Doesn't Support Pinning`() {
        every { ShortcutManagerCompat.isRequestPinShortcutSupported(any()) } returns false

        assertFalse(shortcutManager.canPinToolShortcut(randomTool(type = Tool.Type.ARTICLE)))
        assertFalse(shortcutManager.canPinToolShortcut(randomTool(type = Tool.Type.CYOA)))
        assertFalse(shortcutManager.canPinToolShortcut(randomTool(type = Tool.Type.TRACT)))
        verify { ShortcutManagerCompat.isRequestPinShortcutSupported(any()) }
    }

    @Test
    fun `canPinToolShortcut() - Invalid`() {
        assertFalse(shortcutManager.canPinToolShortcut(null))
        assertFalse(shortcutManager.canPinToolShortcut(randomTool(type = Tool.Type.LESSON)))
        assertFalse(shortcutManager.canPinToolShortcut(randomTool(type = Tool.Type.META)))
        assertFalse(shortcutManager.canPinToolShortcut(randomTool(type = Tool.Type.UNKNOWN)))
        verify(exactly = 0) { ShortcutManagerCompat.isRequestPinShortcutSupported(any()) }
    }

    @Test
    fun `canPinToolShortcut() - Instant App - GT-1977`() {
        mockInstantApp(true)
        every { ShortcutManagerCompat.isRequestPinShortcutSupported(any()) } answers { callOriginal() }

        Tool.Type.entries.forEach { assertFalse(shortcutManager.canPinToolShortcut(randomTool(type = it))) }
    }
    // endregion canPinToolShortcut(tool)

    // region getPendingToolShortcut()
    @Test
    fun `getPendingToolShortcut() - Invalid`() = testScope.runTest {
        val shortcut = shortcutManager.getPendingToolShortcut("invalid")!!
        runCurrent()
        coVerifyAll { toolsRepository.findTool("invalid") }
        assertNull(shortcut.shortcut)
    }

    @Test
    fun `getPendingToolShortcut() - Instant App`() = testScope.runTest {
        mockInstantApp(true)

        assertNull(shortcutManager.getPendingToolShortcut("tool"))
        verify {
            toolsRepository wasNot Called
        }
    }

    @Test
    fun `getPendingToolShortcut() - Valid`() = testScope.runTest {
        val tool = randomTool("tool", type = Tool.Type.TRACT, detailsBannerId = null)
        val translation = randomTranslation("tool", Locale.ENGLISH)
        coEvery { toolsRepository.findTool("tool") } returns tool
        coEvery { translationsRepository.findLatestTranslation("tool", Locale.ENGLISH) } returns translation

        val pending = shortcutManager.getPendingToolShortcut("tool")!!
        assertEquals(ShortcutId.Tool("tool"), pending.id)
        assertNull(pending.shortcut)
        runCurrent()
        assertNotNull(pending.shortcut) {
            val expectedIntent = app.createTractActivityIntent("tool", Locale.ENGLISH)
                .setAction(Intent.ACTION_VIEW)
                .putExtra(SHORTCUT_LAUNCH, true)
            assertTrue(expectedIntent equalsIntent it.intent)
        }
    }

    @Test
    fun `getPendingToolShortcut() - Valid - With Language`() = testScope.runTest {
        val tool = randomTool("tool", type = Tool.Type.TRACT, detailsBannerId = null)
        val translation = randomTranslation("tool", Locale.FRENCH)
        coEvery { toolsRepository.findTool("tool") } returns tool
        coEvery { translationsRepository.findLatestTranslation("tool", Locale.FRENCH) } returns translation

        val pending = shortcutManager.getPendingToolShortcut("tool", Locale.FRENCH)!!
        assertEquals(ShortcutId.Tool("tool", Locale.FRENCH), pending.id)
        assertNull(pending.shortcut)
        runCurrent()
        assertNotNull(pending.shortcut) {
            val expectedIntent = app.createTractActivityIntent("tool", Locale.FRENCH)
                .setAction(Intent.ACTION_VIEW)
                .putExtra(SHORTCUT_LAUNCH, true)
            assertTrue(expectedIntent equalsIntent it.intent)
        }
    }
    // endregion getPendingToolShortcut()

    @Test
    fun verifyUpdatePendingToolShortcuts() = testScope.runTest {
        val shortcut = shortcutManager.getPendingToolShortcut("kgp")!!
        runCurrent()

        // trigger update
        shortcutManager.updatePendingShortcuts()
        coVerifyAll { toolsRepository.findTool("kgp") }

        // prevent garbage collection of the shortcut during the test
        assertNotNull(shortcut) {
            "Reference the shortcut here to prevent garbage collection from collecting it during the test."
        }
    }
    // endregion Pending Shortcuts

    // region updateDynamicShortcuts()
    @Test
    fun `updateDynamicShortcuts() - Don't intercept CancelledException`() = testScope.runTest {
        coEvery { toolsRepository.getNormalTools() } throws CancellationException()

        ExceptionRaisingTree.plant().use {
            launch { shortcutManager.updateDynamicShortcuts() }.apply {
                join()
                assertTrue(isCancelled)
            }
        }
        coVerifyAll { toolsRepository.getNormalTools() }
    }

    @Test
    fun `updateDynamicShortcuts() - Instant App`() = testScope.runTest {
        mockInstantApp(true)

        // This should be a no-op
        shortcutManager.updateDynamicShortcuts()
        verify { toolsRepository wasNot Called }
    }
    // endregion updateDynamicShortcuts()

    // region updatePinnedShortcuts()
    @Test
    fun `updatePinnedShortcuts() - No Pinned Shortcuts`() = testScope.runTest {
        every { ShortcutManagerCompat.getShortcuts(any(), any()) } returns emptyList()

        shortcutManager.updatePinnedShortcuts()
        verifyAll {
            ShortcutManagerCompat.getShortcuts(any(), any())
        }
    }

    @Test
    fun `updatePinnedShortcuts() - Update Existing`() = testScope.runTest {
        val id = ShortcutId.Tool("tool")
        val tool = randomTool("tool", type = Tool.Type.TRACT, detailsBannerId = null)
        val translation = randomTranslation("tool", Locale.ENGLISH)
        val shortcut = ShortcutInfoCompat.Builder(app, id.id)
            .setShortLabel("label")
            .setIntent(Intent())
            .build()
        coEvery { toolsRepository.findTool("tool") } returns tool
        coEvery { translationsRepository.findLatestTranslation("tool", Locale.ENGLISH) } returns translation
        every { ShortcutManagerCompat.getShortcuts(any(), any()) } returns listOf(shortcut)

        shortcutManager.updatePinnedShortcuts()
        verifyAll {
            ShortcutManagerCompat.getShortcuts(any(), any())
            ShortcutManagerCompat.enableShortcuts(any(), match { it.map { it.id } == listOf(id.id) })
            ShortcutManagerCompat.updateShortcuts(any(), match { it.map { it.id } == listOf(id.id) })
        }
    }

    @Test
    fun `updatePinnedShortcuts() - Disable Invalid`() = testScope.runTest {
        val shortcut = ShortcutInfoCompat.Builder(app, "invalid")
            .setShortLabel("label")
            .setIntent(Intent())
            .build()
        every { ShortcutManagerCompat.getShortcuts(any(), any()) } returns listOf(shortcut)

        shortcutManager.updatePinnedShortcuts()
        verifyAll {
            ShortcutManagerCompat.getShortcuts(any(), any())
            ShortcutManagerCompat.disableShortcuts(any(), listOf("invalid"), any())
        }
    }

    @Test
    fun `updatePinnedShortcuts() - Instant App`() = testScope.runTest {
        mockInstantApp(true)

        shortcutManager.updatePinnedShortcuts()
        verify(exactly = 0) {
            ShortcutManagerCompat.getShortcuts(any(), any())
        }
    }
    // endregion updatePinnedShortcuts()

    // region createToolShortcut()
    @Test
    fun `createToolShortcut() - Valid - Favorite Tool Shortcut - Tract`() = testScope.runTest {
        val id = ShortcutId.Tool("tool")
        val tool = randomTool("tool", type = Tool.Type.TRACT, detailsBannerId = null)
        val translation = randomTranslation("tool", Locale.ENGLISH)
        coEvery { toolsRepository.findTool("tool") } returns tool
        coEvery { translationsRepository.findLatestTranslation("tool", Locale.ENGLISH) } returns translation

        assertNotNull(shortcutManager.createToolShortcut(id)) {
            assertEquals(translation.name, it.shortLabel.toString())
            assertEquals(translation.name, it.longLabel.toString())

            val expectedIntent = app.createTractActivityIntent("tool", Locale.ENGLISH)
                .setAction(Intent.ACTION_VIEW)
                .putExtra(SHORTCUT_LAUNCH, true)
            assertTrue(expectedIntent equalsIntent it.intent)
        }
    }

    @Test
    fun `createToolShortcut() - Valid - With Locales - Tract`() = testScope.runTest {
        val id = ShortcutId.Tool("tool", Locale.FRENCH, Locale.GERMAN)
        val tool = randomTool("tool", type = Tool.Type.TRACT, detailsBannerId = null)
        val frTranslation = randomTranslation("tool", Locale.FRENCH)
        val deTranslation = randomTranslation("tool", Locale.GERMAN)
        coEvery { toolsRepository.findTool("tool") } returns tool
        coEvery { translationsRepository.findLatestTranslation("tool", Locale.FRENCH) } returns frTranslation
        coEvery { translationsRepository.findLatestTranslation("tool", Locale.GERMAN) } returns deTranslation

        assertNotNull(shortcutManager.createToolShortcut(id)) {
            assertEquals(frTranslation.name, it.shortLabel.toString())
            assertEquals(frTranslation.name, it.longLabel.toString())

            val expectedIntent = app.createTractActivityIntent("tool", Locale.FRENCH, Locale.GERMAN)
                .setAction(Intent.ACTION_VIEW)
                .putExtra(SHORTCUT_LAUNCH, true)
            assertTrue(expectedIntent equalsIntent it.intent)
        }
    }

    @Test
    fun `createToolShortcut() - Invalid - Tool Not Found`() = testScope.runTest {
        val id = ShortcutId.Tool("tool")
        coEvery { toolsRepository.findTool("tool") } returns null

        assertNull(shortcutManager.createToolShortcut(id))
        verify { translationsRepository wasNot Called }
    }

    @Test
    fun `createToolShortcut() - Invalid - Unsupported Tool Type`() = testScope.runTest {
        val id = ShortcutId.Tool("tool")
        val tool = randomTool("tool", type = Tool.Type.LESSON)
        val translation = randomTranslation("tool", Locale.ENGLISH)
        coEvery { toolsRepository.findTool("tool") } returns tool
        coEvery { translationsRepository.findLatestTranslation("tool", Locale.ENGLISH) } returns translation

        assertNull(shortcutManager.createToolShortcut(id))
    }

    @Test
    fun `createToolShortcut() - Invalid - No Translations - Favorite Tool Shortcut`() = testScope.runTest {
        val id = ShortcutId.Tool("tool")
        val tool = randomTool("tool", type = Tool.Type.TRACT)
        coEvery { toolsRepository.findTool("tool") } returns tool

        assertNull(shortcutManager.createToolShortcut(id))
    }
    // endregion createToolShortcut()

    private fun mockInstantApp(isInstantApp: Boolean) {
        every { InstantApps.isInstantApp(any()) } returns isInstantApp

        if (shortcutManagerService != null) {
            // Instant Apps don't have access to the system ShortcutManager
            every { app.getSystemService<ShortcutManager>() } returns shortcutManagerService.takeUnless { isInstantApp }
        }
    }
}
