package org.cru.godtools.article.aem.db

import io.mockk.Called
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerifyAll
import io.mockk.coVerifySequence
import io.mockk.just
import io.mockk.verify
import java.util.Locale
import kotlinx.coroutines.test.runTest
import org.cru.godtools.article.aem.model.TranslationRef
import org.cru.godtools.article.aem.model.toTranslationRefKey
import org.cru.godtools.model.randomTranslation
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

private const val TOOL = "kgp"
private val LOCALE = Locale.ENGLISH
private const val VERSION = 1

class TranslationRepositoryTest : AbstractArticleRoomDatabaseTest() {
    private val repo = object : TranslationRepository(db) {}
    private val translation = randomTranslation(
        toolCode = TOOL,
        languageCode = LOCALE,
        version = VERSION,
    )
    private val key = translation.toTranslationRefKey()!!
    private val translationRef = TranslationRef(key)

    // region isProcessed()
    @Test
    fun `isProcessed() - Missing Translation`() = runTest {
        everyFindTranslation() returns null

        assertFalse(repo.isProcessed(translation))
        coVerifyAll { translationDao.find(TOOL, LOCALE, VERSION) }
    }

    @Test
    fun `isProcessed() - Invalid Translation`() = runTest {
        val translation = randomTranslation(toolCode = null)

        assertFalse(repo.isProcessed(translation))
        verify { translationDao wasNot Called }
    }

    @Test
    fun `isProcessed() - Not Processed`() = runTest {
        everyFindTranslation() returns translationRef

        assertFalse(repo.isProcessed(translation))
        coVerifyAll { translationDao.find(TOOL, LOCALE, VERSION) }
    }

    @Test
    fun `isProcessed() - Processed`() = runTest {
        translationRef.processed = true
        everyFindTranslation() returns translationRef

        assertTrue(repo.isProcessed(translation))
        coVerifyAll { translationDao.find(TOOL, LOCALE, VERSION) }
    }

    private fun everyFindTranslation() = coEvery { translationDao.find(TOOL, LOCALE, VERSION) }
    // endregion isProcessed()

    // region removeMissingTranslations()
    @Test
    fun `removeMissingTranslations()`() = runTest {
        val translation2 = randomTranslation(
            toolCode = "invalid",
            languageCode = LOCALE,
            version = VERSION
        )
        val translationRef2 = TranslationRef(translation2.toTranslationRefKey()!!)
        coEvery { translationDao.getAll() } returns listOf(translationRef, translationRef2)
        coEvery { aemImportRepository.removeOrphanedAemImports() } just Runs

        repo.removeMissingTranslations(listOf(translation))
        coVerifySequence {
            translationDao.getAll()
            translationDao.remove(listOf(translationRef2))
            aemImportRepository.removeOrphanedAemImports()
        }
    }

    @Test
    fun `removeMissingTranslations() - Nothing to remove`() = runTest {
        coEvery { translationDao.getAll() } returns listOf(translationRef)

        repo.removeMissingTranslations(listOf(translation))
        coVerifyAll {
            translationDao.getAll()
            aemImportRepository wasNot Called
        }
    }
    // endregion removeMissingTranslations()
}
