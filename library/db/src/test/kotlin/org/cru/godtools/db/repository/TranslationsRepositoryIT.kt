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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.cru.godtools.model.Language
import org.cru.godtools.model.Tool
import org.cru.godtools.model.Translation
import org.cru.godtools.model.randomTranslation
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

    // region findLatestTranslationFlow()
    @Test
    fun `findLatestTranslationFlow(downloadedOnly=false)`() = testScope.runTest {
        val version1 = Translation(TOOL, Locale.ENGLISH, 1)
        val version2 = Translation(TOOL, Locale.ENGLISH, 2)
        val version3 = Translation(TOOL, Locale.ENGLISH, 3)

        repository.findLatestTranslationFlow(TOOL, Locale.ENGLISH, downloadedOnly = false).test {
            runCurrent()
            assertNull(expectMostRecentItem())

            repository.storeTranslationFromSync(version1)
            runCurrent()
            assertEquals(version1.id, assertNotNull(expectMostRecentItem()).id)

            repository.storeTranslationFromSync(version3)
            runCurrent()
            assertEquals(version3.id, assertNotNull(expectMostRecentItem()).id)

            repository.storeTranslationFromSync(version2)
            runCurrent()
            assertEquals(version3.id, assertNotNull(expectMostRecentItem()).id)
        }
    }

    @Test
    fun `findLatestTranslationFlow(downloadedOnly=true)`() = testScope.runTest {
        val version1 = Translation(TOOL, Locale.ENGLISH, 1, isDownloaded = true)
        val version2 = Translation(TOOL, Locale.ENGLISH, 2, isDownloaded = true)
        val version3 = Translation(TOOL, Locale.ENGLISH, 3)

        repository.findLatestTranslationFlow(TOOL, Locale.ENGLISH, downloadedOnly = true).test {
            runCurrent()
            assertNull(expectMostRecentItem())

            repository.storeInitialTranslations(setOf(version1))
            runCurrent()
            assertEquals(version1.id, assertNotNull(expectMostRecentItem()).id)

            repository.storeInitialTranslations(setOf(version3))
            runCurrent()
            assertEquals(version1.id, assertNotNull(expectMostRecentItem()).id)

            repository.storeInitialTranslations(setOf(version2))
            runCurrent()
            assertEquals(version2.id, assertNotNull(expectMostRecentItem()).id)

            repository.markTranslationDownloaded(version2.id, false)
            runCurrent()
            assertEquals(version1.id, assertNotNull(expectMostRecentItem()).id)

            repository.markTranslationDownloaded(version3.id, true)
            runCurrent()
            assertEquals(version3.id, assertNotNull(expectMostRecentItem()).id)
        }
    }

    @Test
    fun `findLatestTranslationFlow() - Missing tool or locale`() = testScope.runTest {
        repository.storeInitialTranslations(setOf(Translation(TOOL, Locale.ENGLISH, isDownloaded = true)))
        assertNull(repository.findLatestTranslationFlow(null, Locale.ENGLISH).first())
        assertNull(repository.findLatestTranslationFlow(TOOL, null).first())
        assertNull(repository.findLatestTranslationFlow(null, null).first())
    }
    // endregion findLatestTranslationFlow()

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

        repository.getTranslationsFlowForTool(TOOL).test {
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

    // region getTranslationsForToolsAndLocalesFlow()
    @Test
    fun `getTranslationsForToolsAndLocalesFlow()`() = testScope.runTest {
        val trans1 = Translation(TOOL, Locale.ENGLISH)
        val trans2 = Translation(TOOL, Locale.FRENCH)
        val trans3 = Translation(TOOL, Locale.GERMAN)

        repository.getTranslationsForToolsAndLocalesFlow(setOf(TOOL), setOf(Locale.ENGLISH, Locale.GERMAN)).test {
            repository.storeInitialTranslations(listOf(Translation(TOOL2)))
            runCurrent()
            assertTrue(expectMostRecentItem().isEmpty())

            repository.storeInitialTranslations(List(5) { Translation(TOOL2, Locale.FRENCH) })
            repository.storeInitialTranslations(listOf(trans1))
            runCurrent()
            assertNotNull(expectMostRecentItem()) {
                assertEquals(1, it.size)
                assertEquals(trans1.id, it.first().id)
            }

            repository.storeInitialTranslations(List(5) { Translation(TOOL2, Locale.GERMAN) })
            repository.storeInitialTranslations(listOf(trans2))
            runCurrent()
            assertNotNull(expectMostRecentItem()) {
                assertEquals(1, it.size)
                assertEquals(setOf(trans1.id), it.map { it.id }.toSet())
            }

            repository.storeInitialTranslations(List(5) { Translation(TOOL2, Locale.FRENCH) })
            repository.storeInitialTranslations(listOf(trans3))
            runCurrent()
            assertNotNull(expectMostRecentItem()) {
                assertEquals(2, it.size)
                assertEquals(setOf(trans1.id, trans3.id), it.map { it.id }.toSet())
            }
        }
    }
    // endregion getTranslationsForToolsAndLocalesFlow()

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

    // region storeTranslationsFromSync()
    @Test
    fun `storeTranslationsFromSync() - New Translation`() = testScope.runTest {
        val translation = randomTranslation(TOOL)

        repository.storeTranslationFromSync(translation)
        assertNotNull(repository.findTranslation(translation.id)) {
            assertEquals(translation.toolCode, it.toolCode)
            assertEquals(translation.languageCode, it.languageCode)
            assertEquals(translation.version, it.version)
            assertEquals(translation.name, it.name)
            assertEquals(translation.description, it.description)
            assertEquals(translation.tagline, it.tagline)
            assertEquals(translation.toolDetailsConversationStarters, it.toolDetailsConversationStarters)
            assertEquals(translation.toolDetailsOutline, it.toolDetailsOutline)
            assertEquals(translation.toolDetailsBibleReferences, it.toolDetailsBibleReferences)
            assertEquals(translation.manifestFileName, it.manifestFileName)
        }
    }

    @Test
    fun `storeTranslationsFromSync() - Update Translation`() = testScope.runTest {
        val initial = randomTranslation(TOOL)
        val updated = randomTranslation(TOOL, id = initial.id)

        repository.storeInitialTranslations(listOf(initial))
        repository.storeTranslationFromSync(updated)
        assertNotNull(repository.findTranslation(updated.id)) {
            assertEquals(updated.toolCode, it.toolCode)
            assertEquals(updated.languageCode, it.languageCode)
            assertEquals(updated.version, it.version)
            assertEquals(updated.name, it.name)
            assertEquals(updated.description, it.description)
            assertEquals(updated.tagline, it.tagline)
            assertEquals(updated.toolDetailsConversationStarters, it.toolDetailsConversationStarters)
            assertEquals(updated.toolDetailsOutline, it.toolDetailsOutline)
            assertEquals(updated.toolDetailsBibleReferences, it.toolDetailsBibleReferences)
            assertEquals(updated.manifestFileName, it.manifestFileName)
        }
    }

    @Test
    fun `storeTranslationsFromSync() - Don't update isDownloaded`() = testScope.runTest {
        val initial = Translation(TOOL)
        repository.storeTranslationFromSync(initial)
        assertFalse(assertNotNull(repository.findTranslation(initial.id)).isDownloaded)

        repository.markTranslationDownloaded(initial.id, true)
        assertTrue(assertNotNull(repository.findTranslation(initial.id)).isDownloaded)

        val updated = Translation(TOOL, id = initial.id, isDownloaded = false)
        repository.storeTranslationFromSync(updated)
        assertTrue(assertNotNull(repository.findTranslation(initial.id)).isDownloaded)
    }
    // endregion storeTranslationsFromSync()

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
