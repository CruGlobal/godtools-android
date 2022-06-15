package org.cru.godtools.ui.tools

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.Button
import androidx.compose.material3.CardDefaults.elevatedCardElevation
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalMinimumTouchTargetEnforcement
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
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
import org.cru.godtools.base.ui.util.getCategory
import org.cru.godtools.base.ui.util.getFontFamilyOrNull
import org.cru.godtools.model.Language
import org.cru.godtools.model.Tool
import org.cru.godtools.model.Translation
import org.cru.godtools.model.getName

@Composable
private fun toolViewModel(tool: String) = viewModel<ToolsAdapterViewModel>().getToolViewModel(tool)
private val toolCardElevation @Composable get() = elevatedCardElevation(defaultElevation = 4.dp)

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
            ToolName(viewModel, lines = 2, modifier = Modifier.fillMaxWidth())

            Row(
                modifier = Modifier
                    .align(Alignment.End)
                    .wrapContentWidth()
            ) {
                AvailableInLanguage(
                    language = viewModel.primaryLanguage.collectAsState(),
                    translation = viewModel.primaryTranslation.collectAsState()
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
    onOpenTool: (Tool?, Translation?, Translation?) -> Unit = { _, _, _ -> },
    onOpenToolDetails: (String) -> Unit = {},
    onClick: (Tool?, Translation?, Translation?) -> Unit = onOpenTool
) {
    val viewModel = toolViewModel(toolCode)
    val tool by viewModel.tool.collectAsState()
    val firstTranslation by viewModel.firstTranslation.collectAsState()
    val secondTranslation = viewModel.secondTranslation.collectAsState()
    val secondLanguage = viewModel.secondLanguage.collectAsState()
    val parallelLanguage by viewModel.parallelLanguage.collectAsState()

    ElevatedCard(
        elevation = toolCardElevation,
        onClick = { onClick(tool, firstTranslation, secondTranslation.value) },
        modifier = modifier.width(189.dp)
    ) {
        ToolBanner(viewModel, modifier = Modifier.aspectRatio(189f / 128f))
        Column(modifier = Modifier.padding(16.dp)) {
            ToolName(viewModel, lines = 2, modifier = Modifier.fillMaxWidth())
            ToolCategory(viewModel, modifier = Modifier.fillMaxWidth())
            if (parallelLanguage != null) {
                Row(
                    modifier = Modifier
                        .padding(top = 4.dp)
                        .fillMaxWidth()
                        .alpha(if (secondTranslation.value != null) 1f else 0f)
                ) {
                    AvailableInLanguage(language = secondLanguage, translation = secondTranslation)
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
                        Text(stringResource(R.string.action_tools_about), style = MaterialTheme.typography.labelMedium)
                    }
                    Button(
                        onClick = { onOpenTool(tool, firstTranslation, secondTranslation.value) },
                        contentPadding = contentPadding,
                        modifier = Modifier
                            .weight(1f)
                            .defaultMinSize(minHeight = minHeight)
                    ) {
                        Text(stringResource(R.string.action_tools_open), style = MaterialTheme.typography.labelMedium)
                    }
                }
            }
        }
    }
}

@Composable
private fun ToolBanner(viewModel: ToolsAdapterViewModel.ToolViewModel, modifier: Modifier = Modifier) = AsyncImage(
    model = viewModel.bannerFile.collectAsState().value,
    contentDescription = null,
    contentScale = ContentScale.Crop,
    modifier = modifier.background(GRAY_E6)
)

@Composable
private fun ToolName(viewModel: ToolsAdapterViewModel.ToolViewModel, modifier: Modifier = Modifier, lines: Int = 1) {
    val tool by viewModel.tool.collectAsState()
    val translation by viewModel.firstTranslation.collectAsState()

    val baseStyle = MaterialTheme.typography.titleMedium
    val style by remember(baseStyle) {
        derivedStateOf {
            baseStyle.merge(
                TextStyle(
                    fontFamily = translation?.getFontFamilyOrNull(),
                    fontWeight = FontWeight.Bold
                )
            )
        }
    }

    Text(
        translation.getName(tool).orEmpty(),
        style = style,
        maxLines = lines,
        overflow = TextOverflow.Ellipsis,
        modifier = modifier.minLinesHeight(minLines = lines, textStyle = style)
    )
}

@Composable
private fun ToolCategory(viewModel: ToolsAdapterViewModel.ToolViewModel, modifier: Modifier = Modifier) {
    val tool by viewModel.tool.collectAsState()
    val firstTranslation by viewModel.firstTranslation.collectAsState()

    val context = LocalContext.current
    val locale by remember { derivedStateOf { firstTranslation?.languageCode } }

    Text(
        tool.getCategory(context, locale),
        style = MaterialTheme.typography.bodySmall,
        modifier = modifier
    )
}

@Composable
private fun RowScope.AvailableInLanguage(
    language: State<Language?>,
    translation: State<Translation?>,
    alpha: Float = 0.6f
) {
    val available by remember { derivedStateOf { translation.value != null } }

    Text(
        language.value?.getDisplayName(LocalContext.current).orEmpty(),
        style = MaterialTheme.typography.labelSmall,
        maxLines = 1,
        modifier = Modifier
            .wrapContentWidth()
            .alignByBaseline()
            .alpha(alpha)
    )
    Icon(
        painterResource(if (available) R.drawable.ic_language_available else R.drawable.ic_language_unavailable),
        contentDescription = null,
        modifier = Modifier
            .padding(start = 4.dp)
            .size(8.dp)
            .alignBy { it.measuredHeight }
            .alpha(alpha)
    )
}
