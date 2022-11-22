package org.cru.godtools.db.repository

import java.time.temporal.ChronoUnit
import java.util.Locale
import java.util.UUID
import kotlin.random.Random
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.cru.godtools.model.Followup
import org.junit.Assert.assertEquals
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
abstract class FollowupsRepositoryIT {
    protected val testScope = TestScope()
    abstract val repository: FollowupsRepository

    @Test
    fun testCreateFollowup() = testScope.runTest {
        val followup = randomFollowup()

        repository.createFollowup(followup)
        val persisted = repository.getFollowups().single()
        assertEquals(followup.name, persisted.name)
        assertEquals(followup.email, persisted.email)
        assertEquals(followup.destination, persisted.destination)
        assertEquals(followup.languageCode, persisted.languageCode)
        assertEquals(
            followup.createTime.truncatedTo(ChronoUnit.SECONDS),
            persisted.createTime.truncatedTo(ChronoUnit.SECONDS)
        )
    }

    @Test
    fun testGetFollowups() = testScope.runTest {
        val followups = List(10) { randomFollowup() }
        followups.forEach { repository.createFollowup(it) }

        val persisted = repository.getFollowups()
        assertEquals(followups.size, persisted.size)
        assertEquals(followups.mapTo(mutableSetOf()) { it.name }, persisted.mapTo(mutableSetOf()) { it.name })
    }

    @Test
    fun testDeleteFollowup() = testScope.runTest {
        repeat(5) { repository.createFollowup(randomFollowup()) }
        assertEquals(5, repository.getFollowups().size)

        repository.getFollowups().forEach { repository.deleteFollowup(it) }
        assertEquals(0, repository.getFollowups().size)
    }

    private fun randomFollowup() = Followup(
        id = Random.nextLong(),
        destination = Random.nextLong(),
        languageCode = Locale.FRENCH,
        name = UUID.randomUUID().toString(),
        email = UUID.randomUUID().toString(),
    )
}
