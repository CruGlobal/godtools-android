package org.cru.godtools.ui.dashboard.personalization

import androidx.annotation.StringRes
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource

@Composable
internal fun PersonalizationToggle(
    isPersonalized: Boolean,
    @StringRes personalizedLabel: Int,
    @StringRes allLabel: Int,
    onSelect: (personalized: Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    SingleChoiceSegmentedButtonRow(modifier = modifier) {
        SegmentedButton(
            selected = isPersonalized,
            onClick = { onSelect(true) },
            shape = SegmentedButtonDefaults.itemShape(0, 2),
            icon = {},
        ) {
            Text(stringResource(personalizedLabel))
        }

        SegmentedButton(
            selected = !isPersonalized,
            onClick = { onSelect(false) },
            shape = SegmentedButtonDefaults.itemShape(1, 2),
            icon = {},
        ) {
            Text(stringResource(allLabel))
        }
    }
}
