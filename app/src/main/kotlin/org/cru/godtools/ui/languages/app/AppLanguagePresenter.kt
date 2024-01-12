package org.cru.godtools.ui.languages.app

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.slack.circuit.codegen.annotations.CircuitInject
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
import org.cru.godtools.base.util.getPrimaryCollator

class AppLanguagePresenter @AssistedInject constructor(
    @ApplicationContext
    private val context: Context,
    private val settings: Settings,
    @Assisted private val navigator: Navigator,
) : Presenter<AppLanguageScreen.State> {
    @Composable
    override fun present(): AppLanguageScreen.State {
        val appLanguage by settings.appLanguageFlow.collectAsState(settings.appLanguage)
        var confirmLanguage: Locale? by rememberSaveable { mutableStateOf(null) }

        val eventSink: (AppLanguageScreen.Event) -> Unit = remember {
            {
                when (it) {
                    AppLanguageScreen.Event.NavigateBack -> navigator.pop()

                    is AppLanguageScreen.Event.SelectLanguage -> {
                        if (it.language == appLanguage) {
                            navigator.pop()
                        } else {
                            confirmLanguage = it.language
                        }
                    }

                    AppLanguageScreen.Event.DismissConfirmDialog -> confirmLanguage = null
                    is AppLanguageScreen.Event.ConfirmLanguage -> {
                        settings.appLanguage = it.language
                        confirmLanguage = null
                        navigator.pop()
                    }
                }
            }
        }

        return AppLanguageScreen.State(
            selectedLanguage = confirmLanguage,
            languages = rememberLanguages(appLanguage),
            eventSink = eventSink,
        )
    }

    @Composable
    private fun rememberLanguages(appLanguage: Locale): List<Locale> {
        val languages = remember { LocaleConfigCompat.getSupportedLocales(context)?.asIterable() ?: emptyList() }

        return remember(appLanguage) {
            languages.sortedWith(compareBy(appLanguage.getPrimaryCollator()) { it.getDisplayName(appLanguage) })
        }
    }

    @AssistedFactory
    @CircuitInject(AppLanguageScreen::class, SingletonComponent::class)
    interface Factory {
        fun create(navigator: Navigator): AppLanguagePresenter
    }
}
