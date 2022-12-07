package org.cru.godtools.ui.tooldetails

import android.text.util.Linkify
import androidx.annotation.VisibleForTesting
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Divider
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.ccci.gto.android.common.androidx.compose.foundation.layout.padding
import org.ccci.gto.android.common.androidx.compose.material3.ClickableText
import org.ccci.gto.android.common.androidx.compose.material3.ui.text.addUriAnnotations
import org.ccci.gto.android.common.androidx.compose.ui.text.getUriAnnotations
import org.ccci.gto.android.common.compat.util.LocaleCompat
import org.cru.godtools.R
import org.cru.godtools.analytics.model.ExitLinkActionEvent
import org.cru.godtools.base.ui.compose.LocalEventBus
import org.cru.godtools.base.ui.util.getFontFamilyOrNull
import org.cru.godtools.model.getDescription
import org.cru.godtools.model.getSortedDisplayNames
import org.cru.godtools.ui.tools.ToolViewModels

internal const val TEST_TAG_LANGUAGES_AVAILABLE = "languages_available"

@Composable
internal fun ToolDetailsAbout(toolViewModel: ToolViewModels.ToolViewModel, modifier: Modifier = Modifier) {
    val eventBus = LocalEventBus.current
    val uriHandler = LocalUriHandler.current
    val tool by toolViewModel.tool.collectAsState()
    val translation by toolViewModel.firstTranslation.collectAsState()

    ProvideTextStyle(MaterialTheme.typography.bodyMedium) {
        Column(modifier = modifier) {
            val description = remember { derivedStateOf { translation.value.getDescription(tool).orEmpty() } }.value
                .addUriAnnotations(Linkify.WEB_URLS)
            ClickableText(
                description,
                onClick = {
                    description.getUriAnnotations(it, it).firstOrNull()?.let { (url) ->
                        eventBus.post(ExitLinkActionEvent(toolViewModel.code, url, translation.value?.languageCode))
                        uriHandler.openUri(url)
                    }
                },
                fontFamily = translation.value?.getFontFamilyOrNull(),
            )

            ToolDetailsLanguages(toolViewModel, modifier = Modifier.padding(top = 48.dp))
        }
    }
}

@Composable
@VisibleForTesting
internal fun ToolDetailsLanguages(viewModel: ToolViewModels.ToolViewModel, modifier: Modifier = Modifier) {
    val languages by viewModel.availableLanguages.collectAsState()
    if (languages.isEmpty()) return

    val context = LocalContext.current
    val locale = LocaleCompat.getDefault(LocaleCompat.Category.DISPLAY)
    val displayLanguages by remember(context, locale) {
        derivedStateOf { languages.getSortedDisplayNames(context, locale).joinToString(", ") }
    }

    Column(modifier = modifier.fillMaxWidth()) {
        Text(
            stringResource(R.string.tool_details_section_description_languages_available),
            fontWeight = FontWeight.Bold
        )
        Divider(color = LocalContentColor.current.copy(alpha = 0.12f))
        Text(
            displayLanguages,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier
                .testTag(TEST_TAG_LANGUAGES_AVAILABLE)
                .padding(top = 8.dp)
        )
    }
}
