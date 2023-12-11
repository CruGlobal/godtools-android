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
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
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
import org.cru.godtools.model.Language
import org.cru.godtools.model.Tool
import org.cru.godtools.model.getName
import org.cru.godtools.model.getTagline

private val toolViewModels: ToolViewModels @Composable get() = viewModel()

private val toolCardElevation @Composable get() = elevatedCardElevation(defaultElevation = 4.dp)

@Composable
private fun toolNameStyle(viewModel: ToolViewModels.ToolViewModel): State<TextStyle> {
    val translation by viewModel.firstTranslation.collectAsState()

    val baseStyle = MaterialTheme.typography.titleMedium
    return remember(baseStyle) {
        derivedStateOf {
            baseStyle.merge(
                TextStyle(
                    fontFamily = translation.value?.getFontFamilyOrNull(),
                    fontWeight = FontWeight.Bold
                )
            )
        }
    }
}
private val toolDescriptionStyle @Composable get() = MaterialTheme.typography.bodyMedium
private val toolCategoryStyle @Composable get() = MaterialTheme.typography.bodySmall
private val toolCardInfoLabelColor: Color @Composable get() {
    val baseColor = LocalContentColor.current
    return remember(baseColor) { with(baseColor) { copy(alpha = alpha * 0.6f) } }
}
internal val toolCardInfoLabelStyle @Composable get() = MaterialTheme.typography.labelSmall

sealed class ToolCardEvent(val tool: Tool?, val lang1: Locale?, val lang2: Locale?) {
    class Click(
        tool: Tool?,
        lang1: Locale? = null,
        lang2: Locale? = null,
    ) : ToolCardEvent(tool, lang1, lang2)
    class OpenTool(tool: Tool?, lang1: Locale? = null, lang2: Locale? = null) : ToolCardEvent(tool, lang1, lang2)
    class OpenToolDetails(tool: Tool?, val additionalLocale: Locale? = null) : ToolCardEvent(tool, null, null)
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
    val tool by viewModel.tool.collectAsState()
    val translation by viewModel.firstTranslation.collectAsState()

    ProvideLayoutDirectionFromLocale(locale = { translation.value?.languageCode }) {
        ElevatedCard(
            elevation = toolCardElevation,
            onClick = { onEvent(ToolCardEvent.Click(tool, translation.value?.languageCode)) },
            modifier = modifier.fillMaxWidth()
        ) {
            ToolBanner(viewModel, modifier = Modifier.aspectRatio(335f / 80f))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                ToolName(viewModel, minLines = 2, modifier = Modifier.fillMaxWidth())

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
@OptIn(ExperimentalMaterial3Api::class)
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
    val downloadProgress by viewModel.downloadProgress.collectAsState()

    ProvideLayoutDirectionFromLocale(locale = { firstTranslation.value?.languageCode }) {
        ElevatedCard(
            elevation = toolCardElevation,
            interactionSource = interactionSource,
            onClick = {
                onEvent(
                    ToolCardEvent.Click(
                        tool,
                        lang1 = firstTranslation.value?.languageCode,
                        lang2 = additionalLanguage?.code,
                    )
                )
            },
            modifier = modifier
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {
                ToolBanner(
                    viewModel,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(335f / 87f)
                )
                FavoriteAction(
                    viewModel,
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
                        viewModel,
                        modifier = Modifier
                            .run { if (additionalLanguage != null) widthIn(max = { it - 70.dp }) else this }
                            .alignByBaseline()
                    )
                    if (additionalLanguage != null) {
                        ToolCardInfoContent {
                            AvailableInLanguage(
                                additionalLanguage,
                                horizontalArrangement = Arrangement.End,
                                modifier = Modifier
                                    .padding(start = 8.dp)
                                    .alignByBaseline()
                            )
                        }
                    }
                }
                ToolCategory(
                    viewModel,
                    modifier = Modifier.fillMaxWidth()
                )

                if (showActions) {
                    ToolCardActions(
                        viewModel,
                        buttonWeightFill = false,
                        buttonModifier = Modifier.widthIn(min = 92.dp),
                        onEvent = onEvent,
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
@OptIn(ExperimentalMaterial3Api::class)
fun SquareToolCard(
    toolCode: String,
    modifier: Modifier = Modifier,
    viewModel: ToolViewModels.ToolViewModel = toolViewModels[toolCode],
    showCategory: Boolean = true,
    showActions: Boolean = true,
    floatParallelLanguageUp: Boolean = true,
    confirmRemovalFromFavorites: Boolean = false,
    onEvent: (ToolCardEvent) -> Unit = {},
) {
    val tool by viewModel.tool.collectAsState()
    val firstTranslation by viewModel.firstTranslation.collectAsState()
    val secondTranslation by viewModel.secondTranslation.collectAsState()
    val downloadProgress by viewModel.downloadProgress.collectAsState()

    ProvideLayoutDirectionFromLocale(locale = { firstTranslation.value?.languageCode }) {
        ElevatedCard(
            elevation = toolCardElevation,
            onClick = {
                onEvent(
                    ToolCardEvent.Click(
                        tool,
                        lang1 = firstTranslation.value?.languageCode,
                        lang2 = secondTranslation?.languageCode,
                    )
                )
            },
            modifier = modifier.width(189.dp)
        ) {
            Box(modifier = Modifier.fillMaxWidth()) {
                ToolBanner(
                    viewModel,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(189f / 128f)
                )
                FavoriteAction(
                    viewModel,
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
                Box {
                    Column {
                        ToolName(viewModel, minLines = 1, maxLines = 2, modifier = Modifier.fillMaxWidth())
                        if (showCategory) {
                            ToolCategory(
                                viewModel,
                                modifier = Modifier
                                    .padding(top = 2.dp)
                                    .fillMaxWidth()
                            )
                        }
                        if (floatParallelLanguageUp) SquareToolCardParallelLanguage(viewModel)
                    }

                    // Reserve the maximum height consistently across all cards
                    Column(modifier = Modifier.invisibleIf(true)) {
                        Spacer(modifier = Modifier.minLinesHeight(2, toolNameStyle(viewModel).value))
                        if (showCategory) {
                            Spacer(
                                modifier = Modifier
                                    .padding(top = 2.dp)
                                    .minLinesHeight(1, toolCategoryStyle)
                            )
                        }
                        if (floatParallelLanguageUp) SquareToolCardParallelLanguage(viewModel)
                    }
                }
                if (!floatParallelLanguageUp) SquareToolCardParallelLanguage(viewModel)

                if (showActions) {
                    ToolCardActions(
                        viewModel,
                        onEvent = onEvent,
                        modifier = Modifier.padding(top = 8.dp)
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
    val tool by viewModel.tool.collectAsState()
    val firstTranslation by viewModel.firstTranslation.collectAsState()

    ProvideLayoutDirectionFromLocale(locale = { firstTranslation.value?.languageCode }) {
        ElevatedCard(
            elevation = toolCardElevation,
            onClick = { onEvent(ToolCardEvent.Click(tool)) },
            modifier = modifier
        ) {
            ToolBanner(
                viewModel,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(335f / 87f)
            )
            Row(modifier = Modifier.padding(16.dp)) {
                RadioButton(selected = isSelected, onClick = null)

                Column(modifier = Modifier.padding(start = 16.dp)) {
                    ToolName(viewModel)
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
private fun ToolBanner(viewModel: ToolViewModels.ToolViewModel, modifier: Modifier = Modifier) =
    ToolBanner(state = ToolCard.State(banner = viewModel.bannerFile.collectAsState().value), modifier = modifier)

@Composable
private fun ToolBanner(state: ToolCard.State, modifier: Modifier = Modifier) = AsyncImage(
    model = state.banner,
    contentDescription = null,
    contentScale = ContentScale.Crop,
    modifier = modifier.background(GodToolsTheme.GRAY_E6)
)

@Composable
private fun ToolName(
    viewModel: ToolViewModels.ToolViewModel,
    modifier: Modifier = Modifier,
    minLines: Int = 1,
    maxLines: Int = Int.MAX_VALUE,
) {
    val tool by viewModel.tool.collectAsState()
    val translation by viewModel.firstTranslation.collectAsState()
    val style by toolNameStyle(viewModel)

    Text(
        translation.value.getName(tool).orEmpty(),
        style = style,
        maxLines = maxLines,
        overflow = TextOverflow.Ellipsis,
        modifier = modifier
            .invisibleIf { translation.isInitial }
            .minLinesHeight(minLines = minLines, textStyle = style)
    )
}

@Composable
private fun ToolCategory(viewModel: ToolViewModels.ToolViewModel, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val tool by viewModel.tool.collectAsState()
    val translation by viewModel.firstTranslation.collectAsState()
    val locale by remember { derivedStateOf { translation.value?.languageCode } }

    Text(
        tool.getCategory(context, locale),
        style = toolCategoryStyle,
        maxLines = 1,
        modifier = modifier.invisibleIf { translation.isInitial }
    )
}

@Composable
private fun ToolCardInfoContent(content: @Composable () -> Unit) = CompositionLocalProvider(
    LocalContentColor provides toolCardInfoLabelColor,
    LocalTextStyle provides toolCardInfoLabelStyle,
    content = content
)

@Composable
private fun SquareToolCardParallelLanguage(viewModel: ToolViewModels.ToolViewModel) {
    val parallelLanguage by viewModel.parallelLanguage.collectAsState()

    if (parallelLanguage != null) {
        val secondTranslation by viewModel.secondTranslation.collectAsState()
        val secondLanguage by viewModel.secondLanguage.collectAsState()
        val available by remember { derivedStateOf { secondTranslation != null } }

        ToolCardInfoContent {
            AvailableInLanguage(
                secondLanguage,
                horizontalArrangement = Arrangement.Start,
                modifier = Modifier
                    .padding(top = 2.dp)
                    .invisibleIf { !available }
            )
        }
    }
}
