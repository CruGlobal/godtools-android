package org.cru.godtools.shortcuts

import android.app.Application
import android.content.pm.ActivityInfo
import android.content.pm.ResolveInfo
import android.content.pm.ShortcutManager
import android.os.Build
import androidx.core.content.getSystemService
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import io.mockk.mockk
import java.util.EnumSet
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.job
import kotlinx.coroutines.joinAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.ccci.gto.android.common.db.Query
import org.ccci.gto.android.common.db.find
import org.ccci.gto.android.common.testing.timber.ExceptionRaisingTree
import org.cru.godtools.model.Tool
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.keynote.godtools.android.db.GodToolsDao
import org.mockito.kotlin.any
import org.mockito.kotlin.argThat
import org.mockito.kotlin.clearInvocations
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.doThrow
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.spy
import org.mockito.kotlin.stub
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.whenever
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
    private lateinit var shortcutManagerService: ShortcutManager

    private lateinit var dao: GodToolsDao
    @Deprecated("Transition tests to use runTest closure")
    private val coroutineScope = TestScope()

    private val shortcutManager by lazy { coroutineScope.createShortcutManager() }
    private fun TestScope.createShortcutManager() =
        GodToolsShortcutManager(app, dao, mockk(relaxUnitFun = true), mockk(), mockk(), mockk(), mockk(), this)

    @Before
    fun setup() {
        val rawApp = ApplicationProvider.getApplicationContext<Application>()
        Shadows.shadowOf(rawApp).grantPermissions(INSTALL_SHORTCUT_PERMISSION)
        app = spy(rawApp) {
            val pm = spy(it.packageManager) { pm ->
                val shortcutReceiver = ResolveInfo().apply {
                    activityInfo = ActivityInfo().apply { permission = INSTALL_SHORTCUT_PERMISSION }
                }
                doReturn(listOf(shortcutReceiver))
                    .whenever(pm).queryBroadcastReceivers(argThat { action == ACTION_INSTALL_SHORTCUT }, eq(0))
            }
            on { packageManager } doReturn pm
            it.getSystemService<ShortcutManager>()?.let { sm ->
                shortcutManagerService = spy(sm)
                on { getSystemService(ShortcutManager::class.java) } doReturn shortcutManagerService
            }
        }
        dao = mock()
    }

    // region Pending Shortcuts
    // region canPinShortcut(tool)
    @Test
    fun verifyCanPinToolShortcut() {
        val supportedTypes = EnumSet.of(Tool.Type.ARTICLE, Tool.Type.CYOA, Tool.Type.TRACT)
        Tool.Type.values().forEach {
            assertEquals(supportedTypes.contains(it), shortcutManager.canPinToolShortcut(Tool().apply { type = it }))
        }
    }

    @Test
    fun verifyCanPinToolShortcutNull() {
        assertFalse(shortcutManager.canPinToolShortcut(null))
    }
    // endregion canPinShortcut(tool)

    @Test
    fun verifyGetPendingToolShortcutInvalidTool() = runTest {
        val shortcutManager = createShortcutManager()
        val shortcut = shortcutManager.getPendingToolShortcut("invalid")!!
        joinLaunchedJobs()
        verify(dao).find<Tool>("invalid")
        assertNull(shortcut.shortcut)
    }

    @Test
    fun verifyUpdatePendingToolShortcuts() = runTest {
        val shortcutManager = createShortcutManager()

        val shortcut = shortcutManager.getPendingToolShortcut("kgp")!!
        joinLaunchedJobs()
        clearInvocations(dao)

        // trigger update
        shortcutManager.updatePendingShortcuts()
        verify(dao).find<Tool>("kgp")
        verifyNoMoreInteractions(dao)

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
    fun testUpdateDynamicShortcutsDoesntInterceptChildCancelledException() = runTest {
        dao.stub { on { get(any<Query<Tool>>()) } doThrow CancellationException() }
        val shortcutManager = createShortcutManager()

        ExceptionRaisingTree.plant().use {
            launch { shortcutManager.updateDynamicShortcuts(emptyMap()) }.apply {
                join()
                assertTrue(isCancelled)
            }
        }
        verify(dao).get(any<Query<Tool>>())
        verifyNoInteractions(shortcutManagerService)
    }
    // endregion Update Existing Shortcuts

    // region Instant App
    @Test
    @Config(sdk = [Build.VERSION_CODES.N_MR1, NEWEST_SDK])
    fun verifyUpdateDynamicShortcutsOnInstantAppIsANoop() = runTest {
        // Instant Apps don't have access to the system ShortcutManager
        whenever(app.getSystemService<ShortcutManager>()).thenReturn(null)
        val shortcutManager = createShortcutManager()

        shortcutManager.updateDynamicShortcuts(emptyMap())
        verifyNoInteractions(dao)
    }
    // endregion Instant App

    private suspend fun TestScope.joinLaunchedJobs() = coroutineContext.job.children.toList().joinAll()
}
