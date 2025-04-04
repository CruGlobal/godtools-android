package org.cru.godtools.ui.account.globalactivity

import androidx.compose.foundation.layout.Arrangement.spacedBy
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import java.time.Year
import org.ccci.gto.android.common.androidx.compose.foundation.layout.padding
import org.ccci.gto.android.common.androidx.compose.ui.text.computeHeightForDefaultText
import org.ccci.gto.android.common.util.format
import org.cru.godtools.R

@Composable
fun GlobalActivityLayout(modifier: Modifier = Modifier, viewModel: GlobalActivityViewModel = viewModel()) {
    GlobalActivityLayout(GlobalActivityScreen.UiState(viewModel.activity.collectAsState().value), modifier)
}

@Composable
@OptIn(ExperimentalLayoutApi::class)
fun GlobalActivityLayout(state: GlobalActivityScreen.UiState, modifier: Modifier = Modifier) = Column(modifier) {
    Text(
        stringResource(R.string.profile_global_activity_heading, Year.now().value),
        style = MaterialTheme.typography.titleLarge,
        modifier = Modifier.padding(top = 32.dp)
    )

    FlowRow(
        maxItemsInEachRow = 2,
        horizontalArrangement = spacedBy(16.dp),
        verticalArrangement = spacedBy(16.dp),
        modifier = Modifier.padding(top = 8.dp, bottom = 16.dp)
    ) {
        GlobalActivityCard(
            stringResource(R.string.profile_activity_unique_users),
            state.activity.users,
            modifier = Modifier.weight(1f)
        )
        GlobalActivityCard(
            stringResource(R.string.profile_activity_gospel_presentations),
            state.activity.gospelPresentations,
            modifier = Modifier.weight(1f)
        )
        GlobalActivityCard(
            stringResource(R.string.profile_activity_sessions),
            state.activity.launches,
            modifier = Modifier.weight(1f)
        )
        GlobalActivityCard(
            stringResource(R.string.profile_activity_countries),
            state.activity.countries,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun GlobalActivityCard(label: String, count: Int, modifier: Modifier = Modifier) = ElevatedCard(
    modifier = modifier.heightIn(min = 128.dp)
) {
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
        style = labelStyle,
        textAlign = TextAlign.Center,
        minLines = 2,
        modifier = Modifier
            .align(Alignment.CenterHorizontally)
            .padding(top = 8.dp, bottom = 8.dp, horizontal = 16.dp)
    )
    Spacer(Modifier.weight(1f))
}
