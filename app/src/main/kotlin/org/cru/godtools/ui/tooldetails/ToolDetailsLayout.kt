package org.cru.godtools.ui.tooldetails

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.airbnb.lottie.compose.LottieAnimation
import com.airbnb.lottie.compose.LottieCompositionSpec
import com.airbnb.lottie.compose.LottieConstants
import com.airbnb.lottie.compose.animateLottieCompositionAsState
import com.airbnb.lottie.compose.rememberLottieComposition
import org.cru.godtools.base.ui.theme.GRAY_E6
import org.cru.godtools.base.ui.youtubeplayer.YouTubePlayer
import org.cru.godtools.ui.tools.DownloadProgressIndicator
import org.cru.godtools.ui.tools.ToolViewModels

@Composable
fun ToolDetailsLayout() {
    val viewModel = viewModel<ToolDetailsFragmentDataModel>()
    val toolCode by viewModel.toolCode.collectAsState()

    toolCode?.let {
        ToolDetailsLayout(it)
    }
}

@Composable
private fun ToolDetailsLayout(toolCode: String) {
    val toolViewModel = viewModel<ToolViewModels>()[toolCode]

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
