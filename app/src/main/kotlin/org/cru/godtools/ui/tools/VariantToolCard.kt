package org.cru.godtools.ui.tools

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.unit.dp
import org.cru.godtools.R
import org.cru.godtools.base.ui.util.ProvideLayoutDirectionFromLocale
import org.cru.godtools.base.ui.util.getFontFamilyOrNull
import org.cru.godtools.model.getTagline

@Composable
internal fun VariantToolCard(state: ToolCard.State, modifier: Modifier = Modifier, isSelected: Boolean = false) {
    val tool by rememberUpdatedState(state.tool)
    val translation by rememberUpdatedState(state.translation)
    val appLanguage by rememberUpdatedState(state.appLanguage)
    val appTranslation by rememberUpdatedState(state.appTranslation)
    val secondLanguage by rememberUpdatedState(state.secondLanguage)
    val secondLanguageAvailable by rememberUpdatedState(state.secondLanguageAvailable)
    val languageCount by rememberUpdatedState(state.availableLanguages)

    val eventSink by rememberUpdatedState(state.eventSink)

    ProvideLayoutDirectionFromLocale(locale = { translation?.languageCode }) {
        ElevatedCard(
            elevation = toolCardElevation,
            onClick = { eventSink(ToolCard.Event.Click) },
            modifier = modifier
        ) {
            ToolBanner(
                state,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(335f / 87f)
            )
            Row(modifier = Modifier.padding(16.dp)) {
                RadioButton(selected = isSelected, onClick = { eventSink(ToolCard.Event.Click) })

                Column(modifier = Modifier.padding(start = 16.dp)) {
                    ToolName(state)
                    Text(
                        translation.getTagline(tool).orEmpty(),
                        fontFamily = translation?.getFontFamilyOrNull(),
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            ToolCardInfoContent {
                Column(
                    horizontalAlignment = Alignment.End,
                    modifier = Modifier
                        .align(Alignment.End)
                        .padding(16.dp)
                ) {
                    Text(pluralStringResource(R.plurals.label_tools_languages, languageCount, languageCount))

                    // TODO: I believe we need to suppress the "Unavailable in" prefix for this phrase
                    AvailableInLanguage(appLanguage, available = appTranslation != null)

                    if (secondLanguage != null) {
                        // TODO: I believe we need to suppress the "Unavailable in" prefix for this phrase
                        AvailableInLanguage(secondLanguage, available = secondLanguageAvailable)
                    }
                }
            }
        }
    }
}
