package org.cru.godtools.article.aem.db

import android.net.Uri
import androidx.test.ext.junit.runners.AndroidJUnit4
import java.util.Locale
import kotlinx.coroutines.runBlocking
import org.cru.godtools.article.aem.model.TranslationRef
import org.cru.godtools.article.aem.model.toTranslationRefKey
import org.cru.godtools.model.Translation
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

private val URI1 = Uri.parse("https://example.com/content/experience-fragments/questions_about_god")
private val URI2 = Uri.parse("https://example.com/content/experience-fragments/other")

@RunWith(AndroidJUnit4::class)
class TranslationRepositoryIT : BaseArticleRoomDatabaseIT() {
    private val repository get() = db.translationRepository()

    @Test
    fun verifyAddAemImportsTranslationAlreadyPresent() = runBlocking {
        // setup test
        val translation = Translation().apply {
            toolCode = "kgp"
            languageCode = Locale.ENGLISH
            version = 1
        }
        db.translationDao().insertOrIgnore(TranslationRef(translation.toTranslationRefKey()!!))

        // perform test
        assertFalse(repository.isProcessed(translation))
        repository.addAemImports(translation, listOf(URI1, URI2))
        assertTrue(repository.isProcessed(translation))

        // TODO: test AemImports once we define dao methods for reading AemImports
        val aemImport = runBlocking { db.aemImportDao().find(URI1) }
        assertEquals(URI1, aemImport!!.uri)
    }
}
