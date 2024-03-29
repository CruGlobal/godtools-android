package org.cru.godtools.db.repository

import app.cash.turbine.test
import java.util.Locale
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.cru.godtools.model.Language
import org.cru.godtools.model.Tool
import org.cru.godtools.model.randomTranslation
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.contains
import org.hamcrest.Matchers.containsInAnyOrder
import org.hamcrest.Matchers.empty
import org.hamcrest.Matchers.hasSize

@OptIn(ExperimentalCoroutinesApi::class)
abstract class LanguagesRepositoryIT {
    protected val testScope = TestScope()
    abstract val repository: LanguagesRepository
    abstract val toolsRepository: ToolsRepository
    abstract val translationsRepository: TranslationsRepository

    private val language1 = Language(Locale.ENGLISH, name = "English")
    private val language2 = Language(Locale.FRENCH)

    @Test
    fun `findLanguage()`() = testScope.runTest {
        assertNull(repository.findLanguage(Locale.ENGLISH))

        repository.storeLanguageFromSync(language1)
        assertEquals(language1, repository.findLanguage(Locale.ENGLISH))

        repository.removeLanguagesMissingFromSync(syncedLanguages = emptyList())
        assertNull(repository.findLanguage(Locale.ENGLISH))
    }

    @Test
    fun `findLanguageFlow()`() = testScope.runTest {
        repository.findLanguageFlow(Locale.ENGLISH).test {
            assertNull(awaitItem())

            repository.storeLanguageFromSync(language1)
            assertEquals(language1, awaitItem())

            val languageUpdate = Language(Locale.ENGLISH, name = "English 2")
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
        assertThat(
            repository.getLanguages().map { it.code },
            allOf(hasSize(2), containsInAnyOrder(language1.code, language2.code))
        )
    }

    @Test
    fun `getLanguagesFlow()`() = testScope.runTest {
        repository.getLanguagesFlow().test {
            assertThat(awaitItem(), empty())

            repository.storeLanguageFromSync(language1)
            assertThat(awaitItem(), contains(language1))

            repository.storeLanguageFromSync(language2)
            assertThat(awaitItem(), containsInAnyOrder(language1, language2))
        }
    }

    @Test
    fun `getLanguagesFlowForLocales()`() = testScope.runTest {
        repository.getLanguagesFlowForLocales(listOf(Locale.ENGLISH, Locale.GERMAN)).test {
            assertThat(awaitItem(), empty())

            repository.storeLanguageFromSync(language2)
            assertThat(awaitItem(), empty())

            repository.storeLanguageFromSync(language1)
            assertThat(awaitItem().map { it.code }, containsInAnyOrder(language1.code))

            repository.removeLanguagesMissingFromSync(syncedLanguages = emptyList())
            assertThat(awaitItem(), empty())
        }
    }

    // region getLanguagesFlowForToolCategory()
    @Test
    fun `getLanguagesFlowForToolCategory()`() = testScope.runTest {
        val english = Language(Locale.ENGLISH)
        val french = Language(Locale.FRENCH)
        val german = Language(Locale.GERMAN)

        repository.getLanguagesFlowForToolCategory("cat1").test {
            assertThat(awaitItem(), empty())

            toolsRepository.storeInitialTools(
                listOf(
                    Tool("tool1", category = "cat1"),
                    Tool("tool2", category = "cat2")
                )
            )
            repository.storeInitialLanguages(listOf(english, french, german))
            translationsRepository.storeInitialTranslations(
                listOf(
                    randomTranslation("tool1", Locale.ENGLISH),
                    randomTranslation("tool2", Locale.FRENCH)
                )
            )
            runCurrent()
            assertThat(expectMostRecentItem(), containsInAnyOrder(english))
        }
    }

    @Test
    fun `getLanguagesFlowForToolCategory() - No Duplicate Languages`() = testScope.runTest {
        val english = Language(Locale.ENGLISH)
        toolsRepository.storeInitialTools(
            listOf(
                Tool("tool1", category = "cat1"),
                Tool("tool2", category = "cat1")
            )
        )
        repository.storeInitialLanguages(listOf(english))
        translationsRepository.storeInitialTranslations(
            listOf(
                randomTranslation("tool1", Locale.ENGLISH),
                randomTranslation("tool2", Locale.ENGLISH)
            )
        )

        repository.getLanguagesFlowForToolCategory("cat1").test {
            runCurrent()
            assertThat(expectMostRecentItem(), contains(english))
        }
    }
    // region getLanguagesFlowForToolCategory()

    // region getPinnedLanguagesFlow()
    @Test
    fun `getPinnedLanguagesFlow()`() = testScope.runTest {
        repository.getPinnedLanguagesFlow().test {
            assertTrue(awaitItem().isEmpty())

            repository.storeLanguageFromSync(Language(Locale.ENGLISH))
            assertTrue(awaitItem().isEmpty())

            repository.storeLanguageFromSync(Language(Locale.FRENCH))
            assertTrue(awaitItem().isEmpty())

            repository.pinLanguage(Locale.FRENCH)
            assertThat(awaitItem().map { it.code }, containsInAnyOrder(Locale.FRENCH))
        }
    }
    // endregion getPinnedLanguagesFlow()

    // region pinLanguage()
    @Test
    fun `pinLanguage()`() = testScope.runTest {
        repository.storeInitialLanguages(
            listOf(
                Language(Locale.ENGLISH, isAdded = false),
                Language(Locale.FRENCH, isAdded = false),
            )
        )
        assertFalse(repository.findLanguage(Locale.ENGLISH)!!.isAdded)
        assertFalse(repository.findLanguage(Locale.FRENCH)!!.isAdded)

        repository.pinLanguage(Locale.ENGLISH)
        assertTrue(repository.findLanguage(Locale.ENGLISH)!!.isAdded)
        assertFalse(repository.findLanguage(Locale.FRENCH)!!.isAdded)
    }
    // endregion pinLanguage()

    // region unpinLanguage()
    @Test
    fun `unpinLanguage()`() = testScope.runTest {
        repository.storeInitialLanguages(
            listOf(
                Language(Locale.ENGLISH, isAdded = true),
                Language(Locale.FRENCH, isAdded = true),
            )
        )
        assertTrue(repository.findLanguage(Locale.ENGLISH)!!.isAdded)
        assertTrue(repository.findLanguage(Locale.FRENCH)!!.isAdded)

        repository.unpinLanguage(Locale.ENGLISH)
        assertFalse(repository.findLanguage(Locale.ENGLISH)!!.isAdded)
        assertTrue(repository.findLanguage(Locale.FRENCH)!!.isAdded)
    }
    // endregion unpinLanguage()

    // region storeInitialLanguages()
    @Test
    fun `storeInitialLanguages()`() = testScope.runTest {
        assertThat(repository.getLanguages(), empty())

        repository.storeInitialLanguages(listOf(language1, language2))
        assertThat(repository.getLanguages().map { it.code }, containsInAnyOrder(language1.code, language2.code))
    }

    @Test
    fun `storeInitialLanguages() - Don't overwrite existing languages`() = testScope.runTest {
        val language = Language(Locale.ENGLISH, name = "Newer English")
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
    fun `storeLanguagesFromSync() - Update existing languages`() = testScope.runTest {
        val language = Language(Locale.ENGLISH, name = "Newer English")
        repository.storeLanguageFromSync(language)
        assertThat(repository.getLanguages(), containsInAnyOrder(language))

        repository.storeLanguagesFromSync(listOf(language1, language2))
        assertThat(repository.getLanguages(), containsInAnyOrder(language1, language2))
    }

    @Test
    fun `storeLanguagesFromSync() - Preserve isAdded flag`() = testScope.runTest {
        repository.storeInitialLanguages(listOf(Language(Locale.ENGLISH)))
        repository.pinLanguage(Locale.ENGLISH)

        repository.storeLanguageFromSync(Language(Locale.ENGLISH))
        assertTrue(repository.findLanguage(Locale.ENGLISH)!!.isAdded)
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
