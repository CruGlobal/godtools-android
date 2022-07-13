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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.CardDefaults.elevatedCardElevation
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import org.ccci.gto.android.common.androidx.compose.foundation.layout.widthIn
import org.ccci.gto.android.common.androidx.compose.foundation.text.minLinesHeight
import org.ccci.gto.android.common.androidx.compose.ui.draw.invisibleIf
import org.cru.godtools.base.ui.theme.GRAY_E6
import org.cru.godtools.base.ui.util.ProvideLayoutDirectionFromLocale
import org.cru.godtools.base.ui.util.getCategory
import org.cru.godtools.base.ui.util.getFontFamilyOrNull
import org.cru.godtools.model.Tool
import org.cru.godtools.model.Translation
import org.cru.godtools.model.getName

private val toolViewModels: ToolViewModels @Composable get() = viewModel()
@Composable
private fun toolViewModel(tool: String) = toolViewModels[tool]

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
private val toolCategoryStyle @Composable get() = MaterialTheme.typography.bodySmall
internal val toolCardInfoLabelStyle @Composable get() = MaterialTheme.typography.labelSmall

@Composable
fun PreloadTool(tool: Tool) {
    val code = tool.code ?: return
    toolViewModels.initializeToolViewModel(code, tool)
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun LessonToolCard(
    toolCode: String,
    modifier: Modifier = Modifier,
    onClick: (Tool?, Translation?) -> Unit = { _, _ -> }
) {
    val viewModel = toolViewModel(toolCode)
    val tool by viewModel.tool.collectAsState()
    val translation by viewModel.firstTranslation.collectAsState()

    ProvideLayoutDirectionFromLocale(locale = { translation.value?.languageCode }) {
        ElevatedCard(
            elevation = toolCardElevation,
            onClick = { onClick(tool, translation.value) },
            modifier = modifier.fillMaxWidth()
        ) {
            ToolBanner(viewModel, modifier = Modifier.aspectRatio(335f / 80f))

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            ) {
                ToolName(viewModel, minLines = 2, modifier = Modifier.fillMaxWidth())

                val primaryTranslation by viewModel.primaryTranslation.collectAsState()
                val primaryLanguage by viewModel.primaryLanguage.collectAsState()

                AvailableInLanguage(
                    language = primaryLanguage,
                    translation = { primaryTranslation.value },
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier
                        .align(Alignment.End)
                        .invisibleIf { primaryTranslation.isInitial || primaryLanguage == null }
                )
            }
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun ToolCard(
    toolCode: String,
    modifier: Modifier = Modifier,
    confirmRemovalFromFavorites: Boolean = false,
    interactionSource: MutableInteractionSource = remember { MutableInteractionSource() },
    onOpenTool: (Tool?, Translation?, Translation?) -> Unit = { _, _, _ -> },
    onOpenToolDetails: (String) -> Unit = {},
    onClick: (Tool?, Translation?, Translation?) -> Unit = onOpenTool
) {
    val viewModel = toolViewModel(toolCode)
    val tool by viewModel.tool.collectAsState()
    val firstTranslation by viewModel.firstTranslation.collectAsState()
    val secondTranslation by viewModel.secondTranslation.collectAsState()
    val secondLanguage by viewModel.secondLanguage.collectAsState()
    val downloadProgress by viewModel.downloadProgress.collectAsState()

    ProvideLayoutDirectionFromLocale(locale = { firstTranslation.value?.languageCode }) {
        ElevatedCard(
            elevation = toolCardElevation,
            interactionSource = interactionSource,
            onClick = { onClick(tool, firstTranslation.value, secondTranslation) },
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
                val hasSecondTranslation by remember { derivedStateOf { secondTranslation != null } }
                Row(
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    ToolName(
                        viewModel,
                        modifier = Modifier
                            .run { if (hasSecondTranslation) widthIn(max = { it - 70.dp }) else this }
                            .alignByBaseline()
                    )
                    if (hasSecondTranslation) {
                        AvailableInLanguage(
                            secondLanguage,
                            horizontalArrangement = Arrangement.End,
                            modifier = Modifier
                                .padding(start = 8.dp)
                                .alignByBaseline()
                        )
                    }
                }
                ToolCategory(
                    viewModel,
                    modifier = Modifier.fillMaxWidth()
                )

                ToolCardActions(
                    viewModel,
                    buttonWeightFill = false,
                    buttonModifier = Modifier.widthIn(min = 92.dp),
                    onOpenTool = onOpenTool,
                    onOpenToolDetails = onOpenToolDetails,
                    modifier = Modifier
                        .padding(top = 4.dp)
                        .align(Alignment.End)
                )
            }
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun SquareToolCard(
    toolCode: String,
    modifier: Modifier = Modifier,
    showCategory: Boolean = true,
    showActions: Boolean = true,
    floatParallelLanguageUp: Boolean = true,
    confirmRemovalFromFavorites: Boolean = false,
    onOpenTool: (Tool?, Translation?, Translation?) -> Unit = { _, _, _ -> },
    onOpenToolDetails: (String) -> Unit = {},
    onClick: (Tool?, Translation?, Translation?) -> Unit = onOpenTool
) {
    val viewModel = toolViewModel(toolCode)
    val tool by viewModel.tool.collectAsState()
    val firstTranslation by viewModel.firstTranslation.collectAsState()
    val secondTranslation by viewModel.secondTranslation.collectAsState()
    val downloadProgress by viewModel.downloadProgress.collectAsState()

    ProvideLayoutDirectionFromLocale(locale = { firstTranslation.value?.languageCode }) {
        ElevatedCard(
            elevation = toolCardElevation,
            onClick = { onClick(tool, firstTranslation.value, secondTranslation) },
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
                        onOpenTool = onOpenTool,
                        onOpenToolDetails = onOpenToolDetails,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ToolBanner(viewModel: ToolViewModels.ToolViewModel, modifier: Modifier = Modifier) = AsyncImage(
    model = viewModel.bannerFile.collectAsState().value,
    contentDescription = null,
    contentScale = ContentScale.Crop,
    modifier = modifier.background(GRAY_E6)
)

@Composable
private fun ToolName(
    viewModel: ToolViewModels.ToolViewModel,
    modifier: Modifier = Modifier,
    minLines: Int = 1,
    maxLines: Int = Int.MAX_VALUE
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
private fun SquareToolCardParallelLanguage(viewModel: ToolViewModels.ToolViewModel) {
    val parallelLanguage by viewModel.parallelLanguage.collectAsState()

    if (parallelLanguage != null) {
        val secondTranslation by viewModel.secondTranslation.collectAsState()
        val secondLanguage by viewModel.secondLanguage.collectAsState()
        val available by remember { derivedStateOf { secondTranslation != null } }

        AvailableInLanguage(
            secondLanguage,
            horizontalArrangement = Arrangement.Start,
            modifier = Modifier
                .padding(top = 2.dp)
                .invisibleIf { !available }
        )
    }
}
