package org.cru.godtools.ui.account

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import org.ccci.gto.android.common.androidx.compose.foundation.layout.padding
import org.ccci.gto.android.common.androidx.compose.material3.ui.pullrefresh.PullRefreshIndicator
import org.ccci.gto.android.common.androidx.compose.material3.ui.tabs.pagerTabIndicatorOffset
import org.ccci.gto.android.common.androidx.compose.ui.draw.invisibleIf
import org.cru.godtools.R
import org.cru.godtools.analytics.compose.RecordAnalyticsScreen
import org.cru.godtools.analytics.model.AnalyticsScreenEvent
import org.cru.godtools.model.User
import org.cru.godtools.shared.analytics.AnalyticsScreenNames
import org.cru.godtools.ui.account.activity.AccountActivityLayout
import org.cru.godtools.ui.account.globalactivity.AccountGlobalActivityLayout

internal val ACCOUNT_PAGE_MARGIN_HORIZONTAL = 16.dp

@Composable
@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterialApi::class)
internal fun AccountLayout(onEvent: (AccountLayoutEvent) -> Unit = {}) {
    val viewModel = viewModel<AccountViewModel>()
    val user by viewModel.user.collectAsState()
    val pages by viewModel.pages.collectAsState()
    val refreshing by viewModel.isSyncRunning.collectAsState()

    val pagerState = rememberPagerState()
    val refreshState = rememberPullRefreshState(refreshing, onRefresh = { viewModel.triggerSync(true) })

    RecordAccountPageAnalytics(pages.getOrNull(pagerState.currentPage))
    Box(Modifier.pullRefresh(refreshState)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .verticalScroll(rememberScrollState())
        ) {
            AccountLayoutHeader(
                user = user,
                pages = pages,
                pagerState = pagerState,
                onEvent = onEvent,
            )
            HorizontalPager(
                pageCount = pages.size,
                state = pagerState,
                verticalAlignment = Alignment.Top,
                key = { pages[it] }
            ) {
                when (pages[it]) {
                    AccountPage.ACTIVITY -> AccountActivityLayout()
                    AccountPage.GLOBAL_ACTIVITY -> AccountGlobalActivityLayout()
                }
            }
        }

        PullRefreshIndicator(refreshing, refreshState, modifier = Modifier.align(Alignment.TopCenter))
    }
}

@Composable
@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
private fun AccountLayoutHeader(
    user: User? = null,
    pages: List<AccountPage> = emptyList(),
    onEvent: (AccountLayoutEvent) -> Unit = {},
    coroutineScope: CoroutineScope = rememberCoroutineScope(),
    pagerState: PagerState = rememberPagerState(),
) {
    Surface(shadowElevation = 4.dp) {
        Column {
            TopAppBar(
                title = {},
                navigationIcon = {
                    IconButton(onClick = { onEvent(AccountLayoutEvent.ACTION_UP) }) {
                        Icon(Icons.Filled.ArrowBack, null)
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
                    .invisibleIf { user?.createdAt == null }
            )

            TabRow(
                selectedTabIndex = pagerState.currentPage,
                indicator = { positions ->
                    TabRowDefaults.Indicator(Modifier.pagerTabIndicatorOffset(pagerState, positions))
                },
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

internal enum class AccountLayoutEvent { ACTION_UP }
