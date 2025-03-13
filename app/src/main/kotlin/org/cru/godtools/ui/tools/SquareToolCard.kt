package org.cru.godtools.ui.tools

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.invisibleToUser
import androidx.compose.ui.unit.dp
import org.ccci.gto.android.common.androidx.compose.ui.draw.invisibleIf
import org.cru.godtools.base.ui.util.ProvideLayoutDirectionFromLocale

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

    ProvideLayoutDirectionFromLocale(locale = state.translation?.languageCode) {
        ElevatedCard(
            onClick = { eventSink(ToolCard.Event.Click) },
            elevation = toolCardElevation,
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
                    @OptIn(ExperimentalComposeUiApi::class)
                    Column(modifier = Modifier.invisibleIf(true)) {
                        Text(
                            "",
                            minLines = 2,
                            style = state.toolNameStyle,
                            modifier = Modifier.clearAndSetSemantics { invisibleToUser() }
                        )
                        if (showCategory) {
                            Text(
                                "",
                                minLines = 1,
                                style = toolCategoryStyle,
                                modifier = Modifier
                                    .padding(top = 2.dp)
                                    .clearAndSetSemantics { invisibleToUser() }
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
