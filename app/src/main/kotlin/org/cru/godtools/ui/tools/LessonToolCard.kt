package org.cru.godtools.ui.tools

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import org.ccci.gto.android.common.androidx.compose.ui.draw.invisibleIf
import org.cru.godtools.R
import org.cru.godtools.base.ui.util.ProvideLayoutDirectionFromLocale
import org.cru.godtools.ui.tools.ToolCard.State.Progress

@Composable
fun LessonToolCard(
    state: ToolCard.State,
    modifier: Modifier = Modifier,
    showLanguage: Boolean = false,
    showProgress: Boolean = false,
) {
    val isLoaded by rememberUpdatedState(state.isLoaded)
    val language by rememberUpdatedState(state.language)
    val languageAvailable by rememberUpdatedState(state.languageAvailable)
    val eventSink by rememberUpdatedState(state.eventSink)

    ElevatedCard(
        onClick = { eventSink(ToolCard.Event.Click) },
        elevation = toolCardElevation,
        modifier = modifier.fillMaxWidth()
    ) {
        ToolBanner(state, modifier = Modifier.aspectRatio(335f / 80f))

        Column(
            verticalArrangement = Arrangement.spacedBy(4.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            ProvideLayoutDirectionFromLocale(locale = state.translation?.languageCode) {
                ToolName(state, minLines = 2, modifier = Modifier.fillMaxWidth())
            }

            if (showProgress) {
                val progress by animateFloatAsState(state.progress?.progress?.toFloat() ?: 0f)
                LinearProgressIndicator(
                    { progress },
                    gapSize = 0.dp,
                    drawStopIndicator = {},
                    modifier = Modifier
                        .fillMaxWidth()
                        .invisibleIf { state.progress !is Progress.InProgress }
                )
            }

            if (showProgress || showLanguage) {
                ToolCardInfoContent {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        if (showProgress) {
                            Text(
                                when (state.progress) {
                                    null -> ""
                                    is Progress.InProgress -> stringResource(
                                        R.string.dashboard_lessons_progress_in_progress,
                                        (state.progress.progress * 100).toInt().coerceIn(0, 100)
                                    )
                                    Progress.Completed -> stringResource(
                                        R.string.dashboard_lessons_progress_completed
                                    )
                                },
                                color = MaterialTheme.colorScheme.primary,
                            )
                        }

                        Spacer(Modifier.weight(1f))

                        if (showLanguage) {
                            AvailableInLanguage(
                                language = language,
                                available = languageAvailable,
                                horizontalArrangement = Arrangement.End,
                                modifier = Modifier.invisibleIf { !isLoaded || language == null }
                            )
                        }
                    }
                }
            }
        }
    }
}
