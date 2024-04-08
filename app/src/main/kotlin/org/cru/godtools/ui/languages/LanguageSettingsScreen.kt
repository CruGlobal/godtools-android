package org.cru.godtools.ui.languages

import com.slack.circuit.runtime.CircuitUiEvent
import com.slack.circuit.runtime.CircuitUiState
import com.slack.circuit.runtime.screen.Screen
import java.util.Locale
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.parcelize.Parcelize
import org.cru.godtools.model.Language
import org.cru.godtools.ui.drawer.DrawerMenuScreen

@Parcelize
object LanguageSettingsScreen : Screen {
    data class State(
        val appLanguage: Locale,
        val appLanguages: Int = 0,
        val downloadedLanguages: ImmutableList<Language> = persistentListOf(),
        val drawerState: DrawerMenuScreen.State = DrawerMenuScreen.State(),
        val eventSink: (Event) -> Unit = {},
    ) : CircuitUiState

    interface Event : CircuitUiEvent {
        data object NavigateUp : Event
        data object AppLanguage : Event
        data object DownloadableLanguages : Event
    }
}
