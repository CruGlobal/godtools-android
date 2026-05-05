package org.cru.godtools.ui.tools

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.ElevatedCard
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.ccci.gto.android.common.androidx.compose.foundation.layout.widthIn
import org.cru.godtools.base.ui.util.ProvideLayoutDirectionFromLocale
import org.cru.godtools.ui.tools.ToolCardPresenter.UiEvent
import org.cru.godtools.ui.tools.ToolCardPresenter.UiState

@Composable
fun ToolCard(
    state: UiState,
    modifier: Modifier = Modifier,
    confirmRemovalFromFavorites: Boolean = false,
    showActions: Boolean = true,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
) {
    val secondLanguage by rememberUpdatedState(state.secondLanguage)
    val downloadProgress by rememberUpdatedState(state.downloadProgress)
    val eventSink by rememberUpdatedState(state.eventSink)

    ProvideLayoutDirectionFromLocale(locale = state.translation?.languageCode) {
        ElevatedCard(
            onClick = { eventSink(UiEvent.Click) },
            elevation = toolCardElevation,
            interactionSource = interactionSource,
            modifier = modifier
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {
                ToolBanner(
                    state,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(335f / 87f)
                )
                FavoriteAction(
                    state,
                    confirmRemoval = confirmRemovalFromFavorites,
                    modifier = Modifier.align(Alignment.TopEnd)
                )
                DownloadProgressIndicator(
                    { downloadProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .align(Alignment.BottomCenter)
                )
            }
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    ToolName(
                        state,
                        modifier = Modifier
                            .run { if (secondLanguage != null) widthIn(max = { it - 70.dp }) else this }
                            .alignByBaseline()
                    )
                    if (secondLanguage != null) {
                        ToolCardInfoContent {
                            AvailableInLanguage(
                                secondLanguage,
                                horizontalArrangement = Arrangement.End,
                                modifier = Modifier
                                    .padding(start = 8.dp)
                                    .alignByBaseline()
                            )
                        }
                    }
                }
                ToolCategory(state, modifier = Modifier.fillMaxWidth())

                if (showActions) {
                    ToolCardActions(
                        state,
                        buttonWeightFill = false,
                        buttonModifier = Modifier.widthIn(min = 92.dp),
                        modifier = Modifier
                            .padding(top = 4.dp)
                            .align(Alignment.End)
                    )
                }
            }
        }
    }
}
