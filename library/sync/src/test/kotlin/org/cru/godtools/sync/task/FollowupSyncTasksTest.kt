package org.cru.godtools.sync.task

import io.mockk.Called
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerifyAll
import io.mockk.just
import io.mockk.mockk
import io.mockk.slot
import java.util.Locale
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody.Companion.toResponseBody
import org.cru.godtools.api.FollowupApi
import org.cru.godtools.db.repository.FollowupsRepository
import org.cru.godtools.db.repository.LanguagesRepository
import org.cru.godtools.model.Followup
import org.cru.godtools.model.Language
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.Response

class FollowupSyncTasksTest {

    private val api: FollowupApi = mockk()
    private val followupsRepository: FollowupsRepository = mockk {
        coEvery { getFollowups() } returns emptyList()
        coEvery { deleteFollowup(any()) } just Runs
    }
    private val languagesRepository: LanguagesRepository = mockk {
        coEvery { findLanguage(any()) } returns null
    }

    private val syncTasks = FollowupSyncTasks(api, followupsRepository, languagesRepository)

    @Test
    fun `syncFollowups() - No Followups to sync`() = runTest {
        assertTrue(syncTasks.syncFollowups())
        coVerifyAll {
            followupsRepository.getFollowups()
            api wasNot Called
        }
    }

    @Test
    fun `syncFollowups()`() = runTest {
        val followups = listOf(Followup(email = "test@example.com", destination = 1, languageCode = Locale.ENGLISH))
        coEvery { followupsRepository.getFollowups() } returns followups
        coEvery { languagesRepository.findLanguage(Locale.ENGLISH) } returns Language().apply {
            code = Locale.ENGLISH
            id = 2
        }
        val followup = slot<Followup>()
        coEvery { api.subscribe(capture(followup)) } returns Response.success(null)

        assertTrue(syncTasks.syncFollowups())
        coVerifyAll {
            followupsRepository.getFollowups()
            languagesRepository.findLanguage(Locale.ENGLISH)
            api.subscribe(followup.captured)
            followupsRepository.deleteFollowup(followup.captured)
        }
        assertEquals("Make sure the languageId is set on the followup", 2L, followup.captured.languageId)
    }

    @Test
    fun `syncFollowups() - api failed`() = runTest {
        val followup = Followup(email = "test@example.com", destination = 1, languageCode = Locale.ENGLISH)
        coEvery { followupsRepository.getFollowups() } returns listOf(followup)
        coEvery { languagesRepository.findLanguage(Locale.ENGLISH) } returns Language().apply {
            code = Locale.ENGLISH
            id = 2
        }
        val submittedFollowup = slot<Followup>()
        coEvery { api.subscribe(capture(submittedFollowup)) } returns Response.error(400, "".toResponseBody())

        assertFalse(syncTasks.syncFollowups())
        coVerifyAll {
            followupsRepository.getFollowups()
            languagesRepository.findLanguage(Locale.ENGLISH)
            api.subscribe(followup)
            // We shouldn't delete the followup if the API didn't return success
            // followupsRepository.deleteFollowup(any())
        }
    }
}
