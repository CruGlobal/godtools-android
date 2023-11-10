package org.cru.godtools.ui.account.delete

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import org.cru.godtools.base.ui.theme.GodToolsTheme

@Preview
@Composable
private fun DeleteAccountLayoutDisplayPreview() {
    GodToolsTheme { DeleteAccountLayout(DeleteAccountScreen.State.Display { }) }
}

@Preview
@Composable
private fun DeleteAccountLayoutErrorPreview() {
    GodToolsTheme { DeleteAccountLayout(DeleteAccountScreen.State.Error { }) }
}
