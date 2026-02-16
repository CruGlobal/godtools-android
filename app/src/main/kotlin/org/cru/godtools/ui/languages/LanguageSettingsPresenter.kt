package org.cru.godtools.ui.languages

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import com.slack.circuit.codegen.annotations.CircuitInject
import com.slack.circuit.runtime.CircuitUiEvent
import com.slack.circuit.runtime.CircuitUiState
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.presenter.Presenter
import com.slack.circuitx.effects.ImpressionEffect
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import java.util.Locale
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.combine
import org.ccci.gto.android.common.androidx.core.app.LocaleConfigCompat
import org.ccci.gto.android.common.androidx.core.os.asIterable
import org.cru.godtools.base.Settings
import org.cru.godtools.base.ui.circuit.screen.AppLanguageScreen
import org.cru.godtools.db.repository.LanguagesRepository
import org.cru.godtools.model.Language
import org.cru.godtools.ui.languages.LanguageSettingsPresenter.UiState
import org.cru.godtools.ui.languages.downloadable.DownloadableLanguagesScreen

class LanguageSettingsPresenter @AssistedInject constructor(
    @param:ApplicationContext
    private val context: Context,
    private val settings: Settings,
    private val languagesRepository: LanguagesRepository,
    @Assisted private val navigator: Navigator,
) : Presenter<UiState> {
    data class UiState(
        val appLanguage: Locale,
        val appLanguages: Int = 0,
        val downloadedLanguages: ImmutableList<Language> = persistentListOf(),
        val eventSink: (UiEvent) -> Unit = {},
    ) : CircuitUiState

    interface UiEvent : CircuitUiEvent {
        data object NavigateUp : UiEvent
        data object AppLanguage : UiEvent
        data object DownloadableLanguages : UiEvent
    }

    @Composable
    override fun present(): UiState {
        ImpressionEffect { settings.setFeatureDiscovered(Settings.FEATURE_LANGUAGE_SETTINGS) }

        return UiState(
            appLanguage = settings.produceAppLocaleState().value,
            appLanguages = rememberAppLanguages(),
            downloadedLanguages = produceDownloadedLanguagesState().value,
            eventSink = {
                when (it) {
                    UiEvent.NavigateUp -> navigator.pop()
                    UiEvent.AppLanguage -> navigator.goTo(AppLanguageScreen)
                    UiEvent.DownloadableLanguages -> navigator.goTo(DownloadableLanguagesScreen)
                }
            }
        )
    }

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
