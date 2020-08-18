package org.cru.godtools.article.aem.db

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.whenever
import java.util.Locale
import org.cru.godtools.article.aem.model.TranslationRef
import org.cru.godtools.article.aem.model.toTranslationRefKey
import org.cru.godtools.model.Translation
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class TranslationRepositoryTest : AbstractArticleRoomDatabaseTest() {
    private val repo = object : TranslationRepository(db) {}
    private val translation = Translation().apply {
        toolCode = "kgp"
        languageCode = Locale.ENGLISH
        version = 1
    }
    private val key = translation.toTranslationRefKey()!!
    private val translationRef = TranslationRef(key)

    @Test
    fun verifyIsProcessedMissingTranslation() {
        whenFindingTranslation().thenReturn(null)

        assertFalse(repo.isProcessed(translation))
    }

    @Test
    fun verifyIsProcessedTranslationNotProcessed() {
        whenFindingTranslation().thenReturn(translationRef)

        assertFalse(repo.isProcessed(translation))
    }

    @Test
    fun verifyIsProcessedTranslationProcessed() {
        translationRef.processed = true
        whenFindingTranslation().thenReturn(translationRef)

        assertTrue(repo.isProcessed(translation))
    }

    private fun whenFindingTranslation() = whenever(translationDao.find(any(), any(), any()))
}
