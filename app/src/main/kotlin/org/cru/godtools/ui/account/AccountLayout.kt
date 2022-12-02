package org.cru.godtools.ui.account

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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
import com.google.accompanist.pager.ExperimentalPagerApi
import com.google.accompanist.pager.HorizontalPager
import com.google.accompanist.pager.rememberPagerState
import com.google.accompanist.swiperefresh.SwipeRefresh
import com.google.accompanist.swiperefresh.rememberSwipeRefreshState
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import kotlinx.coroutines.launch
import org.ccci.gto.android.common.androidx.compose.foundation.layout.padding
import org.ccci.gto.android.common.androidx.compose.material3.ui.tabs.pagerTabIndicatorOffset
import org.ccci.gto.android.common.androidx.compose.ui.draw.invisibleIf
import org.cru.godtools.R
import org.cru.godtools.analytics.compose.RecordAnalyticsScreen
import org.cru.godtools.analytics.model.AnalyticsScreenEvent
import org.cru.godtools.shared.analytics.AnalyticsScreenNames
import org.cru.godtools.ui.account.activity.AccountActivityLayout
import org.cru.godtools.ui.account.globalactivity.AccountGlobalActivityLayout

internal val ACCOUNT_PAGE_MARGIN_HORIZONTAL = 16.dp

@Composable
@OptIn(ExperimentalMaterial3Api::class, ExperimentalPagerApi::class)
internal fun AccountLayout(onEvent: (AccountLayoutEvent) -> Unit = {}) {
    val coroutineScope = rememberCoroutineScope()
    val viewModel = viewModel<AccountViewModel>()

    val scrollState = rememberScrollState()
    val pagerState = rememberPagerState()
    val pages by viewModel.pages.collectAsState()

    RecordAccountPageAnalytics(pages.getOrNull(pagerState.currentPage))
    SwipeRefresh(
        rememberSwipeRefreshState(viewModel.isSyncRunning.collectAsState().value),
        onRefresh = { viewModel.triggerSync(true) }
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .background(MaterialTheme.colorScheme.surfaceVariant)
                .verticalScroll(scrollState)
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
                        colors = TopAppBarDefaults.smallTopAppBarColors(
                            navigationIconContentColor = MaterialTheme.colorScheme.primary
                        )
                    )

                    val user by viewModel.user.collectAsState()
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

            HorizontalPager(
                count = pages.size,
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
