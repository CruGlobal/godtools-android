package org.cru.godtools.db.repository

import app.cash.turbine.test
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.cru.godtools.model.UserCounter
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.containsInAnyOrder
import org.hamcrest.Matchers.empty
import org.hamcrest.Matchers.`is`

private const val COUNTER = "counter"
private const val COUNTER2 = "counter2"

abstract class UserCountersRepositoryIT {
    internal abstract val repository: UserCountersRepository

    @Test
    fun `findCounterFlow()`() = runTest {
        repository.findCounterFlow(COUNTER).test {
            assertNull(awaitItem())

            repository.updateCounter(COUNTER2, 3)
            assertNull(awaitItem())

            repository.updateCounter(COUNTER, 2)
            assertEquals(2, assertNotNull(awaitItem()).delta)

            repository.updateCounter(COUNTER, 3)
            assertEquals(5, assertNotNull(awaitItem()).delta)

            repository.updateCounter(COUNTER, -4)
            assertEquals(1, assertNotNull(awaitItem()).delta)
        }
    }

    @Test
    fun `updateCounter()`() = runTest {
        assertNull(findCounter(COUNTER))
        repository.updateCounter(COUNTER, 2)
        assertEquals(2, findCounter(COUNTER)!!.delta)
        repository.updateCounter(COUNTER, 3)
        assertEquals(5, findCounter(COUNTER)!!.delta)
        repository.updateCounter(COUNTER, -4)
        assertEquals(1, findCounter(COUNTER)!!.delta)
    }

    @Test
    fun `getDirtyCounters()`() = runTest {
        assertThat(repository.getDirtyCounters(), `is`(empty()))
        repository.updateCounter(COUNTER, 2)
        assertThat(repository.getDirtyCounters().map { it.name }, containsInAnyOrder(COUNTER))
        repository.updateCounter(COUNTER2, 2)
        assertThat(repository.getDirtyCounters().map { it.name }, containsInAnyOrder(COUNTER, COUNTER2))
        repository.updateCounter(COUNTER, -2)
        assertThat(repository.getDirtyCounters().map { it.name }, containsInAnyOrder(COUNTER2))
        repository.updateCounter(COUNTER2, -2)
        assertThat(repository.getDirtyCounters(), `is`(empty()))
    }

    @Test
    fun `storeCountersFromSync()`() = runTest {
        val counter1 = UserCounter(COUNTER, apiCount = 2, apiDecayedCount = 1.0)
        val counter2 = UserCounter(COUNTER2, apiCount = 4, apiDecayedCount = 3.0)
        repository.updateCounter(COUNTER2, 2)
        repository.storeCountersFromSync(listOf(counter1, counter2))
        with(findCounter(COUNTER)!!) {
            assertEquals(counter1.apiCount, apiCount)
            assertEquals(counter1.apiDecayedCount, apiDecayedCount, 0.00001)
            assertEquals(0, delta)
        }
        with(findCounter(COUNTER2)!!) {
            assertEquals(counter2.apiCount, apiCount)
            assertEquals(counter2.apiDecayedCount, apiDecayedCount, 0.00001)
            assertEquals(2, delta)
        }
    }

    private suspend fun findCounter(id: String) = repository.findCounterFlow(id).first()
}
