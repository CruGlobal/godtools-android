package org.cru.godtools.shortcuts

import android.app.Application
import android.content.pm.ActivityInfo
import android.content.pm.ResolveInfo
import android.content.pm.ShortcutManager
import android.os.Build
import androidx.core.content.getSystemService
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
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
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
import org.cru.godtools.db.repository.ToolsRepository
import org.cru.godtools.model.Tool
import org.cru.godtools.model.event.ToolUsedEvent
import org.cru.godtools.model.randomTool
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
    private lateinit var app: Application
    private val eventBus: EventBus = mockk(relaxUnitFun = true)
    private lateinit var shortcutManagerService: ShortcutManager
    private val testScope = TestScope()
    private val toolsRepository: ToolsRepository = mockk {
        coEvery { findTool(any()) } returns null
    }

    private val shortcutManager by lazy {
        GodToolsShortcutManager(
            attachmentsRepository = mockk(),
            context = app,
            eventBus = eventBus,
            fs = mockk(),
            picasso = mockk(),
            settings = mockk(),
            toolsRepository = toolsRepository,
            translationsRepository = mockk(),
            coroutineScope = testScope.backgroundScope,
            ioDispatcher = UnconfinedTestDispatcher(testScope.testScheduler)
        )
    }

    @BeforeTest
    fun setup() {
        mockkStatic(InstantApps::class, ShortcutManagerCompat::class)

        val rawApp = ApplicationProvider.getApplicationContext<Application>()
        Shadows.shadowOf(rawApp).grantPermissions(INSTALL_SHORTCUT_PERMISSION)
        app = spyk(rawApp) {
            val pm = spyk(packageManager) {
                val shortcutReceiver = ResolveInfo().apply {
                    activityInfo = ActivityInfo().apply { permission = INSTALL_SHORTCUT_PERMISSION }
                }
                every { queryBroadcastReceivers(match { it.action == ACTION_INSTALL_SHORTCUT }, 0) }
                    .returns(listOf(shortcutReceiver))
            }
            every { packageManager } returns pm
            getSystemService<ShortcutManager>()?.let { sm ->
                shortcutManagerService = spyk(sm)
                every { getSystemService(ShortcutManager::class.java) } returns shortcutManagerService
            }
        }

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

    @Test
    fun verifyGetPendingToolShortcutInvalidTool() = testScope.runTest {
        val shortcut = shortcutManager.getPendingToolShortcut("invalid")!!
        runCurrent()
        coVerifyAll { toolsRepository.findTool("invalid") }
        assertNull(shortcut.shortcut)
    }

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

    // region Update Existing Shortcuts
    @Test
    @Config(sdk = [Build.VERSION_CODES.N_MR1, NEWEST_SDK])
    fun testUpdateDynamicShortcutsDoesntInterceptChildCancelledException() = testScope.runTest {
        coEvery { toolsRepository.getNormalTools() } throws CancellationException()

        ExceptionRaisingTree.plant().use {
            launch { shortcutManager.updateDynamicShortcuts(emptyMap()) }.apply {
                join()
                assertTrue(isCancelled)
            }
        }
        coVerifyAll {
            toolsRepository.getNormalTools()
            shortcutManagerService wasNot Called
        }
    }
    // endregion Update Existing Shortcuts

    // region Instant App
    @Test
    @Config(sdk = [Build.VERSION_CODES.N_MR1, NEWEST_SDK])
    fun `Instant App - updateDynamicShortcuts()`() = testScope.runTest {
        mockInstantApp(true)

        // This should be a no-op
        shortcutManager.updateDynamicShortcuts(emptyMap())
        verify { toolsRepository wasNot Called }
    }
    // endregion Instant App

    private fun mockInstantApp(isInstantApp: Boolean) {
        every { InstantApps.isInstantApp(any()) } returns isInstantApp

        if (::shortcutManagerService.isInitialized) {
            // Instant Apps don't have access to the system ShortcutManager
            every { app.getSystemService<ShortcutManager>() } returns shortcutManagerService.takeUnless { isInstantApp }
        }
    }
}
