package org.cru.godtools.ui.tools

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.launch
import org.ccci.gto.android.common.kotlin.coroutines.flow.StateFlowValue
import org.cru.godtools.base.Settings
import org.cru.godtools.base.ToolFileSystem
import org.cru.godtools.base.produceAppLocaleState
import org.cru.godtools.db.repository.AttachmentsRepository
import org.cru.godtools.db.repository.ToolsRepository
import org.cru.godtools.db.repository.TranslationsRepository
import org.cru.godtools.db.repository.produceLatestTranslationState
import org.cru.godtools.db.repository.rememberAttachmentFile
import org.cru.godtools.model.Language
import org.cru.godtools.model.Tool

@Singleton
class ToolCardPresenter @Inject constructor(
    private val fileSystem: ToolFileSystem,
    private val settings: Settings,
    private val attachmentsRepository: AttachmentsRepository,
    private val toolsRepository: ToolsRepository,
    private val translationsRepository: TranslationsRepository,
) {
    @Composable
    fun present(
        tool: Tool,
        secondLanguage: Language? = null,
        eventSink: (ToolCard.Event) -> Unit = {},
    ): ToolCard.State {
        val toolCode = tool.code
        val defaultLocale = tool.defaultLocale
        val coroutineScope = rememberCoroutineScope()

        // Translation
        val appLocale by settings.produceAppLocaleState()
        val primaryTranslationFlow = remember(toolCode, appLocale) {
            translationsRepository.findLatestTranslationFlow(toolCode, appLocale)
        }
        val defaultTranslationFlow = remember(toolCode, defaultLocale) {
            translationsRepository.findLatestTranslationFlow(toolCode, defaultLocale).onStart { emit(null) }
        }
        val translation by remember(primaryTranslationFlow, defaultTranslationFlow) {
            combine(primaryTranslationFlow, defaultTranslationFlow) { t1, t2 -> StateFlowValue(t1 ?: t2) }
        }.collectAsState(StateFlowValue.Initial(null))

        // Second Translation
        val secondTranslation by translationsRepository.produceLatestTranslationState(toolCode, secondLanguage?.code)

        // eventSink
        val interceptingEventSink: (ToolCard.Event) -> Unit = remember(eventSink) {
            {
                when (it) {
                    ToolCard.Event.PinTool ->
                        coroutineScope.launch(NonCancellable) { toolCode?.let { toolsRepository.pinTool(toolCode) } }
                    ToolCard.Event.UnpinTool ->
                        coroutineScope.launch(NonCancellable) { toolCode?.let { toolsRepository.unpinTool(toolCode) } }
                    else -> eventSink(it)
                }
            }
        }

        return ToolCard.State(
            tool = tool,
            isLoaded = !translation.isInitial,
            banner = attachmentsRepository.rememberAttachmentFile(fileSystem, tool.bannerId),
            translation = translation.value,
            secondLanguage = secondLanguage,
            secondTranslation = when (secondLanguage?.code) {
                translation.value?.languageCode -> null
                else -> secondTranslation
            },
            eventSink = interceptingEventSink,
        )
    }
}
