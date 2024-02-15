package org.cru.godtools.ui.tools

import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.CardDefaults.elevatedCardElevation
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
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
import org.cru.godtools.base.ui.theme.GodToolsTheme
import org.cru.godtools.base.ui.util.ProvideLayoutDirectionFromLocale
import org.cru.godtools.base.ui.util.getCategory
import org.cru.godtools.base.ui.util.withCompatFontFamilyFor
import org.cru.godtools.model.Language
import org.cru.godtools.model.Tool
import org.cru.godtools.model.getName

internal const val TEST_TAG_TOOL_CATEGORY = "tool_category"

internal val toolViewModels: ToolViewModels @Composable get() = viewModel()

internal val toolCardElevation @Composable get() = elevatedCardElevation(defaultElevation = 4.dp)

internal val ToolCard.State.toolNameStyle: TextStyle
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

internal val toolCategoryStyle @Composable get() = MaterialTheme.typography.bodySmall
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
internal fun ToolBanner(state: ToolCard.State, modifier: Modifier = Modifier) = AsyncImage(
    model = state.banner,
    contentDescription = null,
    contentScale = ContentScale.Crop,
    modifier = modifier.background(GodToolsTheme.GRAY_E6)
)

@Composable
internal fun ToolName(
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
internal fun ToolCategory(state: ToolCard.State, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val tool by rememberUpdatedState(state.tool)
    val translation by rememberUpdatedState(state.translation)
    val locale by remember { derivedStateOf { translation?.languageCode } }
    val category by remember(context) { derivedStateOf { tool.getCategory(context, locale) } }

    Text(
        category,
        style = toolCategoryStyle,
        maxLines = 1,
        modifier = modifier.testTag(TEST_TAG_TOOL_CATEGORY)
    )
}

@Composable
internal fun ToolCardInfoContent(content: @Composable () -> Unit) = CompositionLocalProvider(
    LocalContentColor provides toolCardInfoLabelColor,
    LocalTextStyle provides toolCardInfoLabelStyle,
    content = content
)
