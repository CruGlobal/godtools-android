package org.cru.godtools.ui.languages.downloadable

import android.app.Application
import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import app.cash.turbine.test
import io.mockk.every
import io.mockk.mockk
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.cru.godtools.db.repository.LanguagesRepository
import org.cru.godtools.model.Language
import org.cru.godtools.model.LanguageMatchers.languageMatcher
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.Matchers.contains
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(application = Application::class)
@OptIn(ExperimentalCoroutinesApi::class)
class DownloadableLanguagesViewModelTest {
    private val languages = MutableSharedFlow<List<Language>>()

    private val context: Context get() = ApplicationProvider.getApplicationContext()
    private val languagesRepository: LanguagesRepository = mockk {
        every { getLanguagesFlow() } returns languages
    }
    private val savedStateHandle = SavedStateHandle()
    private val testScope = TestScope()

    private lateinit var viewModel: DownloadableLanguagesViewModel

    @Before
    fun setup() {
        Dispatchers.setMain(UnconfinedTestDispatcher(testScope.testScheduler))
        viewModel = DownloadableLanguagesViewModel(
            context = context,
            languagesRepository = languagesRepository,
            savedStateHandle = savedStateHandle,
        )
    }

    @Test
    fun `Property searchQuery - Persists through re-creation of ViewModel`() {
        viewModel.updateSearchQuery("query")

        val viewModel2 = DownloadableLanguagesViewModel(
            context = context,
            languagesRepository = languagesRepository,
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
            awaitItem()

            languages.emit(listOf(french, english))
            assertThat(awaitItem(), contains(languageMatcher(english), languageMatcher(french)))
        }
    }

    @Test
    fun `Property languages - float initial pinned languages permanently`() = testScope.runTest {
        val english = Language(Locale.ENGLISH)
        val french = Language(Locale.FRENCH, isAdded = true)
        val german = Language(Locale.GERMAN, isAdded = true)

        viewModel.languages.test {
            awaitItem()

            languages.emit(listOf(english, french))
            assertThat(awaitItem(), contains(languageMatcher(french), languageMatcher(english)))

            languages.emit(listOf(english, french, german))
            assertThat(
                awaitItem(),
                contains(languageMatcher(french), languageMatcher(english), languageMatcher(german))
            )
        }
    }

    @Test
    fun `Property languages - filter by search query`() = testScope.runTest {
        val english = Language(Locale.ENGLISH)
        val french = Language(Locale.FRENCH)

        viewModel.languages.test {
            awaitItem()

            languages.emit(listOf(french, english))
            assertThat(awaitItem(), contains(languageMatcher(english), languageMatcher(french)))

            viewModel.updateSearchQuery("eng")
            assertThat(awaitItem(), contains(languageMatcher(english)))

            viewModel.updateSearchQuery("fra")
            assertThat(awaitItem(), contains(languageMatcher(french)))

            viewModel.updateSearchQuery("eng ish")
            assertThat(awaitItem(), contains(languageMatcher(english)))
        }
    }
    // endregion Property: languages
}
