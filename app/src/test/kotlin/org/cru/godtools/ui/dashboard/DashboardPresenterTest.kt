package org.cru.godtools.ui.dashboard

import android.app.Application
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.cash.turbine.ReceiveTurbine
import com.jeppeman.mockposable.mockk.everyComposable
import com.slack.circuit.foundation.NavEvent
import com.slack.circuit.runtime.CircuitContext
import com.slack.circuit.runtime.InternalCircuitApi
import com.slack.circuit.test.FakeNavigator
import com.slack.circuit.test.test
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.test.runTest
import org.ccci.gto.android.common.androidx.compose.ui.platform.AndroidUiDispatcherUtil
import org.ccci.gto.support.turbine.awaitItemMatching
import org.cru.godtools.base.ui.circuit.screen.dashboard.DashboardScreen
import org.cru.godtools.base.ui.circuit.screen.dashboard.page.HomeScreen
import org.cru.godtools.base.ui.circuit.screen.dashboard.page.LessonsScreen
import org.cru.godtools.sync.GodToolsSyncService
import org.cru.godtools.ui.dashboard.DashboardPresenter.UiEvent
import org.cru.godtools.ui.dashboard.DashboardPresenter.UiState
import org.cru.godtools.ui.dashboard.SyncTaskRegistry.Companion.syncTaskRegistry
import org.cru.godtools.ui.drawer.DrawerMenuPresenter
import org.cru.godtools.ui.drawer.DrawerMenuScreen
import org.cru.godtools.ui.tooldetails.ToolDetailsScreen
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(application = Application::class)
@Suppress("DeferredResultUnused")
@OptIn(InternalCircuitApi::class)
class DashboardPresenterTest {
    private val screen = DashboardScreen(HomeScreen)
    private val syncLock = Mutex(true)
    private val circuitContext = CircuitContext(null)

    private val drawerMenuPresenter: DrawerMenuPresenter = mockk {
        everyComposable { present() } returns DrawerMenuScreen.State()
    }
    private val navigator = FakeNavigator(screen)
    private val syncService: GodToolsSyncService = mockk {
        coEvery { syncFollowupsAsync() } returns CompletableDeferred(true)
        coEvery { syncToolSharesAsync() } returns CompletableDeferred(true)

        coEvery { syncFavoriteTools(any()) } coAnswers { syncLock.withLock { true } }
        coEvery { syncTools(any()) } coAnswers { syncLock.withLock { true } }
    }

    private val presenter = DashboardPresenter(
        drawerMenuPresenter = drawerMenuPresenter,
        syncService = syncService,
        circuitContext = circuitContext,
        navigator = navigator,
        screen = screen,
    )

    @AfterTest
    fun cleanup() {
        AndroidUiDispatcherUtil.runScheduledDispatches()
        navigator.assertGoToIsEmpty()
        navigator.assertPopIsEmpty()
    }

    // region State.initialPage
    @Test
    fun `State - initialPage`() = runTest {
        val presenter = DashboardPresenter(
            drawerMenuPresenter = drawerMenuPresenter,
            syncService = syncService,
            circuitContext = circuitContext,
            navigator = navigator,
            screen = DashboardScreen(LessonsScreen),
        )

        presenter.test {
            assertEquals(LessonsScreen, awaitInitialState().initialPage)
        }
    }
    // endregion State.initialPage

    // region State.drawerState
    @Test
    fun `State - drawerState`() = runTest {
        val drawerState = DrawerMenuScreen.State(isLoggedIn = true)
        everyComposable { drawerMenuPresenter.present() } returns drawerState

        presenter.test {
            assertEquals(drawerState, awaitInitialState().drawerState)
        }
    }
    // endregion State.drawerState

    // region State.isSyncing
    @Test
    fun `State - isSyncing - initial sync`() = runTest {
        presenter.test {
            assertTrue(awaitInitialState().isSyncing)
            coVerify { syncService.syncFollowupsAsync() }
            coVerify { syncService.syncToolSharesAsync() }
            coVerify { syncService.syncFavoriteTools(false) }
            coVerify { syncService.syncTools(false) }

            syncLock.unlock()
            assertFalse(awaitItem().isSyncing)
        }
    }
    // endregion State.isSyncing

    // region UiEvent.NestedNavEvent
    @Test
    fun `UiEvent - NestedNavEvent - GoTo`() = runTest {
        presenter.test {
            val state = awaitInitialState()
            val screen = ToolDetailsScreen("kgp")
            state.eventSink(UiEvent.NestedNavEvent(NavEvent.GoTo(screen)))
            assertEquals(screen, navigator.awaitNextScreen())
        }
    }
    // endregion UiEvent.NestedNavEvent

    // region UiEvent.TriggerSync
    @Test
    fun `UiEvent - TriggerSync`() = runTest {
        presenter.test {
            assertTrue(awaitInitialState().isSyncing)
            syncLock.unlock()
            val state = awaitItem()
            assertFalse(state.isSyncing)
            syncLock.lock()

            state.eventSink(UiEvent.TriggerSync)
            assertTrue(awaitItem().isSyncing)
            coVerify { syncService.syncFavoriteTools(true) }
            coVerify { syncService.syncTools(true) }
        }
    }
    // endregion UiEvent.TriggerSync

    // region SideEffect - SyncTaskRegistry
    @Test
    fun `SideEffect - SyncTaskRegistry - set on CircuitContext while presenter is active`() = runTest {
        assertNull(circuitContext.syncTaskRegistry)
        presenter.test {
            awaitInitialState()
            assertNotNull(circuitContext.syncTaskRegistry)
        }
        assertNull(circuitContext.syncTaskRegistry)
    }
    // endregion SideEffect - SyncTaskRegistry

    private suspend fun ReceiveTurbine<UiState>.awaitInitialState() = awaitItemMatching { it.isSyncing }
}
