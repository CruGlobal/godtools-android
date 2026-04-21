package org.cru.godtools.ui.settings.language.app

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.slack.circuit.codegen.annotations.CircuitInject
import com.slack.circuit.runtime.CircuitUiEvent
import com.slack.circuit.runtime.CircuitUiState
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.presenter.Presenter
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import java.util.Locale
import org.ccci.gto.android.common.androidx.core.app.LocaleConfigCompat
import org.ccci.gto.android.common.androidx.core.os.asIterable
import org.cru.godtools.base.Settings
import org.cru.godtools.base.ui.circuit.screen.AppLanguageScreen
import org.cru.godtools.base.util.filterByDisplayAndNativeName
import org.cru.godtools.base.util.getDisplayName
import org.cru.godtools.base.util.getPrimaryCollator
import org.cru.godtools.ui.settings.language.app.AppLanguagePresenter.UiState

class AppLanguagePresenter @AssistedInject constructor(
    @param:ApplicationContext
    private val context: Context,
    private val settings: Settings,
    @Assisted private val navigator: Navigator,
) : Presenter<UiState> {
    data class UiState(
        val languages: List<Locale> = emptyList(),
        val languageQuery: MutableState<String> = mutableStateOf(""),
        val selectedLanguage: Locale? = null,
        val eventSink: (UiEvent) -> Unit = {}
    ) : CircuitUiState

    sealed interface UiEvent : CircuitUiEvent {
        data object NavigateBack : UiEvent
        data class SelectLanguage(val language: Locale) : UiEvent
        data class ConfirmLanguage(val language: Locale) : UiEvent
        data object DismissConfirmDialog : UiEvent
    }

    @Composable
    override fun present(): UiState {
        val appLocale by settings.produceAppLocaleState()
        val languageQuery = remember { mutableStateOf("") }
        var confirmLanguage: Locale? by rememberSaveable { mutableStateOf(null) }

        val eventSink: (UiEvent) -> Unit = remember {
            {
                when (it) {
                    UiEvent.NavigateBack -> navigator.pop(AppLanguageScreen.Result.Dismiss)

                    is UiEvent.SelectLanguage -> {
                        if (it.language == appLocale) {
                            navigator.pop(AppLanguageScreen.Result.Dismiss)
                        } else {
                            confirmLanguage = it.language
                        }
                    }

                    is UiEvent.ConfirmLanguage -> {
                        settings.appLanguage = it.language
                        confirmLanguage = null
                        navigator.pop(AppLanguageScreen.Result.LanguageSelected)
                    }

                    UiEvent.DismissConfirmDialog -> confirmLanguage = null
                }
            }
        }

        return UiState(
            languages = rememberLanguages(appLocale, languageQuery.value),
            languageQuery = languageQuery,
            selectedLanguage = confirmLanguage,
            eventSink = eventSink,
        )
    }

    @Composable
    private fun rememberLanguages(appLanguage: Locale, query: String): List<Locale> {
        val languages = remember { LocaleConfigCompat.getSupportedLocales(context)?.asIterable() ?: emptyList() }
        val sortedLanguages = remember(appLanguage) {
            languages.sortedWith(
                compareBy(appLanguage.getPrimaryCollator()) { it.getDisplayName(context, inLocale = appLanguage) }
            )
        }

        return remember(sortedLanguages, appLanguage, query) {
            sortedLanguages.filterByDisplayAndNativeName(query, context, inLocale = appLanguage)
        }
    }

    @AssistedFactory
    @CircuitInject(AppLanguageScreen::class, SingletonComponent::class)
    interface Factory {
        fun create(navigator: Navigator): AppLanguagePresenter
    }
}
