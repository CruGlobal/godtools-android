package org.cru.godtools.db.repository

import java.util.Locale
import java.util.UUID
import kotlin.random.Random
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
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
    fun `findLatestTranslation() - Published only`() = testScope.runTest {
        repository.storeInitialTranslations(
            listOf(
                createTranslation(
                    toolCode = TOOL,
                    languageCode = Locale.ENGLISH,
                    version = 1,
                    isPublished = true,
                ),
                createTranslation(
                    toolCode = TOOL,
                    languageCode = Locale.ENGLISH,
                    version = 2,
                    isPublished = false,
                ),
            )
        )

        assertNotNull(repository.findLatestTranslation(TOOL, Locale.ENGLISH)) {
            assertEquals(TOOL, it.toolCode)
            assertEquals(Locale.ENGLISH, it.languageCode)
            assertEquals(1, it.version)
        }
    }

    @Test
    fun `findLatestTranslation(isDownloaded=true)`() = testScope.runTest {
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

        assertNotNull(repository.findLatestTranslation(TOOL, Locale.ENGLISH, isDownloaded = true)) {
            assertEquals(TOOL, it.toolCode)
            assertEquals(Locale.ENGLISH, it.languageCode)
            assertEquals(1, it.version)
        }
    }
    // endregion findLatestTranslation()

    // region getTranslationsFor()
    @Test
    fun `getTranslationsFor()`() = testScope.runTest {
        val translations = List(10) { createTranslation() }
        repository.storeInitialTranslations(translations)

        assertEquals(translations.map { it.id }.toSet(), repository.getTranslationsFor().map { it.id }.toSet())
    }

    @Test
    fun `getTranslationsFor(tools=list())`() = testScope.runTest {
        val tool1 = createTranslation(toolCode = "tool1")
        val tool2 = createTranslation(toolCode = "tool2")
        val tool3 = createTranslation(toolCode = "tool3")
        repository.storeInitialTranslations(listOf(tool1, tool2, tool3))

        assertEquals(
            setOf(tool1.id, tool2.id),
            repository.getTranslationsFor(tools = listOf("tool1", "tool2")).map { it.id }.toSet()
        )
    }

    @Test
    fun `getTranslationsFor(languages=list())`() = testScope.runTest {
        val english = createTranslation(languageCode = Locale.ENGLISH)
        val french = createTranslation(languageCode = Locale.FRENCH)
        val german = createTranslation(languageCode = Locale.GERMAN)
        repository.storeInitialTranslations(listOf(english, french, german))

        assertEquals(
            setOf(english.id, french.id),
            repository.getTranslationsFor(languages = setOf(Locale.ENGLISH, Locale.FRENCH)).map { it.id }.toSet()
        )
    }

    @Test
    fun `getTranslationsFor(tools=list(), languages=list())`() = testScope.runTest {
        val tool1English = createTranslation(toolCode = "tool1", languageCode = Locale.ENGLISH)
        val tool1French = createTranslation(toolCode = "tool1", languageCode = Locale.FRENCH)
        val tool2English = createTranslation(toolCode = "tool2", languageCode = Locale.ENGLISH)
        val tool2French = createTranslation(toolCode = "tool2", languageCode = Locale.FRENCH)
        repository.storeInitialTranslations(listOf(tool1English, tool1French, tool2English, tool2French))

        assertEquals(
            setOf(tool1English.id),
            repository.getTranslationsFor(tools = setOf("tool1"), languages = setOf(Locale.ENGLISH))
                .map { it.id }.toSet()
        )
    }
    // endregion getTranslationsFor()

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

    private fun createTranslation(
        id: Long = Random.nextLong(),
        toolCode: String = UUID.randomUUID().toString(),
        languageCode: Locale = Locale.ENGLISH,
        version: Int = Random.nextInt(),
        isPublished: Boolean = true,
        isDownloaded: Boolean = true,
    ) = Translation().also {
        it.id = id
        it.toolCode = toolCode
        it.languageCode = languageCode
        it.version = version
        it.isPublished = isPublished
        it.isDownloaded = isDownloaded
    }
}
