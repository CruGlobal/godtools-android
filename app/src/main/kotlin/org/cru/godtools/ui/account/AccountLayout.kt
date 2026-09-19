package org.cru.godtools.ui.account

import androidx.annotation.VisibleForTesting
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SecondaryTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults
import androidx.compose.material3.pulltorefresh.pullToRefresh
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.slack.circuit.codegen.annotations.CircuitInject
import dagger.hilt.components.SingletonComponent
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.ccci.gto.android.common.compose.foundation.layout.padding
import org.ccci.gto.android.common.compose.ui.draw.invisibleIf
import org.cru.godtools.R
import org.cru.godtools.analytics.compose.RecordAnalyticsScreen
import org.cru.godtools.analytics.model.AnalyticsScreenEvent
import org.cru.godtools.model.User
import org.cru.godtools.shared.analytics.AnalyticsScreenNames
import org.cru.godtools.ui.account.AccountPresenter.UiEvent
import org.cru.godtools.ui.account.AccountPresenter.UiState
import org.cru.godtools.ui.account.activity.AccountActivityLayout
import org.cru.godtools.ui.account.globalactivity.GlobalActivityLayout
import org.cru.godtools.ui.drawer.DrawerMenuLayout

internal val ACCOUNT_PAGE_MARGIN_HORIZONTAL = 16.dp

@Composable
@CircuitInject(AccountScreen::class, SingletonComponent::class)
internal fun AccountLayout(state: UiState, modifier: Modifier = Modifier) =
    AccountLayout(state, rememberPagerState { state.pages.size }, modifier)

@Composable
@VisibleForTesting
@OptIn(ExperimentalMaterial3Api::class)
internal fun AccountLayout(state: UiState, pagerState: PagerState, modifier: Modifier = Modifier) =
    DrawerMenuLayout(state.drawerState, modifier) {
        val pages by rememberUpdatedState(state.pages)
        val eventSink by rememberUpdatedState(state.eventSink)

        val refreshState = rememberPullToRefreshState()

        RecordAccountPageAnalytics(pages.getOrNull(pagerState.currentPage))
        Scaffold {
            Box(
                Modifier
                    .padding(it)
                    .consumeWindowInsets(it)
                    .pullToRefresh(state.isSyncRunning, refreshState, onRefresh = { eventSink(UiEvent.TriggerSync) })
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .verticalScroll(rememberScrollState())
                ) {
                    AccountLayoutHeader(
                        user = state.user,
                        pages = pages,
                        pagerState = pagerState,
                        eventSink = eventSink,
                    )
                    HorizontalPager(
                        state = pagerState,
                        verticalAlignment = Alignment.Top,
                        key = { pages[it] },
                    ) {
                        @Suppress("ktlint:standard:blank-line-between-when-conditions")
                        when (pages[it]) {
                            AccountPage.ACTIVITY -> AccountActivityLayout(state.userActivity)
                            AccountPage.GLOBAL_ACTIVITY -> GlobalActivityLayout(
                                state.globalActivity,
                                Modifier.padding(horizontal = ACCOUNT_PAGE_MARGIN_HORIZONTAL)
                            )
                        }
                    }
                }

                PullToRefreshDefaults.Indicator(
                    state = refreshState,
                    isRefreshing = state.isSyncRunning,
                    modifier = Modifier.align(Alignment.TopCenter)
                )
            }
        }
    }

@Composable
@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
private fun AccountLayoutHeader(
    modifier: Modifier = Modifier,
    user: User? = null,
    pages: List<AccountPage> = emptyList(),
    coroutineScope: CoroutineScope = rememberCoroutineScope(),
    pagerState: PagerState = rememberPagerState { pages.size },
    eventSink: (UiEvent) -> Unit = {},
) {
    Surface(shadowElevation = 4.dp, modifier = modifier) {
        Column {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = { eventSink(UiEvent.NavigateUp) }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    navigationIconContentColor = MaterialTheme.colorScheme.primary
                )
            )

            Text(
                user?.name.orEmpty(),
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center,
                maxLines = 1,
                modifier = Modifier
                    .padding(top = 40.dp, horizontal = ACCOUNT_PAGE_MARGIN_HORIZONTAL)
                    .align(Alignment.CenterHorizontally)
            )
            Text(
                stringResource(
                    R.string.account_joined,
                    user?.createdAt?.atZone(ZoneId.systemDefault())
                        ?.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG))
                        .orEmpty()
                ),
                style = MaterialTheme.typography.labelSmall,
                maxLines = 1,
                modifier = Modifier
                    .padding(top = 8.dp, horizontal = ACCOUNT_PAGE_MARGIN_HORIZONTAL)
                    .align(Alignment.CenterHorizontally)
                    .invisibleIf(user?.createdAt == null)
            )

            SecondaryTabRow(
                selectedTabIndex = pagerState.currentPage,
                divider = {},
                modifier = Modifier.padding(top = 12.dp, horizontal = ACCOUNT_PAGE_MARGIN_HORIZONTAL)
                // TODO: set the correct padding
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
}

@Composable
private fun RecordAccountPageAnalytics(page: AccountPage?) {
    val screen = when (page) {
        AccountPage.ACTIVITY -> AnalyticsScreenEvent(AnalyticsScreenNames.ACCOUNT_ACTIVITY)
        AccountPage.GLOBAL_ACTIVITY -> AnalyticsScreenEvent(AnalyticsScreenNames.ACCOUNT_GLOBAL_ACTIVITY)
        else -> null
    }
    if (screen != null) RecordAnalyticsScreen(screen)
}
