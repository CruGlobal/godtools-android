package org.cru.godtools.tutorial.layout

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.dimensionResource
import com.google.accompanist.pager.HorizontalPagerIndicator
import com.slack.circuit.codegen.annotations.CircuitInject
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.launch
import org.ccci.gto.android.common.androidx.compose.material3.ui.appbar.AppBarActionButton
import org.cru.godtools.analytics.compose.RecordAnalyticsScreen
import org.cru.godtools.base.LocalAppLanguage
import org.cru.godtools.base.ui.theme.GodToolsTheme
import org.cru.godtools.tutorial.R
import org.cru.godtools.tutorial.analytics.model.TutorialAnalyticsScreenEvent
import org.cru.godtools.tutorial.layout.TutorialPresenter.UiEvent
import org.cru.godtools.tutorial.layout.TutorialPresenter.UiState
import org.cru.godtools.tutorial.theme.TutorialThemeOverlay
import org.cru.godtools.tutorial.theme.tutorialAppBarColors
import org.cru.godtools.tutorial.theme.tutorialBackgroundColor

internal const val TEST_TAG_NAVIGATE_UP = "navigate_up"
internal const val TEST_TAG_PAGE_INDICATOR = "page_indicator"

@Composable
@CircuitInject(TutorialScreen::class, SingletonComponent::class)
fun TutorialLayout(state: UiState, modifier: Modifier = Modifier) {
    val pageSet = state.pageSet
    val eventSink by rememberUpdatedState(state.eventSink)

    val coroutineScope = rememberCoroutineScope()
    val locale = LocalAppLanguage.current
    val pages = remember(pageSet, locale) { pageSet.pagesFor(locale) }
    val pagerState = rememberPagerState { pages.size }
    val currentPage by remember { derivedStateOf { pages[pagerState.currentPage] } }

    RecordAnalyticsScreen(TutorialAnalyticsScreenEvent(pageSet, currentPage, pagerState.currentPage, locale))

    TutorialThemeOverlay {
        Scaffold(
            topBar = {
                TutorialAppBar(
                    showUpNavigation = pageSet.showUpNavigation,
                    onNavigateUp = { eventSink(UiEvent.Back) },
                    actions = {
                        val showMenu by remember { derivedStateOf { currentPage.showMenu } }
                        if (showMenu) {
                            pageSet.menu.forEach { (item, event) ->
                                AppBarActionButton(item, onClick = { eventSink(event) })
                            }
                        }
                    }
                )
            },
            bottomBar = {
                if (pageSet.showPageIndicator) {
                    HorizontalPagerIndicator(
                        pagerState = pagerState,
                        pageCount = pagerState.pageCount,
                        activeColor = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .testTag(TEST_TAG_PAGE_INDICATOR)
                            .navigationBarsPadding()
                            .fillMaxWidth()
                            .height(dimensionResource(R.dimen.tutorial_indicator_height))
                            .wrapContentSize()
                    )
                }
            },
            containerColor = GodToolsTheme.tutorialBackgroundColor,
            contentColor = MaterialTheme.colorScheme.onBackground,
            modifier = modifier,
        ) { insets ->
            HorizontalPager(
                key = { pages[it] },
                state = pagerState,
                modifier = Modifier
                    .padding(insets)
                    .consumeWindowInsets(insets)
                    .fillMaxSize()
            ) { i ->
                TutorialPageLayout(
                    pages[i],
                    nextPage = {
                        coroutineScope.launch {
                            pagerState.animateScrollToPage(
                                i + 1,
                                animationSpec = spring(stiffness = Spring.StiffnessMediumLow)
                            )
                        }
                    },
                    eventSink = eventSink,
                )
            }
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
fun TutorialAppBar(
    modifier: Modifier = Modifier,
    showUpNavigation: Boolean = true,
    onNavigateUp: () -> Unit = {},
    actions: @Composable RowScope.() -> Unit = {},
) = TopAppBar(
    title = {},
    navigationIcon = {
        if (showUpNavigation) {
            IconButton(
                onClick = onNavigateUp,
                modifier = Modifier.testTag(TEST_TAG_NAVIGATE_UP),
            ) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
            }
        }
    },
    actions = actions,
    colors = GodToolsTheme.tutorialAppBarColors,
    modifier = modifier
)
