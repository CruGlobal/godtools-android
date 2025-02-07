package org.cru.godtools.ui.tools

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ElevatedCard
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.ccci.gto.android.common.androidx.compose.ui.draw.invisibleIf
import org.cru.godtools.base.ui.util.ProvideLayoutDirectionFromLocale

@Composable
fun LessonToolCard(state: ToolCard.State, modifier: Modifier = Modifier) {
    val isLoaded by rememberUpdatedState(state.isLoaded)
    val translation by rememberUpdatedState(state.translation)
    val language by rememberUpdatedState(state.language)
    val languageAvailable by rememberUpdatedState(state.languageAvailable)
    val eventSink by rememberUpdatedState(state.eventSink)

    ProvideLayoutDirectionFromLocale(locale = { translation?.languageCode }) {
        ElevatedCard(
            onClick = { eventSink(ToolCard.Event.Click) },
            elevation = toolCardElevation,
            modifier = modifier.fillMaxWidth()
        ) {
            ToolBanner(state, modifier = Modifier.aspectRatio(335f / 80f))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                ToolName(state, minLines = 2, modifier = Modifier.fillMaxWidth())

                ToolCardInfoContent {
                    AvailableInLanguage(
                        language = language,
                        available = languageAvailable,
                        horizontalArrangement = Arrangement.End,
                        modifier = Modifier
                            .align(Alignment.End)
                            .invisibleIf { !isLoaded || language == null }
                    )
                }
            }
        }
    }
}
