package org.cru.godtools.shortcuts

import android.app.Application
import android.content.pm.ActivityInfo
import android.content.pm.ResolveInfo
import android.content.pm.ShortcutManager
import android.os.Build
import androidx.core.content.getSystemService
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.android.gms.common.wrappers.InstantApps
import io.mockk.Called
import io.mockk.coEvery
import io.mockk.coVerifyAll
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.spyk
import io.mockk.unmockkStatic
import io.mockk.verify
import java.util.EnumSet
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
import org.cru.godtools.model.randomTool
import org.greenrobot.eventbus.EventBus
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
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

    @Before
    fun setup() {
        mockkStatic(InstantApps::class)
        every { InstantApps.isInstantApp(any()) } returns false

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
    }

    @After
    fun cleanup() {
        unmockkStatic(InstantApps::class)
    }

    // region Pending Shortcuts
    // region canPinShortcut(tool)
    @Test
    fun verifyCanPinToolShortcut() {
        val supportedTypes = EnumSet.of(Tool.Type.ARTICLE, Tool.Type.CYOA, Tool.Type.TRACT)
        Tool.Type.values().forEach {
            assertEquals(supportedTypes.contains(it), shortcutManager.canPinToolShortcut(randomTool(type = it)))
        }
    }

    @Test
    fun verifyCanPinToolShortcutNull() {
        assertFalse(shortcutManager.canPinToolShortcut(null))
    }
    // endregion canPinShortcut(tool)

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
        assertNotNull(
            "Reference the shortcut here to prevent garbage collection from collecting it during the test.",
            shortcut
        )
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
    fun `Instant App - Don't register with EventBus`() {
        every { InstantApps.isInstantApp(any()) } returns true

        assertFalse(shortcutManager.isEnabled)
        verify { eventBus wasNot Called }
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.N_MR1, NEWEST_SDK])
    fun `Instant App - canPinToolShortcut() - GT-1977`() {
        every { InstantApps.isInstantApp(any()) } returns true
        // Instant Apps don't have access to the system ShortcutManager
        every { app.getSystemService<ShortcutManager>() } returns null

        assertFalse(shortcutManager.canPinToolShortcut(Tool("kgp", type = Tool.Type.TRACT)))
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.N_MR1, NEWEST_SDK])
    fun `Instant App - updateDynamicShortcuts()`() = testScope.runTest {
        every { InstantApps.isInstantApp(any()) } returns true
        // Instant Apps don't have access to the system ShortcutManager
        every { app.getSystemService<ShortcutManager>() } returns null

        // This should be a no-op
        shortcutManager.updateDynamicShortcuts(emptyMap())
        verify { toolsRepository wasNot Called }
    }
    // endregion Instant App
}
