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
