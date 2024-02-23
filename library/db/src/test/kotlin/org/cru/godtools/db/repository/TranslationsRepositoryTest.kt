package org.cru.godtools.db.repository

import androidx.test.ext.junit.runners.AndroidJUnit4
import app.cash.molecule.RecompositionMode
import app.cash.molecule.moleculeFlow
import app.cash.turbine.test
import io.mockk.every
import io.mockk.mockk
import io.mockk.verifyAll
import java.util.Locale
import java.util.UUID
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.cru.godtools.model.Translation
import org.cru.godtools.model.randomTranslation
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TranslationsRepositoryTest {
    private val latestTranslationFlow = MutableStateFlow<Translation?>(null)

    private val repository: TranslationsRepository = mockk {
        every { findLatestTranslationFlow(any(), any(), any()) } returns latestTranslationFlow
    }

    @Test
    fun `produceLatestTranslationState()`() = runTest {
        val code = UUID.randomUUID().toString()
        val locale: Locale = mockk()
        val downloadedOnly = Random.nextBoolean()

        moleculeFlow(RecompositionMode.Immediate) {
            repository.produceLatestTranslationState(code, locale, downloadedOnly).value
        }.test {
            assertNull(awaitItem())

            val translation = randomTranslation()
            latestTranslationFlow.value = translation
            assertEquals(translation, expectMostRecentItem())
        }

        verifyAll {
            repository.findLatestTranslationFlow(code, locale, downloadedOnly)
        }
    }
}
