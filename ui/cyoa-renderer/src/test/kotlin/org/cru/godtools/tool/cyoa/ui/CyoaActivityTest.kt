package org.cru.godtools.tool.cyoa.ui

import android.content.Context
import android.content.Intent
import android.content.Intent.ACTION_VIEW
import android.graphics.Color
import android.net.Uri
import android.view.ViewGroup
import androidx.activity.viewModels
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
import io.mockk.every
import io.mockk.mockk
import java.util.Locale
import javax.inject.Inject
import org.cru.godtools.base.tool.activity.MultiLanguageToolActivityDataModel
import org.cru.godtools.base.tool.model.Event
import org.cru.godtools.base.tool.service.ManifestManager
import org.cru.godtools.base.ui.createCyoaActivityIntent
import org.cru.godtools.shared.tool.parser.model.EventId
import org.cru.godtools.shared.tool.parser.model.Gravity
import org.cru.godtools.shared.tool.parser.model.ImageScaleType
import org.cru.godtools.shared.tool.parser.model.Manifest
import org.cru.godtools.shared.tool.parser.model.page.CardCollectionPage
import org.cru.godtools.shared.tool.parser.model.page.ContentPage
import org.cru.godtools.shared.tool.parser.model.page.Page
import org.cru.godtools.shared.tool.parser.model.page.backgroundColor
import org.cru.godtools.shared.tool.parser.model.page.backgroundImageGravity
import org.cru.godtools.shared.tool.parser.model.page.backgroundImageScaleType
import org.cru.godtools.shared.tool.parser.model.page.controlColor
import org.cru.godtools.tool.cyoa.R
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

private const val TOOL = "test"

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
@Config(application = HiltTestApplication::class)
class CyoaActivityTest {
    private val context get() = ApplicationProvider.getApplicationContext<Context>()

    @get:Rule
    val hiltRule = HiltAndroidRule(this)
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private fun <R> scenario(
        intent: Intent = context.createCyoaActivityIntent(TOOL, Locale.ENGLISH),
        block: (ActivityScenario<CyoaActivity>) -> R
    ): R = ActivityScenario.launch<CyoaActivity>(intent).use(block)

    // region Mocks
    @Inject
    internal lateinit var manifestManager: ManifestManager

    private val manifestEnglish = MutableLiveData<Manifest?>(null)

    private val page1 = contentPage("page1")
    private val page2 = contentPage("page2")
    private val page3 = contentPage("page3")
    private val page4 = contentPage("page4")

    private val cardCollectionPage1 = cardCollectionPage("page1")
    private val cardCollectionPage2 = cardCollectionPage("page2")

    private val eventId1 = EventId.parse("event1").first()
    private val eventId2 = EventId.parse("event2").first()

    @Before
    fun setupMocks() {
        hiltRule.inject()
        every { manifestManager.getLatestPublishedManifestLiveData(TOOL, Locale.ENGLISH) } returns manifestEnglish
    }

    private fun manifest(pages: List<Page> = emptyList()) = Manifest(
        code = TOOL,
        type = Manifest.Type.CYOA,
        locale = Locale.ENGLISH,
        pages = { pages }
    )

    private fun contentPage(id: String, block: ContentPage.() -> Unit = {}) = mockk<ContentPage> {
        every { this@mockk.id } returns id
        initializeMockk()
        every { content } returns emptyList()
        block()
    }

    private fun cardCollectionPage(id: String, block: CardCollectionPage.() -> Unit = {}) = mockk<CardCollectionPage> {
        every { this@mockk.id } returns id
        initializeMockk()
        every { cards } returns emptyList()
        block()
    }

    private fun Page.initializeMockk() {
        every { manifest } returns manifest()
        every { isHidden } returns false
        every { parentPage } returns null
        every { listeners } returns emptySet()
        every { dismissListeners } returns emptySet()
        every { getAnalyticsEvents(any()) } returns emptyList()
        every { backgroundColor } returns Color.BLUE
        every { backgroundImage } returns null
        every { backgroundImageGravity } returns Gravity.CENTER
        every { backgroundImageScaleType } returns ImageScaleType.FILL
        every { controlColor } returns Color.CYAN
    }

    private fun EventId.event() = Event.Builder(manifest())
        .id(this)
        .build()
    // endregion Mocks

    @Test
    fun `Initial Page - Skip Hidden Pages`() {
        val hiddenPage = contentPage("hiddenPage") {
            every { isHidden } returns true
        }
        manifestEnglish.value = manifest(listOf(hiddenPage, page1, page2))

        scenario {
            it.onActivity {
                assertEquals("page1", it.pageFragment!!.pageId)
            }
        }
    }

    // region Intent Processing
    @Test
    fun `Intent Processing - Normal Launch`() {
        manifestEnglish.value = manifest(listOf(page1, page2))

        scenario(intent = context.createCyoaActivityIntent(TOOL, Locale.ENGLISH)) {
            it.onActivity {
                assertEquals(TOOL, it.dataModel.toolCode.value)
                assertEquals(listOf(Locale.ENGLISH), it.dataModel.primaryLocales.value)
                assertEquals("page1", it.pageFragment!!.pageId)
            }
        }
    }

    @Test
    fun `Intent Processing - Normal Launch - Deferred Manifest`() {
        scenario(intent = context.createCyoaActivityIntent(TOOL, Locale.ENGLISH)) {
            it.onActivity {
                assertEquals(TOOL, it.dataModel.toolCode.value)
                assertEquals(listOf(Locale.ENGLISH), it.dataModel.primaryLocales.value)
                assertNull(it.pageFragment)
                manifestEnglish.value = manifest(listOf(page1, page2))
                assertEquals("page1", it.pageFragment!!.pageId)
            }
        }
    }

    @Test
    fun `Intent Processing - Open Specific Page`() {
        manifestEnglish.value = manifest(listOf(page1, page2))

        scenario(intent = context.createCyoaActivityIntent(TOOL, Locale.ENGLISH, pageId = "page2")) {
            it.onActivity {
                assertEquals(TOOL, it.dataModel.toolCode.value)
                assertEquals(listOf(Locale.ENGLISH), it.dataModel.primaryLocales.value)
                assertEquals("page2", it.pageFragment!!.pageId)
            }
        }
    }

    @Test
    fun `Intent Processing - Open Specific Page - Deferred Manifest`() {
        scenario(intent = context.createCyoaActivityIntent(TOOL, Locale.ENGLISH, pageId = "page2")) {
            it.onActivity {
                assertEquals(TOOL, it.dataModel.toolCode.value)
                assertEquals(listOf(Locale.ENGLISH), it.dataModel.primaryLocales.value)
                assertNull(it.pageFragment)
                manifestEnglish.value = manifest(listOf(page1, page2))
                assertEquals("page2", it.pageFragment!!.pageId)
            }
        }
    }

    @Test
    fun `Intent Processing - Uri Scheme Deep Link`() {
        manifestEnglish.value = manifest(listOf(page1, page2))

        scenario(intent = Intent(ACTION_VIEW, Uri.parse("godtools://org.cru.godtools.test/tool/cyoa/test/en"))) {
            it.onActivity {
                assertEquals("test", it.dataModel.toolCode.value)
                assertEquals(listOf(Locale("en")), it.dataModel.primaryLocales.value)
                assertEquals("page1", it.pageFragment!!.pageId)
            }
        }
    }

    @Test
    fun `Intent Processing - Uri Scheme Deep Link - Specific Page`() {
        manifestEnglish.value = manifest(listOf(page1, page2))

        scenario(intent = Intent(ACTION_VIEW, Uri.parse("godtools://org.cru.godtools.test/tool/cyoa/test/en/page2"))) {
            it.onActivity {
                assertEquals("test", it.dataModel.toolCode.value)
                assertEquals(listOf(Locale("en")), it.dataModel.primaryLocales.value)
                assertEquals("page2", it.pageFragment!!.pageId)
            }
        }
    }
    // endregion Intent Processing

    // region navigateToParentPage()
    @Test
    fun `navigateToParentPage() - No Parents`() {
        manifestEnglish.value = manifest(listOf(page1, page2))

        scenario {
            it.onActivity {
                it.showPage(page2)
                it.assertPageStack("page1", "page2")

                // TODO: There isn't a reliable way to ensure that regular up navigation is processed, so for now we use
                //       the underlying navigateToParentPage() for this test
//                shadowOf(it).clickMenuItem(android.R.id.home)
                assertFalse(it.navigateToParentPage())
            }
        }
    }

    @Test
    fun `navigateToParentPage() - Simple`() {
        every { page2.parentPage } returns page1
        manifestEnglish.value = manifest(listOf(page1, page2))

        scenario {
            it.onActivity {
                it.showPage(page2)
                it.assertPageStack("page1", "page2")

                assertTrue(shadowOf(it).clickMenuItem(android.R.id.home))
                it.assertPageStack("page1")
            }
        }
    }

    @Test
    fun `navigateToParentPage() - Skip extra entries`() {
        every { page3.parentPage } returns page1
        manifestEnglish.value = manifest(listOf(page1, page2, page3))

        scenario {
            it.onActivity {
                it.showPage(page2)
                it.showPage(page3)
                it.assertPageStack("page1", "page2", "page3")

                assertTrue(shadowOf(it).clickMenuItem(android.R.id.home))
                it.assertPageStack("page1")
            }
        }
    }

    @Test
    fun `navigateToParentPage() - Parent not in backstack`() {
        every { page3.parentPage } returns page2
        manifestEnglish.value = manifest(listOf(page1, page2, page3))

        scenario {
            it.onActivity {
                it.showPage(page3)
                it.assertPageStack("page1", "page3")

                assertTrue(shadowOf(it).clickMenuItem(android.R.id.home))
                it.assertPageStack("page2")
            }
        }
    }

    @Test
    fun `navigateToParentPage() - Parent not in backstack - Initial page`() {
        every { page1.parentPage } returns page2
        manifestEnglish.value = manifest(listOf(page1, page2))

        scenario {
            it.onActivity {
                it.assertPageStack("page1")

                assertTrue(shadowOf(it).clickMenuItem(android.R.id.home))
                it.assertPageStack("page2")
            }
        }
    }

    @Test
    fun `navigateToParentPage() - Parent not in backstack - Grandparent exists`() {
        every { page2.parentPage } returns page1
        every { page3.parentPage } returns page1
        every { page4.parentPage } returns page2
        manifestEnglish.value = manifest(listOf(page1, page2, page3, page4))

        scenario {
            it.onActivity {
                it.showPage(page3)
                it.showPage(page4)
                it.assertPageStack("page1", "page3", "page4")

                assertTrue(shadowOf(it).clickMenuItem(android.R.id.home))
                it.assertPageStack("page1", "page2")
            }
        }
    }

    @Test
    fun `navigateToParentPage() - Cycle`() {
        every { page1.parentPage } returns page2
        every { page2.parentPage } returns page3
        every { page3.parentPage } returns page2
        manifestEnglish.value = manifest(listOf(page1, page2, page3))

        scenario {
            it.onActivity {
                it.assertPageStack("page1")

                assertTrue(shadowOf(it).clickMenuItem(android.R.id.home))
                it.assertPageStack("page2")

                repeat(3) { _ ->
                    assertTrue(shadowOf(it).clickMenuItem(android.R.id.home))
                    it.assertPageStack("page3")

                    assertTrue(shadowOf(it).clickMenuItem(android.R.id.home))
                    it.assertPageStack("page2")
                }
            }
        }
    }
    // endregion Up navigation

    // region checkForPageEvent()
    @Test
    fun `checkForPageEvent() - Single Event - Go to new page`() {
        every { page2.listeners } returns setOf(eventId1)
        manifestEnglish.value = manifest(listOf(page1, page2))

        scenario {
            it.onActivity {
                it.processContentEvent(eventId1.event())
                it.assertPageStack("page1", "page2")

                it.supportFragmentManager.popBackStackImmediate()
                it.assertPageStack("page1")
            }
        }
    }

    @Test
    fun `checkForPageEvent() - Single Event - dismiss initial page & Go to new page`() {
        every { page1.dismissListeners } returns setOf(eventId1)
        every { page2.listeners } returns setOf(eventId1)
        manifestEnglish.value = manifest(listOf(page1, page2))

        scenario {
            it.onActivity {
                it.processContentEvent(eventId1.event())
                it.assertPageStack("page2")
            }
        }
    }

    @Test
    fun `checkForPageEvent() - Multiple Events - Go to multiple new pages`() {
        every { page2.listeners } returns setOf(eventId1)
        every { page3.listeners } returns setOf(eventId2)
        manifestEnglish.value = manifest(listOf(page1, page2, page3))

        scenario {
            it.onActivity {
                it.processContentEvent(eventId1.event())
                it.processContentEvent(eventId2.event())
                it.assertPageStack("page1", "page2", "page3")

                it.supportFragmentManager.popBackStackImmediate()
                it.assertPageStack("page1", "page2")
                it.supportFragmentManager.popBackStackImmediate()
                it.assertPageStack("page1")
            }
        }
    }

    @Test
    fun `checkForPageEvent() - Multiple Events - Go to page and then dismiss it`() {
        every { page2.listeners } returns setOf(eventId1)
        every { page2.dismissListeners } returns setOf(eventId2)
        manifestEnglish.value = manifest(listOf(page1, page2))

        scenario {
            it.onActivity {
                it.processContentEvent(eventId1.event())
                it.processContentEvent(eventId2.event())
                it.assertPageStack("page1")
            }
        }
    }

    @Test
    fun `checkForPageEvent() - Multiple Events - dismiss initial page & Go to new page`() {
        every { page1.dismissListeners } returns setOf(eventId1)
        every { page2.listeners } returns setOf(eventId2)
        manifestEnglish.value = manifest(listOf(page1, page2))

        scenario {
            it.onActivity {
                it.processContentEvent(eventId1.event())
                it.processContentEvent(eventId2.event())
                it.assertPageStack("page2")
            }
        }
    }
    // endregion checkForPageEvent()

    // region Update Manifest
    @Test
    fun `Update Manifest - page removed - current`() {
        manifestEnglish.value = manifest(listOf(page1, page2))

        scenario {
            it.onActivity {
                it.showPage(page2)
                it.assertPageStack("page1", "page2")

                manifestEnglish.value = manifest(listOf(page1))
                it.assertPageStack("page1")
            }
        }
    }

    @Test
    fun `Update Manifest - page removed - current - initial`() {
        manifestEnglish.value = manifest(listOf(page1, page2))

        scenario {
            it.onActivity {
                it.assertPageStack("page1")

                manifestEnglish.value = manifest(listOf(page2))
                assertTrue(it.isFinishing)
            }
        }
    }

    @Test
    fun `Update Manifest - page removed - parent`() {
        manifestEnglish.value = manifest(listOf(page1, page2, page3))

        scenario {
            it.onActivity {
                it.showPage(page2)
                it.showPage(page3)
                it.assertPageStack("page1", "page2", "page3")

                manifestEnglish.value = manifest(listOf(page1, page3))
                it.onBackPressed()
                it.assertPageStack("page1")
            }
        }
    }

    @Test
    fun `Update Manifest - page removed - parent - initial`() {
        manifestEnglish.value = manifest(listOf(page1, page2))

        scenario {
            it.onActivity {
                it.showPage(page2)
                it.assertPageStack("page1", "page2")

                manifestEnglish.value = manifest(listOf(page2))
                assertFalse(it.isFinishing)
                it.onBackPressed()
                assertTrue(it.isFinishing)
            }
        }
    }

    @Test
    fun `Update Manifest - page removed - multiple`() {
        manifestEnglish.value = manifest(listOf(page1, page2, page3))

        scenario {
            it.onActivity {
                it.showPage(page2)
                it.showPage(page3)
                it.assertPageStack("page1", "page2", "page3")

                manifestEnglish.value = manifest(listOf(page1))
                it.assertPageStack("page1")
            }
        }
    }

    @Test
    fun `Update Manifest - page type changed - current`() {
        manifestEnglish.value = manifest(listOf(page1, page2))

        scenario {
            it.onActivity {
                it.showPage(page2)
                it.assertPageStack("page1", "page2")
                assertTrue(it.pageFragment is CyoaContentPageFragment)

                manifestEnglish.value = manifest(listOf(page1, cardCollectionPage2))
                it.assertPageStack("page1", "page2")
                assertTrue(it.pageFragment is CyoaCardCollectionPageFragment)

                it.onBackPressed()
                it.assertPageStack("page1")
            }
        }
    }

    @Test
    fun `Update Manifest - page type changed - parent`() {
        manifestEnglish.value = manifest(listOf(page1, page2, page3))

        scenario {
            it.onActivity {
                it.showPage(page2)
                it.assertPageStack("page1", "page2")
                assertTrue(it.pageFragment is CyoaContentPageFragment)
                it.showPage(page3)
                it.assertPageStack("page1", "page2", "page3")

                manifestEnglish.value = manifest(listOf(page1, cardCollectionPage2, page3))
                it.onBackPressed()
                it.assertPageStack("page1", "page2")
                assertTrue(it.pageFragment is CyoaCardCollectionPageFragment)
            }
        }
    }

    @Test
    fun `Update Manifest - page type changed - parent & current removed`() {
        manifestEnglish.value = manifest(listOf(page1, page2, page3))

        scenario {
            it.onActivity {
                it.showPage(page2)
                it.assertPageStack("page1", "page2")
                assertTrue(it.pageFragment is CyoaContentPageFragment)

                it.showPage(page3)
                it.assertPageStack("page1", "page2", "page3")

                manifestEnglish.value = manifest(listOf(page1, cardCollectionPage2))
                it.assertPageStack("page1", "page2")
                assertTrue(it.pageFragment is CyoaCardCollectionPageFragment)
            }
        }
    }
    // endregion Update Manifest

    private val CyoaActivity.dataModel get() = viewModels<MultiLanguageToolActivityDataModel>().value

    private fun CyoaActivity.assertPageStack(vararg pages: String) {
        supportFragmentManager.executePendingTransactions()
        assertEquals(pages.size - 1, supportFragmentManager.backStackEntryCount)
        pages.dropLast(1).forEachIndexed { i, page ->
            assertEquals(page, supportFragmentManager.getBackStackEntryAt(i).name)
        }
        assertEquals(pages.last(), pageFragment!!.pageId)
        assertEquals(1, findViewById<ViewGroup>(R.id.page).childCount)
    }
}
