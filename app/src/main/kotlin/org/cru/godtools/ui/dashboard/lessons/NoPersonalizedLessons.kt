package org.cru.godtools.ui.dashboard.lessons

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.cru.godtools.R

@Composable
internal fun NoPersonalizedLessons(onGoToAllLessons: () -> Unit, modifier: Modifier = Modifier) {
    Surface(
        color = MaterialTheme.colorScheme.surfaceVariant,
        modifier = modifier.fillMaxWidth(),
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(24.dp),
        ) {
            Icon(
                painter = painterResource(R.drawable.ic_priority_high),
                contentDescription = null,
                modifier = Modifier.size(64.dp),
            )
            Text(
                text = stringResource(R.string.dashboard_lessons_section_personalized_no_lessons_title),
                style = MaterialTheme.typography.titleMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 16.dp),
            )
            Text(
                text = stringResource(R.string.dashboard_lessons_section_personalized_no_lessons_description),
                style = MaterialTheme.typography.bodyMedium,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(top = 8.dp),
            )
            Button(
                onClick = onGoToAllLessons,
                modifier = Modifier.padding(top = 16.dp),
            ) {
                Text(stringResource(R.string.dashboard_lessons_section_personalized_no_lessons_action_all_lessons))
            }
        }
    }
}
