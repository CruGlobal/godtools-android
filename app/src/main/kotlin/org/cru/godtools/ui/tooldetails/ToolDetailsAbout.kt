package org.cru.godtools.ui.tooldetails

import android.text.util.Linkify
import androidx.annotation.VisibleForTesting
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.indication
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
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
import org.cru.godtools.model.Language.Companion.getSortedDisplayNames
import org.cru.godtools.model.getDescription
import org.cru.godtools.ui.tools.ToolViewModels

internal const val TEST_TAG_LANGUAGES_AVAILABLE = "languages_available"

@Composable
internal fun ToolDetailsAbout(toolViewModel: ToolViewModels.ToolViewModel, modifier: Modifier = Modifier) {
    val tool by toolViewModel.tool.collectAsState()
    val translation by toolViewModel.firstTranslation.collectAsState()

    ProvideTextStyle(MaterialTheme.typography.bodyMedium) {
        Column(modifier = modifier) {
            val description by remember { derivedStateOf { translation.value.getDescription(tool).orEmpty() } }
            ToolDetailsLinkifiedText(
                text = description,
                viewModel = toolViewModel,
                modifier = Modifier.padding(bottom = 32.dp)
            )

            var expandedSection by remember { mutableStateOf<ToolDetailsAboutAccordionSection?>(null) }
            val toggleSection: (ToolDetailsAboutAccordionSection) -> Unit =
                remember { { expandedSection = it.takeUnless { it == expandedSection } } }

            // Conversation Starters section
            val conversationStarters by remember {
                derivedStateOf { translation.value?.toolDetailsConversationStarters.orEmpty() }
            }
            if (conversationStarters.isNotBlank()) {
                HorizontalDivider()
                ToolDetailsAboutAccordionSection(
                    header = stringResource(R.string.tool_details_section_description_conversation_starters),
                    expanded = expandedSection == ToolDetailsAboutAccordionSection.CONVERSATION_STARTERS,
                    onToggleSection = { toggleSection(ToolDetailsAboutAccordionSection.CONVERSATION_STARTERS) },
                ) {
                    ToolDetailsLinkifiedText(conversationStarters, viewModel = toolViewModel)
                }
            }

            // Outline section
            val outline by remember { derivedStateOf { translation.value?.toolDetailsOutline.orEmpty() } }
            if (outline.isNotBlank()) {
                HorizontalDivider()
                ToolDetailsAboutAccordionSection(
                    header = stringResource(R.string.tool_details_section_description_outline),
                    expanded = expandedSection == ToolDetailsAboutAccordionSection.OUTLINE,
                    onToggleSection = { toggleSection(ToolDetailsAboutAccordionSection.OUTLINE) },
                ) {
                    ToolDetailsLinkifiedText(outline, viewModel = toolViewModel)
                }
            }

            // Bible References section
            val bibleReferences by remember {
                derivedStateOf { translation.value?.toolDetailsBibleReferences.orEmpty() }
            }
            if (bibleReferences.isNotBlank()) {
                HorizontalDivider()
                ToolDetailsAboutAccordionSection(
                    header = stringResource(R.string.tool_details_section_description_bible_references),
                    expanded = expandedSection == ToolDetailsAboutAccordionSection.BIBLE_REFERENCES,
                    onToggleSection = { toggleSection(ToolDetailsAboutAccordionSection.BIBLE_REFERENCES) },
                ) {
                    ToolDetailsLinkifiedText(bibleReferences, viewModel = toolViewModel)
                }
            }

            // Languages section
            HorizontalDivider()
            ToolDetailsLanguages(
                toolViewModel,
                expanded = expandedSection == ToolDetailsAboutAccordionSection.LANGUAGES,
                onToggleLanguages = { toggleSection(ToolDetailsAboutAccordionSection.LANGUAGES) },
            )
            HorizontalDivider()
        }
    }
}

private enum class ToolDetailsAboutAccordionSection { OUTLINE, BIBLE_REFERENCES, CONVERSATION_STARTERS, LANGUAGES }

@Composable
@VisibleForTesting
internal fun ToolDetailsLanguages(
    viewModel: ToolViewModels.ToolViewModel,
    expanded: Boolean,
    onToggleLanguages: () -> Unit,
    modifier: Modifier = Modifier
) {
    val languages by viewModel.availableLanguages.collectAsState()
    if (languages.isEmpty()) return

    val context = LocalContext.current
    val locale = LocaleCompat.getDefault(LocaleCompat.Category.DISPLAY)
    val displayLanguages by remember(context, locale) {
        derivedStateOf { languages.getSortedDisplayNames(context, locale).joinToString(", ") }
    }

    ToolDetailsAboutAccordionSection(
        header = stringResource(R.string.tool_details_section_description_languages_available),
        expanded = expanded,
        onToggleSection = onToggleLanguages,
        modifier = modifier
    ) {
        Text(
            displayLanguages,
            modifier = Modifier.testTag(TEST_TAG_LANGUAGES_AVAILABLE)
        )
    }
}

@Composable
private fun ToolDetailsAboutAccordionSection(
    header: String,
    expanded: Boolean,
    modifier: Modifier = Modifier,
    onToggleSection: () -> Unit = {},
    content: @Composable () -> Unit = {}
) = Column(modifier = modifier.fillMaxWidth()) {
    val headerInteractions = remember { MutableInteractionSource() }
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clickable(
                onClick = onToggleSection,
                interactionSource = headerInteractions,
                indication = null,
            )
            .padding(vertical = 16.dp)
    ) {
        Text(
            header,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.weight(1f)
        )
        Icon(
            if (expanded) Icons.Filled.Remove else Icons.Filled.Add,
            contentDescription = null,
            modifier = Modifier.indication(headerInteractions, rememberRipple(bounded = false, radius = 20.dp))
        )
    }
    AnimatedVisibility(visible = expanded) {
        Column {
            CompositionLocalProvider(
                LocalTextStyle provides MaterialTheme.typography.bodyMedium,
                content = content
            )
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun ToolDetailsLinkifiedText(
    text: String?,
    viewModel: ToolViewModels.ToolViewModel,
    modifier: Modifier = Modifier
) {
    val eventBus = LocalEventBus.current
    val uriHandler = LocalUriHandler.current
    val translation by viewModel.firstTranslation.collectAsState()

    val linkified = text.orEmpty().addUriAnnotations(Linkify.WEB_URLS)
    ClickableText(
        linkified,
        onClick = {
            linkified.getUriAnnotations(it, it).firstOrNull()?.let { (url) ->
                eventBus.post(ExitLinkActionEvent(viewModel.code, url, translation.value?.languageCode))
                uriHandler.openUri(url)
            }
        },
        fontFamily = translation.value?.getFontFamilyOrNull(),
        modifier = modifier
    )
}
