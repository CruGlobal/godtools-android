package org.cru.godtools.download.manager.db

import androidx.test.ext.junit.runners.AndroidJUnit4
import app.cash.turbine.test
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
import java.util.Locale
import javax.inject.Inject
import kotlin.random.Random
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.cru.godtools.model.Tool
import org.cru.godtools.model.Translation
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.containsInAnyOrder
import org.hamcrest.Matchers.`in`
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
@OptIn(ExperimentalCoroutinesApi::class)
class DownloadManagerRepositoryTest {
    @get:Rule
    var hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var dao: GodToolsDao

    @Inject
    internal lateinit var repository: DownloadManagerRepository

    @Before
    fun setup() {
        hiltRule.inject()
    }

    // region getFavoriteTranslationsThatNeedDownload()
    @Test
    fun `getFavoriteTranslationsThatNeedDownload() - product of pinned tools and specified languages`() = runTest {
        val tool1 = createTool("tool1") { isAdded = true }
        val tool2 = createTool("tool2") { isAdded = true }
        val tool3 = createTool("tool3")
        // pinned translations
        val trans11 = createTranslation(tool1, Locale.ENGLISH)
        val trans12 = createTranslation(tool1, Locale.FRENCH)
        val trans21 = createTranslation(tool2, Locale.ENGLISH)
        // unpinned translations
        val trans13 = createTranslation(tool1, Locale.GERMAN)
        val trans31 = createTranslation(tool3, Locale.ENGLISH)
        val trans33 = createTranslation(tool3, Locale.GERMAN)

        repository.getFavoriteTranslationsThatNeedDownload(listOf(Locale.ENGLISH, Locale.FRENCH)).test {
            val ids = awaitItem().map { it.id }
            assertThat(ids, containsInAnyOrder(trans11.id, trans12.id, trans21.id))
            assertThat(trans13.id, not(`in`(ids)))
            assertThat(trans31.id, not(`in`(ids)))
            assertThat(trans33.id, not(`in`(ids)))

            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun `getFavoriteTranslationsThatNeedDownload() - ignore downloaded & unpublished translations`() = runTest {
        val tool = createTool { isAdded = true }
        val trans1 = createTranslation(tool, version = 1)
        val trans2 = createTranslation(tool, version = 2) { isPublished = false }
        val trans3 = createTranslation(tool, version = 2) { isDownloaded = true }

        repository.getFavoriteTranslationsThatNeedDownload(listOf(Locale.ENGLISH)).test {
            val translation = awaitItem().single()
            assertTrue(translation.isPublished)
            assertEquals(trans1.id, translation.id)
            assertNotEquals(trans2.id, translation.id)
            assertNotEquals(trans3.id, translation.id)

            cancelAndIgnoreRemainingEvents()
        }
    }
    // endregion getFavoriteTranslationsThatNeedDownload()

    private fun createTool(code: String = "tool", config: Tool.() -> Unit = {}) = Tool().apply {
        id = Random.nextLong()
        this.code = code
        config()
    }.also { dao.insert(it) }

    private fun createTranslation(
        tool: Tool,
        locale: Locale = Locale.ENGLISH,
        version: Int = 1,
        config: Translation.() -> Unit = {}
    ) = Translation().apply {
        id = Random.nextLong()
        toolCode = tool.code
        languageCode = locale
        this.version = version
        isPublished = true
        config()
    }.also { dao.insert(it) }
}
