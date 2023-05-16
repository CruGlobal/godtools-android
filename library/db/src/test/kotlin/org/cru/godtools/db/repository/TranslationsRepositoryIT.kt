package org.cru.godtools.db.repository

import app.cash.turbine.test
import java.util.Locale
import java.util.UUID
import kotlin.random.Random
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
import org.cru.godtools.model.Translation

private const val TOOL = "tool"

@OptIn(ExperimentalCoroutinesApi::class)
abstract class TranslationsRepositoryIT {
    protected val testScope = TestScope()
    abstract val repository: TranslationsRepository

    // region findLatestTranslation()
    @Test
    fun `findLatestTranslation()`() = testScope.runTest {
        repository.storeInitialTranslations(
            listOf(
                createTranslation(
                    toolCode = TOOL,
                    languageCode = Locale.ENGLISH,
                    version = 1,
                ),
                createTranslation(
                    toolCode = TOOL,
                    languageCode = Locale.ENGLISH,
                    version = 2,
                ),
                createTranslation(
                    toolCode = TOOL,
                    languageCode = Locale.GERMAN,
                    version = 3,
                ),
                createTranslation(
                    toolCode = "${TOOL}other",
                    languageCode = Locale.ENGLISH,
                    version = 3,
                ),
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
                createTranslation(
                    toolCode = TOOL,
                    languageCode = Locale.ENGLISH,
                    version = 1,
                    isDownloaded = true,
                ),
                createTranslation(
                    toolCode = TOOL,
                    languageCode = Locale.ENGLISH,
                    version = 2,
                    isDownloaded = false,
                ),
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
        val translations = List(10) { createTranslation() }
        repository.storeInitialTranslations(translations)

        assertEquals(translations.map { it.id }.toSet(), repository.getTranslations().map { it.id }.toSet())
    }
    // endregion getTranslations()

    // region getTranslationsForLanguages()
    @Test
    fun `getTranslationsForLanguages()`() = testScope.runTest {
        val english = createTranslation(languageCode = Locale.ENGLISH)
        val french = createTranslation(languageCode = Locale.FRENCH)
        val german = createTranslation(languageCode = Locale.GERMAN)
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
        val tool1 = Translation("tool1")
        val tool2 = Translation("tool2")
        repository.storeInitialTranslations(listOf(tool1, tool2))

        assertEquals(
            setOf(tool1.id),
            repository.getTranslationsForToolBlocking("tool1").map { it.id }.toSet()
        )
    }
    // endregion getTranslationsForToolBlocking()

    // region getTranslationsFlow()
    @Test
    fun `getTranslationsFlow()`() = testScope.runTest {
        repository.getTranslationsFlow().test {
            assertTrue(awaitItem().isEmpty())

            val translations1 = List(2) { createTranslation() }
            repository.storeInitialTranslations(translations1)
            assertNotNull(awaitItem()) {
                assertEquals(2, it.size)
                assertEquals(translations1.map { it.id }.toSet(), it.map { it.id }.toSet())
            }

            val translations2 = List(8) { createTranslation() }
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
        val tool1 = createTranslation(toolCode = "tool")
        val tool2 = createTranslation(toolCode = "tool")

        repository.getTranslationsForToolFlow("tool").test {
            repository.storeInitialTranslations(listOf(createTranslation()))
            runCurrent()
            assertTrue(expectMostRecentItem().isEmpty())

            repository.storeInitialTranslations(List(5) { createTranslation() })
            repository.storeInitialTranslations(listOf(tool1))
            runCurrent()
            assertNotNull(expectMostRecentItem()) {
                assertEquals(1, it.size)
                assertEquals(tool1.id, it.first().id)
            }

            repository.storeInitialTranslations(List(5) { createTranslation() })
            repository.storeInitialTranslations(listOf(tool2))
            runCurrent()
            assertNotNull(expectMostRecentItem()) {
                assertEquals(2, it.size)
                assertEquals(setOf(tool1.id, tool2.id), it.mapTo(mutableSetOf()) { it.id })
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

            val translation = createTranslation(isDownloaded = false)
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
        val translation = createTranslation(isDownloaded = false)
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
        val translation1 = createTranslation(
            toolCode = "tool",
            languageCode = Locale.ENGLISH,
            version = 1,
            isDownloaded = true
        )
        val translation2 = createTranslation(
            toolCode = "tool",
            languageCode = Locale.ENGLISH,
            version = 2,
            isDownloaded = true
        )
        repository.storeInitialTranslations(listOf(translation1, translation2))

        assertTrue(repository.markStaleTranslationsAsNotDownloaded())
        assertNotNull(repository.findTranslation(translation1.id)) { assertFalse(it.isDownloaded) }
        assertNotNull(repository.findTranslation(translation2.id)) { assertTrue(it.isDownloaded) }
    }

    @Test
    fun `markStaleTranslationsAsNotDownloaded() - no changes`() = testScope.runTest {
        val translation1 = createTranslation(isDownloaded = true)
        val translation2 = createTranslation(isDownloaded = true)
        repository.storeInitialTranslations(listOf(translation1, translation2))

        assertFalse(repository.markStaleTranslationsAsNotDownloaded())
        assertNotNull(repository.findTranslation(translation1.id)) { assertTrue(it.isDownloaded) }
        assertNotNull(repository.findTranslation(translation2.id)) { assertTrue(it.isDownloaded) }
    }
    // endregion markStaleTranslationsAsNotDownloaded()

    // region storeInitialTranslations()
    @Test
    fun `storeInitialTranslations()`() = testScope.runTest {
        val translation = createTranslation()
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
        val broken = List(2) { createTranslation(manifestFileName = "broken.xml", isDownloaded = true) }
        val valid = List(2) { createTranslation(isDownloaded = true) }
        repository.storeInitialTranslations(broken + valid)

        repository.markBrokenManifestNotDownloaded("broken.xml")
        broken.forEach { assertNotNull(repository.findTranslation(it.id)) { assertFalse(it.isDownloaded) } }
        valid.forEach { assertNotNull(repository.findTranslation(it.id)) { assertTrue(it.isDownloaded) } }
    }
    // endregion markBrokenManifestNotDownloaded()

    @Test
    fun `storeTranslationsFromSync()`() = testScope.runTest {
        val translation = Translation("tool")

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

    private fun createTranslation(
        id: Long = Random.nextLong(),
        toolCode: String = UUID.randomUUID().toString(),
        languageCode: Locale = Locale.ENGLISH,
        version: Int = Random.nextInt(),
        manifestFileName: String? = UUID.randomUUID().toString(),
        isDownloaded: Boolean = true,
    ) = Translation().also {
        it.id = id
        it.toolCode = toolCode
        it.languageCode = languageCode
        it.version = version
        it.manifestFileName = manifestFileName
        it.isDownloaded = isDownloaded
    }
}
