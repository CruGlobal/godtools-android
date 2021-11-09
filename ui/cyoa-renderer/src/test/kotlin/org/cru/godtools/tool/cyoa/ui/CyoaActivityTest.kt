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
import org.cru.godtools.tool.model.page.ContentPage
import org.cru.godtools.tool.model.page.Page
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.kotlin.KStubbing
import org.mockito.kotlin.doReturn
import org.mockito.kotlin.mock
import org.mockito.kotlin.stub
import org.mockito.kotlin.whenever
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

    // region checkForPageEvent()
    @Test
    fun `checkForPageEvent() - Single Event - Go to new page`() {
        whenever(page2.listeners) doReturn setOf(eventId1)
        manifestEnglish.value = manifest(listOf(page1, page2))

        scenario {
            it.onActivity {
                it.processContentEvent(eventId1.event())
                it.supportFragmentManager.executePendingTransactions()

                assertEquals("page2", it.pageFragment!!.pageId)
                assertEquals(1, it.supportFragmentManager.backStackEntryCount)
                assertEquals("page1", it.supportFragmentManager.getBackStackEntryAt(0).name)
                it.supportFragmentManager.popBackStackImmediate()
                assertEquals("page1", it.pageFragment!!.pageId)
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
                it.supportFragmentManager.executePendingTransactions()

                assertEquals("page2", it.pageFragment!!.pageId)
                assertEquals(0, it.supportFragmentManager.backStackEntryCount)
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
                it.supportFragmentManager.executePendingTransactions()

                assertEquals("page3", it.pageFragment!!.pageId)
                assertEquals(2, it.supportFragmentManager.backStackEntryCount)
                assertEquals("page1", it.supportFragmentManager.getBackStackEntryAt(0).name)
                assertEquals("page2", it.supportFragmentManager.getBackStackEntryAt(1).name)

                it.supportFragmentManager.popBackStackImmediate()
                assertEquals("page2", it.pageFragment!!.pageId)
                it.supportFragmentManager.popBackStackImmediate()
                assertEquals("page1", it.pageFragment!!.pageId)
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
                it.supportFragmentManager.executePendingTransactions()

                assertEquals("page1", it.pageFragment!!.pageId)
                assertEquals(0, it.supportFragmentManager.backStackEntryCount)
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
                it.supportFragmentManager.executePendingTransactions()

                assertEquals("page2", it.pageFragment!!.pageId)
                assertEquals(0, it.supportFragmentManager.backStackEntryCount)
            }
        }
    }
    // endregion checkForPageEvent()
}
