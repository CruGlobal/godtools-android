package org.cru.godtools.article.aem.db

import java.util.Locale
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.cru.godtools.article.aem.model.TranslationRef
import org.cru.godtools.article.aem.model.toTranslationRefKey
import org.cru.godtools.model.Translation
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.kotlin.verify
import org.mockito.kotlin.verifyNoInteractions
import org.mockito.kotlin.verifyNoMoreInteractions
import org.mockito.kotlin.whenever

private const val TOOL = "kgp"
private val LOCALE = Locale.ENGLISH
private const val VERSION = 1

@OptIn(ExperimentalCoroutinesApi::class)
class TranslationRepositoryTest : AbstractArticleRoomDatabaseTest() {
    private val repo = object : TranslationRepository(db) {}
    private val translation = Translation().apply {
        toolCode = TOOL
        languageCode = LOCALE
        version = VERSION
    }
    private val key = translation.toTranslationRefKey()!!
    private val translationRef = TranslationRef(key)

    // region isProcessed()
    @Test
    fun `isProcessed() - Missing Translation`() = runBlockingTest {
        whenFindingTranslation().thenReturn(null)

        assertFalse(repo.isProcessed(translation))
        verify(translationDao).find(TOOL, LOCALE, VERSION)
        verifyNoMoreInteractions(translationDao)
    }

    @Test
    fun `isProcessed() - Invalid Translation`() = runBlockingTest {
        translation.toolCode = null

        assertFalse(repo.isProcessed(translation))
        verifyNoInteractions(translationDao)
    }

    @Test
    fun `isProcessed() - Not Processed`() = runBlockingTest {
        whenFindingTranslation().thenReturn(translationRef)

        assertFalse(repo.isProcessed(translation))
        verify(translationDao).find(TOOL, LOCALE, VERSION)
        verifyNoMoreInteractions(translationDao)
    }

    @Test
    fun `isProcessed() - Processed`() = runBlockingTest {
        translationRef.processed = true
        whenFindingTranslation().thenReturn(translationRef)

        assertTrue(repo.isProcessed(translation))
        verify(translationDao).find(TOOL, LOCALE, VERSION)
        verifyNoMoreInteractions(translationDao)
    }

    private suspend fun whenFindingTranslation() = whenever(translationDao.find(TOOL, LOCALE, VERSION))
    // endregion isProcessed()
}
