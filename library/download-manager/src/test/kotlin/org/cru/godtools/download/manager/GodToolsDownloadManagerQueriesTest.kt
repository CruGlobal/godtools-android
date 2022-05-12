package org.cru.godtools.download.manager

import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
import java.util.Locale
import javax.inject.Inject
import kotlin.random.Random
import org.cru.godtools.model.Language
import org.cru.godtools.model.Tool
import org.cru.godtools.model.Translation
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.`in`
import org.hamcrest.Matchers.contains
import org.hamcrest.Matchers.containsInAnyOrder
import org.hamcrest.Matchers.not
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.keynote.godtools.android.db.GodToolsDao
import org.robolectric.annotation.Config

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
@Config(application = HiltTestApplication::class)
class GodToolsDownloadManagerQueriesTest {
    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var dao: GodToolsDao

    @Before
    fun setup() {
        hiltRule.inject()
    }

    // region QUERY_PINNED_TRANSLATIONS
    @Test
    fun `QUERY_PINNED_TRANSLATIONS - product of pinned tools and languages`() {
        val tool1 = createTool("tool1") { isAdded = true }
        val tool2 = createTool("tool2") { isAdded = true }
        val tool3 = createTool("tool3")
        val lang1 = createLanguage(Locale.ENGLISH) { isAdded = true }
        val lang2 = createLanguage(Locale.FRENCH) { isAdded = true }
        val lang3 = createLanguage(Locale.GERMAN)
        // pinned translations
        val trans11 = createTranslation(tool1, lang1)
        val trans12 = createTranslation(tool1, lang2)
        val trans21 = createTranslation(tool2, lang1)
        // unpinned translations
        val trans13 = createTranslation(tool1, lang3)
        val trans31 = createTranslation(tool3, lang1)
        val trans33 = createTranslation(tool3, lang3)

        val translations = dao.get(QUERY_PINNED_TRANSLATIONS)
        val ids = translations.map { it.id }
        assertThat(ids, containsInAnyOrder(trans11.id, trans12.id, trans21.id))
        assertThat(trans13.id, not(`in`(ids)))
        assertThat(trans31.id, not(`in`(ids)))
        assertThat(trans33.id, not(`in`(ids)))
    }

    @Test
    fun `QUERY_PINNED_TRANSLATIONS - ignore downloaded & unpublished translations`() {
        val tool = createTool { isAdded = true }
        val lang = createLanguage { isAdded = true }
        val trans1 = createTranslation(tool, lang, 1)
        val trans2 = createTranslation(tool, lang, 2) { isPublished = false }
        val trans3 = createTranslation(tool, lang, 2) { isDownloaded = true }

        val translation = dao.get(QUERY_PINNED_TRANSLATIONS).single()
        assertTrue(translation.isPublished)
        assertEquals(trans1.id, translation.id)
        assertNotEquals(trans2.id, translation.id)
        assertNotEquals(trans3.id, translation.id)
    }
    // endregion QUERY_PINNED_TRANSLATIONS

    private fun createTool(code: String = "tool", config: Tool.() -> Unit = {}) = Tool().apply {
        id = Random.nextLong()
        this.code = code
        config()
    }.also { dao.insert(it) }

    private fun createLanguage(code: Locale = Locale.ENGLISH, config: Language.() -> Unit = {}) = Language().apply {
        id = Random.nextLong()
        this.code = code
        config()
    }.also { dao.insert(it) }

    private fun createTranslation(
        tool: Tool,
        language: Language,
        version: Int = 1,
        config: Translation.() -> Unit = {}
    ) = Translation().apply {
        id = Random.nextLong()
        toolCode = tool.code
        languageCode = language.code
        this.version = version
        isPublished = true
        config()
    }.also { dao.insert(it) }
}
