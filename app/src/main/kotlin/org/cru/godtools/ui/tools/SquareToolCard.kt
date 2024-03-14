package org.cru.godtools.ui.tools

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ElevatedCard
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.ccci.gto.android.common.androidx.compose.foundation.text.minLinesHeight
import org.ccci.gto.android.common.androidx.compose.ui.draw.invisibleIf
import org.cru.godtools.base.ui.util.ProvideLayoutDirectionFromLocale

@Composable
fun SquareToolCard(
    toolCode: String,
    modifier: Modifier = Modifier,
    viewModel: ToolViewModels.ToolViewModel = toolViewModels[toolCode],
    showCategory: Boolean = true,
    showSecondLanguage: Boolean = false,
    showActions: Boolean = true,
    floatParallelLanguageUp: Boolean = true,
    confirmRemovalFromFavorites: Boolean = false,
    onEvent: (ToolCardEvent) -> Unit = {},
) {
    val tool by viewModel.tool.collectAsState()
    val firstTranslation by viewModel.firstTranslation.collectAsState()
    val secondTranslation by viewModel.secondTranslation.collectAsState()

    val eventSink: (ToolCard.Event) -> Unit = remember(viewModel) {
        {
            when (it) {
                ToolCard.Event.Click -> onEvent(
                    ToolCardEvent.Click(
                        tool = tool?.code,
                        type = tool?.type,
                        lang1 = firstTranslation.value?.languageCode,
                        lang2 = secondTranslation?.languageCode
                    )
                )
                ToolCard.Event.OpenTool -> onEvent(
                    ToolCardEvent.OpenTool(
                        tool = tool?.code,
                        type = tool?.type,
                        lang1 = firstTranslation.value?.languageCode,
                        lang2 = secondTranslation?.languageCode
                    )
                )
                ToolCard.Event.OpenToolDetails -> onEvent(ToolCardEvent.OpenToolDetails(toolCode))
                ToolCard.Event.PinTool -> viewModel.pinTool()
                ToolCard.Event.UnpinTool -> viewModel.unpinTool()
            }
        }
    }

    SquareToolCard(
        state = viewModel.toState(eventSink = eventSink),
        modifier = modifier,
        showCategory = showCategory,
        showSecondLanguage = showSecondLanguage,
        showActions = showActions,
        floatParallelLanguageUp = floatParallelLanguageUp,
        confirmRemovalFromFavorites = confirmRemovalFromFavorites,
    )
}

@Composable
fun SquareToolCard(
    state: ToolCard.State,
    modifier: Modifier = Modifier,
    showCategory: Boolean = true,
    showSecondLanguage: Boolean = false,
    showActions: Boolean = true,
    floatParallelLanguageUp: Boolean = true,
    confirmRemovalFromFavorites: Boolean = false,
) {
    val downloadProgress by rememberUpdatedState(state.downloadProgress)
    val eventSink by rememberUpdatedState(state.eventSink)

    ProvideLayoutDirectionFromLocale(locale = { state.translation?.languageCode }) {
        ElevatedCard(
            elevation = toolCardElevation,
            onClick = { eventSink(ToolCard.Event.Click) },
            modifier = modifier.width(189.dp)
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {
                ToolBanner(
                    state = state,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(189f / 128f)
                )
                FavoriteAction(
                    state = state,
                    modifier = Modifier.align(Alignment.TopEnd),
                    confirmRemoval = confirmRemovalFromFavorites
                )
                DownloadProgressIndicator(
                    { downloadProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                )
            }
            Column(modifier = Modifier.padding(16.dp)) {
                Box {
                    Column {
                        ToolName(state, minLines = 1, maxLines = 2, modifier = Modifier.fillMaxWidth())
                        if (showCategory) {
                            ToolCategory(
                                state,
                                modifier = Modifier
                                    .padding(top = 2.dp)
                                    .fillMaxWidth()
                            )
                        }
                        if (showSecondLanguage && floatParallelLanguageUp) SquareToolCardSecondLanguage(state)
                    }

                    // Reserve the maximum height consistently across all cards
                    Column(modifier = Modifier.invisibleIf(true)) {
                        Spacer(modifier = Modifier.minLinesHeight(2, state.toolNameStyle))
                        if (showCategory) {
                            Spacer(
                                modifier = Modifier
                                    .padding(top = 2.dp)
                                    .minLinesHeight(1, toolCategoryStyle)
                            )
                        }
                        if (showSecondLanguage && floatParallelLanguageUp) SquareToolCardSecondLanguage(state)
                    }
                }
                if (showSecondLanguage && !floatParallelLanguageUp) SquareToolCardSecondLanguage(state)

                if (showActions) {
                    ToolCardActions(
                        state,
                        modifier = Modifier.padding(top = 8.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun SquareToolCardSecondLanguage(state: ToolCard.State) = ToolCardInfoContent {
    val available by rememberUpdatedState(state.secondLanguageAvailable)

    AvailableInLanguage(
        state.secondLanguage,
        horizontalArrangement = Arrangement.Start,
        modifier = Modifier
            .padding(top = 2.dp)
            .invisibleIf { !available }
    )
}
