package org.cru.godtools.ui.languages

import android.app.Application
import android.content.Context
import androidx.core.os.LocaleListCompat
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.jeppeman.mockposable.mockk.everyComposable
import com.slack.circuit.test.FakeNavigator
import com.slack.circuit.test.test
import com.slack.circuitx.android.IntentScreen
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import java.util.Locale
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.assertTrue
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.runTest
import org.ccci.gto.android.common.androidx.core.app.LocaleConfigCompat
import org.ccci.gto.android.common.util.os.equalsBundle
import org.cru.godtools.base.Settings
import org.cru.godtools.db.repository.LanguagesRepository
import org.cru.godtools.model.Language
import org.cru.godtools.ui.drawer.DrawerMenuPresenter
import org.cru.godtools.ui.drawer.DrawerMenuScreen
import org.cru.godtools.ui.languages.LanguageSettingsScreen.Event
import org.cru.godtools.ui.languages.app.AppLanguageScreen
import org.cru.godtools.ui.languages.downloadable.createDownloadableLanguagesIntent
import org.junit.runner.RunWith
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@Config(application = Application::class)
class LanguageSettingsPresenterTest {
    private val appLanguage = MutableStateFlow(Locale.ENGLISH)
    private val pinnedLanguages = MutableStateFlow(emptyList<Language>())

    private val context: Context get() = ApplicationProvider.getApplicationContext()
    private val navigator = FakeNavigator(LanguageSettingsScreen)
    private val languagesRepository: LanguagesRepository = mockk {
        every { getPinnedLanguagesFlow() } returns pinnedLanguages
    }
    private val drawerMenuPresenter: DrawerMenuPresenter = mockk {
        everyComposable { present() } returns DrawerMenuScreen.State()
    }
    private val settings: Settings = mockk {
        every { appLanguage } returns this@LanguageSettingsPresenterTest.appLanguage.value
        every { appLanguageFlow } returns this@LanguageSettingsPresenterTest.appLanguage
    }

    private lateinit var presenter: LanguageSettingsPresenter

    @BeforeTest
    fun setup() {
        mockkObject(LocaleConfigCompat)
        every { LocaleConfigCompat.getSupportedLocales(any()) } returns LocaleListCompat.getEmptyLocaleList()

        presenter = LanguageSettingsPresenter(
            context = context,
            settings = settings,
            languagesRepository = languagesRepository,
            drawerMenuPresenter = drawerMenuPresenter,
            navigator = navigator
        )
    }

    @Test
    fun `State - appLanguage`() = runTest {
        presenter.test {
            assertEquals(Locale.ENGLISH, expectMostRecentItem().appLanguage)

            appLanguage.value = Locale.FRENCH
            assertEquals(Locale.FRENCH, expectMostRecentItem().appLanguage)
        }
    }

    // region State.appLanguages
    @Test
    fun `State - appLanguages`() = runTest {
        every { LocaleConfigCompat.getSupportedLocales(any()) }
            .returns(LocaleListCompat.create(Locale.ENGLISH, Locale.FRENCH, Locale.GERMAN))

        presenter.test {
            assertEquals(3, expectMostRecentItem().appLanguages)
        }
    }

    @Test
    fun `State - appLanguages - Unable to load`() = runTest {
        every { LocaleConfigCompat.getSupportedLocales(any()) } returns null

        presenter.test {
            assertEquals(0, expectMostRecentItem().appLanguages)
        }
    }
    // endregion State.appLanguages

    // region State.downloadedLanguages
    @Test
    fun `State - downloadedLanguages`() = runTest {
        presenter.test {
            assertEquals(emptyList(), expectMostRecentItem().downloadedLanguages)

            val languages = listOf(Language(Locale.ENGLISH), Language(Locale.FRENCH))
            pinnedLanguages.value = languages
            assertEquals(languages, expectMostRecentItem().downloadedLanguages)
        }
    }
    // endregion State.downloadedLanguages

    // region Event.AppLanguage
    @Test
    fun `Event - AppLanguage`() = runTest {
        presenter.test {
            expectMostRecentItem().eventSink(Event.AppLanguage)
            assertEquals(AppLanguageScreen, navigator.awaitNextScreen())
        }
    }
    // endregion Event.AppLanguage

    // region Event.DownloadableLanguages
    @Test
    fun `Event - DownloadableLanguages`() = runTest {
        presenter.test {
            expectMostRecentItem().eventSink(Event.DownloadableLanguages)

            with(navigator.awaitNextScreen()) {
                assertIs<IntentScreen>(this)
                val expected = context.createDownloadableLanguagesIntent()
                assertEquals(expected.component, intent.component)
                assertTrue(expected.extras equalsBundle intent.extras)
            }
        }
    }
    // endregion Event.DownloadableLanguages
}
