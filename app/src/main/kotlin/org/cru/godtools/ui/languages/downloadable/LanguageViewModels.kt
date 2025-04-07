package org.cru.godtools.ui.languages.downloadable

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.Locale
import javax.inject.Inject
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import org.cru.godtools.db.repository.LanguagesRepository
import org.cru.godtools.db.repository.ToolsRepository
import org.cru.godtools.model.Language
import org.cru.godtools.model.Tool
import org.cru.godtools.ui.languages.downloadable.DownloadableLanguagesScreen.UiState.UiLanguage

@HiltViewModel
class LanguageViewModels @Inject constructor(
    private val languagesRepository: LanguagesRepository,
    private val toolsRepository: ToolsRepository,
) : ViewModel() {
    private val viewModels = mutableMapOf<Locale, LanguageViewModel>()
    fun get(language: Language) = viewModels.getOrPut(language.code) { LanguageViewModel(language) }
        .also { it.language.value = language }

    inner class LanguageViewModel(language: Language) {
        val code = language.code
        val language = MutableStateFlow(language)

        val numberOfTools = toolsRepository.getNormalToolsFlowByLanguage(code)
            .map { it.size }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)
        val toolsDownloaded = toolsRepository.getDownloadedToolsFlowByTypesAndLanguage(Tool.Type.NORMAL_TYPES, code)
            .map { it.size }
            .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), 0)

        @Composable
        fun toState() = UiLanguage(
            language = language.collectAsState().value,
            downloadedTools = toolsDownloaded.collectAsState().value,
            totalTools = numberOfTools.collectAsState().value,
            eventSink = {
                when (it) {
                    UiLanguage.UiEvent.PinLanguage -> viewModelScope.launch(NonCancellable) {
                        languagesRepository.pinLanguage(code)
                    }
                    UiLanguage.UiEvent.UnpinLanguage -> viewModelScope.launch(NonCancellable) {
                        languagesRepository.unpinLanguage(code)
                    }
                }
            }
        )
    }
}
