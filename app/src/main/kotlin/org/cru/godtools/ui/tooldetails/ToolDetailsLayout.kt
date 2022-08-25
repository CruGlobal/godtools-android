package org.cru.godtools.ui.tooldetails

import androidx.annotation.VisibleForTesting
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import org.ccci.gto.android.common.androidx.compose.foundation.layout.padding
import org.cru.godtools.R
import org.cru.godtools.base.ui.theme.GRAY_E6
import org.cru.godtools.base.ui.theme.GT_RED
import org.cru.godtools.base.ui.util.getFontFamilyOrNull
import org.cru.godtools.base.ui.youtubeplayer.YouTubePlayer
import org.cru.godtools.model.Tool
import org.cru.godtools.model.Translation
import org.cru.godtools.model.getName
import org.cru.godtools.ui.tools.DownloadProgressIndicator
import org.cru.godtools.ui.tools.ToolViewModels

private val TOOL_DETAILS_HORIZONTAL_MARGIN = 32.dp

@Composable
fun ToolDetailsLayout(
    onOpenTool: (Tool?, Translation?, Translation?) -> Unit = { _, _, _ -> },
    onOpenToolTraining: (Tool?, Translation?) -> Unit = { _, _ -> },
) {
    val viewModel = viewModel<ToolDetailsFragmentDataModel>()
    val toolCode by viewModel.toolCode.collectAsState()

    toolCode?.let {
        ToolDetailsLayout(it, onOpenTool = onOpenTool, onOpenToolTraining = onOpenToolTraining)
    }
}

@Composable
@OptIn(ExperimentalComposeUiApi::class)
private fun ToolDetailsLayout(
    toolCode: String,
    onOpenTool: (Tool?, Translation?, Translation?) -> Unit,
    onOpenToolTraining: (Tool?, Translation?) -> Unit,
) {
    val toolViewModel = viewModel<ToolViewModels>()[toolCode]
    val tool by toolViewModel.tool.collectAsState()
    val translation by toolViewModel.firstTranslation.collectAsState()

    Column {
        Box(modifier = Modifier.fillMaxWidth()) {
            ToolDetailsBanner(
                toolViewModel,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(21f / 10f)
            )

            val downloadProgress by toolViewModel.downloadProgress.collectAsState()
            DownloadProgressIndicator(
                { downloadProgress },
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
            )
        }

        Text(
            translation.value.getName(tool).orEmpty(),
            fontFamily = translation.value?.getFontFamilyOrNull(),
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier.padding(top = 40.dp, horizontal = TOOL_DETAILS_HORIZONTAL_MARGIN)
        )

        val shares by remember { derivedStateOf { tool?.shares ?: 0 } }
        Text(
            pluralStringResource(R.plurals.label_tools_shares, shares, shares),
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(top = 10.dp, horizontal = TOOL_DETAILS_HORIZONTAL_MARGIN)
        )

        ToolDetailsActions(
            toolViewModel,
            onOpenTool = onOpenTool,
            onOpenToolTraining = onOpenToolTraining,
            modifier = Modifier.padding(top = 16.dp, horizontal = TOOL_DETAILS_HORIZONTAL_MARGIN)
        )
    }
}

@Composable
private fun ToolDetailsBanner(
    toolViewModel: ToolViewModels.ToolViewModel,
    modifier: Modifier = Modifier
) {
    val tool by toolViewModel.tool.collectAsState()
    val banner = toolViewModel.detailsBanner.collectAsState().value
    val bannerAnimation = toolViewModel.detailsBannerAnimation.collectAsState().value
    val youtubeVideo = remember { derivedStateOf { tool?.detailsBannerYoutubeVideoId } }.value

    when {
        youtubeVideo != null -> YouTubePlayer(
            youtubeVideo,
            recue = true,
            modifier = modifier
        )
        bannerAnimation != null -> {
            val composition by rememberLottieComposition(LottieCompositionSpec.File(bannerAnimation.path))
            val progress by animateLottieCompositionAsState(
                composition,
                restartOnPlay = false,
                iterations = LottieConstants.IterateForever,
            )
            LottieAnimation(
                composition,
                { progress },
                modifier = modifier
            )
        }
        banner != null -> AsyncImage(
            model = banner,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = modifier
        )
        else -> Spacer(modifier = modifier.background(GRAY_E6))
    }
}

@Composable
@VisibleForTesting
internal fun ToolDetailsActions(
    toolViewModel: ToolViewModels.ToolViewModel,
    modifier: Modifier = Modifier,
    onOpenTool: (Tool?, Translation?, Translation?) -> Unit = { _, _, _ -> },
    onOpenToolTraining: (Tool?, Translation?) -> Unit = { _, _ -> },
) = Column(modifier = modifier) {
    val tool by toolViewModel.tool.collectAsState()
    val translation by toolViewModel.firstTranslation.collectAsState()
    val secondTranslation by toolViewModel.secondTranslation.collectAsState()

    Button(
        onClick = { onOpenTool(tool, translation.value, secondTranslation) },
        modifier = Modifier.fillMaxWidth()
    ) { Text(stringResource(R.string.action_tools_open_tool)) }

    val manifest by toolViewModel.firstManifest.collectAsState()
    if (manifest?.hasTips == true) {
        Button(
            onClick = { onOpenToolTraining(tool, translation.value) },
            modifier = Modifier
                .testTag("action_tool_training")
                .fillMaxWidth()
        ) { Text(stringResource(R.string.action_tools_open_training)) }
    }

    val isAdded by remember { derivedStateOf { tool?.isAdded == true } }
    OutlinedButton(
        onClick = { if (isAdded) toolViewModel.unpinTool() else toolViewModel.pinTool() },
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = if (isAdded) GT_RED else MaterialTheme.colorScheme.primary
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(
            painterResource(if (isAdded) R.drawable.ic_favorite_border_24dp else R.drawable.ic_favorite_24dp),
            contentDescription = null
        )
        Text(
            stringResource(
                if (isAdded) R.string.action_tools_remove_favorite
                else R.string.action_tools_add_favorite
            ),
            modifier = Modifier.padding(start = 4.dp)
        )
    }
}
