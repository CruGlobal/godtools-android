package org.cru.godtools.ui.tools

import androidx.annotation.VisibleForTesting
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults.elevatedCardElevation
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.LocalMinimumTouchTargetEnforcement
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Shapes
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisallowComposableCalls
import androidx.compose.runtime.State
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import org.ccci.gto.android.common.androidx.compose.foundation.text.minLinesHeight
import org.cru.godtools.R
import org.cru.godtools.base.ui.theme.GRAY_E6
import org.cru.godtools.base.ui.util.ProvideLayoutDirectionFromLocale
import org.cru.godtools.base.ui.util.getCategory
import org.cru.godtools.base.ui.util.getFontFamilyOrNull
import org.cru.godtools.download.manager.DownloadProgress
import org.cru.godtools.model.Language
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
private val infoLabelStyle @Composable get() = MaterialTheme.typography.labelSmall

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
    val downloadProgress = viewModel.downloadProgress.collectAsState()

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
                    downloadProgress,
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
                                        .minLinesHeight(1, infoLabelStyle)
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
                                    .minLinesHeight(1, infoLabelStyle)
                            )
                        }
                    }
                }

                Row(
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.padding(top = 8.dp)
                ) {
                    val contentPadding = PaddingValues(horizontal = 4.dp, vertical = 8.dp)
                    val minHeight = 30.dp

                    CompositionLocalProvider(LocalMinimumTouchTargetEnforcement provides false) {
                        OutlinedButton(
                            onClick = { onOpenToolDetails(toolCode) },
                            contentPadding = contentPadding,
                            modifier = Modifier
                                .weight(1f)
                                .defaultMinSize(minHeight = minHeight)
                        ) {
                            Text(
                                stringResource(R.string.action_tools_about),
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                        Button(
                            onClick = { onOpenTool(tool, firstTranslation, secondTranslation) },
                            contentPadding = contentPadding,
                            modifier = Modifier
                                .weight(1f)
                                .defaultMinSize(minHeight = minHeight)
                        ) {
                            Text(
                                stringResource(R.string.action_tools_open),
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                    }
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

// TODO: this can be refactored & moved elsewhere when we need to use it outside of tool cards
@Composable
private fun DownloadProgressIndicator(downloadProgress: State<DownloadProgress?>, modifier: Modifier = Modifier) {
    val hasProgress by remember { derivedStateOf { downloadProgress.value != null } }
    val isIndeterminate by remember { derivedStateOf { downloadProgress.value?.isIndeterminate == true } }
    val progress by remember {
        derivedStateOf {
            downloadProgress.value
                ?.takeIf { it.max > 0 }
                ?.let { it.progress.toFloat() / it.max }
                ?.coerceIn(0f, 1f) ?: 0f
        }
    }

    if (hasProgress) {
        if (isIndeterminate) {
            LinearProgressIndicator(modifier = modifier)
        } else {
            // TODO: figure out how to animate progress updates to make a more smooth UI
            LinearProgressIndicator(progress, modifier = modifier)
        }
    }
}

@Composable
@VisibleForTesting
@OptIn(ExperimentalMaterial3Api::class)
internal fun FavoriteAction(
    viewModel: ToolViewModels.ToolViewModel,
    modifier: Modifier = Modifier,
    confirmRemoval: Boolean = true
) {
    val tool by viewModel.tool.collectAsState()
    val isAdded by remember { derivedStateOf { tool?.isAdded == true } }
    var showRemovalConfirmation by rememberSaveable { mutableStateOf(false) }

    Surface(
        onClick = {
            when {
                !isAdded -> viewModel.pinTool()
                confirmRemoval -> showRemovalConfirmation = true
                else -> viewModel.unpinTool()
            }
        },
        shape = Shapes.Full,
        shadowElevation = 6.dp,
        modifier = modifier
    ) {
        Icon(
            painter = painterResource(if (isAdded) R.drawable.ic_favorite_24dp else R.drawable.ic_favorite_border_24dp),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .padding(horizontal = 5.dp)
                .padding(top = 6.dp, bottom = 4.dp)
                .size(18.dp)
        )
    }

    if (showRemovalConfirmation) {
        val translation by viewModel.firstTranslation.collectAsState()

        AlertDialog(
            onDismissRequest = { showRemovalConfirmation = false },
            text = {
                Text(
                    stringResource(
                        R.string.tools_list_remove_favorite_dialog_title,
                        translation.getName(tool).orEmpty()
                    )
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.unpinTool()
                        showRemovalConfirmation = false
                    }
                ) { Text(stringResource(R.string.tools_list_remove_favorite_dialog_confirm)) }
            },
            dismissButton = {
                TextButton(onClick = { showRemovalConfirmation = false }) {
                    Text(stringResource(R.string.tools_list_remove_favorite_dialog_dismiss))
                }
            }
        )
    }
}

@Composable
private inline fun AvailableInLanguage(
    language: Language?,
    crossinline translation: @DisallowComposableCalls () -> Translation?,
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
) {
    val available by remember { derivedStateOf { translation() != null } }
    AvailableInLanguage(
        language,
        available = available,
        horizontalArrangement = horizontalArrangement,
        modifier = modifier
    )
}

@Composable
private fun AvailableInLanguage(
    language: Language?,
    available: Boolean = true,
    modifier: Modifier = Modifier,
    horizontalArrangement: Arrangement.Horizontal = Arrangement.Start,
    alpha: Float = 0.6f
) = Row(
    horizontalArrangement = horizontalArrangement,
    modifier = modifier
        .widthIn(min = 50.dp)
        .alpha(alpha)
) {
    val context = LocalContext.current
    val languageName = remember(language, context) { language?.getDisplayName(context).orEmpty() }

    Text(
        if (available) languageName else stringResource(R.string.tool_card_label_language_unavailable, languageName),
        style = infoLabelStyle,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = Modifier
            .alignByBaseline()
            .weight(1f, false)
    )
    Icon(
        painterResource(if (available) R.drawable.ic_language_available else R.drawable.ic_language_unavailable),
        contentDescription = null,
        modifier = Modifier
            .padding(start = 4.dp)
            .size(with(LocalDensity.current) { (infoLabelStyle.fontSize * 0.65).toDp() })
            .alignBy { it.measuredHeight }
    )
}
