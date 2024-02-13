package org.cru.godtools.tutorial.layout

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisallowComposableCalls
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import com.google.accompanist.pager.HorizontalPagerIndicator
import kotlinx.coroutines.launch
import org.ccci.gto.android.common.androidx.compose.material3.ui.appbar.AppBarActionButton
import org.ccci.gto.android.common.androidx.compose.ui.draw.invisibleIf
import org.cru.godtools.analytics.compose.RecordAnalyticsScreen
import org.cru.godtools.base.LocalAppLanguage
import org.cru.godtools.tutorial.Action
import org.cru.godtools.tutorial.Page
import org.cru.godtools.tutorial.PageSet
import org.cru.godtools.tutorial.R
import org.cru.godtools.tutorial.analytics.model.TutorialAnalyticsScreenEvent

@Composable
@OptIn(ExperimentalFoundationApi::class)
internal fun TutorialLayout(pageSet: PageSet, onTutorialAction: (Action) -> Unit = {}) {
    val coroutineScope = rememberCoroutineScope()
    val locale = LocalAppLanguage.current
    val pages = pageSet.pages

    val pagerState = rememberPagerState { pages.size }
    val currentPage by remember { derivedStateOf { pages[pagerState.currentPage] } }

    RecordAnalyticsScreen(TutorialAnalyticsScreenEvent(pageSet, currentPage, pagerState.currentPage, locale))

    Scaffold(
        topBar = {
            TutorialAppBar(
                pageSet,
                currentPage = { currentPage },
                onTutorialAction = onTutorialAction
            )
        },
        bottomBar = {
            val indicatorVisible by remember { derivedStateOf { currentPage.showIndicator } }
            HorizontalPagerIndicator(
                pagerState = pagerState,
                pageCount = pagerState.pageCount,
                activeColor = MaterialTheme.colorScheme.primary,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(dimensionResource(R.dimen.tutorial_indicator_height))
                    .wrapContentSize()
                    .invisibleIf { !indicatorVisible }
            )
        }
    ) { insets ->
        HorizontalPager(
            key = { pages[it] },
            state = pagerState,
            modifier = Modifier
                .padding(insets)
                .fillMaxSize()
        ) { i ->
            TutorialPageLayout(
                pages[i],
                nextPage = { coroutineScope.launch { pagerState.animateScrollToPage(i + 1) } },
                onTutorialAction = onTutorialAction
            )
        }
    }
}

@Composable
@OptIn(ExperimentalMaterial3Api::class)
private inline fun TutorialAppBar(
    pageSet: PageSet,
    crossinline currentPage: @DisallowComposableCalls () -> Page,
    crossinline onTutorialAction: (Action) -> Unit,
) = TopAppBar(
    title = {},
    navigationIcon = {
        if (pageSet.showUpNavigation) {
            IconButton(onClick = { onTutorialAction(Action.BACK) }) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
            }
        }
    },
    actions = {
        val showMenu by remember { derivedStateOf { currentPage().showMenu } }
        if (showMenu) {
            pageSet.menu.forEach { (item, action) ->
                AppBarActionButton(item, onClick = { onTutorialAction(action) })
            }
        }
    },
    colors = TopAppBarDefaults.topAppBarColors(
        navigationIconContentColor = MaterialTheme.colorScheme.primary,
        actionIconContentColor = MaterialTheme.colorScheme.primary
    ),
)
