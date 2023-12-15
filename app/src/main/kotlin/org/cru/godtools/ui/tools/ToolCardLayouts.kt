package org.cru.godtools.ui.tools

import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.CardDefaults.elevatedCardElevation
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import java.util.Locale
import org.ccci.gto.android.common.androidx.compose.foundation.layout.widthIn
import org.ccci.gto.android.common.androidx.compose.foundation.text.minLinesHeight
import org.ccci.gto.android.common.androidx.compose.ui.draw.invisibleIf
import org.cru.godtools.R
import org.cru.godtools.base.ui.theme.GodToolsTheme
import org.cru.godtools.base.ui.util.ProvideLayoutDirectionFromLocale
import org.cru.godtools.base.ui.util.getCategory
import org.cru.godtools.base.ui.util.getFontFamilyOrNull
import org.cru.godtools.base.ui.util.withCompatFontFamilyFor
import org.cru.godtools.model.Language
import org.cru.godtools.model.Tool
import org.cru.godtools.model.getName
import org.cru.godtools.model.getTagline

private val toolViewModels: ToolViewModels @Composable get() = viewModel()

private val toolCardElevation @Composable get() = elevatedCardElevation(defaultElevation = 4.dp)

private val ToolCard.State.toolNameStyle: TextStyle
    @Composable
    get() {
        val baseStyle = MaterialTheme.typography.titleMedium
        val translation by rememberUpdatedState(translation)

        return remember(baseStyle) {
            derivedStateOf {
                baseStyle
                    .withCompatFontFamilyFor(translation)
                    .merge(TextStyle(fontWeight = FontWeight.Bold))
            }
        }.value
    }

private val toolDescriptionStyle @Composable get() = MaterialTheme.typography.bodyMedium
private val toolCategoryStyle @Composable get() = MaterialTheme.typography.bodySmall
private val toolCardInfoLabelColor: Color @Composable get() {
    val baseColor = LocalContentColor.current
    return remember(baseColor) { with(baseColor) { copy(alpha = alpha * 0.6f) } }
}
private val toolCardInfoLabelStyle @Composable get() = MaterialTheme.typography.labelSmall

sealed class ToolCardEvent(
    val tool: String?,
    val toolType: Tool.Type?,
    val lang1: Locale? = null,
    val lang2: Locale? = null
) {
    class Click(tool: String?, type: Tool.Type?, lang1: Locale? = null, lang2: Locale? = null) :
        ToolCardEvent(tool, type, lang1, lang2)
    class OpenTool(tool: String?, type: Tool.Type?, lang1: Locale?, lang2: Locale? = null) :
        ToolCardEvent(tool, type, lang1, lang2)
    class OpenToolDetails(tool: String?, val additionalLocale: Locale? = null) : ToolCardEvent(tool, null)
}

@Composable
fun PreloadTool(tool: Tool) {
    val code = tool.code ?: return
    toolViewModels[code, tool]
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun LessonToolCard(
    toolCode: String,
    modifier: Modifier = Modifier,
    viewModel: ToolViewModels.ToolViewModel = toolViewModels[toolCode],
    onEvent: (ToolCardEvent) -> Unit = {},
) {
    val state = viewModel.toState()
    val tool by viewModel.tool.collectAsState()
    val translation by viewModel.firstTranslation.collectAsState()

    ProvideLayoutDirectionFromLocale(locale = { translation.value?.languageCode }) {
        ElevatedCard(
            elevation = toolCardElevation,
            onClick = { onEvent(ToolCardEvent.Click(tool?.code, tool?.type, translation.value?.languageCode)) },
            modifier = modifier.fillMaxWidth()
        ) {
            ToolBanner(state, modifier = Modifier.aspectRatio(335f / 80f))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                ToolName(state, minLines = 2, modifier = Modifier.fillMaxWidth())

                val appLanguage by viewModel.appLanguage.collectAsState()
                val appTranslation by viewModel.appTranslation.collectAsState()

                ToolCardInfoContent {
                    AvailableInLanguage(
                        language = appLanguage,
                        translation = { appTranslation.value },
                        horizontalArrangement = Arrangement.End,
                        modifier = Modifier
                            .align(Alignment.End)
                            .invisibleIf { appTranslation.isInitial || appLanguage == null }
                    )
                }
            }
        }
    }
}

@Composable
fun ToolCard(
    viewModel: ToolViewModels.ToolViewModel,
    modifier: Modifier = Modifier,
    additionalLanguage: Language? = null,
    confirmRemovalFromFavorites: Boolean = false,
    showActions: Boolean = true,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    onEvent: (ToolCardEvent) -> Unit = {},
) {
    val tool by viewModel.tool.collectAsState()
    val firstTranslation by viewModel.firstTranslation.collectAsState()

    val state = viewModel.toState(secondLanguage = additionalLanguage) {
        when (it) {
            ToolCard.Event.Click -> onEvent(
                ToolCardEvent.Click(
                    tool = tool?.code,
                    type = tool?.type,
                    lang1 = firstTranslation.value?.languageCode,
                    lang2 = additionalLanguage?.code,
                )
            )
            ToolCard.Event.OpenTool -> onEvent(
                ToolCardEvent.OpenTool(
                    tool = tool?.code,
                    type = tool?.type,
                    lang1 = firstTranslation.value?.languageCode,
                    lang2 = additionalLanguage?.code,
                )
            )
            ToolCard.Event.OpenToolDetails -> onEvent(ToolCardEvent.OpenToolDetails(tool?.code))
            ToolCard.Event.PinTool -> viewModel.pinTool()
            ToolCard.Event.UnpinTool -> viewModel.unpinTool()
        }
    }

    ToolCard(
        state = state,
        modifier = modifier,
        confirmRemovalFromFavorites = confirmRemovalFromFavorites,
        showActions = showActions,
        interactionSource = interactionSource,
    )
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun ToolCard(
    state: ToolCard.State,
    modifier: Modifier = Modifier,
    confirmRemovalFromFavorites: Boolean = false,
    showActions: Boolean = true,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
) {
    val translation by rememberUpdatedState(state.translation)
    val secondLanguage by rememberUpdatedState(state.secondLanguage)
    val downloadProgress by rememberUpdatedState(state.downloadProgress)
    val eventSink by rememberUpdatedState(state.eventSink)

    ProvideLayoutDirectionFromLocale(locale = { translation?.languageCode }) {
        ElevatedCard(
            elevation = toolCardElevation,
            interactionSource = interactionSource,
            onClick = { eventSink(ToolCard.Event.Click) },
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
@OptIn(ExperimentalMaterial3Api::class)
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
@OptIn(ExperimentalMaterial3Api::class)
internal fun VariantToolCard(
    viewModel: ToolViewModels.ToolViewModel,
    isSelected: Boolean,
    modifier: Modifier = Modifier,
    onEvent: (ToolCardEvent) -> Unit = {},
) {
    val state = viewModel.toState()
    val tool by viewModel.tool.collectAsState()
    val firstTranslation by viewModel.firstTranslation.collectAsState()

    ProvideLayoutDirectionFromLocale(locale = { firstTranslation.value?.languageCode }) {
        ElevatedCard(
            elevation = toolCardElevation,
            onClick = { onEvent(ToolCardEvent.Click(tool?.code, tool?.type)) },
            modifier = modifier
        ) {
            ToolBanner(
                state,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(335f / 87f)
            )
            Row(modifier = Modifier.padding(16.dp)) {
                RadioButton(selected = isSelected, onClick = null)

                Column(modifier = Modifier.padding(start = 16.dp)) {
                    ToolName(state)
                    Text(
                        firstTranslation.value.getTagline(tool).orEmpty(),
                        fontFamily = firstTranslation.value?.getFontFamilyOrNull(),
                        style = toolDescriptionStyle,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }
            }

            ToolCardInfoContent {
                Row(
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .align(Alignment.End)
                        .padding(16.dp)
                ) {
                    val languages by viewModel.availableLanguages.collectAsState()
                    val appTranslation by viewModel.appTranslation.collectAsState()
                    val appLanguage by viewModel.appLanguage.collectAsState()

                    val languageCount by remember { derivedStateOf { languages.size } }
                    Text(pluralStringResource(R.plurals.label_tools_languages, languageCount, languageCount))

                    Box(
                        modifier = Modifier
                            .padding(horizontal = 4.dp)
                            .size(4.dp)
                            .clip(CircleShape)
                            .background(LocalContentColor.current)
                    )

                    // TODO: I believe we need to suppress the "Unavailable in" prefix for this phrase
                    AvailableInLanguage(appLanguage, { appTranslation.value })
                }
            }
        }
    }
}

@Composable
private fun ToolBanner(state: ToolCard.State, modifier: Modifier = Modifier) = AsyncImage(
    model = state.banner,
    contentDescription = null,
    contentScale = ContentScale.Crop,
    modifier = modifier.background(GodToolsTheme.GRAY_E6)
)

@Composable
private fun ToolName(
    state: ToolCard.State,
    modifier: Modifier = Modifier,
    minLines: Int = 1,
    maxLines: Int = Int.MAX_VALUE,
) {
    val style = state.toolNameStyle

    Text(
        state.translation.getName(state.tool).orEmpty(),
        style = style,
        maxLines = maxLines,
        overflow = TextOverflow.Ellipsis,
        modifier = modifier.minLinesHeight(minLines = minLines, textStyle = style)
    )
}

@Composable
private fun ToolCategory(state: ToolCard.State, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val tool by rememberUpdatedState(state.tool)
    val translation by rememberUpdatedState(state.translation)
    val locale by remember { derivedStateOf { translation?.languageCode } }
    val category by remember(context) { derivedStateOf { tool.getCategory(context, locale) } }

    Text(
        category,
        style = toolCategoryStyle,
        maxLines = 1,
        modifier = modifier
    )
}

@Composable
private fun ToolCardInfoContent(content: @Composable () -> Unit) = CompositionLocalProvider(
    LocalContentColor provides toolCardInfoLabelColor,
    LocalTextStyle provides toolCardInfoLabelStyle,
    content = content
)

@Composable
private fun SquareToolCardSecondLanguage(state: ToolCard.State) = ToolCardInfoContent {
    val secondTranslation by rememberUpdatedState(state.secondTranslation)
    val available by remember { derivedStateOf { secondTranslation != null } }

    AvailableInLanguage(
        state.secondLanguage,
        horizontalArrangement = Arrangement.Start,
        modifier = Modifier
            .padding(top = 2.dp)
            .invisibleIf { !available }
    )
}
