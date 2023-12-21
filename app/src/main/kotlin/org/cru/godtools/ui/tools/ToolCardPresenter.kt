package org.cru.godtools.ui.tools

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import org.ccci.gto.android.common.kotlin.coroutines.flow.StateFlowValue
import org.cru.godtools.base.Settings
import org.cru.godtools.base.ToolFileSystem
import org.cru.godtools.db.repository.AttachmentsRepository
import org.cru.godtools.db.repository.TranslationsRepository
import org.cru.godtools.model.Language
import org.cru.godtools.model.Tool

@Singleton
class ToolCardPresenter @Inject constructor(
    private val fileSystem: ToolFileSystem,
    private val settings: Settings,
    private val attachmentsRepository: AttachmentsRepository,
    private val translationsRepository: TranslationsRepository,
) {
    @Composable
    fun present(
        tool: Tool,
        secondLanguage: Language? = null,
        eventSink: (ToolCard.Event) -> Unit = {},
    ): ToolCard.State {
        val toolCode = tool.code

        // Tool Card Banner
        val bannerId = tool.bannerId
        val banner by remember(bannerId) {
            when {
                bannerId != null -> attachmentsRepository.findAttachmentFlow(bannerId)
                    .map { it?.takeIf { it.isDownloaded }?.getFile(fileSystem) }
                else -> flowOf(null)
            }
        }.collectAsState(null)

        // Translation
        val appLanguage by remember { settings.appLanguageFlow }.collectAsState(settings.appLanguage)
        val primaryTranslationFlow = remember(toolCode, appLanguage) {
            translationsRepository.findLatestTranslationFlow(toolCode, appLanguage)
        }
        val defaultTranslationFlow = remember(toolCode) {
            translationsRepository.findLatestTranslationFlow(toolCode, Settings.defaultLanguage).onStart { emit(null) }
        }
        val translation by remember(primaryTranslationFlow, defaultTranslationFlow) {
            combine(primaryTranslationFlow, defaultTranslationFlow) { t1, t2 -> StateFlowValue(t1 ?: t2) }
        }.collectAsState(StateFlowValue.Initial(null))

        // Second Translation
        val secondLocale = secondLanguage?.code
        val secondTranslation by remember(toolCode, secondLocale) {
            translationsRepository.findLatestTranslationFlow(toolCode, secondLocale)
        }.collectAsState(null)

        return ToolCard.State(
            tool = tool,
            isLoaded = !translation.isInitial,
            banner = banner,
            translation = translation.value,
            secondLanguage = secondLanguage,
            secondTranslation = when (secondLocale) {
                translation.value?.languageCode -> null
                else -> secondTranslation
            },
            eventSink = eventSink,
        )
    }
}
