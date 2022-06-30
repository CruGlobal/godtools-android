package org.cru.godtools.ui.tools

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import org.ccci.gto.android.common.androidx.compose.foundation.text.minLinesHeight
import org.cru.godtools.base.ui.theme.GRAY_E6
import org.cru.godtools.base.ui.util.ProvideLayoutDirectionFromLocale
import org.cru.godtools.base.ui.util.getCategory
import org.cru.godtools.base.ui.util.getFontFamilyOrNull
import org.cru.godtools.model.Tool
import org.cru.godtools.model.Translation
import org.cru.godtools.model.getName

@Composable
private fun toolViewModel(tool: String) = viewModel<ToolViewModels>()[tool]
private val toolCardElevation @Composable get() = elevatedCardElevation(defaultElevation = 4.dp)

@Composable
private fun toolNameStyle(viewModel: ToolViewModels.ToolViewModel): State<TextStyle> {
    val translation by viewModel.firstTranslation.collectAsState()

    val baseStyle = MaterialTheme.typography.titleMedium
    return remember(baseStyle) {
        derivedStateOf {
            baseStyle.merge(
                TextStyle(
                    fontFamily = translation?.getFontFamilyOrNull(),
                    fontWeight = FontWeight.Bold
                )
            )
        }
    }
}
private val toolCategoryStyle @Composable get() = MaterialTheme.typography.bodySmall
internal val toolCardInfoLabelStyle @Composable get() = MaterialTheme.typography.labelSmall

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

    ProvideLayoutDirectionFromLocale(locale = { translation?.languageCode }) {
        ElevatedCard(
            elevation = toolCardElevation,
            onClick = { onClick(tool, translation) },
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
                    translation = { primaryTranslation },
                    horizontalArrangement = Arrangement.End,
                    modifier = Modifier.align(Alignment.End)
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
    confirmRemovalFromFavorites: Boolean = false,
    onOpenTool: (Tool?, Translation?, Translation?) -> Unit = { _, _, _ -> },
    onOpenToolDetails: (String) -> Unit = {},
    onClick: (Tool?, Translation?, Translation?) -> Unit = onOpenTool
) {
    val viewModel = toolViewModel(toolCode)
    val tool by viewModel.tool.collectAsState()
    val firstTranslation by viewModel.firstTranslation.collectAsState()
    val secondTranslation by viewModel.secondTranslation.collectAsState()
    val secondLanguage by viewModel.secondLanguage.collectAsState()
    val parallelLanguage by viewModel.parallelLanguage.collectAsState()
    val downloadProgress by viewModel.downloadProgress.collectAsState()

    ProvideLayoutDirectionFromLocale(locale = { firstTranslation?.languageCode }) {
        ElevatedCard(
            elevation = toolCardElevation,
            onClick = { onClick(tool, firstTranslation, secondTranslation) },
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
                        ToolCategory(
                            viewModel,
                            modifier = Modifier
                                .padding(top = 2.dp)
                                .fillMaxWidth()
                        )
                        if (parallelLanguage != null) {
                            val available by remember { derivedStateOf { secondTranslation != null } }
                            if (available) {
                                AvailableInLanguage(
                                    secondLanguage,
                                    horizontalArrangement = Arrangement.Start,
                                    modifier = Modifier.padding(top = 2.dp)
                                )
                            } else {
                                Spacer(
                                    modifier = Modifier
                                        .padding(top = 2.dp)
                                        .minLinesHeight(1, toolCardInfoLabelStyle)
                                )
                            }
                        }
                    }

                    // use Spacers to reserve the maximum height consistently across all cards
                    Column {
                        Spacer(modifier = Modifier.minLinesHeight(2, toolNameStyle(viewModel).value))
                        Spacer(
                            modifier = Modifier
                                .padding(top = 2.dp)
                                .minLinesHeight(1, toolCategoryStyle)
                        )
                        if (parallelLanguage != null) {
                            Spacer(
                                modifier = Modifier
                                    .padding(top = 2.dp)
                                    .minLinesHeight(1, toolCardInfoLabelStyle)
                            )
                        }
                    }
                }

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

@Composable
private fun ToolBanner(viewModel: ToolViewModels.ToolViewModel, modifier: Modifier = Modifier) = AsyncImage(
    model = viewModel.bannerFile.collectAsState().value,
    contentDescription = null,
    contentScale = ContentScale.Crop,
    modifier = modifier.background(GRAY_E6)
)

@Composable
private inline fun ToolName(
    viewModel: ToolViewModels.ToolViewModel,
    modifier: Modifier = Modifier,
    lines: Int,
) = ToolName(viewModel = viewModel, modifier = modifier, minLines = lines, maxLines = lines)

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
        translation.getName(tool).orEmpty(),
        style = style,
        maxLines = maxLines,
        overflow = TextOverflow.Ellipsis,
        modifier = modifier.minLinesHeight(minLines = minLines, textStyle = style)
    )
}

@Composable
private fun ToolCategory(viewModel: ToolViewModels.ToolViewModel, modifier: Modifier = Modifier) {
    val tool by viewModel.tool.collectAsState()
    val firstTranslation by viewModel.firstTranslation.collectAsState()

    val context = LocalContext.current
    val locale by remember { derivedStateOf { firstTranslation?.languageCode } }

    Text(
        tool.getCategory(context, locale),
        style = toolCategoryStyle,
        maxLines = 1,
        modifier = modifier
    )
}
