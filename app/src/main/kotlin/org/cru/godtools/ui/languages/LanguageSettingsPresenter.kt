package org.cru.godtools.ui.languages

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import com.slack.circuit.codegen.annotations.CircuitInject
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.presenter.Presenter
import com.slack.circuitx.android.IntentScreen
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.combine
import org.ccci.gto.android.common.androidx.core.app.LocaleConfigCompat
import org.ccci.gto.android.common.androidx.core.os.asIterable
import org.cru.godtools.base.Settings
import org.cru.godtools.base.produceAppLocaleState
import org.cru.godtools.db.repository.LanguagesRepository
import org.cru.godtools.model.Language
import org.cru.godtools.ui.drawer.DrawerMenuPresenter
import org.cru.godtools.ui.languages.LanguageSettingsScreen.Event
import org.cru.godtools.ui.languages.LanguageSettingsScreen.State
import org.cru.godtools.ui.languages.app.AppLanguageScreen
import org.cru.godtools.ui.languages.downloadable.createDownloadableLanguagesIntent

class LanguageSettingsPresenter @AssistedInject constructor(
    @ApplicationContext
    private val context: Context,
    private val settings: Settings,
    private val languagesRepository: LanguagesRepository,
    private val drawerMenuPresenter: DrawerMenuPresenter,
    @Assisted private val navigator: Navigator,
) : Presenter<State> {
    @Composable
    override fun present() = State(
        appLanguage = settings.produceAppLocaleState().value,
        appLanguages = rememberAppLanguages(),
        downloadedLanguages = produceDownloadedLanguagesState().value,
        drawerState = drawerMenuPresenter.present(),
        eventSink = {
            when (it) {
                Event.NavigateUp -> navigator.pop()
                Event.AppLanguage -> navigator.goTo(AppLanguageScreen)
                Event.DownloadableLanguages ->
                    navigator.goTo(IntentScreen(context.createDownloadableLanguagesIntent()))
            }
        }
    )

    @Composable
    private fun rememberAppLanguages() =
        remember { LocaleConfigCompat.getSupportedLocales(context)?.asIterable()?.count() ?: 0 }

    @Composable
    private fun produceDownloadedLanguagesState() = remember {
        combine(languagesRepository.getPinnedLanguagesFlow(), settings.appLanguageFlow) { pinned, app ->
            pinned.sortedWith(Language.displayNameComparator(context, app))
                .toImmutableList()
        }
    }.collectAsState(persistentListOf())

    @AssistedFactory
    @CircuitInject(LanguageSettingsScreen::class, SingletonComponent::class)
    interface Factory {
        fun create(navigator: Navigator): LanguageSettingsPresenter
    }
}
