package org.cru.godtools.tool.cyoa.ui

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import dagger.hilt.android.testing.HiltTestApplication
import java.util.Locale
import javax.inject.Inject
import org.cru.godtools.base.tool.model.Event
import org.cru.godtools.base.tool.service.ManifestManager
import org.cru.godtools.base.ui.createCyoaActivityIntent
import org.cru.godtools.tool.model.EventId
import org.cru.godtools.tool.model.Manifest
import org.cru.godtools.tool.model.page.CardCollectionPage
import org.cru.godtools.tool.model.page.ContentPage
import org.cru.godtools.tool.model.page.Page
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.KStubbing
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.stub
import org.mockito.kotlin.whenever
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config

private const val TOOL = "test"

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
@Config(application = HiltTestApplication::class)
class CyoaActivityTest {
    @get:Rule
    val hiltRule = HiltAndroidRule(this)
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    private fun <R> scenario(block: (ActivityScenario<CyoaActivity>) -> R): R {
        val intent = ApplicationProvider.getApplicationContext<Context>().createCyoaActivityIntent(TOOL, Locale.ENGLISH)
        return ActivityScenario.launch<CyoaActivity>(intent).use(block)
    }

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
        manifestManager.stub {
            on { getLatestPublishedManifestLiveData(TOOL, Locale.ENGLISH) } doReturn manifestEnglish
        }
    }

    private fun manifest(pages: List<Page> = emptyList()) = Manifest(
        code = TOOL,
        type = Manifest.Type.CYOA,
        locale = Locale.ENGLISH,
        pages = { pages }
    )

    private fun contentPage(id: String, stubbing: KStubbing<ContentPage>.(ContentPage) -> Unit = {}) =
        mock<ContentPage> {
            on { this.id } doReturn id
            on { manifest } doReturn manifest()
            stubbing(it)
        }

    private fun cardCollectionPage(id: String, stubbing: KStubbing<CardCollectionPage>.(CardCollectionPage) -> Unit = {}) =
        mock<CardCollectionPage> {
            on { this.id } doReturn id
            on { manifest } doReturn manifest()
            stubbing(it)
        }

    private fun EventId.event() = mock<Event> {
        on { id } doReturn this@event
        on { tool } doReturn TOOL
        on { locale } doReturn Locale.ENGLISH
    }
    // endregion Mocks

    @Test
    fun `Initial Page - Skip Hidden Pages`() {
        val hiddenPage = contentPage("hiddenPage") {
            on { isHidden } doReturn true
        }
        manifestEnglish.value = manifest(listOf(hiddenPage, page1, page2))

        scenario {
            it.onActivity {
                assertEquals("page1", it.pageFragment!!.pageId)
            }
        }
    }

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
        whenever(page2.parentPage) doReturn page1
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
        whenever(page3.parentPage) doReturn page1
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
        whenever(page3.parentPage) doReturn page2
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
        whenever(page1.parentPage) doReturn page2
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
        whenever(page2.parentPage) doReturn page1
        whenever(page3.parentPage) doReturn page1
        whenever(page4.parentPage) doReturn page2
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
        whenever(page1.parentPage) doReturn page2
        whenever(page2.parentPage) doReturn page3
        whenever(page3.parentPage) doReturn page2
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
        whenever(page2.listeners) doReturn setOf(eventId1)
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
        whenever(page1.dismissListeners) doReturn setOf(eventId1)
        whenever(page2.listeners) doReturn setOf(eventId1)
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
        whenever(page2.listeners) doReturn setOf(eventId1)
        whenever(page3.listeners) doReturn setOf(eventId2)
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
        page2.stub {
            on { listeners } doReturn setOf(eventId1)
            on { dismissListeners } doReturn setOf(eventId2)
        }
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
        whenever(page1.dismissListeners) doReturn setOf(eventId1)
        whenever(page2.listeners) doReturn setOf(eventId2)
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

    private fun CyoaActivity.assertPageStack(vararg pages: String) {
        supportFragmentManager.executePendingTransactions()
        assertEquals(pages.size - 1, supportFragmentManager.backStackEntryCount)
        pages.dropLast(1).forEachIndexed { i, page ->
            assertEquals(page, supportFragmentManager.getBackStackEntryAt(i).name)
        }
        assertEquals(pages.last(), pageFragment!!.pageId)
    }
}
