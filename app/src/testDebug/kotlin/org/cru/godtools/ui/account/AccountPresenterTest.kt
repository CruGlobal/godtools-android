package org.cru.godtools.ui.account

import android.app.Application
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.jeppeman.mockposable.mockk.everyComposable
import com.slack.circuit.test.FakeNavigator
import com.slack.circuit.test.test
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifyAll
import io.mockk.every
import io.mockk.mockk
import kotlin.test.AfterTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.ccci.gto.android.common.androidx.compose.ui.platform.AndroidUiDispatcherUtil
import org.cru.godtools.base.CONFIG_UI_GLOBAL_ACTIVITY_ENABLED
import org.cru.godtools.db.repository.GlobalActivityRepository
import org.cru.godtools.model.GlobalActivityAnalytics
import org.cru.godtools.model.User
import org.cru.godtools.model.randomUser
import org.cru.godtools.shared.user.activity.model.UserActivity
import org.cru.godtools.sync.GodToolsSyncService
import org.cru.godtools.ui.account.AccountPresenter.UiEvent
import org.cru.godtools.ui.drawer.DrawerMenuPresenter
import org.cru.godtools.ui.drawer.DrawerMenuScreen
import org.cru.godtools.user.activity.UserActivityManager
import org.cru.godtools.user.data.UserManager
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(application = Application::class)
class AccountPresenterTest {
    private val userFlow = MutableStateFlow<User?>(null)
    private val userActivityFlow = MutableStateFlow(UserActivity(emptyMap()))
    private val globalActivityFlow = MutableStateFlow(GlobalActivityAnalytics())

    private val drawerMenuPresenter: DrawerMenuPresenter = mockk {
        everyComposable { present() } returns DrawerMenuScreen.State()
    }
    private val globalActivityRepository: GlobalActivityRepository = mockk {
        every { getGlobalActivityFlow() } returns globalActivityFlow
    }
    private val navigator = FakeNavigator(AccountScreen)
    private val remoteConfig: FirebaseRemoteConfig = mockk {
        every { getBoolean(CONFIG_UI_GLOBAL_ACTIVITY_ENABLED) } returns false
    }
    private val syncService: GodToolsSyncService = mockk {
        coEvery { syncUser(any()) } returns true
        coEvery { syncUserCounters(any()) } returns true
        coEvery { syncGlobalActivity(any()) } returns true
    }
    private val userActivityManager: UserActivityManager = mockk {
        every { userActivityFlow } returns this@AccountPresenterTest.userActivityFlow
    }
    private val userManager: UserManager = mockk {
        every { userFlow } returns this@AccountPresenterTest.userFlow
    }

    private val presenter = AccountPresenter(
        remoteConfig = remoteConfig,
        globalActivityRepository = globalActivityRepository,
        syncService = syncService,
        userActivityManager = userActivityManager,
        userManager = userManager,
        drawerMenuPresenter = drawerMenuPresenter,
        navigator = navigator,
    )

    @AfterTest
    fun cleanup() = AndroidUiDispatcherUtil.runScheduledDispatches()

    @Test
    fun `State - user`() = runTest {
        presenter.test {
            assertNull(expectMostRecentItem().user)

            val user = randomUser()
            userFlow.value = user
            assertEquals(user, expectMostRecentItem().user)
        }
    }

    @Test
    fun `State - pages - global activity disabled`() = runTest {
        presenter.test {
            assertEquals(persistentListOf(AccountPage.ACTIVITY), expectMostRecentItem().pages)
        }
    }

    @Test
    fun `State - pages - global activity enabled`() = runTest {
        every { remoteConfig.getBoolean(CONFIG_UI_GLOBAL_ACTIVITY_ENABLED) } returns true

        presenter.test {
            assertEquals(
                persistentListOf(AccountPage.ACTIVITY, AccountPage.GLOBAL_ACTIVITY),
                expectMostRecentItem().pages
            )
        }
    }

    @Test
    fun `State - userActivity`() = runTest {
        val activity = UserActivity(mapOf("tool_opens" to 5))
        userActivityFlow.value = activity

        presenter.test {
            assertEquals(activity, expectMostRecentItem().userActivity)
        }
    }

    @Test
    fun `State - globalActivity`() = runTest {
        val activity = GlobalActivityAnalytics(users = 1, countries = 2, launches = 3, gospelPresentations = 4)
        globalActivityFlow.value = activity

        presenter.test {
            assertEquals(activity, expectMostRecentItem().globalActivity.activity)
        }
    }

    @Test
    fun `State - drawerState`() = runTest {
        presenter.test {
            assertFalse(expectMostRecentItem().drawerState.isLoggedIn)
        }
    }

    @Test
    fun `State - isSyncRunning - triggers a sync when launched`() = runTest {
        val sync = CompletableDeferred<Unit>()
        coEvery { syncService.syncUser(any()) } coAnswers {
            sync.await()
            true
        }

        presenter.test {
            while (!awaitItem().isSyncRunning) {
                // await the initial sync starting
            }
            sync.complete(Unit)
            while (awaitItem().isSyncRunning) {
                // await the initial sync finishing
            }
        }

        coVerifyAll {
            syncService.syncUser(false)
            syncService.syncUserCounters(false)
            syncService.syncGlobalActivity(false)
        }
    }

    @Test
    fun `Event - TriggerSync - forces a sync`() = runTest {
        val sync = CompletableDeferred<Unit>()
        coEvery { syncService.syncUser(true) } coAnswers {
            sync.await()
            true
        }

        presenter.test {
            expectMostRecentItem().eventSink(UiEvent.TriggerSync)
            while (!awaitItem().isSyncRunning) {
                // await the forced sync starting
            }
            sync.complete(Unit)
            while (awaitItem().isSyncRunning) {
                // await the forced sync finishing
            }
        }

        coVerify {
            syncService.syncUser(true)
            syncService.syncUserCounters(true)
            syncService.syncGlobalActivity(true)
        }
    }

    @Test
    fun `Event - NavigateUp`() = runTest {
        presenter.test {
            expectMostRecentItem().eventSink(UiEvent.NavigateUp)
            navigator.awaitPop()
        }
    }
}
