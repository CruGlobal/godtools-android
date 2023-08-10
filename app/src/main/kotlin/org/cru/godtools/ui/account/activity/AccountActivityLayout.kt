package org.cru.godtools.ui.account.activity

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import org.ccci.gto.android.common.androidx.compose.foundation.layout.padding
import org.ccci.gto.android.common.androidx.compose.material3.isLight
import org.ccci.gto.android.common.util.format
import org.cru.godtools.R
import org.cru.godtools.shared.common.model.ThemeType
import org.cru.godtools.shared.user.activity.model.IconColors
import org.cru.godtools.shared.user.activity.model.UserActivity
import org.cru.godtools.ui.account.ACCOUNT_PAGE_MARGIN_HORIZONTAL

@Composable
fun AccountActivityLayout(
    modifier: Modifier = Modifier
) = Column(modifier = modifier.padding(horizontal = ACCOUNT_PAGE_MARGIN_HORIZONTAL)) {
    val viewModel = viewModel<AccountActivityViewModel>()
    val activity by viewModel.userActivity.collectAsState()

    AccountActivity(activity, modifier = Modifier.padding(top = 32.dp))
    AccountActivityBadges(activity, modifier = Modifier.padding(vertical = 32.dp))
}

@Composable
private fun AccountActivity(activity: UserActivity, modifier: Modifier = Modifier) = Column(modifier = modifier) {
    Text(
        stringResource(R.string.account_activity_header),
        style = MaterialTheme.typography.titleLarge,
    )
    ElevatedCard(modifier = Modifier.padding(top = 8.dp)) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(all = 16.dp)
        ) {
            AccountActivityItem(
                label = pluralStringResource(R.plurals.account_activity_tool_opens, activity.toolOpens),
                icon = painterResource(R.drawable.ic_all_tools),
                count = activity.toolOpens,
                colors = UserActivity.Colors.toolOpens,
                modifier = Modifier.weight(1f)
            )
            AccountActivityItem(
                label = pluralStringResource(R.plurals.account_activity_lesson_completions, activity.lessonCompletions),
                icon = painterResource(R.drawable.ic_lessons),
                count = activity.lessonCompletions,
                colors = UserActivity.Colors.lessonCompletions,
                modifier = Modifier.weight(1f)
            )
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(horizontal = 16.dp, bottom = 16.dp)
        ) {
            AccountActivityItem(
                label = pluralStringResource(R.plurals.account_activity_screen_shares, activity.screenShares),
                icon = painterResource(org.cru.godtools.tool.tract.R.drawable.ic_tract_live_share),
                count = activity.screenShares,
                colors = UserActivity.Colors.screenShares,
                modifier = Modifier.weight(1f)
            )
            AccountActivityItem(
                label = pluralStringResource(R.plurals.account_activity_links_shared, activity.linksShared),
                icon = painterResource(org.cru.godtools.tool.R.drawable.ic_share),
                count = activity.linksShared,
                colors = UserActivity.Colors.linksShared,
                modifier = Modifier.weight(1f)
            )
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(horizontal = 16.dp, bottom = 16.dp)
        ) {
            AccountActivityItem(
                label = pluralStringResource(R.plurals.account_activity_languages_used, activity.languagesUsed),
                icon = painterResource(R.drawable.ic_language),
                count = activity.languagesUsed,
                colors = UserActivity.Colors.languagesUsed,
                modifier = Modifier.weight(1f)
            )
            AccountActivityItem(
                label = pluralStringResource(R.plurals.account_activity_sessions, activity.sessions),
                icon = painterResource(R.drawable.ic_activity_sessions),
                count = activity.sessions,
                colors = UserActivity.Colors.sessions,
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun AccountActivityItem(
    label: String,
    icon: Painter,
    count: Int,
    colors: IconColors,
    modifier: Modifier = Modifier
) = Row(
    verticalAlignment = Alignment.CenterVertically,
    modifier = modifier
) {
    val themeType = when {
        MaterialTheme.colorScheme.isLight -> ThemeType.LIGHT
        else -> ThemeType.DARK
    }

    Icon(
        icon,
        contentDescription = null,
        tint = colors.color(themeType),
        modifier = Modifier
            .align(Alignment.Top)
            .padding(end = 8.dp)
            .size(44.dp)
            .background(colors.containerColor(themeType), CircleShape)
            .padding(10.dp)
    )
    Column(modifier = Modifier.weight(1f)) {
        Text(
            count.format(),
            color = colors.color(themeType),
            fontWeight = FontWeight.SemiBold,
            maxLines = 1,
            style = MaterialTheme.typography.titleLarge,
        )
        Text(
            label,
            overflow = TextOverflow.Ellipsis,
            maxLines = 2,
            style = MaterialTheme.typography.labelSmall,
        )
    }
}
