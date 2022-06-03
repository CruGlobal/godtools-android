package org.cru.godtools.ui.dashboard.lessons

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.cru.godtools.R
import org.cru.godtools.base.ui.theme.GodToolsTheme

@Composable
@Preview(showBackground = true)
fun LessonsHeader() = GodToolsTheme {
    CompositionLocalProvider(
        LocalContentColor provides contentColorFor(MaterialTheme.colorScheme.background)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
                stringResource(R.string.dashboard_lessons_header_title),
                style = MaterialTheme.typography.titleLarge
            )
            Text(
                stringResource(R.string.dashboard_lessons_header_description),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
