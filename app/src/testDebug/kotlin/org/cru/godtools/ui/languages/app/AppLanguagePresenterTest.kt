package org.cru.godtools.ui.languages.app

import android.app.Application
import androidx.core.os.LocaleListCompat
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.slack.circuit.test.FakeNavigator
import com.slack.circuit.test.test
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkObject
import io.mockk.verifyAll
import java.util.Locale
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.ccci.gto.android.common.androidx.core.app.LocaleConfigCompat
import org.cru.godtools.TestUtils.clearAndroidUiDispatcher
import org.cru.godtools.base.Settings
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(application = Application::class)
class AppLanguagePresenterTest {
    private val appLanguage = MutableStateFlow(Locale.ENGLISH)

    private val navigator = FakeNavigator(AppLanguageScreen)
    private val settings: Settings = mockk {
        every { appLanguage } returns this@AppLanguagePresenterTest.appLanguage.value
        every { appLanguage = any() } answers { this@AppLanguagePresenterTest.appLanguage.value = firstArg() }
        every { appLanguageFlow } returns this@AppLanguagePresenterTest.appLanguage
    }

    private lateinit var presenter: AppLanguagePresenter

    @BeforeTest
    fun setup() {
        mockkObject(LocaleConfigCompat)
        every { LocaleConfigCompat.getSupportedLocales(any()) } returns LocaleListCompat.getEmptyLocaleList()

        presenter = AppLanguagePresenter(
            context = ApplicationProvider.getApplicationContext(),
            settings = settings,
            navigator = navigator
        )
    }

    @AfterTest
    fun cleanup() {
        clearAndroidUiDispatcher()
        unmockkObject(LocaleConfigCompat)
    }

    // region State.languages
    @Test
    fun `State - languages`() = runTest {
        every { LocaleConfigCompat.getSupportedLocales(any()) } returns LocaleListCompat.create(Locale.ENGLISH)

        presenter.test {
            assertEquals(listOf(Locale.ENGLISH), awaitItem().languages)
        }
        verifyAll { LocaleConfigCompat.getSupportedLocales(any()) }
        navigator.assertIsEmpty()
    }

    @Test
    fun `State - languages - sorted by app language`() = runTest {
        every { LocaleConfigCompat.getSupportedLocales(any()) }
            .returns(LocaleListCompat.create(Locale.FRENCH, Locale("es")))

        presenter.test {
            appLanguage.value = Locale.ENGLISH
            assertEquals(listOf(Locale.FRENCH, Locale("es")), expectMostRecentItem().languages)

            appLanguage.value = Locale("es")
            assertEquals(listOf(Locale("es"), Locale.FRENCH), expectMostRecentItem().languages)
        }
        navigator.assertIsEmpty()
    }

    @Test
    fun `State - languages - filtered by language query`() = runTest {
        every { LocaleConfigCompat.getSupportedLocales(any()) }
            .returns(LocaleListCompat.create(Locale.ENGLISH, Locale("es")))
        appLanguage.value = Locale.ENGLISH

        presenter.test {
            val eventSink = expectMostRecentItem().eventSink

            eventSink(AppLanguageScreen.Event.UpdateLanguageQuery("Spanish"))
            assertEquals(listOf(Locale("es")), expectMostRecentItem().languages)

            eventSink(AppLanguageScreen.Event.UpdateLanguageQuery(""))
            assertEquals(listOf(Locale.ENGLISH, Locale("es")), expectMostRecentItem().languages)
        }
        navigator.assertIsEmpty()
    }
    // endregion State.languages

    // region Event.NavigateBack
    @Test
    fun `Event - NavigateBack`() = runTest {
        presenter.test {
            awaitItem().eventSink(AppLanguageScreen.Event.NavigateBack)
            navigator.awaitPop()
        }
    }
    // endregion Event.NavigateBack

    // region Event.SelectLanguage
    @Test
    fun `Event - SelectLanguage`() = runTest {
        presenter.test {
            awaitItem().eventSink(AppLanguageScreen.Event.SelectLanguage(Locale.FRENCH))

            assertEquals(Locale.FRENCH, awaitItem().selectedLanguage)
            navigator.assertIsEmpty()
        }
    }

    @Test
    fun `Event - SelectLanguage - Selected app language`() = runTest {
        presenter.test {
            awaitItem().eventSink(AppLanguageScreen.Event.SelectLanguage(Locale.ENGLISH))
            navigator.awaitPop()
        }
    }
    // endregion Event.SelectLanguage

    // region Event.ConfirmLanguage
    @Test
    fun `Event - ConfirmLanguage`() = runTest {
        presenter.test {
            expectMostRecentItem().eventSink(AppLanguageScreen.Event.SelectLanguage(Locale.FRENCH))

            with(expectMostRecentItem()) {
                val selectedLanguage = assertNotNull(selectedLanguage)
                navigator.assertIsEmpty()
                eventSink(AppLanguageScreen.Event.ConfirmLanguage(selectedLanguage))
            }

            navigator.awaitPop()
            assertNull(expectMostRecentItem().selectedLanguage)
            assertEquals(Locale.FRENCH, appLanguage.value)
        }
    }
    // endregion Event.ConfirmLanguage

    // region Event.DismissConfirmDialog
    @Test
    fun `Event - DismissConfirmDialog`() = runTest {
        presenter.test {
            expectMostRecentItem().eventSink(AppLanguageScreen.Event.SelectLanguage(Locale.FRENCH))

            with(expectMostRecentItem()) {
                assertNotNull(selectedLanguage)
                eventSink(AppLanguageScreen.Event.DismissConfirmDialog)
            }

            assertNull(expectMostRecentItem().selectedLanguage)
        }

        assertNotEquals(Locale.FRENCH, appLanguage.value)
        navigator.assertIsEmpty()
    }
    // endregion Event.DismissConfirmDialog
}
