package org.cru.godtools.db.repository

import app.cash.turbine.test
import kotlin.random.Random
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.cru.godtools.model.GlobalActivityAnalytics
import org.junit.Assert.assertEquals
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
abstract class GlobalActivityRepositoryIT {
    internal abstract val repository: GlobalActivityRepository

    @Test
    fun `getGlobalActivityFlow() - Initial value`() = runTest {
        repository.getGlobalActivityFlow().test {
            // initial value
            with(awaitItem()) {
                assertEquals(0, users)
                assertEquals(0, countries)
                assertEquals(0, launches)
                assertEquals(0, gospelPresentations)
            }

            // update triggers new emission
            repository.updateGlobalActivity(
                GlobalActivityAnalytics().apply {
                    users = 1
                    countries = 2
                    launches = 3
                    gospelPresentations = 4
                }
            )
            with(awaitItem()) {
                assertEquals(1, users)
                assertEquals(2, countries)
                assertEquals(3, launches)
                assertEquals(4, gospelPresentations)
            }
        }
    }

    @Test
    fun verifyUpdateGlobalActivity() = runTest {
        val orig = GlobalActivityAnalytics().apply {
            users = Random.nextInt()
            countries = Random.nextInt()
            launches = Random.nextInt()
            gospelPresentations = Random.nextInt()
        }
        repository.updateGlobalActivity(orig)
        val activity = repository.getGlobalActivityFlow().first()
        assertEquals(orig.users, activity.users)
        assertEquals(orig.countries, activity.countries)
        assertEquals(orig.launches, activity.launches)
        assertEquals(orig.gospelPresentations, activity.gospelPresentations)
    }
}
