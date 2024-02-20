package org.cru.godtools.ui.tooldetails

import androidx.annotation.VisibleForTesting
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReusableContent
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
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
import java.util.Locale
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.launch
import org.ccci.gto.android.common.androidx.compose.foundation.layout.padding
import org.ccci.gto.android.common.androidx.compose.material3.ui.tabs.pagerTabIndicatorOffset
import org.cru.godtools.R
import org.cru.godtools.analytics.compose.RecordAnalyticsScreen
import org.cru.godtools.base.LocalAppLanguage
import org.cru.godtools.base.ui.theme.GodToolsTheme
import org.cru.godtools.base.ui.util.getFontFamilyOrNull
import org.cru.godtools.base.ui.youtubeplayer.YouTubePlayer
import org.cru.godtools.downloadmanager.compose.DownloadLatestTranslation
import org.cru.godtools.model.Language.Companion.getSortedDisplayNames
import org.cru.godtools.model.Tool
import org.cru.godtools.model.getName
import org.cru.godtools.shortcuts.PendingShortcut
import org.cru.godtools.ui.drawer.DrawerMenuLayout
import org.cru.godtools.ui.tooldetails.ToolDetailsScreen.Event
import org.cru.godtools.ui.tooldetails.ToolDetailsScreen.State
import org.cru.godtools.ui.tooldetails.analytics.model.ToolDetailsScreenEvent
import org.cru.godtools.ui.tools.AvailableInLanguage
import org.cru.godtools.ui.tools.DownloadProgressIndicator
import org.cru.godtools.ui.tools.ToolCard
import org.cru.godtools.ui.tools.ToolViewModels
import org.cru.godtools.ui.tools.VariantToolCard

private val TOOL_DETAILS_HORIZONTAL_MARGIN = 32.dp

internal const val TEST_TAG_ACTION_TOOL_TRAINING = "action_tool_training"

sealed interface ToolDetailsEvent {
    data object NavigateUp : ToolDetailsEvent
    class OpenTool(val tool: Tool?, val lang1: Locale?, val lang2: Locale?) : ToolDetailsEvent
    class OpenToolTraining(val tool: Tool?, val lang: Locale?) : ToolDetailsEvent
    class PinShortcut(val shortcut: PendingShortcut) : ToolDetailsEvent
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun ToolDetailsLayout(
    viewModel: ToolDetailsViewModel,
    modifier: Modifier = Modifier,
    onEvent: (ToolDetailsEvent) -> Unit = {},
) = DrawerMenuLayout(modifier) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = { onEvent(ToolDetailsEvent.NavigateUp) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                },
                actions = {
                    var showOverflow by remember { mutableStateOf(false) }
                    val shortcut = viewModel.shortcut.collectAsState().value

                    if (shortcut != null) {
                        IconButton(onClick = { showOverflow = !showOverflow }) {
                            Icon(Icons.Filled.MoreVert, null)
                        }
                        DropdownMenu(expanded = showOverflow, onDismissRequest = { showOverflow = false }) {
                            DropdownMenuItem(
                                text = { Text(stringResource(R.string.menu_add_to_home)) },
                                onClick = { onEvent(ToolDetailsEvent.PinShortcut(shortcut)) }
                            )
                        }
                    }
                },
                colors = GodToolsTheme.topAppBarColors,
            )
        }
    ) {
        ToolDetailsContent(viewModel, onEvent = onEvent, modifier = Modifier.padding(it))
    }
}

@Composable
@OptIn(ExperimentalFoundationApi::class)
private fun ToolDetailsContent(
    viewModel: ToolDetailsViewModel,
    modifier: Modifier = Modifier,
    toolViewModels: ToolViewModels = viewModel(key = "ToolDetailsContent"),
    onEvent: (ToolDetailsEvent) -> Unit = {},
) {
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    val toolCode by viewModel.toolCode.collectAsState()
    val toolViewModel = toolViewModels[toolCode.orEmpty()]
    val tool by toolViewModel.tool.collectAsState()
    val translation by toolViewModel.firstTranslation.collectAsState()
    val secondTranslation by toolViewModel.secondTranslation.collectAsState()
    val onEvent by rememberUpdatedState(onEvent)

    val context = LocalContext.current
    val appLocale = LocalAppLanguage.current
    val rawLanguages by toolViewModel.availableLanguages.collectAsState()
    val languages by remember(context, appLocale) {
        derivedStateOf { rawLanguages.getSortedDisplayNames(context, appLocale).toImmutableList() }
    }

    val eventSink: (Event) -> Unit = remember(viewModel) {
        {
            when (it) {
                Event.OpenTool -> onEvent(
                    ToolDetailsEvent.OpenTool(
                        tool,
                        translation.value?.languageCode,
                        secondTranslation?.languageCode
                    )
                )
                Event.OpenToolTraining ->
                    onEvent(ToolDetailsEvent.OpenToolTraining(tool, translation.value?.languageCode))
                is Event.SwitchVariant -> {
                    coroutineScope.launch { scrollState.animateScrollTo(0) }
                    viewModel.setToolCode(it.variant)
                }
                Event.PinTool -> toolViewModel.pinTool()
                Event.UnpinTool -> toolViewModel.unpinTool()
                else -> TODO()
            }
        }
    }

    val state = State(
        toolCode = toolCode,
        tool = tool,
        banner = toolViewModel.detailsBanner.collectAsState().value,
        bannerAnimation = toolViewModel.detailsBannerAnimation.collectAsState().value,
        downloadProgress = toolViewModel.downloadProgress.collectAsState().value,
        translation = translation.value,
        secondTranslation = secondTranslation,
        secondLanguage = toolViewModel.secondLanguage.collectAsState().value,
        manifest = toolViewModel.firstManifest.collectAsState().value,
        availableLanguages = languages,
        variants = viewModel.variants.collectAsState().value.mapNotNull {
            it.code?.let { code ->
                toolViewModels[code, it].toState(
                    eventSink = { e ->
                        when (e) {
                            ToolCard.Event.Click -> eventSink(Event.SwitchVariant(code))
                            else -> Unit
                        }
                    }
                )
            }
        },
        eventSink = eventSink
    )

    val secondLanguage by rememberUpdatedState(state.secondLanguage)
    val downloadProgress by rememberUpdatedState(state.downloadProgress)

    val pages by viewModel.pages.collectAsState()
    val pagerState = rememberPagerState { pages.size }

    DownloadLatestTranslation(toolCode, translation.value?.languageCode)
    DownloadLatestTranslation(toolCode, secondTranslation?.languageCode)

    toolCode?.let { RecordAnalyticsScreen(ToolDetailsScreenEvent(it)) }

    Column(
        modifier = modifier
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .verticalScroll(scrollState)
    ) {
        Surface(shadowElevation = 4.dp) {
            Column {
                Box(modifier = Modifier.fillMaxWidth()) {
                    ToolDetailsBanner(
                        state,
                        modifier = Modifier
                            .fillMaxWidth()
                            .aspectRatio(21f / 10f)
                    )

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

                Row(modifier = Modifier.padding(top = 10.dp, horizontal = TOOL_DETAILS_HORIZONTAL_MARGIN)) {
                    val shares by remember { derivedStateOf { tool?.shares ?: 0 } }
                    Text(
                        pluralStringResource(R.plurals.label_tools_shares, shares, shares),
                        style = MaterialTheme.typography.bodyMedium,
                    )

                    if (secondTranslation != null) {
                        Spacer(modifier = Modifier.weight(1f))
                        AvailableInLanguage(
                            language = secondLanguage,
                            color = GodToolsTheme.GT_DARK_GREEN,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }

                ToolDetailsActions(
                    state,
                    modifier = Modifier.padding(top = 16.dp, horizontal = TOOL_DETAILS_HORIZONTAL_MARGIN)
                )

                TabRow(
                    selectedTabIndex = pagerState.currentPage,
                    indicator = { positions ->
                        TabRowDefaults.SecondaryIndicator(Modifier.pagerTabIndicatorOffset(pagerState, positions))
                    },
                    divider = {},
                    modifier = Modifier.padding(horizontal = TOOL_DETAILS_HORIZONTAL_MARGIN)
                ) {
                    pages.forEachIndexed { index, page ->
                        Tab(
                            text = { Text(stringResource(page.tabLabel)) },
                            selected = pagerState.currentPage == index,
                            onClick = { coroutineScope.launch { pagerState.animateScrollToPage(index) } },
                        )
                    }
                }
            }
        }

        HorizontalPager(
            state = pagerState,
            verticalAlignment = Alignment.Top,
            key = { pages[it] }
        ) {
            when (pages[it]) {
                ToolDetailsPage.DESCRIPTION -> ToolDetailsAbout(state, modifier = Modifier.padding(32.dp))
                ToolDetailsPage.VARIANTS -> ToolDetailsVariants(state, modifier = Modifier.padding(16.dp))
            }
        }
    }
}

@Composable
private fun ToolDetailsBanner(state: State, modifier: Modifier = Modifier) {
    val youtubeVideo = state.tool?.detailsBannerYoutubeVideoId

    when {
        youtubeVideo != null -> YouTubePlayer(
            youtubeVideo,
            recue = true,
            modifier = modifier
        )
        state.bannerAnimation != null -> {
            val composition by rememberLottieComposition(LottieCompositionSpec.File(state.bannerAnimation.path))
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
        state.banner != null -> AsyncImage(
            model = state.banner,
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = modifier
        )
        else -> Spacer(modifier = modifier.background(GodToolsTheme.GRAY_E6))
    }
}

@Composable
@VisibleForTesting
internal fun ToolDetailsActions(state: State, modifier: Modifier = Modifier) = Column(modifier = modifier) {
    val tool by rememberUpdatedState(state.tool)
    val eventSink by rememberUpdatedState(state.eventSink)

    Button(
        onClick = { eventSink(Event.OpenTool) },
        modifier = Modifier.fillMaxWidth()
    ) { Text(stringResource(R.string.action_tools_open_tool)) }

    val manifest by rememberUpdatedState(state.manifest)
    if (manifest?.hasTips == true) {
        Button(
            onClick = { eventSink(Event.OpenToolTraining) },
            modifier = Modifier
                .testTag(TEST_TAG_ACTION_TOOL_TRAINING)
                .fillMaxWidth()
        ) { Text(stringResource(R.string.action_tools_open_training)) }
    }

    val isFavorite by remember { derivedStateOf { tool?.isFavorite == true } }
    OutlinedButton(
        onClick = { eventSink(if (isFavorite) Event.UnpinTool else Event.PinTool) },
        colors = ButtonDefaults.outlinedButtonColors(
            contentColor = if (isFavorite) GodToolsTheme.GT_RED else MaterialTheme.colorScheme.primary
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(
            painterResource(if (isFavorite) R.drawable.ic_favorite_border_24dp else R.drawable.ic_favorite_24dp),
            contentDescription = null
        )
        Text(
            stringResource(
                if (isFavorite) R.string.action_tools_remove_favorite else R.string.action_tools_add_favorite
            ),
            modifier = Modifier.padding(start = 4.dp)
        )
    }
}

@Composable
private fun ToolDetailsVariants(state: State, modifier: Modifier = Modifier) {
    val currentTool by rememberUpdatedState(state.toolCode)
    val variants by rememberUpdatedState(state.variants)

    Column(modifier = modifier, verticalArrangement = spacedBy(16.dp)) {
        Text(
            stringResource(R.string.tool_details_section_variants_description),
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(horizontal = 16.dp)
        )

        variants.forEach {
            ReusableContent(it.toolCode) {
                VariantToolCard(
                    state = it,
                    isSelected = currentTool == it.toolCode,
                )
            }
        }
    }
}
