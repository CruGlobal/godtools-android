package org.cru.godtools.ui.tools

import androidx.compose.foundation.background
import androidx.compose.material3.CardDefaults.elevatedCardElevation
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import org.cru.godtools.base.ui.theme.GodToolsTheme
import org.cru.godtools.base.ui.util.getCategory
import org.cru.godtools.model.getName

internal const val TEST_TAG_TOOL_CATEGORY = "tool_category"

internal val toolCardElevation @Composable get() = elevatedCardElevation(defaultElevation = 4.dp)

internal val ToolCard.State.toolNameStyle: TextStyle
    @Composable
    get() {
        val baseStyle = MaterialTheme.typography.titleMedium
        return remember(baseStyle) { baseStyle.merge(TextStyle(fontWeight = FontWeight.Bold)) }
    }

internal val toolCategoryStyle @Composable get() = MaterialTheme.typography.bodySmall
private val toolCardInfoLabelColor: Color @Composable get() {
    val baseColor = LocalContentColor.current
    return remember(baseColor) { with(baseColor) { copy(alpha = alpha * 0.6f) } }
}
private val toolCardInfoLabelStyle @Composable get() = MaterialTheme.typography.labelSmall

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
        minLines = minLines,
        overflow = TextOverflow.Ellipsis,
        modifier = modifier
    )
}

@Composable
internal fun ToolCategory(state: ToolCard.State, modifier: Modifier = Modifier) = Text(
    state.tool.getCategory(LocalContext.current),
    style = toolCategoryStyle,
    maxLines = 1,
    modifier = modifier.testTag(TEST_TAG_TOOL_CATEGORY)
)

@Composable
internal fun ToolCardInfoContent(content: @Composable () -> Unit) = CompositionLocalProvider(
    LocalContentColor provides toolCardInfoLabelColor,
    LocalTextStyle provides toolCardInfoLabelStyle,
    content = content
)
