package org.cru.godtools.ui.languages.downloadable

import android.content.Context
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import com.slack.circuit.codegen.annotations.CircuitInject
import com.slack.circuit.runtime.Navigator
import com.slack.circuit.runtime.presenter.Presenter
import dagger.assisted.Assisted
import dagger.assisted.AssistedFactory
import dagger.assisted.AssistedInject
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import java.util.Locale
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import org.ccci.gto.android.common.dagger.coroutines.DispatcherType
import org.ccci.gto.android.common.dagger.coroutines.DispatcherType.Type.IO
import org.cru.godtools.base.Settings
import org.cru.godtools.db.repository.LanguagesRepository
import org.cru.godtools.db.repository.ToolsRepository
import org.cru.godtools.model.Language
import org.cru.godtools.model.Language.Companion.filterByDisplayAndNativeName
import org.cru.godtools.model.Tool
import org.cru.godtools.ui.languages.downloadable.DownloadableLanguagesScreen.UiState
import org.cru.godtools.ui.languages.downloadable.DownloadableLanguagesScreen.UiState.UiLanguage

class DownloadableLanguagesPresenter @AssistedInject constructor(
    @ApplicationContext
    private val context: Context,
    private val languagesRepository: LanguagesRepository,
    private val settings: Settings,
    private val toolsRepository: ToolsRepository,
    @DispatcherType(IO) private val ioDispatcher: CoroutineDispatcher,
    @Assisted private val navigator: Navigator,
) : Presenter<UiState> {
    @Composable
    override fun present(): UiState {
        val coroutineScope = rememberCoroutineScope()
        val query = rememberSaveable { mutableStateOf("") }

        return UiState(
            query = query,
            languages = rememberLanguagesFlow(query).collectAsState(emptyList()).value
                .map { lang ->
                    key(lang.code) {
                        UiLanguage(
                            language = lang,
                            downloadedTools = rememberDownloadedTools(lang.code),
                            totalTools = rememberTotalTools(lang.code),
                        )
                    }
                }
                .toImmutableList(),
            eventSink = {
                when (it) {
                    UiState.UiEvent.NavigateUp -> navigator.pop()
                    is UiState.UiEvent.PinLanguage ->
                        coroutineScope.launch(NonCancellable) { languagesRepository.pinLanguage(it.locale) }
                    is UiState.UiEvent.UnpinLanguage ->
                        coroutineScope.launch(NonCancellable) { languagesRepository.unpinLanguage(it.locale) }
                }
            }
        )
    }

    @Composable
    private fun rememberLanguagesFlow(query: State<String>): Flow<List<Language>> {
        var floatedLanguages: Set<Locale>? by rememberSaveable(
            saver = Saver(
                save = { it.value?.mapTo(ArrayList()) { it.toLanguageTag() } },
                restore = { mutableStateOf(it.mapTo(mutableSetOf()) { Locale.forLanguageTag(it) }) }
            )
        ) { mutableStateOf(null) }

        return remember {
            languagesRepository.getLanguagesFlow()
                .combine(settings.appLanguageFlow) { langs, appLanguage ->
                    val floated = floatedLanguages
                        ?: langs.filter { it.isAdded }.mapTo(mutableSetOf()) { it.code }.also { floatedLanguages = it }
                    val comparator = compareByDescending<Language> { it.code in floated }
                        .then(Language.displayNameComparator(context, appLanguage))

                    langs.sortedWith(comparator) to appLanguage
                }
                .distinctUntilChanged()
                .combine(snapshotFlow { query.value }) { (langs, appLang), q ->
                    langs.filterByDisplayAndNativeName(q, context, appLang)
                }
                .flowOn(ioDispatcher)
        }
    }

    @Composable
    private fun rememberDownloadedTools(locale: Locale) = remember(locale) {
        toolsRepository.getDownloadedToolsFlowByTypesAndLanguage(Tool.Type.NORMAL_TYPES, locale)
            .map { it.size }
    }.collectAsState(0).value

    @Composable
    private fun rememberTotalTools(locale: Locale) = remember(locale) {
        toolsRepository.getNormalToolsFlowByLanguage(locale)
            .map { it.size }
    }.collectAsState(0).value

    @AssistedFactory
    @CircuitInject(DownloadableLanguagesScreen::class, SingletonComponent::class)
    interface Factory {
        fun create(navigator: Navigator): DownloadableLanguagesPresenter
    }
}
