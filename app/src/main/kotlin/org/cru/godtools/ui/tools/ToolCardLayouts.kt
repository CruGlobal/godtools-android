package org.cru.godtools.ui.tools

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.material3.CardDefaults.elevatedCardElevation
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import org.ccci.gto.android.common.androidx.compose.foundation.text.minLinesHeight
import org.cru.godtools.R
import org.cru.godtools.base.ui.theme.GRAY_E6
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
fun LessonToolCard(toolCode: String, onClick: (Tool?, Translation?) -> Unit = { _, _ -> }) {
    val viewModel = toolViewModel(toolCode)
    val tool by viewModel.tool.collectAsState()
    val translation by viewModel.firstTranslation.collectAsState()

    ElevatedCard(
        elevation = toolCardElevation,
        onClick = { onClick(tool, translation) },
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 16.dp)
    ) {
        AsyncImage(
            model = viewModel.bannerFile.collectAsState().value,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
                .background(GRAY_E6)
                .aspectRatio(4.1875f)
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            val baseToolNameStyle = MaterialTheme.typography.titleMedium
            val toolNameStyle by remember(baseToolNameStyle) {
                derivedStateOf { baseToolNameStyle.merge(TextStyle(fontFamily = translation?.getFontFamilyOrNull())) }
            }

            Text(
                translation.getName(tool).orEmpty(),
                style = toolNameStyle,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                modifier = Modifier
                    .fillMaxWidth()
                    .minLinesHeight(minLines = 2, textStyle = toolNameStyle)
            )

            Box(modifier = Modifier.align(Alignment.End)) {
                AvailableInLanguage(
                    language = viewModel.primaryLanguage.collectAsState(),
                    translation = viewModel.primaryTranslation.collectAsState()
                )
            }
        }
    }
}

@Composable
private fun AvailableInLanguage(language: State<Language?>, translation: State<Translation?>) = Row(
    verticalAlignment = Alignment.Bottom,
    modifier = Modifier
        .wrapContentWidth()
        .alpha(0.6f)
) {
    val available by remember { derivedStateOf { translation.value != null } }

    Text(
        language.value?.getDisplayName(LocalContext.current).orEmpty(),
        style = MaterialTheme.typography.labelSmall,
        maxLines = 1,
        modifier = Modifier
            .wrapContentWidth()
            .alignByBaseline()
    )
    Icon(
        painterResource(if (available) R.drawable.ic_language_available else R.drawable.ic_language_unavailable),
        contentDescription = null,
        modifier = Modifier
            .padding(start = 4.dp)
            .size(8.dp)
            .alignBy { it.measuredHeight }
    )
}
