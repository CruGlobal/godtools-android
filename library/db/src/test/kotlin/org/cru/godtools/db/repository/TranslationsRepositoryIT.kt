package org.cru.godtools.db.repository

import app.cash.turbine.test
import java.util.Locale
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.cru.godtools.model.Language
import org.cru.godtools.model.Tool
import org.cru.godtools.model.Translation
import org.junit.Before

private const val TOOL = "tool"
private const val TOOL2 = "tool2"

@OptIn(ExperimentalCoroutinesApi::class)
abstract class TranslationsRepositoryIT {
    protected val testScope = TestScope()
    abstract val repository: TranslationsRepository
    abstract val toolsRepository: ToolsRepository
    abstract val languagesRepository: LanguagesRepository

    @Before
    fun createToolAndLanguage() {
        toolsRepository.storeToolFromSync(Tool(TOOL))
        toolsRepository.storeToolFromSync(Tool(TOOL2))
        languagesRepository.storeLanguageFromSync(Language().apply { code = Locale.ENGLISH })
        languagesRepository.storeLanguageFromSync(Language().apply { code = Locale.FRENCH })
        languagesRepository.storeLanguageFromSync(Language().apply { code = Locale.GERMAN })
    }

    // region findLatestTranslation()
    @Test
    fun `findLatestTranslation()`() = testScope.runTest {
        repository.storeInitialTranslations(
            listOf(
                Translation(TOOL, Locale.ENGLISH, 1),
                Translation(TOOL, Locale.ENGLISH, 2),
                Translation(TOOL, Locale.GERMAN, 3),
                Translation(TOOL2, Locale.ENGLISH, 3),
            )
        )

        assertNotNull(repository.findLatestTranslation(TOOL, Locale.ENGLISH)) {
            assertEquals(TOOL, it.toolCode)
            assertEquals(Locale.ENGLISH, it.languageCode)
            assertEquals(2, it.version)
        }
    }

    @Test
    fun `findLatestTranslation(downloadedOnly=true)`() = testScope.runTest {
        repository.storeInitialTranslations(
            listOf(
                Translation(TOOL, Locale.ENGLISH, 1, isDownloaded = true),
                Translation(TOOL, Locale.ENGLISH, 2, isDownloaded = false),
            )
        )

        assertNotNull(repository.findLatestTranslation(TOOL, Locale.ENGLISH, downloadedOnly = true)) {
            assertEquals(TOOL, it.toolCode)
            assertEquals(Locale.ENGLISH, it.languageCode)
            assertEquals(1, it.version)
        }
    }
    // endregion findLatestTranslation()

    // region getTranslations()
    @Test
    fun `getTranslations()`() = testScope.runTest {
        val translations = List(10) { Translation(TOOL, Locale.ENGLISH) }
        repository.storeInitialTranslations(translations)

        assertEquals(translations.map { it.id }.toSet(), repository.getTranslations().map { it.id }.toSet())
    }
    // endregion getTranslations()

    // region getTranslationsForLanguages()
    @Test
    fun `getTranslationsForLanguages()`() = testScope.runTest {
        val english = Translation(TOOL, Locale.ENGLISH)
        val french = Translation(TOOL, Locale.FRENCH)
        val german = Translation(TOOL, Locale.GERMAN)
        repository.storeInitialTranslations(listOf(english, french, german))

        assertEquals(
            setOf(english.id, french.id),
            repository.getTranslationsForLanguages(setOf(Locale.ENGLISH, Locale.FRENCH)).map { it.id }.toSet()
        )
    }
    // endregion getTranslationsForLanguages()

    // region getTranslationsForToolBlocking()
    @Test
    fun `getTranslationsForToolBlocking()`() = testScope.runTest {
        val tool1 = Translation(TOOL)
        val tool2 = Translation(TOOL2)
        repository.storeInitialTranslations(listOf(tool1, tool2))

        assertEquals(
            setOf(tool1.id),
            repository.getTranslationsForToolBlocking(TOOL).map { it.id }.toSet()
        )
    }
    // endregion getTranslationsForToolBlocking()

    // region getTranslationsFlow()
    @Test
    fun `getTranslationsFlow()`() = testScope.runTest {
        repository.getTranslationsFlow().test {
            assertTrue(awaitItem().isEmpty())

            val translations1 = List(2) { Translation(TOOL) }
            repository.storeInitialTranslations(translations1)
            assertNotNull(awaitItem()) {
                assertEquals(2, it.size)
                assertEquals(translations1.map { it.id }.toSet(), it.map { it.id }.toSet())
            }

            val translations2 = List(8) { Translation(TOOL) }
            repository.storeInitialTranslations(translations2)
            assertNotNull(awaitItem()) {
                assertEquals(10, it.size)
                assertEquals((translations1 + translations2).map { it.id }.toSet(), it.map { it.id }.toSet())
            }
        }
    }
    // endregion getTranslationsFlow()

    // region getTranslationsForToolFlow()
    @Test
    fun `getTranslationsForToolFlow()`() = testScope.runTest {
        val trans1 = Translation(TOOL)
        val trans2 = Translation(TOOL)

        repository.getTranslationsForToolFlow(TOOL).test {
            repository.storeInitialTranslations(listOf(Translation(TOOL2)))
            runCurrent()
            assertTrue(expectMostRecentItem().isEmpty())

            repository.storeInitialTranslations(List(5) { Translation(TOOL2) })
            repository.storeInitialTranslations(listOf(trans1))
            runCurrent()
            assertNotNull(expectMostRecentItem()) {
                assertEquals(1, it.size)
                assertEquals(trans1.id, it.first().id)
            }

            repository.storeInitialTranslations(List(5) { Translation(TOOL2) })
            repository.storeInitialTranslations(listOf(trans2))
            runCurrent()
            assertNotNull(expectMostRecentItem()) {
                assertEquals(2, it.size)
                assertEquals(setOf(trans1.id, trans2.id), it.map { it.id }.toSet())
            }
        }
    }
    // endregion getTranslationsForToolFlow()

    // region translationsChangeFlow()
    @Test
    fun `translationsChangeFlow()`() = testScope.runTest {
        repository.translationsChangeFlow().test {
            runCurrent()
            expectMostRecentItem()

            val translation = Translation(TOOL, Locale.ENGLISH, isDownloaded = false)
            repository.storeInitialTranslations(listOf(translation))
            runCurrent()
            expectMostRecentItem()

            repository.markTranslationDownloaded(translation.id, true)
            runCurrent()
            expectMostRecentItem()
        }
    }
    // endregion translationsChangeFlow()

    // region markTranslationDownloaded()
    @Test
    fun `markTranslationDownloaded()`() = testScope.runTest {
        val translation = Translation(TOOL, Locale.ENGLISH, isDownloaded = false)
        repository.storeInitialTranslations(listOf(translation))

        assertNotNull(repository.findTranslation(translation.id)) { assertFalse(it.isDownloaded) }
        repository.markTranslationDownloaded(translation.id, true)
        assertNotNull(repository.findTranslation(translation.id)) { assertTrue(it.isDownloaded) }
        repository.markTranslationDownloaded(translation.id, false)
        assertNotNull(repository.findTranslation(translation.id)) { assertFalse(it.isDownloaded) }
    }
    // endregion markTranslationDownloaded()

    // region markStaleTranslationsAsNotDownloaded()
    @Test
    fun `markStaleTranslationsAsNotDownloaded()`() = testScope.runTest {
        val translation1 = Translation(TOOL, Locale.ENGLISH, 1, isDownloaded = true)
        val translation2 = Translation(TOOL, Locale.ENGLISH, 2, isDownloaded = true)
        repository.storeInitialTranslations(listOf(translation1, translation2))

        assertTrue(repository.markStaleTranslationsAsNotDownloaded())
        assertNotNull(repository.findTranslation(translation1.id)) { assertFalse(it.isDownloaded) }
        assertNotNull(repository.findTranslation(translation2.id)) { assertTrue(it.isDownloaded) }
    }

    @Test
    fun `markStaleTranslationsAsNotDownloaded() - no changes`() = testScope.runTest {
        val translation1 = Translation(TOOL, Locale.ENGLISH, isDownloaded = true)
        val translation2 = Translation(TOOL2, Locale.ENGLISH, isDownloaded = true)
        repository.storeInitialTranslations(listOf(translation1, translation2))

        assertFalse(repository.markStaleTranslationsAsNotDownloaded())
        assertNotNull(repository.findTranslation(translation1.id)) { assertTrue(it.isDownloaded) }
        assertNotNull(repository.findTranslation(translation2.id)) { assertTrue(it.isDownloaded) }
    }
    // endregion markStaleTranslationsAsNotDownloaded()

    // region storeInitialTranslations()
    @Test
    fun `storeInitialTranslations()`() = testScope.runTest {
        val translation = Translation(TOOL, Locale.ENGLISH)
        repository.storeInitialTranslations(listOf(translation))

        assertNotNull(repository.findTranslation(translation.id)) {
            assertEquals(translation.toolCode, it.toolCode)
            assertEquals(translation.languageCode, it.languageCode)
            assertEquals(translation.version, it.version)
        }
    }
    // endregion storeInitialTranslations()

    // region markBrokenManifestNotDownloaded()
    @Test
    fun `markBrokenManifestNotDownloaded()`() = testScope.runTest {
        val broken = List(2) { Translation(TOOL, Locale.ENGLISH, manifestFileName = "broken.xml", isDownloaded = true) }
        val valid = List(2) { Translation(TOOL, Locale.ENGLISH, isDownloaded = true) }
        repository.storeInitialTranslations(broken + valid)

        repository.markBrokenManifestNotDownloaded("broken.xml")
        broken.forEach { assertNotNull(repository.findTranslation(it.id)) { assertFalse(it.isDownloaded) } }
        valid.forEach { assertNotNull(repository.findTranslation(it.id)) { assertTrue(it.isDownloaded) } }
    }
    // endregion markBrokenManifestNotDownloaded()

    @Test
    fun `storeTranslationsFromSync()`() = testScope.runTest {
        val translation = Translation(TOOL)

        repository.storeTranslationFromSync(translation)
        assertNotNull(repository.findTranslation(translation.id)) {
            assertEquals(translation.toolCode, it.toolCode)
            assertEquals(translation.manifestFileName, it.manifestFileName)
        }
    }

    // region deleteTranslationIfNotDownloadedBlocking()
    @Test
    fun `deleteTranslationIfNotDownloadedBlocking()`() = testScope.runTest {
        val translation = Translation("tool", Locale.ENGLISH, 1) { isDownloaded = false }
        repository.storeInitialTranslations(listOf(translation))
        assertNotNull(repository.findTranslation(translation.id))

        repository.deleteTranslationIfNotDownloadedBlocking(translation.id)
        assertNull(repository.findTranslation(translation.id))
    }

    @Test
    fun `deleteTranslationIfNotDownloadedBlocking() - Translation Downloaded`() = testScope.runTest {
        val translation = Translation("tool", Locale.ENGLISH, 1) { isDownloaded = true }
        repository.storeInitialTranslations(listOf(translation))
        assertNotNull(repository.findTranslation(translation.id))

        repository.deleteTranslationIfNotDownloadedBlocking(translation.id)
        assertNotNull(repository.findTranslation(translation.id))
    }
    // endregion deleteTranslationIfNotDownloadedBlocking()
}
