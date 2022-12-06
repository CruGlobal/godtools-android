package org.cru.godtools.db.repository

import app.cash.turbine.test
import java.util.Locale
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import org.cru.godtools.model.Language
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.containsInAnyOrder
import org.hamcrest.Matchers.empty
import org.hamcrest.Matchers.hasSize
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
abstract class LanguagesRepositoryIT {
    protected val testScope = TestScope()
    abstract val repository: LanguagesRepository

    private val language1 = Language().apply {
        id = 1
        code = Locale.ENGLISH
        name = "English"
    }
    private val language2 = Language().apply {
        id = 2
        code = Locale.FRENCH
    }

    @Test
    fun `getLanguageFlow()`() = testScope.runTest {
        repository.getLanguageFlow(Locale.ENGLISH).test {
            assertNull(awaitItem())

            repository.storeLanguageFromSync(language1)
            assertEquals(language1, awaitItem())

            val languageUpdate = Language().apply {
                id = 5
                code = Locale.ENGLISH
                name = "English 2"
            }
            repository.storeLanguageFromSync(languageUpdate)
            assertEquals(languageUpdate, awaitItem())

            repository.removeLanguagesMissingFromSync(emptyList())
            assertNull(awaitItem())
        }
    }

    @Test
    fun `getLanguages()`() = testScope.runTest {
        assertTrue(repository.getLanguages().isEmpty())

        repository.storeLanguagesFromSync(listOf(language1, language2))
        assertThat(repository.getLanguages(), allOf(hasSize(2), containsInAnyOrder(language1, language2)))
    }

    @Test
    fun `getLanguagesForLocalesFlow()`() = testScope.runTest {
        repository.getLanguagesForLocalesFlow(listOf(Locale.ENGLISH, Locale.GERMAN)).test {
            assertThat(awaitItem(), empty())

            repository.storeLanguageFromSync(language2)
            assertThat(awaitItem(), empty())

            repository.storeLanguageFromSync(language1)
            assertThat(awaitItem(), containsInAnyOrder(language1))

            repository.removeLanguagesMissingFromSync(syncedLanguages = emptyList())
            assertThat(awaitItem(), empty())
        }
    }

    // region storeInitialLanguages()
    @Test
    fun `storeInitialLanguages()`() = testScope.runTest {
        assertThat(repository.getLanguages(), empty())

        repository.storeInitialLanguages(listOf(language1, language2))
        assertThat(repository.getLanguages(), containsInAnyOrder(language1, language2))
    }

    @Test
    fun `storeInitialLanguages() - Don't overwrite existing languages`() = testScope.runTest {
        val language = Language().apply {
            id = 5
            code = Locale.ENGLISH
            name = "Newer English"
        }
        repository.storeLanguageFromSync(language)
        assertThat(repository.getLanguages(), containsInAnyOrder(language))

        repository.storeInitialLanguages(listOf(language1, language2))
        assertThat(repository.getLanguages(), containsInAnyOrder(language, language2))
    }
    // endregion storeInitialLanguages()

    // region storeLanguagesFromSync()
    @Test
    fun `storeLanguagesFromSync()`() = testScope.runTest {
        assertThat(repository.getLanguages(), empty())

        repository.storeLanguagesFromSync(listOf(language1, language2))
        assertThat(repository.getLanguages(), containsInAnyOrder(language1, language2))
    }

    @Test
    fun `storeLanguagesFromSync() - Overwrite existing languages`() = testScope.runTest {
        val language = Language().apply {
            id = 5
            code = Locale.ENGLISH
            name = "Newer English"
        }
        repository.storeLanguageFromSync(language)
        assertThat(repository.getLanguages(), containsInAnyOrder(language))

        repository.storeLanguagesFromSync(listOf(language1, language2))
        assertThat(repository.getLanguages(), containsInAnyOrder(language1, language2))
    }
    // endregion storeLanguagesFromSync()

    @Test
    fun `removeLanguagesMissingFromSync()`() = testScope.runTest {
        repository.storeLanguagesFromSync(listOf(language1, language2))
        assertThat(repository.getLanguages(), containsInAnyOrder(language1, language2))

        val syncedLanguages = listOf(language1)
        repository.removeLanguagesMissingFromSync(syncedLanguages = syncedLanguages)
        assertThat(repository.getLanguages(), containsInAnyOrder(*syncedLanguages.toTypedArray()))
    }
}
