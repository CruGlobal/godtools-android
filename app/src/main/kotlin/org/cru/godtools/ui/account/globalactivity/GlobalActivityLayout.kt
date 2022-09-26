package org.cru.godtools.ui.account.globalactivity

import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import java.util.Calendar
import org.ccci.gto.android.common.androidx.compose.foundation.layout.padding
import org.ccci.gto.android.common.androidx.compose.foundation.text.minLinesHeight
import org.ccci.gto.android.common.androidx.compose.ui.text.computeHeightForDefaultText
import org.ccci.gto.android.common.util.format
import org.cru.godtools.R

private val ACCOUNT_PAGE_MARGIN_HORIZONTAL = 16.dp

@Composable
fun AccountGlobalActivityLayout() = Column(modifier = Modifier.padding(horizontal = ACCOUNT_PAGE_MARGIN_HORIZONTAL)) {
    val viewModel = viewModel<GlobalActivityViewModel>()
    val activity by viewModel.activity.collectAsState()

    Text(
        stringResource(R.string.profile_global_activity_heading, Calendar.getInstance().get(Calendar.YEAR)),
        style = MaterialTheme.typography.titleLarge,
        modifier = Modifier.padding(top = 32.dp)
    )

    Row(
        horizontalArrangement = spacedBy(16.dp),
        modifier = Modifier.padding(top = 16.dp)
    ) {
        GlobalActivityCard(
            stringResource(R.string.profile_activity_unique_users),
            activity.users,
            modifier = Modifier.weight(1f)
        )
        GlobalActivityCard(
            stringResource(R.string.profile_activity_gospel_presentations),
            activity.gospelPresentations,
            modifier = Modifier.weight(1f)
        )
    }
    Row(horizontalArrangement = spacedBy(16.dp), modifier = Modifier.padding(vertical = 16.dp)) {
        GlobalActivityCard(
            stringResource(R.string.profile_activity_sessions),
            activity.launches,
            modifier = Modifier.weight(1f)
        )
        GlobalActivityCard(
            stringResource(R.string.profile_activity_countries),
            activity.countries,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun GlobalActivityCard(
    label: String,
    count: Int,
    modifier: Modifier = Modifier
) = ElevatedCard(modifier = modifier.heightIn(min = 128.dp)) {
    val labelStyle = MaterialTheme.typography.bodyMedium

    Spacer(Modifier.weight(1f))
    Spacer(Modifier.height(computeHeightForDefaultText(labelStyle, 2) - computeHeightForDefaultText(labelStyle, 1)))
    Text(
        count.format(),
        style = MaterialTheme.typography.headlineSmall,
        fontWeight = FontWeight.Bold,
        modifier = Modifier
            .align(Alignment.CenterHorizontally)
            .padding(top = 8.dp, horizontal = 16.dp)
            .alpha(0.63f)
    )
    Text(
        label,
        style = MaterialTheme.typography.bodyMedium,
        textAlign = TextAlign.Center,
        modifier = Modifier
            .align(Alignment.CenterHorizontally)
            .padding(top = 8.dp, bottom = 8.dp, horizontal = 16.dp)
            .minLinesHeight(2, textStyle = labelStyle)
    )
    Spacer(Modifier.weight(1f))
}
