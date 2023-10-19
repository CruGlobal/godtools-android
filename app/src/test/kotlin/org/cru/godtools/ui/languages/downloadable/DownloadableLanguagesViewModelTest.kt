package org.cru.godtools.ui.languages.downloadable

import android.app.Application
import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.cash.turbine.test
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import java.util.Locale
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.cru.godtools.base.Settings
import org.cru.godtools.db.repository.LanguagesRepository
import org.cru.godtools.model.Language
import org.cru.godtools.sync.GodToolsSyncService
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(application = Application::class)
@OptIn(ExperimentalCoroutinesApi::class)
class DownloadableLanguagesViewModelTest {
    private val appLanguageFlow = MutableStateFlow<Locale>(Locale.ENGLISH)
    private val languages = MutableSharedFlow<List<Language>>()

    private val context: Context get() = ApplicationProvider.getApplicationContext()
    private val languagesRepository: LanguagesRepository = mockk {
        every { getLanguagesFlow() } returns languages
    }
    private val savedStateHandle = SavedStateHandle()
    private val settings: Settings = mockk {
        every { appLanguageFlow } returns this@DownloadableLanguagesViewModelTest.appLanguageFlow
    }
    private val syncService: GodToolsSyncService = mockk {
        coEvery { syncLanguages() } returns true
    }
    private val testScope = TestScope()

    private lateinit var viewModel: DownloadableLanguagesViewModel

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(UnconfinedTestDispatcher(testScope.testScheduler))
        viewModel = DownloadableLanguagesViewModel(
            context = context,
            languagesRepository = languagesRepository,
            settings = settings,
            syncService = syncService,
            savedStateHandle = savedStateHandle,
        )
    }

    @AfterTest
    fun cleanup() {
        Dispatchers.resetMain()
    }

    @Test
    fun `Property searchQuery - Persists through re-creation of ViewModel`() {
        viewModel.updateSearchQuery("query")

        val viewModel2 = DownloadableLanguagesViewModel(
            context = context,
            languagesRepository = languagesRepository,
            settings = settings,
            syncService = syncService,
            savedStateHandle = savedStateHandle
        )

        assertEquals("query", viewModel2.searchQuery.value)
    }

    // region Property: languages
    @Test
    fun `Property languages - sorted by name`() = testScope.runTest {
        val english = Language(Locale.ENGLISH)
        val french = Language(Locale.FRENCH)

        viewModel.languages.test {
            languages.subscriptionCount.first { it >= 1 }
            awaitItem()

            languages.emit(listOf(french, english))
            assertEquals(listOf(english, french), awaitItem())
        }
    }

    @Test
    fun `Property languages - float initial pinned languages permanently`() = testScope.runTest {
        val english = Language(Locale.ENGLISH)
        val french = Language(Locale.FRENCH, isAdded = true)
        val german = Language(Locale.GERMAN, isAdded = true)

        viewModel.languages.test {
            languages.subscriptionCount.first { it >= 1 }
            awaitItem()

            languages.emit(listOf(english, french))
            assertEquals(listOf(french, english), awaitItem())

            languages.emit(listOf(english, french, german))
            assertEquals(listOf(french, english, german), awaitItem())
        }
    }

    @Test
    fun `Property languages - filter by search query`() = testScope.runTest {
        val english = Language(Locale.ENGLISH)
        val french = Language(Locale.FRENCH)

        viewModel.languages.test {
            languages.subscriptionCount.first { it >= 1 }
            awaitItem()

            languages.emit(listOf(french, english))
            assertEquals(listOf(english, french), awaitItem())

            viewModel.updateSearchQuery("eng")
            assertEquals(listOf(english), awaitItem())

            viewModel.updateSearchQuery("fra")
            assertEquals(listOf(french), awaitItem())

            viewModel.updateSearchQuery("eng ish")
            assertEquals(listOf(english), awaitItem())
        }
    }
    // endregion Property: languages
}
