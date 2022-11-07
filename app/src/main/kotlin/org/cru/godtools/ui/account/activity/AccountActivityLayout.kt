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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import org.ccci.gto.android.common.androidx.compose.foundation.layout.padding
import org.ccci.gto.android.common.androidx.compose.material3.isLight
import org.ccci.gto.android.common.util.format
import org.cru.godtools.R
import org.cru.godtools.shared.user.activity.model.UserActivity
import org.cru.godtools.ui.account.ACCOUNT_PAGE_MARGIN_HORIZONTAL
import palettes.TonalPalette

@Composable
fun AccountActivityLayout(
    modifier: Modifier = Modifier
) = Column(modifier = modifier.padding(horizontal = ACCOUNT_PAGE_MARGIN_HORIZONTAL)) {
    val viewModel = viewModel<AccountActivityViewModel>()
    val activity by viewModel.userActivity.collectAsState()

    AccountActivity(activity, modifier = Modifier.padding(top = 32.dp))
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
                label = stringResource(R.string.account_activity_tool_opens),
                icon = painterResource(R.drawable.ic_all_tools),
                count = activity.toolOpens,
                color = Color(0xFF05699B),
                modifier = Modifier.weight(1f)
            )
            AccountActivityItem(
                label = stringResource(R.string.account_activity_lesson_completions),
                icon = painterResource(R.drawable.ic_lessons),
                count = activity.lessonCompletions,
                color = Color(0xFFA6EDE8),
                modifier = Modifier.weight(1f)
            )
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(horizontal = 16.dp, bottom = 16.dp)
        ) {
            AccountActivityItem(
                label = stringResource(R.string.account_activity_screen_shares),
                icon = painterResource(org.cru.godtools.tool.tract.R.drawable.ic_tract_live_share),
                count = activity.screenShares,
                color = Color(0xFFE55B36),
                modifier = Modifier.weight(1f)
            )
            AccountActivityItem(
                label = stringResource(R.string.account_activity_links_shared),
                icon = painterResource(org.cru.godtools.tool.R.drawable.ic_share),
                count = activity.linksShared,
                color = Color(0xFF2F3676),
                modifier = Modifier.weight(1f)
            )
        }
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier.padding(horizontal = 16.dp, bottom = 16.dp)
        ) {
            AccountActivityItem(
                label = stringResource(R.string.account_activity_languages_used),
                icon = painterResource(R.drawable.ic_language),
                count = activity.languagesUsed,
                color = Color(0xFFCEFFC1),
                modifier = Modifier.weight(1f)
            )
            AccountActivityItem(
                label = stringResource(R.string.account_activity_sessions),
                icon = painterResource(R.drawable.ic_activity_sessions),
                count = activity.sessions,
                color = Color(0xFFE0CE26),
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
    color: Color,
    modifier: Modifier = Modifier
) = Row(
    verticalAlignment = Alignment.CenterVertically,
    modifier = modifier
) {
    val palette = remember(color) { TonalPalette.fromInt(color.toArgb()) }
    val (primaryColor, primaryContainerColor) = when {
        MaterialTheme.colorScheme.isLight -> Pair(Color(palette.tone(40)), Color(palette.tone(90)))
        else -> Pair(Color(palette.tone(80)), Color(palette.tone(30)))
    }

    Icon(
        icon,
        contentDescription = null,
        tint = primaryColor,
        modifier = Modifier
            .align(Alignment.Top)
            .padding(end = 8.dp)
            .size(44.dp)
            .background(primaryContainerColor, CircleShape)
            .padding(10.dp)
    )
    Column(modifier = Modifier.weight(1f)) {
        Text(
            count.format(),
            color = primaryColor,
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
