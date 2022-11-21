package org.cru.godtools.user.activity

import app.cash.turbine.test
import io.mockk.Called
import io.mockk.coEvery
import io.mockk.coVerifySequence
import io.mockk.every
import io.mockk.excludeRecords
import io.mockk.mockk
import io.mockk.verifyAll
import java.util.Locale
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull
import kotlin.test.assertTrue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.cru.godtools.db.repository.TrainingTipsRepository
import org.cru.godtools.db.repository.UserCountersRepository
import org.cru.godtools.model.TrainingTip
import org.cru.godtools.model.UserCounter
import org.cru.godtools.shared.user.activity.UserCounterNames
import org.cru.godtools.shared.user.activity.model.Badge.BadgeType
import org.cru.godtools.sync.GodToolsSyncService

@OptIn(ExperimentalCoroutinesApi::class)
class UserActivityManagerTest {
    private companion object {
        private const val COUNTER = "counter"
    }

    private val countersFlow = MutableStateFlow(emptyList<UserCounter>())
    private val completedTipsFlow = MutableStateFlow(emptySet<TrainingTip>())

    private val syncService: GodToolsSyncService = mockk {
        coEvery { syncDirtyUserCounters() } returns true
    }
    private val tipsRepository: TrainingTipsRepository = mockk {
        every { getCompletedTipsFlow() } returns completedTipsFlow
    }
    private val userCountersRepository: UserCountersRepository = mockk(relaxUnitFun = true) {
        every { getCountersFlow() } returns countersFlow
        excludeRecords { getCountersFlow() }
    }
    private val testScope = TestScope()

    private val manager = UserActivityManager(
        { syncService },
        tipsRepository,
        userCountersRepository,
        testScope.backgroundScope
    )

    // region updateCounter()
    @Test
    fun `updateCounter()`() = testScope.runTest {
        manager.updateCounter(COUNTER, 3)
        runCurrent()
        coVerifySequence {
            userCountersRepository.updateCounter(COUNTER, 3)
            syncService.syncDirtyUserCounters()
        }
    }

    @Test
    fun `updateCounter() - Invalid Name`() = testScope.runTest {
        assertFailsWith<IllegalArgumentException> {
            manager.updateCounter("Invalid Name", 3)
        }
        runCurrent()
        verifyAll {
            userCountersRepository wasNot Called
            syncService wasNot Called
        }
    }
    // endregion updateCounter()

    // region userActivityFlow
    @Test
    fun `Property userActivityFlow`() = testScope.runTest {
        manager.userActivityFlow.test {
            assertNotNull(awaitItem()) {
                assertEquals(0, it.sessions)
                assertTrue(it.badges.none { it.isEarned })
            }

            countersFlow.value = listOf(UserCounter(UserCounterNames.SESSION).apply { apiCount = 5 })
            assertNotNull(awaitItem()) {
                assertEquals(5, it.sessions)
                assertTrue(it.badges.none { it.isEarned })
            }

            completedTipsFlow.value = List(30) { TrainingTip("tool", Locale.ENGLISH, "tipId$it", true) }.toSet()
            assertNotNull(awaitItem()) {
                assertEquals(5, it.sessions)
                assertTrue(it.badges.filter { it.type == BadgeType.TIPS_COMPLETED }.all { it.isEarned })
                assertTrue(it.badges.filterNot { it.type == BadgeType.TIPS_COMPLETED }.none { it.isEarned })
            }
        }
    }
    // endregion userActivityFlow
}
